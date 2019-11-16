package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibStandardkeyDAO;
import com.knowology.bll.CommonLibStandardvalueDAO;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;

public class MetafieldDao {

	/**
	 * 插入配置键
	 * 
	 * @param metafiledMapping 参数配置名
	 * @param standardKeys     参数配置键集合
	 * @return
	 */
	public static Object insertKey(String metafieldMapping, List<String> standardKeys) {
		JSONObject jsonObj = new JSONObject();
		// 获取当前用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		String metafieldMappingId = getMetafieldMappingId(metafieldMapping);
		// 判断配置键是否已存在
		for (String standardKey : standardKeys) {
			boolean isExist = CommonLibStandardkeyDAO.ExistsKey(metafieldMappingId, standardKey);
			if (isExist) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "配置键" + standardKey + "已存在!");
				return jsonObj;
			}
		}
		// 插入配置键
		int insertCount = CommonLibStandardkeyDAO.insert(user, metafieldMappingId, metafieldMapping, standardKeys);
		if (insertCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "插入成功!");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "插入失败!");
		return jsonObj;
	}

	/**
	 * 更新配置键
	 * 
	 * @param oldStandardKey 旧的配置键
	 * @param newStandardKey 新的配置键
	 * @param standardKeyId  配置键ID
	 * @return
	 */
	public static Object updateKey(String oldStandardKey, String newStandardKey, String standardKeyId) {
		JSONObject jsonObj = new JSONObject();
		// 获取当前用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 更新配置键值
		int updateCount = CommonLibStandardkeyDAO.update(user, oldStandardKey, newStandardKey, standardKeyId);
		if (updateCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新成功!");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "更新失败!");
		return jsonObj;
	}

	/**
	 * 删除配置键
	 * 
	 * @param metafiledMappingName 参数配置名
	 * @param standardKey          参数配置键值
	 * @return
	 */
	public static Object deleteKey(String metafiledMappingName, String standardKey) {
		JSONObject jsonObj = new JSONObject();
		// 获取当前用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		String standardKeyId = getStandardKeyId(metafiledMappingName, standardKey);
		int deleteCount = CommonLibStandardkeyDAO.delete(user, standardKeyId, metafiledMappingName, standardKey);
		if (deleteCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新成功!");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "更新失败!");
		return jsonObj;
	}

	/**
	 * 获取配置值
	 * 
	 * @param metafiledMapping 参数配置名
	 * @param standardKey      参数配置键
	 * @return
	 */
	public static Object getConfigValue(String metafiledMapping, String standardKey) {
		JSONObject jsonObj = new JSONObject();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue(metafiledMapping, standardKey);
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			jsonObj.put("success", true);
			jsonObj.put("total", rsConfig.getRowCount());
			jsonObj.put("rows", rsConfig.getRows());
			return jsonObj;
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 插入配置值
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param standardkey      参数配置键
	 * @param standardValues   参数配置值
	 * @return
	 */
	public static Object insertConfigValue(String metafieldMapping, String standardkey, List<String> standardValues) {
		JSONObject jsonObj = new JSONObject();
		// 获取当前用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		String metafieldMappingId = getMetafieldMappingId(metafieldMapping);
		String standardKeyId = getStandardKeyId(metafieldMapping, standardkey);
		int insertCount = CommonLibStandardvalueDAO.insert(user, metafieldMappingId, standardValues, standardKeyId,
				standardkey, metafieldMapping);
		if (insertCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "插入成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "插入失败");
		return jsonObj;
	}

	/**
	 * 更新配置值
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param oldStandardValue 旧的配置值
	 * @param newStandardValue 新的配置值
	 * @return
	 */
	public static Object updateConfigValue(String metafieldMapping, String standardkey, String oldStandardValue,
			String newStandardValue) {
		JSONObject jsonObj = new JSONObject();
		// 获取当前用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		String standarValuedId = getStandardValueId(metafieldMapping, standardkey, newStandardValue);
		int updateCount = CommonLibStandardvalueDAO.update(user, oldStandardValue, newStandardValue, standarValuedId,
				null);
		if (updateCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "更新失败");
		return jsonObj;
	}

	/**
	 * 删除配置值
	 * 
	 * @param standarValuedId  参数配置值ID
	 * @param standardValue    参数配置值
	 * @param standardKey      参数配置键
	 * @param metafieldMapping 参数配置名
	 * @return
	 */
	public static Object deleteConfigValue(String standarValuedId, String metafieldMapping, String standardValue,
			String standardKey) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int updateCount = CommonLibStandardvalueDAO.delete(user, standarValuedId, standardValue, standardKey,
				metafieldMapping);
		if (updateCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "删除失败");
		return jsonObj;
	}
	
	/**
	 * 删除配置值
	 * 
	 * @param standardValue    参数配置值
	 * @param standardKey      参数配置键
	 * @param metafieldMapping 参数配置名
	 * @return
	 */
	public static Object deleteConfigValue(String metafieldMapping, String standardKey, String standardValue) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String standardValuedId = getStandardValueId(metafieldMapping, standardKey, standardValue);
		int updateCount = CommonLibStandardvalueDAO.delete(user, standardValuedId , standardValue, standardKey,
				metafieldMapping);
		if (updateCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "删除失败");
		return jsonObj;
	}
	
	/**
	 * 删除配置值
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param standardValue    参数配置值
	 * @return
	 */
	public static Object deleteConfigValue(String metafieldMapping, String standardValue) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String standardValuedId = getStandardValueId(metafieldMapping, standardValue);
		int updateCount = CommonLibStandardvalueDAO.delete(user, standardValuedId);
		if (updateCount > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "删除失败");
		return jsonObj;
	}

	/**
	 * 获取配置名ID
	 * 
	 * @param metafieldMapping 参数配置名
	 * @return
	 */
	private static String getMetafieldMappingId(String metafieldMapping) {
		String sql = "select METAFIELDMAPPINGID from metafieldmapping a where a.name =? ";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, metafieldMapping);
			if (rs != null && rs.getRowCount() > 0) {
				return rs.getRows()[0].get("METAFIELDMAPPINGID")+"";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取配置键ID
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param standardKey      参数配置键
	 * @return
	 */
	public static String getStandardKeyId(String metafieldMapping, String standardKey) {
		String sql = "select t.METAFIELDID from metafieldmapping a, metafield t where a.name=? and t.metafieldmappingid=a.metafieldmappingid and t.name=? ";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, metafieldMapping, standardKey);
			if (rs != null && rs.getRowCount() > 0) {
				return rs.getRows()[0].get("METAFIELDID")+"";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取配置值ID
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param standardKey      参数配置键
	 * @param standardValue    参数配置值
	 * @return
	 */
	public static String getStandardValueId(String metafieldMapping, String standardKey, String standardValue) {
		String sql = "select s.metafieldid, s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =? and t.name =? and s.name=?";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, metafieldMapping, standardKey, standardValue);
			if (rs != null && rs.getRowCount() > 0) {
				return rs.getRows()[0].get("METAFIELDID").toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取配置值ID
	 * 
	 * @param metafieldMapping 参数配置名
	 * @param standardValue    参数配置值
	 * @return
	 */
	public static String getStandardValueId(String metafieldMapping, String standardValue) {
		String sql = "select s.metafieldid, s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =? and s.name=?";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, metafieldMapping, standardValue);
			if (rs != null && rs.getRowCount() > 0) {
				return rs.getRows()[0].get("METAFIELDID").toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

}
