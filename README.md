# 🎫 축제의 민족

## 📌 프로젝트 개요

이 프로젝트는 제한된 리소스 환경(AWS t3.small EC2 인스턴스 2대, RDS, Redis)에서 안정적인 운영을 목표로 하는 고성능 페스티벌 티켓 예매 시스템입니다. 순간적인 대규모 트래픽 상황에서도
안정적으로 동작하도록 설계되었으며, 효율적인 대기열 관리와 비동기 처리를 통해 사용자 경험을 최적화합니다.

### 프로젝트 목표

- 초당 1,000건 이상의 주문 처리
- 평균 응답 시간 100ms 이하
- 시스템 가용성 99.9% 이상

## 🚀 핵심 기능

1. **실시간 대기열 시스템**: Redis 기반의 공정한 대기열 관리
2. **안정적인 결제 처리**: 비동기 방식의 결제 처리 및 실시간 상태 조회
3. **효율적인 재고 관리**: Redis를 활용한 실시간 재고 추적
4. **비동기 주문 처리**: 인메모리 큐 시스템을 통한 주문 처리 최적화

## 👀 데모
### 메인 화면

<img src="https://github.com/user-attachments/assets/c2b894f4-d381-45f1-9df6-28fead36171e" alt="메인페이지" width="400">

### 마이 페이지

<img src="https://github.com/user-attachments/assets/87df9a5f-07df-4cf8-a464-f042014fd8a6" alt="마이페이지" width="400">

### 구매 페이지

<img src="https://github.com/user-attachments/assets/4cfc7c55-f86b-4f69-9416-7817bced7235" alt="구매 성공" width="400">

### 어드민 페이지

<img src="https://github.com/user-attachments/assets/eacdc384-18a8-48eb-832a-3ce88bc20230" alt="어드민 페이지" width="600">

### 체크인

<img src="https://github.com/user-attachments/assets/7fbeccd3-f301-43dd-b489-886ad27a4c4e" alt="마이페이지" width="600">

## 🛠 기술 스택

<p align="center">
  <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white" alt="Spring" />
  <img src="https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white" alt="Redis" />
  <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" />
  <img src="https://img.shields.io/badge/grafana-%23F46800.svg?style=for-the-badge&logo=grafana&logoColor=white" alt="Grafana" />
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white" alt="Prometheus" />
  <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white" alt="k6" />
</p>

## 📐 아키텍처

![아키텍처.png](img.png)

## 🔧 핵심 컴포넌트

### 1. 대기열 시스템 (WaitOrderService)

- 원리: 청크 기반의 대기열 관리 시스템
- 대기열 관리:
   - Redis Set을 사용하여 각 티켓에 대한 대기열 유지
   - 사용자 진입 시 현재 대기열의 크기를 기반으로 대기 순서 할당
- 입장 관리:
   - passChunkSize 단위로 입장 가능한 범위 관리
   - 주기적으로 입장 가능 범위를 passChunkSize만큼 증가시켜 순차적 입장 허용
- 입장 가능 여부 확인:
   - 사용자의 대기 순서가 현재 입장 가능 범위 내에 있는지 확인
   - 재고 여부도 함께 체크하여 입장 가능 여부 결정

### 2. 결제 시스템 (PurchaseFacadeService, PaymentService)

- 원리: 비동기 결제 처리 및 상태 관리
- 결제 프로세스:
   - 결제 요청 시 UUID 기반의 결제 ID 생성 후 비동기로 결제 처리 시작
   - CompletableFuture를 사용하여 비동기 결제 처리 구현
- 상태 관리:
   - Caffeine 캐시를 사용하여 결제 상태 관리
   - 결제 ID를 키로 사용하여 결제 정보 및 상태 저장
- 결제 완료 후 처리:
   - 결제 성공 시 QueueService를 통해 구매 정보 처리
   - 결제 실패 시 CompensationService를 통해 재고 및 상태 롤백

### 3. 재고 관리 (TicketStockCountRedisRepository)

- 원리: Redis를 활용한 실시간 재고 관리
- 재고 관리:
   - Redis에 각 티켓의 재고 수량 저장
   - 원자적 감소 연산을 사용하여 동시성 문제 해결
- 재고 동기화:
   - TicketScheduleService를 통해 주기적으로 Redis의 재고 정보 업데이트
   - 판매 시작 전 또는 진행 중인 티켓의 재고 정보만 Redis에 유지

### 4. 주문 처리 (QueueService)

- 원리: 인메모리 큐를 활용한 비동기 주문 처리
- 주문 접수:
   - InMemoryQueue에 구매 데이터(PurchaseData) 저장
   - 큐가 가득 찼을 경우 지수 백오프를 적용한 재시도 로직 구현
- 주문 처리:
   - 주기적으로(5초마다) 큐에서 배치 단위로 주문 데이터 처리
   - 배치 크기는 큐의 현재 크기에 따라 동적으로 조정
- 데이터 일관성:
   - 주문 정보와 체크인 정보를 트랜잭션으로 묶어 일괄 처리
   - JDBC batch update를 사용하여 데이터베이스 연산 최적화
- 장애 복구:
   - 서버 재시작 시 로그 파일을 분석하여 미처리된 주문 복구
   - 처리 실패한 주문에 대한 재시도 메커니즘 구현

## 📈 성능 최적화 전략

1. **인메모리 캐싱 및 데이터 관리**
   - Redis를 활용한 대기열, 재고, 결제 상태 관리
   - Caffeine 캐시를 이용한 애플리케이션 레벨 캐싱
   - 캐시 워밍으로 콜드 스타트 문제 해결 및 초기 응답 시간 개선

2. **비동기 및 병렬 처리**
   - CompletableFuture를 이용한 비동기 결제 처리
   - 주문 처리를 위한 커스텀 인메모리 큐 구현
   - 배치 처리를 통한 데이터베이스 쓰기 최적화

3. **데이터베이스 최적화**
   - Skip Lock 적용으로 동시성 제어 및 성능 향상 (`SELECT ... FOR UPDATE SKIP LOCKED`)
   - 커넥션 풀 최적화 (HikariCP 튜닝)
   - 인덱스 최적화 및 쿼리 튜닝
   - JDBC 배치 업데이트를 통한 벌크 연산 효율화

4. **대기열 시스템 설계**
   - Redis Sorted Set을 활용한 공정한 대기열 관리
   - 청크 단위의 입장 처리로 시스템 부하 분산
   - 동적 대기열 크기 조정으로 리소스 활용 최적화

5. **시스템 안정성 및 복구 전략**
   - 적절한 타임아웃 설정 및 서킷 브레이커 패턴 적용
   - 지수 백오프를 활용한 재시도 로직 구현
   - 장애 상황 대비 복구 메커니즘 구현 (로그 기반 복구)

6. **모니터링 및 성능 분석**
   - Grafana와 Prometheus를 활용한 실시간 시스템 모니터링
   - 사용자 정의 메트릭을 통한 비즈니스 로직 모니터링
   - 성능 병목 지점 식별 및 지속적인 개선

## 👥 팀 소개

저희는 **팀 twoDari**입니다. 사용자와 축제를 이어주는 다리 역할을 합니다.

| 이름  | 역할  | 주요 기여                                | GitHub                                           |
|-----|-----|--------------------------------------|--------------------------------------------------|
| 김현종 | 백엔드 | MySQL비동기 처리, 배치 처리, 프론트엔드 화면 구성      | [@bellringstar](https://github.com/bellringstar) |
| 김규원 | 백엔드 | 캐싱전략, Redis를 사용한 대기열, 프론트엔드 화면 구성    | [@kkyu0718](https://github.com/kkyu0718)         |
| 김현준 | 백엔드 | devops, 스케쥴링 작업, 다양한 모듈 간 연계 및 성능 개선 | [@HyeonJun0530](https://github.com/HyeonJun0530) |
| 박민지 | 백엔드 | Redis를 사용한 대기열 로직, 프로젝트 모듈화          | [@minnim1010](https://github.com/minnim1010)     |

## 📊 성능 테스트 결과

![test_result.png](img_1.png)


