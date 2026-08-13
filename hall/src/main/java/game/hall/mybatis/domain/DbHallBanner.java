package game.hall.mybatis.domain;

import java.util.Date;

/**
 * @TableName db_hall_banner
 */
public class DbHallBanner {
    private Long id;

    private Integer gameId;

    private String title;

    private String subtitle;

    private String tagText;

    private String bgAsset;

    private String buttonAsset;

    private Integer onlineCount;

    private Integer enabled;

    private Integer sort;

    private Date createdAt;

    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTagText() {
        return tagText;
    }

    public void setTagText(String tagText) {
        this.tagText = tagText;
    }

    public String getBgAsset() {
        return bgAsset;
    }

    public void setBgAsset(String bgAsset) {
        this.bgAsset = bgAsset;
    }

    public String getButtonAsset() {
        return buttonAsset;
    }

    public void setButtonAsset(String buttonAsset) {
        this.buttonAsset = buttonAsset;
    }

    public Integer getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
        this.onlineCount = onlineCount;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}