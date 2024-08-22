package com.wootecam.festivals.domain.festival.stub;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import java.time.LocalDateTime;

public class FestivalStub extends Festival {

    private final Long id;
    private final Member admin;
    private final String title;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private FestivalStub(Long id, Member admin, String title, String description,
                         LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.id = id;
        this.admin = admin;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static FestivalStub createValidFestival(Long id) {
        return new FestivalStub(id, Member.builder().name("test").email("test@test.com").profileImg("").build(),
                "Test Festival", "Test Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(7));
    }

    public static FestivalStub createFestivalWithNullId() {
        return new FestivalStub(null, Member.builder().name("test").email("test@test.com").profileImg("").build(),
                "Test Festival", "Test Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(7));
    }

    public static FestivalStub createFestivalWithTime(LocalDateTime festivalStartTime, LocalDateTime festivalEndTime) {
        return new FestivalStub(null, Member.builder().name("test").email("test@test.com").profileImg("").build(),
                "Test Festival", "Test Description",
                festivalStartTime, festivalEndTime);
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public Member getAdmin() {
        return admin;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
