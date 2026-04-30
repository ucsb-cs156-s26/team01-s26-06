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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;
  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdiningcommonsmenuitem/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    // check with empty database
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all_with_real_data() throws Exception {
    // check with sample values
    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();
    ArrayList<UCSBDiningCommonsMenuItem> menuItems =
        new ArrayList<>(Collections.singletonList(menuItem));
    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(menuItems);

    // ensure that the return is correct
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
            .andExpect(status().isOk())
            .andReturn();
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(menuItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Authorization tests for GET /api/ucsbdiningcommonsmenuitem?id=XXX (getById)

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=123")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id_with_empty_database() throws Exception {
    // test with no data, expect 404
    MvcResult result =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem?id=123"))
            .andExpect(status().is(404))
            .andReturn();
    String responseBody = result.getResponse().getContentAsString();
    Map<String, Object> json = mapper.readValue(responseBody, Map.class);
    assertEquals("id 123 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_by_id_with_real_data() throws Exception {
    // test with sample values
    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();
    when(ucsbDiningCommonsMenuItemRepository.findById(0L)).thenReturn(Optional.of(menuItem));

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem?id=0"))
            .andExpect(status().isOk())
            .andReturn();
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(0L);
    String expectedJson = mapper.writeValueAsString(menuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for PUT /api/ucsbdiningcommonsmenuitem?id=XXX

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_edit_an_existing_menu_item() throws Exception {
    UCSBDiningCommonsMenuItem menuItemOrig =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();

    UCSBDiningCommonsMenuItem menuItemEdited =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("dlg")
            .name("pizza")
            .station("pizza station")
            .build();

    String requestBody = mapper.writeValueAsString(menuItemEdited);

    when(ucsbDiningCommonsMenuItemRepository.findById(67L)).thenReturn(Optional.of(menuItemOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem")
                    .param("id", "67")
                    .header("Content-Type", "application/json")
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItemEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_cannot_edit_menu_item_that_does_not_exist() throws Exception {
    UCSBDiningCommonsMenuItem menuItemEdited =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();

    String requestBody = mapper.writeValueAsString(menuItemEdited);

    when(ucsbDiningCommonsMenuItemRepository.findById(67L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsbdiningcommonsmenuitem")
                    .param("id", "67")
                    .header("Content-Type", "application/json")
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);
    String responseBody = response.getResponse().getContentAsString();
    Map<String, Object> json = mapper.readValue(responseBody, Map.class);
    assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
  }

  // Authorization tests for /api/ucsbdiningcommonsmenuitem/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "chicken")
                .param("station", "entree")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "chicken")
                .param("station", "entree")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // Authorization tests for DELETE /api/ucsbdiningcommonsmenuitem?id=XXX
  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsbdiningcommonsmenuitem?id=123").param("id", "123").with(csrf()))
        // regular users cannot delete
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admin_can_delete() throws Exception {
    // test with no data, expect 404
    MvcResult result =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem?id=123").with(csrf()))
            .andExpect(status().is(404))
            .andReturn();
    // ensure that response message is correct
    String responseBody = result.getResponse().getContentAsString();
    Map<String, Object> json = mapper.readValue(responseBody, Map.class);
    assertEquals("record 123 not found", json.get("message"));

    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();
    when(ucsbDiningCommonsMenuItemRepository.findById(0L)).thenReturn(Optional.of(menuItem));
    result =
        mockMvc
            .perform(delete("/api/ucsbdiningcommonsmenuitem?id=0").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();
    // ensure that response message is correct
    responseBody = result.getResponse().getContentAsString();
    json = mapper.readValue(responseBody, Map.class);
    assertEquals("record 0 deleted", json.get("message"));
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(menuItem);
  }

  // Test that admins can create a new menu item
  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admin_can_create_menu_item() throws Exception {

    UCSBDiningCommonsMenuItem expectedItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("chicken")
            .station("entree")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(any())).thenReturn(expectedItem);

    // check for 200 status
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "chicken")
                    .param("station", "entree")
                    .with(csrf()))
            .andExpect(status().is(200))
            .andReturn();

    String expectedJson = mapper.writeValueAsString(expectedItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
