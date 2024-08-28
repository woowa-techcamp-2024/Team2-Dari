package com.wootecam.festivals.domain.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    @ResponseStatus(HttpStatus.OK)
    public void health() {
    }
}
