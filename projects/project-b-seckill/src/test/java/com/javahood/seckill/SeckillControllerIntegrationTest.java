package com.javahood.seckill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javahood.seckill.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeckillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullHttpSeckillLifecycle_claimDedupSoldOutPayAndClose() throws Exception {
        // 1. Create campaign with stock = 1
        MvcResult createRes = mockMvc.perform(post("/api/seckill/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Flash-1\",\"stock\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        long cid = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. User 101 claims -> 202 Accepted
        MvcResult claimRes = mockMvc.perform(post("/api/seckill/campaigns/" + cid + "/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":101}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.outcome").value("ACCEPTED"))
                .andExpect(jsonPath("$.orderNo").isNotEmpty())
                .andReturn();
        String orderNo = objectMapper.readTree(claimRes.getResponse().getContentAsString()).get("orderNo").asText();

        // 3. User 101 retries -> 409 ALREADY_CLAIMED
        mockMvc.perform(post("/api/seckill/campaigns/" + cid + "/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":101}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.outcome").value("ALREADY_CLAIMED"));

        // 4. User 102 claims -> 409 SOLD_OUT
        mockMvc.perform(post("/api/seckill/campaigns/" + cid + "/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":102}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.outcome").value("SOLD_OUT"));

        // Wait briefly for async order persistence
        Thread.sleep(300);

        // 5. Pay order -> paid=true; subsequent close -> closed=false
        mockMvc.perform(post("/api/seckill/orders/" + orderNo + "/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paid").value(true));

        mockMvc.perform(post("/api/seckill/orders/" + orderNo + "/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closed").value(false));

        mockMvc.perform(get("/api/seckill/orders/" + orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.PAID.name()));
    }
}
