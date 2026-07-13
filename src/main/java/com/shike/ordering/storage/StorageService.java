package com.shike.ordering.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile storeImage(MultipartFile file);

    record StoredFile(String url, String path) { }
}
