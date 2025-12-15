package news.bombomadmin.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockJpaConfig {

    @Bean
    public JpaMetamodelMappingContext jpaMetamodelMappingContext() {
        return mock(JpaMetamodelMappingContext.class);
    }
}
