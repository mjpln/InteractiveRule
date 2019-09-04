package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;

public class SceneElementDAO {

	/**
	 * 分页查询场景要素
	 * 
	 * @param scenariosId   场景ID
	 * @param sceneElementName 场景要素名称
	 * @param page          当前页码
	 * @param rows          分页条数
	 * @return
	 */
	public static Object listPagingSceneElements(String scenariosId, String sceneElementName, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibInteractiveSceneDAO.getElementNameCount(scenariosId, sceneElementName);
		// 判断数据源不为空且含有数据
		if (count > 0) {
			// 将获取的条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			Result rs = CommonLibInteractiveSceneDAO.getElementName(scenariosId, sceneElementName, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("scenarioselementid", rs.getRows()[i].get("scenarioselementid"));
					// 生成name对象
					obj.put("name", rs.getRows()[i].get("name"));
					// 生成weight对象
					obj.put("weight", rs.getRows()[i].get("weight"));
					// 生成wordclassid对象
					obj.put("wordclassid", rs.getRows()[i].get("wordclassid"));
					// 生成wordclass对象
					obj.put("wordclass", rs.getRows()[i].get("wordclass"));
					// 生成信息表列名对应对象
					obj.put("infotalbepath", rs.getRows()[i].get("infotalbepath"));
					// 生成是否共享对象
					obj.put("isshare", rs.getRows()[i].get("isshare"));
					// 生成城市对象
					obj.put("city", rs.getRows()[i].get("city"));
					obj.put("cityname", rs.getRows()[i].get("cityname"));

					// 生成交互模板对象
					obj.put("interpat", rs.getRows()[i].get("interpat"));
					// 生成选项填写方式对象
					obj.put("itemmode", rs.getRows()[i].get("itemmode"));
					// 生成场景ID对象
					obj.put("scenariosid", rs.getRows()[i].get("relationserviceid"));
					obj.put("container", rs.getRows()[i].get("container"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}
	
	/**
	 * 查询全部场景要素
	 * 
	 * @param scenariosId      场景ID
	 * @param sceneElementName 场景要素名称
	 * @return
	 */
	public static Object listAllElementName(String scenariosId, String sceneElementName) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		List<Object> lstpara = new ArrayList<Object>();
		// 定义分页查询满足条件的场景要素名称SQL语句
		sql.append("select q.*,(select wordclass from wordclass where wordclassid=q.wordclassid) wordclass from scenarioselement q where q.relationserviceid = ?  ");
		// 定义绑定参数集合
		lstpara = new ArrayList<Object>();
		// 绑定摘要id参数
		lstpara.add(scenariosId);
		// 判断问题要素名称是否为null，空
		if (sceneElementName != null && !"".equals(sceneElementName)) {
			// 加上问题要素名称查询条件
			sql.append(" and q.name = ? ");
			// 绑定问题要素名称参数
			lstpara.add(sceneElementName);
		}
		sql.append(" order by q.weight asc");
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql.toString(), lstpara.toArray());
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				obj.put("id", rs.getRows()[i].get("name"));
				obj.put("text", rs.getRows()[i].get("name"));
				// 生成id对象
				obj.put("scenarioselementid", rs.getRows()[i].get("scenarioselementid"));
				// 生成name对象
				obj.put("name", rs.getRows()[i].get("name"));
				// 生成weight对象
				obj.put("weight", rs.getRows()[i].get("weight"));
				// 生成wordclassid对象
				obj.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				// 生成wordclass对象
				obj.put("wordclass", rs.getRows()[i].get("wordclass"));
				// 生成信息表列名对应对象
				obj.put("infotalbepath", rs.getRows()[i].get("infotalbepath"));
				// 生成是否共享对象
				obj.put("isshare", rs.getRows()[i].get("isshare"));
				// 生成城市对象
				obj.put("city", rs.getRows()[i].get("city"));
				obj.put("cityname", rs.getRows()[i].get("cityname"));

				// 生成交互模板对象
				obj.put("interpat", rs.getRows()[i].get("interpat"));
				// 生成选项填写方式对象
				obj.put("itemmode", rs.getRows()[i].get("itemmode"));
				// 生成场景ID对象
				obj.put("scenariosid", rs.getRows()[i].get("relationserviceid"));
				obj.put("container", rs.getRows()[i].get("container"));
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		// 将jsonArr数组放入jsonObj的root对象中
		jsonObj.put("rows", jsonArr);
		jsonObj.put("total", jsonArr.size());
		return jsonObj;
	}

	/**
	 * 新增场景要素信息
	 * 
	 * @param scenariosId        场景ID
	 * @param scenariosName      场景名称
	 * @param sceneElementName   场景要素名称
	 * @param weight             优先级
	 * @param wordClass          词类名称
	 * @param city               地市编码
	 * @param cityName           地市名称
	 * @param infoTablePath      对应信息表
	 * @param itemMode           选项填写方式
	 * @param container          归属
	 * @param interactivePattern 交互模板
	 * @return
	 */
	public static Object insertScenariosElement(String scenariosId, String scenariosName, String sceneElementName, String weight, String wordClass, String city, String cityName, 
			String infoTablePath, String itemMode, String container, String interactivePattern) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		
		int c = CommonLibInteractiveSceneDAO.insertElementName(user, scenariosId,
				 scenariosName, infoTablePath, city, cityName, itemMode, sceneElementName, interactivePattern,
				 weight, wordClass, serviceType, container);
		// 判断事务处理的结果
		if (c == -2) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "对应词类库中不存在!");
		} else if (c == -3) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景要素名称已存在!");
		} else {
			if (c > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "添加成功!");
			} else {
				jsonObj.put("success", true);
				jsonObj.put("msg", "添加失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 编辑场景要素信息
	 * 
	 * @param scenariosId        场景ID
	 * @param scenariosName      场景名称
	 * @param sceneElementName   场景要素名称
	 * @param weight             优先级
	 * @param oldWeight          旧优先级
	 * @param wordClass          词类名称
	 * @param city               地市编码
	 * @param cityName           地市名称
	 * @param infoTablePath      对应信息表
	 * @param itemMode           选项填写方式
	 * @param container          归属
	 * @param interactivePattern 交互模板
	 * @param sceneElementId     场景元素ID
	 * @return
	 */
	public static Object updateScenariosElement(String scenariosId, String scenariosName, String sceneElementName, String weight, String oldWeight, String wordClass, String city, String cityName, 
			String infoTablePath, String itemMode, String container, String interactivePattern, String sceneElementId) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.updateElementName(user, scenariosId,
				scenariosName, sceneElementId, infoTablePath, city, cityName, 
				itemMode, sceneElementName, interactivePattern, weight, oldWeight, wordClass, serviceType,
				container);
		// 判断事务处理的结果
		if (c == -2) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "对应词类库中不存在!");
		} else if (c == -3) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景要素名称已存在!");
		} else {
			if (c > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "修改成功!");
			} else {
				jsonObj.put("success", true);
				jsonObj.put("msg", "修改失败!");
			}
		}
		return jsonObj;
	}
	
	/**
	 * 删除场景要素信息
	 * 
	 * @param scenariosId      场景ID
	 * @param scenariosName    场景名称
	 * @param sceneElementName 场景要素名称
	 * @param weight           优先级
	 * @param sceneElementId   场景元素ID
	 * @return
	 */
	public static Object deleteScenariosElement(String scenariosId, String scenariosName, String sceneElementName, String weight, String sceneElementId) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.deleteElementName(user, serviceType, sceneElementName, sceneElementId, weight, scenariosId, scenariosName);
		// 判断事务处理的结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将场景要素删除成功放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将场景要素删除失败放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
}
