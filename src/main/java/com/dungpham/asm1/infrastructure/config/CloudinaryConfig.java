package com.dungpham.asm1.infrastructure.config;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private final Dotenv dotenv;
    private static final String CLOUDINARY_CLOUD_NAME = "CLOUDINARY_CLOUD_NAME";
    private static final String CLOUDINARY_API_KEY = "CLOUDINARY_API_KEY";
    private static final String CLOUDINARY_API_SECRET = "CLOUDINARY_API_SECRET";

    public CloudinaryConfig() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put(CLOUDINARY_CLOUD_NAME, dotenv.get("CLOUDINARY_CLOUD_NAME"));
        config.put(CLOUDINARY_API_KEY, dotenv.get("CLOUDINARY_API_KEY"));
        config.put(CLOUDINARY_API_SECRET, dotenv.get("CLOUDINARY_API_SECRET"));
        return new Cloudinary(config);
    }
}