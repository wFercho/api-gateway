package com.apigateway.Gateway.filters;

import com.apigateway.Gateway.entities.AuthResponse;
import com.apigateway.Gateway.services.AuthService;
import com.apigateway.Gateway.services.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@Component
public class JWTCreateTokenFilter extends AbstractGatewayFilterFactory<JWTCreateTokenFilter.Config> {

    private final RestTemplate restTemplate;

    public JWTCreateTokenFilter() {
    super(JWTCreateTokenFilter.Config.class);
    this.restTemplate = new RestTemplate();
}

@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        // Ejecutar la cadena de filtros y obtener la respuesta
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            int statusCode = exchange.getResponse().getStatusCode().value();
            System.out.println("STATUS "+statusCode);
            if(statusCode >= 200 && statusCode < 300){
                String token = postDataToRemoteService("https://token-generator-ten.vercel.app/generateToken", "{}");
                exchange.getResponse().getHeaders().set("Authorization", token);
            }else{
                exchange.getResponse().getHeaders().set("Authorization", "");
            }
        }));
    };
}
    public String postDataToRemoteService(String url,  String data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);

        return restTemplate.postForObject(url, requestEntity, String.class);
    }
public static class Config {
    private boolean logHeaders;

    public boolean isLogHeaders() {
        return logHeaders;
    }

    public void setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
    }
}
}