package com.wootecam.festivals.domain.member.repository;

import com.wootecam.festivals.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
