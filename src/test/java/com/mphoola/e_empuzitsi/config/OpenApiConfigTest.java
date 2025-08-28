package com.mphoola.e_empuzitsi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = OpenApiConfig.class)
@TestPropertySource(properties = {
    "server.port=8080"
})
@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig openApiConfig;

    @Autowired
    private OpenAPI openAPI;

    @Test
    @DisplayName("Should create OpenApiConfig bean successfully")
    void should_create_open_api_config_bean() {
        // Then
        assertNotNull(openApiConfig);
    }

    @Test
    @DisplayName("Should create OpenAPI bean with correct configuration")
    void should_create_open_api_bean() {
        // Then
        assertNotNull(openAPI);
        
        // Test API info
        assertNotNull(openAPI.getInfo());
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("📚 E-Empuzitsi LMS API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
            .contains("E-Empuzitsi LMS API documentation")
            .contains("JWT");
    }

    @Test
    @DisplayName("Should configure JWT security scheme correctly")
    void should_configure_jwt_security_scheme() {
        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        
        SecurityScheme jwtScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(jwtScheme);
        
        assertThat(jwtScheme.getName()).isEqualTo("Bearer Authentication");
        assertThat(jwtScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(jwtScheme.getScheme()).isEqualTo("bearer");
        assertThat(jwtScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(jwtScheme.getDescription())
            .contains("JWT token")
            .contains("/api/auth/login");
    }

    @Test
    @DisplayName("Should add security requirement for Bearer Authentication")
    void should_add_security_requirement() {
        // Then
        assertNotNull(openAPI.getSecurity());
        assertThat(openAPI.getSecurity()).hasSize(1);
        
        SecurityRequirement securityReq = openAPI.getSecurity().get(0);
        assertThat(securityReq.keySet()).contains("Bearer Authentication");
    }

    @Test
    @DisplayName("Should create OpenAPI with custom configuration")
    void should_create_open_api_with_custom_configuration() {
        // Given
        OpenApiConfig config = new OpenApiConfig();
        
        // When
        OpenAPI customOpenAPI = config.customOpenAPI();
        
        // Then
        assertNotNull(customOpenAPI);
        assertNotNull(customOpenAPI.getInfo());
        assertNotNull(customOpenAPI.getComponents());
        assertNotNull(customOpenAPI.getSecurity());
        
        // Verify title format
        assertThat(customOpenAPI.getInfo().getTitle()).startsWith("📚");
        
        // Verify security configuration
        assertThat(customOpenAPI.getComponents().getSecuritySchemes()).containsKey("Bearer Authentication");
        
        // Verify security requirement
        assertThat(customOpenAPI.getSecurity()).isNotEmpty();
    }

    @Test
    @DisplayName("Should use default server port when not specified")
    void should_use_default_server_port() {
        // Given
        OpenApiConfig config = new OpenApiConfig();
        
        // When
        OpenAPI api = config.customOpenAPI();
        
        // Then - Should not throw exception and create valid OpenAPI
        assertNotNull(api);
        assertNotNull(api.getInfo());
    }

    @Test
    @DisplayName("Should have proper API documentation structure")
    void should_have_proper_api_documentation_structure() {
        // Then
        // Verify info section
        assertThat(openAPI.getInfo().getTitle()).isNotBlank();
        assertThat(openAPI.getInfo().getVersion()).isNotBlank();
        assertThat(openAPI.getInfo().getDescription()).isNotBlank();
        
        // Verify components section exists
        assertNotNull(openAPI.getComponents());
        
        // Verify security schemes
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotEmpty();
        
        // Verify security requirements
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }

    @Test
    @DisplayName("Should configure Bearer token format correctly")
    void should_configure_bearer_token_format() {
        // Given
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        
        // Then
        assertThat(bearerAuth.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerAuth.getScheme()).isEqualToIgnoringCase("bearer");
        assertThat(bearerAuth.getBearerFormat()).isEqualToIgnoringCase("JWT");
        
        // Verify description gives proper instructions
        assertThat(bearerAuth.getDescription())
            .containsIgnoringCase("JWT token")
            .containsIgnoringCase("login")
            .containsIgnoringCase("without 'Bearer '");
    }
}
