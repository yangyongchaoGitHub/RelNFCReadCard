package com.dataexpo.nfcsample.pojo;

import java.io.Serializable;

/**
 * 上传记录实体
 */
public class Record implements Serializable {
    private int admUserId;
    private String admCode;
    private String admAdd;
    //1成功， 2失败
    private int admState;

    public int getAdmUserId() {
        return admUserId;
    }

    public void setAdmUserId(int admUserId) {
        this.admUserId = admUserId;
    }

    public String getAdmCode() {
        return admCode;
    }

    public void setAdmCode(String admCode) {
        this.admCode = admCode;
    }

    public String getAdmAdd() {
        return admAdd;
    }

    public void setAdmAdd(String admAdd) {
        this.admAdd = admAdd;
    }

    public int getAdmState() {
        return admState;
    }

    public void setAdmState(int admState) {
        this.admState = admState;
    }
}
