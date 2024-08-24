package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_STOCK_KEY;
import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_TIMESTAMP_KEY;
import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static com.wootecam.festivals.utils.Fixture.createTicket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockJdbcRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.config.CustomMapSessionRepository;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.session.MapSession;

class PurchasableAuthorityCleanupScheduleServiceTest extends SpringBootTestConfig {

    @MockBean
    private TimeProvider timeProvider;

    @Autowired
    private CustomMapSessionRepository sessionRepository;

    @Autowired
    private PurchaseAuthorityCleanupScheduleService purchaseCleanupService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketStockRepository ticketStockRepository;

    @Autowired
    private TicketStockJdbcRepository ticketStockJdbcRepository;

    @Nested
    @DisplayName("cleanUpSessions 메소드는")
    class Describe_cleanUpSessions {

        private Festival festival;
        private Ticket ticket;
        private LocalDateTime ticketSaleStartTime = LocalDateTime.now();

        @BeforeEach
        void setUp() {
            clear();

            Member admin = memberRepository.save(createMember("admin", "admin@test.com"));
            festival = festivalRepository.save(createFestival(admin, "Test Festival", "Test Festival Detail",
                    ticketSaleStartTime.plusDays(1), ticketSaleStartTime.plusDays(4)));
            ticket = ticketRepository.save(
                    createTicket(festival, 10000L, 100, ticketSaleStartTime, ticketSaleStartTime.plusDays(3)));
            ticketStockJdbcRepository.saveTicketStocks(ticket.createTicketStock());
        }

        @Nested
        @DisplayName("만료된 티켓 구매 권한이 있는 세션이 있을 때")
        class Context_withExpiredTicketPurchasePermissions {

            private MapSession session1;

            @BeforeEach
            void setUp() {
                session1 = sessionRepository.createSession();
                session1.setAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY,
                        LocalDateTime.parse("2021-08-01T00:00:00").toString());
                session1.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, ticket.getId());
                sessionRepository.save(session1);

                given(timeProvider.getCurrentTime()).willReturn(LocalDateTime.parse("2021-08-04T00:00:01"));
            }

            @Test
            @DisplayName("만료된 티켓 구매 권한을 삭제한다")
            void cleanUpSessions_removesExpiredTicketPurchasePermissions() {
                // When
                purchaseCleanupService.cleanUpSessions();

                // Then
                MapSession savedSession1 = sessionRepository.findById(session1.getId());
                assertThat((String) savedSession1.getAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY)).isNull();
                assertThat((Long) savedSession1.getAttribute(PURCHASABLE_TICKET_STOCK_KEY)).isNull();
            }
        }

        @Nested
        @DisplayName("만료되지 않은 티켓 구매 권한이 있는 세션이 있을 때")
        class Context_withNonExpiredTicketPurchasePermissions {

            private MapSession session1;

            @BeforeEach
            void setUp() {
                session1 = sessionRepository.createSession();
                session1.setAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY,
                        LocalDateTime.parse("2021-08-04T00:00:00").toString());
                session1.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, ticket.getId());
                sessionRepository.save(session1);

                given(timeProvider.getCurrentTime()).willReturn(LocalDateTime.parse("2021-08-01T00:00:00"));
            }

            @Test
            @DisplayName("만료되지 않은 티켓 구매 권한을 삭제하지 않는다")
            void cleanUpSessions_doesNotRemoveNonExpiredPermissions() {
                // When
                purchaseCleanupService.cleanUpSessions();

                // Then
                MapSession savedSession1 = sessionRepository.findById(session1.getId());
                assertThat((String) savedSession1.getAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY)).isNotNull();
                assertThat((Long) savedSession1.getAttribute(PURCHASABLE_TICKET_STOCK_KEY)).isNotNull();
            }
        }
    }
}
