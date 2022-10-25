package io.datatok.djobi.loaders.matrix;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.List;

public class DateMatrixGeneratorTest {
    @Inject
    DateMatrixGenerator generator;

    @Test
    void shouldParseSimpleDates() throws Exception {
        generator.generate(new Parameter("date", "10/01/2022"));
        generator.generate(new Parameter("date", "yesterday"));
        //generator.generate(new Parameter("date", "-3days"));
    }

    @Test
    void shouldParseInterval() throws Exception {
        Assertions.assertEquals(
            generator.generate(new Parameter("date", "10/01/2022:18/01/2022")).size(),
            9
        );

        Assertions.assertEquals(
                generator.generate(new Parameter("date", "01/01/2022:31/12/2022")).size(),
                365
        );

        Assertions.assertTrue(
        generator.generate(new Parameter("date", "01/01/2022:yesterday")).size() > 0
        );
    }


    @Test void testDateParameterSingleDay() throws Exception {

        final Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -1);

        final Parameter parameter = new Parameter(){{
            setId("date");
            setValue("yesterday");
        }};

        final List<ParameterBag> results = runExpandDate(parameter);

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

        final List<ParameterBag> results = runExpandDate(parameter);

        Assertions.assertEquals(results.size(), 2);
        Assertions.assertEquals(results.get(0).size(), 8);
        //assertArrayEquals(parameterMap.keySet().toArray(), new String[]{"day", "month", "year"});
        Assertions.assertEquals(results.get(0).get("day").getValue(), "10");
        Assertions.assertEquals(results.get(0).get("month").getValue(), "12");
        Assertions.assertEquals(results.get(0).get("year").getValue(), "2017");
        Assertions.assertEquals(results.get(1).get("day").getValue(), "11");

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

        final List<ParameterBag> results = runExpandDate(parameter);

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

        final List<ParameterBag> results = runExpandDate(parameter);

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

        final List<ParameterBag> results = runExpandDate(parameter);

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

    @Test
    void testDateParameterNull() {

        final Parameter parameter = new Parameter(){{
            setId("date");
        }};

        Assertions.assertThrows(Exception.class, () -> runExpandDate(parameter));
    }

    private List<ParameterBag> runExpandDate(final Parameter parameter) throws Exception {
        return generator.generate(parameter);
    }
}
