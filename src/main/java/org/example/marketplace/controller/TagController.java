package org.example.marketplace.controller;

import org.example.marketplace.entity.Product;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.service.ProductService;
import org.example.marketplace.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService service;

    public TagController(TagService service) {
        this.service = service;
    }

    @GetMapping()
    public List<Tag> getAllTag(){
        return service.getAllProduct();
    }

    @GetMapping("/{id}")
    public Tag getTag(@PathVariable Long id){
        return service.getProduct(id);
    }

    @PostMapping
    public Tag addTag(Tag tag){
        return  service.addProduct(tag);
    }

    @PutMapping("/{id}")
    public Tag updateTag(@PathVariable Long id){
        return  service.updateProduct(id);
    }

    @DeleteMapping("/{id}")
    public String deleteTag(@PathVariable Long id){
        service.delete(id);
        return "ok";
    }


}
