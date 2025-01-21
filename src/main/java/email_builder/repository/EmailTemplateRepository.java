package email_builder.repository;

import email_builder.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    List<EmailTemplate> findByOrderByCreatedAtDesc();
    List<EmailTemplate> findByTitleContainingIgnoreCase(String title);

}
