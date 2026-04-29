package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB Dining Commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all UCSB Dining Commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Create a new menu item
   *
   * @param diningCommonsCode the code of the dining commons (e.g. "ortega", "dlg")
   * @param name the name of the menu item
   * @param station the station where the item is served (e.g. "Entrees", "Desserts")
   * @return the saved menu item
   */
  @Operation(summary = "Create a new menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(
              name = "diningCommonsCode",
              description = "Code of the dining commons (e.g. ortega, dlg)")
          @RequestParam
          String diningCommonsCode,
      @Parameter(name = "name", description = "Name of the menu item (e.g. 'chicken')")
          @RequestParam
          String name,
      @Parameter(
              name = "station",
              description = "Station where the item is served (e.g. Entrees, Desserts)")
          @RequestParam
          String station)
      throws JsonProcessingException {
    // if any of the parameters are empty or null, ??

    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode(diningCommonsCode)
            .name(name)
            .station(station)
            .build();
    UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return savedMenuItem;
  }

  /**
   * Delete a single menu item by id, ex: /api/ucsbdiningcommonsmenuitem?id=XXX
   *
   * @param id the id of the menu item
   * @return the id that was deleted (if it exists) or was not deleted (if not found)
   */
  @Operation(summary = "Delete a single menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));
    ucsbDiningCommonsMenuItemRepository.delete(menuItem);
    return genericMessage("record %s deleted".formatted(id));
  }
}
