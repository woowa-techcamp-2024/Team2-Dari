package com.wootecam.festivals.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 대기열 조회 테스트를 위한 클래스입니다. MAX_USER 만큼 스레드를 생성하여 totalIterations만큼 대기열 조회를 수행합니다. 이미 통과한 사용자는 다시 조회하지 않습니다. 대기열 조회는 3초
 * 간격으로 수행됩니다.
 * <p>
 * 테스트 시 주의사항: 로그인을 위해 festival application 실행, 대기열 조회를 위해 queue application 실행이 필요합니다. festival application은 8080 포트,
 * queue application은 8081 포트로 실행되어야 합니다.
 */
public class EventTest {

    private static final int MAX_USER = 100;
    private static final String LOGIN_ORIGIN = "http://localhost:8080";
    private static final String LOGIN_URL = LOGIN_ORIGIN + "/api/v1";
    private static final String BASE_ORIGIN = "http://localhost:8081";
    private static final String BASE_URL = BASE_ORIGIN + "/api/v1";
    private static final Set<String> pass = new HashSet<>();
    private static final Map<String, Integer> waitOrderMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

    /**
     * MAX_USER 만큼 스레드를 생성하여 totalIterations만큼 대기열 조회를 수행합니다.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_USER);

        List<UserSession> loggedInUsers = setup();

        int totalIterations = MAX_USER * 10;

        for (int i = 0; i < totalIterations; i++) {
            int iteration = i;
            executor.submit(() -> {
                try {
                    executeTest(loggedInUsers, iteration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }

    /**
     * 사용자 로그인을 수행하여 세션 쿠키를 저장합니다.
     *
     * @return
     * @throws Exception
     */
    public static List<UserSession> setup() throws Exception {
        System.out.println("Starting setup...");

        List<UserSession> loggedInUsers = new ArrayList<>();
        for (int i = 1; i <= MAX_USER; i++) {
            String email = "user" + i + "@example.com";
            String sessionCookie = login(email);
            if (sessionCookie != null) {
                loggedInUsers.add(new UserSession(email, sessionCookie));
            }
        }

        System.out.println("Logged in " + loggedInUsers.size() + " users");
        return loggedInUsers;
    }

    /**
     * 대기열을 3초 간격으로 조회합니다. 이미 통과한 사용자는 다시 조회하지 않습니다.
     *
     * @param loggedInUsers
     * @param iteration
     * @throws Exception
     */
    public static void executeTest(List<UserSession> loggedInUsers, int iteration) throws Exception {
        int userId = iteration % loggedInUsers.size();
        UserSession user = loggedInUsers.get(userId);

        if (pass.contains(user.getEmail())) {
            return;
        }

        int waitOrder = waitOrderMap.getOrDefault(user.getEmail(), 0);
        getWaitOrder(1, 1, user, waitOrder);
        Thread.sleep(3000);
    }

    /**
     * 사용자 로그인을 수행합니다.
     *
     * @param email
     * @return
     * @throws Exception
     */
    public static String login(String email) throws Exception {
        String loginUrl = LOGIN_URL + "/auth/login";
        String payload = "{\"email\":\"" + email + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(loginUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            HttpHeaders headers = response.headers();
            List<String> cookies = headers.allValues("Set-Cookie");
            if (cookies.isEmpty()) {
                System.err.println("No Cookie header found");
                return null;
            }
            return cookies.get(0); // Assuming single session cookie
        } else {
            System.err.println("Login failed: " + response.statusCode() + " " + response.body());
            return null;
        }
    }

    /**
     * 대기열 조회 API를 호출하여 대기열 순서를 조회합니다.
     *
     * @param festivalId
     * @param ticketId
     * @param user
     * @param waitOrder
     * @throws Exception
     */
    public static void getWaitOrder(int festivalId, int ticketId, UserSession user, int waitOrder) throws Exception {
        String url = BASE_URL + "/festivals/" + festivalId + "/tickets/" + ticketId + "/purchase/wait";
        if (waitOrder >= 0) {
            url += "?waitOrder=" + waitOrder;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Cookie", user.getSessionCookie())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            try {
                Map<String, Object> body = parseJson(response.body());
                if (body != null && body.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) body.get("data");
                    if (data.containsKey("purchasable")) {
                        boolean purchasable = (Boolean) data.get("purchasable");
                        if (purchasable) {
                            pass.add(user.getEmail());
                            System.out.println(user.getEmail() + " passed - " + waitOrder);
                        }
                        int newWaitOrder = (Integer) data.get("waitOrder");
                        waitOrderMap.put(user.getEmail(), newWaitOrder);
                        System.out.println(user.getEmail() + " " + newWaitOrder);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse get WaitOrder response: " + e.getMessage());
            }
        } else {
            System.err.println(
                    "Get Wait Order failed: " + user.getEmail() + " " + response.statusCode() + " " + response.body());
        }
    }

    public static Map<String, Object> parseJson(String json) throws Exception {
        return objectMapper.readValue(json, Map.class);
    }

    static class UserSession {
        private String email;
        private String sessionCookie;

        public UserSession(String email, String sessionCookie) {
            this.email = email;
            this.sessionCookie = sessionCookie;
        }

        public String getEmail() {
            return email;
        }

        public String getSessionCookie() {
            return sessionCookie;
        }
    }
}
