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
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.util.DataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Tag("integration")
@Testcontainers
@ImportTestcontainers(DatabaseContainer.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ItemControllerTest extends DataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getItemsTest() throws Exception {
        var mockRequest = get("/items")
            .queryParam("sort", ESortType.PRICE.name())
            .queryParam("pageNumber", "1")
            .queryParam("pageSize", "5");

        var mockBadRequest = get("/items")
            .queryParam("sort", ESortType.PRICE.name())
            .queryParam("pageNumber", "0")
            .queryParam("pageSize", "0");

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(mockBadRequest)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getItemByIdTest() throws Exception {
        var mockRequest = get("/items/{id}", mockItem.getId());
        var mockBadRequest = get("/items/{id}", 0L);
        var mockNotFoundRequest = get("/items/{id}", 9999L);

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
    public void  updateCartTest() throws Exception {
        var mockRequest = post("/items/{id}", mockItem.getId())
            .queryParam("action", EActionType.PLUS.name());

        var mockBadRequest = post("/items/{id}", mockItem.getId())
            .queryParam("action", UUID.randomUUID().toString());

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().encoding(StandardCharsets.UTF_8))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(mockBadRequest)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void updateCartWithRedirectTest() throws Exception {
        var mockRequest = post("/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name());

        var mockBadRequest = post("/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name())
            .queryParam("pageNumber", "0L")
            .queryParam("pageSize", "0L");

        mockMvc.perform(mockRequest)
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(mockBadRequest)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
