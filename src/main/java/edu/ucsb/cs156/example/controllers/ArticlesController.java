package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles */
@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;

  /**
   * List all Articles
   *
   * @return an iterable of Commits
   */
  @Operation(summary = "List all commits")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    Iterable<Articles> articles = articlesRepository.findAll();
    return articles;
  }

  /**
   * Create a article
   *
   * @param title the articles title
   * @param url the articles url
   * @param email the email of the person that submitted the article
   * @param dateAdded the timestamp of when the article was added (in iso format, e.g.
   *     YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)
   * @return the saved article
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postArticle(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(
              name = "dateAdded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateAdded)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateAdded={}", dateAdded);

    Articles article = new Articles();
    article.setTitle(title);
    article.setUrl(url);
    article.setEmail(email);
    article.setDateAdded(dateAdded);

    Articles savedArticle = articlesRepository.save(article);

    return savedArticle;
  }

  /**
   * Get a single date by id
   *
   * @param id the id of the article
   * @return a Article
   */
  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(
                //  () -> new EntityNotFoundException("Articles with id " + id + " not found"));
                () -> new EntityNotFoundException(Articles.class, id));

    return article;
  }
}
