package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_config(配置表)】的数据库操作Mapper
* @createDate 2026-09-11 18:51:58
* @Entity game.hall.mybatis.domain.DbConfig
*/
@Mapper
public interface DbConfigMapper extends BaseMapper<DbConfig> {

}




