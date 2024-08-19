package com.wootecam.festivals.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
public abstract class SpringBootTestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void clear() {
        transactionTemplate.execute(status -> {
            disableConstraints();
            try {
                truncateTables();
            } finally {
                enableConstraints();
            }
            return null;
        });
    }

    private void disableConstraints() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
    }

    private void enableConstraints() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private void truncateTables() {
        List<Class<?>> entities = entityManager.getMetamodel().getEntities().stream()
                .map(type -> type.getJavaType())
                .filter(clazz -> clazz.isAnnotationPresent(Table.class))
                .collect(Collectors.toList());

        for (Class<?> entity : entities) {
            String tableName = entity.getAnnotation(Table.class).name();
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
    }
}