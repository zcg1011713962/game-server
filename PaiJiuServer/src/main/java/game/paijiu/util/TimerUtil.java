package game.paijiu.util;

public class TimerUtil {
    private static final long roundAnimMil = 3000L; // 开始动画
    private static final long grabBankerMil = 6000L; // 抢庄时间
    private static final long bankerAnimMil = 3000L; // 庄家动画播放
    private static final long BetMil = 10000L; // 投注时间
    private static final long dealMil = 1000L; // 发牌
    private static final long showCardMil = 5000L; // 亮牌
    private static final long settleMil = 3000L; // 结算
    private static final long nextRoundMil = 4000L; // 下一轮

    public static long getRoundAnimStartTime(long now){
        return now;
    }

    public static long getRoundAnimEndTime(long now){
        return now + roundAnimMil;
    }

    public static long getGrabBankerStartTime(long now) {
        return now;
    }

    public static long getGrabBankerEndTime(long now){
        return now + grabBankerMil;
    }

    public static long getBankerAnimStartTime(long now){
        return now;
    }

    public static long getBankerAnimEndTime(long now){
        return now + bankerAnimMil;
    }

    public static long getBetStartTime(long now){
        return now;
    }

    public static long getBetEndTime(long now){
        return now + BetMil;
    }

    public static long getDealStartTime(long now) {
        return now + dealMil;
    }

    public static long getShowCardStartTime(long now) {
        long dealStartTime = getDealStartTime(now);
        return dealStartTime + showCardMil;
    }

    public static long getSettleStartTime(long now) {
        long showCardStartTime = getShowCardStartTime(now);
        return showCardStartTime + settleMil;
    }

    public static long getNextRoundStartTime(long now) {
        return getSettleStartTime(now) + nextRoundMil;
    }
}
