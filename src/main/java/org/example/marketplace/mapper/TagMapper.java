package org.example.marketplace.mapper;

import org.example.marketplace.dto.tag.TagRequest;
import org.example.marketplace.dto.tag.TagResponse;
import org.example.marketplace.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag dtoToEntity(TagRequest tag){
        return new Tag(tag.name());

    }

    public TagResponse entityToDto(Tag tag){
        return new TagResponse(tag.getId(), tag.getName());
    }
}
