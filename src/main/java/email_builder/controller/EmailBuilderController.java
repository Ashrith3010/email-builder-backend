package email_builder.controller;

// src/main/java/com/emailbuilder/controller/EmailBuilderController.java
import email_builder.entity.EmailTemplate;
import email_builder.model.ImageUploadResponse;
import email_builder.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EmailBuilderController {

    @Autowired
    private EmailTemplateRepository templateRepository;

    private final String UPLOAD_DIR = "uploads/";

    @GetMapping("/templates")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findByOrderByCreatedAtDesc());
    }

    @GetMapping("/templates/search")
    public ResponseEntity<List<EmailTemplate>> searchTemplates(@RequestParam String query) {
        return ResponseEntity.ok(templateRepository.findByTitleContainingIgnoreCase(query));
    }

    @GetMapping("/templates/{id}/image")
    public ResponseEntity<byte[]> getTemplateImage(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> ResponseEntity.ok().body(template.getImageData())) // Send the image data
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/templates")
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate template) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        template.setId(id);
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        templateRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes(); // Read the image as a byte array

        // Create the email template with the image data
        EmailTemplate template = new EmailTemplate();
        template.setImageData(imageBytes);
        templateRepository.save(template); // Save it to the database

        return ResponseEntity.ok().body(new ImageUploadResponse("Image uploaded successfully"));
    }

}