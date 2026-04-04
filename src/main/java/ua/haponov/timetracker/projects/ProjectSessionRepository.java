package ua.haponov.timetracker.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectSessionRepository extends JpaRepository<ProjectSession, Long> {

    List<ProjectSession> findAllByProjectIdOrderByStartTimeDesc(Long projectId);

}
