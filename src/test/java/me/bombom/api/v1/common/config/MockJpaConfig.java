package me.bombom.api.v1.common.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@TestConfiguration
public class MockJpaConfig {

    @Bean
    public JpaMetamodelMappingContext jpaMetamodelMappingContext() {
        return mock(JpaMetamodelMappingContext.class);
    }
}
