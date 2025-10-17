package SampleJWT.auth.repository;

import SampleJWT.auth.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByTypeAndStatus(String type, String status);
    List<Item> findByTypeOrderByEventDateDesc(String type);

    @Query("SELECT i FROM Item i")
    List<Item> getAll();

    // Get all items (lost + found) reported by a user
    List<Item> findByReporterId(String reporterId);

    // Get only lost items reported by a user
    List<Item> findByReporterIdAndType(String reporterId, String type);
}
