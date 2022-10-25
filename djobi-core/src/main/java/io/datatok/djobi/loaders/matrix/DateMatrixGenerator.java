package io.datatok.djobi.loaders.matrix;

import com.google.inject.Singleton;
import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Generate new jobs from dates, date ranges parameters.
 *
 * Interval: "10/02/2022:19/02/2022"
 * Dates: "10/01/2022,12/01/2022"
 */
@Singleton
public class DateMatrixGenerator implements IMatrixGenerator {

    private final SimpleDateFormat dateParser = new SimpleDateFormat("dd/MM/yy");
    private final SimpleDateFormat monthParser = new SimpleDateFormat("MM/yy");

    private final Calendar calendar = Calendar.getInstance();

    /**
     * Create runs of parameters.
     *
     * @return ArrayList<ParameterBag>
     */
    public ArrayList<ParameterBag> generate(final Parameter parentParameter) throws Exception {
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


    static private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}
