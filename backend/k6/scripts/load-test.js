import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const failureRate = new Rate('failed_requests');

export const options = {
    stages: [
        { duration: '1m', target: 500 },
        { duration: '3m', target: 500 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<100'],
        failed_requests: ['rate<0.1'],
    },
    setupTimeout: '3m',
};

const BASE_URL = 'http://localhost:8080/api/v1';

// 로그인 함수
async function login(email) {
    const loginUrl = `${BASE_URL}/auth/login`;
    const payload = JSON.stringify({ email: email });
    const params = {
        headers: { 'Content-Type': 'application/json' },
    };
    const res = await http.post(loginUrl, payload, params);
    const success = check(res, {
        'login successful': (r) => r.status === 200,
    });

    if (!success) {
        console.error('Login failed:', res.status, res.body);
        return null;
    }

    // 쿠키 추출
    const cookieHeaders = res.request.headers['Cookie'];
    if (!cookieHeaders || cookieHeaders.length === 0) {
        console.error('No Cookie header found');
        return null;
    }

    // 쿠키를 문자열로 변환
    const cookieHeaderString = cookieHeaders.join('; ');

    // 세션 쿠키 파싱
    const sessionCookieMatch = cookieHeaderString.match(/SESSION=[^;]+/);
    if (!sessionCookieMatch) {
        console.error('No SESSION cookie found');
        return null;
    }

    return sessionCookieMatch[0];
}



// 페스티벌 목록 조회
function getFestivalList(sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals`, {
        headers: { 'Cookie': sessionCookie }
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

    sleep(1);
}

// 페스티벌 상세 조회
function getFestivalDetail(festivalId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}`, {
        headers: { 'Cookie': sessionCookie }
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

    sleep(1);
}

// 구매할 티켓 조회
function getTicketInfo(festivalId, ticketId, sessionCookie) {
    const response = http.get(`${BASE_URL}/festivals/${festivalId}/tickets/${ticketId}/purchase`, {
        headers: { 'Cookie': sessionCookie }
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
    }

    failureRate.add(!checkRes);

    sleep(1);
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
    }

    failureRate.add(!checkRes);

    sleep(1);
}

export async function setup() {
    console.log('Starting setup...');

    const totalUsers = 100000;
    const usersToLogin = 1000;
    const totalFestivals = 1000;
    const ticketsPerFestival = 3;

    const loggedInUsers = [];
    for (let i = 0; i < usersToLogin; i++) {
        const userIndex = randomIntBetween(1, totalUsers);
        const email = `user${userIndex}@example.com`;
        const sessionCookie = await login(email);  // await 사용
        if (sessionCookie) {
            loggedInUsers.push({ email, sessionCookie });
        }
    }

    console.log(`Logged in ${loggedInUsers.length} users`);

    // festivals 데이터 구조에 ticketId 범위를 추가
    const festivals = Array.from({ length: totalFestivals }, (_, i) => {
        const festivalId = i + 1;
        const ticketIds = Array.from({ length: ticketsPerFestival }, (_, j) => (festivalId - 1) * ticketsPerFestival + j + 1);
        return { festivalId, ticketIds };
    });

    return { loggedInUsers, festivals };
}

export default function (data) {
    const user = data.loggedInUsers[Math.floor(Math.random() * data.loggedInUsers.length)];
    const festivalData = data.festivals[Math.floor(Math.random() * data.festivals.length)];
    const festivalId = festivalData.festivalId;

    // festivalId에 해당하는 ticketId 중 하나를 선택
    const ticketId = festivalData.ticketIds[Math.floor(Math.random() * festivalData.ticketIds.length)];

    const rand = Math.random();
    if (rand < 0.4) {
        getFestivalList(user.sessionCookie);
    } else if (rand < 0.7) {
        getFestivalDetail(festivalId, user.sessionCookie);
    } else if (rand < 0.9) {
        getTicketInfo(festivalId, ticketId, user.sessionCookie);
    } else {
        purchaseTicket(festivalId, ticketId, user.sessionCookie);
    }
}

