package com.wootecam.festivals.domain.festival.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record ParticipantResponse(Long participantId, String participantName, String participantEmail,
                                  Long ticketId, String ticketName,
                                  Long purchaseId,
                                  @JsonSerialize(using = CustomLocalDateTimeSerializer.class) LocalDateTime purchaseTime,
                                  Long checkinId, boolean isCheckin) {
}
