package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbSettleRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_settle_record(牌九玩家结算记录表)】的数据库操作Mapper
* @createDate 2026-07-24 10:45:56
* @Entity game.hall.mybatis.domain.DbSettleRecord
*/
@Mapper
public interface DbSettleRecordMapper extends BaseMapper<DbSettleRecord> {

}




