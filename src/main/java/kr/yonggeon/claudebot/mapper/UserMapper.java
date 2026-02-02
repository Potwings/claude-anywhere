package kr.yonggeon.claudebot.mapper;

import kr.yonggeon.claudebot.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByTelegramId(@Param("telegramId") Long telegramId);

    int insert(User user);

    int update(User user);

    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
}
