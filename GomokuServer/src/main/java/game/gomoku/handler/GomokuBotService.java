package game.gomoku.handler;

import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.gomoku.netty.GatewayChannelManager;
import game.gomoku.room.GomokuRoom;
import game.gomoku.room.GomokuRoomManager;
import game.gomoku.util.GameThreadPoolUtil;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class GomokuBotService {
    private static final int EMPTY = 0;
    private static final int BLACK = 1;
    private static final int WHITE = 2;
    private static final int SEARCH_CANDIDATE_LIMIT = 14;
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    private final GomokuRoomManager roomManager;

    public GomokuBotService(GomokuRoomManager roomManager) {
        this.roomManager = roomManager;
    }
    public void play(GomokuRoom room, String gatewayId) {
        if (!room.getBotUserId().equals(room.getTurnUserId()) || room.isFinished()) return;
        GameThreadPoolUtil.execute(() -> { try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            GameThreadPoolUtil.executeRoom(String.valueOf(room.getRoomId()), () -> move(room, gatewayId)); });
    }
    private void move(GomokuRoom room, String gatewayId) {
        if (!room.getBotUserId().equals(room.getTurnUserId()) || room.isFinished()) return;
        int stone = room.getBotUserId().equals(room.getBlackUserId()) ? BLACK : WHITE;
        int[] position = selectMove(room.getBoard(), stone);
        int x = position[0];
        int y = position[1];
        room.move(room.getBotUserId(), x, y);
        Map<String,Object> data=GomokuMatchHandler.state(room); data.put("x",x); data.put("y",y); data.put("stone",stone);
        GameResponse push=GameResponse.push(room.getRoomId(), Cmd.GOMOKU_STATE, data); push.setGatewayId(gatewayId); GatewayChannelManager.send(gatewayId,push);
        if(room.isFinished()){data.put("winnerUserId",room.getBotUserId()); GameResponse finish=GameResponse.push(room.getRoomId(),Cmd.GOMOKU_FINISH,data);finish.setGatewayId(gatewayId);GatewayChannelManager.send(gatewayId,finish);roomManager.remove(room);}
    }

    private int[] selectMove(int[][] board, int botStone) {
        int opponentStone = otherStone(botStone);
        List<Move> candidates = collectCandidates(board);
        if (candidates.isEmpty()) return new int[]{GomokuRoom.SIZE / 2, GomokuRoom.SIZE / 2};

        for (Move move : candidates) if (isWinningMove(board, move.x, move.y, botStone)) return move.position();
        for (Move move : candidates) if (isWinningMove(board, move.x, move.y, opponentStone)) return move.position();

        candidates.forEach(move -> move.priority = evaluatePlacement(board, move.x, move.y, botStone)
                + evaluatePlacement(board, move.x, move.y, opponentStone) * 11 / 10);
        candidates.sort(Comparator.comparingInt((Move move) -> move.priority).reversed());
        if (candidates.size() > SEARCH_CANDIDATE_LIMIT) candidates = new ArrayList<>(candidates.subList(0, SEARCH_CANDIDATE_LIMIT));

        Move bestMove = candidates.get(0);
        int bestScore = Integer.MIN_VALUE;
        for (Move move : candidates) {
            board[move.x][move.y] = botStone;
            int searchScore = evaluatePosition(board, botStone) - strongestReplyScore(board, opponentStone) * 12 / 10;
            board[move.x][move.y] = EMPTY;
            if (searchScore > bestScore) {
                bestScore = searchScore;
                bestMove = move;
            }
        }
        return bestMove.position();
    }

    private int strongestReplyScore(int[][] board, int opponentStone) {
        List<Move> replies = collectCandidates(board);
        replies.forEach(move -> move.priority = evaluatePlacement(board, move.x, move.y, opponentStone));
        replies.sort(Comparator.comparingInt((Move move) -> move.priority).reversed());
        int strongest = 0;
        for (int i = 0; i < Math.min(SEARCH_CANDIDATE_LIMIT, replies.size()); i++) {
            Move reply = replies.get(i);
            board[reply.x][reply.y] = opponentStone;
            strongest = Math.max(strongest, evaluatePosition(board, opponentStone));
            board[reply.x][reply.y] = EMPTY;
        }
        return strongest;
    }

    private List<Move> collectCandidates(int[][] board) {
        boolean[][] selected = new boolean[GomokuRoom.SIZE][GomokuRoom.SIZE];
        boolean hasStone = false;
        for (int x = 0; x < GomokuRoom.SIZE; x++) for (int y = 0; y < GomokuRoom.SIZE; y++) {
            if (board[x][y] == EMPTY) continue;
            hasStone = true;
            for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) {
                int nextX = x + dx;
                int nextY = y + dy;
                if (inBoard(nextX, nextY) && board[nextX][nextY] == EMPTY) selected[nextX][nextY] = true;
            }
        }
        List<Move> moves = new ArrayList<>();
        if (!hasStone) {
            moves.add(new Move(GomokuRoom.SIZE / 2, GomokuRoom.SIZE / 2));
            return moves;
        }
        for (int x = 0; x < GomokuRoom.SIZE; x++) for (int y = 0; y < GomokuRoom.SIZE; y++) {
            if (selected[x][y]) moves.add(new Move(x, y));
        }
        return moves;
    }

    private boolean isWinningMove(int[][] board, int x, int y, int stone) {
        board[x][y] = stone;
        boolean win = isWin(board, x, y, stone);
        board[x][y] = EMPTY;
        return win;
    }

    private boolean isWin(int[][] board, int x, int y, int stone) {
        for (int[] direction : DIRECTIONS) {
            int total = count(board, x, y, direction[0], direction[1], stone)
                    + count(board, x, y, -direction[0], -direction[1], stone) - 1;
            if (total >= 5) return true;
        }
        return false;
    }

    private int evaluatePosition(int[][] board, int stone) {
        int best = 0;
        for (Move move : collectCandidates(board)) best = Math.max(best, evaluatePlacement(board, move.x, move.y, stone));
        return best;
    }

    private int evaluatePlacement(int[][] board, int x, int y, int stone) {
        if (board[x][y] != EMPTY) return 0;
        board[x][y] = stone;
        int score = 0;
        for (int[] direction : DIRECTIONS) {
            int forward = count(board, x, y, direction[0], direction[1], stone) - 1;
            int backward = count(board, x, y, -direction[0], -direction[1], stone) - 1;
            int openEnds = 0;
            if (isOpen(board, x + (forward + 1) * direction[0], y + (forward + 1) * direction[1])) openEnds++;
            if (isOpen(board, x - (backward + 1) * direction[0], y - (backward + 1) * direction[1])) openEnds++;
            score += lineScore(forward + backward + 1, openEnds);
        }
        board[x][y] = EMPTY;
        return score;
    }

    private int lineScore(int connected, int openEnds) {
        if (connected >= 5) return 10_000_000;
        if (connected == 4) return openEnds == 2 ? 500_000 : openEnds == 1 ? 80_000 : 0;
        if (connected == 3) return openEnds == 2 ? 25_000 : openEnds == 1 ? 3_000 : 0;
        if (connected == 2) return openEnds == 2 ? 1_200 : openEnds == 1 ? 150 : 0;
        return openEnds == 2 ? 30 : 5;
    }

    private int count(int[][] board, int x, int y, int dx, int dy, int stone) {
        int result = 0;
        while (inBoard(x, y) && board[x][y] == stone) {
            result++;
            x += dx;
            y += dy;
        }
        return result;
    }

    private boolean isOpen(int[][] board, int x, int y) {
        return inBoard(x, y) && board[x][y] == EMPTY;
    }

    private boolean inBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < GomokuRoom.SIZE && y < GomokuRoom.SIZE;
    }

    private int otherStone(int stone) {
        return stone == BLACK ? WHITE : BLACK;
    }

    private static final class Move {
        private final int x;
        private final int y;
        private int priority;

        private Move(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int[] position() {
            return new int[]{x, y};
        }
    }
}
