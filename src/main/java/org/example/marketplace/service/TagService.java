package org.example.marketplace.service;

import org.example.marketplace.dto.tag.TagRequest;
import org.example.marketplace.dto.tag.TagResponse;
import org.example.marketplace.entity.Tag;
import org.example.marketplace.exception.NotFoundException;
import org.example.marketplace.mapper.TagMapper;
import org.example.marketplace.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {
    private final TagRepository repository;
    private final TagMapper mapper;

    public TagService(TagRepository repository, TagMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTag() {
        return repository.findAll().stream().map(mapper::entityToDto).toList();
    }

    @Transactional(readOnly = true)
    public TagResponse getTag(Long id) {
        Tag tag =  repository.findById(id).orElseThrow(() -> new NotFoundException("Tag" + id + "not found"));
        return mapper.entityToDto(tag);
    }

    @Transactional
    public TagResponse addTag(TagRequest request) {
        Tag tag = repository.save(mapper.dtoToEntity(request));
        return mapper.entityToDto(tag);
    }

    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag =  repository.findById(id).orElseThrow(() -> new NotFoundException("Tag" + id + "not found"));
        tag.setName(request.name());

        return mapper.entityToDto(tag); // тут подумать надо
    }

    @Transactional
    public void delete(Long id) {
        if(!repository.existsById(id)) throw new NotFoundException("Tag " + id + " not found");
        repository.deleteById(id);
    }
}
