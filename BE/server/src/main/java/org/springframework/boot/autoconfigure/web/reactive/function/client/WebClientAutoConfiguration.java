package org.springframework.boot.autoconfigure.web.reactive.function.client;

import org.springframework.context.annotation.Configuration;

/**
 * Compatibility class for Spring AI (compiled against Spring Boot 3.x) 
 * running on Spring Boot 4.x.
 * 
 * In Spring Boot 4.x, WebClientAutoConfiguration was relocated or modularized.
 * This empty class satisfies the class loader when Spring AI's auto-configurations
 * are scanned/parsed during startup.
 */
@Configuration
public class WebClientAutoConfiguration {
}
