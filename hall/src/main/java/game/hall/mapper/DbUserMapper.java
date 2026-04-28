package game.hall.mapper;

import game.hall.domain.DbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 广哥
* @description 针对表【db_user(用户表)】的数据库操作Mapper
* @createDate 2026-04-29 01:55:03
* @Entity game.hall.domain.DbUser
*/
@Mapper
public interface DbUserMapper extends BaseMapper<DbUser> {

}




