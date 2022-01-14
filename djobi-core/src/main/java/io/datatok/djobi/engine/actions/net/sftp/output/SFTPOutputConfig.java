package io.datatok.djobi.engine.actions.net.sftp.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;

public class SFTPOutputConfig extends ActionConfiguration {
    String host;
    Integer port;
    String user;
    String password;
    String identity;
    String path = ".";
    String proxyHost = null;
    Integer proxyPort = null;
    Integer timeout;

    /**
     * @since v2.2.11
     */
    String checkService;

    public SFTPOutputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.host = render("host", "localhost");
        this.port = Integer.parseInt(render("port", "22"));
        this.user = render("user", "djobi");
        this.password = render("password");
        this.identity = render("identity");
        this.path = render("path", path);
        this.proxyHost = render("proxy_host", proxyHost);
        this.proxyPort = Integer.parseInt(render("proxy_port", "0"));
        this.timeout = Integer.parseInt(render("timeout", Integer.toString(1000 * 5)));

        this.checkService = render("check_service");
    }
}
