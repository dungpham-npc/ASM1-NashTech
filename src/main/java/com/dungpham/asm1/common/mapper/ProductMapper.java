package com.dungpham.asm1.common.mapper;

import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.ProductResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {Util.class, CategoryMapper.class})
public interface ProductMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "price", target = "price")
    @Mapping(target = "thumbnailImgKey", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "imageKeys", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(source = "category", target = "category", qualifiedByName = "toListResponse")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductResponse toProductResponse(Product product);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(target = "thumbnailImgKey", ignore = true)
    @Mapping(target = "imageKeys", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(source = "category", target = "category", qualifiedByName = "toListResponse")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    ProductResponse toProductDetailsResponse(Product product);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(target = "thumbnailImgKey", ignore = true)
    @Mapping(target = "imageKeys", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(source = "category", target = "category", qualifiedByName = "toListResponse")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "featured", target = "isFeatured")
    ProductResponse toProductManagementDetailsResponse(Product product);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "featured", target = "isFeatured")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "featured", target = "featured")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}