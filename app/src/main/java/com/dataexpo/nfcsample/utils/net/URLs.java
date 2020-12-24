package com.dataexpo.nfcsample.utils.net;

public class URLs {
    private static final String DOMAIN = "http://saas.dataexpo.com.cn/";
    //private static final String DOMAIN = "http://192.168.1.16:8090/";

    //获取人像
    public static final String getHead = DOMAIN + "GbhSystem/api/data/findImg.do";

    //添加一次使用次数
    public static final String addCount = DOMAIN + "sysLogin/getUser";

    //获取卡号在服务器的信息
    public static final String checkCard = DOMAIN + "GbhSystem/api/data/findIC.do";
}