package co.com.solicitud.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

public class UseCasesConfigTest {

    @Test
    void shouldConfigureComponentScanForUseCases() {
        ComponentScan componentScan = UseCasesConfig.class.getAnnotation(ComponentScan.class);
        assertNotNull(componentScan, "UseCasesConfig must declare @ComponentScan");

        assertArrayEquals(new String[]{"co.com.solicitud.usecase"}, componentScan.basePackages());
        assertFalse(componentScan.useDefaultFilters());

        ComponentScan.Filter[] filters = componentScan.includeFilters();
        assertEquals(1, filters.length);
        ComponentScan.Filter filter = filters[0];
        assertEquals(FilterType.REGEX, filter.type());
        assertArrayEquals(new String[]{"^.+UseCase$"}, filter.pattern());
    }
}