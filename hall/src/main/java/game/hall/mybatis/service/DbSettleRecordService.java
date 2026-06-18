package game.hall.mybatis.service;

import game.hall.mybatis.domain.DbSettleRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_settle_record(牌九玩家结算记录表)】的数据库操作Service
* @createDate 2026-06-17 17:43:45
*/
public interface DbSettleRecordService extends IService<DbSettleRecord> {

}
