package com.wootecam.festivals.docs.utils;

import com.wootecam.festivals.domain.organization.entity.OrganizationRole;
import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.docs.TestController.GreetStatus;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @api {get} /test/enums 01. enum 타입 문서화를 위한 컨트롤러. (문서용으로만 사용)
 */
@RestController
@RequestMapping("/test")
public class CommonDocController {

    @GetMapping("/enums")
    public EnumDocs findEnums() {
        Map<String, String> greetStatus = getDocs(GreetStatus.values());
        Map<String, String> organizationRole = getDocs(OrganizationRole.values());

        return new EnumDocs(greetStatus, organizationRole);
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getName, EnumType::getDescription));
    }
}