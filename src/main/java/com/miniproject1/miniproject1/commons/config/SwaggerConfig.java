package com.miniproject1.miniproject1.commons.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final Pattern ORDER_PREFIX = Pattern.compile("^(\\d+)\\.");

    @Bean
    public GlobalOpenApiCustomizer operationOrderCustomizer() {
        return openApi -> {
            Paths original = openApi.getPaths();
            if (original == null) {
                return;
            }

            List<Map.Entry<String, PathItem>> entries = new ArrayList<>(original.entrySet());
            entries.sort(Comparator
                    .<Map.Entry<String, PathItem>, String>comparing(entry -> extractTag(entry.getValue()))
                    .thenComparingInt(entry -> extractOrder(entry.getValue())));

            Paths sorted = new Paths();
            for (Map.Entry<String, PathItem> entry : entries) {
                sorted.addPathItem(entry.getKey(), entry.getValue());
            }
            openApi.setPaths(sorted);
        };
    }

    private String extractTag(PathItem pathItem) {
        for (Operation operation : pathItem.readOperations()) {
            if (operation.getTags() != null && !operation.getTags().isEmpty()) {
                return operation.getTags().get(0);
            }
        }
        return "";
    }

    private int extractOrder(PathItem pathItem) {
        int min = Integer.MAX_VALUE;
        for (Operation operation : pathItem.readOperations()) {
            String summary = operation.getSummary();
            if (summary == null) {
                continue;
            }
            Matcher matcher = ORDER_PREFIX.matcher(summary);
            if (matcher.find()) {
                min = Math.min(min, Integer.parseInt(matcher.group(1)));
            }
        }
        return min;
    }

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(new Info()
                        .title("Miniproject1 API Document")
                        .description("소상공인 지원 플랫폼 API 명세서")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}