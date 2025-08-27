package com.mphoola.e_empuzitsi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EEmpuzitsiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EEmpuzitsiApplication.class, args);
	}

}
