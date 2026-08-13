package game.hall.entity.resp;

import java.util.ArrayList;
import java.util.List;

public class HallGameListResp {
    private HallBannerVO banner;
    private List<HallGameEntryVO> games = new ArrayList<>();

    public HallBannerVO getBanner() {
        return banner;
    }

    public void setBanner(HallBannerVO banner) {
        this.banner = banner;
    }

    public List<HallGameEntryVO> getGames() {
        return games;
    }

    public void setGames(List<HallGameEntryVO> games) {
        this.games = games;
    }
}
