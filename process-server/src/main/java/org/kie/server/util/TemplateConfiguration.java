package org.kie.server.util;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class TemplateConfiguration {

    private static Configuration configuration;
    
    private TemplateConfiguration() {
        
    }
    
    public static Configuration getInstance() {
        if (configuration == null) {
            configuration = new Configuration();
            try {
                configuration.setClassForTemplateLoading(TemplateConfiguration.class, "/templates");
            } catch (Exception e) {
                e.printStackTrace();
            }
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return configuration;
    }
    
}
