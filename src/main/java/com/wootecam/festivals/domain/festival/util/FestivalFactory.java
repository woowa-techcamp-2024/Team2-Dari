package com.wootecam.festivals.domain.festival.util;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.GlobalErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalFactory {

    private final MemberRepository memberRepository;

    public Festival createFromDto(FestivalCreateRequest request) {
        Member admin = memberRepository.findById(request.adminId())
                .orElseThrow(() -> new ApiException(GlobalErrorCode.INVALID_REQUEST_PARAMETER, "유효하지 않는 조직입니다."));

        return Festival.builder()
                .admin(admin)
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }
}
