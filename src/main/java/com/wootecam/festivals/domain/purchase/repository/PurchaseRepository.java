package com.wootecam.festivals.domain.purchase.repository;

import com.wootecam.festivals.domain.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}