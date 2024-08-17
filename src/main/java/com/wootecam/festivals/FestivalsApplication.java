package com.wootecam.festivals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FestivalsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestivalsApplication.class, args);
	}
	// TODO: LocalDateTime 사용시 시, 분 까지만 처리하도록 변경 예정. 축제 생성시 인증 확인으로 변경
}
