package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbMail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_mail】的数据库操作Mapper
* @createDate 2026-08-12 15:03:51
* @Entity game.hall.mybatis.domain.DbMail
*/
@Mapper
public interface DbMailMapper extends BaseMapper<DbMail> {

}




