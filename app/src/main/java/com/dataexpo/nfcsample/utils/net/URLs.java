package com.dataexpo.nfcsample.utils.net;

public class URLs {
    //private static final String DOMAIN = "http://saas.dataexpo.com.cn/";
    private static final String DOMAIN = "http://192.168.1.12:8080/";

    //获取人像
    public static final String getHead = DOMAIN + "pdaSys/api/data/findImg.do";

    //获取卡号在服务器的信息
    public static final String checkCard = DOMAIN + "pdaSys/api/data/findIC.do";

    //获取总决赛卡号在服务器的信息
    public static final String checkFinalCard = DOMAIN + "pdaSys/api/data/findFinalIC.do";

    //获取全部权限
    public static final String queryAccessGroup = DOMAIN + "pdaSys/api/data/findList.do";

    //添加记录
    public static final String putRecord = DOMAIN + "pdaSys/api/data/insertData.do";
}