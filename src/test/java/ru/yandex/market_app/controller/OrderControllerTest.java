package ru.yandex.market_app.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainer;
import ru.yandex.market_app.util.DataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

@Tag("integration")
@Testcontainers
@ImportTestcontainers(DatabaseContainer.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class OrderControllerTest extends DataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllTest() throws Exception {
        mockMvc.perform(get("/orders"))
            .andExpect(status().isOk())
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    public void getOrderById() throws Exception {
        var mockRequest = get("/orders/{id}", mockOrder.getId())
            .queryParam("newOrder", "false");
        
        var mockBadRequest = get("/orders/{id}", 0L);

        var mockNotFoundRequest = get("/orders/{id}", 9999L);

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(mockBadRequest)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(mockNotFoundRequest)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void buyTest() throws Exception {
        mockItem.setCartCount(1);
        itemRepo.save(mockItem);

        var mockRequest = post("/buy");

        mockMvc.perform(mockRequest)
            .andExpect(status().is3xxRedirection());
    }
}
