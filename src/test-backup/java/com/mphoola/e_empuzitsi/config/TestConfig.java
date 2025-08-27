package com.mphoola.e_empuzitsi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing
@EntityScan(basePackages = "com.mphoola.e_empuzitsi.entity")
public class TestConfig {
}
