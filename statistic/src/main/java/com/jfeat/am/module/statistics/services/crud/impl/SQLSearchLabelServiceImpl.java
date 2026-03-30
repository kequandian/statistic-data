package com.jfeat.am.module.statistics.services.crud.impl;

import com.jfeat.am.core.jwt.JWTKit;
import com.jfeat.am.module.statistics.services.crud.SQLSearchLabelService;
import com.jfeat.am.module.statistics.services.crud.model.JWTKitParameter;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLSearchLabelServiceImpl implements SQLSearchLabelService {


    Map<String,String> getDefaultMap(){
        Map<String,String> defaultMap = new HashMap<>();
        if(JWTKit.getOrgId()!=null) {
            defaultMap.put(JWTKitParameter.ORG_ID, JWTKit.getOrgId().toString());
        }
        if(JWTKit.getUserId()!=null) {
            defaultMap.put(JWTKitParameter.USER_ID, JWTKit.getUserId().toString());
        }
        // getBUserType() method not available in new JWTKit version
        // if(JWTKit.getBUserType()!=null) {
        //     defaultMap.put(JWTKitParameter.B_USER_TYPE, JWTKit.getBUserType());
        // }
        return defaultMap;
    }

    Map<String, String[]> setRequestDefauleValue(Map<String, String[]> requestMap){
        //尝试从JWTKIT中获取
        Map<String, String> defaultMap = getDefaultMap();
        for (String key:defaultMap.keySet()){
            String slist[] = new String[1];
            slist[0] = defaultMap.get(key);
            if (requestMap.get(key) == null) {
                requestMap.put(key, slist);
            }
        }
        return requestMap;
    }


    @Override
    public StringBuilder getSQL(Map<String, String[]> requestMap, StringBuilder sql) {

        //设置默认值
        requestMap = setRequestDefauleValue(requestMap);


        int ifIndex = sql.indexOf("@if");
        int endIndex = sql.indexOf("@end");
        /*if(ifIndex==-1|| endIndex ==-1){
            sql= deleteRange("@if","@end",sql);
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE.getCode(), "语法错误 @if 需要和 @end 一起出现");
        }*/

        while(ifIndex>-1 && endIndex >-1){
            sql=ifSQL(requestMap,sql);
            ifIndex = sql.indexOf("@if");
        }

    // 此处判断 后续标签

        return sql;
    }

    //@if标签
    @Override
    public StringBuilder ifSQL(Map<String, String[]> requestMap, StringBuilder sql) {


        String ifString=range("[","]",sql);
        if(conditions(requestMap,new StringBuilder(ifString)).equals("true")){
         // 替换if中的sql
            int index = sql.indexOf("#{");
            while (index!=-1) {
                String pro = range("#{", "}", sql);
                //判断是否为区间形式
                StringBuilder proBuilder=new StringBuilder(pro);
                int left = proBuilder.indexOf("$left");
                int right= proBuilder.indexOf("$right");
                if(left!=-1){
                    pro=proBuilder.delete(left,proBuilder.length()).toString();
                }else if(right != -1){
                    pro=proBuilder.delete(right,proBuilder.length()).toString();
                }
                if (requestMap.get(pro) != null) {
                    if(right != -1){
                        pro = requestMap.get(pro)[1];
                    }else{
                        pro = requestMap.get(pro)[0];
                    }
                  } else {
                    pro = "";
                  }
                pro="'"+pro+"'";
                sql.insert(index, pro);
                index= sql.indexOf("#{");
            }

             index = sql.indexOf("@if");
            sql.delete(index,index+3);
            index = sql.indexOf("@end");
            sql.delete(index,index+4);
        }else {
           sql= deleteRange("@if","@end",sql);
        }


        return sql;
    }

    //if 处理内部判断
    public String conditions(Map<String, String[]> requestMap, StringBuilder sql){
        String resultSQL="";
        StringBuilder stringBuilder=new StringBuilder(sql);
        String[] rule=new String[]{"("};
        String[] rule2=new String[]{"&&","||"};
        int leftIndex = getLeftIndex(rule, stringBuilder);
        int leftIndex2 = getLeftIndex(rule2, stringBuilder);
        if(leftIndex==-1&&leftIndex2==-1){
            resultSQL=EndlessConditions(requestMap,stringBuilder);
        }
        while (leftIndex!=-1||leftIndex2!=-1){
           if(leftIndex!=-1&&"(".equals(stringBuilder.substring(leftIndex,leftIndex+1))){
               String range = range("(", ")", stringBuilder);
               resultSQL=conditions(requestMap,new StringBuilder(range));
               resultSQL=stringBuilder.insert(leftIndex,resultSQL).toString();
           }else if(leftIndex2!=-1 && "&".equals(stringBuilder.substring(leftIndex2,leftIndex2+1))){
               String right = stringBuilder.substring(leftIndex2+2,stringBuilder.length());
               String left = stringBuilder.substring(0,leftIndex2);
               Boolean rightBoolean=Boolean.parseBoolean(conditions(requestMap,new StringBuilder(right)));
               Boolean leftBoolean=Boolean.parseBoolean(conditions(requestMap,new StringBuilder(left)));
               stringBuilder.delete(leftIndex2+2,stringBuilder.length());
               if(leftBoolean && rightBoolean)
               {resultSQL="true";}
               else {resultSQL="false";}
               stringBuilder=new StringBuilder(resultSQL);
           }else if(leftIndex2!=-1 &&"|".equals(stringBuilder.substring(leftIndex2,leftIndex2+1))){
               String right = stringBuilder.substring(leftIndex2+2,stringBuilder.length());
               String left = stringBuilder.substring(0,leftIndex2);
               Boolean rightBoolean=Boolean.parseBoolean(conditions(requestMap,new StringBuilder(right)));
               Boolean leftBoolean=Boolean.parseBoolean(conditions(requestMap,new StringBuilder(left)));
               stringBuilder.delete(leftIndex2+2,stringBuilder.length());
               if(leftBoolean || rightBoolean)
               {resultSQL="true";}
               else {resultSQL="false";}
               stringBuilder=new StringBuilder(resultSQL);
           }
             leftIndex = getLeftIndex(rule, stringBuilder);
             leftIndex2 = getLeftIndex(rule2, stringBuilder);
        }
            return resultSQL;
    }



    public String EndlessConditions(Map<String, String[]> requestMap, StringBuilder sql){
        String [] firstRule=new String[]{"("};
        String [] secondRule= new String[]{"==","<",">","!=","+","-","*","/"};
        int firstIndex=getLeftIndex(firstRule,sql);
        while (firstIndex!=-1){
            String  value = range("(",")",sql);
            String  resultSQL=EndlessConditions(requestMap,new StringBuilder(value));
            sql.insert(firstIndex,resultSQL);
            firstIndex=getLeftIndex(firstRule,new StringBuilder(resultSQL));
        }


        int secondIndex=getLeftIndex(secondRule,sql);
        String resultSQL=sql.toString();
        while (secondIndex!=-1) {
            String  leftValue = sql.substring(0, secondIndex).trim();
            String  rightValue;
            int stringLength=0;
            String string=sql.substring(secondIndex,secondIndex+1);
            if("=".equals(string)||"!".equals(string)){
                stringLength=2;
            }else {
                stringLength=1;
            }
            rightValue=sql.substring(secondIndex + stringLength, sql.length()).trim();

                leftValue=EndlessConditions(requestMap,new StringBuilder(leftValue));
                rightValue=EndlessConditions(requestMap,new StringBuilder(rightValue));
                leftValue = getValue(leftValue, requestMap);
                rightValue = getValue(rightValue, requestMap);
                switch (string) {
                    case "=":
                        if (leftValue.equals(rightValue)) {
                            resultSQL= "true";
                        } else resultSQL= "false";
                        break;
                    case "<":
                        if (Double.parseDouble(leftValue) < Double.parseDouble(rightValue)) {
                            resultSQL="true";
                        } else resultSQL="false";
                        break;
                    case ">":
                        if (Double.parseDouble(leftValue) > Double.parseDouble(rightValue)) {
                            resultSQL= "true";
                        } else resultSQL= "false";
                        break;
                    case "!":
                        if (!leftValue.equals(rightValue)) {
                            resultSQL= "true";
                        } else resultSQL= "false";
                        break;
                    case "+":
                        resultSQL= BigDecimal.valueOf(Double.parseDouble(leftValue) + Double.parseDouble(rightValue)) + "";
                    break;
                    case "-":
                        resultSQL= BigDecimal.valueOf(Double.parseDouble(leftValue) - Double.parseDouble(rightValue)) + "";
                    break;
                    case "*":
                        resultSQL= BigDecimal.valueOf(Double.parseDouble(leftValue) * Double.parseDouble(rightValue))+ "";
                    break;
                    case "/":
                        resultSQL= BigDecimal.valueOf(Double.parseDouble(leftValue) / Double.parseDouble(rightValue)) + "";
                    break;
                    default:
                        break;

                }
           sql=new StringBuilder(resultSQL);
            secondIndex=getLeftIndex(secondRule,sql);
        }

        return resultSQL;
    }

    @Override
    public String range(String left,String right,StringBuilder stringBuilder){
        List<Integer> leftIndexBox= getIndex(left, stringBuilder);
        List<Integer> rightIndexBox= getIndex(right, stringBuilder);
        if(leftIndexBox.size()==0 || rightIndexBox.size()==0){
            return "";
        }
        int endLeftPoint=leftIndexBox.get(0);
        int endRightPoint=rightIndexBox.get(0);
        int count=-1;
        //匹配左右字符串
            for (Integer leftPoint:leftIndexBox){
                if(endRightPoint>leftPoint){
                    count++;
                }
            }
        if(count<0){
            return "";
        }
        endRightPoint=rightIndexBox.get(count);

        if(endLeftPoint+1>=endRightPoint){
            return "";
        }
        String substring = stringBuilder.substring(endLeftPoint+1, endRightPoint);
         stringBuilder.delete(endLeftPoint-left.length()+1,endRightPoint+1);
        return substring;
    }

    @Override
    public StringBuilder deleteRange(String left,String right,StringBuilder stringBuilder){
        int leftIndex = stringBuilder.indexOf(left);
        int rigthIndex = stringBuilder.indexOf(right);
        stringBuilder.delete(leftIndex,rigthIndex+right.length());
        return stringBuilder;
    }
//
    public Boolean ifExists (String s,StringBuilder body){
        int index = body.indexOf(s);
        if(index>-1){
            return true;
        }else
            return false;
    }

//得到左侧index
    public int getLeftIndex(String[] s,StringBuilder body){
        int i=-1;
        int length=body.length();
        int max=length;
        for(String string:s){
            int index = body.indexOf(string);
            if(index!=-1&&index<=max){
                max=index;
                i=max;
            }
        }
        return i;
    }

    //判断数字
    private static boolean isNumString(String str) {
        if (str == null || str.length() == 0  )
            return false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (!(ch >= '0' && ch <= '9')) {
                return false;
            }
        }
        return true;
    }

    //---表达式的值
    public String getValue(String value,Map<String,String[]> requestMap){
        if(value==null||"".equals(value)){
            return "null";
        }
        if(        !"null".equals(value)
                && !"false".equals(value)
                && !"true".equals(value)
                &&!isNumString(value)){
            //判断是否为区间形式
            StringBuilder valueBuilder=new StringBuilder(value);
            int left = valueBuilder.indexOf("$left");
            int right= valueBuilder.indexOf("$right");
            if(left!=-1){
                value=valueBuilder.delete(left,valueBuilder.length()).toString();
            }else if(right != -1){
                value=valueBuilder.delete(right,valueBuilder.length()).toString();
            }
            if(requestMap.get(value)!=null){
                if(right != -1){
                    value=requestMap.get(value)[1];
                }else {
                    value=requestMap.get(value)[0];
                }
            }else
               {value="null";}
        }
        return value;
    }

    public List<Integer> getIndex(String context,StringBuilder body){
        List<Integer> index=new ArrayList();
        StringBuilder stringBuilder=new StringBuilder(body.toString());
        int i = stringBuilder.indexOf(context);
        int sum=0;
        while (i!=-1){
            //本次位置 + 之前位置
            sum=+(sum)+i+context.length()-1;
            index.add(sum);
            if(i+context.length()<stringBuilder.length()){
                stringBuilder=new StringBuilder(stringBuilder.substring(i+context.length(),stringBuilder.length()));
            }
            stringBuilder.delete(0,i+1);
            i=stringBuilder.indexOf(context);

        }
        return index;
    }




}
