package ua.haponov.timetracker.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.haponov.timetracker.auth.User;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findProjectsByIsCompletedIsFalse();

    List<Project> findAllByUser(User currentUser);

    List<Project> findByUserAndIsCompletedIsFalse(User currentUser);
}
