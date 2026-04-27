package game.gateway.netty.server.handler;


import game.common.entity.Packet;

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
    public void exec(Packet packet){}
}
