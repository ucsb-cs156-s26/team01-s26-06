package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/** This is a REST controller for MenuItemreviews */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {
  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all MenuItemReviews
   *
   * @return an iterable of MenuItemReviews
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> menuItemReviews = menuItemReviewRepository.findAll();
    return menuItemReviews;
  }

  /**
   * Create a new menuItemReview
   *
   * @param itemId the id of the thing
   * @param reviewerEmail the email of the reviewer
   * @param stars the number of stars
   * @param dateReviewed the date of the review
   * @param comments the comments in the review
   * @return the saved menuItemReview
   */
  @Operation(summary = "Create a new date")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemreview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "comments") @RequestParam String comments,
      // do i need this?
      @Parameter(
              name = "dateReviewed",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SSZ; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateReviewed={}", dateReviewed);

    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setComments(comments);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }
}
