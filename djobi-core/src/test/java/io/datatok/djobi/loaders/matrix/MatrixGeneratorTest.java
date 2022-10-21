package io.datatok.djobi.loaders.matrix;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.*;

class MatrixGeneratorTest {

    @Inject
    private MatrixGenerator generator;

    @Test void testToID() {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>(){{
            add(new Parameter("hello", "world"));
            add(new Parameter("salut", "monde"));
            add(new Parameter("month", "01"));
        }};

        final String result = MatrixGenerator.toUUID(parameters);

        Assertions.assertEquals(result, "hello=world&salut=monde&month=01");
    }

    @Test void testDateToID() throws ParseException {
        final ArrayList<Parameter> parameters = new ArrayList<Parameter>(){{
            add(new DateParameter("date", "01/01/2018"));
            add(new Parameter("salut", "monde"));
            add(new Parameter("month", "01"));
            add(new Parameter("year", "2018"));
        }};

        final String result = MatrixGenerator.toUUID(parameters);

        Assertions.assertEquals(result, "date=2018-01-01&salut=monde");
    }

    @Test void testTemplating() throws Exception {
        final ParameterBag parameters = ParameterBag.build(
                new Parameter("hello", "world"),
                new Parameter("salut", "hello {{args.hello}}")
        );

        final List<ParameterBag> jobs = generator.generate(parameters);

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

        final List<ParameterBag> jobs = generator.generate(parameters, jobContexts);

        Assertions.assertEquals(2, jobs.size());
        Assertions.assertEquals(3, jobs.get(0).size());
        Assertions.assertEquals("hello in fr = bonjour", jobs.get(0).get("salut").toString());
        Assertions.assertEquals("hello in es = ola", jobs.get(1).get("salut").toString());
    }

}
