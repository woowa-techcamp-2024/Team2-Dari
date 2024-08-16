package com.wootecam.festivals.domain.purchase.controller;


import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ApiResponse<PurchaseIdResponse> createPurchase(@PathVariable Long festivalId, @PathVariable Long ticketId,
                                                          @AuthUser Authentication authentication) {
        return ApiResponse.of(purchaseService.createPurchase(ticketId, authentication.memberId(), LocalDateTime.now()));
    }
}
