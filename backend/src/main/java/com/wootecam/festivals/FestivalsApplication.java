package com.wootecam.festivals;

import com.wootecam.festivals.global.config.CloudConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CloudConfiguration.class)
public class FestivalsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestivalsApplication.class, args);
	}
}
