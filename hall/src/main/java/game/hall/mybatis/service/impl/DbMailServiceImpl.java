package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.DbMail;
import game.hall.mybatis.service.DbMailService;
import game.hall.mybatis.mapper.DbMailMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【db_mail】的数据库操作Service实现
* @createDate 2026-08-12 15:03:51
*/
@Service
public class DbMailServiceImpl extends ServiceImpl<DbMailMapper, DbMail>
    implements DbMailService{

}




