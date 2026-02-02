package kr.yonggeon.claudebot.service;

import kr.yonggeon.claudebot.domain.Project;
import kr.yonggeon.claudebot.domain.UserSession;
import kr.yonggeon.claudebot.mapper.UserSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionMapper sessionMapper;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public Optional<UserSession> findByUserId(Long userId) {
        return sessionMapper.findByUserId(userId);
    }

    @Transactional
    public UserSession getOrCreateSession(Long userId) {
        return sessionMapper.findByUserId(userId)
                .orElseGet(() -> createSession(userId));
    }

    private UserSession createSession(Long userId) {
        UserSession session = UserSession.builder()
                .userId(userId)
                .build();

        sessionMapper.insert(session);
        log.info("Created session for user: userId={}", userId);
        return session;
    }

    @Transactional
    public void selectProject(Long userId, Long projectId) {
        getOrCreateSession(userId);
        sessionMapper.updateCurrentProject(userId, projectId);
        log.info("Selected project: userId={}, projectId={}", userId, projectId);
    }

    @Transactional
    public void clearCurrentProject(Long userId) {
        sessionMapper.updateCurrentProject(userId, null);
        log.info("Cleared current project: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public Optional<Project> getCurrentProject(Long userId) {
        return sessionMapper.findByUserId(userId)
                .map(UserSession::getCurrentProjectId)
                .filter(projectId -> projectId != null)
                .flatMap(projectService::findById)
                .filter(project -> project.getStatus() == Project.ProjectStatus.ACTIVE);
    }

    @Transactional
    public void setState(Long userId, String state, String stateData) {
        getOrCreateSession(userId);
        sessionMapper.updateState(userId, state, stateData);
        log.debug("Updated state: userId={}, state={}", userId, state);
    }

    @Transactional
    public void clearState(Long userId) {
        sessionMapper.updateState(userId, null, null);
        log.debug("Cleared state: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public String getState(Long userId) {
        return sessionMapper.findByUserId(userId)
                .map(UserSession::getState)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String getStateData(Long userId) {
        return sessionMapper.findByUserId(userId)
                .map(UserSession::getStateData)
                .orElse(null);
    }
}
