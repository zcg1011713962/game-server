package game.hall.mybatis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import game.hall.mybatis.domain.MailUser;
import game.hall.mybatis.service.MailUserService;
import game.hall.mybatis.mapper.MailUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zcg10
* @description 针对表【mail_user】的数据库操作Service实现
* @createDate 2026-08-01 17:43:10
*/
@Service
public class MailUserServiceImpl extends ServiceImpl<MailUserMapper, MailUser>
    implements MailUserService{

}




