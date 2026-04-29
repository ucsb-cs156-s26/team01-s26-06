package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequests/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/recommendationrequests/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "a@ucsb.edu")
                .param("professorEmail", "b@ucsb.edu")
                .param("explanation", "PhD")
                .param("dateRequested", "2026-01-03T00:00:00")
                .param("dateNeeded", "2026-06-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "a@ucsb.edu")
                .param("professorEmail", "b@ucsb.edu")
                .param("explanation", "PhD")
                .param("dateRequested", "2026-01-03T00:00:00")
                .param("dateNeeded", "2026-06-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

    // arrange

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("PhD")
            .dateRequested(LocalDateTime.parse("2026-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-06-01T00:00:00"))
            .done(false)
            .build();

    ArrayList<RecommendationRequest> expected = new ArrayList<>();
    expected.add(recommendationRequest1);

    when(recommendationRequestRepository.findAll()).thenReturn(expected);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
    // arrange

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("PhD")
            .dateRequested(LocalDateTime.parse("2026-01-03T00:00:00"))
            .dateNeeded(LocalDateTime.parse("2026-06-01T00:00:00"))
            .done(true)
            .build();

    when(recommendationRequestRepository.save(any())).thenReturn(recommendationRequest1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post")
                    .param("requesterEmail", "a@ucsb.edu")
                    .param("professorEmail", "b@ucsb.edu")
                    .param("explanation", "PhD")
                    .param("dateRequested", "2026-01-03T00:00:00")
                    .param("dateNeeded", "2026-06-01T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    ArgumentCaptor<RecommendationRequest> rrCaptor =
        ArgumentCaptor.forClass(RecommendationRequest.class);

    verify(recommendationRequestRepository, times(1)).save(rrCaptor.capture());

    RecommendationRequest saved = rrCaptor.getValue();

    assertEquals("a@ucsb.edu", saved.getRequesterEmail());
    assertEquals("b@ucsb.edu", saved.getProfessorEmail());
    assertEquals("PhD", saved.getExplanation());
    assertEquals(LocalDateTime.parse("2026-01-03T00:00:00"), saved.getDateRequested());
    assertEquals(LocalDateTime.parse("2026-06-01T00:00:00"), saved.getDateNeeded());
    assertEquals(true, saved.getDone());

    String expectedJson = mapper.writeValueAsString(recommendationRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime requested = LocalDateTime.parse("2022-01-01T00:00:00");
    LocalDateTime needed = LocalDateTime.parse("2022-02-01T00:00:00");

    RecommendationRequest req =
        RecommendationRequest.builder()
            .id(7L)
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("need rec")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(req));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    String expectedJson = mapper.writeValueAsString(req);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(eq(7L));

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {

    // arrange
    LocalDateTime requested1 = LocalDateTime.parse("2022-01-01T00:00:00");
    LocalDateTime needed1 = LocalDateTime.parse("2022-02-01T00:00:00");

    LocalDateTime requested2 = LocalDateTime.parse("2023-01-01T00:00:00");
    LocalDateTime needed2 = LocalDateTime.parse("2023-02-01T00:00:00");

    RecommendationRequest original =
        RecommendationRequest.builder()
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("old")
            .dateRequested(requested1)
            .dateNeeded(needed1)
            .done(false)
            .build();

    RecommendationRequest edited =
        RecommendationRequest.builder()
            .requesterEmail("c@ucsb.edu")
            .professorEmail("d@ucsb.edu")
            .explanation("new")
            .dateRequested(requested2)
            .dateNeeded(needed2)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(original));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1)).save(original);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {

    // arrange
    LocalDateTime requested = LocalDateTime.parse("2022-01-01T00:00:00");
    LocalDateTime needed = LocalDateTime.parse("2022-02-01T00:00:00");

    RecommendationRequest edited =
        RecommendationRequest.builder()
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("test")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(false)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendationrequest() throws Exception {

    // arrange
    LocalDateTime requested = LocalDateTime.parse("2022-01-01T00:00:00");
    LocalDateTime needed = LocalDateTime.parse("2022-02-01T00:00:00");

    RecommendationRequest req =
        RecommendationRequest.builder()
            .requesterEmail("a@ucsb.edu")
            .professorEmail("b@ucsb.edu")
            .explanation("test")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(req));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(req);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void
      admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
          throws Exception {

    // arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequests").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }
}
