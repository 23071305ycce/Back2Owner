package SampleJWT.auth.service;

import SampleJWT.auth.entity.Item;
import SampleJWT.auth.repository.ItemRepository;
import SampleJWT.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemService {
    @Autowired
    UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    //Add lost item : /items/lost
    public Item createLostItem(Long reporterId,Item item) {
        Item createdItem = buildBaseItem(
                reporterId,
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getLocation(),
                item.getEventDate(),
                item.getImageUrl());
        createdItem.setType("LOST");
        createdItem.setStatus("OPEN");
        return itemRepository.save(createdItem);
    }

    //Add found item : /items/found
    public Item createFoundItem(Long reporterId, Item body) {
        Item createdItem = buildBaseItem(
                reporterId,
                body.getTitle(),
                body.getDescription(),
                body.getCategory(),
                body.getLocation(),
                body.getEventDate(),
                body.getImageUrl()
        );
        createdItem.setType("FOUND");
        createdItem.setStatus("OPEN");
        return itemRepository.save(createdItem);
    }

    //get item by id
    public Item get(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    //AI matching
    public String findAIMatches(Long lostItemId) {
        // Get the lost item
        Item lostItem = itemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lost item not found"));

        if (!"LOST".equals(lostItem.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item must be of type LOST to find matches");
        }
        //all open found reports
        List<Item> foundItems = itemRepository.findByTypeAndStatus("FOUND", "OPEN");
        return geminiService.findMatchesWithAI(lostItem, foundItems);
    }

    // Update item (either owner or admin)
    public Item updateWithOwnershipOrAdmin(Long id, Long userId, boolean isAdmin, Item updates) {
        Item existing = get(id);

        if (!isAdmin && !existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can update");
        }

        // Update fields if provided
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getLocation() != null) existing.setLocation(updates.getLocation());
        if (updates.getEventDate() != null) existing.setEventDate(updates.getEventDate());
        if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());

        // type and reporterId are immutable through this endpoint
        return itemRepository.save(existing);
    }

    // Delete item (either owner or admin)
    public void deleteWithOwnershipOrAdmin(Long id, Long userId, boolean isAdmin) {
        Item existing = get(id);

        if (!isAdmin && !existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can delete");
        }

        itemRepository.deleteById(id);
    }

    // Mark item as returned/closed
    public Item markReturned(Long id, Long userId) {
        Item existing = get(id);

        if (!existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can mark returned");
        }

        existing.setStatus("CLOSED");
        return itemRepository.save(existing);
    }

    //get all found reports
    public List<Item> getAllFoundAnyStatus() {
        return itemRepository.findByTypeOrderByEventDateDesc("FOUND");
    }

    //get all lost reports
    public List<Item> getAllLostAnyStatus() {
        return itemRepository.findByTypeOrderByEventDateDesc("LOST");
    }

    //get all reports
    public List<Item> getAllProducts(){
        return itemRepository.getAll();
    }

    // Get all items by userId
    public List<Item> getAllItemsByUserId(Long userId) {
        return itemRepository.findByReporterId(userId);
    }

    // Get all lost items by userId
    public List<Item> getLostItemsByUserId(Long userId) {
        return itemRepository.findByReporterIdAndType(userId, "LOST");
    }

    // Get all found items by userId
    public List<Item> getFoundItemsByUserId(Long userId) {
        return itemRepository.findByReporterIdAndType(userId, "FOUND");
    }

    // Helper method to build base item
    private Item buildBaseItem(Long reporterId,
                               String title,
                               String description,
                               String category,
                               String location,
                               LocalDate eventDate,
                               String imageUrl) {
        Item item = new Item();
        item.setReporterId(reporterId);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(eventDate);
        item.setImageUrl(imageUrl);
        return item;
    }

}
