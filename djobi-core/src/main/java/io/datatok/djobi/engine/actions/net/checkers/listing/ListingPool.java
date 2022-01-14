package io.datatok.djobi.engine.actions.net.checkers.listing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.datatok.djobi.configuration.Configuration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ListingPool {

    static private String CONFIG_PATH = "djobi.checkers";

    static private Logger logger = Logger.getLogger(ListingPool.class);

    /**
     * Hold "listing" by service name.
     */
    private Map<String, HttpListing> listings = new HashMap<>();

    /**
     * Hold "Config" by service name.
     */
    private Config configs;


    @Inject
    private Provider<HttpListing> listingProvider;

    @Inject
    public ListingPool(Configuration configuration) {
        if (configuration.hasPath(CONFIG_PATH)) {
            this.configs = configuration.getConfig(CONFIG_PATH);
        } else {
            logger.info(String.format("Configuration is missing \"%s\"!", CONFIG_PATH));
        }
    }

    public HttpListing.Record getFile(String service, final String file) throws IOException {
        if (this.configs == null || configs.entrySet().size() == 0) {
            return null;
        }
        
        if (service == null) {
            service = configs.root().keySet().iterator().next();
        }

        if (!listings.containsKey(service)) {
            listings.put(service, listingProvider.get());

            if (configs.hasPath(service)) {
                listings.get(service).init(configs.getConfig(service));
            }
        }

        return listings.get(service).getFile(file);
    }

}
