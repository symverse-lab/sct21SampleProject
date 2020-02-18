package com.symverse.prometheus;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class PrometheusCustomConfig {
	
	@Value("${spring.profiles.active}") private String serviceMode ; 
	@Value("${spring.application.name}") private String applicationName ; 
	
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    	log.debug("");
    	log.debug("");
    	log.debug("[[ ↓↓ Prometheus Config ↓↓ ]]");
    	log.debug("***** Prometheus ApplicationTagName : "+ serviceMode + "-" + applicationName  );
    	log.debug("***** PrometheusUrl : http://domain/actuator/prometheus" );
    	log.debug("[[ ↑↑ Prometheus Config ↑↑ ]]");
    	log.debug("");
    	log.debug("");
    	 
        return registry -> registry.config().commonTags("application", serviceMode +"-"+applicationName  );
    }
}