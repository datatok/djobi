package io.datatok.djobi.plugins.stages.api.ambari;

import com.google.inject.Inject;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.NotFoundException;
import io.datatok.djobi.exceptions.StageException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.JSONUtils;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APIAmbariRunner implements ActionRunner {

    @Inject
    private Http http;

    private APIAmbariConfig config;

    private static Logger logger = Logger.getLogger(APIAmbariRunner.class);

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        config = (APIAmbariConfig) stage.getParameters();

        if (config == null) {
            throw new StageException(stage, "configuration is null!");
        }

        switch (config.action)
        {
            case "restart":
                restartComponent(stage);
            break;
        }

        return ActionRunResult.success();
    }

    @SuppressWarnings("unchecked")
    private void restartComponent(final Stage stage) throws Exception {
        String url = getAPIUrl(String.format("services/%s/components/%s", config.service, config.component), MyMapUtils.map());

        logger.debug(String.format("Calling Ambari API at %s", url));

        HttpResponse response = authenticateAPIRequest(http.get(url)).execute();

        if (response == null) {
            throw new StageException(stage, "Ambari API error!");
        }

        if (response.statusCode() == 403) {
            throw new StageException(stage, "Ambari API authentication error!");
        }

        if (response.statusCode() == 404) {
            throw new NotFoundException(stage, String.format("Ambari API error: %s", response.at("message").toString()));
        }

        final List<Map> apiResponseHosts = (List<Map>) response.at("host_components");
        final ArrayList<String> hosts = new ArrayList<>();

        if (apiResponseHosts == null) {
            throw new StageException(stage, "Ambari API error!");
        }

        for (Map host : apiResponseHosts) {
            hosts.add((String) ( (Map) host.get("HostRoles")).get("host_name"));
        }

        if (hosts.size() == 0) {
            throw new NotFoundException(stage, "Component not found on Ambari cluster!");
        }

        url = getAPIUrl("requests", MyMapUtils.map());

        final Map body = MyMapUtils.map(
        "RequestInfo", MyMapUtils.map(
            "command", "RESTART",
                    "context", "Restart " + config.service + " / " + config.component + " via Djobi"
            ),
            "Requests/resource_filters", new Map[]{
            MyMapUtils.map(
                    "service_name", config.service,
                "component_name", config.component,
                "hosts", hosts.get(0)
            )
        });

        logger.debug(String.format("Calling Ambari API at %s", url));

        // Bug https://issues.apache.org/jira/browse/AMBARI-19869 => we must double json encode body data!
        response = authenticateAPIRequest(http.post(url)).setDataAsJSON(JSONUtils.serialize(body)).execute();

        if (response.statusCode() > 299) {
            throw new StageException(stage, String.format("Ambari restart component failure status code: %d (%s)", response.statusCode(), response.raw()));
        }
    }

    private String getAPIUrl(final String service, final Map<String, String> queryParameters) {
        final String queryString = queryParameters.entrySet().stream()
                .map(p -> urlEncodeUTF8(p.getKey()) + "=" + urlEncodeUTF8(p.getValue()))
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        return String.format("%s/api/v1/clusters/%s/%s?%s", config.url, config.cluster, service, queryString);
    }

    private HttpRequest authenticateAPIRequest(final HttpRequest httpRequest) {
        //Unirest.setProxy(new HttpHost("localhost", 8888));

        httpRequest
            .header("authorization", "Basic " + config.credentials)
            .header("X-Requested-By", "Ambari")
        ;

        return httpRequest;
    }

    static private String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
