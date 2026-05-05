package game.common.protocol;

import game.common.constant.ErrorCode;
import lombok.Data;

@Data
public class ServerMsg {

    /**
     * 消息命令
     * 例如：LOGIN_RESULT / BET_RESULT / PLAYER_BET
     */
    private String cmd;

    /**
     * 客户端请求序号
     * 请求响应类消息需要原样返回
     */
    private long seq;

    /**
     * 状态码
     * 0 = 成功
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private Object data;


    public static ServerMsg ok() {
        ServerMsg res = new ServerMsg();
        res.setCode(0);
        return res;
    }

    public static ServerMsg ok(String cmd, long seq, Object data, int code, String msg) {
        ServerMsg res = info(cmd, seq, code, msg);
        res.setData(data);
        return res;
    }

    public static ServerMsg info(String cmd, long seq, int code, String msg) {
        ServerMsg res = new ServerMsg();
        res.setCmd(cmd);
        res.setSeq(seq);
        res.setCode(code);
        res.setMsg(msg);
        return res;
    }

    public static ServerMsg error(String cmd, long seq, ErrorCode errorCode) {
        ServerMsg res = new ServerMsg();
        res.setCmd(cmd);
        res.setSeq(seq);
        res.setCode(errorCode.code());
        res.setMsg(errorCode.msg());
        return res;
    }

}