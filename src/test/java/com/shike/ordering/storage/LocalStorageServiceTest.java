package com.shike.ordering.storage;

import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.config.StorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {
    @TempDir Path tempDir;

    @Test
    void storeImage_whenPngIsValid_shouldUseServerGeneratedSafeName() {
        LocalStorageService service = service(DataSize.ofMegabytes(5));
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        MockMultipartFile file = new MockMultipartFile("file", "../../user-name.png", "image/png", png);

        StorageService.StoredFile stored = service.storeImage(file);

        assertThat(stored.path()).doesNotContain("..", "user-name");
        assertThat(stored.url()).startsWith("https://static.example/files/");
        assertThat(Files.exists(tempDir.resolve(stored.path()))).isTrue();
    }

    @Test
    void storeImage_whenDeclaredTypeDoesNotMatchSignature_shouldReject() {
        LocalStorageService service = service(DataSize.ofMegabytes(5));
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not-png".getBytes());

        assertThatThrownBy(() -> service.storeImage(file)).isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(80001);
    }

    @Test
    void storeImage_whenFileExceedsLimit_shouldReject() {
        LocalStorageService service = service(DataSize.ofBytes(4));
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", new byte[8]);

        assertThatThrownBy(() -> service.storeImage(file)).isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(80002);
    }

    private LocalStorageService service(DataSize maxSize) {
        return new LocalStorageService(new StorageProperties(
                tempDir.toString(), "https://static.example/files", maxSize));
    }
}
