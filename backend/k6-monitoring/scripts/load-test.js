import http from 'k6/http';
import {check, group, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';
import {randomIntBetween} from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import exec from 'k6/execution';

// 커스텀 메트릭 정의
const failureRate = new Rate('failed_requests');  // 실패한 요청 비율
const responseTime = new Trend('response_time');  // 응답 시간
const connectionPoolMetric = new Trend('connection_pool');  // 연결 풀 상태
const threadPoolMetric = new Trend('thread_pool');  // 스레드 풀 상태

// 테스트 구성 옵션
export const options = {
    // 단계별 부하 테스트 설정
    stages: [
        {duration: '30s', target: 500},  // 1분 동안 500명의 가상 사용자로 증가
        {duration: '20s', target: 500},  // 3분 동안 500명의 가상 사용자 유지
        {duration: '10s', target: 0},    // 1분 동안 0명으로 감소
    ],
    // 성능 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<100'],  // 95%의 요청이 100ms 이내에 완료되어야 함
        failed_requests: ['rate<0.1'],     // 실패율이 10% 미만이어야 함
        response_time: ['p(95) < 200'],    // 95%의 응답 시간이 200ms 미만이어야 함
    },
    setupTimeout: '3m',  // 셋업 단계의 최대 실행 시간
};

// const BASE_URL = 'http://13.125.202.151:8080/api/v1';  // API 기본 URL
const BASE_URL = 'http://localhost:8080/api/v1';  // API 기본 URL
// 로그인 함수
async function login(email) {
    const loginUrl = `${BASE_URL}/auth/login`;
    const payload = JSON.stringify({email: email});
    const params = {
        headers: {'Content-Type': 'application/json'},
    };

    // 쿠키 저장소를 가져오기
    const jar = http.cookieJar();
    jar.clear('http://localhost:8080');

    const res = await http.post(loginUrl, payload, params);
    const success = check(res, {
        'login successful': (r) => r.status === 200,
    });

    if (!success) {
        console.error('Login failed:', res.status, res.body);
        return null;
    }

    // 쿠키 추출 및 세션 쿠키 파싱
    const cookieHeaders = res.headers['Set-Cookie'];
    if (!cookieHeaders || cookieHeaders.length === 0) {
        console.error('No Cookie header found');
        return null;
    }

    return cookieHeaders;
}

// 페스티벌 목록 조회
function getFestivalList(sessionCookie, cursor, pageSize) {
    const url = `${BASE_URL}/festivals?${cursor ? `time=${cursor.time}&id=${cursor.id}&` : ''}pageSize=${pageSize}`;
    const response = http.get(url, {
        headers: {'Cookie': sessionCookie}
    });

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'response body has content': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body && body.data && body.data.content && body.data.content.length > 0;
            } catch (e) {
                console.error('Failed to parse getFestivalList response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Get festival list failed:', response.status, response.body);
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? JSON.parse(response.body).data : null;
}

// 페스티벌 상세 조회
function getFestivalDetail(festivalId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}`, {
        headers: {'Cookie': sessionCookie}
    });

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has festivalId': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body && body.data && body.data.festivalId === festivalId;
            } catch (e) {
                console.error('Failed to parse getFestivalDetail response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Get festival detail failed:', response.status, response.body);
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? JSON.parse(response.body).data : null;
}

// 티켓 목록 조회
function getTicketList(festivalId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}/tickets`, {
        headers: {'Cookie': sessionCookie}
    });

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has tickets': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body && body.data && body.data.tickets && body.data.tickets.length > 0;
            } catch (e) {
                console.error('Failed to parse getTicketList response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Get ticket list failed:', response.status, response.body);
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? JSON.parse(response.body).data.tickets : null;
}

// 티켓 결제 가능 여부 조회
function checkPurchasable(festivalId, ticketId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}/tickets/${ticketId}/purchase/check`, {
        headers: {'Cookie': sessionCookie}
    });
    let result;

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has purchasable': (r) => {
            try {
                const body = JSON.parse(r.body);
                if (body && body.data && body.data.purchasable !== undefined) {
                    result = {festivalId, ticketId, sessionCookie};
                    return true;
                }
                return false;
            } catch (e) {
                console.error('Failed to parse checkPurchasable response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Check Purchasable failed:', response.status, response.body);
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? result : null;
}

// 구매할 티켓 조회
function getTicketInfo(festivalId, ticketId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}/tickets/${ticketId}/purchase`, {
        headers: {'Cookie': sessionCookie}
    });

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has ticketInfo': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body && body.data && body.data.ticketId !== undefined;
            } catch (e) {
                console.error('Failed to parse getTicketInfo response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Get ticket info failed:', response.status, response.body);
        console.log(sessionCookie)
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? JSON.parse(response.body).data : null;
}

// 티켓 결제
function purchaseTicket(festivalId, ticketId, sessionCookie) {
    const response = http.post(`${BASE_URL}/festivals/${festivalId}/tickets/${ticketId}/purchase`, JSON.stringify({}), {
        headers: {
            'Content-Type': 'application/json',
            'Cookie': sessionCookie
        },
    });

    const checkRes = check(response, {
        'status is 200': (r) => r.status === 200,
        'purchase successful': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body && body.data && body.data.purchaseId !== undefined;
            } catch (e) {
                console.error('Failed to parse purchaseTicket response:', e);
                return false;
            }
        },
    });

    if (!checkRes) {
        console.error('Purchase ticket failed:', response.status, response.body);
        console.log(sessionCookie)
    }

    failureRate.add(!checkRes);
    responseTime.add(response.timings.duration);

    return checkRes ? JSON.parse(response.body).data : null;
}

// 테스트 설정 및 초기화
export async function setup() {
    console.log('Starting setup...');

    const totalUsers = 100000;
    const usersToLogin = 3000;
    const totalFestivals = 1000;
    const ticketsPerFestival = 3;

    // 사용자 로그인 및 세션 쿠키 획득
    const loggedInUsers = [];
    for (let i = 0; i < usersToLogin; i++) {
        const userIndex = randomIntBetween(1, totalUsers);
        const email = `user${userIndex}@example.com`;
        const sessionCookie = await login(email);
        let lastUsed = new Date().getTime() - 1000000;
        if (sessionCookie) {
            loggedInUsers.push({email, sessionCookie, lastUsed});
        }
    }

    console.log(`Logged in ${loggedInUsers.length} users`);

    // 축제id 및 티켓id 데이터 생성
    const festivals = Array.from({length: totalFestivals}, (_, i) => {
        const festivalId = i + 1;
        const ticketIds = Array.from({length: ticketsPerFestival}, (_, j) => (festivalId - 1) * ticketsPerFestival + j + 1);
        return {festivalId, ticketIds};
    });

    return {loggedInUsers, festivals};
}

// 메인 테스트 함수

export default function (data) {
    group('User Flow', function () {
        let now = new Date().getTime();
        let user = data.loggedInUsers[Math.floor(Math.random() * data.loggedInUsers.length)];
        while (now - user.lastUsed < 50000) {
            user = data.loggedInUsers[Math.floor(Math.random() * data.loggedInUsers.length)];
            now = new Date().getTime();
        }
        user.lastUsed = new Date().getTime();

        const festivalData = data.festivals[Math.floor(Math.random() * data.festivals.length)];
        const festivalId = festivalData.festivalId;
        const ticketId = festivalData.ticketIds[Math.floor(Math.random() * festivalData.ticketIds.length)];

        const rand = Math.random();
        // API 호출 비율에 따른 시나리오 실행
        if (rand < 0.4) {
            group('Festival List', function () {
                const festivalListData = getFestivalList(user.sessionCookie, null, 10);
                if (festivalListData && festivalListData.content && festivalListData.content.length > 0) {
                    const selectedFestival = festivalListData.content[Math.floor(Math.random() * festivalListData.content.length)];
                    getFestivalDetail(selectedFestival.festivalId, user.sessionCookie);
                }
            });
        } else if (rand < 0.65) {
            group('Festival Detail', function () {
                getFestivalDetail(festivalId, user.sessionCookie);
            });
        } else if (rand < 0.9) {
            group('Ticket List', function () {
                getTicketList(festivalId, user.sessionCookie);
            })
        } else {
            group('Purchase Ticket', function () {
                const sessionInfo = checkPurchasable(festivalId, ticketId, user.sessionCookie);
                sleep(0.5);
                if (sessionInfo != null) {
                    const ticketInfo = getTicketInfo(sessionInfo.festivalId, sessionInfo.ticketId, sessionInfo.sessionCookie);
                    sleep(0.5);
                    if (ticketInfo) {
                        purchaseTicket(sessionInfo.festivalId, sessionInfo.ticketId, sessionInfo.sessionCookie);
                    }
                }
            });
        }
    });

    // 시스템 메트릭 기록
    connectionPoolMetric.add(exec.instance.vusActive);
    threadPoolMetric.add(exec.instance.iterationsCompleted);

    sleep(0.5);  // 각 반복 사이에 0.5초 대기
}

/* 사용법:
k6 run load-test.js

grafana + influxdb 사용시
k6 run --out influxdb=http://localhost:8086/k6 load-test.js

옵션:
- 가상 사용자 수 변경: k6 run --vus 100 load-test.js
- 실행 시간 변경: k6 run --duration 30s load-test.js
- 결과를 파일로 저장: k6 run load-test.js --out json=results.json
*/