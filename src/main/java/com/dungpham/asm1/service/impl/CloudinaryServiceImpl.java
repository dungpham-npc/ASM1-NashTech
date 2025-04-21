package com.dungpham.asm1.service.impl;

import com.cloudinary.utils.ObjectUtils;
import com.dungpham.asm1.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dungpham.asm1.infrastructure.config.CloudinaryConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final CloudinaryConfig cloudinaryConfig;

    @Override
    public String uploadImage(byte[] image) {
        var params =
                ObjectUtils.asMap(
                        "folder", "asm1",
                        "resource_type", "image");
        try {
            var uploadResult = cloudinaryConfig.cloudinary().uploader().upload(image, params);
            return uploadResult.get("public_id").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getImageUrl(String assetKey) {
        try {
            var response = cloudinaryConfig.cloudinary().api().resource(assetKey, ObjectUtils.emptyMap());
            log.info("Cloudinary response: {}", response);
            return response.get("secure_url").toString();
        } catch (Exception e) {
            log.error("Error getting image URL for assetKey {}: {}", assetKey, e.getMessage(), e);
            return "https://support.heberjahiz.com/hc/article_attachments/21013076295570";
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinaryConfig.cloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

