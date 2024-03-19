package com.apigateway.Gateway.filters;


import com.apigateway.Gateway.services.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JWTValidateTokenFilter extends AbstractGatewayFilterFactory<JWTValidateTokenFilter.Config> {
    private final RestTemplate restTemplate;
    public JWTValidateTokenFilter() {
        super(JWTValidateTokenFilter.Config.class);
        this.restTemplate = new RestTemplate();
    }
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorizationHeader == null) {
                System.out.println("NO ESTÁ DEFINIDA LA CABECERA DE AUTH");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String jwtToken = authorizationHeader;
            try {
                ResponseEntity<String> validationResponse = postDataToRemoteService("https://token-generator-ten.vercel.app/validateToken", jwtToken);
                int status = validationResponse.getStatusCode().value();
                System.out.println("STATUS CODE TOKEN SERVICE: "+status);
                System.out.println("STATUS CODE MICROSERVICE: "+exchange.getResponse().getStatusCode().value());
                if (status >= 200 && status < 300) {
                    String tokenRefresh = postDataToRemote("https://token-generator-ten.vercel.app/refreshToken", "{}");
                    System.out.println("REFRESCA EL TOKEN");
                    exchange.getResponse().getHeaders().set("Authorization", tokenRefresh);
                    exchange.getResponse().setStatusCode(HttpStatus.ACCEPTED);
                    System.out.println("CONTENT TYPE MICROSERVICE RESPONSE: "+exchange.getResponse().getHeaders().getContentType());
                }
            } catch (Exception e) {
                System.out.println("ESTÁ FALLANDO"+e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }



    public ResponseEntity<String> postDataToRemoteService(String url, String data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //"{\"browsers\":\"asdasdasd\"}"
        String json = "{\"token\":";
        json = json + "\""+data+"\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

        return restTemplate.postForEntity(url, requestEntity, String.class);
    }

    public String postDataToRemote(String url,  String data) {
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
