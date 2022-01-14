package io.datatok.djobi.engine.actions.fs.output;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FSOutputConfig extends ActionConfiguration {
    // Common
    public String path;

    // "overwrite" , "error"
    public String mode;

    // If Spark DataFrame
    public String format;
    public int partitions;

    public Map<String, String> options;

    public Map<String, String> s3 = null;

    private static final Logger logger = Logger.getLogger(FSOutputConfig.class);

    public FSOutputConfig(Bag stageConfiguration, Job job, TemplateUtils templateUtils) {
        super(stageConfiguration, job, templateUtils);

        this.path = renderPath("path");
        this.format = render("format", "json");
        this.mode = render("mode");
        this.partitions = Integer.parseInt(render("partitions", "-1"));
        this.options = renderMap("options");

        if (this.options == null)
        {
            this.options = new HashMap<>();
        }
        else {
            // support legacy
            if (this.options.containsKey("mode"))
            {
                logger.info("options.mode is deprecated, use mode instead.");

                this.mode = this.options.get("mode");

                this.options.remove("mode");
            }
        }

        if (this.has("s3"))
        {
            this.s3 = renderMap("s3");
        }
    }
}
