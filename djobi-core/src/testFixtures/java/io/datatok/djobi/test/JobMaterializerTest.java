package io.datatok.djobi.test;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.loaders.JobMaterializer;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.*;

class JobMaterializerTest {

    @Inject
    private JobMaterializer jobMaterializer;

    @Test void testToID() {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>(){{
            add(new Parameter("hello", "world"));
            add(new Parameter("salut", "monde"));
            add(new Parameter("month", "01"));
        }};

        final String result = JobMaterializer.toID(parameters);

        Assertions.assertEquals(result, "hello=world&salut=monde&month=01");
    }

    @Test void testDateToID() throws ParseException {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>(){{
            add(new DateParameter("date", "01/01/2018"));
            add(new Parameter("salut", "monde"));
            add(new Parameter("month", "01"));
            add(new Parameter("year", "2018"));
        }};

        final String result = JobMaterializer.toID(parameters);

        Assertions.assertEquals(result, "date=2018-01-01&salut=monde");
    }

    @Test void testTemplating() throws Exception {
        final ParameterBag parameters = ParameterBag.build(
                new Parameter("hello", "world"),
                new Parameter("salut", "hello {{args.hello}}")
        );

        final List<ParameterBag> jobs = jobMaterializer.materialize(parameters);

        Assertions.assertEquals(1, jobs.size());
        Assertions.assertEquals("hello world", jobs.get(0).get("salut").toString());
    }

    @Test void testWithContexts() throws Exception {
        final ParameterBag parameters = ParameterBag.build(
            new Parameter("hello", "world"),
            new Parameter("salut", "hello in {{args._context_}} = {{args.hello}}")
        );

        final Map<String, ParameterBag> jobContexts= new HashMap<String, ParameterBag>(){{
            put("fr", ParameterBag.build(new Parameter("hello", "bonjour")));
            put("es", ParameterBag.build(new Parameter("hello", "ola")));
        }};

        final List<ParameterBag> jobs = jobMaterializer.materialize(parameters, jobContexts);

        Assertions.assertEquals(2, jobs.size());
        Assertions.assertEquals(3, jobs.get(0).size());
        Assertions.assertEquals("hello in fr = bonjour", jobs.get(0).get("salut").toString());
        Assertions.assertEquals("hello in es = ola", jobs.get(1).get("salut").toString());
    }

    @Test void testDateParameterNull() {

        final Parameter parameter = new Parameter(){{
            setId("date");
        }};

        Assertions.assertThrows(Exception.class, () -> runExpandDate(parameter));
    }

    @Test void testDateParameterSingleDay() throws Exception {

        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("yesterday");
        }};

        final ArrayList<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 1);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        Assertions.assertEquals(results.get(0).get("month").getValue(), String.valueOf(1 + calendar.get(Calendar.MONTH)));
        Assertions.assertEquals(results.get(0).get("year").getValue(), String.valueOf(calendar.get(Calendar.YEAR)));

    }

    @Test void testDateParameter2Days() throws Exception {
        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("10/12/2017,11/12/2017");
        }};

        final ArrayList<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 2);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), "10");
        Assertions.assertEquals(results.get(0).get("month").getValue(), "12");
        Assertions.assertEquals(results.get(0).get("year").getValue(), "2017");
        Assertions.assertEquals(results.get(1).get("day").getValue(), "11");

        @SuppressWarnings("unchecked")
        final DateParameter dayBefore = (DateParameter) results.get(0).get("day_before");

        Assertions.assertEquals("9", dayBefore.getDay());
        Assertions.assertEquals( "09", dayBefore.getDay0());
        Assertions.assertEquals( "12", dayBefore.getMonth());
    }

    @Test void testDateParameterMonth() throws Exception {

        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("12/2017");
        }};

        final ArrayList<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 31);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), "1");
        Assertions.assertEquals(results.get(0).get("month").getValue(), "12");
        Assertions.assertEquals(results.get(0).get("year").getValue(), "2017");
        Assertions.assertEquals(results.get(30).get("day").getValue(), "31");
        Assertions.assertEquals(results.get(30).get("month").getValue(), "12");
        Assertions.assertEquals(results.get(30).get("year").getValue(), "2017");
    }

    @Test void testDateParameter3Month() throws Exception {

        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("11/2017,12/2017,01/2018");
        }};

        final ArrayList<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 30 + 31 + 31);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), "1");
        Assertions.assertEquals(results.get(0).get("month").getValue(), "11");
        Assertions.assertEquals(results.get(0).get("year").getValue(), "2017");
        Assertions.assertEquals(results.get(30 + 31 + 31 - 1).get("day").getValue(), "31");
        Assertions.assertEquals(results.get(30 + 31 + 31 - 1).get("month").getValue(), "1");
        Assertions.assertEquals(results.get(30 + 31 + 31 - 1).get("year").getValue(), "2018");
    }

    @Test void testDateParameterInterval() throws Exception {

        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("20/12/2017:10/01/2018");
        }};

        final ArrayList<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 12 + 10);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), "20");
        Assertions.assertEquals(results.get(0).get("month").getValue(), "12");
        Assertions.assertEquals(results.get(0).get("year").getValue(), "2017");
        Assertions.assertEquals(results.get(12).get("day").getValue(), "1");
        Assertions.assertEquals(results.get(12).get("month").getValue(), "1");
        Assertions.assertEquals(results.get(12).get("year").getValue(), "2018");
        Assertions.assertEquals(results.get(results.size() - 1).get("day").getValue(), "10");
        Assertions.assertEquals(results.get(results.size() - 1).get("month").getValue(), "1");
        Assertions.assertEquals(results.get(results.size() - 1).get("year").getValue(), "2018");
    }

    @SuppressWarnings("unchecked")
    private ArrayList<ParameterBag> runExpandDate(final Parameter parameter) throws Exception {
        Method method = JobMaterializer.class.getDeclaredMethod("expandDate", Parameter.class);
        method.setAccessible(true);

        return (ArrayList<ParameterBag>) method.invoke(jobMaterializer, parameter);
    }
}
