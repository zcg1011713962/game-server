package game.hall.exception;

import lombok.Getter;

/**
 * 自定义大厅业务异常
 */
@Getter
public class HallException extends RuntimeException {

    private final int code;

    public HallException(String message) {
        super(message);
        this.code = 5000; // 默认业务异常码
    }

    public HallException(int code, String message) {
        super(message);
        this.code = code;
    }

    public HallException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
