package com.knowology.km.access;

import java.util.HashSet;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.bll.Role_RuleDAO;
import com.knowology.dal.Database;

public class Role_RuleManager {
	
	public static String getLogicByRoleID(int roleID,String RESOURCETYPE){
		Result rs = Role_RuleDAO.getLogicByRoleID(roleID,RESOURCETYPE);
		String logicString="";
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				logicString= rs.getRows()[i].get("logic") != null ? rs.getRows()[i].get("logic").toString() : "";
			}
		}
		return logicString;
	}
	
	public static HashSet<String> getCitySetByRoleId(int roleID,String RESOURCETYPE){
		HashSet<String> citySet=new HashSet<String>();
		String logicString=getLogicByRoleID( roleID,RESOURCETYPE);
		String cityString="";
		if (logicString.contains("地市")) {
			int begin=logicString.indexOf("地市")+3;
			int end=logicString.indexOf("&",begin);
			if(begin<end)
				cityString=logicString.substring(begin,end);
		}
		if (!"".equals(cityString)) {
			String temp[]=cityString.split(",");
			for (int i = 0; i < temp.length; i++) {
				citySet.add(temp[i].trim());
			}
		}
		return citySet;
	}
}
