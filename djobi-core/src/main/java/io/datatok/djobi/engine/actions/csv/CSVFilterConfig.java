package io.datatok.djobi.engine.actions.csv;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.livecycle.ActionConfiguration;
import io.datatok.djobi.utils.Bag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.csv.QuoteMode;

public class CSVFilterConfig extends ActionConfiguration {

    public char delimiter;
    public QuoteMode quoteMode = null;

    CSVFilterConfig(final Bag stageConfiguration, final Job job, final TemplateUtils templateUtils) {

        super(stageConfiguration, job, templateUtils);

        this.delimiter = render("delimiter", ",").charAt(0);

        final String qm = render("quote_mode");

        if (qm != null) {
            switch (qm.toLowerCase()) {
                case "all":
                    quoteMode = QuoteMode.ALL;
                break;
                case "minimal":
                    quoteMode = QuoteMode.MINIMAL;
                break;
                case "none":
                    quoteMode = QuoteMode.NONE;
            }
        }
    }

}
