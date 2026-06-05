package game.hall.service;

import game.common.protocol.ServerMsg;
import game.hall.entity.req.GuestLoginReq;

public interface LoginService {
    ServerMsg loginByGuest(GuestLoginReq guestLoginReq);
}
