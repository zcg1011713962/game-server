package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbHallPaymentOrder;
import game.hall.mybatis.service.DbHallPaymentOrderService;
import game.hall.mybatis.mapper.DbHallPaymentOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_hall_payment_order(OKX充值订单)】的数据库操作Service实现
* @createDate 2026-09-11 18:52:12
*/
@Service
public class DbHallPaymentOrderServiceImpl extends ServiceImpl<DbHallPaymentOrderMapper, DbHallPaymentOrder>
    implements DbHallPaymentOrderService{

}




