package email_builder.controller;

import email_builder.entity.EmailTemplate;
import email_builder.model.ImageUploadResponse;
import email_builder.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EmailBuilderController {

    @Autowired
    private EmailTemplateRepository templateRepository;

    @PostMapping("/upload-image")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponse("No file provided", null, null));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponse("Invalid file type. Only images are allowed", null, null));
            }

            // Create a new template specifically for image storage
            EmailTemplate template = new EmailTemplate();
            template.setImageData(file.getBytes());
            template.setIsImageOnly(true);
            template.setTitle("Image Upload " + LocalDateTime.now());
            // Set a placeholder content since it's required
            template.setContent("Image placeholder content");
            template = templateRepository.save(template);

            String imageUrl = "/api/templates/" + template.getId() + "/image";

            return ResponseEntity.ok(new ImageUploadResponse(
                    "Image uploaded successfully",
                    imageUrl,
                    template.getId()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageUploadResponse("Failed to process image: " + e.getMessage(), null, null));
        }
    }

    @GetMapping(value = "/templates/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getTemplateImage(@PathVariable Long id) {
        return (ResponseEntity<byte[]>) templateRepository.findById(id)
                .map(template -> {
                    if (template.getImageData() == null) {
                        return ResponseEntity.notFound().build();
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(template.getImageData());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<EmailTemplate> getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/templates")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findByOrderByCreatedAtDesc());
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        templateRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(@RequestBody EmailTemplate template) {
        Map<String, String> errors = validateTemplate(template);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            template.setCreatedAt(LocalDateTime.now());
            // Ensure non-null values for optional fields
            template.setFooter(template.getFooter() != null ? template.getFooter() : "");
            template.setImageUrl(template.getImageUrl() != null ? template.getImageUrl() : "");
            template.setIsImageOnly(template.getIsImageOnly() != null ? template.getIsImageOnly() : false);

            EmailTemplate savedTemplate = templateRepository.save(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save template: " + e.getMessage()));
        }
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate template) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> errors = validateTemplate(template);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            template.setId(id); // Ensure ID is set for update
            template.setCreatedAt(templateRepository.findById(id).get().getCreatedAt()); // Preserve original creation date
            // Ensure non-null values for optional fields
            template.setFooter(template.getFooter() != null ? template.getFooter() : "");
            template.setImageUrl(template.getImageUrl() != null ? template.getImageUrl() : "");
            template.setIsImageOnly(template.getIsImageOnly() != null ? template.getIsImageOnly() : false);

            EmailTemplate savedTemplate = templateRepository.save(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update template: " + e.getMessage()));
        }
    }

    private Map<String, String> validateTemplate(EmailTemplate template) {
        Map<String, String> errors = new HashMap<>();

        // Validate title
        if (template.getTitle() == null || template.getTitle().trim().isEmpty()) {
            errors.put("title", "Title is required");
        } else if (template.getTitle().length() > 255) {
            errors.put("title", "Title cannot be longer than 255 characters");
        }

        // Validate content
        if (template.getContent() == null || template.getContent().trim().isEmpty()) {
            errors.put("content", "Content is required");
        }

        // Optional fields don't need validation since we'll set defaults

        return errors;
    }
}