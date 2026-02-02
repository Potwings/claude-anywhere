package kr.yonggeon.claudebot.mapper;

import kr.yonggeon.claudebot.domain.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {

    Optional<Project> findById(@Param("id") Long id);

    Optional<Project> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    List<Project> findByUserId(@Param("userId") Long userId);

    List<Project> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Project.ProjectStatus status);

    int insert(Project project);

    int update(Project project);

    int updateStatus(@Param("id") Long id, @Param("status") Project.ProjectStatus status);

    int deleteById(@Param("id") Long id);
}
