package kr.yonggeon.claudebot.mapper;

import kr.yonggeon.claudebot.domain.UserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserSessionMapper {

    Optional<UserSession> findByUserId(@Param("userId") Long userId);

    int insert(UserSession session);

    int update(UserSession session);

    int updateCurrentProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    int updateState(@Param("userId") Long userId, @Param("state") String state, @Param("stateData") String stateData);

    int deleteByUserId(@Param("userId") Long userId);
}
