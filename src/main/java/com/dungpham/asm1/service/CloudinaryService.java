package com.dungpham.asm1.service;

public interface CloudinaryService {
    String uploadImage(byte[] image);

    String getImageUrl(String assetKey);

    void deleteImage(String publicId);
}
