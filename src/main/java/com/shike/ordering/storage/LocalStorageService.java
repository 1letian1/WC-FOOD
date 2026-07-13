package com.shike.ordering.storage;

import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {
    private static final Map<String, String> MIME_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "gif", "image/gif");
    private final StorageProperties properties;

    @Override
    public StoredFile storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        if (file.getSize() > properties.maxImageSize().toBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String extension = extension(file.getOriginalFilename());
        String expectedMime = MIME_BY_EXTENSION.get(extension);
        if (expectedMime == null || !expectedMime.equals(normalizeMime(file.getContentType()))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        }
        try {
            byte[] bytes = file.getBytes();
            if (!expectedMime.equals(detectMime(bytes))) throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path base = Path.of(properties.uploadPath()).toAbsolutePath().normalize();
            Path directory = base.resolve(datePath).normalize();
            Path target = directory.resolve(fileName).normalize();
            if (!target.startsWith(base)) throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
            Files.createDirectories(directory);
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            String relativePath = datePath + "/" + fileName;
            String baseUrl = properties.publicBaseUrl().replaceAll("/+$", "");
            return new StoredFile(baseUrl + "/" + relativePath, relativePath);
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    private String extension(String originalFilename) {
        String filename = StringUtils.getFilename(originalFilename);
        if (!StringUtils.hasText(filename)) throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        int dot = filename.lastIndexOf('.');
        if (dot < 1 || dot == filename.length() - 1) throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeMime(String contentType) {
        if (contentType == null) return "";
        int separator = contentType.indexOf(';');
        return (separator < 0 ? contentType : contentType.substring(0, separator)).trim().toLowerCase(Locale.ROOT);
    }

    private String detectMime(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return "image/png";
        }
        if (bytes.length >= 6) {
            String signature = new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII);
            if ("GIF87a".equals(signature) || "GIF89a".equals(signature)) return "image/gif";
        }
        return "";
    }
}
