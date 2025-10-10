package SampleJWT.auth.repository;

import SampleJWT.auth.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(
            value = """
            SELECT i FROM Item i
            WHERE (:type IS NULL OR LOWER(i.type) = LOWER(:type))
              AND (:category IS NULL OR LOWER(i.category) = LOWER(:category))
              AND (COALESCE(:location, '') = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (
                    COALESCE(:q, '') = ''
                 OR LOWER(i.title)       LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%'))
              )
              AND (:startDate IS NULL OR i.eventDate >= :startDate)
              AND (:endDate   IS NULL OR i.eventDate <= :endDate)
        """,
            countQuery = """
            SELECT COUNT(i) FROM Item i
            WHERE (:type IS NULL OR LOWER(i.type) = LOWER(:type))
              AND (:category IS NULL OR LOWER(i.category) = LOWER(:category))
              AND (COALESCE(:location, '') = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (
                    COALESCE(:q, '') = ''
                 OR LOWER(i.title)       LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%'))
              )
              AND (:startDate IS NULL OR i.eventDate >= :startDate)
              AND (:endDate   IS NULL OR i.eventDate <= :endDate)
        """
    )
    Page<Item> search(
            @Param("type") String type,
            @Param("category") String category,
            @Param("location") String location,
            @Param("q") String q,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
