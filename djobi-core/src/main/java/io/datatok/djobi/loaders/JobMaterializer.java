package io.datatok.djobi.loaders;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.bags.ParameterBag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class JobMaterializer {

    @Inject
    private TemplateUtils templateUtils;

    private final SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yy");
    private final SimpleDateFormat monthParser = new SimpleDateFormat("MM/yy");

    private final Calendar calendar = Calendar.getInstance();

    /**
     * Generate job run ID.
     * @param parameterMap
     * @return String
     */
    static public String toID(Collection<Parameter> parameterMap) {

        final boolean hasDate = parameterMap.stream().anyMatch(p -> p.getId() != null && p.getId().equals("date"));

        return
            StringUtils.strip(
                parameterMap.stream()
                    .filter(p -> p.getId() != null)
                    .filter(p -> !hasDate || !Arrays.asList("year", "month", "day", "month0", "day0", "day_before").contains(p.getId()))
                    .filter(p -> !p.getId().equals("context") && !p.getId().startsWith("_"))
                    .reduce(
                "",
                        (ret, p) -> ret += String.format("&%s=%s", p.getId(), p.getValueDescription()),
                        (p1, p2) -> p1 + p2
                    )
            , "&"
        );
    }

    public List<ParameterBag> materialize(final ParameterBag parameters) throws Exception {
        return materialize(parameters, new HashMap<>());
    }

    /**
     * Transform args X contexts in real jobs.
     *
     * @param parameters
     * @param jobContexts
     * @return
     * @throws Exception
     */
    public List<ParameterBag> materialize(final ParameterBag parameters, final Map<String, ParameterBag> jobContexts) throws Exception {
        final ArrayList<ParameterBag> runsFromParameters = new ArrayList<>();
        final ArrayList<Parameter> jobParameters = new ArrayList<>();

        // Expand parameters
        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters.values()) {
                switch (parameter.getType()) {
                    case Parameter.TYPE_DAILY_DATE:
                        runsFromParameters.addAll(expandDate(parameter));
                    break;
                    default:
                        jobParameters.add(parameter);
                }
            }
        }

        // If no expandable parameters -> empty
        if (runsFromParameters.size() == 0) {
            runsFromParameters.addAll(new ArrayList<ParameterBag>(){{
                add(new ParameterBag());
            }});
        }

        // Merge other parameters
        runsFromParameters.forEach(run ->
            jobParameters.forEach(p -> run.put(p.getId(), p))
        );

        if (jobContexts != null && jobContexts.size() > 0) {
            final ArrayList<ParameterBag> ret = new ArrayList<>();

            jobContexts.forEach( (contextKey, context) -> {
                runsFromParameters.forEach(run -> {
                    final ParameterBag runClone = ParameterBag.merge(run);

                    runClone.putAll(context);
                    runClone.put("_context_", new Parameter("context", contextKey));
                    runClone.putAll(resolveParametersForJob(runClone));

                    ret.add(runClone);
                });
            });

            return ret;
        } else {
            runsFromParameters.forEach(run -> run.put("_context_", new Parameter("context", "default")));

            return runsFromParameters.stream().map(this::resolveParametersForJob).collect(Collectors.toList());
        }
    }

    /**
     * Last pass: render string template if any.
     *
     * @param parameterBag
     * @return
     */
    private ParameterBag resolveParametersForJob(final ParameterBag parameterBag) {
        final ParameterBag ret = new ParameterBag();

        for (Map.Entry<String, Parameter> entry : parameterBag.entrySet()) {
            final Parameter p = entry.getValue();
            if (p.getType().equals(Parameter.TYPE_STRING) && p.getValue() != null && p.getValueAsString().contains("{")) {
                final Parameter pc = p.clone();

                pc.setValue(templateUtils.render((String) p.getValue(), Arrays.asList(MyMapUtils.map("args", parameterBag))));

                ret.put(entry.getKey(), pc);
            } else {
                ret.put(entry.getKey(), entry.getValue());
            }
        }

        return ret;
    }

    public ArrayList<ParameterBag> expandDefault(final Parameter parentParameter) throws Exception {
        final ArrayList<ParameterBag> ret = new ArrayList<>();

        ret.add(new ParameterBag(parentParameter.getId(), parentParameter));

        return ret;
    }

    /**
     * Create runs of parameters.
     *
     * @return ArrayList<ParameterBag>
     * @throws Exception
     */
    private ArrayList<ParameterBag> expandDate(final Parameter parentParameter) throws Exception {
        final ArrayList<ParameterBag> ret = new ArrayList<>();

        if (parentParameter.getValue() == null) {
            throw new NullArgumentException(String.format("Parameter %s have a null value!", parentParameter.getId()));
        }

        // Parse "VALUE1,VALUE2,VALUE3..."
        final String[] values = parentParameter.getValueAsString().split(",");

        for (String value : values) {
            if (value.equals("today")) {
                calendar.setTime(new Date());
                ret.add(generateForDay(parentParameter));
            } else if (value.equals("yesterday")) {
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                ret.add(generateForDay(parentParameter));
            } else if (value.equals("month")) {
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, 0);
                ret.addAll(byMonth(parentParameter));
            } else if (value.length() == 10) {
                calendar.setTime(dateParser.parse(value));
                ret.add(generateForDay(parentParameter));
            } else if (value.length() == 7) { // 10/2017
                final Date firstDay = monthParser.parse(value);

                calendar.setTime(firstDay);

                ret.addAll(byMonth(parentParameter));
            }  else if (value.length() == 4 && StringUtils.isNumeric(value)) { // 2017
                /** @todo Implement year parsing **/
                throw new Exception("year parsing is not yet implemented!");
            } else if (value.contains(":")) { // Range: 04/2017:now
                ret.addAll(byInterval(parentParameter, value));
            } else {
                throw new Exception("date format not supported!");
            }
        }

        return ret;
    }

    /**
     * - 01/01/2018:today
     * - 01/02/2018:10/02/2018
     *
     * @param interval
     * @return
     * @throws ParseException
     */
    private ArrayList<ParameterBag> byInterval(final Parameter parentParameter, final String interval) throws ParseException {
        final String[] ranges = interval.split(":");
        final ArrayList<ParameterBag> ret = new ArrayList<>();

        calendar.setTime(dateParser.parse(ranges[0]));

        Calendar end = Calendar.getInstance();

        if (ranges.length == 1 || ranges[1].equals("now") || ranges[1].equals("today"))
        {
            end.setTime(new Date());
        }
        else if (ranges[1].equals("yesterday"))
        {
            end.setTime(yesterday());
        }
        else
        {
            end.setTime(dateParser.parse(ranges[1]));
        }

        while(!calendar.after(end)) {
            ret.add(generateForDay(parentParameter));

            calendar.add(Calendar.DATE, 1);
        }

        return ret;
    }

    private ArrayList<ParameterBag> byMonth(final Parameter parentParameter) {
        final ArrayList<ParameterBag> ret = new ArrayList<>();
        final int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int d = 1; d <= maxDay; d++) {
            calendar.set(Calendar.DAY_OF_MONTH, d);

            ret.add(generateForDay(parentParameter));
        }

        return ret;
    }

    /**
     * Create value for a day.
     *
     * @param parentParameter
     * @return
     */
    private ParameterBag generateForDay(final Parameter parentParameter) {
        /**
         * @deprecated direct values, use "date" object instead.
         */
        ParameterBag p = new ParameterBag(
            "day", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                "month", String.valueOf(1 + calendar.get(Calendar.MONTH)),
                "day0", String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)),
                "month0", String.format("%02d", 1 + calendar.get(Calendar.MONTH)),
                "year", String.valueOf(calendar.get(Calendar.YEAR))
        );

        final Calendar cc = (Calendar) calendar.clone();

        cc.add(Calendar.DATE, -1);

        // Replace original string date by DateParameter
        p.put(parentParameter.getId(), new DateParameter(parentParameter.getId(), (Calendar) calendar.clone()));
        p.put(parentParameter.getId() + "_raw", parentParameter);

        p.put("day_before", new DateParameter("day_before", cc));

        return p;
    }

    static private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

}
