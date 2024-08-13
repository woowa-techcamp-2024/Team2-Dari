package com.wootecam.festivals.utils;

import org.springframework.data.jpa.repository.JpaRepository;

public class TestDBCleaner {
    public static void clear(JpaRepository jpaRepository) {
        jpaRepository.deleteAll();
    }
}
