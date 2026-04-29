package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
}
