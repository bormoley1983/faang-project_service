package faang.school.projectservice.repository;

import faang.school.projectservice.model.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import faang.school.projectservice.model.InternshipStatus;

public interface InternshipRepository extends JpaRepository<Internship, Long> {
    @Query("""
            SELECT DISTINCT i FROM Internship i LEFT JOIN i.interns intern
            WHERE (:status IS NULL OR i.status = :status)
              AND (:roleId IS NULL OR i.mentorId.id = :roleId OR intern.id = :roleId)
            """)
    Page<Internship> search(InternshipStatus status, Long roleId, Pageable pageable);
}
