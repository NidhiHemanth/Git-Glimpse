package org.bootcamp.com.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/*
 @Component - define class as a spring bean
 @ConfigurationProperties - identifies the property file to load
 */

@Component
@Validated
@ConfigurationProperties(prefix = "implType")
@Getter
@Setter
public class ApplicationProperties {
    private String useSqlQueries;
}
