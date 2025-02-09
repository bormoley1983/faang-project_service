package faang.school.projectservice.repository;

import faang.school.projectservice.model.Campaign;
import faang.school.projectservice.model.CampaignStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByTitleAndProjectId(String title, Long projectId);

    @Query(
            "SELECT c FROM Campaign c " +
            "WHERE (:namePattern IS NULL OR c.title LIKE %:namePattern%) " +
            "AND (:minGoal IS NULL OR c.goal >= :minGoal) " +
            "AND (:maxGoal IS NULL OR c.goal <= :maxGoal) " +
            "AND (:status IS NULL OR c.status = :status)"
    )
    List<Campaign> findAllByFilters(@Param("namePattern") String namePattern,
                                    @Param("minGoal") BigDecimal minGoal,
                                    @Param("maxGoal") BigDecimal maxGoal,
                                    @Param("status") String status,
                                    Pageable pageable);

    @Query(
            "SELECT c FROM Campaign c " +
                    "WHERE c.project.id = :projectId " +
                    "AND (:idCreatedBy IS NULL OR c.createdBy = :idCreatedBy) " +
                    "AND (:namePattern IS NULL OR c.title LIKE %:namePattern%) " +
                    "AND (:minGoal IS NULL OR c.goal >= :minGoal) " +
                    "AND (:maxGoal IS NULL OR c.goal <= :maxGoal) " +
                    "AND ((cast(:dateCreatedFrom as date) is null ) OR :dateCreatedFrom <= c.createdAt ) " +
                    "AND ((cast(:dateCreatedTo as date) is null ) OR :dateCreatedTo >= c.createdAt ) " +
                    "AND ((cast(:dateUpdatedFrom as date) is null ) OR :dateUpdatedFrom <= c.updatedAt ) " +
                    "AND ((cast(:dateUpdatedTo as date) is null ) OR :dateUpdatedTo >= c.updatedAt ) " +
                    "AND (:status IS NULL OR c.status = :status) " +
                    "ORDER BY c.createdAt DESC"
    )
    List<Campaign> findAllByFiltersAndProjectId(@Param("projectId") Long projectId,
                                                @Param("idCreatedBy") Long idCreatedBy,
                                                @Param("namePattern") String namePattern,
                                                @Param("minGoal") BigDecimal minGoal,
                                                @Param("maxGoal") BigDecimal maxGoal,
                                                @Param("dateCreatedFrom") LocalDateTime dateCreatedFrom,
                                                @Param("dateCreatedTo") LocalDateTime dateCreatedTo,
                                                @Param("dateUpdatedFrom") LocalDateTime dateUpdatedFrom,
                                                @Param("dateUpdatedTo") LocalDateTime dateUpdatedTo,
                                                @Param("status") CampaignStatus status);
}
