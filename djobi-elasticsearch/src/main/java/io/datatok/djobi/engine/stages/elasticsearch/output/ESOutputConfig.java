package io.datatok.djobi.engine.stages.elasticsearch.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.lang.StringUtils;

public class ESOutputConfig extends ActionConfiguration {

    String host;
    String url;

    /**
     * @deprecated
     *
     * Use "realIndex" for es7
     */
    String index;

    /**
     *
     */
    String realIndex;

    String clean_query;

    public ESOutputConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.host = resolveHost(render("host"));
        this.url = resolveUrl(render("host"));
        this.index = render("index");
        this.clean_query = render("clean_query");

        this.realIndex = getIndex(this.index);
    }

    private String getIndex(final String value) {
        if (value != null && value.contains("/")) {
            return value.substring(0, value.indexOf("/"));
        } else {
            return value;
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
