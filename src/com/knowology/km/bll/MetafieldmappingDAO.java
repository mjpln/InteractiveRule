package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;

public class MetafieldmappingDAO {
	/**
	 * 更新配置名
	 * 
	 * @param metafieldmappingid参数配置名id
	 * @param oldvalue参数旧的配置名
	 * @param newvalue参数新的配置名
	 * @return 更新返回的json串
	 */
	public static Object update(String metafieldmappingid, String oldvalue,
			String newvalue) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		// 获取当前用户
		Object sre = GetSession.getSessionByKey("accessUser");
		// 判断当前用户是否有效
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		
		// 判断是否已存在相同配置名
		if (CommonLibMetafieldmappingDAO.Exists(newvalue)) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将重复信息放入jsonObj的msg对象中
			jsonObj.put("msg", "该配置名已存在!");
		} else {
			// 执行修改操作，返回事务处理的结果
			int c = UserOperResource.updateMetafieldMapping(user, metafieldmappingid, oldvalue, newvalue);
			// 判断事务处理结果
			if (c > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "修改失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 添加配置名
	 * 
	 * @param metafieldmapping参数配置名
	 * @return 新增返回的json串
	 */
	public static Object insert(String metafieldmapping) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		// 获取当前用户
		Object sre = GetSession.getSessionByKey("accessUser");
		// 判断当前用户是否有效
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		
		// 用换行符拆分用户输入的多条配置名,去除空字符串和空格等空白字符
		List<String> lstName = Arrays.asList(metafieldmapping.split("\n"));
		// 循环遍历配置名集合
		for (int i = 0; i < lstName.size(); i++) {
			// 判断是否已存在相同配置名
			if (CommonLibMetafieldmappingDAO.Exists(lstName.get(i))) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "第" + (i + 1) + "条配置名已存在!");
				return jsonObj;
			}
		}
		// 执行添加操作，返回添加事务处理的结果
		int c = UserOperResource.insertMetafieldMapping(user,lstName);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	/**
	 * 删除配置名
	 * 
	 * @param metafieldmappingid参数配置名id
	 * @param metafieldmapping参数配置名
	 * @return 删除返回的json串
	 */
	public static Object delete(String metafieldmappingid,
			String metafieldmapping) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
	
		// 获取当前用户
		Object sre = GetSession.getSessionByKey("accessUser");
		// 判断当前用户是否有效
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		int c = UserOperResource.deleteMetafieldMapping(user,metafieldmappingid,metafieldmapping);;
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
	
	/**
	 *@description 获得参数配置表具体值数据源
	 *@param name  配置参数名
	 *@param key   配置参数名对应key
	 *@return 
	 *@returnType Result 
	 */
	public static Result getConfigValue(String name ,String key){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =? and t.name =?  order by s.metafieldid ";
		lstpara = new ArrayList<Object>();
		//根据四层结构串获得brand
		lstpara.add(name);
		lstpara.add(key);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	   
	}
	
	/**
	 * 获取某一种配置下的所有映射，例如“地市编码配置” 、“资源表名到呈现名称映射配置”
	 * @param name
	 * @return “地市编码配置” 第一个是编码第二个是汉字名称   “资源表名到呈现名称映射配置”第一个是汉字名称 第二个是对应数据库表名
	 */
	public static Result getMapConfigValue(String name){
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="select t.name,s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =?";
		if("地市编码配置".equals(name)){
			sql=sql+" and s.name not like '%省' and s.name not like '%市'";//and t.name like '%00'  
		}
		lstpara = new ArrayList<Object>();
		//根据四层结构串获得brand
		lstpara.add(name);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
}
