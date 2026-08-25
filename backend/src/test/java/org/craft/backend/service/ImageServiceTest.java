package org.craft.backend.service;

import org.craft.backend.exceptions.ImageFileException;
import org.craft.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import software.amazon.awssdk.core.exception.SdkException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private R2Service r2Service;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(r2Service);
    }

    private static final byte[] PNG_BYTES = new byte[] {
            (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D
    };

    private static User user() {
        return User.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void acceptsAValidPngFromThePngExtension() throws Exception {
        // A proper PNG signature so ImageIO can sniff it.
        MockMultipartFile file = new MockMultipartFile(
                "file", "hat.png", "image/png", PNG_BYTES);

        imageService.upload(user(), file);

        ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
        verify(r2Service).putObject(contains(".png"), bytes.capture(), eq("image/png"));
        assertThat(bytes.getValue()).isEqualTo(PNG_BYTES);
    }

    @Test
    void acceptsAValidJpeg() throws Exception {
        byte[] jpeg = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46};
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg);

        imageService.upload(user(), file);

        verify(r2Service).putObject(contains(".jpg"), any(), eq("image/jpeg"));
    }

    @Test
    void acceptsAValidWebp() throws Exception {
        byte[] webp = new byte[] {
                'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '
        };
        MockMultipartFile file = new MockMultipartFile("file", "logo.webp", "image/webp", webp);

        imageService.upload(user(), file);

        verify(r2Service).putObject(contains(".webp"), any(), eq("image/webp"));
    }

    @Test
    void rejectsAnUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("Unsupported file type");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void rejectsAMismatchedMimeType() {
        // Filename says png but the browser-declared MIME is text/plain.
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "text/plain", PNG_BYTES);

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("Unsupported file type");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void rejectsAFileThatIsNotActuallyAnImage() {
        // Extension and MIME both claim png, but the bytes are junk.
        MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not an image at all".getBytes());

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("doesn't look like a valid image");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void rejectsAFileWhoseSignatureDoesNotMatchItsExtension() {
        // PNG bytes renamed to .jpg — extension and MIME string are both innocent.
        MockMultipartFile file = new MockMultipartFile("file", "renamed.jpg", "image/jpeg", PNG_BYTES);

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("doesn't look like a valid image");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void rejectsAnOversizedFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.png", "image/png", new byte[10 * 1024 * 1024 + 1]);

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("File is too large");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void rejectsAnEmptyImage() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("empty");
        verify(r2Service, never()).putObject(any(), any(), any());
    }

    @Test
    void propagatesExceptionWhenPutObjectFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hat.png", "image/png", PNG_BYTES);
        SdkException storageError = SdkException.builder().message("Connection refused").build();

        when(r2Service.putObject(any(), any(), any())).thenThrow(storageError);

        assertThatThrownBy(() -> imageService.upload(user(), file))
                .isSameAs(storageError);
    }
}