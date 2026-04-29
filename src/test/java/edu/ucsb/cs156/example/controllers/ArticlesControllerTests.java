package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticleRepository articleRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/articles/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Golden Retriever Overview")
                .param("url", "https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
                .param("explanation", "A brief overview of the Golden Retriever breed.")
                .param("email", "prishabobde@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "Golden Retriever Overview")
                .param("url", "https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
                .param("explanation", "A brief overview of the Golden Retriever breed.")
                .param("email", "prishabobde@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .dateAdded(ldt1)
            .explanation("A brief overview of the Golden Retriever breed.")
            .build();

    ArrayList<Article> expectedArticles = new ArrayList<>();
    expectedArticles.add(article1);

    when(articleRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articleRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .explanation("A brief overview of the Golden Retriever breed.")
            .dateAdded(ldt1)
            .build();

    when(articleRepository.save(eq(article1))).thenReturn(article1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "Golden Retriever Overview")
                    .param(
                        "url",
                        "https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
                    .param("email", "prishabobde@ucsb.edu")
                    .param("explanation", "A brief overview of the Golden Retriever breed.")
                    .param("dateAdded", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).save(eq(article1));
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/articles").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(articleRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(articleRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Article with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

    // arrange

    Article article1 =
        Article.builder()
            .id(7L)
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .explanation("A brief overview of the Golden Retriever breed.")
            .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
            .build();

    when(articleRepository.findById(eq(7L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/articles").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(articleRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-04T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-04T00:00:00");

    Article article1 =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .explanation("A brief overview of the Golden Retriever breed.")
            .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
            .build();

    Article editedArticle =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds2/the-golden-retriever/")
            .title("Golden Retriever Overview_EDITED")
            .email("prishabobde@gmail.com")
            .explanation("An edited overview of the Golden Retriever breed.")
            .dateAdded(ldt2)
            .build();

    String requestBody = mapper.writeValueAsString(editedArticle);

    when(articleRepository.findById(eq(67L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).findById(67L);
    verify(articleRepository, times(1)).save(editedArticle);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .explanation("A brief overview of the Golden Retriever breed.")
            .dateAdded(ldt1)
            .build();

    String requestBody = mapper.writeValueAsString(article1);

    when(articleRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .url("https://moderndogmagazine.com/articles/breeds/the-golden-retriever/")
            .title("Golden Retriever Overview")
            .email("prishabobde@ucsb.edu")
            .explanation("A brief overview of the Golden Retriever breed.")
            .dateAdded(ldt1)
            .build();

    when(articleRepository.findById(eq(15L))).thenReturn(Optional.of(article1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).findById(15L);
    verify(articleRepository, times(1)).delete(eq(article1));

    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(articleRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/articles").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Article with id 15 not found", json.get("message"));
  }
}
