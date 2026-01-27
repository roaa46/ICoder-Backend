package com.icoder.core.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Cloudinary cloudinary;

    public void checkPictureType(MultipartFile file) {
        String pictureType = file.getContentType();
        if (pictureType == null ||
                !(pictureType.equals("image/png") ||
                        pictureType.equals("image/jpeg") ||
                        pictureType.equals("image/jpg") ||
                        pictureType.equals("image/gif"))) {
            throw new IllegalStateException(
                    "Invalid file type. Only PNG, JPEG, JPG, and GIF are allowed."
            );
        }
    }

    public void deleteImageFromCloudinary(String imageUrl, String folderPath) throws IOException {
        String publicId = extractPublicId(imageUrl, folderPath);

        Map result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("invalidate", true)
        );

        if (!"ok".equals(result.get("result"))) {
            throw new IOException("Cloudinary deletion failed: " + result);
        }
    }
    private String extractPublicId(String imageUrl, String folderPath) {
        // https://res.cloudinary.com/demo/image/upload/v123/users/profile-pictures/abc.png
        String[] parts = imageUrl.split("/");
        String filename = parts[parts.length - 1];
        return folderPath + "/" + filename.substring(0, filename.lastIndexOf('.'));
    }
}
