package game.hall.mapper;

import game.hall.domain.DbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_user】的数据库操作Mapper
* @createDate 2026-04-27 16:12:42
* @Entity game.hall.domain.DbUser
*/
@Mapper
public interface DbUserMapper extends BaseMapper<DbUser> {

}




