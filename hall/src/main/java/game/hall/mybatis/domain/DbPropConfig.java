package game.hall.mybatis.domain;

import java.util.Date;

/**
 * @TableName db_prop_config
 */
public class DbPropConfig {
    private Long id;

    private String propCode;

    private String propName;

    private Integer propType;

    private String icon;

    private Integer maxStack;

    private Integer isEnable;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPropCode() {
        return propCode;
    }

    public void setPropCode(String propCode) {
        this.propCode = propCode;
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public Integer getPropType() {
        return propType;
    }

    public void setPropType(Integer propType) {
        this.propType = propType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(Integer maxStack) {
        this.maxStack = maxStack;
    }

    public Integer getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Integer isEnable) {
        this.isEnable = isEnable;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}