package com.danone.pdpbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private boolean dev = false;
    
    public boolean isDev() {
        return dev;
    }
    
    public void setDev(boolean dev) {
        this.dev = dev;
    }
}
