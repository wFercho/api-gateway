package com.apigateway.Gateway.config;

import com.apigateway.Gateway.filters.JWTCreateTokenFilter;
import com.apigateway.Gateway.filters.JWTValidateTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;


@Configuration
@EnableDiscoveryClient
@RequiredArgsConstructor
public class GatewayConfig {

    private static final int REQUEST_PER_SECOND = 5;
    private final JWTCreateTokenFilter jwtCreateTokenFilter;
    private final JWTValidateTokenFilter jwtValidateTokenFilter;


    @Bean
    public RouteLocator rutas(RouteLocatorBuilder routeLocatorBuilder){

        return routeLocatorBuilder.routes()
                .route(r -> r
                        .path("/login/")
                        .filters(f -> f
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("https://microservice-production-b78e.up.railway.app")
                )
                .route(r -> r
                        .path("/login/access")
                        .filters(f -> f
                                .filter(jwtCreateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("https://microservice-production-b78e.up.railway.app")
                )
                .route(r -> r
                        .path("/event/**")
                        .filters(f -> f
                                .filter(
                                        jwtValidateTokenFilter.apply(config -> config.setLogHeaders(false))
                                )
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://apieventos")

                )
                .route(r -> r
                        .path("/assistant/**")
                        .filters(f -> f
                                .filter(jwtValidateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://apieventos")
                )
                .route(r -> r
                        .path("/enviar-correo")
                        .filters(f -> f
                                .filter(jwtValidateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("https://microserviciomaven-production.up.railway.app")
                )
                .route(r -> r
                        .path("/**")
                        .filters(f -> f
                                .filter(jwtValidateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("https://microserviciomaven-production.up.railway.app")
                )
                .route(r -> r
                        .path("/inscription/**")
                        .filters(f -> f
                                .filter(jwtValidateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://INSCRIPCIONES-MICROSERVICE")
                )
                .route(r -> r
                        .path("/**")
                        .filters(f -> f
                                .filter(jwtValidateTokenFilter.apply(config -> {config.setLogHeaders(false);}))
                                .requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()))
                        )
                        .uri("lb://MICROSERVICE_REPORTS")
                )
                .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter(){
        return new RedisRateLimiter(REQUEST_PER_SECOND, REQUEST_PER_SECOND);
    }

    /**@Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(){
        return factory->factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(2)).build()).build());
    }**/

    @Bean
    KeyResolver userKeyResolver(){
        return  exchange -> Mono.just("1");
    }
}