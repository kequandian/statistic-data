package com.jfeat.am.module.statistics.services.crud.model;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import static com.jfeat.am.module.statistics.services.crud.impl.ExtendedStatisticsImpl.DEFAULT_EXCEED_TIME;

public class ReportCode {
    private Long userId;
    private Boolean checkUser = false;
    private Long exceedTime;
    private String filed;
    private String appid;

    ReportCode(){}

    public ReportCode(String code){
        ReportCode report = getByCode(code);
        this.userId = report.getUserId();
        this.checkUser = report.getCheckUser();
        this.exceedTime = report.getExceedTime();
        this.filed = report.getFiled();
        this.appid = report.getAppid();
    }

    public static final String USER_ID_CODE = "U1";
    public static final String FILED_CODE = "F2";
    public static final String EXCEED_TIME_CODE = "T3";
    public static final String APPID_CODE = "A4";

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public Boolean getCheckUser() {
        return checkUser;
    }

    public void setCheckUser(Boolean checkUser) {
        this.checkUser = checkUser;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExceedTime() {
        return exceedTime;
    }

    public void setExceedTime(Long exceedTime) {
        this.exceedTime = exceedTime;
    }

    public String getFiled() {
        return filed;
    }

    public void setFiled(String filed) {
        this.filed = filed;
    }

   public ReportCode getByCode(String code){
        ReportCode reportCode = new ReportCode();
        Integer userIndex = code.indexOf(USER_ID_CODE);
        Integer filedIndex = code.indexOf(FILED_CODE);
        Integer timeIndex = code.indexOf(EXCEED_TIME_CODE);
        Integer appidIndex = code.indexOf(APPID_CODE);
        if( userIndex != -1 ){
            String userString = code.substring(userIndex + USER_ID_CODE.length(), filedIndex);
            reportCode.setUserId(Long.parseLong(userString));
            reportCode.setCheckUser(true);
        }
        reportCode.setFiled(code.substring(filedIndex+FILED_CODE.length(),timeIndex));
        String timeString = code.substring(timeIndex + EXCEED_TIME_CODE.length(),appidIndex == -1 ? code.length():appidIndex);
        reportCode.setExceedTime(Long.parseLong(timeString) * DEFAULT_EXCEED_TIME);

        if(appidIndex != -1){
            String appidString = code.substring(appidIndex + APPID_CODE.length(), code.length());
            reportCode.setAppid(appidString);
        }

       return reportCode;
    }
}
