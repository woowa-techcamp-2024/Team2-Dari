package com.wootecam.festivals.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    구매한 회원을 관리하는 Repository
    구매한 회원은 Set 으로 구현되어 있음
    중복 구매를 방지하기 위함
    - tickets:ticketId:purchasedMembers
 */
@Repository
@RequiredArgsConstructor
public class PurchasedMemberRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void addPurchasedMember(Long ticketId, Long userId) {
        redisTemplate.opsForSet().add("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }

    public void removePurchasedMember(Long ticketId, Long userId) {
        redisTemplate.opsForSet().remove("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }

    public Boolean isPurchasedMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().isMember("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }
}
