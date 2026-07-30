package org.example.marketplace.controller;

import jakarta.validation.Valid;
import org.example.marketplace.dto.tag.TagRequest;
import org.example.marketplace.dto.tag.TagResponse;
import org.example.marketplace.service.TagService;
import org.springframework.http.HttpStatus;
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
    public List<TagResponse> getAllTag(){
        return service.getAllTag();
    }

    @GetMapping("/{id}")
    public TagResponse getTag(@PathVariable Long id){
        return service.getTag(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse addTag(@Valid @RequestBody TagRequest tag){
        return  service.addTag(tag);
    }

    @PutMapping("/{id}")
    public TagResponse updateTag(@PathVariable Long id,@Valid @RequestBody TagRequest tag){
        return  service.updateTag(id, tag);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id){
        service.delete(id);
    }


}
