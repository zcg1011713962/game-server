package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbGameInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_game_info(游戏基础配置)】的数据库操作Mapper
* @createDate 2026-08-12 15:04:17
* @Entity game.hall.mybatis.domain.DbGameInfo
*/
@Mapper
public interface DbGameInfoMapper extends BaseMapper<DbGameInfo> {

}




