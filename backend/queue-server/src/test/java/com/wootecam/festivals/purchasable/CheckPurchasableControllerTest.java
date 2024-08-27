package com.wootecam.festivals.purchasable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.utils.TimeProvider;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(CheckPurchasableController.class)
@ActiveProfiles("test")
class CheckPurchasableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckPurchasableService checkPurchasableService;

    @MockBean
    private TimeProvider timeProvider;

    @MockBean
    private Authentication authentication;

    private MockHttpSession mockHttpSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHttpSession = new MockHttpSession();
    }

    @Test
    @DisplayName("티켓 결제가 가능하다면 티켓 결제 권한을 부여한다")
    void testCheckPurchasable_WhenPurchasable_ShouldSetSessionAttributes() throws Exception {
        // Given
        Long festivalId = 1L;
        Long ticketId = 1L;
        Long memberId = 1L;
        LocalDateTime currentTime = LocalDateTime.now();
        PurchasableResponse purchasableResponse = new PurchasableResponse(true);

        when(authentication.memberId()).thenReturn(memberId);
        when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        when(checkPurchasableService.checkPurchasable(any(), any(), any())).thenReturn(purchasableResponse);

        // When & Then
        MvcResult result = mockMvc.perform(
                        get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check", festivalId, ticketId)
                                .session(mockHttpSession)
                                .principal(() -> authentication.toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchasable").value(true))
                .andReturn();

        HttpSession session = result.getRequest().getSession();
        assertEquals(ticketId, session.getAttribute(CheckPurchasableController.PURCHASABLE_TICKET_KEY));
        assertNotNull(session.getAttribute(CheckPurchasableController.PURCHASABLE_TICKET_TIMESTAMP_KEY));
    }

    @Test
    @DisplayName("티켓 결제가 불가능하다면 티켓 결제 권한을 부여하지 않는다")
    void testCheckPurchasable_WhenNotPurchasable_ShouldNotSetSessionAttributes() throws Exception {
        // Given
        Long festivalId = 1L;
        Long ticketId = 1L;
        Long memberId = 1L;
        LocalDateTime currentTime = LocalDateTime.now();
        PurchasableResponse purchasableResponse = new PurchasableResponse(false);

        when(authentication.memberId()).thenReturn(memberId);
        when(timeProvider.getCurrentTime()).thenReturn(currentTime);
        when(checkPurchasableService.checkPurchasable(any(), any(), any())).thenReturn(purchasableResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check", festivalId, ticketId)
                        .session(mockHttpSession)
                        .principal(() -> authentication.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchasable").value(false));

        assertNull(mockHttpSession.getAttribute(CheckPurchasableController.PURCHASABLE_TICKET_KEY));
        assertNull(mockHttpSession.getAttribute(CheckPurchasableController.PURCHASABLE_TICKET_TIMESTAMP_KEY));
    }
}
