package org.example.marketplace.controller;

import jakarta.validation.Valid;
import org.example.marketplace.dto.product.CreateProductRequest;
import org.example.marketplace.dto.product.ProductResponse;
import org.example.marketplace.dto.product.UpdateProductRequest;
import org.example.marketplace.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService productService) {
        this.service = productService;
    }

    /**
     * ВАЖНО! Оставлено чисто в рамках обучения и теста, на проде так не делаем, используем пагинацию
     */
    @GetMapping("/all")
    public List<ProductResponse> getAllProduct(){
        return service.getAllProduct();
    }

    /**
     * Пример НЕоптимальной пагинации — оставлен для сравнения с {@link #getProducts}.
     *
     * Под капотом обычный findAll(pageable): связи не фетчатся, category и tags
     * остаются ленивыми и догружаются уже после основного запроса.
     *
     * Само по себе это не катастрофа, потому что в application.yaml включён
     * default_batch_fetch_size — он батчит и коллекции, и ленивые @ManyToOne-прокси.
     * Поэтому запросов получается 4, а не 151: товары, count, категории пачкой,
     * теги пачкой.
     *
     * Чем всё же хуже правильного варианта:
     *  - на один запрос больше (категория берётся отдельно, а не джойном);
     *  - корректность держится на глобальной настройке. Уберут batch_fetch_size —
     *    и тихо вернётся N+1 на 150 запросов. В "хорошем" варианте fetch прописан
     *    в самом запросе и от конфига не зависит.
     *
     * Про Pageable: Spring видит этот тип в сигнатуре и подключает резолвер, который
     * читает из query-строки page, size и sort. Имена стандартные, @RequestParam
     * писать не нужно. Ручная альтернатива выглядела бы так:
     *
     * Альтернатива
     * @RequestParam(value = "size", defaultValue = "10") int size,
     * @RequestParam(value = "page", defaultValue = "0") int page
     *
     * Pageble pageble = Page.of(int, size)
     */
    @GetMapping("/bad_page")
    public PagedModel<ProductResponse> getProductsBad(@PageableDefault(size = 10, page = 0,
            sort = {"price", "name" }, direction = ASC) Pageable pageble){
        return service.getPageProductsBad(pageble);
    }

    /**
     * Пример хорошего запроса
     */
    @GetMapping
    public PagedModel<ProductResponse> getProducts(@PageableDefault(size = 10, page = 0,
            sort = {"price", "name" }, direction = ASC) Pageable pageble){

        Page<ProductResponse> page = service.getPageProductsGood(pageble);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id){
        return service.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse addProduct(@Valid @RequestBody CreateProductRequest product){
        return  service.addProduct(product);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@RequestBody @Valid UpdateProductRequest product,
                                         @PathVariable Long id){
        return  service.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id){
        service.delete(id);
    }


}
