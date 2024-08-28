package com.wootecam.festivals.domain.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockJdbcRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class PurchaseSyncTest extends SpringBootTestConfig {

    @Autowired
    private PurchaseService purchaseService;

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

    @BeforeEach
    void setup() {
        clear();
    }

    @Test
    @DisplayName("유저가 티켓을 점유하는 로직 PurchaseFacadeService의 checkPurchasable 메소드 테스트")
    void purchase() throws InterruptedException {
        int customerCount = 100;
        int ticketCount = 10;

        Member admin = createMember();
        Festival festival = createFestival(admin);
        Ticket ticket = createTicket(festival, ticketCount);

        List<Member> customers = createMembers(customerCount);
        ExecutorService executorService = Executors.newFixedThreadPool(customerCount);
        CountDownLatch latch = new CountDownLatch(customerCount);

        AtomicInteger ticketStockDecreaseSuccessCount = new AtomicInteger();
        AtomicInteger ticketStockEmptyCount = new AtomicInteger();
        AtomicInteger ticketStockDecreaseExceptionCount = new AtomicInteger();

        for (Member customer : customers) {
            executorService.submit(() -> {
                try {

                    PurchasableResponse purchasableResponse = purchaseService.checkPurchasable(ticket.getId(),
                            customer.getId(), LocalDateTime.now());
                    if (purchasableResponse.purchasable()) {
                        ticketStockDecreaseSuccessCount.incrementAndGet();
                    } else {
                        ticketStockEmptyCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    ticketStockDecreaseExceptionCount.incrementAndGet();
                    e.printStackTrace();
                    log.error("fail message : {} customerId : {}", e.getMessage(), customer.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertAll(
                () -> assertThat(ticketStockDecreaseSuccessCount.get()).
                        as("티켓 재고 차감 성공 횟수").
                        isEqualTo(ticketCount),
                () -> assertThat(ticketStockEmptyCount.get()).
                        as("티켓 재고 차감 실패 횟수").
                        isEqualTo(customerCount - ticketCount),
                () -> assertThat(ticketStockDecreaseExceptionCount.get()).
                        as("티켓 재고 차감 시 예외 발생 횟수").
                        isEqualTo(0)
        );
    }

    private Member createMember() {
        return memberRepository.save(Member.builder()
                .name("name")
                .email("email")
                .build());
    }

    private Ticket createTicket(Festival festival, int quantity) {
        Ticket saved = ticketRepository.save(Ticket.builder()
                .name("티켓")
                .detail("티켓 설명")
                .startSaleTime(festival.getStartTime())
                .endSaleTime(festival.getEndTime())
                .refundEndTime(festival.getEndTime())
                .festival(festival)
                .quantity(quantity)
                .price(10000L)
                .build());
        List<TicketStock> ticketStocks = saved.createTicketStock();
        ticketStockJdbcRepository.saveTicketStocks(ticketStocks);

        return saved;
    }

    private Festival createFestival(Member admin) {
        return festivalRepository.save(Festival.builder()
                .admin(admin)
                .title("페스티벌")
                .description("페스티벌 설명")
                .festivalImg("페스티벌 이미지")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .festivalProgressStatus(FestivalProgressStatus.UPCOMING)
                .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                .build());
    }

    private List<Member> createMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> memberRepository.save(Member.builder()
                        .name("name" + index)
                        .email("email" + index)
                        .build()))
                .toList();
    }
}
