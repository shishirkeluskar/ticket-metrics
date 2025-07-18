package com.shishir.ticketmetrics.testsupport.annotation;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("test")
@Tag("test")
public @interface UnitTest {
}
