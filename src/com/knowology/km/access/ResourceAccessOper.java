package com.knowology.km.access;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.km.dal.Database;
/**
 * 资源权限操作
 * @author
 */
public class ResourceAccessOper {

	/**
	 * 根据资源id查找资源属性
	 * @param resName 资源类型
	 * @param resSelfID 资源id
	 * @return
	 */
	public static Map<String,String> searchAttrsByResID(String resName, String resSelfID) {
		Result rs;
		// 存放返回值
		Map<String,String> result = new HashMap<String,String>();
		// 存放列名和列的序号
		Map<String,String> colNameMap = new HashMap<String,String>();
		// 查询列名和列值的sql
		String sql = "select * from ResourceAttrName2FieldColNum where resourceType like '%" + resName +"%'";
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 属性类型
					String attrName = rs.getRows()[i].get("name") != null ? rs.getRows()[i].get("name").toString() : "";
					// 数据类型
					String dataType = rs.getRows()[i].get("dataType") != null ? rs.getRows()[i].get("dataType").toString() : "";
					// 对应的列值
					String columnNum = rs.getRows()[i].get("columnNum") != null ? rs.getRows()[i].get("columnNum").toString() : "";
					colNameMap.put(attrName, columnNum+"_"+dataType);
					result.put(attrName, "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		sql = "select * from ResourceAcessManager where resourceID like '" + resName + "_" + resSelfID;
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				// 遍历map，找到查询的列
				for (Entry<String,String> entry : result.entrySet()) {
					String value = rs.getRows()[0].get(colNameMap.get(entry.getKey())) != null ? rs.getRows()[0].get(colNameMap.get(entry.getKey())).toString() : "";
					// 设置对应的值
					entry.setValue(value);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 通过属性条件查询资源id
	 * @param map 属性条件<属性种类，条件表达式>
	 * @param resName 资源类型
	 * @return
	 */
	public static List<String> searchResIDByAttrs(Map<String,String> map, String resName) {
		// 判断包含操作的属性列和属性值
		Map<String,String> attrMap = new HashMap<String, String>();
		// 计算符
		String calStr = "><!=>=<=<>";
		// 存放资源id
		List<String> result = new ArrayList<String>();
		Result rs;
		// 存放列名和列的序号
		Map<String,String> colNameMap = new HashMap<String,String>();
		// 根据资源类型查找列值
		String sql = "select * from ResourceAttrName2FieldColNum where resourceType like '%" + resName + "%'";
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 属性类型
					String attrName = rs.getRows()[i].get("name") != null ? rs.getRows()[i].get("name").toString() : "";
					// 数据类型
					String dataType = rs.getRows()[i].get("dataType") != null ? rs.getRows()[i].get("dataType").toString() : "";
					// 对应的列值
					String columnNum = rs.getRows()[i].get("columnNum") != null ? rs.getRows()[i].get("columnNum").toString() : "";
					colNameMap.put(attrName, columnNum+"_"+dataType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		sql = "select * from ResourceAcessManager where resourceID like '%" + resName +"%' ";
		// 如果查询参数不为空
		if (map.isEmpty()) {
			sql += " and ";
		}
		// 遍历资源属性参数
		for (Entry<String,String> entry : map.entrySet()) {
			// 属性对应的列名
			String colName = colNameMap.get(entry.getKey());
			// 参数值
			String param = entry.getValue();
			// 获得字段类型
			String dataType = colName.split("_")[1];
			if (dataType.equals("number")) {// number 类型
				// 给sql添加where条件字段
				sql = sql + " and Attr" + colName;
				String firstChar = param.substring(0,1);
				// 参数中有计算式
				if (calStr.contains(firstChar)) {
					sql = sql + param;
				} else {
					sql = sql + "=" + param;
				}
			} else if(dataType.equals("time")) {// 时间类型
				// 给sql添加where条件字段
				sql = sql + " and Attr" + colName;
				String firstChar = param.substring(0,1);
				if(calStr.contains(firstChar)) {// 参数中有逻辑表达式
					String secondChar = param.substring(1,2);
					if (calStr.contains(secondChar)) {// 如果是类似<>,>=这种类型的运算符
						sql = sql + firstChar + secondChar + "to_date('"+param.substring(2,param.length())+"','YYYY-MM-dd HH24:mi:ss')";
					} else {// +,-类似的操作符
						sql = sql + firstChar + "to_date('"+param.substring(1,param.length())+"','YYYY-MM-dd HH24:mi:ss')";
					}
				} else {
					sql = sql + "=to_date('YYYY-MM-dd HH24:mi:ss')";
				}
			} else if(dataType.equals("varchar")) {// 字符类型
				if (entry.getKey().equals("地市")) {// 属性集合，别的资源只要包含该属性值中的某一个就返回
					//sql = sql + " like '%," + param + "%'";
					attrMap.put("Attr" + colName, param);
				} else {
					// 给sql添加where条件字段
					sql = sql + " and Attr" + colName;
					sql = sql + "=" + param;
				}
			}
			
			sql += " and ";
		}
		
		// 裁剪sql
		sql = sql.substring(0, sql.lastIndexOf("and"));
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				// 遍历map，找到查询的列
				for (int i = 0; i < rs.getRowCount(); i++) {
					String resourceID = rs.getRows()[i].get("resourceID") != null ? rs.getRows()[i].get("resourceID").toString() : "";
					// 判断属性是否包含
					if (!attrMap.isEmpty()) {
						for (Entry<String,String> entry : attrMap.entrySet()) {// 遍历属性
							String attrValue = rs.getRows()[i].get(entry.getKey()) != null ? rs.getRows()[i].get(entry.getKey()).toString() : "";
							if(isContain(attrValue, entry.getValue())) {
								result.add(resourceID.split("_")[1]);
							}
						}
					} else {
						result.add(resourceID.split("_")[1]);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
		return result;
	}
	
	/**
	 * 判断库保存的属性是否有值包含在参数传入的属性中
	 * @param attrValueByCheck 数据库中查询出的属性
	 * @param attrValueByParam 参数传入的属性
	 * @return
	 */
	public static boolean isContain(String attrValueByCheck, String attrValueByParam) {
		// 数据库中查询出的属性
		List<String> checkList = new ArrayList<String>(Arrays.asList(attrValueByCheck.split(",")));
		// 参数传入的属性
		List<String> paramList = new ArrayList<String>(Arrays.asList(attrValueByParam.split(",")));
		if (checkList.removeAll(paramList)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 判断资源是否有对应属性条件的权限
	 * @param resName 资源类型
	 * @param resSelfID 资源id
	 * @param userOwnAccessMap 属性条件集合
	 * @return
	 */
	public static boolean isAccess(String resName, String resSelfID,  Map<String,String> userOwnAccessMap) {
		List<String> list = searchResIDByAttrs(userOwnAccessMap, resName);
		if(list.contains(resSelfID))
			return true;
		return false;
	}
	
	
	/**
	 * 根据父业务找到相关的子业务
	 * @param array
	 */
	public static List<String> getChildService(Object[] array) {
		// 返回值
		List<String> list = new ArrayList<String>();
		if (array.length == 0) {// 当没有参数时
			return list;
		}
		String sql = "select serviceID from service start WITH serviceid in ("+org.apache.commons.lang.StringUtils.join(array,",")+")　connect BY nocycle prior serviceid = parentid";
		try {
			Result rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String serviceID = rs.getRows()[i].get("serviceID") != null ? rs.getRows()[i].get("serviceID").toString() : "";
					list.add(serviceID);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 根据资源名称，找到资源id
	 * @param array 资源名数组
	 * @param resourceType 资源类型
	 * @return
	 */
	public static List<String> getResourceIDByName(Object[] array, String resourceType) {
		// 返回值
		List<String> list = new ArrayList<String>();
		if (array.length == 0) {// 如果参数为空
			return list;
		}
		String sql = "";
		if (resourceType.equals("service")) {// 业务
			sql = "select serviceID as resourceID from service where service in ("+org.apache.commons.lang.StringUtils.join(array,",")+")";
		} else {// 摘要
			sql = "select kbdataID as resourceID from kbdata where abstract in ("+org.apache.commons.lang.StringUtils.join(array,",")+")";
		}
		try {
			Result rs = Database.executeQuery(sql, array);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String resourceID = rs.getRows()[i].get("resourceID") != null ? rs.getRows()[i].get("resourceID").toString() : "";
					list.add(resourceID);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 新增资源属性
	 * @param resName 资源类型
	 * @param resSelfID 资源id
	 * @param resMap 资源属性
	 * @return
	 */
	public static int addResource(String resName, String resSelfID, Map<String,String> resMap) {
		// 返回值
		int count = 0;
		// 获得插入的列名及类型
		String sql = "select * from ResourceAttrName2FieldColNum where resourceType like '%" + resName + "%'";
		// 属性类型
		Object[] params = resMap.keySet().toArray();
		String attributes = "(";
		for (Object param : params) {
			attributes += param+",";
		}
		attributes = attributes.substring(0, attributes.lastIndexOf(",")) + ")";
		sql += " and " + attributes;
		// 存放列名和列的序号
		Map<String,String> colNameMap = new HashMap<String,String>();
		Result rs;
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 属性类型
					String attrName = rs.getRows()[i].get("name") != null ? rs.getRows()[i].get("name").toString() : "";
					// 数据类型
					String dataType = rs.getRows()[i].get("dataType") != null ? rs.getRows()[i].get("dataType").toString() : "";
					// 对应的列值
					String columnNum = rs.getRows()[i].get("columnNum") != null ? rs.getRows()[i].get("columnNum").toString() : "";
					colNameMap.put(attrName, columnNum+"_"+dataType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// insert语句参数
		List<Object> insertParams = new ArrayList<Object>();
		// 拼接存值sql
		String firstInsert = "insert into ResourceAcessManager(id,resourceID";
		String lastInsert = " values (ResourceAcessManager.nextval,?";
		insertParams.add(resName + "_" + resSelfID);
		for (Entry<String,String> entry : colNameMap.entrySet()) {
			firstInsert += entry.getValue()+",";
			// 构造出入的参数
			if (entry.getValue().split("_")[1].equals("time")) {// 时间类型
				insertParams.add("'"+resMap.get(entry.getKey())+"','YYYY-MM-dd HH24:mi:ss'");
				lastInsert += "to_date(?),";
			} else {
				insertParams.add(resMap.get(entry.getKey()));
				lastInsert += "?,";
			}
		}
		firstInsert = firstInsert.substring(0,firstInsert.lastIndexOf(",")) + ")";
		lastInsert = lastInsert.substring(0,lastInsert.lastIndexOf(",")) + ")";
		sql = firstInsert + lastInsert;
		try {
			count = Database.executeNonQuery(sql,insertParams.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * 修改资源属性
	 * @param resName 资源类型
	 * @param resSelfID 资源id
	 * @param resMap 属性参数集合
	 * @return
	 */
	public static int updateResource(String resName, String resSelfID, Map<String,String> resMap) {
		// 返回值
		int count = 0;
		// 获得插入的列名及类型
		String sql = "select * from ResourceAttrName2FieldColNum where resourceType like '%" + resName + "%'";
		// 属性类型
		Object[] params = resMap.keySet().toArray();
		String attributes = "(";
		for (Object param : params) {
			attributes += param+",";
		}
		attributes = attributes.substring(0, attributes.lastIndexOf(",")) + ")";
		sql += " and " + attributes;
		// 存放列名和列的序号
		Map<String,String> colNameMap = new HashMap<String,String>();
		Result rs;
		try {
			rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 属性类型
					String attrName = rs.getRows()[i].get("name") != null ? rs.getRows()[i].get("name").toString() : "";
					// 数据类型
					String dataType = rs.getRows()[i].get("dataType") != null ? rs.getRows()[i].get("dataType").toString() : "";
					// 对应的列值
					String columnNum = rs.getRows()[i].get("columnNum") != null ? rs.getRows()[i].get("columnNum").toString() : "";
					colNameMap.put(attrName, columnNum+"_"+dataType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// insert语句参数
		List<Object> updateParams = new ArrayList<Object>();
		
		// 更新操作sql
		sql = "update ResourceAcessManager set ";
		
		// 组合sql
		for (Entry<String,String> entry : colNameMap.entrySet()) {
			// 构造出入的参数
			if (entry.getValue().split("_")[1].equals("time")) {// 时间类型
				updateParams.add("'"+resMap.get(entry.getKey())+"','YYYY-MM-dd HH24:mi:ss'");
				sql += entry.getValue()+"=to_date(?),";
			} else {
				updateParams.add(resMap.get(entry.getKey()));
				sql += entry.getValue()+"=?,";
			}
		}
		
		sql = sql.substring(0,sql.lastIndexOf(","));
		sql = sql + " where resourceID=?";
		// 添加where参数
		updateParams.add(resName+"_"+resSelfID);
		try {
			count = Database.executeNonQuery(sql,updateParams.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	/**
	 * 删除资源对应的属性
	 * @param belongCom 所属机构
	 * @param resName 资源类型
	 * @param resSelfID 资源id
	 * @return
	 */
	public static int deleteResource(String resName, String resSelfID) {
		// 返回值
		int count = 0;
		// 操作sql语句
		String sql = "delete from ResourceAcessManager where resourceID=?";
		try {
			count = Database.executeNonQuery(sql,new Object[]{resName+"_"+resSelfID});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
}
