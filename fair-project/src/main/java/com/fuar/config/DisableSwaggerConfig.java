package com.fuar.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class will disable the Swagger UI when the springdoc.swagger-ui.enabled property is set to false.
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "false")
@EnableAutoConfiguration(excludeName = {
    "org.springdoc.core.SpringDocConfiguration",
    "org.springdoc.webmvc.core.SpringDocWebMvcConfiguration",
    "org.springdoc.webmvc.ui.SwaggerConfig",
    "org.springdoc.core.SwaggerUiConfigProperties",
    "org.springdoc.core.SwaggerUiOAuthProperties",
    "org.springdoc.core.SpringDocConfigProperties",
    "org.springdoc.core.MultipleOpenApiSupportConfiguration"
})
public class DisableSwaggerConfig {
    // No additional configuration needed
}
