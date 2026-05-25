package com.ecommerce.pricing.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OrderPricingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculateEndpointReturnsChallengeExample() throws Exception {
        String body = """
                {
                  "customerType": "VIP",
                  "items": [
                    { "sku": "A100", "price": 100, "quantity": 2 },
                    { "sku": "B200", "price": 50, "quantity": 1 }
                  ],
                  "couponCode": "SUMMER10"
                }
                """;

        mockMvc.perform(post("/orders/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(250))
                .andExpect(jsonPath("$.discount").value(35))
                .andExpect(jsonPath("$.finalPrice").value(215))
                .andExpect(jsonPath("$.orderId").exists());
    }
}
