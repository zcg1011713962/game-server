package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_user(用户表)】的数据库操作Mapper
* @createDate 2026-06-05 18:20:05
* @Entity game.hall.mybatis.domain.DbUser
*/
@Mapper
public interface DbUserMapper extends BaseMapper<DbUser> {

}




