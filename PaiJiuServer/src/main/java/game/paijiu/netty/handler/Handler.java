package game.paijiu.netty.handler;


import game.common.entity.Packet;
import game.common.entity.req.GameRequest;

public abstract class Handler {
    /**
     * 注册handler
     * @param cmd
     * @param handler
     */
    public abstract void registerHandler(String cmd, Handler handler);

    /**
     * 执行方法
     */
    public void exec(GameRequest gameRequest){}
}
