package game.gomoku.room;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class GomokuRoom {
    public static final int SIZE = 15;
    private final long roomId;
    private Long blackUserId;
    private Long whiteUserId;
    private Long turnUserId;
    private final int[][] board = new int[SIZE][SIZE];
    private boolean started;
    private boolean finished;
    private long turnStartedAt;
    private long turnTimeoutSeconds = 30;
    private Long botUserId;
    private Map<String, Object> botProfile = Map.of();

    public GomokuRoom(long roomId) { this.roomId = roomId; }
    public long getRoomId() { return roomId; }
    public Long getBlackUserId() { return blackUserId; }
    public Long getWhiteUserId() { return whiteUserId; }
    public Long getTurnUserId() { return turnUserId; }
    public boolean isStarted() { return started; }
    public boolean isFinished() { return finished; }
    public long getTurnStartedAt() { return turnStartedAt; }
    public long getTurnTimeoutSeconds() { return turnTimeoutSeconds; }
    public void setTurnTimeoutSeconds(long turnTimeoutSeconds) { this.turnTimeoutSeconds = Math.max(1, turnTimeoutSeconds); }
    public Long getBotUserId() { return botUserId; }
    public Map<String, Object> getBotProfile() { return botProfile; }
    public void setBotProfile(Map<String, Object> botProfile) {
        this.botProfile = new LinkedHashMap<>(botProfile);
        Object id = botProfile.get("id");
        this.botUserId = id instanceof Number number ? number.longValue() : null;
    }
    public int[][] getBoard() { return board; }

    public boolean join(long userId) {
        if (blackUserId == null) { blackUserId = userId; return false; }
        if (blackUserId.equals(userId)) return started;
        if (whiteUserId == null) { whiteUserId = userId; started = true; turnUserId = blackUserId; turnStartedAt = System.currentTimeMillis(); return true; }
        throw new IllegalStateException("Room is full");
    }

    public int move(long userId, int x, int y) {
        if (!started || finished) throw new IllegalStateException("Game is not running");
        if (!userIdEquals(turnUserId, userId)) throw new IllegalStateException("Not your turn");
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE || board[x][y] != 0) throw new IllegalArgumentException("Invalid move");
        int stone = blackUserId.equals(userId) ? 1 : 2;
        board[x][y] = stone;
        if (win(x, y, stone)) finished = true;
        else { turnUserId = stone == 1 ? whiteUserId : blackUserId; turnStartedAt = System.currentTimeMillis(); }
        return stone;
    }

    public boolean finishByTimeout(long now, long timeoutMillis) {
        if (!started || finished || turnStartedAt <= 0 || now - turnStartedAt < timeoutMillis) return false;
        finished = true;
        return true;
    }

    private boolean userIdEquals(Long expected, long userId) { return expected != null && expected == userId; }
    private boolean win(int x, int y, int stone) {
        int[][] dirs={{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d:dirs) if (count(x,y,d[0],d[1],stone)+count(x,y,-d[0],-d[1],stone)-1>=5) return true;
        return false;
    }
    private int count(int x,int y,int dx,int dy,int stone){int n=0;while(x>=0&&y>=0&&x<SIZE&&y<SIZE&&board[x][y]==stone){n++;x+=dx;y+=dy;}return n;}
}

