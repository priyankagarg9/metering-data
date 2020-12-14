package com.meter.energy.consumption;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.meter.energy.consumption.service.ProcessCsvDataService;

@SpringBootApplication
public class MeteringDataApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext appContext = SpringApplication.run(MeteringDataApplication.class, args);
		ProcessCsvDataService service = appContext.getBean(ProcessCsvDataService.class);
		service.processCsvFiles();
	}

}
