package com.wootecam.festivals.domain.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(HealthController.class)
@ActiveProfiles("test")
public class HealthControllerTest extends RestDocsSupport {

    @Test
    @DisplayName("health api")
    void health_returnsOkStatus() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Override
    protected Object initController() {
        return new HealthController();
    }
}
