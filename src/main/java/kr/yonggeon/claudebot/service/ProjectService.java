package kr.yonggeon.claudebot.service;

import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.Project.ProjectStatus;
import kr.yonggeon.claudebot.exception.DuplicateProjectException;
import kr.yonggeon.claudebot.exception.ProjectNotFoundException;
import kr.yonggeon.claudebot.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectMapper.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findByUserIdAndName(Long userId, String name) {
        return projectMapper.findByUserIdAndName(userId, name);
    }

    @Transactional(readOnly = true)
    public List<Project> findByUserId(Long userId) {
        return projectMapper.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Project> findActiveProjects(Long userId) {
        return projectMapper.findByUserIdAndStatus(userId, ProjectStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Project> findArchivedProjects(Long userId) {
        return projectMapper.findByUserIdAndStatus(userId, ProjectStatus.ARCHIVED);
    }

    @Transactional
    public Project createProject(Long userId, String name, String description) {
        // Check for duplicate project name
        if (projectMapper.findByUserIdAndName(userId, name).isPresent()) {
            throw new DuplicateProjectException("Project with name '" + name + "' already exists");
        }

        Project project = Project.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .status(ProjectStatus.ACTIVE)
                .build();

        projectMapper.insert(project);
        log.info("Created project: userId={}, name={}", userId, name);
        return project;
    }

    @Transactional
    public Project createProject(Long userId, String name) {
        return createProject(userId, name, null);
    }

    @Transactional
    public Project updateProject(Long projectId, String name, String description) {
        Project project = projectMapper.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        // Check for duplicate name if name is being changed
        if (name != null && !name.equals(project.getName())) {
            if (projectMapper.findByUserIdAndName(project.getUserId(), name).isPresent()) {
                throw new DuplicateProjectException("Project with name '" + name + "' already exists");
            }
            project.setName(name);
        }

        if (description != null) {
            project.setDescription(description);
        }

        projectMapper.update(project);
        log.info("Updated project: id={}", projectId);
        return project;
    }

    @Transactional
    public void archiveProject(Long projectId) {
        Project project = projectMapper.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        projectMapper.updateStatus(projectId, ProjectStatus.ARCHIVED);
        log.info("Archived project: id={}, name={}", projectId, project.getName());
    }

    @Transactional
    public void unarchiveProject(Long projectId) {
        Project project = projectMapper.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        projectMapper.updateStatus(projectId, ProjectStatus.ACTIVE);
        log.info("Unarchived project: id={}, name={}", projectId, project.getName());
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectMapper.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        // Soft delete - mark as DELETED
        projectMapper.updateStatus(projectId, ProjectStatus.DELETED);
        log.info("Deleted project: id={}, name={}", projectId, project.getName());
    }

    @Transactional
    public void hardDeleteProject(Long projectId) {
        projectMapper.deleteById(projectId);
        log.info("Hard deleted project: id={}", projectId);
    }

    @Transactional
    public Project setWorkingDirectory(Long projectId, String workingDirectory) {
        Project project = projectMapper.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + projectId));

        project.setWorkingDirectory(workingDirectory);
        projectMapper.update(project);
        log.info("Set working directory for project: id={}, dir={}", projectId, workingDirectory);
        return project;
    }
}
