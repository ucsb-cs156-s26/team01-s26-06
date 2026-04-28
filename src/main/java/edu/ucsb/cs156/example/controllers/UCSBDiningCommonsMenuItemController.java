package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItemController")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB Dining common Menu Items
   *
   * @return an iterable of UCSBDate
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Create a new menu item
   *
   * @param diningCommonsCode the code of the dining commons
   * @param name the name of the menu item
   * @param station the station of the menu item (e.g. "Entrees" "Desserts")
   * @return the saved menu item
   */
  @Operation(summary = "Create a new menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {
    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode(diningCommonsCode)
            .name(name)
            .station(station)
            .build();
    UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return savedMenuItem;
  }
}
