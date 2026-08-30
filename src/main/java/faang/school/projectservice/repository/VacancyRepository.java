package faang.school.projectservice.repository;

import faang.school.projectservice.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import faang.school.projectservice.model.TeamRole;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    @Query("""
            SELECT v FROM Vacancy v
            WHERE (:position IS NULL OR v.position = :position)
              AND (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Vacancy> search(TeamRole position, String name, Pageable pageable);
}
