package com.shike.ordering.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties("shike.storage")
public record StorageProperties(String uploadPath, String publicBaseUrl, DataSize maxImageSize) {
    public StorageProperties {
        if (uploadPath == null || uploadPath.isBlank()) uploadPath = "./uploads";
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) publicBaseUrl = "http://localhost:8080/files";
        if (maxImageSize == null) maxImageSize = DataSize.ofMegabytes(5);
    }
}
