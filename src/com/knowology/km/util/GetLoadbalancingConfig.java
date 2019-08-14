package com.knowology.km.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.bll.CommonLibMetafieldmappingDAO;

public class GetLoadbalancingConfig {
	/**
	 * 定义全局 provinceToIP字典
	 */
	public static Map<String, Map<String, String >> provinceToUrl = new HashMap<String, Map<String,String>>();
	
	/**
	 * 定义全局 cityCodeToCityName字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();
	static {
		Result rs = CommonLibMetafieldmappingDAO.getConfigKey("各省负载均衡配置表_线下");
		if (rs != null && rs.getRowCount() > 0) {
			for (SortedMap row : rs.getRows()) {
				String key = (row.get("k") == null ? null : row.get("k")
						.toString());
				if (key != null) {
					String value = (row.get("name") == null ? "" : row.get(
							"name").toString());
					Map<String,String> m = new HashMap<String, String>();
					m.put("简要分析","http://"+key+"/NLPAppWS/AnalyzeEnterPort?wsdl");
					m.put("高级分析","http://"+key+"/NLPWebService/NLPCallerWS?wsdl");
					provinceToUrl.put(value, m);
				}
			}
		}
		
		rs = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String key = rs.getRows()[i].get("k") == null ? "" : rs
						.getRows()[i].get("k").toString();
				String value = rs.getRows()[i].get("name") == null ? "" : rs
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
			}
		}

	}
	
}
