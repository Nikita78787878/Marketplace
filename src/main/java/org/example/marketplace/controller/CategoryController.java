package org.example.marketplace.controller;

import jakarta.validation.Valid;
import org.example.marketplace.dto.category.CategoryRequest;
import org.example.marketplace.dto.category.CategoryResponse;
import org.example.marketplace.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping()
    public List<CategoryResponse> getAllCategory(){
        return service.getAllCategory();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id){
        return service.getCategory(id);
    }

    /**
     * Статус ответа: @ResponseStatus, если он у метода всегда один (POST → 201, DELETE → 204).
     * ResponseEntity — только когда статус вычисляется в рантайме или нужен заголовок
     * (например Location в POST). По HTTP результат одинаковый, разница в читаемости:
     * @ResponseStatus виден в сигнатуре и попадает в Swagger, ResponseEntity — нет.
     *
     * void + 204 = тела в ответе нет вообще. Это и означает No Content.
     *
     * Коды ошибок здесь НЕ ставим: контроллер кидает NotFoundException,
     * статус проставляет GlobalExceptionHandler.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
//    public ResponseEntity<CategoryResponse> addCategory(@Valid @RequestBody CategoryRequest category){
    public CategoryResponse addCategory(@Valid @RequestBody CategoryRequest category){
//        return  ResponseEntity.status(201).body(service.addCategory(category));
        return  service.addCategory(category);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest category){
        return  service.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id){
        service.delete(id);

    }


}
