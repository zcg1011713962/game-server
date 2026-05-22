package game.hall.mapper;

import game.hall.domain.DbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_user(用户表)】的数据库操作Mapper
* @createDate 2026-05-22 18:16:26
* @Entity game.hall.domain.DbUser
*/
@Mapper
public interface DbUserMapper extends BaseMapper<DbUser> {

}




