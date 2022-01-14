package io.datatok.djobi.engine.actions.net.http.input;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HTTPInputConfig extends ActionConfiguration {

    public String method = "GET";

    public String URLBase;

    public String URLPath;

    public String URLQueryString;

    /**
     * Auth login.
     */
    public String authLogin;

    /**
     * Auth password.
     */
    public String authPassword;


    public HTTPInputConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.authLogin = render("auth_login");
        this.authPassword = render("auth_password");

        this.URLBase = render("url_base");
        this.URLPath = render("url_path");

        if (this.has("method")) {
            this.method = render("method");
        }

        if (this.has("url_query")) {
            Map<String, String> query = this.renderMap("url_query", true);
            List<BasicNameValuePair> nameValuePairs = query.entrySet().stream()
                    .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparing(BasicNameValuePair::getName))
                    .collect(Collectors.toList());

            this.URLQueryString = URLEncodedUtils.format(nameValuePairs, StandardCharsets.UTF_8);
        }
    }
}
