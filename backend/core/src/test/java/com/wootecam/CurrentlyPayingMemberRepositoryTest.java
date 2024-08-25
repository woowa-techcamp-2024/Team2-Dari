package com.wootecam;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("CurrentlyPayingMemberRepository 테스트")
class CurrentlyPayingMemberRepositoryTest {

    @Autowired
    private CurrentlyPayingMemberRepository repository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Long TEST_TICKET_ID = 1L;
    private static final Long TEST_USER_ID = 100L;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("addCurrentlyPayingMember 메소드는")
    class Describe_addCurrentlyPayingMember {

        @Nested
        @DisplayName("새로운 결제 중인 회원을 추가할 때")
        class Context_with_new_paying_member {

            @Test
            @DisplayName("1을 반환한다")
            void it_returns_one() {
                Long result = repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isEqualTo(1L);
            }

            @Test
            @DisplayName("실제로 Set에 회원이 추가된다")
            void it_actually_adds_member_to_set() {
                repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                Boolean isMember = repository.isCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(isMember).isTrue();
            }
        }

        @Nested
        @DisplayName("이미 결제 중인 회원을 다시 추가할 때")
        class Context_with_existing_paying_member {

            @BeforeEach
            void setUp() {
                repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
            }

            @Test
            @DisplayName("0을 반환한다")
            void it_returns_zero() {
                Long result = repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isZero();
            }

            @Test
            @DisplayName("Set의 크기는 변하지 않는다")
            void it_does_not_change_set_size() {
                Long initialSize = redisTemplate.opsForSet().size(repository.TICKETS_PREFIX + TEST_TICKET_ID + ":" + repository.CURRENTLY_PAYING_MEMBERS_PREFIX);
                repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                Long finalSize = redisTemplate.opsForSet().size(repository.TICKETS_PREFIX + TEST_TICKET_ID + ":" + repository.CURRENTLY_PAYING_MEMBERS_PREFIX);
                assertThat(finalSize).isEqualTo(initialSize);
            }
        }
    }

    @Nested
    @DisplayName("removeCurrentlyPayingMember 메소드는")
    class Describe_removeCurrentlyPayingMember {

        @Nested
        @DisplayName("존재하는 결제 중인 회원을 제거할 때")
        class Context_with_existing_paying_member {

            @BeforeEach
            void setUp() {
                repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
            }

            @Test
            @DisplayName("1을 반환한다")
            void it_returns_one() {
                Long result = repository.removeCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isEqualTo(1L);
            }

            @Test
            @DisplayName("실제로 Set에서 회원이 제거된다")
            void it_actually_removes_member_from_set() {
                repository.removeCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                Boolean isMember = repository.isCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(isMember).isFalse();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 결제 중인 회원을 제거하려 할 때")
        class Context_with_non_existing_paying_member {

            @Test
            @DisplayName("0을 반환한다")
            void it_returns_zero() {
                Long result = repository.removeCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isZero();
            }

            @Test
            @DisplayName("Set의 크기는 변하지 않는다")
            void it_does_not_change_set_size() {
                Long initialSize = redisTemplate.opsForSet().size(repository.TICKETS_PREFIX + TEST_TICKET_ID + ":" + repository.CURRENTLY_PAYING_MEMBERS_PREFIX);
                repository.removeCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                Long finalSize = redisTemplate.opsForSet().size(repository.TICKETS_PREFIX + TEST_TICKET_ID + ":" + repository.CURRENTLY_PAYING_MEMBERS_PREFIX);
                assertThat(finalSize).isEqualTo(initialSize);
            }
        }
    }

    @Nested
    @DisplayName("isCurrentlyPayingMember 메소드는")
    class Describe_isCurrentlyPayingMember {

        @Nested
        @DisplayName("결제 중인 회원이 존재할 때")
        class Context_with_existing_paying_member {

            @BeforeEach
            void setUp() {
                repository.addCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
            }

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                Boolean result = repository.isCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("결제 중인 회원이 존재하지 않을 때")
        class Context_with_non_existing_paying_member {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                Boolean result = repository.isCurrentlyPayingMember(TEST_TICKET_ID, TEST_USER_ID);
                assertThat(result).isFalse();
            }
        }
    }
}