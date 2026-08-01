package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.Mail;
import game.hall.mybatis.service.MailService;
import game.hall.mybatis.mapper.MailMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【mail】的数据库操作Service实现
* @createDate 2026-08-01 17:43:10
*/
@Service
public class MailServiceImpl extends ServiceImpl<MailMapper, Mail>
    implements MailService{

}




