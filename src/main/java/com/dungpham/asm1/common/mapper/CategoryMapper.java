package com.dungpham.asm1.common.mapper;

import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.request.CategoryRequest;
import com.dungpham.asm1.response.CategoryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {Util.class})
public interface CategoryMapper {

    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "id", target = "id")
    CategoryResponse toDetailedResponse(Category category);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Named("toListResponse")
    CategoryResponse toListResponse(Category category);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Category toEntity(CategoryRequest request);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category entity);
}