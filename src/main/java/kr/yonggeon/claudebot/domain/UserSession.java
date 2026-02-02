package kr.yonggeon.claudebot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private Long id;
    private Long userId;
    private Long currentProjectId;
    private String state;
    private String stateData;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
