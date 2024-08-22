package com.wootecam.festivals.global.config;//package com.wootecam.festivals.global.config;
//
//import java.util.Collection;
//import java.util.Map;
//import org.springframework.session.MapSessionRepository;
//import org.springframework.session.Session;
//
//public class CustomMapSessionRepository extends MapSessionRepository {
//
//    private final Map<String, Session> sessionMap;
//
//    public CustomMapSessionRepository(Map<String, Session> sessions) {
//        super(sessions);
//        this.sessionMap = sessions;
//    }
//
//    public Collection<Session> getSessions() {
//        return sessionMap.values().stream()
//                .toList();
//    }
//}
