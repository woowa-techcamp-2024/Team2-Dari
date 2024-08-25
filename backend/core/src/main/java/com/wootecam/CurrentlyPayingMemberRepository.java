package com.wootecam;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    현재 결제중인 회원을 관리하는 Repository
    결제중인 회원은 Set 으로 구현되어 있음
    중복 결제를 방지하기 위함
    - tickets:ticketId:currentlyPayingMembers
 */
@Repository
public class CurrentlyPayingMemberRepository extends RedisRepository {

    public CurrentlyPayingMemberRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        결제중인 회원을 추가하는 메소드
        추가에 성공했다면 1
        이미 결제중인 회원이라면 0
     */
    public Long addCurrentlyPayingMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().add(TICKETS_PREFIX + ticketId + ":" + CURRENTLY_PAYING_MEMBERS_PREFIX, String.valueOf(userId));
    }

    /*
        결제중인 회원을 제거하는 메소드
        성공한다면 제거된 원소 개수 반환
        실패한다면 0 반환
     */
    public Long removeCurrentlyPayingMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().remove(TICKETS_PREFIX + ticketId + ":" + CURRENTLY_PAYING_MEMBERS_PREFIX, String.valueOf(userId));
    }

    /*
        결제중인 회원인지 확인하는 메소드
     */
    public Boolean isCurrentlyPayingMember(Long ticketId, Long userId) {
        return redisTemplate.opsForSet().isMember(TICKETS_PREFIX + ticketId + ":" + CURRENTLY_PAYING_MEMBERS_PREFIX, String.valueOf(userId));
    }
}
