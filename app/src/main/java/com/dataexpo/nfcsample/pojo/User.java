package com.dataexpo.nfcsample.pojo;

import java.io.Serializable;

/**
 * 返回IC卡
 * @author Administrator
 */
public class User implements Serializable{
    //卡号是否存在:1:存在；0/null：不存在
    private Integer isFort;
    // 状态（0:待审核,1:已审核,2 驳回.3 已打印；4已发放）
    public Integer euStatus;
    //姓名
    private String uiName;
    //团组/组别
    public String euDefine;
    //euId
    public Integer euId;
    //人像照片
    public String euImage;

    public Integer getIsFort() {
        return isFort;
    }
    public void setIsFort(Integer isFort) {
        this.isFort = isFort;
    }
    public Integer getEuStatus() {
        return euStatus;
    }
    public void setEuStatus(Integer euStatus) {
        this.euStatus = euStatus;
    }

    public String getUiName() {
        return uiName;
    }
    public void setUiName(String uiName) {
        this.uiName = uiName;
    }
    public String getEuDefine() {
        return euDefine;
    }
    public void setEuDefine(String euDefine) {
        this.euDefine = euDefine;
    }
    public Integer getEuId() {
        return euId;
    }
    public void setEuId(Integer euId) {
        this.euId = euId;
    }
    public String getEuImage() {
        return euImage;
    }
    public void setEuImage(String euImage) {
        this.euImage = euImage;
    }
}
