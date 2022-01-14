package io.datatok.djobi.utils;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.utils.templating.TemplateUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OptionBagUtils {

    @Inject
    protected Configuration configuration;

    @Inject
    protected TemplateUtils templateUtils;

}
