package io.datatok.djobi.engine.actions.net.http.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.http.Http;
import io.datatok.djobi.utils.http.HttpRequest;
import io.datatok.djobi.utils.http.HttpResponse;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.IOException;

public class HTTPInputRunner implements ActionRunner {

    private static Logger logger = Logger.getLogger(HTTPInputRunner.class);

    @Inject
    private Http http;

    @Override
    public ActionRunResult run(Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final HTTPInputConfig config = (HTTPInputConfig) stage.getParameters();

        logger.debug(String.format("[config] %s", config.toString()));

        final HttpRequest request = this.buildAPIRequest(config);

        final HttpResponse response = executeRequest(request, 5);

        if (response.statusCode() == 200) {
            return ActionRunResult.success(new StageDataString(response.raw()));
        }

        return ActionRunResult.fail(String.format("Get HTTP code: %d", response.statusCode()));
    }


    /**
     * Manage headers, auth.
     */
    private HttpRequest buildAPIRequest(final HTTPInputConfig config) {
        final HttpRequest request = http.call(config.method, config.URLBase + config.URLPath, null);

        return request
            .viaTransport("web")
            .header("Content-Type", "application/json")
        ;
    }

    private HttpResponse executeRequest(final HttpRequest request, int retryCount) throws IOException {
        HttpResponse response = request.execute();

        if (response.statusCode() == 200) {
            return response;
        } else if (response.statusCode() == 202) {
            if (retryCount > 0) {
                logger.debug("Got 202 response, let's retry");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn("error when sleeping", e);
                }

                return executeRequest(request, retryCount - 1);
            } else {
                throw new IOException("job not ready, tried X times");
            }
        } else {
            throw new IOException("Got HTTP status " + response.statusCode());
        }
    }
}
