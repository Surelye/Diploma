package sgu.borodin.nas.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

@Configuration
public class ObservationConfig {

    @Bean
    public ObservationPredicate actuatorServerContextPredicate() {
        return (name, context) -> {
            if (name.equals("http.server.requests") && context instanceof ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getURI().getPath().contains("/actuator");
            }
            return true;
        };
    }

    @Bean
    public ObservationPredicate springSecurityContextPredicate() {
        return (name, context) -> !name.startsWith("spring.security");
    }
}
