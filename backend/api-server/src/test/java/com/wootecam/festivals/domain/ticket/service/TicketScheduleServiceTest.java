package com.wootecam.festivals.domain.ticket.service;

import static com.wootecam.festivals.domain.ticket.service.TicketScheduleServiceTestFixture.createMembers;
import static com.wootecam.festivals.domain.ticket.service.TicketScheduleServiceTestFixture.createSaleOngoingTickets;
import static com.wootecam.festivals.domain.ticket.service.TicketScheduleServiceTestFixture.createSaleUpcomingTicketsAfterTenMinutes;
import static com.wootecam.festivals.domain.ticket.service.TicketScheduleServiceTestFixture.createSaleUpcomingTicketsWithinTenMinutes;
import static com.wootecam.festivals.domain.ticket.service.TicketScheduleServiceTestFixture.createUpcomingFestival;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.TicketInfo;
import com.wootecam.festivals.domain.purchase.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.TicketStockRedisRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@SpringBootTest
@DisplayName("TicketScheduleService 통합 테스트")
class TicketScheduleServiceTest extends SpringBootTestConfig {

    @Autowired
    private TicketScheduleService ticketScheduleService;

    @Autowired
    private TicketInfoRedisRepository ticketInfoRedisRepository;

    @Autowired
    private TicketStockRedisRepository ticketStockRedisRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TicketStockRepository ticketStockRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private List<Ticket> saleUpcomingTicketsWithinTenMinutes;
    private List<Ticket> saleUpcomingTicketsAfterTenMinutes;
    private List<Ticket> saleOngoingTickets;
    private List<Ticket> saleCompletedTickets;

    @BeforeEach
    void setUp() {
        clear();
        Member admin = createMembers(1).get(0);
        memberRepository.save(admin);

        Festival festival = createUpcomingFestival(admin);
        festivalRepository.save(festival);

        saleUpcomingTicketsWithinTenMinutes = createSaleUpcomingTicketsWithinTenMinutes(2, festival);
        saleUpcomingTicketsAfterTenMinutes = createSaleUpcomingTicketsAfterTenMinutes(2, festival);
        saleOngoingTickets = createSaleOngoingTickets(2, festival);

        // Ticket Builder 의 TicketValidator 정책 상 판매 종료 시간이 현재 시간 이전인 티켓을 저장할 수 없기 때문에 JdbcTemplate 으로 직접 저장
        // JpaRepository 로 fetch 를 하면 NoArgsConstructor 로 Ticket Builder 의 TicketValidator 를 우회할 수 있음
        saleCompletedTickets = createSaleCompletedTicketsWithJdbc(2, festival);

        ticketRepository.saveAll(saleUpcomingTicketsWithinTenMinutes);
        ticketRepository.saveAll(saleUpcomingTicketsAfterTenMinutes);
        ticketRepository.saveAll(saleOngoingTickets);
//        ticketRepository.saveAll(saleCompletedTickets);

        List<TicketStock> saleUpcomingTicketWithinTenMinutesStocks = saleUpcomingTicketsWithinTenMinutes.stream()
                .flatMap(ticket -> ticket.createTicketStock().stream()).toList();
        List<TicketStock> saleUpcomingTicketAfterTenMinutesStocks = saleUpcomingTicketsAfterTenMinutes.stream()
                .flatMap(ticket -> ticket.createTicketStock().stream()).toList();
        List<TicketStock> saleOngoingTicketStocks = saleOngoingTickets.stream()
                .flatMap(ticket -> ticket.createTicketStock().stream()).toList();
        List<TicketStock> saleCompletedTicketStocks = saleCompletedTickets.stream()
                .flatMap(ticket -> ticket.createTicketStock().stream()).toList();

        ticketStockRepository.saveAll(saleUpcomingTicketWithinTenMinutesStocks);
        ticketStockRepository.saveAll(saleUpcomingTicketAfterTenMinutesStocks);
        ticketStockRepository.saveAll(saleOngoingTicketStocks);
        ticketStockRepository.saveAll(saleCompletedTicketStocks);
    }

    @Nested
    @DisplayName("scheduleRedisTicketInfoUpdate 메소드는")
    class DescribeScheduleRedisTicketInfoUpdate {

        @Test
        @DisplayName("10분 이내에 판매 시작되는 티켓만 Redis에 업데이트한다")
        void itUpdatesOnlyUpcomingTicketsWithinTenMinutes() {
            // When
            ticketScheduleService.scheduleRedisTicketInfoUpdate();

//            Set<String> keys = redisTemplate.keys("*");
//            for (String key : keys) {
//                if (redisTemplate.type(key) == DataType.HASH) {
//                    Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
//                    System.out.println("Hash Key: " + key);
//                    for (Map.Entry<Object, Object> entry : entries.entrySet()) {
//                        System.out.println("  Field: " + entry.getKey() + ", Value: " + entry.getValue());
//                    }
//                }
//            }

            // Then
            saleUpcomingTicketsWithinTenMinutes.forEach(ticket -> {
                TicketInfo ticketInfo = ticketInfoRedisRepository.getTicketInfo(ticket.getId());
                assertThat(ticketInfo).isNotNull();
                assertThat(ticketInfo.startSaleTime()).isEqualTo(ticket.getStartSaleTime());
                assertThat(ticketInfo.endSaleTime()).isEqualTo(ticket.getEndSaleTime());
            });

            saleUpcomingTicketsAfterTenMinutes.forEach(ticket -> {
                TicketInfo ticketInfo = ticketInfoRedisRepository.getTicketInfo(ticket.getId());
                assertThat(ticketInfo).isNull();
            });

            saleOngoingTickets.forEach(ticket -> {
                TicketInfo ticketInfo = ticketInfoRedisRepository.getTicketInfo(ticket.getId());
                assertThat(ticketInfo).isNull();
            });

            saleCompletedTickets.forEach(ticket -> {
                TicketInfo ticketInfo = ticketInfoRedisRepository.getTicketInfo(ticket.getId());
                assertThat(ticketInfo).isNull();
            });
        }
    }

    @Nested
    @DisplayName("scheduleRedisTicketRemainStockUpdate 메소드는")
    class DescribeScheduleRedisTicketRemainStockUpdate {

        @Test
        @DisplayName("현재 판매 중인 티켓의 재고만 Redis에 업데이트한다")
        void itUpdatesOnlyOngoingTicketsStock() {
            // When
            ticketScheduleService.scheduleRedisTicketRemainStockUpdate();

            // Then
            saleOngoingTickets.forEach(ticket -> {
                String stockCount = ticketStockRedisRepository.getTicketStockCount(ticket.getId());
                assertThat(stockCount).isNotNull();
                assertThat(Long.parseLong(stockCount)).isEqualTo(ticket.getQuantity());
            });

            saleUpcomingTicketsWithinTenMinutes.forEach(ticket -> {
                assertThat(ticketStockRedisRepository.getTicketStockCount(ticket.getId())).isNull();
            });

            saleUpcomingTicketsAfterTenMinutes.forEach(ticket -> {
                assertThat(ticketStockRedisRepository.getTicketStockCount(ticket.getId())).isNull();
            });

            saleCompletedTickets.forEach(ticket -> {
                assertThat(ticketStockRedisRepository.getTicketStockCount(ticket.getId())).isNull();
            });
        }
    }

    private List<Ticket> createSaleCompletedTicketsWithJdbc(int count, Festival festival) {
        List<Long> ticketIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= count; i++) {
            String sql =
                    "INSERT INTO ticket (is_deleted, ticket_quantity, created_at, end_refund_time, end_sale_time, " +
                            "festival_id, start_sale_time, ticket_price, updated_at, ticket_detail, ticket_name) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            int finalI = i;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setBoolean(1, false);
                ps.setInt(2, finalI * 10);
                ps.setTimestamp(3, Timestamp.valueOf(now.minusDays(3)));
                ps.setTimestamp(4, Timestamp.valueOf(festival.getEndTime().minusDays(1)));
                ps.setTimestamp(5, Timestamp.valueOf(now.minusDays(1)));
                ps.setLong(6, festival.getId());
                ps.setTimestamp(7, Timestamp.valueOf(now.minusDays(2)));
                ps.setLong(8, finalI * 1000L);
                ps.setTimestamp(9, Timestamp.valueOf(now.minusDays(3)));
                ps.setString(10, "Completed ticket detail " + finalI);
                ps.setString(11, "Completed ticket " + finalI);
                return ps;
            }, keyHolder);

            Long ticketId = keyHolder.getKey().longValue();
            ticketIds.add(ticketId);
        }

        return ticketRepository.findAllById(ticketIds);
    }
}