package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Article;
import com.mycompany.myapp.service.dto.ArticleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Article} and its DTO {@link ArticleDTO}.
 */
@Mapper(componentModel = "spring")
public interface ArticleMapper extends EntityMapper<ArticleDTO, Article> {}
