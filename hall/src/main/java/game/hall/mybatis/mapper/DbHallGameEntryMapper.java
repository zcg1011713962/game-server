package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbHallGameEntry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_hall_game_entry(大厅游戏入口配置)】的数据库操作Mapper
* @createDate 2026-08-12 15:04:23
* @Entity game.hall.mybatis.domain.DbHallGameEntry
*/
@Mapper
public interface DbHallGameEntryMapper extends BaseMapper<DbHallGameEntry> {

}




