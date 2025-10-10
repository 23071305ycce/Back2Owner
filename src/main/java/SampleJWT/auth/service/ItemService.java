package SampleJWT.auth.service;

import SampleJWT.auth.entity.Item;
import SampleJWT.auth.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // JSON create for LOST
    public Item createLostItem(Long reporterId,
                               String title,
                               String description,
                               String category,
                               String location,
                               LocalDate eventDate,
                               String imageUrl) {
        Item item = buildBaseItem(reporterId, title, description, category, location, eventDate, imageUrl);
        item.setType("LOST");
        item.setStatus("OPEN");
        return itemRepository.save(item);
    }

    // JSON create for FOUND
    public Item createFoundItem(Long reporterId,
                                String title,
                                String description,
                                String category,
                                String location,
                                LocalDate eventDate,
                                String imageUrl) {
        Item item = buildBaseItem(reporterId, title, description, category, location, eventDate, imageUrl);
        item.setType("FOUND");
        item.setStatus("OPEN");
        return itemRepository.save(item);
    }

    public Item get(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public Page<Item> search(String type,
                             String category,
                             String location,
                             String q,
                             LocalDate startDate,
                             LocalDate endDate,
                             Pageable pageable) {
        Pageable p = (pageable == null) ? PageRequest.of(0, 20) : pageable;
        return itemRepository.search(norm(type), norm(category), norm(location), norm(q), startDate, endDate, p);
    }

    public Item updateIfOwner(Long id, Long userId, Item updates) {
        Item existing = get(id);
        if (!existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can update");
        }
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
        if (updates.getLocation() != null) existing.setLocation(updates.getLocation());
        if (updates.getEventDate() != null) existing.setEventDate(updates.getEventDate());
        if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        // type and reporterId immutable through this endpoint
        return itemRepository.save(existing);
    }

    public void deleteWithOwnershipOrAdmin(Long id, Long userId, boolean isAdmin) {
        Item existing = get(id);
        if (!isAdmin && !existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can delete");
        }
        itemRepository.deleteById(id);
    }

    public Item markReturned(Long id, Long userId) {
        Item existing = get(id);
        if (!existing.getReporterId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can mark returned");
        }
        existing.setStatus("CLOSED");
        return itemRepository.save(existing);
    }

    public Page<Item> searchByKeyword(String q, Pageable pageable) {
        Pageable p = (pageable == null) ? PageRequest.of(0, 20) : pageable;
        // keyword-only search via unified query
        return itemRepository.search(null, null, null, norm(q), null, null, p);
    }

    public Page<Item> filter(String type,
                             String category,
                             String location,
                             LocalDate startDate,
                             LocalDate endDate,
                             Pageable pageable) {
        Pageable p = (pageable == null) ? PageRequest.of(0, 20) : pageable;
        // filter-only query via unified query
        return itemRepository.search(norm(type), norm(category), norm(location), null, startDate, endDate, p);
    }

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

    private static String norm(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
