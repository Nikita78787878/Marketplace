package org.example.marketplace.controller;

import org.example.marketplace.dto.category.CategoryResponse;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.tag.TagResponse;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * УРОВЕНЬ 2: СРЕЗ ВЕБ-СЛОЯ (тест с моками).
 *
 * @WebMvcTest поднимает ТОЛЬКО веб-часть контекста: контроллеры, конвертеры
 * JSON, валидацию, @RestControllerAdvice.
 *
 * Сервисы, репозитории, база, Hibernate
 * НЕ поднимаются вообще — их в контексте просто нет.
 *
 * Поэтому ProductService подменяется моком через @MockitoBean (в Spring Boot 3.4+
 * пришёл на смену @MockBean). Мы сами говорим, что он вернёт, и проверяем,
 * как контроллер это превратит в HTTP-ответ.
 *
 * Что проверяем: маршрутизацию (какой URL к какому методу), сериализацию в JSON,
 * коды ответов, работу валидации и обработчика ошибок.
 * Что НЕ проверяем: работает ли сам сервис и корректен ли SQL — сервис здесь
 * ненастоящий.
 *
 * Плюс такого теста: стартует за секунду, потому что половина контекста не грузится.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void getProduct_возвращает200ИТелоСВложеннымиОбъектами() throws Exception {
        ProductResponse stub = new ProductResponse(
                1L, "iPhone 15", "Apple smartphone",
                new BigDecimal("99999.99"), 10,
                new CategoryResponse(1L, "Смартфоны", "Мобильные телефоны"),
                List.of(new TagResponse(1L, "Новинка"))
        );
        given(service.getProduct(1L)).willReturn(stub);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.category.name").value("Смартфоны"))
                .andExpect(jsonPath("$.tags[0].name").value("Новинка"));
    }

    @Test
    void getProduct_несуществующийId_возвращает404() throws Exception {
        // проверяем связку: сервис кинул исключение -> GlobalExceptionHandler превратил его в 404
        given(service.getProduct(999L)).willThrow(new NotFoundException("Product 999 not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addProduct_пустоеИмя_возвращает400СОписаниемПоля() throws Exception {
        String body = """
                {
                  "name": "",
                  "description": "какое-то описание",
                  "price": 100.00,
                  "stockQuantity": 5,
                  "categoryId": 1,
                  "tags": [1]
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                // ErrorResponseFieldValid отдаёт мапу "поле -> сообщение"
                .andExpect(jsonPath("$.error.name").exists());

        // сервис даже не должен быть вызван: запрос отсеян валидацией на входе
        org.mockito.Mockito.verify(service, org.mockito.Mockito.never()).addProduct(any());
    }
}
