package email_builder.model;


public class ImageUploadResponse {
    private String message;
    private String imageUrl;
    private Long imageId;

    public ImageUploadResponse(String message, String imageUrl, Long imageId) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.imageId = imageId;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
}