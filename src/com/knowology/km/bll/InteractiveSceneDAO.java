package com.knowology.km.bll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibKbdataAttrDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.bll.CommonLibServiceAttrDao;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.bll.CommonLibWordclassDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.access.UserManager;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.action.BaseAction;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.GlobalValues;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.NumberUtil;
import com.knowology.km.util.WordToHtml;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;
import com.str.NewEquals;

/**
 * @author ll
 * 
 */
public class InteractiveSceneDAO {
	public static Logger logger = Logger.getLogger("train");
	public static String docpath = System.getProperty("os.name").toLowerCase()
			.startsWith("win") ? com.knowology.km.dal.Database
			.getJDBCValues("winDir")
			+ "scenariosdoc" : com.knowology.km.dal.Database
			.getJDBCValues("linDir")
			+ "/" + "scenariosdoc";

	/**
	 * 定义全局 city字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();
	
	/**
	 * 定义全局 cityNameToCityCode 字典
	 */
	public static Map<String, String> cityNameToCityCode = new HashMap<String, String>();
	
	
	
	/**
	 *创建字典
	 */

	static {
		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");

		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
				cityNameToCityCode.put(key, value);
			}
		}
		
	}
	/**
	 * 存放规则优先级及规则ID字典
	 * */
	public static Map<String, String> weightToRuleid;

	/**
	 * 分页查询满足条件的问题要素信息
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @param serviceid
	 *            业务ID
	 * @param abstractid
	 *            摘要ID
	 * @param userquery
	 *            用户问题
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object selectSceneRelation(String scenariosid,
			String serviceid, String abstractid, String userquery, int page,
			int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		// user
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		
		int count = CommonLibInteractiveSceneDAO.getSceneRelationCount(
				scenariosid, serviceid, abstractid, userquery, user);
		// 判断数据源不为空且含有数据
		if (count > 0) {
			// 将获取的条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			Result rs = CommonLibInteractiveSceneDAO.getgetSceneRelation(
					scenariosid, serviceid, abstractid, userquery, page, rows,user);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义一个接送对象
					JSONObject obj = new JSONObject();
					obj.put("scenerelationid", rs.getRows()[i].get("scenarios2kbdataid"));
					obj.put("name", rs.getRows()[i].get("name"));
					obj.put("service", rs.getRows()[i].get("service"));
					obj.put("serviceid", rs.getRows()[i].get("serviceid"));
					obj.put("abstract", rs.getRows()[i].get("query"));
//					obj.put("abstract", rs.getRows()[i].get("abstract"));
					obj.put("abstractid", rs.getRows()[i].get("abstractid"));
					obj.put("userquery", rs.getRows()[i].get("userquery"));
//					obj.put("abstract", rs.getRows()[i].get("abstract"));
					String citycodes = rs.getRows()[i].get("city") == null ? "全国" : rs.getRows()[i].get("city").toString();
					obj.put("citycodes", citycodes);
					
					String[] citycode = citycodes.split(",");
					String citynames = "";
					for (int j = 0;j < citycode.length;j++){
						citynames = citynames + cityCodeToCityName.get(citycode[j]) + ",";
					}
					if (citynames.contains(",")){
						citynames = citynames.substring(0, citynames.lastIndexOf(","));
					}
//					citycodes = citycodes.substring(0, 2);
					if (!"".equals(citynames) && !"全国".equals(citynames)){
						obj.put("city", citynames);
					}else {
						obj.put("city", "全国");
					}
					
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
	 * 分页查询满足条件的问题要素信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param name参数问题要素名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object selectElementName(String scenariosid, String name,
			int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibInteractiveSceneDAO.getElementNameCount(
				scenariosid, name);
		// 判断数据源不为空且含有数据
		if (count > 0) {
			// 将获取的条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			Result rs = CommonLibInteractiveSceneDAO.getElementName(
					scenariosid, name, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("scenarioselementid", rs.getRows()[i]
							.get("scenarioselementid"));
					// 生成name对象
					obj.put("name", rs.getRows()[i].get("name"));
					// 生成weight对象
					obj.put("weight", rs.getRows()[i].get("weight"));
					// 生成wordclassid对象
					obj.put("wordclassid", rs.getRows()[i].get("wordclassid"));
					// 生成wordclass对象
					obj.put("wordclass", rs.getRows()[i].get("wordclass"));
					// 生成信息表列名对应对象
					obj.put("infotalbepath", rs.getRows()[i]
							.get("infotalbepath"));
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
	 * 查询当前问题元素下的优先级
	 * 
	 * @param kbdataid参数摘要id
	 * @param oldWeight
	 *            旧原始优先级
	 * @param kbcontentid参数kbcontentid
	 * @return 优先级的json串
	 */
	public static Object getWeight(String scenariosid, String oldWeight) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义问题要素的所有的优先级的集合并赋值
		List<String> weightLst = new ArrayList<String>();
		// 循环遍历1-GetConfigValue.NUMBER_OF_MAX_SCENE
		for (int i = 0; i < GetConfigValue.NUMBER_OF_MAX_SCENE; i++) {
			// 给问题要素的所有的优先级赋值
			weightLst.add(String.valueOf(i + 1));
		}
		// 定义当前的优先级集合
		List<String> weightNow = new ArrayList<String>();
		// 执行SQL语句，获取相应的数据源
		Result rs = CommonLibInteractiveSceneDAO.getWeight(scenariosid);
		// 判断数据源不为空且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取优先级
				String weight = rs.getRows()[i].get("weight").toString();
				if (weight.equals(oldWeight) ||NewEquals.equals(weight,oldWeight)) {
					continue;
				}
				// 将优先级放入当前的优先级集合中
				weightNow.add(weight);
			}
		}

		// 在所有的优先级的集合中移除当前的优先级集合
		weightLst.removeAll(weightNow);

		// 循环遍历剩余的优先级集合
		for (int i = 0; i < weightLst.size(); i++) {
			JSONObject obj = new JSONObject();
			// 生成一个id对象
			obj.put("id", weightLst.get(i));
			// 生成一个text对象
			obj.put("text", weightLst.get(i));
			// 将obj的json对象放入jsonArr数组中
			jsonArr.add(obj);
		}
		// 将jsonArr数组放入jsonObj的rows对象中
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}

	/**
	 *添加场景要素，并返回相应的信息
	 * 
	 * @param scenariosid
	 *            场景ID
	 *@param infotalbepath
	 *            对应信息表
	 *@param city
	 *            地市编码
	 *@param cityname
	 *            地市名称
	 *@param itemmode
	 *            选项填写方式
	 *@param name
	 *            场景要素名称
	 *@param interpat
	 *            交互模板
	 *@param weight
	 *            优先级
	 *@param wordclass
	 *            词类名称
	 *@param serviceType
	 *            四层结构串
	 *@return
	 *@returnType Object
	 */
	public static Object insertElementName(String scenariosid,String scenariosName,
			String infotalbepath, String city, String cityname,
			String itemmode, String name, String interpat, String weight,
			String wordclass, String container) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		
		int c = CommonLibInteractiveSceneDAO.insertElementName(user,scenariosid,
				 scenariosName,infotalbepath, city, cityname, itemmode, name, interpat,
				weight, wordclass, serviceType, container);
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
	 *修改场景要素，并返回相应的信息
	 * 
	 * @param scenariosid
	 *            场景ID
	 *@param scenarioselementid
	 *            场景元素ID
	 *@param infotalbepath
	 *            对应信息表
	 *@param city
	 *            地市编码
	 *@param cityname
	 *            地市名称
	 *@param itemmode
	 *            选项填写方式
	 *@param name
	 *            场景要素名称
	 *@param interpat
	 *            交互模板
	 *@param weight
	 *            优先级
	 *@param oldweight
	 *            旧优先级
	 *@param wordclass
	 *            词类名称
	 *@param serviceType
	 *            四层结构串
	 *@return
	 *@returnType Object
	 */
	public static Object updateElementName(String scenariosid,String scenariosName,
			String scenarioselementid, String infotalbepath, String city,
			String cityname, String itemmode, String name, String interpat,
			String weight, String oldweight, String wordclass, String container) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.updateElementName(user,scenariosid,
				scenariosName,scenarioselementid, infotalbepath, city, cityname, 
				itemmode,name, interpat, weight, oldweight, wordclass, serviceType,
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
	 * 删除场景要素，并返回相应的信息
	 * 
	 *@param scenarioselementid
	 *            场景元素ID
	 *@param weight
	 *            优先级
	 *@param scenariosid
	 *            场景ID
	 *@return
	 *@returnType int
	 */
	public static Object deleteElementName(String name, String scenarioselementid,
			String weight, String scenariosid ,String scenariosName) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.deleteElementName(
				user,serviceType,name,scenarioselementid, weight, scenariosid,scenariosName);
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

	/**
	 * 分页查询满足条件的问题要素值(词条)
	 * 
	 * @param wordclassid参数词类id
	 * @param name参数问题要素值名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object SelectWord(String wordclassid, String name, int page,
			int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		int count = UserOperResource.getElementWordCount(wordclassid, name);
		// 判断数据源不为null且含有数据
		if (count > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			// 定义SQL语句
			Result rs = UserOperResource.getElementWord(wordclassid, name,
					page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成wordid对象
					obj.put("wordid", rs.getRows()[i].get("wordid"));
					// 生成word对象
					obj.put("word", rs.getRows()[i].get("word"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
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
	 * 新增问题要素值
	 * 
	 * @param name参数场景要素值
	 * @param wordclassid参数词类名称id
	 * @param wordclass参数词类名称
	 * @return 新增返回的json串
	 */
	public static Object insertElementValue(String name, String wordclassid,
			String wordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		boolean rs = CommonLibWordDAO.exist(name, wordclassid);
		// 判断数据源不为null且含有数据
		if (rs) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将属性名称已存在放入jsonObj的msg对象中
			jsonObj.put("msg", "场景要素值已存在!");
			return jsonObj;
		} else {
			int c = CommonLibWordDAO.insert(name, wordclassid, user);
			// 判断事务处理结果
			if (c > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将新增成功放入jsonObj的msg对象中
				jsonObj.put("msg", "添加成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将新增失败放入jsonObj的msg对象中
				jsonObj.put("msg", "添加失败!");
			}
		}

		return jsonObj;
	}

	/**
	 * 删除问题要素值(词条),并更新对应的数据和规则
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param elementvalueid参数问题要素值id
	 * @param weight参数问题要素的优先级
	 * @param name参数问题要素值名称
	 * @param wordclass参数词类名称
	 * @return 删除返回的json串
	 */
	public static Object DeleteElementValue(String kbdataid,
			String kbcontentid, String elementvalueid, String weight,
			String name, String wordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.deleteElementValue(kbdataid, kbcontentid,
				elementvalueid, weight, name, wordclass);
		// 判断事务处理结果
		if (c > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "问题要素值删除成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "问题要素值删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 查询当前场景id下的场景要素组合信息
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return 问题要素组合的json串
	 */
	public static Object queryElement(String scenariosid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode = "";
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
			}
		}

		// 定义问题要素和问题要素值的map集合
		Map<String, List<String>> elementnamevalueMap = new HashMap<String, List<String>>();
		Result rs = UserOperResource.getAnswercontent(scenariosid);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 获取回复模板数据
			String answercontent = rs.getRows()[0].get("answercontent") != null ? rs.getRows()[0].get("answercontent").toString() : "";
			// 将回复模板放入jsonObj的answer对象中
			jsonObj.put("answer", answercontent);
		} else {
			// 将空的回复模板放入jsonObj的answer对象中
			jsonObj.put("answer", "");
		}
		// 获取问题要素组合的SQL语句
		rs = CommonLibInteractiveSceneDAO.queryElementAndWord(scenariosid,
				cityCode);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取问题要素名称
				String name = rs.getRows()[i].get("name").toString();
				// 获取问题要素值(词条)
				Object word = rs.getRows()[i].get("word");
				// 判断问题要素名称和问题要素值的map集合是否含有问题要素名称
				if (elementnamevalueMap.containsKey(name)) {
					// 判断问题要素值(词条)是否为null
					if (word != null) {
						// 将key为当前问题要素名称的对应的value的集合中添加问题要素值(词条)
						elementnamevalueMap.get(name).add(word.toString());
					}
				} else {
					// 定义问题要素(词条)集合
					List<String> valueLst = new ArrayList<String>();
					// 判断问题要素值(词条)是否为null
					if (word != null) {
						// 将问题要素值(词条)放入问题要素值(词条)集合中
						valueLst.add(word.toString());
					}
					// 将key和value放入map集合中
					elementnamevalueMap.put(name, valueLst);
				}
			}
		}

		rs = CommonLibInteractiveSceneDAO
				.queryElementServiceAndAbstract(scenariosid);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取问题要素名称
				String service = rs.getRows()[i].get("service").toString();
				// 获取问题要素值(词条)
				String abs = rs.getRows()[i].get("abstract") == null ? "" : rs
						.getRows()[i].get("abstract").toString();
				if (elementnamevalueMap.containsKey("问题对象")) {
					if (!elementnamevalueMap.get("问题对象").contains(service)) {
						elementnamevalueMap.get("问题对象").add(service);
					}

				}
				if (elementnamevalueMap.containsKey("标准问题")) {
					if (!"".equals(abs)) {
						elementnamevalueMap.get("标准问题").add(abs);
					}

				}

			}

		}

		// 定义查询问题要素名称
		rs = CommonLibInteractiveSceneDAO.queryElement(scenariosid);
		boolean b = false;
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String isshare = rs.getRows()[i].get("isshare").toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs
						.getRows()[i].get("city").toString();

				// 判断是否满足当前用户city的列信息
				if ("".equals(city) || "全国".equals(city)) {
					if (!"全行业".equals(customer)) {
						if ("否".equals(isshare)) {
							continue;
						}
					}
				} else {
					if (!"全行业".equals(customer)) {
						if (!containsOneCity(city, cityList.get(0))) {
							continue;
						}
					}
				}

				// 获取问题要素的名称
				String name = rs.getRows()[i].get("name").toString();
				// 获取问题要素的优先级
				String weight = rs.getRows()[i].get("weight").toString();
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				// 生成name对象
				obj.put("name", name);
				// 生成columnnum对象
				obj.put("weight", Integer.valueOf(weight));
				// 定义json数组
				JSONArray arr = new JSONArray();
				
				// add by xzh
				// 加载实体机器人id列
				if ("userID".equals(name)){
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成id对象
					o.put("id", "(空)");
					// 生成text对象
					o.put("text", "(空)");
					// 将空的放入jsonArr数组中
					arr.add(o);
					
					o = new JSONObject();
					o.put("id", "交互");
					// 生成text对象
					o.put("text", "<用户未选或未告知,系统提示输入>");
					// 将缺失的放入jsonArr数组中
					arr.add(o);
					// 定义一个对象
					o = new JSONObject();
					// 生成id对象
					o.put("id", "已选");
					// 生成text对象
					o.put("text", "<用户告知或已选择，系统可获知>");
					arr.add(o);
					// 定义一个对象
					o = new JSONObject();
					// 生成id对象
					o.put("id", "缺失");
					// 生成text对象
					o.put("text", "<用户未选或未告知>");
					// 将存在的放入jsonArr数组中
					arr.add(o);
					
					Result robotidnameRs = CommonLibMetafieldmappingDAO.getConfigKeyValue("实体机器人ID配置");
					if (robotidnameRs != null && robotidnameRs.getRowCount() > 0){
						Map<String,String> robotidtoNameMap = new HashMap<String,String>();
						Map<String,String> robotidtoCitycodeMap = new HashMap<String,String>();
						for(int k = 0;k < robotidnameRs.getRowCount();k++){
							if (robotidnameRs.getRows()[k].get("k").toString().startsWith("Name:")){
								robotidtoNameMap.put(robotidnameRs.getRows()[k].get("name").toString(), robotidnameRs.getRows()[k].get("k").toString().replace("Name:", ""));
							} else if (robotidnameRs.getRows()[k].get("k").toString().startsWith("CityCode:")){
								robotidtoCitycodeMap.put(robotidnameRs.getRows()[k].get("name").toString(), robotidnameRs.getRows()[k].get("k").toString().replace("Name:", ""));
							}
						}
						if (!"全行业".equals(customer)){// 非全行业
							if (!"".equals(cityCode) && !"全国".equals(cityCode)){
								for (Map.Entry<String, String> map : robotidtoCitycodeMap.entrySet()){
									if (!map.getValue().equals("CityCode:"+cityCode)){
										robotidtoNameMap.remove(map.getKey());
									}
								}
							}
						}
						for (Map.Entry<String, String> map : robotidtoNameMap.entrySet()){
							o = new JSONObject();
							o.put("id", map.getKey());
							o.put("text", map.getValue());
							// 将生成的对象放入arr数组中
							arr.add(o);
						}
					}
				} else {
					// 根据问题要素名称获取问题要素值(词条)集合
					List<String> valuelst = elementnamevalueMap.get(name);
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成id对象
					o.put("id", "(空)");
					// 生成text对象
					o.put("text", "(空)");
					// 将空的放入jsonArr数组中
					arr.add(o);
					// 定义一个对象
					// o = new JSONObject();
					// // 生成id对象
					// o.put("id", "交互");
					// // 生成text对象
					// o.put("text", "交互");
					// // 将缺失的放入jsonArr数组中
					// arr.add(o);
					// // 定义一个对象
					// o = new JSONObject();
					// // 生成id对象
					// o.put("id", "已选");
					// // 生成text对象
					// o.put("text", "已选");
					
					o = new JSONObject();
					o.put("id", "交互");
					// 生成text对象
					o.put("text", "<用户未选或未告知,系统提示输入>");
					// 将缺失的放入jsonArr数组中
					arr.add(o);
					// 定义一个对象
					o = new JSONObject();
					// 生成id对象
					o.put("id", "已选");
					// 生成text对象
					o.put("text", "<用户告知或已选择，系统可获知>");
					arr.add(o);
					// 定义一个对象
					o = new JSONObject();
					// 生成id对象
					o.put("id", "缺失");
					// 生成text对象
					o.put("text", "<用户未选或未告知>");
					// 将存在的放入jsonArr数组中
					arr.add(o);
					// 循环遍历问题要素值(词条)集合
					if (valuelst != null) {
						for (int j = 0; j < valuelst.size(); j++) {
							// 定义json对象
							o = new JSONObject();
							// 生成id对象
							o.put("id", valuelst.get(j));
							// 生成text对象
							o.put("text", valuelst.get(j));
							// 将生成的对象放入arr数组中
							arr.add(o);
						}
					}
				}
				// 将属性值json数组放入obj的attrvalue对象中
				obj.put("elementvalue", arr);
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		// 将jsonArr数组放入jsonObj的root对象中
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}

	/**
	 *判断用户cityList，是否有一条元素包含在 city中
	 * 
	 * @param city
	 *@param cityList
	 *@return
	 *@returnType boolean
	 */
	public static boolean containsOneCity(String city, List<String> cityList) {
		boolean b = false;
		for (int i = 0; i < cityList.size(); i++) {
			if (city.contains(cityList.get(i))) {
				b = true;
			}
		}
		return b;
	}

	/**
	 *判断用户cityList，是否有一条元素包含在 city中
	 * 
	 * @param cityStr
	 *@param city
	 *@return
	 *@returnType boolean
	 */
	public static boolean containsOneCity(String cityStr, String city) {
		boolean b = false;
		String arry[] = cityStr.split(",");
		for (int i = 0; i < arry.length; i++) {
			if (city.equals(arry[i])) {
				b = true;
			}
		}
		return b;
	}

	/**
	 * 查询满足条件的带分页的问题要素数据信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合条件
	 * @param returntxttype参数答案类型
	 * @param status参数状态
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return 满足条件的json串
	 */
	public static Object SelectConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String status, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = UserOperResource.getConditionCombToReturnTxtCount(kbdataid,
				kbcontentid, conditions, returntxttype, status);
		// 判断数据源不为null,且含有数据
		if (count > 0) {
			// 将满足条件的数量放入jsonObj的total对象中
			jsonObj.put("total", count);
			// 查询满足条件的带分页的SQL语句
			Result rs = UserOperResource.getConditionCombToReturnTxt(kbdataid,
					kbcontentid, conditions, returntxttype, status, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj
							.put("id", rs.getRows()[i].get("combitionid")
									.toString());
					// 循环遍历condition1-20，并生成相应的对象
					for (int j = 1; j < GetConfigValue.NUMBER_OF_MAX_SCENE+1; j++) {
						obj.put("condition" + j, rs.getRows()[i]
								.get("condition" + j) != null ? rs.getRows()[i]
								.get("condition" + j).toString() : "");
					}
					// 生成答案类型对象
					obj.put("type", Integer.parseInt(rs.getRows()[i].get(
							"returntxttype").toString()));
					// 生成状态对象
					obj.put("status", rs.getRows()[i].get("status").toString());
					// 生成答案内容对象
					obj.put("returntxt",
							rs.getRows()[i].get("returntxt") != null ? rs
									.getRows()[i].get("returntxt").toString()
									: "");
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 将问题要素信息添加到数据库中，并返回相应的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param returntxttype参数答案类型
	 * @param returntxt参数答案内容
	 * @param abs参数摘要名称
	 * @return 添加后返回的json串
	 */
	public static Object InsertConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String returntxt, String abs) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if (sre == null || "".equals(sre)) {
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User) sre;
		String userIp = user.getUserIP();
		String userId = user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();
		boolean b = UserOperResource.isExitConditionCombToReturnTxt(kbdataid,
				kbcontentid, conditions, returntxttype, returntxt);
		// 判断数据源不为空且含有数据
		if (b) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将数据已存在信息放入jsonObj的msg对象中
			jsonObj.put("msg", "该条数据已存在!");
		} else {
			int c = UserOperResource.insertConditionCombToReturnTxt(kbdataid,
					kbcontentid, conditions, returntxttype, returntxt, abs,
					serviceType);
			// 判断事务处理结果
			if (c > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将添加成功放入jsonObj的msg对象中
				jsonObj.put("msg", "添加成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "添加失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 根据数据id删除相应的数据，并返回相应的信息
	 * 
	 * @param combitionid参数数据id
	 * @param abs参数摘要名称
	 * @return 删除后的相应json串
	 */
	public static Object DeleteConditionCombToReturnTxt(String combitionid,
			String abs) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.deleteConditionCombToReturnTxt(combitionid,
				abs);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 全量删除数据
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param abs参数摘要名称
	 * @return 全量删除返回的json串
	 */
	public static Object DeleteAllConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String abs) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.deleteAllConditionCombToReturnTxt(kbdataid,
				kbcontentid, abs);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功放入jsonObj的msg对象中
			jsonObj.put("msg", "全量删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败放入jsonObj的msg对象中
			jsonObj.put("msg", "全量删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 确认问题元素值组合，将待审核变成已审核
	 * 
	 * @param combitionid参数combitionid
	 * @return 确认处理的返回的json串
	 */
	public static Object ConfirmConditionCombToReturnTxt(String combitionid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.ConfirmConditionCombToReturnTxt(combitionid);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将确认成功放入jsonObj的msg对象中
			jsonObj.put("msg", "确认成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将确认失败放入jsonObj的msg对象中
			jsonObj.put("msg", "确认失败!");
		}
		return jsonObj;
	}

	/**
	 * 全量确认
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @return 确认返回的json串
	 */
	public static Object ConfirmAllConditionCombToReturnTxt(String kbdataid,
			String kbcontentid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.confirmAllConditionCombToReturnTxt(kbdataid,
				kbcontentid);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将确认成功放入jsonObj的msg对象中
			jsonObj.put("msg", "全量确认成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将确认失败放入jsonObj的msg对象中
			jsonObj.put("msg", "全量确认失败!");
		}
		return jsonObj;
	}

	/**
	 * 更新当前数据中需要修改的值，并将状态改为未审核，并返回相应的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param returntxttype参数答案类型
	 * @param returntxt参数答案内容
	 * @param combitionid参数数据id
	 * @return 更新后返回的json串
	 */
	public static Object UpdateConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String returntxt, String combitionid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		boolean b = UserOperResource.isExitConditionCombToReturnTxt(kbdataid,
				kbcontentid, conditions, returntxttype, returntxt);
		// 判断数据源不为null且含有数据
		if (b) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将数据已存在信息放入jsonObj的msg对象中
			jsonObj.put("msg", "该条数据已存在!");
		} else {
			int c = UserOperResource.updateConditionCombToReturnTxt(kbdataid,
					kbcontentid, conditions, returntxttype, returntxt,
					combitionid);
			// 判断事务处理结果
			if (c > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将更新成功放入jsonObj的msg对象中
				jsonObj.put("msg", "更新成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将更新失败放入jsonObj的msg对象中
				jsonObj.put("msg", "更新失败!");
			}
		}

		return jsonObj;
	}

	/**
	 * 新增列，并返回相应的信息
	 * 
	 * @param scenariosid参数场景id
	 * @param columns
	 *            列信息
	 * @return 添加后返回的json串
	 */
	public static Object addColumn(String scenariosid, String columns) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "添加失败!");
				return jsonObj;
			}
		}
		String cityCodeStr = "";
		String cityNameStr = "";
		// for (int k = 0; k < cityList.size(); k++) {
		// cityCodeStr = cityCodeStr + cityList.get(k) + ",";
		// cityNameStr = cityNameStr + cityCodeToCityName.get(cityList.get(k))
		// + ",";
		// }

		// cityCodeStr = cityCodeStr.substring(0, cityCodeStr.lastIndexOf(","));
		// cityNameStr = cityNameStr.substring(0, cityNameStr.lastIndexOf(","));

		cityCodeStr = cityList.get(0);
		cityNameStr = cityCodeToCityName.get(cityList.get(0));

		// 定义问题元素数组
		String[] columnArr = new String[] {};
		columnArr = columns.split("\\|");

		Result rs = CommonLibInteractiveSceneDAO
				.getColumnCityByScenariosidAndColumn(scenariosid, columnArr);
		List<List<String>> list = new ArrayList<List<String>>();
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("scenarioselementid")
						.toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs
						.getRows()[i].get("city").toString();
				String cityName = rs.getRows()[i].get("cityname") == null ? ""
						: rs.getRows()[i].get("cityname").toString();
				if ("".equals(city) && "".equals(cityName)) {
					city = cityCodeStr;
					cityName = cityNameStr;
				} else {
					city = city + "," + cityCodeStr;
					cityName = cityName + "," + cityNameStr;
				}

				List<String> l = new ArrayList<String>();
				l.add(sid);
				l.add(city);
				l.add(cityName);
				list.add(l);

			}
		}
		int c = CommonLibInteractiveSceneDAO.updateElementCity(list);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将添加失败放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
		}
		return jsonObj;
	}

	/**
	 * 移除列，并返回相应的信息
	 * 
	 * @param scenariosid参数场景id
	 * @param columns
	 *            列信息
	 * @return 添加后返回的json串
	 */
	public static Object deleteColumn(String scenariosid, String columns,
			String ruletype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		String cityCodeStr = "";
		String cityNameStr = "";
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "请配置用户相关地市信息!");
				return jsonObj;
			}
			cityCodeStr = cityList.get(0);
			cityNameStr = cityCodeToCityName.get(cityList.get(0));
		}

		// for (int k = 0; k < cityList.size(); k++) {
		// cityCodeStr = cityCodeStr + cityList.get(k) + ",";
		// cityNameStr = cityNameStr + cityCodeToCityName.get(cityList.get(k))
		// + ",";
		// }

		// cityCodeStr = cityCodeStr.substring(0, cityCodeStr.lastIndexOf(","));
		// cityNameStr = cityNameStr.substring(0, cityNameStr.lastIndexOf(","));

		// 定义问题元素数组
		String[] columnArr = new String[] {};
		columnArr = columns.split("\\|");

		Result rs = CommonLibInteractiveSceneDAO
				.getColumnCityByScenariosidAndColumn(scenariosid, columnArr);
		List<List<String>> list = new ArrayList<List<String>>();
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("scenarioselementid")
						.toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs
						.getRows()[i].get("city").toString();
				String cityName = rs.getRows()[i].get("cityname") == null ? ""
						: rs.getRows()[i].get("cityname").toString();
				String weight = rs.getRows()[i].get("weight").toString();
				List<String> list_city = new ArrayList<String>();
				List<String> list_cityName = new ArrayList<String>();
				list_city = new ArrayList<String>(Arrays
						.asList(city.split(",")));
				list_city.remove(cityCodeStr);

				list_cityName = new ArrayList<String>(Arrays.asList(cityName
						.split(",")));
				list_cityName.remove(cityNameStr);

				city = listJoin(list_city, ",");
				cityName = listJoin(list_cityName, ",");

				List<String> l = new ArrayList<String>();
				l.add(sid);
				l.add(city);
				l.add(cityName);
				l.add(weight);
				l.add(cityCodeStr);
				l.add(scenariosid);
				l.add(ruletype);
				list.add(l);

			}
		}
		int c = CommonLibInteractiveSceneDAO.updateElementCityAndRules(list);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将添加失败放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	public static Object updateMenuitems(String scenariosid, String rulesid,
			String interpat, String interactiveoptions, String customvalue,
			String conditions, String weight, String ruletype,
			String ruleresponse, String excludedcity, String city,
			String responsetype, String wordclassid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		// String answer = interpat.split("<")[0];
		String answer = interpat;
		// if(("多条值以|分隔开").equals(customvalue)){
		// customvalue="";
		// }
		List<String> list = new ArrayList<String>();
		interactiveoptions = interactiveoptions + "|" + customvalue;
		String options[] = interactiveoptions.split("\\|");
		String items = "";
		String ruleresponsetemplate = "";
		String template = "";
		String opt = "";
		int k = 0;
		for (int i = 0; i < options.length; i++) {
			if ("".equals(options[i])) {
				continue;
			}
			k++;
			list.add(options[i]);
			items = items + "[" + k + "]" + options[i] + ".<br/>";
			template = template + "[" + k + "]" + options[i] + ".";
			opt = opt + options[i] + "||";
		}
		template = answer + template;
		answer = answer + "<br/>" + items;

		opt = opt.substring(0, opt.lastIndexOf("||"));
		ruleresponsetemplate = "菜单询问(\"" + template + "\",\"" + opt + "\")";

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String serviceType = user.getIndustryOrganizationApplication();

		int c = -1;
		boolean b = false;
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "请联系管理员配置用户规则地市!");
				return jsonObj;
			} else {// 复制规则
				cityCode = cityList.get(0);
				cityName = cityCodeToCityName.get(cityList.get(0));

				// 插入自定义词条

				if (list.size() > 0) {
					if (CommonLibWordDAO.insertWord(wordclassid, list,
							cityCode, cityName, serviceType) < 1) {
						// 将false放入jsonObj的success对象中
						jsonObj.put("success", false);
						// 将添加失败放入jsonObj的msg对象中
						jsonObj.put("msg", "保存自定义值失败!");
						return jsonObj;
					}
				}

				if (cityCode.equals(city)) {// 当前规则地市等于用户地市,直接update
					// 执行SQL语句，并返回相应的数据源
					// b = CommonLibInteractiveSceneDAO.isExitSceneRules(
					// scenariosid, conditions, ruletype, answer, weight,
					// cityCode, responsetype,"", "");
					// if (b) {
					// // 将false放入jsonObj的success对象中
					// jsonObj.put("success", false);
					// // 将数据已存在信息放入jsonObj的msg对象中
					// jsonObj.put("msg", "规则已存在!");
					// return jsonObj;
					// } else {
					c = CommonLibInteractiveSceneDAO.updateMenuitems(
							scenariosid, rulesid, answer, ruleresponsetemplate);
					// }

				} else {

					// 执行SQL语句，并返回相应的数据源
					b = CommonLibInteractiveSceneDAO.isExitSceneRules(
							scenariosid, conditions, ruletype, answer, weight,
							cityCode, responsetype, "", "");
					if (b) {
						// 将false放入jsonObj的success对象中
						jsonObj.put("success", false);
						// 将数据已存在信息放入jsonObj的msg对象中
						jsonObj.put("msg", "规则已存在!");
						return jsonObj;
					} else {
						c = CommonLibInteractiveSceneDAO.copyRules(rulesid,
								scenariosid, conditions, weight, ruletype,
								answer, serviceType, cityCode, cityName,
								excludedcity, ruleresponsetemplate, "2", "","");
					}
				}
			}
		} else {
			if (list.size() > 0) {
				if (CommonLibWordDAO.insertWord(wordclassid, list, cityCode,
						cityName, serviceType) < 1) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将添加失败放入jsonObj的msg对象中
					jsonObj.put("msg", "保存自定义值失败!");
					return jsonObj;
				}
			}

			c = CommonLibInteractiveSceneDAO.updateMenuitems(scenariosid,
					rulesid, answer, ruleresponsetemplate);

		}

		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将添加失败放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	public static Object insertMenuitems(String customvalue, String wordclassid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", true);
		// 将添加失败放入jsonObj的msg对象中
		jsonObj.put("msg", "保存自定义值成功!");
		// 无新增加词条
		if ("".equals(customvalue) || customvalue == null) {
			return jsonObj;
		}
		// 有新增加词条
		List<String> list = new ArrayList<String>();
		String options[] = customvalue.split("\\|");
		String items = "";
		String ruleresponsetemplate = "";
		String template = "";
		String opt = "";
		int k = 0;
		for (int i = 0; i < options.length; i++) {
			if ("".equals(options[i])) {
				continue;
			}
			k++;
			list.add(options[i].trim());
		}

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String serviceType = user.getIndustryOrganizationApplication();

		int c = -1;
		boolean b = false;
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "请联系管理员配置用户规则地市!");
				return jsonObj;
			} else {
				
				cityCode = cityList.get(0);
				cityName = cityCodeToCityName.get(cityList.get(0));

				// 插入自定义词条

				if (list.size() > 0) {
					if (CommonLibWordDAO.insertWord(wordclassid, list,
							cityCode, cityName, serviceType) < 1) {
						// 将false放入jsonObj的success对象中
						jsonObj.put("success", false);
						// 将添加失败放入jsonObj的msg对象中
						jsonObj.put("msg", "保存自定义值失败!");
					}
				}
			}
		} else {
			cityCode = "全国";
			cityName = "全国";
			if (list.size() > 0) {
				if (CommonLibWordDAO.insertWord(wordclassid, list, cityCode,
						cityName, serviceType) < 1) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将添加失败放入jsonObj的msg对象中
					jsonObj.put("msg", "保存自定义值失败!");
				}
			}

		}

		return jsonObj;
	}

	/**
	 * 将规则添加到规则表中，并返回相应的信息
	 * 
	 * @param scenariosid参数场景id
	 * @param conditions参数问题要素组合
	 * @param weight参数规则优先级
	 * @param ruletype参数规则类型
	 * @param ruleresponse参数规则回复内容
	 * @param abs参数摘要名称
	 * @param flag
	 *            复制标识
	 * @param copycity
	 *            当前复制时页面地市编码
	 * @param userquestion
	 *            用户问题
	 * @return 添加后返回的json串
	 */
	public static Object insertSceneRules(String scenariosid,String scenariosName,
			String conditions, String weight, String ruletype,
			String ruleresponse, String service, String abs,
			String ruleresponsetemplate, String responsetype, String city,
			String excludedcity,
			String questionobject, String standardquestion, String flag,
			String copycity, String userquestion, String currentnode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		String customer = user.getCustomer();
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		
		// 对规则进行审查
		if (null != ruleresponse && null != ruleresponsetemplate && !"".equals(ruleresponse) && !"".equals(ruleresponsetemplate)){
			ruleresponse = responseCheck(ruleresponse);
			ruleresponsetemplate = responseCheck(ruleresponsetemplate);
		}

		if (!"全行业".equals(customer)) {
			if (!"".equals(city) && city != null) {
				cityCode = city;
				cityName = cityCodeToCityName.get(cityCode);
			} else {
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "scenariosrules", "S");
				// 该操作类型用户能够操作的资源
				cityList = resourseMap.get("地市");
				if (cityList != null) {
					cityCode = cityList.get(0);
					cityName = cityCodeToCityName.get(cityCode);
				}

			}

		} else {
			if (!"".equals(city) && city != null) {
				cityCode = city;
				cityName = cityCodeToCityName.get(cityCode);
			}
		}

		// 定义问题元素数组
		String[] conditionArr = new String[] {};
		// 判断问题要素组合不为null且不为空
		if (conditions != null && !"".equals(conditions)) {
			// 将问题元素中按照@拆分
			conditionArr = conditions.split("@", 20);
		}

		// 根据内容判断添加的规则是否数据型，如果是ruletype="4"
		List<String> list = new ArrayList<String>(Arrays.asList(conditionArr));
		if (! NewEquals.equals("2",ruletype) && ! NewEquals.equals("3",ruletype) && ! NewEquals.equals("5",ruletype)) {

			if (!list.contains("交互") && !list.contains("已选")) {
				ruletype = "4";
			}
		}

		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();

		if ("copy".equals(flag)) {// 复制时，重新给优先级赋值
			List<String> weightList = getRuleWeight(ruletype, copycity,
					scenariosid);
			if (weightList.size() == 1) {
				weight = Double.parseDouble(weight) + 0.001 + "";
			} else {
				int index = weightList.indexOf(weight);
				if (weightList.size() == (index + 1)) {
					weight = Double.parseDouble(weight) + 0.001 + "";
				} else {
					weight = (Double.parseDouble(weight) + Double
							.parseDouble(weightList.get(index + 1)))
							/ 2 + "";
				}
			}
			// double w = Double.parseDouble(weight)+ 0.001;
			// BigDecimal b = new BigDecimal(w);
			// weight = b.setScale(3,
			// BigDecimal.ROUND_HALF_UP).doubleValue()+"";
		} else {
			// 获得当前场景下符合规则的规则最大优先级值
			double maxWeight = CommonLibInteractiveSceneDAO.getMaxWeight(scenariosid);
			weight = maxWeight + 1 + "";
		}

//		boolean b = CommonLibInteractiveSceneDAO.isExitSceneRules(scenariosid,
//				conditions, ruletype, ruleresponse, weight, cityCode, responsetype,service,
//				abs);
		boolean b =false;
		// 判断数据源不为空且含有数据
		if (b) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将规则已存在信息放入jsonObj的msg对象中
			jsonObj.put("msg", "规则已存在!");
		} else {
			int c = CommonLibInteractiveSceneDAO.insertSceneRules(user,scenariosid,
					scenariosName,conditions, weight, ruletype, ruleresponse, serviceType,
					cityCode, cityName, excludedcity, service, abs, ruleresponsetemplate,
					responsetype, questionobject, standardquestion,
					userquestion, currentnode);

			// 判断事务处理结果
			if (c > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将添加成功放入jsonObj的msg对象中
				jsonObj.put("msg", "添加成功!");
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "添加失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 根据不同的条件分页查询满足条件的规则信息
	 * 
	 * @param scenariosid参数场景id
	 * @param conditions参数问题要素组合
	 * @param ruletype参数规则类型
	 * @param weight参数规则优先级
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return 满足条件的json串
	 */
	public static Object selectSceneRules(String scenariosid,
			String conditions, String ruletype, String weight, int page,
			int rows, String city, String belong, String ruleresponse, String issue ,String strategy) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		List<String> cityList = new ArrayList<String>();
		String cityCode = "";
		String cityName = "";
		String userCityCode = "";
		
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			if (city != null && !"".equals(city) ) {// 判断页面传入city
				if ("全国".equals(city)) {// 非全行业用户显示全国+本省规则
					cityCode = "全国";
					HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
							.resourseAccess(user.getUserID(), "scenariosrules", "S");
					// 该操作类型用户能够操作的资源
					cityList = resourseMap.get("地市");
					if (cityList != null) {
						userCityCode = cityList.get(0);
					}
				} else {// 显示选择地市规则
					cityCode = city;
					cityName = cityCodeToCityName.get(city);
				}

			} else {// 没有city参数
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "scenariosrules", "S");
				// 该操作类型用户能够操作的资源
				cityList = resourseMap.get("地市");
				if (cityList != null) {
					cityCode = cityList.get(0);
					cityName = cityCodeToCityName.get(cityList.get(0));
				}
			}

		} else {// 全行业用户
			if (city != null && !"".equals(city)) {
				if (!"全国".equals(city)) {
					cityCode = city;
					cityName = cityCodeToCityName.get(city);
				} else {
					cityCode = "全国";
					cityName = "全国";
				}
			}
		}

		int count = CommonLibInteractiveSceneDAO.getSceneRulesCount(
				scenariosid, conditions, ruletype, weight, cityCode, cityName,
				belong, userCityCode, ruleresponse,issue,strategy);
		// 判断数据源不为null
		if (count > 0) {
			// 将满足条件的数量放入jsonObj的total对象中
			jsonObj.put("total", count);
			JSONArray rs = CommonLibInteractiveSceneDAO.getSceneRules(scenariosid,
					conditions, ruletype, weight, page, rows, cityCode,
					cityName,belong,userCityCode,cityCodeToCityName,ruleresponse,issue,strategy);
			jsonObj.put("rows", rs);
		} else {
			// 清空jsonArr
			jsonArr.clear();
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 将空的jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 根据规则id删除规则信息，并返回相应的信息
	 * 
	 * @param ruleid参数规则id
	 * @param currentcitycode
	 *            规则地市编码
	 * @return 删除后返回的json串
	 */
	public static Object deleteSceneRules(String ruleid,String scenariosName,
			String currentcitycode, String excludedcity) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		String customer = user.getCustomer();
		String cityCode = "";
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				// jsonObj.put("success", false);
				// // 将添加失败放入jsonObj的msg对象中
				// jsonObj.put("msg", "请联系管理员配置用户规则地市!");
				// return jsonObj;
			} else {
				cityCode = cityList.get(0);
			}

		}
		int c = CommonLibInteractiveSceneDAO.deleteSceneRules(user,serviceType,
				ruleid,scenariosName,cityCode,currentcitycode, excludedcity);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 更新规则中需要修改的值，并返回相应的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param weight参数规则优先级
	 * @param ruletype参数规则类型
	 * @param ruleresponse参数规则回复内容
	 * @param ruleid参数规则id
	 * @param userquestion
	 *            用户问题
	 * @return 更新后返回的json串
	 */
	public static Object updateSceneRules(String scenariosid,String scenariosName,
			String conditions, String weight, String ruletype,
			String ruleresponse, String ruleid, String excludedcity,
			String city, String service, String abs, String responsetype,
			String ruleresponsetemplate, String questionobject,
			String standardquestion, String userquestion, String currentcitycode,String currentnode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String serviceType = user.getIndustryOrganizationApplication();
		
		// 对规则进行审查
		if (null != ruleresponse && null != ruleresponsetemplate && !"".equals(ruleresponse) && !"".equals(ruleresponsetemplate)){
			ruleresponse = responseCheck(ruleresponse);
			ruleresponsetemplate = responseCheck(ruleresponsetemplate);
		}
		
		int c = -1;
		boolean b = false;
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		// 定义问题元素数组
		String[] conditionArr = new String[] {};
		// 判断问题要素组合不为null且不为空
		if (conditions != null && !"".equals(conditions)) {
			// 将问题元素中按照@拆分
			conditionArr = conditions.split("@", 20);
		}
		// 根据内容判断添加的规则是否数据型，如果是ruletype="4"
		List<String> list = new ArrayList<String>(Arrays.asList(conditionArr));
		if (!NewEquals.equals("3",ruletype) && ! NewEquals.equals("2",ruletype) && ! NewEquals.equals("5",ruletype)) {
			if (!list.contains("交互") && !list.contains("已选")) {
				ruletype = "4";
			}
		}
		if (!"全行业".equals(customer)) {// 非全行业用户
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				jsonObj.put("success", false);
				// 将添加失败放入jsonObj的msg对象中
				jsonObj.put("msg", "请联系管理员配置用户规则地市!");
				return jsonObj;
			} else {
				cityCode = cityList.get(0);
				cityName = cityCodeToCityName.get(cityList.get(0));
				if (currentcitycode == null || "".equals(currentcitycode) || "全国".equals(currentcitycode)) {// 判断如果规则本身地市全国，直接copy
					if (!"".equals(city) && city != null) {// 优先考虑页面选择地市，如果不为空取其值
						cityCode = city;
						cityName = cityCodeToCityName.get(cityCode);
					}
 
					// 执行SQL语句，并返回相应的数据源
					b = CommonLibInteractiveSceneDAO.isExitSceneRules(
							scenariosid, conditions, ruletype, ruleresponse,
							weight, cityCode, responsetype, "", "");
					if (b) {
						// 将false放入jsonObj的success对象中
						jsonObj.put("success", false);
						// 将数据已存在信息放入jsonObj的msg对象中
						jsonObj.put("msg", "规则已存在!");
						return jsonObj;
					} else {
						// add by xzh
						// 复制其他规则
						int d = CommonLibInteractiveSceneDAO.copyOtherRules(ruleid,
								cityCode, cityName, scenariosid, ruletype, serviceType);
						// 复制规则
						c = CommonLibInteractiveSceneDAO.copyRules(ruleid,
								scenariosid, conditions, weight, ruletype,
								ruleresponse, serviceType, cityCode, cityName,
								excludedcity, ruleresponsetemplate,
								responsetype, userquestion, currentnode);
					}
				} else {
					if (city==null || city.equals("")) {
						city = cityCode;
					}
					// 修改规则地市不为全国
					if (cityCode.equals(city)) {// 当前规则修改地市等于用户地市,直接update
						// 执行SQL语句，并返回相应的数据源
						b = CommonLibInteractiveSceneDAO.isExitSceneRules(
								scenariosid, conditions, ruletype,
								ruleresponse, weight, cityCode, responsetype,
								"", "");
						if (b) {
							// 将false放入jsonObj的success对象中
							jsonObj.put("success", false);
							// 将数据已存在信息放入jsonObj的msg对象中
							jsonObj.put("msg", "规则已存在!");
							return jsonObj;
						} else {
							c = CommonLibInteractiveSceneDAO.updateSceneRules(
									user,serviceType,scenariosid,scenariosName,conditions, weight, ruletype,
									ruleresponse, ruleid, service, abs,
									responsetype, ruleresponsetemplate,
									cityCode, cityName,  excludedcity, questionobject,
									standardquestion, userquestion, currentnode);
						}

					} else {// 复制规则
						if (cityCode.endsWith("0000")) {// 判断是否是省级或者直辖市用户
							if (!"".equals(city) && city != null) {// 判断修改的规则地市是否是当前省级下的，如果是直接修改
								String ccityCode = cityCode.substring(0, 2);
								if (city.startsWith(ccityCode)) {
									cityName = cityCodeToCityName.get(city);
									// 执行SQL语句，并返回相应的数据源
									b = CommonLibInteractiveSceneDAO
											.isExitSceneRules(scenariosid,
													conditions, ruletype,
													ruleresponse, weight, city,
													responsetype, "", "");
									if (b) {
										// 将false放入jsonObj的success对象中
										jsonObj.put("success", false);
										// 将数据已存在信息放入jsonObj的msg对象中
										jsonObj.put("msg", "规则已存在!");
										return jsonObj;
									} else {
										c = CommonLibInteractiveSceneDAO
												.updateSceneRules(user,serviceType,scenariosid,
														scenariosName,conditions, weight,
														ruletype, ruleresponse,
														ruleid, service, abs,
														responsetype,
														ruleresponsetemplate,
														city, cityName,
														excludedcity,
														questionobject,
														standardquestion,
														userquestion, currentnode);
									}

								}
							} else {// 页面传入city 为空直接 复制规则
								// 执行SQL语句，并返回相应的数据源
								b = CommonLibInteractiveSceneDAO
										.isExitSceneRules(scenariosid,
												conditions, ruletype,
												ruleresponse, weight, cityCode,
												responsetype, "", "");
								if (b) {
									// 将false放入jsonObj的success对象中
									jsonObj.put("success", false);
									// 将数据已存在信息放入jsonObj的msg对象中
									jsonObj.put("msg", "规则已存在!");
									return jsonObj;
								} else {
									c = CommonLibInteractiveSceneDAO.copyRules(
											ruleid, scenariosid, conditions,
											weight, ruletype, ruleresponse,
											serviceType, cityCode, cityName,
											excludedcity, ruleresponsetemplate,
											responsetype, userquestion, currentnode);
								}
							}
						} else {
							
							if ("".equals(city) || city == null) {// 修改省级规则
								// 页面传入city 为空直接 复制规则
								// 执行SQL语句，并返回相应的数据源
								b = CommonLibInteractiveSceneDAO
										.isExitSceneRules(scenariosid,
												conditions, ruletype,
												ruleresponse, weight, cityCode,
												responsetype, "", "");
								if (b) {
									// 将false放入jsonObj的success对象中
									jsonObj.put("success", false);
									// 将数据已存在信息放入jsonObj的msg对象中
									jsonObj.put("msg", "规则已存在!");
									return jsonObj;
								} else {
									c = CommonLibInteractiveSceneDAO.copyRules(
											ruleid, scenariosid, conditions,
											weight, ruletype, ruleresponse,
											serviceType, cityCode, cityName,
											excludedcity, ruleresponsetemplate,
											responsetype, userquestion, currentnode);
								}

							}

						}

					}

				}

			}
		} else {// 全行业用户
			if (!"".equals(city) && city != null) {
				cityCode = city;
				cityName = cityCodeToCityName.get(cityCode);
			}

			// 执行SQL语句，并返回相应的数据源
			b = CommonLibInteractiveSceneDAO.isExitSceneRules(scenariosid,
					conditions, ruletype, ruleresponse, weight, cityCode,
					responsetype, service, abs);
			// 判断数据源不为null且含有数据
			if (b) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将数据已存在信息放入jsonObj的msg对象中
				jsonObj.put("msg", "规则已存在!");
				return jsonObj;
			} else {
				c = CommonLibInteractiveSceneDAO.updateSceneRules(user,serviceType,scenariosid,
						scenariosName,conditions, weight, ruletype, ruleresponse, ruleid,
						service, abs, responsetype, ruleresponsetemplate,
						cityCode, cityName, excludedcity, questionobject, standardquestion,
						userquestion, currentnode);

			}

		}

		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将更新成功放入jsonObj的msg对象中
			jsonObj.put("msg", "修改成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将更新失败放入jsonObj的msg对象中
			jsonObj.put("msg", "修改失败!");
		}

		return jsonObj;
	}

	/**
	 * 通过配置文件获取这是什么版本,是用来空答案页面的多条件查询按钮是否显示
	 * 
	 * @return
	 */
	public static Object GetVersion() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 将配置文件global.properties中key为version的值放入jsonObj的result对象中
		jsonObj.put("result", getConfigValue.version);
		return jsonObj;
	}

	/**
	 * 将回复模板保存到答案表中，并返回相应的信息
	 * 
	 * @param answer参数回复模板
	 * @param kbanswerid参数kbanswerid
	 * @return 保存后返回的json串
	 */
	public static Object SaveModel(String answer, String kbanswerid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = UserOperResource.saveModel(answer, kbanswerid);
		// 判断事务处理的结果
		if (c > 0) {
			// 事务处理成功
			// 将成功放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!");
		} else {
			// 事务处理失败
			// 将失败放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	/**
	 * 为数据和规则提供导入功能
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param filename参数文件名称
	 * @param importtype参数导入类型
	 *            0-数据 1-缺失补全规则 2-问题要素冲突判断规则
	 * @return 导入后返回的json串
	 */
	public static Object ImportExcel(String kbdataid, String kbcontentid,
			String filename, String importtype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取上传文件的路径
		String pathName = Database.getJDBCValues("fileDirectory")
				+ File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取导入文件的类型
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename
				.substring(filename.lastIndexOf(".") + 1);
		// 定义当前文件后得到的集合
		List<List<Object>> comb = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			comb = read2003Excel(file);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			comb = read2007Excel(file);
		}
		// 删除文件
		file.delete();
		// 插入数据库绑定事务处理的结果
		int count = insert(comb, kbdataid, kbcontentid, importtype);
		// 判断事务处理结果
		if (count > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将导入成功的信息放入jsonObj的msg对象中
			jsonObj.put("msg", "导入成功!");
		} else {
			// 导入文件失败，将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将导入失败的信息放入jsonObj的msg对象中
			jsonObj.put("msg", "导入失败!");
		}
		return jsonObj;
	}

	/**
	 * 根据导入类型分别插入到不同的数据表中
	 * 
	 * @param combition参数问题要素
	 *            、优先级(importtype不为0时才有)、答案组成的集合
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param importtype参数导入类型
	 *            (0:数据;1:缺失补全规则;2:问题要素冲突判断规则)
	 * @return 存入数据库表中的绑定事务处理的结果
	 */
	public static int insert(List<List<Object>> combition, String kbdataid,
			String kbcontentid, String importtype) {
		// 定义问题要素的集合
		List<String> lst = new ArrayList<String>();
		// 定义多条SQL语句集合对应的绑定参数集合
		List<List<?>> lstParam = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历问题要素、优先级(importtype不为0时才有)、答案组成的集合
		for (int i = 0; i < combition.size(); i++) {
			// 定义第i个集合
			List<Object> comb = combition.get(i);
			// 定义组合集合中的问题要素的个数
			int c = 0;
			// 判断导入类型是数据
			if (NewEquals.equals("0",importtype)) {
				// 获取问题要素的个数(最后一列是答案，其他的都是问题要素)
				c = comb.size() - 1;
			} else {
				// 获取问题要素的个数(最后两列是优先级和答案，其他的都是问题要素)
				c = comb.size() - 2;
			}
			// 定义问题要素的集合
			List<List<String>> eleval = new ArrayList<List<String>>();
			// 获取问题要素信息，集合的第1个就是问题要素信息
			if (i == 0) {
				// 循环遍历问题要素集合
				for (int j = 0; j < c; j++) {
					// 获取每一个问题要素
					String ele = comb.get(j) != null ? comb.get(j).toString()
							: "";
					// 将问题要素放入集合中
					lst.add(ele);
				}
			} else {
				// 循环遍历集合中除了第一个，其他剩余的集合
				// 循环遍历问题要素集合
				for (int j = 0; j < c; j++) {
					// 获取每一个问题要素
					String con = comb.get(j) != null ? comb.get(j).toString()
							: "";
					// 将每一个问题要素按照||来拆分
					String[] condition = con.split("\\|\\|");
					// 将拆分得到的集合放入问题要素的集合
					eleval.add(Arrays.asList(condition));
				}
				// 定义规则的优先级
				String ruleweight = "";
				// 定义答案
				String returntxt = "";
				// 判断导入类型是数据
				if (NewEquals.equals("0",importtype)) {
					// 集合的最后一个是答案
					returntxt = comb.get(c) != null ? comb.get(c).toString()
							: "";
				} else {
					// 集合倒数第二个是规则优先级
					ruleweight = comb.get(c) != null ? comb.get(c).toString()
							: "";
					// 集合的最后一个是答案
					returntxt = comb.get(c + 1) != null ? comb.get(c + 1)
							.toString() : "";
				}
				// 根据不同的导入类型来获取不同的SQL语句对应的绑定参数集合，这些参数集合是通过笛卡尔差乘得到的
				lstParam.addAll(CombBedSores(eleval, ruleweight, returntxt,
						kbdataid, kbcontentid, importtype));
			}
		}
		// 判断组合集合的个是否大于0
		if (combition.size() > 0) {
			// 定义SQL语句
			StringBuilder sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 查询当前问题要素的优先级
			sql
					.append("select * from queryelement where kbdataid=? and kbcontentid=? and name in (");
			// 绑定摘要id参数
			lstpara.add(kbdataid);
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 循环遍历问题要素数组
			for (int i = 0; i < lst.size(); i++) {
				if (i != lst.size() - 1) {
					// 判断最后一个不要添加逗号，其他的都要加上逗号
					sql.append("?,");
				} else {
					// 最后一个不加逗号
					sql.append("?");
				}
				// 绑定相应的参数
				lstpara.add(lst.get(i));
			}
			sql.append(") order by weight asc");

			try {
				// 执行SQL语句，获取数据源
				Result rs = Database.executeQuery(sql.toString(), lstpara
						.toArray());
				// 定义优先级的数组，大小是问题要素的集合的大小
				String[] weightArr = new String[lst.size()];
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 判断查询的优先级的个数与导入文件的问题要素的个数是否相等
					if (rs.getRowCount() == lst.size()) {
						// 循环遍历数据源
						for (int i = 0; i < rs.getRowCount(); i++) {
							// 将优先级放入优先级数组中
							weightArr[i] = rs.getRows()[i].get("weight")
									.toString();
						}
					} else {
						// 如果不相等，将优先级数组中的值，从1至数组的长度
						for (int i = 0; i < weightArr.length; i++) {
							weightArr[i] = String.valueOf(i + 1);
						}
					}
				} else {
					// 如果查询不到，将优先级数组中的值，从1至数组的长度
					for (int i = 0; i < weightArr.length; i++) {
						weightArr[i] = String.valueOf(i + 1);
					}
				}
				// 判断导入类型是数据
				if (NewEquals.equals("0",importtype)) {
					// 定义SQL语句
					sql = new StringBuilder();
					// 插入数据表的SQL语句
					sql
							.append("insert into conditioncombtoreturntxt (combitionid,");
					// 循环遍历优先级数组
					for (int i = 0; i < weightArr.length; i++) {
						// 判断第i个值不为null且不为空
						if (weightArr[i] != null && !"".equals(weightArr[i])) {
							// 将插入语句的condition几补充完整
							sql.append("condition" + weightArr[i] + ",");
						}
					}
					// 将插入语句补充完整
					sql
							.append("kbdataid,kbcontentid,status,returntxttype,returntxt) values (conditioncombtoreturntxt_seq.nextval,");
					// 循环遍历优先级数组
					for (int i = 0; i < weightArr.length; i++) {
						// 判断第i个值不为null且不为空
						if (weightArr[i] != null && !"".equals(weightArr[i])) {
							// 将插入语句的condition几补充完整
							sql.append("?,");
						}
					}
					// 将插入语句补充完整
					sql.append("?,?,0,0,?)");
				} else {
					// 定义SQL语句
					sql = new StringBuilder();
					// 插入规则表的SQL语句
					sql.append("insert into scenerules (ruleid,");
					// 循环遍历优先级数组
					for (int i = 0; i < weightArr.length; i++) {
						// 判断第i个值不为null且不为空
						if (weightArr[i] != null && !"".equals(weightArr[i])) {
							// 将插入语句的condition几补充完整
							sql.append("condition" + weightArr[i] + ",");
						}
					}
					// 将插入语句补充完整
					sql
							.append("kbdataid,kbcontentid,ruletype,weight,ruleresponse) values (scenerules_seq.nextval,");
					// 循环遍历优先级数组
					for (int i = 0; i < weightArr.length; i++) {
						// 判断第i个值不为null且不为空
						if (weightArr[i] != null && !"".equals(weightArr[i])) {
							// 将插入语句的condition几补充完整
							sql.append("?,");
						}
					}
					// 将插入语句补充完整
					sql.append("?,?," + (Integer.parseInt(importtype) - 1)
							+ ",?,?)");
				}
				// 定义多条SQL语句
				List<String> lstSql = new ArrayList<String>();
				// 循环遍历多条SQL语句对应的绑定参数集合
				for (int i = 0; i < lstParam.size(); i++) {
					lstSql.add(sql.toString());
				}
				// 执行SQL语句，绑定事务处理，返回事务处理的结果
				return Database.executeNonQueryTransaction(lstSql, lstParam);
			} catch (SQLException e) {
				e.printStackTrace();
				// 出现错误，直接返回0
				return 0;
			}
		} else {
			// 组合集合的个数为空，直接返回0
			return 0;
		}
	}

	/**
	 * 产生笛卡尔积组合
	 * 
	 * @param crossArgs参数问题要素集合
	 * @param ruleweight参数规则类型
	 * @param returntxt参数答案
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param importtype参数导入类型
	 * @return 笛卡尔积组合结果
	 */
	public static List<List<String>> CombBedSores(List<List<String>> crossArgs,
			String ruleweight, String returntxt, String kbdataid,
			String kbcontentid, String importtype) {
		// 计算出笛卡尔积行数
		int rows = crossArgs.size() > 0 ? 1 : 0;
		// 循环遍历集合中每一个集合
		for (List<String> data : crossArgs) {
			// 将集合中的每一个的个数相乘，得到最后笛卡尔积的个数
			rows *= data.size();
		}
		// 笛卡尔积索引记录
		int[] record = new int[crossArgs.size()];
		// 定义绑定参数集合
		List<List<String>> results = new ArrayList<List<String>>();
		// 产生笛卡尔积
		for (int i = 0; i < rows; i++) {
			// 定义一个集合，用来存放绑定参数
			List<String> row = new ArrayList<String>();
			// 生成笛卡尔积的每组数据
			for (int index = 0; index < record.length; index++) {
				// 获取crossArgs第index个集合的第record[index]个值
				String s = crossArgs.get(index).get(record[index]);
				// 判断值是否为空，空添加null，否则直接添加到集合中
				if ("".equals(s)) {
					row.add(null);
				} else {
					row.add(s);
				}
			}
			// 将摘要id放入集合中
			row.add(kbdataid);
			// 将kbcontentid放入集合中
			row.add(kbcontentid);
			// 判断导入类型不是数据
			if (!NewEquals.equals("0",importtype)) {
				// 将规则优先级放入集合中,为空就填入0
				row.add("".equals(ruleweight) ? "0" : ruleweight);
			}
			// 将答案放入集合中
			row.add(returntxt);
			// 将集合放入集合中
			results.add(row);
			// 产生笛卡尔积当前行索引记录.
			crossRecord(crossArgs, record, crossArgs.size() - 1);
		}
		return results;
	}

	/**
	 * 产生笛卡尔积当前行索引记录.
	 * 
	 * @param sourceArgs要产生笛卡尔积的源数据
	 * @param record每行笛卡尔积的索引组合
	 * @param level索引组合的当前计算层级
	 */
	private static void crossRecord(List<List<String>> sourceArgs,
			int[] record, int level) {
		record[level] = record[level] + 1;
		if (record[level] >= sourceArgs.get(level).size() && level > 0) {
			record[level] = 0;
			crossRecord(sourceArgs, record, level - 1);
		}
	}

	/**
	 * 读取 office 2003 excel
	 * 
	 * @param file参数导入文件
	 * @return 读取文件后的集合
	 */
	private static List<List<Object>> read2003Excel(File file) {
		// 定义返回的集合
		List<List<Object>> list = new ArrayList<List<Object>>();
		// 定义每一行组成的集合
		List<Object> param = new ArrayList<Object>();
		try {
			// 将导入的文件变成工作簿对象
			HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
			// 获取工作簿的第一个sheet
			HSSFSheet sheet = hwb.getSheetAt(0);
			// 定义每一个单元格的值变量
			Object value = null;
			// 定义每一行的变量
			HSSFRow row = null;
			// 定义每一个单元格变量
			HSSFCell cell = null;
			// 定义第一行中有多少列
			int count = 0;
			// 读取第一行
			row = sheet.getRow(0);
			// 判断第一行是否为null
			if (row != null) {
				// 第一行不为null，循环变量第一行的每个单元格
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 为null，继续下一个单元格
						continue;
					}
					// 获取第j个单元格的值
					value = cell.getStringCellValue().trim();
					// 将值放入集合中
					param.add(value);
					// 判断第j个单元格的值是否是答案(表示读取到最后一列)
					if ("答案".equals(value)) {
						// 将集合的大小赋值给列数变量
						count = param.size();
						// 跳出循环
						break;
					}
				}
				// 将读取的第一列组成的集合放入集合中
				list.add(param);
			}
			// 读取第一行以下的部分
			// 循环遍历当前sheet的除第一行以下的行数
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				// 获取第i行，赋值给行变量
				row = sheet.getRow(i);
				// 判断第i行是否为null
				if (row == null) {
					// 第i行为null，继续读取下一行
					continue;
				}
				// 定义每一行组成的集合
				param = new ArrayList<Object>();
				// 循环遍历每一行的列数
				for (int j = 0; j < count; j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 将null放入集合中
						param.add(null);
					} else {
						// 第j个单元格不为null，判断当前单元格的类型是什么
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
							// 获取当前单元格的值
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:// 空单元格
							// 将null赋值给当前单元格的值变量
							value = null;
							break;
						default:// 缺省类型
							// 直接转换为字符串
							value = cell.toString();
						}
						// 将当前单元格的值放入集合中
						param.add(value);
					}
				}
				// 将读取的每一列组成的集合放入集合中
				list.add(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误,返回空集合
			// 定义每一行组成的集合
			param = new LinkedList<Object>();
			// 将空的集合放入集合中
			list.add(param);
		}
		return list;
	}

	/**
	 * 读取Office 2007 excel
	 * 
	 * @param file参数导入的文件
	 * @return 读取文件后的集合
	 */
	private static List<List<Object>> read2007Excel(File file) {
		// 定义返回的集合
		List<List<Object>> list = new ArrayList<List<Object>>();
		// 定义每一行组成的集合
		List<Object> param = new ArrayList<Object>();
		try {
			// 将导入的文件变成工作簿对象
			XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
			// 获取工作簿的第一个sheet
			XSSFSheet sheet = xwb.getSheetAt(0);
			// 定义每一个单元格的值变量
			Object value = null;
			// 定义每一行的变量
			XSSFRow row = null;
			// 定义每一个单元格变量
			XSSFCell cell = null;
			// 定义第一行中有多少列
			int count = 0;
			// 读取第一行
			row = sheet.getRow(0);
			// 判断第一行是否为null
			if (row != null) {
				// 第一行不为null，循环变量第一行的每个单元格
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 为null，继续下一个单元格
						continue;
					}
					// 获取第j个单元格的值
					value = cell.getStringCellValue().trim();
					// 将值放入集合中
					param.add(value);
					// 判断第j个单元格的值是否是答案(表示读取到最后一列)
					if ("答案".equals(value)) {
						// 将集合的大小赋值给列数变量
						count = param.size();
						// 跳出循环
						break;
					}
				}
				// 将读取的第一列组成的集合放入集合中
				list.add(param);
			}
			// 读取第一行以下的部分
			// 循环遍历当前sheet的除第一行以下的行数
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				// 获取第i行，赋值给行变量
				row = sheet.getRow(i);
				// 判断第i行是否为null
				if (row == null) {
					// 第i行为null，继续读取下一行
					continue;
				}
				// 定义每一行组成的集合
				param = new ArrayList<Object>();
				// 循环遍历每一行的列数
				for (int j = 0; j < count; j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 将null放入集合中
						param.add(null);
					} else {
						// 第j个单元格不为null，判断当前单元格的类型是什么
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
							// 获取当前单元格的值
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:// 空单元格
							// 将null赋值给当前单元格的值变量
							value = null;
							break;
						default:// 缺省类型
							// 直接转换为字符串
							value = cell.toString();
						}
						// 将当前单元格的值放入集合中
						param.add(value);
					}
				}
				// 将读取的每一列组成的集合放入集合中
				list.add(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误,返回空集合
			// 定义每一行组成的集合
			param = new LinkedList<Object>();
			// 将空的集合放入集合中
			list.add(param);
		}
		return list;
	}

	/**
	 * 将在页面上读取html字符，转换成非html字符存入库中
	 * 
	 * @param content
	 * @return
	 */
	public static String HtmlToString(String content) {
		if (content == null)
			return "";
		String html = content;
		html = html.replaceAll("&", "&amp;");
		html = html.replace("\"", "&quot;");
		html = html.replace("\t", "&nbsp;&nbsp;");// 替换跳格
		html = html.replace(" ", "&nbsp;");// 替换空格
		html = html.replace("<", "&lt;");
		html = html.replaceAll(">", "&gt;");
		return html;
	}

	/**
	 *读取库中内容，转换成html字符，显示在页面上
	 * 
	 * @param html
	 * @return
	 */
	public static String StringToHtml(String html) {
		if (html == null)
			return "";
		String content = html;
		content = content.replaceAll("&gt;", ">");
		content = content.replace("&lt;", "<");
		content = content.replace("&nbsp;", " ");// 替换成空格
		content = content.replace("&nbsp;&nbsp;", "\t");// 替换成跳格
		content = content.replace("&quot;", "\"");
		content = content.replaceAll("&amp;", "&");
		return content;
	}

	/**
	 * 读取数据库，生成Excel文件，返回文件的路径
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbconid
	 * @param exporttype参数导出文件类型
	 *            0-数据 1-缺失补全规则 2-问题要素冲突判断规则
	 * @param abs参数摘要名称
	 * @return 返回生成文件的路径
	 */
	public static Object ExportExcel(String kbdataid, String kbcontentid,
			String exporttype, String abs) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义存放生成Excel文件的每一行内容的集合
		List<String> rowList = new ArrayList<String>();
		// 定义存放生成Excel文件的所有内容的集合
		List<List<String>> elementinfoList = new ArrayList<List<String>>();
		try {
			// 定义存放问题要素对应的优先级的数组
			String[] weightArr = null;
			// 定义查询问题要素的SQL语句
			String sql = "select * from queryelement where kbdataid=? and kbcontentid=? order by weight asc";
			// 定义绑定参数集合
			List<Object> lstpara = new ArrayList<Object>();
			// 绑定摘要id参数
			lstpara.add(kbdataid);
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null，且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 设置列值数组的长度与查询出来属性名称对应列值的数量一致
				weightArr = new String[rs.getRowCount()];
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 将问题要素名称放入Excel文件的第一行内容的集合中
					rowList.add(rs.getRows()[i].get("name").toString());
					// 给优先级数组赋值
					weightArr[i] = rs.getRows()[i].get("weight").toString();
				}
			}
			// 判断导出文件类型是否是数据
			if (NewEquals.equals("0",exporttype)) {
				// 将答案类型放入第一行内容的集合中
				rowList.add("答案类型");
				// 将状态放入第一行内容的集合中
				rowList.add("状态");
				// 将知识点名称放入第一行内容的集合中
				rowList.add("知识点名称");
				// 将回复文本放入第一行内容的集合中
				rowList.add("回复文本");
			} else {
				// 将规则类型放入第一行内容的集合中
				rowList.add("规则类型");
				// 将规则优先级放入第一行内容的集合中
				rowList.add("规则优先级");
				// 将知识点名称放入第一行内容的集合中
				rowList.add("知识点名称");
				// 将回复内容放入第一行内容的集合中
				rowList.add("回复内容");
			}
			// 将每一行的内容就会放入所有内容的集合中
			elementinfoList.add(rowList);

			// 判断导出文件类型是否是数据
			if (NewEquals.equals("0",exporttype)) {
				// 查询数据的SQL语句
				sql = "select * from conditioncombtoreturntxt where kbdataid=? and kbcontentid=? ";
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 绑定kbcontentid参数
				lstpara.add(kbcontentid);
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql, lstpara.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义存放生成Excel文件的每一行内容的集合
						rowList = new ArrayList<String>();
						// 循环遍历列值数组
						for (int j = 0; j < weightArr.length; j++) {
							// 获取condition的值
							String condition = rs.getRows()[i].get("condition"
									+ weightArr[j]) != null ? rs.getRows()[i]
									.get("condition" + weightArr[j]).toString()
									: "";
							// 将attr的值放入行内容的集合中
							rowList.add(condition);
						}
						// 获取答案类型
						String returntxttype = rs.getRows()[i].get(
								"returntxttype").toString();
						// 判断答案类型
						if (NewEquals.equals("0",returntxttype)) {
							// 将答案类型放入第一行内容的集合中
							rowList.add("普通文本");
						} else {
							// 将答案类型放入第一行内容的集合中
							rowList.add("知识点映射");
						}
						// 获取状态
						String status = rs.getRows()[i].get("status")
								.toString();
						// 判断状态
						if (NewEquals.equals("0",status)) {
							// 将状态放入第一行内容的集合中
							rowList.add("未审核");
						} else {
							// 将状态放入第一行内容的集合中
							rowList.add("已审核");
						}
						// 将知识点名称放入第一行内容的集合中
						rowList.add(abs);
						// 将回复文本放入第一行内容的集合中
						rowList
								.add(rs.getRows()[i].get("returntxt") != null ? rs
										.getRows()[i].get("returntxt")
										.toString()
										: "");
						// 将行内容的集合放入全内容集合中
						elementinfoList.add(rowList);
					}
				}
			} else {
				// 查询规则的SQL语句
				sql = "select * from scenerules where kbdataid=? and kbcontentid=? and ruletype=? ";
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 绑定kbcontentid参数
				lstpara.add(kbcontentid);
				// 绑定规则类型参数
				lstpara.add(Integer.parseInt(exporttype) - 1);
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql, lstpara.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义存放生成Excel文件的每一行内容的集合
						rowList = new ArrayList<String>();
						// 循环遍历列值数组
						for (int j = 0; j < weightArr.length; j++) {
							// 获取condition的值
							String condition = rs.getRows()[i].get("condition"
									+ weightArr[j]) != null ? rs.getRows()[i]
									.get("condition" + weightArr[j]).toString()
									: "";
							// 将attr的值放入行内容的集合中
							rowList.add(condition);
						}
						// 获取规则类型
						String ruletype = rs.getRows()[i].get("ruletype")
								.toString();
						// 判断规则类型
						if (NewEquals.equals("0",ruletype)) {
							// 将规则类型放入第一行内容的集合中
							rowList.add("缺失补全规则");
						} else if (NewEquals.equals("1",ruletype)) {
							// 将规则类型放入第一行内容的集合中
							rowList.add("问题要素冲突判断规则");
						} else {
							// 将规则类型放入第一行内容的集合中
							rowList.add("其他规则");
						}
						// 将规则优先级放入第一行内容的集合中
						rowList.add(rs.getRows()[i].get("weight").toString());
						// 将知识点名称放入第一行内容的集合中
						rowList.add(abs);
						// 将回复内容放入第一行内容的集合中
						rowList
								.add(rs.getRows()[i].get("ruleresponse") != null ? rs
										.getRows()[i].get("ruleresponse")
										.toString()
										: "");
						// 将行内容的集合放入全内容集合中
						elementinfoList.add(rowList);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		if (NewEquals.equals("0",exporttype)) {
			pathName = pathName + "数据.xls";
		} else if (NewEquals.equals("1",exporttype)) {
			pathName = pathName + "缺失补全规则.xls";
		} else {
			pathName = pathName + "问题要素冲突判断规则.xls";
		}
		// 调用生成Excel2003的方法，并返回生成Excel文件的路径
		creat2003Excel(elementinfoList, pathName);
		// 定义文件对象
		File file = new File(Database.getJDBCValues("fileDirectory")
				+ File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 创建2003版本的Excel文件
	 * 
	 * @param attrinfo参数要生成文件的集合
	 * @param pathName参数文件路径
	 */
	private static void creat2003Excel(List<List<String>> attrinfo,
			String pathName) {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建 一个excel文档对象
			HSSFSheet sheet = workBook.createSheet();// 创建一个工作薄对象
			HSSFCellStyle style = workBook.createCellStyle();// 创建样式对象
			HSSFFont font = workBook.createFont();// 创建字体对象
			font.setFontHeightInPoints((short) 12);// 设置字体大小
			style.setFont(font);// 将字体加入到样式对象
			// 产生表格标题行
			for (int i = 0; i < attrinfo.size(); i++) {
				HSSFRow row = sheet.createRow(i);
				List<String> c = attrinfo.get(i);
				for (int j = 0; j < c.size(); j++) {
					HSSFCell cell = row.createCell(j);// 创建单元格
					cell.setCellValue(c.get(j));// 写入当前值
					cell.setCellStyle(style);// 应用样式对象
				}
			}
			FileOutputStream os = new FileOutputStream(Database
					.getJDBCValues("fileDirectory")
					+ File.separator + pathName);
			workBook.write(os);// 将文档对象写入文件输出流
			os.close();// 关闭文件输出流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 构造场景树
	 * 
	 * @param scenariosid
	 *            场景id
	 * @param servicetype
	 *            行业
	 * @return Object
	 */
	public static Object createInteractiveSceneTree(String scenariosid, String citySelect) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取用户ID
		String userID = user.getUserID();
		
		try {
			citySelect =  java.net.URLDecoder.decode(citySelect,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibInteractiveSceneDAO.createInteractiveSceneTreeNew(
				scenariosid, serviceType,citySelect,userID);
//		List<String> cityList = new ArrayList<String>();
//		cityList.add(citySelect);
//		List<Map<String,String>> rsList = CommonLibPermissionDAO.getServiceResource(userID,"scenariosrules",serviceType,cityList);
//			
//		if (!rsList.isEmpty()){
//			if (scenariosid != null && !"".equals(scenariosid)){
//				for (Map<String,String> rsMap : rsList){
//					if (NewEquals.equals(rsMap.get("pid"),scenariosid)){
//						JSONObject jsonObj = new JSONObject();
//						jsonObj.put("id", rsMap.get("id"));
//						jsonObj.put("text", rsMap.get("name"));
//						for (Map<String,String> rsMap2 : rsList){
//							if (NewEquals.equals(rsMap2.get("pid"),rsMap.get("id"))){// 有子业务
//								jsonObj.put("cls", "folder");
//								jsonObj.put("leaf", false);
//								jsonObj.put("state", "closed");
//								break;
//							}
//							jsonObj.put("leaf", true);
//						}
//						array.add(jsonObj);
//					}
//				}
//			} else {
//				for (Map<String,String> rsMap : rsList){
//					if (rsMap.get("pid").equals("0")||rsMap.get("pid").equals("0.0")){
//						JSONObject jsonObj = new JSONObject();
//						jsonObj.put("id", rsMap.get("id"));
//						jsonObj.put("text", rsMap.get("name"));
//						for (Map<String,String> rsMap2 : rsList){
//							if (NewEquals.equals(rsMap2.get("pid"),rsMap.get("id"))){// 有子业务
//								jsonObj.put("cls", "folder");
//								jsonObj.put("leaf", false);
//								jsonObj.put("state", "closed");
//								break;
//							}
//							jsonObj.put("leaf", true);
//						}
//						array.add(jsonObj);
//					}
//				}
//			}
//		}
		
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("scenariosid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("name").toString());
				if (CommonLibInteractiveSceneDAO.hasChildNew(sid) == 0) {// 如果没有子业务
					// jsonObj.put("iconCls", "icon-servicehit");
					jsonObj.put("leaf", true);
				} else {
					// jsonObj.put("expanded","true");
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
				}

				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 *添加知识名称
	 * 
	 * @param name
	 *            场景名称
	 *@param knocontent
	 *            知识名称内容
	 *@return
	 *@returnType Object
	 */
	public static Object addKnoName(String name, String knocontent) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		String wordclass = "";
		if (name.endsWith("场景")) {
			wordclass = name + "父子句";
		} else {
			wordclass = name + "场景父子句";
		}
		// //查询当前场景父子句下所有词条别名
		Map<String, Map<String, List<String>>> wordAndSynonym = getWordAndSynonym(wordclass);

		// 封装知识名称对应词条
		Map<String, List<String>> knonameAndCity = getKnoWordAndSynonym(knocontent);
		int c = CommonLibWordDAO.insert(wordAndSynonym, knonameAndCity,
				serviceType, wordclass);

		// //查询知识名称对应词条别名
		// Map<String,Map<String,List<String>>> kno_wordAndSynonym =
		// getSynonymByWordAndCity(knonameAndCity);
		// //比较wordAndSynonym和kno_wordAndSynonym

		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "插入成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "插入失败!");
		}
		return jsonObj;
	}

	// public static String
	// compareAndInsert(Map<String,Map<String,List<String>>>
	// wordAndSynonym,Map<String,Map<String,List<String>>>
	// kno_wordAndSynonym,String name ){
	// for(Map.Entry<String,Map<String,List<String>>>
	// entry:kno_wordAndSynonym.entrySet()){
	// String knoName = entry.getKey();
	// if(!wordAndSynonym.containsKey(knoName)){//当前场景父子句下不包含knname 直接 insert
	//				
	// }
	// }
	//			
	//		
	// return "";
	//		
	// }

	/**
	 *@description 通过词条名称及地市查询其别名
	 *@param knonameAndCity
	 *@return
	 *@returnType Map<String,Map<String,List<String>>>
	 *             例：{2016年双节促销国际漫游数据上网半价优惠活动={ wordclassid=[44627],
	 *             synonym=[[<!
	 *             2016年近类>]*<!双节近类>*<!促销近类>*<!国际漫游近类>*<!数据近类>*<!上网近类
	 *             >*<!一半近类>*<!价格近类>*<!活动近类>,
	 *             [<!2016年近类>]*<!双节近类>*<!国际漫游近类>*<!数据上网近类
	 *             |!上网近类>*<!一半近类>*<!价格近类>*<!优惠近类|!活动近类>,
	 *             [<!2016年近类>]*<!双节近类>*<!
	 *             国际漫游近类>*<!数据上网近类|!上网近类>*<!一半近类>*<!价格近类>*[<!优惠近类|!活动近类>],
	 *             [<!2016
	 *             年近类>]*<!双节近类>*<!国际漫游近类>*<!数据近类>*<!上网近类>*<!一半近类>*<!价格近类>
	 *             *[<!优惠近类|!活动近类>]], city=[150000|620000|610000, 内蒙古|甘肃|陕西] }}
	 */
	public static Map<String, Map<String, List<String>>> getSynonymByWordAndCity(
			Map<String, List<String>> knonameAndCity) {
		Map<String, Map<String, List<String>>> wordAndSynonym = new HashMap<String, Map<String, List<String>>>();
		List<String> tempList = null;
		Map<String, String> tempMap = null;
		Map<String, List<String>> tempMapList = null;
		Result rs = CommonLibWordDAO.getSynonymByWordAndCity(knonameAndCity);
		int rsCount = rs.getRowCount();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rsCount; i++) {
				String word = rs.getRows()[i].get("word").toString();
				String city = knonameAndCity.get(word).get(0);
				String cityName = knonameAndCity.get(word).get(1);
				// String city = rs.getRows()[i].get("city")==null ? "":
				// rs.getRows()[i].get("city").toString().replace(",", "|");
				// String cityName = rs.getRows()[i].get("cityname")==null ? "":
				// rs.getRows()[i].get("cityname").toString().replace(",", "|");
				String synonym = rs.getRows()[i].get("syn") == null ? "" : rs
						.getRows()[i].get("syn").toString();
				String wordclassid = rs.getRows()[i].get("wordclassid") == null ? ""
						: rs.getRows()[i].get("wordclassid").toString();
				String wordid = rs.getRows()[i].get("wordid") == null ? "" : rs
						.getRows()[i].get("wordid").toString();
				if (wordAndSynonym.containsKey(word)) {
					Map<String, List<String>> synonymInfo = wordAndSynonym
							.get(word);
					List<String> synonymNameList = synonymInfo.get("synonym");
					if (!synonymNameList.contains(synonym)) {
						synonymNameList.add(synonym);
						synonymInfo.put("synonym", synonymNameList);
						wordAndSynonym.put(word, synonymInfo);
					}
				} else {
					tempMapList = new HashMap<String, List<String>>();
					tempList = new ArrayList<String>();

					tempList.add(city);
					tempList.add(cityName);
					tempMapList.put("city", tempList);

					tempList = new ArrayList<String>();
					tempList.add(synonym);
					tempMapList.put("synonym", tempList);

					tempList = new ArrayList<String>();
					tempList.add(wordclassid);
					tempMapList.put("wordclassid", tempList);

					tempList = new ArrayList<String>();
					tempList.add(wordid);
					tempMapList.put("wordid", tempList);

					wordAndSynonym.put(word, tempMapList);
				}
			}
		}

		return wordAndSynonym;

	}

	/**
	 *@description 通过词类名称获取词条别名字典
	 *@param wordclass
	 *            词类名称
	 *@return
	 *@returnType Map<String,Map<String,List<String>>>
	 */
	public static Map<String, Map<String, List<String>>> getWordAndSynonym(
			String wordclass) {
		Map<String, Map<String, List<String>>> wordAndSynonym = new HashMap<String, Map<String, List<String>>>();
		List<String> tempList = null;
		Map<String, String> tempMap = null;
		Map<String, List<String>> tempMapList = null;

		Result rs = CommonLibWordDAO.getWordByWordclass(wordclass);
		int rsCount = rs.getRowCount();
		if (rs != null && rsCount > 0) {
			for (int i = 0; i < rsCount; i++) {
				String word = rs.getRows()[i].get("word") == null ? "" : rs
						.getRows()[i].get("word").toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs
						.getRows()[i].get("city").toString().replace(",", "|");
				String cityName = rs.getRows()[i].get("cityname") == null ? ""
						: rs.getRows()[i].get("cityname").toString().replace(
								",", "|");
				String synonym = rs.getRows()[i].get("syn") == null ? "" : rs
						.getRows()[i].get("syn").toString();
				String wordclassid = rs.getRows()[i].get("wordclassid") == null ? ""
						: rs.getRows()[i].get("wordclassid").toString();
				String wordid = rs.getRows()[i].get("wordid") == null ? "" : rs
						.getRows()[i].get("wordid").toString();
				if (wordAndSynonym.containsKey(word)) {
					Map<String, List<String>> synonymInfo = wordAndSynonym
							.get(word);
					List<String> synonymNameList = synonymInfo.get("synonym");
					if (!synonymNameList.contains(synonym)) {
						synonymNameList.add(synonym);
						synonymInfo.put("synonym", synonymNameList);
						wordAndSynonym.put(word, synonymInfo);
					}
				} else {
					tempMapList = new HashMap<String, List<String>>();
					tempList = new ArrayList<String>();

					tempList.add(city);
					tempList.add(cityName);
					tempMapList.put("city", tempList);

					tempList = new ArrayList<String>();
					tempList.add(synonym);
					tempMapList.put("synonym", tempList);

					tempList = new ArrayList<String>();
					tempList.add(wordclassid);
					tempMapList.put("wordclassid", tempList);

					tempList = new ArrayList<String>();
					tempList.add(wordid);
					tempMapList.put("wordid", tempList);

					wordAndSynonym.put(word, tempMapList);
				}
			}
		}

		return wordAndSynonym;

	}

	/**
	 *@description 通过知识名称获取词条别名字典
	 *@param knocontent
	 *@return
	 *@returnType Map<String,List<String>>
	 *             例：{2016年双节促销国际漫游数据上网半价优惠活动=[150000|620000|610000|230000,
	 *             内蒙古|甘肃|陕西|黑龙江]}
	 */
	public static Map<String, List<String>> getKnoWordAndSynonym(
			String knocontent) {
		String knocontenArry[] = knocontent.split("&&");
		Map<String, List<String>> knonameAndcity = new HashMap<String, List<String>>();
		List<String> tempList = null;
		for (int i = 0; i < knocontenArry.length; i++) {
			String lineKnoContent = knocontenArry[i];
			if ("".equals(lineKnoContent)) {
				continue;
			}
			String lineKnoContentArry[] = lineKnoContent.split("@#@");
			String knoname = lineKnoContentArry[0];
			String cityName = lineKnoContentArry[2];
			String cityCode = lineKnoContentArry[3];
			if (knonameAndcity.containsKey(knoname)) {
				tempList = new ArrayList<String>();
				String _cityCode = knonameAndcity.get(knoname).get(0);
				String _cityName = knonameAndcity.get(knoname).get(1);
				if (!_cityCode.contains(cityCode)) {
					_cityCode = _cityCode + "|" + cityCode;
					_cityName = _cityName + "|" + cityName;
					tempList.add(_cityCode);
					tempList.add(_cityName);
					knonameAndcity.put(knoname, tempList);
				}

			} else {
				tempList = new ArrayList<String>();
				tempList.add(cityCode);
				tempList.add(cityName);
				knonameAndcity.put(knoname, tempList);
			}
		}
		return knonameAndcity;

	}

	/**
	 *添加场景
	 * 
	 * @param parentid
	 *            父节点ID
	 *@param name
	 *            场景名称
	 *@return
	 *@returnType Object
	 */
	public static Object addMenu(String parentid, String name) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();

		if (CommonLibInteractiveSceneDAO.isExistSceneName(name, serviceType,parentid) > 0) {// 判断是否存在相同名称场景
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景名称已存在!");
			return jsonObj;
		}
		String sid = "";
		String scenarioselementid = "";
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		// 获得主键ID
		if (GetConfigValue.isOracle) {
//			sid = ConstructSerialNum.GetOracleNextValNew("seq_scenarios_id",
			sid = ConstructSerialNum.GetOracleNextValNew("SEQ_SERVICE_ID",
					bussinessFlag);
//					bussinessFlag);
			scenarioselementid = ConstructSerialNum.GetOracleNextValNew(
					"seq_scenarioselement_id", bussinessFlag);
		} else if (GetConfigValue.isMySQL) {
//			sid = ConstructSerialNum.getSerialIDNew("scenarios", "scenariosid",
//					bussinessFlag);
			sid = ConstructSerialNum.getSerialIDNew("service", "serviceid",
							bussinessFlag);
			scenarioselementid = ConstructSerialNum.getSerialIDNew(
					"scenarioselement", "scenarioselementid", bussinessFlag);
		}
		String wordclass = "";
		if (name.endsWith("场景")) {
			wordclass = name + "父子句";
		} else {
			wordclass = name + "场景父子句";
		}
		int c = -1;

		// if(CommonLibWordclassDAO.exist(wordclass)){//如果场景名称父子句已存在，插入场景及场景元素：知识名称
		// c = CommonLibInteractiveSceneDAO.insertSceneName(sid, parentid,
		// name, serviceType,wordclass);
		// }else{//反之，插入场景父子句、场景及场景元素：知识名称
		// 插入数据
		c = CommonLibInteractiveSceneDAO.insertSceneInfo(user,sid, parentid, name, serviceType, wordclass);
		// }

		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			jsonObj.put("id", sid);
			jsonObj.put("success", true);
			jsonObj.put("msg", "插入成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "插入失败!");
		}
		return jsonObj;
	}

	/**
	 *修改场景文档名称
	 * 
	 * @param scenariosid
	 *            场景ID
	 *@param name
	 *            文档名称
	 *@return
	 *@returnType Object
	 */
	public static Object updateDocname(String scenariosid, String name) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();

		// 插入数据
		int r = CommonLibInteractiveSceneDAO.updateDocname(scenariosid, name);

		// 判断事务处理结果
		if (r > 0) {
			String sysPath = System.getProperty("user.dir");
			String path = "";
			if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
				path = sysPath.substring(0, sysPath.lastIndexOf("\\"))
						+ File.separator + "webapps" + File.separator + "KM"
						+ File.separator + "scenariosdoc";
			} else {
				if (sysPath.endsWith("\\")) {
					path = sysPath.substring(0, sysPath.lastIndexOf("\\"))
							+ File.separator + "webapps" + File.separator
							+ "KM" + File.separator + "scenariosdoc";
				} else {
					path = sysPath.substring(0, sysPath.lastIndexOf("/"))
							+ File.separator + "webapps" + File.separator
							+ "KM" + File.separator + "scenariosdoc";
					;
				}
			}

			String a = path + File.separator + scenariosid + File.separator
					+ name;
			String b = path + File.separator + scenariosid + File.separator
					+ name.split("\\.")[0] + ".html";
			String c = path + File.separator + scenariosid;
			try {
				WordToHtml.convert2Html(a, b, c);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "上传成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "上传失败!");
		}
		return jsonObj;
	}

	/**
	 *查询景文档路径
	 * 
	 * @param scenariosid
	 *            场景ID
	 *@return
	 *@returnType Object
	 */
	public static Object getDocname(String scenariosid) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();

		// 插入数据
		String docname = CommonLibInteractiveSceneDAO.getDocname(scenariosid);

		// 判断事务处理结果
		if (!"".equals(docname) && docname != null) {
			String path = "../scenariosdoc/" + scenariosid + "/"
					+ docname.split("\\.")[0] + ".html";
			;
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("path", path);
			jsonObj.put("msg", "查找成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "文档或不存在!");
		}
		return jsonObj;
	}

	/**
	 *添加场景业务摘要对应关系
	 * 
	 * @param scenariosid
	 *            场景ID
	 *@param service
	 *            业务名
	 *@param serviceid
	 *            业务ID
	 *@param abstractid
	 *            摘要ID
	 *@param abs
	 *            摘要名
	 *@param userquery
	 *            用户问题
	 *@return
	 *@returnType Object
	 */
	public static Object addScenarios2kbdataRelation(String scenariosid,String name,
			String service, String serviceid, String abstractid, String abs,
			String userquery) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		if (CommonLibInteractiveSceneDAO.isExistSceneRelation(service,
				serviceid, abstractid, abs, userquery) > 0) {// 判断是否存在相同场景业务摘要对应关系
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景业务摘要对应关系已存在!");
			return jsonObj;
		}
		String sid = "";
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		// 获得主键ID
		if (GetConfigValue.isOracle) {
			sid = ConstructSerialNum.GetOracleNextValNew(
					"seq_scenarios2kbdata_id", bussinessFlag);
		} else if (GetConfigValue.isMySQL) {
			sid = ConstructSerialNum.getSerialIDNew("scenarios2kbdata",
					"scenarios2kbdataid", bussinessFlag);
		}
		// 插入数据
		int c = CommonLibInteractiveSceneDAO.insertSceneRelation(user,serviceType,sid,
				scenariosid, name,service, serviceid, abstractid, abs, userquery);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "插入成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "插入失败!");
		}
		return jsonObj;
	}

	/**
	 *删除场景
	 * 
	 *@param scenariosid 场景ID
	 *@param name 		 场景名称
	 */
	public static Object deleteMenu(String scenariosid, String name) {
		scenariosid = NumberUtil.formatSceneId(scenariosid);
		// 定义返回JSON
		JSONObject jsonObj = new JSONObject();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 删除场景
		int c = CommonLibInteractiveSceneDAO.deleteSceneName(user,scenariosid, name, serviceType);
		// 查询实体机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		if(StringUtils.isNotBlank(robotId)) {
			// 删除场景语义对应关系
			MetafieldDao.deleteConfigValue("场景机器人ID对应关系", serviceType, scenariosid + "::" + robotId);
			// 删除实体机器人ID
			MetafieldDao.deleteKey("实体机器人ID配置", robotId);
			// 删除场景地市编码
			MetafieldDao.deleteConfigValue("地市编码配置", robotId);
			// 删除机器人ID词条
			String wordClass = "实体机器人ID父类";
			Result result = CommonLibWordclassDAO.getWordclassID(wordClass);
			if (result != null && result.getRowCount() > 0) {
				String wordClassId = result.getRows()[0].get("wordclassid").toString();
				jsonObj = (JSONObject) ScenariosDAO.listPagingWordItem(scenariosid, wordClassId, wordClass, robotId, 1, 10);
				if(jsonObj.getIntValue("total") > 0) {
					String wordId = jsonObj.getJSONArray("rows").getJSONObject(0).getString("wordid");
					ScenariosDAO.deleteWordItem(wordId, wordClassId, robotId);
				}
			}
		}
		// 删除场景词类信息
		if(StringUtils.isNotBlank(name)) {
			// 删除用户回答父类
			String wordClassId = "";
			String wordClass = "sys"+name+"用户回答父类";
			Result result = CommonLibWordclassDAO.getWordclassID(wordClass);
			if (result != null && result.getRowCount() > 0) {
				wordClassId = result.getRows()[0].get("wordclassid").toString();
				CommonLibWordclassDAO.delete(user, wordClassId, wordClass, "", "");
			}
			// 删除上文:节点名父类
			wordClass = "sys"+name+"上文:节点名父类";
			result = CommonLibWordclassDAO.getWordclassID(wordClass);
			if (result != null && result.getRowCount() > 0) {
				wordClassId = result.getRows()[0].get("wordclassid").toString();
				CommonLibWordclassDAO.delete(user, wordClassId, wordClass, "", "");
			}
			// 删除信息收集父类
			wordClass = "sys"+name+"信息收集父类";
			result = CommonLibWordclassDAO.getWordclassID(wordClass);
			if (result != null && result.getRowCount() > 0) {
				wordClassId = result.getRows()[0].get("wordclassid").toString();
				CommonLibWordclassDAO.delete(user, wordClassId, wordClass, "", "");
			}
		}
		// 删除场景问题库
		Result result = ScenariosDAO.getQuestionRootService(serviceType);
		if (result != null && result.getRowCount() > 0) {
			String brand = result.getRows()[0].get("service").toString();
			if(StringUtils.isNotBlank(brand)) {;
				result = CommonLibServiceDAO.getServiceID(name+"问题库", brand);
				if (result != null && result.getRowCount() > 0) {
					String serviceId = result.getRows()[0].get("serviceid").toString();
					List<String> serviceIds = new ArrayList<String>();
					serviceIds.add(serviceId);
					CommonLibServiceDAO.deleteServiceByID(serviceIds);
				}
			}
		}
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 *删除场景业务摘要对应关系
	 * 
	 * @param scenerelationid
	 *            场景关主键ID
	 *@param name
	 *            场景名称
	 *@return
	 *@returnType Object
	 */
	public static Object deleteSceneRelation(String scenerelationid,String abs,String service,String name) {
		// 定义返回json
		JSONObject jsonObj = new JSONObject();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 插入数据
		int c = CommonLibInteractiveSceneDAO
				.deleteSceneRelation(user,serviceType,scenerelationid,abs,service,name);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 *构造业务树
	 * 
	 * @param serviceid
	 *@return
	 *@returnType Object
	 */
	public static Object createServiceTree(String serviceid) {

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = getServiceRoot(serviceType);
		brand = brand.replace("@", ",");
		// 定义返回的json串
		JSONArray array = new JSONArray();
//		Result rs = CommonLibServiceDAO.createServiceTree(serviceid, brand);
//		if (rs != null && rs.getRowCount() > 0) {
//			for (int i = 0; i < rs.getRowCount(); i++) {
//				String sid = rs.getRows()[i].get("serviceid").toString();
//				JSONObject jsonObj = new JSONObject();
//				jsonObj.put("id", sid);
//				jsonObj.put("text", rs.getRows()[i].get("service").toString());
//				if (CommonLibKbdataAttrDAO.hasChild(sid) == 0) {// 如果没有子业务
//					// jsonObj.put("iconCls", "icon-servicehit");
//					jsonObj.put("leaf", true);
//				} else {
//					// jsonObj.put("expanded","true");
//					jsonObj.put("cls", "folder");
//					jsonObj.put("leaf", false);
//					jsonObj.put("state", "closed");
//				}
//
//				array.add(jsonObj);
//			}
//		}
		String citySelect = "全国";
		List<String> cityList2 = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(), "scenariosrules", "S");
		// 该操作类型用户能够操作的资源
		cityList2 = resourseMap.get("地市");
		citySelect = cityList2.get(0);
		List<String> cityList = new ArrayList<String>();
		cityList.add(citySelect);
		List<Map<String,String>> rsList = CommonLibPermissionDAO.getServiceResource(user.getUserID(),"querymanage",serviceType,cityList);
		
		if (!rsList.isEmpty()){
			if (serviceid != null && !"".equals(serviceid)){
				for (Map<String,String> rsMap : rsList){
					if (NewEquals.equals(rsMap.get("pid"),serviceid)){
						JSONObject jsonObj = new JSONObject();
						jsonObj.put("id", rsMap.get("id"));
						jsonObj.put("text", rsMap.get("name"));
						for (Map<String,String> rsMap2 : rsList){
							if (NewEquals.equals(rsMap2.get("pid"),rsMap.get("id"))){// 有子业务
								jsonObj.put("cls", "folder");
								jsonObj.put("leaf", false);
								jsonObj.put("state", "closed");
								break;
							}
							jsonObj.put("leaf", true);
						}
						array.add(jsonObj);
					}
				}
			} else {
				for (Map<String,String> rsMap : rsList){
					if (rsMap.get("pid").equals("0")||rsMap.get("pid").equals("0.000")){
						JSONObject jsonObj = new JSONObject();
						jsonObj.put("id", rsMap.get("id"));
						jsonObj.put("text", rsMap.get("name"));
						for (Map<String,String> rsMap2 : rsList){
							if (NewEquals.equals(rsMap2.get("pid"),rsMap.get("id"))){// 有子业务
								jsonObj.put("cls", "folder");
								jsonObj.put("leaf", false);
								jsonObj.put("state", "closed");
								break;
							}
							jsonObj.put("leaf", true);
						}
						array.add(jsonObj);
					}
				}
			}
		}
		return array;

	}

	/**
	 *通过四层接口串获得业务根
	 * 
	 * @param industryOrganizationApplication
	 *            四层接口串
	 *@return
	 *@returnType String
	 */
	public static String getServiceRoot(String industryOrganizationApplication) {
		StringBuilder sb = new StringBuilder();
		List<String> paras = new ArrayList<String>();
		Result res = null;
		String ioaArray[] = industryOrganizationApplication.split("->");
		res = UserOperResource.getServiceRoot(ioaArray[0], ioaArray[1],
				ioaArray[2]);
		String serviceroot = res.getRows()[0].get("serviceroot").toString();
		String servicearray[] = serviceroot.split("\\|");
		String serviceString = "";
		for (int s = 0; s < servicearray.length; s++) {
			if (!servicearray[s].equals(ioaArray[1]+"场景")){
			serviceString += "'" + servicearray[s] + "'@";
			}
		}
		serviceString = serviceString.substring(0, serviceString.length() - 1);
		return serviceString;
	}

	/**
	 * 
	 * 
	 * @param serviceid
	 *            业务id
	 * @return
	 */
	public static Object getMenuitemsInfo(String scenariosid, String weight) {
		if ("".equals(scenariosid)) {
			return "";
		}
		Result rs = CommonLibInteractiveSceneDAO
				.getColumnByScenariosidAndWeight(scenariosid, weight);
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("name", rs.getRows()[0].get("name").toString());
			jsonObj.put("wordclassid",
					rs.getRows()[0].get("wordclassid") == null ? "" : rs
							.getRows()[0].get("wordclassid").toString());
			jsonObj.put("interpat",
					rs.getRows()[0].get("interpat") == null ? ""
							: rs.getRows()[0].get("interpat").toString());
			jsonObj.put("itemmode",
					rs.getRows()[0].get("itemmode") == null ? ""
							: rs.getRows()[0].get("itemmode").toString());
		}

		return jsonObj;
	}

	/**
	 * 构造摘要下拉框
	 * 
	 * @param serviceid
	 *            业务id
	 * @return
	 */
	public static Object createAbstractCombobox(String serviceid) {
		if ("".equals(serviceid)) {
			return "";
		}
		Result rs = CommonLibKbDataDAO.getAbstractByServiceid(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("kbdataid").toString());
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString());
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 *@description 构造查场景下拉列表
	 *@return
	 *@returnType Object
	 */
	public static Object createScenariosCombobox(String citySelect) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibInteractiveSceneDAO
				.getScenariosByserviceType(serviceType,citySelect);
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String city = rs.getRows()[i].get("city").toString().split(",")[0];
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("serviceid").toString());
//				jsonObj.put("id", rs.getRows()[i].get("scenariosid").toString());
//				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				jsonObj.put("text", rs.getRows()[i].get("service").toString() + "--" + cityCodeToCityName.get(city));
//				jsonObj.put("text", rs.getRows()[i].get("name").toString());
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 * 构造词类下标准词
	 * 
	 * @param wordclassid
	 *            词类ID
	 * 
	 * @return
	 */
	public static Object createInteractiveOptionsCombobox(String wordclassid) {
		if ("".equals(wordclassid)) {
			return "";
		}
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		String cityCodeStr = "";
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				return array;
			}
			cityCodeStr = cityList.get(0);
		} else {
			cityCodeStr = "全国";
		}

		Result rs = CommonLibWordDAO.getWordByWordclassid2(wordclassid,
				cityCodeStr);

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String word = rs.getRows()[i].get("word") == null ? "" : rs
						.getRows()[i].get("word").toString();
				jsonObj.put("id", word);
				jsonObj.put("text", word);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 * 构造场景下拉框
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return
	 */
	public static Object createColumnCombobox(String scenariosid) {
		if ("".equals(scenariosid)) {
			return "";
		}
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			cityList = resourseMap.get("地市");
		}
		if (cityList == null) {
			return array;
		}

		Result rs = CommonLibInteractiveSceneDAO
				.getColumnByScenariosid(scenariosid);

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();

				String isshare = rs.getRows()[i].get("isshare").toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs
						.getRows()[i].get("city").toString();

				// 判断是否满足当前用户city的列信息
				if ("".equals(city)) {
					if ("否".equals(isshare)) {
						if (!"全行业".equals(customer)) {

							jsonObj.put("id", rs.getRows()[i].get(
									"scenarioselementid").toString());
							jsonObj.put("text", rs.getRows()[i].get("name")
									.toString());
							array.add(jsonObj);

						}
					}
				} else {
					if (!"全行业".equals(customer)) {
						if (!containsOneCity(city, cityList.get(0))) {
							jsonObj.put("id", rs.getRows()[i].get(
									"scenarioselementid").toString());
							jsonObj.put("text", rs.getRows()[i].get("name")
									.toString());
							array.add(jsonObj);
						}
					}
				}

			}
		}

		return array;
	}

	
	/**
	 * 获取业务表下属性名对应的内容
	 * @param serviceid
	 * @param column
	 * @return
	 */
	public static Object getAttrValues(String serviceid, String column,String city){
		if(StringUtils.isEmpty(serviceid) && StringUtils.isEmpty(column)){
			return getAttrValuesAll(city);
		}
		
		Result rs  =null;
		if("all".equals(city)){
			rs = CommonLibServiceAttrDao.selectColumnValue(serviceid,column);
		}else{
			String newCity = city.split(",")[0].substring(0,2);
			rs = CommonLibServiceAttrDao.selectColumnValue(serviceid,column,newCity);
		}
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String attrvalue = rs.getRows()[i].get("attr").toString();
				jsonObj.put("id", attrvalue);
				jsonObj.put("text", attrvalue);
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 获取所有业务表下属性名对应的内容
	 * @param city
	 * @return
	 */
	public static Object getAttrValuesAll(String city){
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("模板业务根对应配置",serviceType);
		String brand = rs.getRows()[0].get("name").toString();
		// 获取所有信息表
		rs = CommonLibServiceDAO.getServiceInfoName(brand);
		
		Result colRs = null;
		Result attrValues = null;
		// 定义返回的json串
		JSONArray array = new JSONArray();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String service = rs.getRows()[i].get("service").toString();
				String serviceid = rs.getRows()[i].get("serviceid").toString();
				
				// 获取所有属性
				colRs = CommonLibServiceAttrDao.getServiceAttributions2(serviceid);
				if(colRs != null && colRs.getRowCount() > 0){
					for(int j = 0; j < colRs.getRowCount(); j++){
						String name = (colRs.getRows()[j].get("name") == null ? "" : colRs.getRows()[j].get("name").toString());
						String semanticskeyword = (colRs.getRows()[j].get("semanticskeyword") == null ? "" : colRs.getRows()[j].get("semanticskeyword").toString());
						if(StringUtils.isNotEmpty(semanticskeyword)){
							name = semanticskeyword;
						}
						String column = (colRs.getRows()[j].get("columnnum") == null ? null : colRs.getRows()[j].get("columnnum").toString());
						if(StringUtils.isNumeric(column)){
							
							// 获取信息表下的所有属性
							if("all".equals(city)){
								attrValues = CommonLibServiceAttrDao.selectColumnValue(serviceid,column);
							}else{
								String newCity = city.split(",")[0].substring(0,2);
								attrValues = CommonLibServiceAttrDao.selectColumnValue(serviceid,column,newCity);
							}
							if (attrValues != null && rs.getRowCount() > 0) {
								for (int k = 0; k < attrValues.getRowCount(); k++) {
									JSONObject jsonObj = new JSONObject();
									String attrvalue = attrValues.getRows()[k].get("attr").toString();
									jsonObj.put("id", attrvalue);
									jsonObj.put("text", attrvalue);
									jsonObj.put("service", service);
									jsonObj.put("serviceid", serviceid);
									jsonObj.put("attrname", name);
									array.add(jsonObj);
								}
							}
						}
					}
					
				}
			}
		}
		return array;
	}
	
	public static Object getAttrName(String serviceid) {
		Result rs = CommonLibServiceAttrDao.getServiceAttributions(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String cm = rs.getRows()[i].get("columnnum").toString();
				String attrName = rs.getRows()[i].get("name").toString();
				Object attrKeyword = rs.getRows()[i].get("semanticskeyword");
				jsonObj.put("id", cm);
				jsonObj.put("text", attrKeyword == null ? attrName : attrKeyword.toString());
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 构造需移除场景列下拉框
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return
	 */
	public static Object createDeleteColumnCombobox(String scenariosid) {
		if ("".equals(scenariosid)) {
			return "";
		}
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		Result rs = CommonLibInteractiveSceneDAO
				.getColumnByScenariosid(scenariosid);
		List<String> cityList = new ArrayList<String>();
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			cityList = resourseMap.get("地市");
			if (cityList == null) {
				return array;
			}

			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject jsonObj = new JSONObject();
					String isshare = rs.getRows()[i].get("isshare").toString();
					String city = rs.getRows()[i].get("city") == null ? "" : rs
							.getRows()[i].get("city").toString();

					// 判断是否满足当前用户city的列信息
					if ("".equals(city)) {
						if ("是".equals(isshare)) {
							jsonObj.put("id", rs.getRows()[i].get(
									"scenarioselementid").toString());
							jsonObj.put("text", rs.getRows()[i].get("name")
									.toString());
							array.add(jsonObj);
						}
					} else {
						if (containsOneCity(city, cityList.get(0))) {
							jsonObj.put("id", rs.getRows()[i].get(
									"scenarioselementid").toString());
							jsonObj.put("text", rs.getRows()[i].get("name")
									.toString());
							array.add(jsonObj);
						}
					}

				}
			}

		} else {

			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject jsonObj = new JSONObject();
					String isshare = rs.getRows()[i].get("isshare").toString();
					String city = rs.getRows()[i].get("city") == null ? "" : rs
							.getRows()[i].get("city").toString();
					jsonObj.put("id", rs.getRows()[i].get("scenarioselementid")
							.toString());
					jsonObj.put("text", rs.getRows()[i].get("name").toString());
					array.add(jsonObj);

				}
			}

		}

		return array;
	}

	/**
	 * 构造信息表信息下拉框
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return
	 */
	public static Object createServiceInfoCombobox(String scenariosid) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = "";
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("模板业务根对应配置",serviceType);
		if (rsConfig != null && rsConfig.getRowCount() > 0){
			brand = rsConfig.getRows()[0].get("name").toString();
		}
		Result rs = CommonLibServiceDAO.getServiceInfoName(brand);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String service = rs.getRows()[i].get("service").toString();
				String serviceid = rs.getRows()[i].get("serviceid").toString();
				jsonObj.put("id", serviceid);
				jsonObj.put("text", service);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 构造模板列下拉框
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return
	 */
	public static Object createTemplateColumnCombobox(String scenariosid) {
		if ("".equals(scenariosid)) {
			return "";
		}
		Result rs = CommonLibServiceAttrDao.getColumnName(scenariosid);
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 * 构造触发动作下拉列表
	 * 
	 * @param scenariosid
	 *            场景ID
	 * @return
	 */
	public static Object createTriggeractionNamecombobox(String scenariosid) {
		if ("".equals(scenariosid)) {
			return "";
		}
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("规则动作谓词", serviceType);
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 *@description 获得的场景元素下拉框数据
	 *@param scenariosid
	 *            场景ID
	 *@return
	 *@returnType Object
	 */
	public static Object createEmentColumnCombobox(String scenariosid) {
		// 定义返回的json串
		JSONArray array = new JSONArray();
		List<String> cityList = new ArrayList<String>();
		String cityCode = "";
		String cityName = "";
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
			}
		}
		Result rs = CommonLibInteractiveSceneDAO.getElementName(scenariosid,
				cityCode);

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String scenarioselementid = rs.getRows()[i].get(
						"scenarioselementid").toString();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", scenarioselementid);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 构造父类词类下拉框
	 * 
	 * @return
	 */
	public static Object createWordclassCombobox() {
		Result rs = CommonLibWordclassDAO.getFWordclass();
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String wordclassid = rs.getRows()[i].get("wordclassid")
						.toString();
				String wordclass = rs.getRows()[i].get("wordclass").toString();
				jsonObj.put("id", wordclassid);
				jsonObj.put("text", wordclass);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 * 获取省份
	 * 
	 * @return json串
	 */
	public static Object getProvince() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode = "";
		Result rs = null;
		if (!"全行业".equals(customer)) {
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			List<String> cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
				if (!cityCode.endsWith("0000")) {// 非省份
					cityCode = cityCode.substring(0, 2) + "0000";
				}
				// 执行SQL语句，获取相应的数据源
				rs = CommonLibQuestionUploadDao.selAllProvince(cityCode);
			}
		} else {
			// 执行SQL语句，获取相应的数据源
			rs = CommonLibQuestionUploadDao.selAllProvince();
		}

		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义json对象
				JSONObject obj = new JSONObject();
				// 生成id对象
				obj.put("id", rs.getRows()[i].get("id"));
				// 生成channel对象
				obj.put("text", rs.getRows()[i].get("province") != null ? rs
						.getRows()[i].get("province").toString() : "");
				// 将删除的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}

		return jsonArr;
	}

	/**
	 * 获取地市
	 * 
	 * @return json串
	 */
	public static Object getCity(String province) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode = "";
		Result rs = null;
		if (!"全行业".equals(customer)) {
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			// 该操作类型用户能够操作的资源
			List<String> cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
				if (!cityCode.endsWith("0000")) {// 非省份
					rs = CommonLibQuestionUploadDao.getOneCity(cityCode);
				} else {
					// 执行SQL语句，获取相应的数据源
					rs = CommonLibQuestionUploadDao.getCity(province);
				}
			}
		} else {
			// 执行SQL语句，获取相应的数据源
			rs = CommonLibQuestionUploadDao.getCity(province);

		}
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义json对象
				JSONObject obj = new JSONObject();
				// 生成id对象
				obj.put("id", rs.getRows()[i].get("id"));
				// 生成channel对象
				obj.put("text", rs.getRows()[i].get("city") != null ? rs
						.getRows()[i].get("city").toString() : "");
				// 将删除的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		return jsonArr;
	}

	/**
	 *@description 测试命中问题
	 *@param question
	 *            问题
	 *@param province
	 *            省份
	 *@param city
	 *            地市
	 *@return
	 *@returnType Object
	 */
	public static Object testHitQuestion(String question, String province,
			String city) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		String servicetype = user.getIndustryOrganizationApplication();
		String url = "";
//		Result rs = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
//		if (rs != null && rs.getRowCount() > 0) {
//			// 获取配置表的ip
//			url = rs.getRows()[0].get("name").toString();
//		}
//		 url = "http://180.153.69.0:8082/NLPAppWS/AnalyzeEnterPort?wsdl";
		if ("".equals(city) || city == null) {
			city = province;
		}
		
		// 渠道
		String channel = "Web";
		String applycode = "wenfa";
		
		Result rs3 = UserOperResource.getConfigValue("问题库批量理解接口入参配置", servicetype);
		if (rs3 != null && rs3.getRowCount() > 0) {
			Map<String ,String> caMap = new HashMap<String, String>();
			for (int i = 0;i < rs3.getRowCount();i++){
				caMap.put(rs3.getRows()[i].get("name").toString().split("->")[0]
				        , rs3.getRows()[i].get("name").toString().split("->")[1]);
			}
			if (caMap.containsKey("applyCode")){
				applycode = caMap.get("applyCode");
			}
			if (caMap.containsKey("channel")){
				channel = caMap.get("channel");
			}
		}
		if("集团".equals(province) || "电渠".equals(province) || "全国".equals(province)){
			province = "默认";
		}
		url = GetLoadbalancingConfig.provinceToUrl.get(province)==null ? "" : GetLoadbalancingConfig.provinceToUrl.get(province).get("简要分析");
		if ( "".equals(url) ){
			Result rs = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
			if (rs != null && rs.getRowCount() > 0) {
				// 获取配置表的ip
				url = rs.getRows()[0].get("name").toString();
			}
		}
		logger.info("*****"+url);
//		Object object = QuestionUploadDao.KAnalyzeByFistResult(userid,
//				servicetype, channel, question, url, province, city, "热点问法测试");
		
		// 标准问理解
		Object object = QuestionUploadDao.KAnalyzeByFistResult(userid, servicetype, channel,
				question, url, null, city, "场景命中问题测试",applycode);

		String answer1 = "";
		JSONObject jsonObj = (JSONObject) object;
		String success = jsonObj.get("success").toString();
		if ("false".equals(success)) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "理解失败");
		} else {
			answer1 = jsonObj.getJSONArray("result").getJSONObject(0)
					.getString("answer");

			// 去除<html>标签
			answer1 = QuestionUploadDao.HtmlText(answer1);

			if (answer1.length() > 950) {
				answer1 = answer1.substring(0, 900) + "...";
			}

			String result = "<b style=\"color:red;\">标准问法理解结果：</b><br>"
					+ answer1;
			jsonObj.put("success", true);
			jsonObj.put("msg", answer1);
		}

		return jsonObj;
	}

	/**
	 *拼接list元素
	 * 
	 * @param list
	 *@param flag
	 *@return
	 *@returnType String
	 */
	public static String listJoin(List<String> list, String flag) {
		String str = "";
		for (int i = 0; i < list.size(); i++) {
			str = str + list.get(i) + flag;
		}
		if (!"".equals(str)) {
			str = str.substring(0, str.lastIndexOf(flag));
		}

		return str;
	}

	/**
	 *@description 获取当前用户customer
	 *@param userid
	 *            用户ID
	 *@return
	 *@returnType Object
	 */
	public static Object getCustomer(String userid, String ioa) {
		JSONObject jsonObj = new JSONObject();
		// Object sre = GetSession.getSessionByKey("accessUser");
		// User user = (User) sre;
		User user = UserManager.constructLoginUser(userid, ioa);
		new BaseAction().session.put("accessUser", user);
		jsonObj.put("customer", user.getCustomer());
		return jsonObj;
	}

	/**
	 *@description 上移规则优先级
	 *@param ruletype
	 *            规则类型
	 *@param city
	 *            地市编码
	 *@param weight
	 *            优先级
	 *@param scenariosid
	 *            场景ID
	 *@return
	 *@returnType Object
	 */
	public static Object moveRule(String ruletype, String city, String weight,
			String scenariosid, String moveway) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		String cityCode = "";
		String cityName = "";
		if ( city != null && !"".equals(city)) {
			cityCode = city;
		} else {
			if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "scenariosrules", "S");
				// 该操作类型用户能够操作的资源
				cityList = resourseMap.get("地市");
				if (cityList != null) {
					city = cityList.get(0);
					cityCode = cityList.get(0);
					cityName = cityCodeToCityName.get(cityList.get(0));
					if (cityCode.endsWith("0000")) {// 判断地市信息是否是省级
						cityCode = cityCode.replace("0000", "");
					}
				}

			}
		}

		List<String> list = getRuleWeight(ruletype, city, scenariosid);
		if (moveway != null && "top".equals(moveway)){// 规则置顶操作
			if ("0".equals(list.get(0))){
				jsonObj.put("success", false);
				jsonObj.put("msg", "置顶规则已存在！");
			} else {
				int count = CommonLibInteractiveSceneDAO.setTopRule(weightToRuleid.get(weight));
				if (count > 0) {
					jsonObj.put("success", true);
					jsonObj.put("msg", "规则置顶成功！");
				} else {
					jsonObj.put("success", false);
					jsonObj.put("msg", "规则置顶失败！");
				}
			}
		} else if (moveway != null && "bottom".equals(moveway)){// 规则置底操作
			if ("99999".equals(list.get(list.size()-1))){
				jsonObj.put("success", false);
				jsonObj.put("msg", "置底规则已存在！");
			} else {
				int count = CommonLibInteractiveSceneDAO.setBottomRule(weightToRuleid.get(weight));
				if (count > 0) {
					jsonObj.put("success", true);
					jsonObj.put("msg", "规则置底成功！");
				} else {
					jsonObj.put("success", false);
					jsonObj.put("msg", "规则置底失败！");
				}
			}
		} else {// 规则上移操作
			if ("99999".equals(weight)){
				jsonObj.put("success", false);
				jsonObj.put("msg", "请勿上移置底规则！");
				return jsonObj;
			}
			if (list.size() == 1) {// 满足条件的规则只有当前一条不做规则上移
				jsonObj.put("success", false);
				jsonObj.put("msg", "满足条件的规则只有当前一条，不做规则上移！");
			} else {
				int index = list.indexOf(weight);
				if (index == 0) {
					jsonObj.put("success", false);
					jsonObj.put("msg", "当前规则类型下的规则优先级已最高，不做规则上移！");
				}else if (index == 1 && "0".equals(list.get(0))){
					jsonObj.put("success", false);
					jsonObj.put("msg", "当前规则类型下的规则优先级已最高，不做规则上移！");
				} else {
					int beforeIndex = index - 1;
					String beforeWeight = list.get(beforeIndex);
					int count = CommonLibInteractiveSceneDAO.updateWeight(weight,
							weightToRuleid.get(weight), beforeWeight,
							weightToRuleid.get(beforeWeight));
					if (count > 0) {
						jsonObj.put("success", true);
						jsonObj.put("msg", "规则上移成功！");
					} else {
						jsonObj.put("success", false);
						jsonObj.put("msg", "规则上移失败！");
					}
				}
			}
		}

		return jsonObj;
	}

	/**
	 *@description 查询满足条件规则优先级
	 *@param ruletype
	 *            规则类型
	 *@param city
	 *            城市编码
	 *@param scenariosid
	 *            场景ID
	 *@return
	 *@returnType Map<String, String>
	 */
	public static List<String> getRuleWeight(String ruletype, String city,
			String scenariosid) {
		List<String> list = new ArrayList<String>();
		weightToRuleid = new HashMap<String, String>();
		Result rs = CommonLibInteractiveSceneDAO.getRuleWeight(ruletype, city,
				scenariosid);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String weight = rs.getRows()[i].get("weight").toString();
				String ruleid = rs.getRows()[i].get("ruleid").toString();
				weightToRuleid.put(weight, ruleid);
				list.add(weight);
			}
		}
		return list;
	}

	// 传进来的是serviceName
	@SuppressWarnings("unchecked")
	public static JSONArray getJsonScenariosByName(String name) {
		JSONArray ja = new JSONArray();
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String sql;
		Result res = null;
		res = CommonLibInteractiveSceneDAO.getScenariosTree(serviceType, name);
		if (res == null || res.getRowCount() == 0) {
			return ja;
		}
		// 此时获取的数据为多个，一组一组的，我们先进行分组
		SortedMap[] maps = res.getRows();
		// System.out.println(maps.length);
		List<ServicesMap> serviceList = new ArrayList<ServicesMap>();
		ServicesMap serviceSingle = null;
		ArrayList li = CommonLibInteractiveSceneDAO.getScenariosIDByName(name);
		for (int i = 0; i < maps.length; i++) {
			SortedMap map = maps[i];
			String servicename = map.get("service").toString();
//			String servicename = map.get("name").toString();
			String id = map.get("serviceid").toString();
//			String id = map.get("scenariosid").toString();
			if (i == maps.length - 1) {// 最后一个
				HashMap servicesMap = serviceSingle.getList();
				ArrayList serviceKeys = serviceSingle.getIndexs();
				servicesMap.put(id, map);
				serviceKeys.add(id);
				if (isexist(serviceKeys.get(serviceKeys.size() - 2).toString(),
						li)) {
					serviceList.add(serviceSingle);
				}
			} else if (name.equals(id)) {// 如果为当前查询的业务，那么作为第一个
				if (serviceSingle != null) {// 如果之前已经有了，那么先加入到集合中

					ArrayList serviceKeys1 = serviceSingle.getIndexs();
					if (isexist(serviceKeys1.get(serviceKeys1.size() - 2)
							.toString(), li))
						serviceList.add(serviceSingle);
					serviceSingle = new ServicesMap();// 清空
					HashMap servicesMap = serviceSingle.getList();
					ArrayList serviceKeys = serviceSingle.getIndexs();
					servicesMap.put(id, map);

					serviceKeys.add(id);
				} else {
					serviceSingle = new ServicesMap();
					HashMap servicesMap = serviceSingle.getList();
					ArrayList serviceKeys = serviceSingle.getIndexs();
					servicesMap.put(id, map);
					serviceKeys.add(id);
				}
			} else {
				HashMap servicesMap = serviceSingle.getList();
				ArrayList serviceKeys = serviceSingle.getIndexs();
				servicesMap.put(id, map);
				serviceKeys.add(id);
			}

		}
		// 分组完后，进行，排序
		Collections.sort(serviceList, new MyListComp());
		JSONArray SON = creatTree(serviceList);
		// 排序结束后，进行2次遍历
		return SON;

	}

	public static boolean isexist(String key, ArrayList li) {
		boolean res = false;
		for (int i = 0; i < li.size(); i++) {
			String service = li.get(i).toString();
			if (service.equals(key)) {
				res = true;
				break;
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public static JSONArray creatTree(List<ServicesMap> serviceList) {
		// 使用第一条数据生成一棵基本树,这棵树是最长的一棵
		ServicesMap smap = serviceList.get(0);
		HashMap<String, SortedMap> servicemap = smap.getList();
		ArrayList servicekeys = smap.getIndexs();
		JSONArray son = new JSONArray();
		for (int i = 0; i < servicekeys.size(); i++) {
			SortedMap map = servicemap.get(servicekeys.get(i));
			JSONObject o = new JSONObject();
			if (i == 0) {// 第一个为当前查询的，看是否有子场景
				boolean leaf = CommonLibInteractiveSceneDAO
						.hasChildrenByScenariosid(map.get("SERVICEID")
								.toString());
				o.put("leaf", leaf);
			} else {
				o.put("leaf", false);
			}

			o.put("text", map.get("SERVICE"));
			o.put("id", map.get("SERVICEID"));
			o.put("children", son);
			son = new JSONArray();
			son.add(o);
		}
		// 第一条树创建成功后，遍历后面的树，判断是否后面的树，该添加到哪个节点
		JSONObject root = son.getJSONObject(0);// 这个是树根
		for (int i = 1; i < serviceList.size(); i++) {// 从第二组开始遍历，整棵树（深度为该组的长度）
			ServicesMap smapN = serviceList.get(i);
			HashMap<String, SortedMap> servicemapN = smapN.getList();
			ArrayList servicekeysN = smapN.getIndexs();
			JSONArray nroot = root.getJSONArray("children");
			for (int x = 1; x <= servicekeys.size(); x++) {
				String key = servicekeysN.get(servicekeysN.size() - x - 1)
						.toString();// 从第二个key开始，例如 基金服务
				// String serviceid =
				// servicemapN.get(key).get("SERVICEID").toString();
				boolean haveParent = false;
				for (int y = 0; y < nroot.size(); y++) {
					JSONObject nodeN = nroot.getJSONObject(y);
					if (!NewEquals.equals(nodeN.getString("serviceid"),key)) {
						haveParent = false;
					} else {
						haveParent = true;
						nroot = nodeN.getJSONArray("children");
						break;
					}
				}
				if (!haveParent) {// 如果有父节点后，继续下一个,如果没有的话，那么生成分支
					JSONArray arr = nroot;
					JSONArray arrN = new JSONArray();
					for (int z = 0; z < servicekeysN.size() - x; z++) {
						SortedMap map = servicemapN.get(servicekeysN.get(z));
						JSONObject o = new JSONObject();
						if (z == 0) {// 第一个为当前查询的，看是否有子场景
							boolean leaf = CommonLibInteractiveSceneDAO
									.hasChildrenByScenariosid(map.get(
											"SCENARIOSID").toString());
							o.put("leaf", leaf);
						} else {
							o.put("leaf", false);
						}

						o.put("text", map.get("SERVICE"));
						o.put("id", map.get("SCENARIOSID"));

						o.put("children", arrN);
						arrN = new JSONArray();
						arrN.add(o);
					}
					arr.add(arrN.get(0));
					break;// 生成树后，跳出循环

				}
			}
		}
		return son;
	}
	
	/**
	 * 调用接口更新业务规则，并返回相应的信息
	 * 
	 * @return
	 */
	public static Object UpdateRule() {
		// 定义接送串的格式
		JSONObject jsonObj = new JSONObject();
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient();
		// 判断接口的客户端是否为空
		if (NLPAppWSClient == null) {
			// 将失败信息放入jsonObj的result对象中
			jsonObj.put("msg", "更新业务规则失败!");
		}
		try {
			// 开始更新业务规则
			boolean flag = NLPAppWSClient.updateProcessController();
			if (flag) {
				// 将失败信息放入jsonObj的result对象中
				jsonObj.put("msg", "更新场景知识成功!");
			} else {
				// 将失败信息放入jsonObj的result对象中
				jsonObj.put("msg", "更新场景知识失败!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 将失败信息放入jsonObj的result对象中
			jsonObj.put("msg", "更新场景知识失败!");
		}
		return jsonObj;
	}

	public static Object editmenu(String scenariosid, String name, String oldName) {
		// 定义接送串的格式
		JSONObject jsonObj = new JSONObject();
		
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		
		if (CommonLibInteractiveSceneDAO.isExistSceneName(name, serviceType,scenariosid) > 0) {// 判断是否存在相同名称场景
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景名称已存在!");
			return jsonObj;
		}
		
		int c = CommonLibInteractiveSceneDAO.editmenu(user,serviceType,scenariosid, name,oldName);
		if (c > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新场景名称成功！");
		} else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "更新场景名称失败！");
		}
		return jsonObj;
	}

	/**
	 * 发布
	 * @param city 要发布的城市id
	 * @return
	 */
	public static Object issue(String scenariosid, String service, String city) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		int c = -1;
		
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(), "scenariosrules", "S");
		// 该操作类型用户能够操作的资源
		List<String> cityList = resourseMap.get("地市");
		city = cityList.get(0);

		if (!"全行业".equals(customer) && !"全国".equals(city)){// 非全行业用户
			if (city.endsWith("0000")){// 省管理员
				c = CommonLibInteractiveSceneDAO.issueOnProvince(scenariosid, service, city, user);
			}
		} else {// 全行业用户
//			if ("全国".equals(city)){
				c = CommonLibInteractiveSceneDAO.issueOnAll(scenariosid, service, user);
//			} else{
//				if (city.endsWith("0000")){// 更新单省交互规则
//					c = CommonLibInteractiveSceneDAO.issueOnProvince(scenariosid,city);
//				}
//			}
		}
		if (c > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "场景发布成功！");
		} else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景发布失败！");
		}
		return jsonObj;
	}

	/**
	 * 获取表状态
	 * @return
	 */
	public static Object getIssueData() {
		// 定义返回的json数组格式
		JSONArray jsonArr = new JSONArray();
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		Result rs = CommonLibInteractiveSceneDAO.getIssueData();
		if (rs != null && rs.getRowCount() > 0){
			for (int i = 0;i < rs.getRowCount();i++){
				jsonObj = new JSONObject();
				String key = rs.getRows()[i].get("key1").toString();
				String value = rs.getRows()[i].get("value1").toString().equals("未发布")?"线下规则":rs.getRows()[i].get("value1").toString();
				jsonObj.put("id", key);
				jsonObj.put("text", value);
				jsonArr.add(jsonObj);
			}
		}
		return jsonArr;
	}
	
	/**
	 *@description 通过业务ID，列元素查询 列元素语义关键词
	 *@param serviceid
	 *@param name
	 *@return 
	 *@returnType Object 
	 */
	public static Object getSemanticsKeyWordName(String serviceid) {
		Result rs = CommonLibServiceAttrDao.getSemanticsKeyWordName(serviceid, "docName");
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
				jsonObj = new JSONObject();
				String semanticskeyword = rs.getRows()[0].get("name") == null ? "":rs.getRows()[0].get("name").toString();
				jsonObj.put("success", true);
				jsonObj.put("name", semanticskeyword);
		}else{
			jsonObj.put("success", false);
			jsonObj.put("name", "");
		}
		return jsonObj;
	
	}
	
	/**
	 *@description  获得业务下文档名称
	 *@param serviceid
	 *@return 
	 *@returnType Object 
	 */
	public static Object getKnoName(String serviceid, String attrname) {
		Result rs = CommonLibServiceAttrDao.selectDocname(serviceid, attrname);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String docName = rs.getRows()[i].get("attr").toString();
				jsonObj.put("id", docName);
				jsonObj.put("text", docName);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	/**
	 * 规则检查
	 * @return
	 */
	public static String responseCheck(String response){
		if (response.indexOf("命中问题") > -1 && response.indexOf(")<!--") > -1){
			response = response.substring(0, response.lastIndexOf(")<!--")+1);
		}
		response = response.trim();
		return response;
	}

	public static Object reloadScenarios(String scenariosid) {
		
		// 定义接送串的格式
		JSONObject jsonObj = new JSONObject();
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient();
		// 判断接口的客户端是否为空
		if (NLPAppWSClient == null) {
			// 将失败信息放入jsonObj的result对象中
			jsonObj.put("msg", "更新场景失败!");
		}
		try {
			// 开始更新业务规则
			String flag = NLPAppWSClient.reloadScenarios(scenariosid);
			if ("true".equals(flag)) {
				// 将失败信息放入jsonObj的result对象中
				jsonObj.put("msg", "更新场景知识成功!");
			} else {
				// 将失败信息放入jsonObj的result对象中
				jsonObj.put("msg", "更新场景知识失败!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 将失败信息放入jsonObj的result对象中
			jsonObj.put("msg", "更新场景知识失败!");
		}
		return jsonObj;
		
	}

	/**
	 * 场景下线功能
	 * @param scenariosid
	 * @param city
	 * @return
	 */
	public static Object deleteOnlineRule(String scenariosid, String service, String city) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		int c = -1;
		
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(), "scenariosrules", "S");
		// 该操作类型用户能够操作的资源
		List<String> cityList = resourseMap.get("地市");
		city = cityList.get(0);

		if (!"全行业".equals(customer) && !"全国".equals(city)){// 非全行业用户
			if (city.endsWith("0000")){// 省管理员
				c = CommonLibInteractiveSceneDAO.deleteOnlineOnProvince(scenariosid,service,city,user);
			}
		} else {// 全行业用户
//			if ("全国".equals(city)){
				c = CommonLibInteractiveSceneDAO.deleteOnlineOnAll(scenariosid,service,user);
//			} else{
//				if (city.endsWith("0000")){// 更新单省交互规则
//					c = CommonLibInteractiveSceneDAO.issueOnProvince(scenariosid,city);
//				}
//			}
		}
		if (c > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "规则下线成功！");
		} else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "规则下线失败！");
		}
		return jsonObj;
	}

	public static Object getrobotconfig() {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		
		Result robotidnameRs = CommonLibMetafieldmappingDAO.getConfigKeyValue("实体机器人ID配置");
		if (robotidnameRs != null && robotidnameRs.getRowCount() > 0){
			Map<String,String> robotidtoNameMap = new HashMap<String,String>();
			Map<String,String> robotidtoCitycodeMap = new HashMap<String,String>();
			JSONArray jsonArr = new JSONArray();
			JSONObject o = new JSONObject();
			for(int k = 0;k < robotidnameRs.getRowCount();k++){
				if (robotidnameRs.getRows()[k].get("k").toString().startsWith("Name:")){
					robotidtoNameMap.put(robotidnameRs.getRows()[k].get("name").toString(), robotidnameRs.getRows()[k].get("k").toString().replace("Name:", ""));
				} else if (robotidnameRs.getRows()[k].get("k").toString().startsWith("CityCode:")){
					robotidtoCitycodeMap.put(robotidnameRs.getRows()[k].get("name").toString(), robotidnameRs.getRows()[k].get("k").toString().replace("Name:", ""));
				}
			}
			for (Map.Entry<String, String> map : robotidtoNameMap.entrySet()){
				o = new JSONObject();
				o.put("id", map.getKey());
				o.put("text", map.getValue());
				// 将生成的对象放入arr数组中
				jsonArr.add(o);
			}
			jsonObj.put("success", true);
			jsonObj.put("rows", jsonArr);
		} else{
			jsonObj.put("success", false);
			jsonObj.put("rows", "");
		}
		return jsonObj;
	}

	public static Object getResConfig() {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result result = CommonLibMetafieldmappingDAO.getConfigValue("场景其他形式答案配置", serviceType);
		if (result != null && result.getRowCount() > 0){
			for (int i = 0 ; i < result.getRowCount() ; i++){
				JSONObject jsonObjPart = new JSONObject();
				String info = result.getRows()[i].get("name").toString();
				String key = "";
				String value = "";
				// 获取配置
				if (info.contains("::")){
					String infos[] = info.split("::");
					key = infos[0];
					value = infos[1];
				}else{
					key = info;
					value = "自定义";
				}
				// 有对应词类
				JSONArray jsonArrWord = new JSONArray();
				if (!"自定义".equals(value)){
					Result wordResult = CommonLibWordDAO.select("", false, true, "", value, "基础");
					if (wordResult != null && wordResult.getRowCount() > 0){
						for (int k = 0 ; k < wordResult.getRowCount() ; k++){
							String word = wordResult.getRows()[k].get("word").toString();
							JSONObject jsonObjWord = new JSONObject();
							jsonObjWord.put("id", word);
							jsonObjWord.put("text", word);
							jsonArrWord.add(jsonObjWord);
						}
					}
				}
				jsonObjPart.put("weight", i+1);
				jsonObjPart.put("key", key);
				jsonObjPart.put("value", value);
				jsonObjPart.put("words", jsonArrWord);
				jsonArr.add(jsonObjPart);
			}
		}
		jsonObj.put("success", true);
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}
	
	/**
	 * 保存统计关联
	 * @param serviceid
	 * @param sign
	 * @param column
	 * @param statisticsCount
	 * @param statisticsObj
	 * @param statisticsObjValue
	 * @return
	 */
	public static Object savestatisticinfo(String serviceid, String sign,
			String column, String statisticsCount, String statisticsObj,
			String statisticsObjValue, String minValue, String maxValue) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		
		int i = CommonLibServiceAttrDao.savestatisticinfo(serviceid, sign,
				column, statisticsCount, statisticsObj,
				statisticsObjValue, minValue, maxValue);
		if (i > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功！");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败");
		}
		return jsonObj;
	}

	/**
	 * 
	 * @param serviceid
	 * @param sign
	 * @param column
	 * @return
	 */
	public static Object deleteColumnStatisticInfo(String serviceid,
			String sign, String column) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		
		int i = CommonLibServiceAttrDao.deleteColumnStatisticInfo(serviceid, sign, column);
		if (i > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功！");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除失败");
		}
		return jsonObj;
	}

	/**
	 * 
	 * @param serviceid
	 * @param sign
	 * @param column
	 * @return
	 */
	public static Object getColumnStatisticInfo(String serviceid, String sign,
			String column) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Result result = CommonLibServiceAttrDao.getColumnStatisticInfo(serviceid, sign, column);
		if (result != null && result.getRowCount() > 0){
			JSONObject jsonObjPart = new JSONObject();
			String columnnum = result.getRows()[0].get("columnnum").toString();
			String statisticsCount = result.getRows()[0].get("statisticsCount")==null ? "" : result.getRows()[0].get("statisticsCount").toString();
			String statisticsObj = result.getRows()[0].get("statisticsObj")==null ? "" : result.getRows()[0].get("statisticsObj").toString();
			String statisticsObjValue = result.getRows()[0].get("statisticsObjValue")==null ? "" : result.getRows()[0].get("statisticsObjValue").toString();
			String minValue = result.getRows()[0].get("min") == null ? "" : result.getRows()[0].get("min").toString();
			String maxValue = result.getRows()[0].get("max") == null ? "" : result.getRows()[0].get("max").toString();
			jsonObjPart.put("columnnum", columnnum);
			jsonObjPart.put("statisticsCount", statisticsCount);
			jsonObjPart.put("statisticsObj", statisticsObj);
			jsonObjPart.put("statisticsObjValue", statisticsObjValue);
			jsonObjPart.put("minValue", minValue);
			jsonObjPart.put("maxValue", maxValue);
			jsonArr.add(jsonObjPart);
		}else {
			jsonObj.put("success", true);
			jsonObj.put("rows", "空");
			return jsonObj;
		}
		jsonObj.put("success", true);
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}

	/**
	 * 获取交互要素下拉列表
	 * @return
	 */
	public static Object getInteractiveElement() {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Result result = CommonLibInteractiveSceneDAO.getInteractiveElement();
		if (result != null && result.getRowCount() > 0){
			for (int i = 0;i < result.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				String id  = (result.getRows()[i].get("wordclassid") == null ? "" : result.getRows()[i].get("wordclassid").toString()) + "@@@" + result.getRows()[i].get("kbdataid").toString();
				String text = result.getRows()[i].get("elementname").toString() + "(标准问:" + result.getRows()[i].get("abstract").toString().substring(result.getRows()[i].get("abstract").toString().indexOf(">") + 1, result.getRows()[i].get("abstract").toString().length()) + ")";
				jsonObject.put("id", id);
				jsonObject.put("text", text);
				jsonArr.add(jsonObject);
			}
		}
		return jsonArr;
	}

	/**
	 * 添加交互要素
	 * @param scenariosid
	 * @param wordclassid
	 * @param name
	 * @return
	 */
	public static Object saveinteractiveelement(String scenariosid, String scenariosName,
			String wordclassid, String name, String weight, String city, String cityname) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		String customer = user.getCustomer();
		if(!"全行业".equals(customer)){
			if (city==null || "".equals(city)){
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(), "scenariosrules", "S");
				// 该操作类型用户能够操作的资源
				List<String> cityList = resourseMap.get("地市");
				city = cityList.get(0);
				cityname= cityCodeToCityName.get(city);
			}
		}else {
			city = "全国";
			cityname= "全国";
		}
			
		
		String elementName = name;
		String kbdataid = "";
//		String kbdata = "";
		if (wordclassid.contains("@@@")){
			kbdataid = wordclassid.split("@@@")[1];
			wordclassid = wordclassid.split("@@@")[0];
		}
		if (name.contains("(")){
			elementName = name.split("\\(")[0];
//			kbdata = name.split("-->")[1];
		}
		int resultCount = -1;
		resultCount = CommonLibInteractiveSceneDAO.saveinteractiveelement(user, scenariosid, scenariosName, wordclassid, kbdataid, elementName, weight, serviceType, city, cityname);
		if (resultCount > -1){
			jsonObj.put("success", true);
			jsonObj.put("msg", "添加交互要素成功！");
		}else if (resultCount == -3){
			jsonObj.put("success", false);
			jsonObj.put("msg", "该交互要素已存在");
		}else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "添加交互要素失败");
		}
		return jsonObj;
	}

	public static Object getUrl(String ioa) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("场景页面配置TEST", ioa);
		if (rs != null && rs.getRowCount() > 0 ){
			jsonObj.put("success", true);
			jsonObj.put("url", rs.getRows()[0].get("name").toString());
		}else {
			jsonObj.put("success", true);
			jsonObj.put("url", "./rule.html");
		}
		return jsonObj;
	}

	/**
	 * 批量复制 规则
	 * @param ruleid
	 * @return
	 */
	public static Object copyrules(String ruleid) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		
		int count = 0;
		if (ruleid != null && !"".equals(ruleid)){
			String[] ruleids = ruleid.split("@@");
			for (int i=0 ; i < ruleids.length ; i++){
				Result rs = CommonLibInteractiveSceneDAO.getrulebyID(ruleids[i]);
				if (rs != null && rs.getRowCount() > 0){
					String ruletype = rs.getRows()[0].get("ruletype").toString();
					String copycity = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
					String scenariosid = rs.getRows()[0].get("relationserviceid").toString();
					String weight = rs.getRows()[0].get("weight").toString();;
					
					List<String> weightList = getRuleWeight(ruletype, copycity, scenariosid);
					if (weightList.size() == 1) {
						weight = Double.parseDouble(weight) + 0.001 + "";
					} else {
						int index = weightList.indexOf(weight);
						if (weightList.size() == (index + 1)) {
							weight = Double.parseDouble(weight) + 0.001 + "";
						} else {
							weight = (Double.parseDouble(weight) + Double
									.parseDouble(weightList.get(index + 1)))
									/ 2 + "";
						}
					}
					
					int c = CommonLibInteractiveSceneDAO.copyRule(ruleids[i], weight, serviceType);
					count += c;
				}
			}
			jsonObj.put("success", true);
			jsonObj.put("msg", "成功复制" + count + "条规则！");
		}else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "复制规则失败！");
		}
		return jsonObj;
	}

	/**
	 * 获取策略
	 * @return
	 */
	public static Object getStrategyType() {
		// 定义json串的格式
		JSONArray jsonArr = new JSONArray();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", "");
		jsonObject.put("text", "");
		jsonArr.add(jsonObject);
		
		Result rs = CommonLibWordDAO.select("", false, true, "1", "策略维度父类", "基础");
		if (rs != null && rs.getRowCount() > 0){
			for (int i = 0; i < rs.getRowCount(); i++){
				jsonObject = new JSONObject();
				String id = rs.getRows()[i].get("word").toString();
				jsonObject.put("id", id);
				jsonObject.put("text", id);
				jsonArr.add(jsonObject);
			}
		}
		return jsonArr;
	}
	
//	public static void main(String[] args) {
//		System.out.println(responseCheck("命中问题(&quot;有什么优惠的手机套餐&quot;)<!--@有什么优惠的手机套餐-->"));
//		System.out.println("5555"+"@@@123".split("@@@")[1]+"333");
//		System.out.println("aaa@@".split("@@")[0]);
//	}
	
}

class ServicesMap {
	@SuppressWarnings("unchecked")
	private HashMap<String, SortedMap> list = new HashMap<String, SortedMap>();
	private ArrayList<String> keys = new ArrayList<String>();

	@SuppressWarnings("unchecked")
	public HashMap<String, SortedMap> getList() {
		return list;
	}

	@SuppressWarnings("unchecked")
	public void setList(HashMap<String, SortedMap> list) {
		this.list = list;
	}

	public ArrayList<String> getIndexs() {
		return keys;
	}

	public void setIndexs(ArrayList<String> indexs) {
		this.keys = indexs;
	}

	public int getIndex(String key) {
		if (list.containsKey(key)) {
			for (int i = 0; i < keys.size(); i++) {
				String nkey = keys.get(i);
				if (key.equals(nkey)) {
					return keys.size() - i;
				}
			}

			return 0;
		} else {
			return -1;
		}
	}

}

class MyListComp implements Comparator<ServicesMap> {
	public int compare(ServicesMap object1, ServicesMap object2) {
		int r = object1.getIndexs().size() - object1.getIndexs().size();
		return r;
	}
}