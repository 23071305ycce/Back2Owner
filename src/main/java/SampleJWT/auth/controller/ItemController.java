package SampleJWT.auth.controller;

import SampleJWT.auth.entity.Item;
import SampleJWT.auth.entity.User;
import SampleJWT.auth.repository.UserRepository;
import SampleJWT.auth.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final UserRepository userRepository;

    public ItemController(ItemService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    // FURTHER USE: CONVERT AUTH TO ID
    private String getUserIdFromAuth(Authentication auth) {
        if (auth == null) {
            throw new RuntimeException("Unauthorized");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getCollegeId();
    }

    // FURTHER USE: CHECK IF AUTH IS ADMIN
    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // REPORT A LOST ITEM
    @PostMapping(value = "/lost", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Item reportLost(Authentication auth, @RequestBody Item item) {
        String userId = getUserIdFromAuth(auth);
        return service.createLostItem(userId, item);
    }

    // REPORT A FOUND ITEM
    @PostMapping(value = "/found", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Item reportFound(Authentication auth, @RequestBody Item body) {
        String userId = getUserIdFromAuth(auth);
        return service.createFoundItem(userId, body);
    }

    // COMPARE THE DESCRIPTION OF LOST TO FOUND
    @GetMapping("/{id}/ai-matches")
    public ResponseEntity<String> getAIMatches(@PathVariable Long id) {
        try {
            String aiResponse = service.findAIMatches(id);
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error finding matches: " + e.getMessage());
        }
    }

    // GET ITEM BY ID
    @GetMapping("/{id}")
    public Item getById(@PathVariable Long id) {
        return service.get(id);
    }

    // UPDATE THE REPORT
    @PutMapping("/{id}")
    public Item update(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody Item updates
    ) {
        String userId = getUserIdFromAuth(auth);
        boolean isAdmin = isAdmin(auth);
        return service.updateWithOwnershipOrAdmin(id, userId, isAdmin, updates);
    }

    // DELETE THE REPORT THROUGH ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        String userId = getUserIdFromAuth(auth);
        boolean isAdmin = isAdmin(auth);
        service.deleteWithOwnershipOrAdmin(id, userId, isAdmin);
    }

    // MARK IF THE PRODUCT IS RETURNED
    @PatchMapping("/{id}/mark-returned")
    public Item markReturned(Authentication auth, @PathVariable Long id) {
        String userId = getUserIdFromAuth(auth);
        return service.markReturned(id, userId);
    }

    // GET ALL FOUND REPORTS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/found")
    public ResponseEntity<List<Item>> getAllFoundProducts() {
        List<Item> response = service.getAllFoundAnyStatus();
        return ResponseEntity.ok(response);
    }

    // GET ALL LOST REPORTS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/lost")
    public ResponseEntity<List<Item>> getAllLostProducts() {
        List<Item> response = service.getAllLostAnyStatus();
        return ResponseEntity.ok(response);
    }

    // GET ALL REPORTS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getall")
    public ResponseEntity<List<Item>> getAllProducts() {
        List<Item> response = service.getAllProducts();
        return ResponseEntity.ok(response);
    }

    // Get all reports by the current user (both lost + found)
    @GetMapping("/my")
    public List<Item> getAllUserItems(@RequestParam String userId) {
        return service.getAllItemsByUserId(userId);
    }

    // Get all lost reports by the current user
    @GetMapping("/my/lost")
    public List<Item> getUserLostItems(@RequestParam String userId) {
        return service.getLostItemsByUserId(userId);
    }

    // Get all found reports by the current user
    @GetMapping("/my/found")
    public List<Item> getUserFoundItems(@RequestParam String userId) {
        return service.getFoundItemsByUserId(userId);
    }
}
