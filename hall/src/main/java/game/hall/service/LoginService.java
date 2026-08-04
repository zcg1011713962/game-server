package game.hall.service;

import game.common.protocol.ServerMsg;
import game.hall.entity.req.GuestLoginReq;
import game.hall.entity.req.LoginTokenReq;

public interface LoginService {
    ServerMsg loginByGuest(GuestLoginReq guestLoginReq);

    ServerMsg loginByPassword(LoginTokenReq req);
}
