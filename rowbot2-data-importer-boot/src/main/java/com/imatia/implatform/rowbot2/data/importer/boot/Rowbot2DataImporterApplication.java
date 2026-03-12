package com.imatia.implatform.rowbot2.data.importer.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;


@SpringBootApplication(scanBasePackages = "com.imatia.implatform.rowbot2.data.importer")
@EnableJpaRepositories(
		basePackages = "com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository",
		bootstrapMode = BootstrapMode.LAZY
)

@EntityScan(
		basePackages = "com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity"
)
public class Rowbot2DataImporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(Rowbot2DataImporterApplication.class, args);
	}

}
