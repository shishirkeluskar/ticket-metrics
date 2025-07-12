package com.shishir.ticketmetrics.testsupport;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("est")
@Tag("test")
public @interface UnitTest {
}
