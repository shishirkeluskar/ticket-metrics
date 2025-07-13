package com.shishir.ticketmetrics.testsupport.annotation;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("integration-test")
@Tag("integration-test")
public @interface IntegrationTest {
}
