package game.gateway.netty.server.handler;

import game.common.util.DelayTaskUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DispatcherHandler extends Handler{
    private static final Map<String, Handler> handlerObjectMap = new ConcurrentHashMap<>();

    public DispatcherHandler(String cmd) {
        registerHandler(cmd, this);
    }

    public static Handler getHandler(String cmd){
        Handler handler = handlerObjectMap.get(cmd);
        if(handler == null){
            throw new NullPointerException("根据cmd:"+cmd+"找不到handler");
        }
        return handlerObjectMap.get(cmd);
    }

    @Override
    public void registerHandler(String cmd, Handler handler) {
        handlerObjectMap.putIfAbsent(cmd, handler);
    }


    protected void delayTask(String taskId, Runnable task, long delay, TimeUnit unit){
        DelayTaskUtil.submit(taskId, task, delay, unit);
    }
}
