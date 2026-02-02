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
public class Project {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String workingDirectory;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        DELETED
    }
}
