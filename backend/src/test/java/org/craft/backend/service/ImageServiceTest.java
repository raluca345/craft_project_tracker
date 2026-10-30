package org.craft.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;

import org.craft.backend.exceptions.ImageFileException;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService.UploadedImage;
import org.craft.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
    @Mock
    private R2Service r2Service;

    @InjectMocks
    private ImageService imageService;

    static Stream<Arguments> validImageFiles() {
        return Stream.of(
                Arguments.of("image.png", "image/png",
                        new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x00 }),
                Arguments.of("image.jpg", "image/jpeg",
                        new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                0x00, 0x00 }),
                Arguments.of("image.jpeg", "image/jpeg",
                        new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                0x00, 0x00 }),
                Arguments.of("image.webp", "image/webp",
                        new byte[] { 'R', 'I', 'F', 'F', 0x00, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P' }));
    }

    @ParameterizedTest(name = "{0} ({1})")
    @MethodSource("validImageFiles")
    void upload_validImage_returnsUploadedImage(String filename, String contentType, byte[] headerBytes)
            throws IOException {
        User user = TestUsers.user();
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, headerBytes);

        when(r2Service.putObject(anyString(), any(byte[].class), eq(contentType))).thenReturn("key");

        UploadedImage result = imageService.upload(user, file);

        assertNotNull(result);
        assertTrue(result.key().startsWith(user.getId().toString()));
        assertTrue(result.key().endsWith(filename.substring(filename.lastIndexOf('.'))));
        assertEquals(contentType, result.contentType());
        verify(r2Service).putObject(anyString(), any(byte[].class), eq(contentType));
    }

    @Test
    void upload_unsupportedExtension_throwsAndNeverCallsR2() throws IOException {
        User user = TestUsers.user();
        byte[] bytes = new byte[12];
        MockMultipartFile file = new MockMultipartFile("file", "image.gif", "image/gif", bytes);

        assertThatThrownBy(() -> imageService.upload(user, file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("Unsupported file type");

        verify(r2Service, never()).putObject(anyString(), any(byte[].class), anyString());
    }

    @Test
    void upload_unsupportedMimeType_throwsAndNeverCallsR2() throws IOException {
        User user = TestUsers.user();
        byte[] bytes = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x00 };
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/gif", bytes);

        assertThatThrownBy(() -> imageService.upload(user, file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("Unsupported file type");

        verify(r2Service, never()).putObject(anyString(), any(byte[].class), anyString());
    }

    @Test
    void upload_emptyFile_throwsAndNeverCallsR2() throws IOException {
        User user = TestUsers.user();
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> imageService.upload(user, file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("empty");

        verify(r2Service, never()).putObject(anyString(), any(byte[].class), anyString());
    }

    @Test
    void upload_oversizedFile_throwsAndNeverCallsR2() throws IOException {
        User user = TestUsers.user();
        byte[] bytes = new byte[(int) (10 * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", bytes);

        assertThatThrownBy(() -> imageService.upload(user, file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("too large");

        verify(r2Service, never()).putObject(anyString(), any(byte[].class), anyString());
    }

    @Test
    void upload_renamedNonImage_throwsAndNeverCallsR2() throws IOException {
        User user = TestUsers.user();
        // PNG extension but JPEG magic bytes
        byte[] bytes = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00 };
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", bytes);

        assertThatThrownBy(() -> imageService.upload(user, file))
                .isInstanceOf(ImageFileException.class)
                .hasMessageContaining("doesn't look like a valid image");

        verify(r2Service, never()).putObject(anyString(), any(byte[].class), anyString());
    }

    @Test
    void presignUrl_nullKey_returnsNull() {
        String result = imageService.presignUrl(null);
        assertNull(result);
    }

    @Test
    void presignUrl_nonNullKey_delegatesToR2() {
        String key = "user-123/image.png";
        when(r2Service.presignGetUrl(key, ImageService.PRESIGN_TTL)).thenReturn("https://presigned.url");

        String result = imageService.presignUrl(key);

        assertEquals("https://presigned.url", result);
        verify(r2Service).presignGetUrl(key, ImageService.PRESIGN_TTL);
    }
}
