package io.datatok.djobi.engine.actions.net.ftp.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class FTPOutputConfig extends ActionConfiguration {
    String host = "localhost";
    Integer port = 21;
    String user = "djobi";
    String password = null;
    String path = ".";
    String proxyHost = null;
    Integer proxyPort = null;
    Integer timeout = 5 * 10000;

    FTPOutputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.host = render("host", host);
        this.port = Integer.parseInt(render("port", port.toString()));
        this.user = render("user", user);
        this.password = render("password", password);
        this.path = render("path", path);
        this.proxyHost = render("proxy_host", proxyHost);
        this.proxyPort = Integer.parseInt(render("proxy_port", "0"));
        this.timeout = Integer.parseInt(render("timeout", timeout.toString()));
    }
}
