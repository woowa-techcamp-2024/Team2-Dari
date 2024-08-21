package com.wootecam.festivals.domain.festival.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record ParticipantsPaginationResponse(List<ParticipantResponse> participants,
                                             int currentPage,
                                             int itemsPerPage,
                                             long totalItems,
                                             int totalPages,
                                             boolean hasNext,
                                             boolean hasPrevious) {
    public static ParticipantsPaginationResponse from(Page<ParticipantResponse> participantsPagination) {
        return new ParticipantsPaginationResponse(participantsPagination.getContent(),
                participantsPagination.getNumber(),
                participantsPagination.getSize(),
                participantsPagination.getTotalElements(),
                participantsPagination.getTotalPages(),
                participantsPagination.hasNext(),
                participantsPagination.hasPrevious());
    }
}
