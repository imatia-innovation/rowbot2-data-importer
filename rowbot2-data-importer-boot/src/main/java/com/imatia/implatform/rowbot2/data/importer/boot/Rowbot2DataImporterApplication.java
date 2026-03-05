package com.imatia.implatform.rowbot2.data.importer.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication(scanBasePackages = "com.imatia.implatform.rowbot2.data.importer")

public class Rowbot2DataImporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(Rowbot2DataImporterApplication.class, args);
	}

}
