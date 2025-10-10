package SampleJWT.auth.controller;

import SampleJWT.auth.entity.Item;
import SampleJWT.auth.entity.User;
import SampleJWT.auth.repository.UserRepository;
import SampleJWT.auth.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final UserRepository userRepository;

    public ItemController(ItemService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromAuth(Authentication auth) {
        if (auth == null) {
            throw new RuntimeException("Unauthorized");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping(value = "/lost", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Item reportLost(Authentication auth, @RequestBody Item body) {
        Long userId = getUserIdFromAuth(auth);
        return service.createLostItem(
                userId,
                body.getTitle(),
                body.getDescription(),
                body.getCategory(),
                body.getLocation(),
                body.getEventDate(),
                body.getImageUrl()
        );
    }

    @PostMapping(value = "/found", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Item reportFound(Authentication auth, @RequestBody Item body) {
        Long userId = getUserIdFromAuth(auth);
        return service.createFoundItem(
                userId,
                body.getTitle(),
                body.getDescription(),
                body.getCategory(),
                body.getLocation(),
                body.getEventDate(),
                body.getImageUrl()
        );
    }

    @GetMapping
    public Page<Item> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        return service.search(null, category, location, q, startDate, endDate, pageable);
    }

    @GetMapping("/lost")
    public Page<Item> getAllLost(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        return service.search("LOST", category, location, q, startDate, endDate, pageable);
    }

    @GetMapping("/found")
    public Page<Item> getAllFound(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        return service.search("FOUND", category, location, q, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    public Item getById(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public Item update(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody Item updates
    ) {
        Long userId = getUserIdFromAuth(auth);
        return service.updateIfOwner(id, userId, updates);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(auth);
        boolean isAdmin = isAdmin(auth);
        service.deleteWithOwnershipOrAdmin(id, userId, isAdmin);
    }

    @PatchMapping("/{id}/mark-returned")
    public Item markReturned(Authentication auth, @PathVariable Long id) {
        Long userId = getUserIdFromAuth(auth);
        return service.markReturned(id, userId);
    }

    @GetMapping("/search")
    public Page<Item> searchByKeyword(
            @RequestParam String q,
            Pageable pageable
    ) {
        return service.searchByKeyword(q, pageable);
    }

    @GetMapping("/filter")
    public Page<Item> filter(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        return service.filter(type, category, location, startDate, endDate, pageable);
    }
}
