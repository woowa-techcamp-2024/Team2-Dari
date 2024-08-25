package com.wootecam;

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

    /*
        티켓을 결제한 회원을 추가하는 메소드
        @return 추가에 성공했다면 1, 이미 구매한 회원이라면 0
     */
    public Long addPurchasedMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().add("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }

    /*
        티켓을 결제한 회원을 제거하는 메소드
        성공한다면 제거된 원소 개수
        실패한다면 0 반환
     */
    public Long removePurchasedMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().remove("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }

    public Boolean isPurchasedMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().isMember("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId));
    }
}
