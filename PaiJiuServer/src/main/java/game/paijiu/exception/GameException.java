package game.paijiu.exception;

import game.common.constant.GameError;
import lombok.Getter;

@Getter
public class GameException extends RuntimeException {

    private final int code;

    public GameException(int code, String message) {
        super(message);
        this.code = code;
    }

    public GameException(GameError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }
}
