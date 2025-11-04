package com.newssum.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures MongoDB client settings and auditing for the application.
 */
@Slf4j
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder
            .applyToConnectionPoolSettings(pool -> pool
                .maxSize(50)
                .minSize(0)
                .maxConnectionIdleTime(30, TimeUnit.SECONDS)
                .maxConnectionLifeTime(5, TimeUnit.MINUTES))
            .applyToSocketSettings(socket -> socket
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS));
    }

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(final LocalValidatorFactoryBean validatorFactoryBean) {
        log.debug("Registering MongoDB validation listener");
        return new ValidatingMongoEventListener(validatorFactoryBean);
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
