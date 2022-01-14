package io.datatok.djobi.plugins.stages.api.yarn.clean;

import com.google.inject.Inject;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @since v1.6
 */
public class APIYarnCleanRunner implements ActionRunner {

    @Inject
    private Http http;

    static private Logger logger = Logger.getLogger(APIYarnCleanRunner.class);

    private APIYarnCleanConfig config;

    @Override
    @SuppressWarnings("unchecked")
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        config = (APIYarnCleanConfig) stage.getParameters();

        final String url = getYARNAPIUrl("apps", MyMapUtils.map("limit", "10", "states", "running"));

        logger.debug(String.format("Calling YARN API at %s", url));

        final List<Map> apps = (List<Map>) http.get(url).execute().at("apps.app");

         for (Map app : apps) {
             final String appName = (String) app.get("name");
             final String appId = (String) app.get("id");

             // Kill not whitelisted apps
             if (!appId.equals(( (SparkContext) stage.getJob().getPipeline().getExecutor().get("context")).applicationId()) &&
                 !config.whitelistApps.contains(appName)) {
                 logger.info(String.format("[YARN] Found not whitelisted application %s (%s)", appName, appId));
                 killApp(appId);
             }
         }

         return ActionRunResult.success();
    }

    private void killApp(final String appID) throws IOException {
        final String url = getYARNAPIUrl(String.format("apps/%s/state", appID), MyMapUtils.map());

        final HttpResponse response = http.put(url, MyMapUtils.map("state", "KILLED")).execute();

        logger.info(String.format("[YARN] Kill response %d %s", response.statusCode(), response.raw()));
    }

    private String getYARNAPIUrl(final String service, final Map<String, String> queryParameters) {
        final String queryString = queryParameters.entrySet().stream()
                .map(p -> urlEncodeUTF8(p.getKey()) + "=" + urlEncodeUTF8(p.getValue()))
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        return String.format("%s/ws/v1/cluster/%s?%s", config.url, service, queryString);
    }

    static private String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
