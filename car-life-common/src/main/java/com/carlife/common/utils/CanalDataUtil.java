package com.carlife.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
@AllArgsConstructor
public class CanalDataUtil extends LinkedHashMap<String, Object> {
    private static ObjectMapper om = new ObjectMapper();
    private CanalDataUtil(Map<String, Object> data) {
        super();
        super.putAll(data);
    }
    public static CanalDataUtil fromJsonString(String json){
        CanalDataUtil canalData = null;
        try {
            canalData = om.readValue(json, CanalDataUtil.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return canalData;
    }
    /**
     * 获取表名
     * @return
     */
    public String getTable() {
        return super.containsKey("table") ? (String) super.get("table") : "";
    }
    public String getType() {
        return super.containsKey("type") ? (String) super.get("type") : "";
    }
    /**
     * 获取本次操作的sql语句
     * @return
     */
    public String getDmlSql()  {
        String type = this.getType();
        switch (type) {
            case "INSERT":
                return getInsertSql();
            case "UPDATE":
                return getUpdateSql();
            default:
                System.err.println("不支持该DML操作type:"+ type);
        }
        return "";
    }

    public String getInsertSql()  {
        return "insert into " + getTable() + insColAndVal();
    }

    public String getUpdateSql() {
        return "update " + getTable() + " set " + updColAndVal();
    }

    /**
     * 获取本次操作的数据
     * @return
     */
    public Map<String, String> getData()  {
        String json = null;
        List list = null;
        Map<String, String> data = null;
        try {
            json = om.writeValueAsString(super.get("data"));
            list = om.readValue(json,List.class);
            data = om.readValue(om.writeValueAsString(list.get(0)), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public String updColAndVal() {
        StringBuilder updColAndVal = new StringBuilder();
        Map<String, String> dataMap = this.getData();
        for (String key : dataMap.keySet()) {
            if (Objects.equals(key, getPkNames())) {
                continue;
            }
            updColAndVal.append(key).append("='").append(dataMap.get(key)).append("',");
        }
        updColAndVal = new StringBuilder(StringUtils.substringBeforeLast(updColAndVal.toString(), ","));
        updColAndVal.append(" where ").append(getPkNames()).append("='").append(dataMap.get(getPkNames())).append("'");
        return updColAndVal.toString();
    }

    public String insColAndVal()  {
        Map<String, String> sqlType = this.getData();
        Set<String> set = sqlType.keySet();
        String columns = StringUtils.join(set.iterator(), ",");
        String values = StringUtils.join(sqlType.values(), "','");
        return " (" + columns + ") values ('" + values + "')";
    }
    /**
     * 主键名：获取主键名
     * @return
     */
    public String getPkNames() {
        String json = null;
        List list = null;
        try {
            json = om.writeValueAsString(super.get("pkNames"));
            list = om.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return String.valueOf(list.get(0));
    }

    /**
     * 获取表的字段类型
     * @return
     */
    public String getMysqlType() {
        return super.containsKey("mysqlType") ? ( super.get("mysqlType")).toString() : StringUtils.EMPTY;
    }
}