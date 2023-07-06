package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Article;
import com.mycompany.myapp.service.dto.ArticleDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Article entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {
    List<ArticleDTO> findAllByOrderByCreatedat();
}
