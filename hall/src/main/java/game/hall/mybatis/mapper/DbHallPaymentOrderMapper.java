package game.hall.mybatis.mapper;

import game.hall.mybatis.domain.DbHallPaymentOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zcg10
* @description 针对表【db_hall_payment_order(OKX充值订单)】的数据库操作Mapper
* @createDate 2026-09-11 18:52:12
* @Entity game.hall.mybatis.domain.DbHallPaymentOrder
*/
@Mapper
public interface DbHallPaymentOrderMapper extends BaseMapper<DbHallPaymentOrder> {

}




