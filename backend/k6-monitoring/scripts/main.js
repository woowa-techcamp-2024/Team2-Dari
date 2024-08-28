import http from 'k6/http';
import {check, sleep} from 'k6';
import exec from 'k6/execution';

const MAX_USER = 6000;
const BASE_ORIGIN = 'http://localhost:8080';
const BASE_URL = BASE_ORIGIN + '/api/v1';

export const options = {
    scenarios: {
        // login: {
        //     executor: 'per-vu-iterations',
        //     vus: MAX_USER,
        //     iterations: 1,
        //     exec: 'loginTest',
        //     startTime: '0s',  // 로그인은 즉시 시작
        // },
        getFestivalList: {
            executor: 'per-vu-iterations',
            vus: MAX_USER,
            iterations: 1,
            exec: 'getFestivalListTest',
            startTime: '0s',
        },
        getFestivalDetail: {
            executor: 'per-vu-iterations',
            vus: MAX_USER,
            iterations: 1,
            exec: 'getFestivalDetailTest',
            startTime: '0s',
        },
        getTicketList: {
            executor: 'per-vu-iterations',
            vus: MAX_USER,
            iterations: 1,
            exec: 'getTicketListTest',
            startTime: '0s',
        },
        checkPurchasableAndPurchase: {
            executor: 'per-vu-iterations',
            vus: MAX_USER,
            iterations: 1,
            exec: 'checkPurchasableAndPurchaseTest',
            startTime: '0s',
        }
    },
    setupTimeout: '3m',
};

export async function setup() {
    console.log('Starting setup...');

    const usersToLogin = MAX_USER;

    const loggedInUsers = [];

    for (let i = 1; i <= usersToLogin; i++) {
        const email = `user${i}@example.com`;
        const sessionCookie = await login(email);
        if (sessionCookie) {
            loggedInUsers.push({email, sessionCookie});
        }
    }

    console.log(`Logged in ${loggedInUsers.length} users`);

    return {loggedInUsers};
}

// 로그인 테스트
export function loginTest(data) {
    const uniqueUserId = exec.scenario.iterationInTest;
    const user = data.loggedInUsers[uniqueUserId];
    const sessionCookie = login(user.email);
    sleep(0.3);
}

// 페스티벌 목록 조회 테스트
export function getFestivalListTest(data) {
    const uniqueUserId = exec.scenario.iterationInTest;
    const user = data.loggedInUsers[uniqueUserId];
    getFestivalList(user.sessionCookie, null, 10);
    sleep(1);
}

// 페스티벌 상세 조회 테스트
export function getFestivalDetailTest(data) {
    const uniqueUserId = exec.scenario.iterationInTest;
    const user = data.loggedInUsers[uniqueUserId];
    getFestivalDetail(1, user.sessionCookie);
    sleep(1);
}

// 티켓 목록 조회 테스트
export function getTicketListTest(data) {
    const uniqueUserId = exec.scenario.iterationInTest;
    const user = data.loggedInUsers[uniqueUserId];
    getTicketList(1, user.sessionCookie);
    sleep(1);
}

// 티켓 결제 가능 여부 확인 후 결제 테스트
export function checkPurchasableAndPurchaseTest(data) {
    const uniqueUserId = exec.scenario.iterationInTest;
    const user = data.loggedInUsers[uniqueUserId];

    const sessionInfo = checkPurchasable(1, 1, user.sessionCookie, user.email);
    sleep(0.5);

    if (sessionInfo != null) {
        const ticketInfo = getTicketInfo(sessionInfo.festivalId, sessionInfo.ticketId, sessionInfo.sessionCookie, sessionInfo.purchaseSession);
        sleep(0.5);
        if (ticketInfo) {
            purchaseTicket(sessionInfo.festivalId, sessionInfo.ticketId, sessionInfo.sessionCookie, user.email, sessionInfo.purchaseSession);
        }
    }
}


async function login(email) {
    const loginUrl = `${BASE_URL}/auth/login`;
    const payload = JSON.stringify({email: email});
    const params = {
        headers: {'Content-Type': 'application/json'},
    };

    // 쿠키 저장소를 가져오기
    const jar = http.cookieJar();
    jar.clear(BASE_ORIGIN);

    const res = await http.post(loginUrl, payload, params);
    const success = res.status === 200;

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

    return checkRes ? JSON.parse(response.body).data.tickets : null;
}

// 티켓 결제 가능 여부 조회
function checkPurchasable(festivalId, ticketId, sessionCookie, email) {
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
        console.error('Check Purchasable failed:', email, response.status, response.body);
    }

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

    return checkRes ? JSON.parse(response.body).data : null;
}

// 티켓 결제
function purchaseTicket(festivalId, ticketId, sessionCookie, email) {
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
        console.error('Purchase ticket failed:', response.status, response.body, email);
        console.log(sessionCookie, email)
    }

    return checkRes ? JSON.parse(response.body).data : null;
}
