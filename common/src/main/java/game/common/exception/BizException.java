package game.common.exception;

import game.common.constant.ErrorCode;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final String msg;

    public BizException(ErrorCode error) {
        super(error.msg());
        this.code = error.code();
        this.msg = error.msg();
    }

    public BizException(ErrorCode error, String msg) {
        super(msg);
        this.code = error.code();
        this.msg = msg;
    }


}