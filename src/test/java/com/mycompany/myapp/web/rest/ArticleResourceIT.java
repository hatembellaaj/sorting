package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Article;
import com.mycompany.myapp.repository.ArticleRepository;
import com.mycompany.myapp.service.dto.ArticleDTO;
import com.mycompany.myapp.service.mapper.ArticleMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link ArticleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ArticleResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATEDAT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATEDAT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/articles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private MockMvc restArticleMockMvc;

    private Article article;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Article createEntity() {
        Article article = new Article().title(DEFAULT_TITLE).content(DEFAULT_CONTENT).createdat(DEFAULT_CREATEDAT);
        return article;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Article createUpdatedEntity() {
        Article article = new Article().title(UPDATED_TITLE).content(UPDATED_CONTENT).createdat(UPDATED_CREATEDAT);
        return article;
    }

    @BeforeEach
    public void initTest() {
        articleRepository.deleteAll();
        article = createEntity();
    }

    @Test
    void createArticle() throws Exception {
        int databaseSizeBeforeCreate = articleRepository.findAll().size();
        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);
        restArticleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(articleDTO)))
            .andExpect(status().isCreated());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeCreate + 1);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testArticle.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testArticle.getCreatedat()).isEqualTo(DEFAULT_CREATEDAT);
    }

    @Test
    void createArticleWithExistingId() throws Exception {
        // Create the Article with an existing ID
        article.setId("existing_id");
        ArticleDTO articleDTO = articleMapper.toDto(article);

        int databaseSizeBeforeCreate = articleRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restArticleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(articleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllArticles() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        // Get all the articleList
        restArticleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(article.getId())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].createdat").value(hasItem(sameInstant(DEFAULT_CREATEDAT))));
    }

    @Test
    void getArticle() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        // Get the article
        restArticleMockMvc
            .perform(get(ENTITY_API_URL_ID, article.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(article.getId()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.createdat").value(sameInstant(DEFAULT_CREATEDAT)));
    }

    @Test
    void getNonExistingArticle() throws Exception {
        // Get the article
        restArticleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingArticle() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        int databaseSizeBeforeUpdate = articleRepository.findAll().size();

        // Update the article
        Article updatedArticle = articleRepository.findById(article.getId()).get();
        updatedArticle.title(UPDATED_TITLE).content(UPDATED_CONTENT).createdat(UPDATED_CREATEDAT);
        ArticleDTO articleDTO = articleMapper.toDto(updatedArticle);

        restArticleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, articleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isOk());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testArticle.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testArticle.getCreatedat()).isEqualTo(UPDATED_CREATEDAT);
    }

    @Test
    void putNonExistingArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, articleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(articleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateArticleWithPatch() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        int databaseSizeBeforeUpdate = articleRepository.findAll().size();

        // Update the article using partial update
        Article partialUpdatedArticle = new Article();
        partialUpdatedArticle.setId(article.getId());

        partialUpdatedArticle.title(UPDATED_TITLE).content(UPDATED_CONTENT).createdat(UPDATED_CREATEDAT);

        restArticleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedArticle.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedArticle))
            )
            .andExpect(status().isOk());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testArticle.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testArticle.getCreatedat()).isEqualTo(UPDATED_CREATEDAT);
    }

    @Test
    void fullUpdateArticleWithPatch() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        int databaseSizeBeforeUpdate = articleRepository.findAll().size();

        // Update the article using partial update
        Article partialUpdatedArticle = new Article();
        partialUpdatedArticle.setId(article.getId());

        partialUpdatedArticle.title(UPDATED_TITLE).content(UPDATED_CONTENT).createdat(UPDATED_CREATEDAT);

        restArticleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedArticle.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedArticle))
            )
            .andExpect(status().isOk());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
        Article testArticle = articleList.get(articleList.size() - 1);
        assertThat(testArticle.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testArticle.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testArticle.getCreatedat()).isEqualTo(UPDATED_CREATEDAT);
    }

    @Test
    void patchNonExistingArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, articleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamArticle() throws Exception {
        int databaseSizeBeforeUpdate = articleRepository.findAll().size();
        article.setId(UUID.randomUUID().toString());

        // Create the Article
        ArticleDTO articleDTO = articleMapper.toDto(article);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restArticleMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(articleDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Article in the database
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteArticle() throws Exception {
        // Initialize the database
        articleRepository.save(article);

        int databaseSizeBeforeDelete = articleRepository.findAll().size();

        // Delete the article
        restArticleMockMvc
            .perform(delete(ENTITY_API_URL_ID, article.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Article> articleList = articleRepository.findAll();
        assertThat(articleList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
