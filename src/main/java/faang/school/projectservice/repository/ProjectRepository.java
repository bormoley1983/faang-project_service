package faang.school.projectservice.repository;

import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.ProjectStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(
            "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
                    "FROM Project p " +
                    "WHERE p.ownerId = :ownerId AND p.name = :name"
    )
    boolean existsByOwnerIdAndName(Long ownerId, String name);

    @Query(
            "SELECT DISTINCT tm.userId " +
                    "FROM TeamMember tm " +
                    "JOIN tm.team t " +
                    "WHERE t.project.id IN :projectIds"
    )
    List<Long> getUserIdsByProjectIds(List<Long> projectIds);

    @Query("SELECT tm.userId FROM TeamMember tm " +
            "JOIN tm.team t " +
            "JOIN t.project p " +
            "WHERE p.id = :projectId")
    List<Long> findAllTeamMemberUserIdsByProjectId(@Param("projectId") Long projectId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.parentProject IS NULL
              AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR p.status = :status)
              AND (p.visibility = faang.school.projectservice.model.ProjectVisibility.PUBLIC
                   OR p.ownerId = :userId
                   OR EXISTS (
                       SELECT tm.id FROM TeamMember tm
                       WHERE tm.team.project = p AND tm.userId = :userId
                   ))
            """)
    Page<Project> findVisibleProjects(@Param("name") String name,
                                      @Param("status") ProjectStatus status,
                                      @Param("userId") Long userId,
                                      Pageable pageable);

    @Query("""
            SELECT p FROM Project p
            WHERE p.parentProject.id = :parentProjectId
              AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR p.status = :status)
              AND (p.visibility = faang.school.projectservice.model.ProjectVisibility.PUBLIC
                   OR p.ownerId = :userId
                   OR EXISTS (
                       SELECT tm.id FROM TeamMember tm
                       WHERE tm.team.project = p AND tm.userId = :userId
                   ))
            """)
    Page<Project> findVisibleSubProjects(@Param("parentProjectId") Long parentProjectId,
                                         @Param("name") String name,
                                         @Param("status") ProjectStatus status,
                                         @Param("userId") Long userId,
                                         Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.googleCalendarId = :googleCalendarId")
    Project findByGoogleCalendarId(String googleCalendarId);
}

