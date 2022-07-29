package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.lang.StringUtils;

public class ESOutputConfig extends ActionConfiguration {

    String host;
    String url;

    String index;

    String clean_query;

    public ESOutputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.host = resolveHost(render("host"));
        this.url = resolveUrl(render("host"));
        this.index = render("index");
        this.clean_query = render("clean_query");
    }

    public void validate() throws Exception {
        if (this.index.contains("/")) {
            throw new Exception("index name must not contains type!");
        }
    }

    private String resolveUrl(final String in) {
        if (in.startsWith("http:") || in.startsWith("https:")) {
            return in;
        }

        return "http://" + in;
    }

    private String resolveHost(final String in) {
        String out;

        if (in.startsWith("http:")) {
            out = in.substring(7);
        } else if (in.startsWith("https:")) {
            out = in.substring(8);
        } else {
            out = in;
        }

        out = out.trim();

        out = StringUtils.strip(out, "/");

        return out;
    }

}
