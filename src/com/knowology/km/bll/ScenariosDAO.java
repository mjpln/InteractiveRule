package com.knowology.km.bll;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.km.util.NumberUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibSynonymDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.bll.CommonLibWordclassDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.constant.ParamTypeConsts;
import com.knowology.km.constant.RegnitionRuleTypeConsts;
import com.knowology.km.constant.SceneTypeConsts;
import com.knowology.km.constant.UrlActionInvocationTypeConsts;
import com.knowology.km.enums.ComparisionRelationEnum;
import com.knowology.km.pojo.AndCondition;
import com.knowology.km.pojo.ReturnKeyValue;
import com.knowology.km.pojo.SceneElement;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.pojo.URLActionNode;
import com.knowology.km.pojo.URLActionParam;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

import oracle.sql.CLOB;

public class ScenariosDAO {

	private static Logger logger = Logger.getLogger("ScenariosDAO");

	private static final int SceneElementCount = 101;

	/**
	 * 初始化流程图
	 */
	public static Object loadData(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		String sql = "select * from scene_configuration where relationserviceid=" + scenariosid;
		logger.info("加载数据执行sql=" + sql);
		Result result = Database.executeQuery(sql);
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("id", result.getRows()[i].get("sceneid"));
				obj.put("relationserviceid", result.getRows()[i].get("relationserviceid"));

				String sceneJsonData = "";
				// 获得主键ID
				if (!GetConfigValue.isToMysql) {
					sceneJsonData = MyUtil.oracleClob2Str((CLOB) result.getRows()[i].get("scenejsondata"));
				} else {
					try {
						sceneJsonData = new String((byte[]) result.getRows()[i].get("scenejsondata"), "UTF-8");
					} catch (UnsupportedEncodingException e) {

					}
				}
				obj.put("scenejsondata", sceneJsonData);
				jsonArr.add(obj);
			}
			jsonObj.put("success", true);
			jsonObj.put("rowdata", jsonArr);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 获取流程图URL地址
	 * 
	 */
	public static Object getUrl(String ioa) {
		JSONObject jsonObj = new JSONObject();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("场景页面配置TEST", ioa);
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("success", true);
			jsonObj.put("url", rs.getRows()[0].get("name").toString());
		} else {
			jsonObj.put("success", true);
			jsonObj.put("url", "./scenariosCall.html");
		}
		return jsonObj;
	}

	/**
	 * 配置机器人信息
	 */
	public static Object configRobot(String robotId, String robotName, String scenariosId) {
		// 配置场景与机器人ID对应关系
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		jsonObj.put("msg", "提交失败");
		boolean result = configSceneRobotRelation(robotId, scenariosId);
		if (!result) {
			logger.info("配置场景与机器人ID对应关系失败, robotID=" + robotId + ",scenariosId=" + scenariosId);
			return jsonObj;
		}
		// 配置地市编码
		int hashcode = Math.abs(robotId.hashCode());
		String cityCode = hashcode + "0000";
		result = addCityCode(cityCode, robotId);
		if (!result) {
			logger.info("配置地市编码失败, robotID=" + robotId);
			return jsonObj;
		}
		// 机器人ID参数配置
		result = configRobotStandardValues(robotId, robotName, cityCode);
		if (!result) {
			logger.info("机器人ID参数配置失败, robotID=" + robotId + ",robotName=" + robotName);
			return jsonObj;
		}

		jsonObj.put("success", true);
		jsonObj.put("msg", "提交成功");
		return jsonObj;
	}

	/**
	 * 配置场景机器人对应关系
	 */
	private static boolean configSceneRobotRelation(String robotId, String scenariosId) {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 配置键是否存在
		String standardKeyId = MetafieldDao.getStandardKeyId("场景机器人ID对应关系", serviceType);
		List<String> standardKeys = new ArrayList<String>();
		if (standardKeyId == null || "".equals(standardKeyId)) {
			// 插入配置键
			MetafieldDao.insertKey("场景机器人ID对应关系", standardKeys);
		}
		JSONObject jsonObj = new JSONObject();
		List<String> standardValues = new ArrayList<String>();
		standardValues.add(scenariosId + "::" + robotId);
		jsonObj = (JSONObject) MetafieldDao.insertConfigValue("场景机器人ID对应关系", serviceType, standardValues);
		return jsonObj.getBooleanValue("success");
	}

	/**
	 * 机器人ID参数配置
	 */
	private static boolean configRobotStandardValues(String robotId, String robotName, String cityCode) {
		JSONObject jsonObj = new JSONObject();
		// 配置参数键
		List<String> standardKeys = new ArrayList<String>();
		standardKeys.add(robotId);
		jsonObj = (JSONObject) MetafieldDao.insertKey("实体机器人ID配置", standardKeys);
		if (jsonObj.getBooleanValue("success")) {
			List<String> standardValues = new ArrayList<String>();
			// 配置参数值
			standardValues.add("Name:" + robotName);
			standardValues.add("City:" + robotId);
			standardValues.add("CityCode:" + cityCode);
			standardValues.add("MAC:abc");
			standardValues.add("servicePosition:北京演示");
			jsonObj = (JSONObject) MetafieldDao.insertConfigValue("实体机器人ID配置", robotId, standardValues);
		}
		return jsonObj.getBooleanValue("success");
	}

	/**
	 * 添加地市编码
	 */
	public static boolean addCityCode(String cityCode, String cityName) {
		JSONObject jsonObj = new JSONObject();
		// 配置参数键
		List<String> standardKeys = new ArrayList<String>();
		standardKeys.add(cityCode);
		jsonObj = (JSONObject) MetafieldDao.insertKey("地市编码配置", standardKeys);
		if (jsonObj.getBooleanValue("success")) {
			List<String> standardValues = new ArrayList<String>();
			// 配置参数值
			standardValues.add(cityName);
			jsonObj = (JSONObject) MetafieldDao.insertConfigValue("地市编码配置", cityCode, standardValues);
		}
		return jsonObj.getBooleanValue("success");
	}

	/**
	 * 保存规则
	 */
	public static boolean insertSceneRules(String scenariosid, List<SceneRule> sceneRules) throws Exception {
		// 存在先删除
		List<String> listSqls = new ArrayList<String>();
		List<List<?>> listParams = new ArrayList<List<?>>();
		String deleteSql = "delete from scenariosrules where relationserviceid=? and ruletype in ('0','3') "
				+ "or (relationserviceid=? and ruletype in ('2','5') and currentnode is not null)";
		listSqls.add(deleteSql);
		List<String> params = new ArrayList<String>();
		params.add(scenariosid);
		params.add(scenariosid);
		listParams.add(params);
		Database.executeNonQueryTransaction(listSqls, listParams);

		// 插入场景规则
		listSqls = new ArrayList<String>();
		listParams = new ArrayList<List<?>>();
		for (SceneRule sceneRule : sceneRules) {
			StringBuffer insertSql = new StringBuffer();
			params = new ArrayList<String>();
			insertSql.append("insert into SCENARIOSRULES (ruleid,relationserviceid");
			params.add(sceneRule.getRuleId());
			params.add(sceneRule.getRelationServiceId());
			Field[] fields = SceneRule.class.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if (fieldName.indexOf("condition") > -1) {
					PropertyDescriptor pd = new PropertyDescriptor(fieldName, SceneRule.class);
					Method wM = pd.getReadMethod();
					insertSql.append("," + fieldName);
					params.add((String) wM.invoke(sceneRule));
				}
			}
			insertSql.append(",ruletype");
			params.add(sceneRule.getRuleType());
			insertSql.append(",weight");
			params.add(sceneRule.getWeight());
			insertSql.append(",city");
			params.add(sceneRule.getCityCode());
			insertSql.append(",cityname");
			params.add(sceneRule.getCityName());
			insertSql.append(",responsetype");
			params.add(sceneRule.getResponseType());
			insertSql.append(",questionobject");
			params.add(sceneRule.getQuestionObject());
			insertSql.append(",standardquestion");
			params.add(sceneRule.getStandardQuestion());
			insertSql.append(",ruleresponse");
			params.add(sceneRule.getRuleResponse());
			insertSql.append(",ruleresponsetemplate");
			params.add(sceneRule.getRuleResponseTemplate());
			insertSql.append(",isedit");
			params.add(sceneRule.getIsEdit());
			insertSql.append(",currentnode)");
			params.add(sceneRule.getCurrentNode());
			insertSql.append(" values(?");
			for (int i = 0; i < params.size() - 1; i++) {
				insertSql.append(",?");
			}
			insertSql.append(")");
			listSqls.add(insertSql.toString());
			listParams.add(params);
		}
		Database.executeNonQueryTransaction(listSqls, listParams);
		return true;
	}

	/**
	 * 保存数据
	 */
	public static boolean insertSceneJsonData(String scenariosid, String sceneJson) {
		// 存在先删除
		String sql = "delete from scene_configuration where relationserviceid = ?";
		Database.executeNonQuery(sql, scenariosid);
		sql = "insert into scene_configuration(scenejsondata, relationserviceid) values(?,?)";
		Database.executeNonQuery(sql, sceneJson, scenariosid);
		return true;
	}

	/**
	 * 获取条件
	 * 
	 * @param scenariosid   场景ID
	 * @param sceneElements 场景要素
	 * @return 条件集合
	 */
	public static String[] getSceneConditions(String scenariosid, List<SceneElement> sceneElementValues) {
		List<SceneElement> scenariosElementList = getSceneElements(scenariosid);
		String[] conditions = new String[SceneElementCount];
		for (SceneElement sceneElement : scenariosElementList) {
			String elementName = sceneElement.getElementName();
			int weight = Integer.parseInt(sceneElement.getWeight());
			for (SceneElement sceneElementValue : sceneElementValues) {
				if (elementName.equals(sceneElementValue.getElementName())) {
					conditions[weight] = sceneElementValue.getElementValue();
				}
			}
		}
		return conditions;
	}

	/**
	 * 获取场景要素
	 */
	public static List<SceneElement> getSceneElements(String scenariosid) {
		List<SceneElement> scenariosElementList = new ArrayList<SceneElement>();
		Result result = CommonLibInteractiveSceneDAO.getElementName(scenariosid, "", 1, SceneElementCount);
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				SceneElement sceneElement = new SceneElement();
				sceneElement.setElementName((String) result.getRows()[i].get("name"));
				sceneElement.setWeight((String) result.getRows()[i].get("weight").toString());
				scenariosElementList.add(sceneElement);
			}
			return scenariosElementList;
		}
		return null;
	}

	/**
	 * 构建规则实体
	 * 
	 * @param scenariosid      场景ID
	 * @param currentNodeName  当前节点
	 * @param ruleType         规则类型
	 * @param conditions       条件集合
	 * @param questionObject   问题对象
	 * @param standardQuestion 标准问题
	 * @param ruleResponse     回复内容
	 * @param responseType     回复类型
	 * @param weight           优先级
	 * @return
	 */
	public static SceneRule buildSceneRuleInfo(String scenariosid, String currentNodeName, String ruleType,
			String[] conditions, String questionObject, String standardQuestion, String ruleResponse,
			String responseType, String weight) {
		try {

			User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
			String serviceType = user.getIndustryOrganizationApplication(); // 获取服务类别
			String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(serviceType);

			SceneRule sceneRule = new SceneRule();
			String ruleId = ""; // 规则ID

			if (GetConfigValue.isOracle) {
				ruleId = ConstructSerialNum.GetOracleNextValNew("seq_scenariosrules_id", bussinessFlag);
			} else if (GetConfigValue.isMySQL) {
				ruleId = ConstructSerialNum.getSerialIDNew("scenariosrules", "ruleid", bussinessFlag);
			}
			sceneRule.setRuleId(ruleId);
			Field[] fields = SceneRule.class.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if (fieldName.indexOf("condition") > -1) {
					int conditionIndex = Integer.parseInt(fieldName.substring(9));
					PropertyDescriptor pd = new PropertyDescriptor(fieldName, SceneRule.class);
					Method wM = pd.getWriteMethod();
					if (StringUtils.isNotBlank(conditions[conditionIndex])) {
						wM.invoke(sceneRule, conditions[conditionIndex].trim());
					}
				}
			}
			sceneRule.setRuleType(ruleType);
			sceneRule.setWeight(weight);
			sceneRule.setCityCode("全国");
			sceneRule.setCityName("全国");
			sceneRule.setResponseType(responseType);
			sceneRule.setQuestionObject(questionObject);
			sceneRule.setStandardQuestion(standardQuestion);
			sceneRule.setRuleResponse(ruleResponse.replace("\"", "&quot;"));
			sceneRule.setRuleResponseTemplate(ruleResponse.replace("\"", "&quot;"));
			sceneRule.setRelationServiceId(scenariosid);
			sceneRule.setIsEdit("1");
			sceneRule.setCurrentNode(currentNodeName);
			return sceneRule;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询机器人ID
	 */
	public static String getSceneRobotID(String scenariosid) {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("场景机器人ID对应关系", serviceType);
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			for (int i = 0; i < rsConfig.getRowCount(); i++) {
				String seneRobotIDConfig = (String) rsConfig.getRows()[i].get("name").toString();
				if (NumberUtil.formatSceneId(scenariosid).equals(NumberUtil.formatSceneId(seneRobotIDConfig.split("::")[0]))) {
					return seneRobotIDConfig.split("::")[1];
				}
			}
		}
		return "";
	}

	/**
	 * 查询机器人对应的地市
	 */
	public static String getRobotCityCode(String robotId) {
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("实体机器人ID配置", robotId);
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			for (int i = 0; i < rsConfig.getRowCount(); i++) {
				String robotIDConfig = (String) rsConfig.getRows()[i].get("name").toString();
				if ("CityCode".equals(robotIDConfig.split(":")[0])) {
					return robotIDConfig.split(":")[1];
				}
			}
		}
		return "";
	}

	/**
	 * 设置场景要素值
	 */
	public static SceneElement getSceneElementValue(String elementName, String elementValue) {
		SceneElement sceneElementValue = new SceneElement();
		sceneElementValue.setElementName(elementName);
		sceneElementValue.setElementValue(elementValue);
		return sceneElementValue;
	}

	/**
	 * 设置回复内容
	 */
	public static String getRuleResponse(Map<String, String> setItems) {
		if (setItems != null && !setItems.isEmpty()) {
			StringBuffer ruleResponse = new StringBuffer();
			for (Entry<String, String> item : setItems.entrySet()) {
				ruleResponse.append("SET(\"" + item.getKey() + "\",\"" + item.getValue() + "\")").append(";");
			}
			return ruleResponse.toString();
		}
		return "";

	}

	public static String getRuleResponse(Map<String, String> setItems, List<String> otherThings) {
		StringBuffer ruleResponse = new StringBuffer();
		if (setItems != null && !setItems.isEmpty()) {
			for (Entry<String, String> item : setItems.entrySet()) {
				ruleResponse.append("SET(\"" + item.getKey() + "\",\"" + item.getValue() + "\")").append(";");
			}
		}
		if (otherThings != null && !otherThings.isEmpty()) {
			for (String element : otherThings) {
				ruleResponse.append(element).append(";");
			}
		}
		return ruleResponse.toString();
	}

	public static String getRuleResponse(String condition, String result) {
		return condition + "==>" + result;
	}

	public static String getMenuRuleResponse(String menuStartWords, String menuOptions, String menuEndWords) {
		StringBuffer ruleResponse = new StringBuffer();
		ruleResponse.append(menuStartWords).append("<br/>");
		if (StringUtils.isNotBlank(menuOptions)) {
			String[] options = menuOptions.split("\\|");
			for (int i = 0; i < options.length; i++) {
				ruleResponse.append("[" + (i + 1) + "]").append(options[i]).append("<br/>");
			}
		}
		ruleResponse.append(menuEndWords);
		return ruleResponse.toString();
	}

	public static String getMenuRuleResponseTemplate(String menuStartWords, String menuOptions, String menuEndWords) {
		StringBuffer ruleResponseTemplate = new StringBuffer();
		ruleResponseTemplate.append("菜单询问(\"");
		ruleResponseTemplate.append(menuStartWords).append("<@选项文本>");
		ruleResponseTemplate.append(menuEndWords).append("\",");
		if (StringUtils.isNotBlank(menuOptions)) {
			String[] options = menuOptions.split("\\|");
			for (int i = 0; i < options.length; i++) {
				ruleResponseTemplate.append(options[i]);
				if (i < options.length - 1) {
					ruleResponseTemplate.append("||");
				}
			}
		}
		ruleResponseTemplate.append(")");
		return ruleResponseTemplate.toString();
	}

	/**
	 * 查询公共关联意图
	 */
	public static Result queryPublicCollectionIntention() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取X行业->通用商家->多渠道应用根问题库
		String businessServiceType = serviceType.split("->")[0] + "->通用商家->多渠道应用";
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), businessServiceType, "", "全国",
				"");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String sql = "select k.KBDATAID,k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and s.parentName='信息收集' and s.brand = ? ";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName);
			return rs;
		}
		return null;
	}

	/**
	 * 查询公共用户意图
	 */
	public static Result queryPublicUserIntention() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取X行业->通用商家->多渠道应用根问题库
		String businsessServiceType = serviceType.split("->")[0] + "->通用商家->多渠道应用";
		// 获取行业根问题库
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), businsessServiceType, "", "全国",
				"");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String sql = "select k.KBDATAID,k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and s.parentName='用户意图' and s.brand = ? ";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName);
			return rs;
		}
		return null;
	}

	/**
	 * 获取商家下识别规则业务ID
	 * 
	 * @param parentName 父业务名
	 * @return 识别规则业务ID
	 */
	public static String getBusinessRegnitionRuleServiceId() {
		String sql = "select SERVICEID from service s where s.service='识别规则业务' and PARENTNAME like ?";
		// 获取商家根问题库
		User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
		String serviceType = user.getIndustryOrganizationApplication(); // 获取商家
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), serviceType, "", "全国", "");
		if (result == null || result.getRowCount() == 0) {
			return "";
		}
		String rootServiceName = result.getRows()[0].get("service") + "";
		Result rs = Database.executeQuery(sql, rootServiceName);
		if (rs != null && rs.getRowCount() > 0) {
			String serviceId = rs.getRows()[0].get("SERVICEID") + "";
			return serviceId;
		}
		return "";
	}

	/**
	 * 获取场景下识别规则业务ID
	 * 
	 * @param sceneName 场景名称
	 * @return 识别规则业务ID
	 */
	public static String getSceneRegnitionRuleServiceId(String sceneName) {
		String sql = "select SERVICEID from service s where s.service='识别规则业务' and PARENTNAME like ?";
		Result rs = Database.executeQuery(sql, sceneName);
		if (rs != null && rs.getRowCount() > 0) {
			String serviceId = rs.getRows()[0].get("SERVICEID") + "";
			return serviceId;
		}
		return "";
	}

	/**
	 * 配置接口信息
	 * 
	 * @param urlActionNode 动作组件
	 */
	public static boolean configInterfaceInfo(URLActionNode urlActionNode) {
		// 配置接口信息
		List<String> standardKeys = buildInterfaceInfo(urlActionNode);
		// 插入接口配置
		JSONObject jsonObj = insertInterfaceConfigInfo(standardKeys, urlActionNode.getInterfaceName());
		return jsonObj.getBooleanValue("success");
	}

	/**
	 * 插入接口配置
	 * 
	 * @param standardValues 配置信息
	 * @param interfaceName  接口名称
	 * @return
	 */
	private static JSONObject insertInterfaceConfigInfo(List<String> standardValues, String interfaceName) {
		User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
		String serviceType = user.getIndustryOrganizationApplication(); // 获取行业
		String standardKey = serviceType + "::" + interfaceName;
		// 存在则先删除
		String standardKeyId = MetafieldDao.getStandardKeyId("第三方接口信息配制", standardKey);
		if (StringUtils.isNotBlank(standardKeyId)) {
			MetafieldDao.deleteKey(standardKeyId, "第三方接口信息配制", standardKey);
		}
		// 插入接口配置键
		List<String> standardKeys = new ArrayList<String>();
		standardKeys.add(standardKey);
		JSONObject jsonObj = (JSONObject) MetafieldDao.insertKey("第三方接口信息配制", standardKeys);
		if (jsonObj.getBooleanValue("success")) {
			// 插入接口配置值
			jsonObj = (JSONObject) MetafieldDao.insertConfigValue("第三方接口信息配制", standardKey, standardValues);
		}
		return jsonObj;
	}

	/**
	 * 配置接口信息
	 */
	private static List<String> buildInterfaceInfo(URLActionNode urlActionNode) {
		String actionUrl = urlActionNode.getActionUrl();
		String invocationWay = urlActionNode.getInvocationWay();
		String httpMethod = urlActionNode.getHttpMethod();
		String functionName = urlActionNode.getFunctionName();
		String nameSpace = urlActionNode.getNamespace();
		List<URLActionParam> inParams = urlActionNode.getInParams();
		List<URLActionParam> outParams = urlActionNode.getOutParams();
		// 组装参数顺序
		String paramOrder = buildInterfaceParamOrders(inParams);
		// 组装参数转换
		List<String> inner2calledParams = buildInner2CalledParamMaps(inParams);
		List<String> calledRes2InnerResParams = buildCalledRes2InnerResParas(outParams);
		// 配置接口信息
		List<String> standardValues = getInterfaceConfigInfo(nameSpace, actionUrl, invocationWay, httpMethod,
				functionName, paramOrder, inner2calledParams, calledRes2InnerResParams);
		return standardValues;
	}

	/**
	 * 获取接口配置
	 * 
	 * @param nameSpace                命名空间
	 * @param actionUrl                接口地址
	 * @param invocationWay            调用方式
	 * @param httpMethod               HTTP方法
	 * @param functionName             函数名称
	 * @param paramOrder               参数顺序
	 * @param inner2calledParams       输入参数
	 * @param calledRes2InnerResParams 输出参数
	 * @return
	 */
	private static List<String> getInterfaceConfigInfo(String nameSpace, String actionUrl, String invocationWay,
			String httpMethod, String functionName, String paramOrder, List<String> inner2calledParams,
			List<String> calledRes2InnerResParams) {
		List<String> standardValues = new ArrayList<String>();
		if (StringUtils.isNotBlank(nameSpace)) {
			standardValues.add("NameSpace:=" + nameSpace);
		}
		if (StringUtils.isNotBlank(actionUrl)) {
			standardValues.add("URL:=" + actionUrl);
		}
		standardValues.add("ParasType:=Json_KeyValue");
		standardValues.add("ReturnParasType:=Json_KeyValue");
		standardValues.add("BufferType:=close");
		if (UrlActionInvocationTypeConsts.WEBSERVICE.equals(invocationWay)) {
			if (StringUtils.isNotBlank(functionName)) {
				standardValues.add("CallFuncName:=" + functionName);
			}
		}
		if (UrlActionInvocationTypeConsts.HTTP.equals(invocationWay)) {
			if (StringUtils.isNotBlank(httpMethod)) {
				standardValues.add("CallType:=" + httpMethod);
			}
		}
		if (StringUtils.isNotBlank(paramOrder)) {
			standardValues.add("ParasOrder:=" + paramOrder);
		}
		standardValues.addAll(inner2calledParams);
		standardValues.addAll(calledRes2InnerResParams);
		return standardValues;
	}

	/**
	 * 返回参数转为内部参数
	 */
	private static List<String> buildCalledRes2InnerResParas(List<URLActionParam> outParams) {
		List<String> calledRes2InnerResParams = new ArrayList<String>();
		if (!outParams.isEmpty()) {
			for (URLActionParam param : outParams) {
				String outerParamName = param.getParamName();
				String innerParamName = param.getParamValue();
				if (StringUtils.isNotBlank(innerParamName) && StringUtils.isNotBlank(outerParamName)) {
					calledRes2InnerResParams.add("CalledRes2InnerResParas:=" + outerParamName + "->" + innerParamName);
				}
			}
		}
		return calledRes2InnerResParams;
	}

	/**
	 * 内部参数转为输入参数
	 */
	private static List<String> buildInner2CalledParamMaps(List<URLActionParam> inParams) {
		List<String> inner2calledParams = new ArrayList<String>();
		if (!inParams.isEmpty()) {
			for (URLActionParam param : inParams) {
				String outerParamName = param.getParamName();
				String innerParamName = param.getParamValue();
				if (StringUtils.isNotBlank(innerParamName) && StringUtils.isNotBlank(outerParamName)) {
					inner2calledParams.add("Inner2CalledParasMap:=" + outerParamName + "<-" + innerParamName);
				}
			}
		}
		return inner2calledParams;
	}

	/**
	 * 组装参数顺序
	 */
	private static String buildInterfaceParamOrders(List<URLActionParam> inParams) {
		StringBuffer paramOrderBuffer = new StringBuffer();
		if (!inParams.isEmpty()) {
			for (int i = 0; i < inParams.size(); i++) {
				URLActionParam inParam = inParams.get(i);
				paramOrderBuffer.append(inParam.getParamName());
				if (i < inParams.size() - 1) {
					paramOrderBuffer.append("#");
				}
			}
		}
		return paramOrderBuffer.toString();
	}

	/**
	 * 获取识别规则业务ID
	 * 
	 * @param sceneType 场景类型
	 * @param sceneName 场景名称
	 * @return
	 */
	public static String getRegnitionRuleServiceId(String sceneType, String sceneName) {
		String recognitionServiceId = "";
		if (SceneTypeConsts.CALL_IN.equals(sceneType)) {
			recognitionServiceId = ScenariosDAO.getBusinessRegnitionRuleServiceId();
		}
		if (SceneTypeConsts.CALL_OUT.equals(sceneType)) {
			recognitionServiceId = ScenariosDAO.getSceneRegnitionRuleServiceId(sceneName);
		}
		return recognitionServiceId;
	}

	/**
	 * 获取条件值
	 * 
	 * @param andConditions AND条件集合
	 * @return 条件值
	 */
	@SuppressWarnings("unused")
	private static String getAndConditions(ArrayList<AndCondition> andConditions) {
		StringBuffer condition = new StringBuffer();
		if (!andConditions.isEmpty()) {
			for (int i = 0; i < andConditions.size(); i++) {
				AndCondition andCondition = andConditions.get(i);
				if (StringUtils.isNotBlank(andCondition.getParamValue())) {
					String paramName = andCondition.getParamName();
					String paramRelation = andCondition.getParamRelation();
					String paramType = andCondition.getParamType();
					condition
							.append(getAndCondition(paramName, paramRelation, paramType, andCondition.getParamValue()));
					if (i < andConditions.size() - 1) {
						condition.append(" and ");
					}
				}

			}
		}
		return condition.toString();
	}

	/**
	 * 获取AND条件
	 * 
	 * @param paramName     参数1
	 * @param paramRelation 比较关系
	 * @param paramType     参数2类型
	 * @param paramValue    参数2值
	 * @return
	 */
	private static String getAndCondition(String paramName, String paramRelation, String paramType, String paramValue) {
		StringBuffer andCondition = new StringBuffer();
		andCondition.append(paramName);
		andCondition.append(ComparisionRelationEnum.getEnum(paramRelation).getValue());
		if (ParamTypeConsts.STRING.equals(paramType)) {
			andCondition.append("\"" + paramValue + "\"");
		}
		if (ParamTypeConsts.INTEGER.equals(paramType)) {
			andCondition.append(Integer.parseInt(paramValue));
		}
		if (ParamTypeConsts.VARIABLE.equals(paramType)) {
			andCondition.append("<@" + paramValue + ">");
		}
		return andCondition.toString();
	}

	/**
	 * 获取地市
	 * 
	 * @param scenariosid 场景ID
	 * @param sceneType   场景类型
	 * @return
	 */
	public static String getCityCode(String scenariosid, String sceneType) {
		String cityCode = "";
		if (SceneTypeConsts.CALL_IN.equals(sceneType)) {
			User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
					"scenariosrules", "S");
			List<String> cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
			}
		}
		if (SceneTypeConsts.CALL_OUT.equals(sceneType)) {
			String robotId = ScenariosDAO.getSceneRobotID(scenariosid); // 机器人ID
			cityCode = StringUtils.isNotBlank(robotId) ? ScenariosDAO.getRobotCityCode(robotId) : "全国"; // 地市ID
		}
		return cityCode;
	}

	/**
	 * 分页查询词条
	 * 
	 * @param scenariosid 场景ID
	 * @param wordclassid 词类ID
	 * @param wordClass   词类名称
	 * @param wordItem    词条名称
	 * @param currentPage 当前页码
	 * @param pageSize    分页条数
	 * @return
	 */
	public static Object listPagingWordItem(String scenariosid, String wordclassid, String wordClass, String wordItem,
			int currentPage, int pageSize) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		int totalCount = CommonLibWordDAO.getWordCount(wordclassid, wordItem);
		Result rs = CommonLibWordDAO.select(wordclassid, wordItem, currentPage, pageSize);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject row = new JSONObject();
				row.put("wordclassid", rs.getRows()[i].get("wordclassid").toString());
				row.put("wordclass", wordClass);
				row.put("worditem", rs.getRows()[i].get("word").toString());
				row.put("wordid", rs.getRows()[i].get("wordid").toString());
				row.put("type", rs.getRows()[i].get("type").toString());
				rows.add(row);
			}
		}
		jsonObj.put("rows", rows);
		jsonObj.put("total", totalCount >=0 ? totalCount : 0);
		return jsonObj;
	}

	/**
	 * 不分页查询词条
	 * 
	 * @param scenariosid 场景ID
	 * @param wordclassid 词类ID
	 * @param wordClass   词类名称
	 * @param wordItem    词条名称
	 * @return
	 */
	public static Object listAllWordItem(String scenariosid, String wordclassid, String wordClass, String wordItem) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		Result rs = CommonLibWordDAO.select(wordItem, true, true, "", wordClass, "基础");
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject row = new JSONObject();
				row.put("wordclassid", rs.getRows()[i].get("wordclassid").toString());
				row.put("wordclass", wordClass);
				row.put("worditem", rs.getRows()[i].get("word").toString());
				row.put("wordid", rs.getRows()[i].get("wordid").toString());
				row.put("type", rs.getRows()[i].get("type").toString());
				rows.add(row);
			}
		}
		jsonObj.put("rows", rows);
		jsonObj.put("total", rows.size());
		return jsonObj;
	}

	/**
	 * 分页查询词条别名
	 * 
	 * @param scenariosid 场景ID
	 * @param wordItem    词条
	 * @param wordClass   词类
	 * @param wordAlias   别名
	 * @param currentPage 页码
	 * @param pageSize    条数
	 * @return
	 */
	public static Object listPagingWordAlias(String scenariosid, String wordItem, String wordClass, String wordAlias,
			int currentPage, int pageSize) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		int totalCount = 0;
		Result rs = CommonLibSynonymDAO.getSynonymCount(wordAlias, true, true, "", wordItem, wordClass, "基础");
		if (rs != null && rs.getRowCount() > 0) {
			totalCount = rs.getRowCount();
			rs = CommonLibSynonymDAO.select((currentPage - 1) * pageSize, currentPage * pageSize, wordAlias, true, true,
					"", wordItem, wordClass, "基础");
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject row = new JSONObject();
					row.put("stdwordid", rs.getRows()[i].get("stdwordid").toString());
					row.put("wordclass", rs.getRows()[i].get("wordclass").toString());
					row.put("synonym", rs.getRows()[i].get("word").toString());
					row.put("worditem", rs.getRows()[i].get("worditem").toString());
					row.put("wordid", rs.getRows()[i].get("wordid").toString());
					row.put("type", rs.getRows()[i].get("type").toString());
					rows.add(row);
				}
			}
		}
		jsonObj.put("rows", rows);
		jsonObj.put("total", totalCount);
		return jsonObj;
	}

	/**
	 * 更新词条
	 * 
	 * @param newworditem 新词条
	 * @param wordid      词条ID
	 * @param wordClassId 词类ID
	 * @param scenariosid 场景ID
	 * @return
	 */
	public static Object updateWordItem(String scenariosid, String newworditem, String wordid, String wordClassId) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibWordDAO.update(user, "", newworditem, "", "标准名称", wordid, wordClassId, "", "", "");
		if (count > 0) {
			jsonObj.put("success", true);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 插入词条
	 * 
	 * @param wordItems   词条集合，逗号分割
	 * @param wordClassId 词类ID
	 * @return
	 */
	public static Object insertWordItem(String wordItems, String wordClassId) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		String[] wordItemArray = wordItems.split("\\,");
		if (wordItemArray.length > 0) {
			for (String wordItem : wordItemArray) {
				if (!CommonLibWordDAO.exist(wordItem, wordClassId)) {
					CommonLibWordDAO.insert(wordItem, wordClassId, user);
				}
			}
			jsonObj.put("success", true);
			return jsonObj;
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 删除词条
	 * 
	 * @param wordId    词条ID
	 * @param wordClass 词类名称
	 * @param wordItem  词条名称
	 * @return
	 */
	public static Object deleteWordItem(String wordId, String wordClass, String wordItem) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibWordDAO.delete(user, wordId, wordClass, "", wordItem, "");
		if (count > 0) {
			jsonObj.put("success", true);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 新增别名
	 * 
	 * @param wordAliases    别名集合，逗号分割
	 * @param wordClassId    词类ID
	 * @param wordClass      词类名称
	 * @param standardWordId 词条ID
	 * @param wordItem       词条名称
	 * @return
	 */
	public static Object insertWordAlias(String wordAliases, String wordClassId, String wordClass,
			String standardWordId, String wordItem) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		List<String> wordAliasList = new ArrayList<String>();
		if (StringUtils.isNotBlank(wordAliases)) {
			String[] wordAliasArray = wordAliases.split("\\|");
			if (wordAliasArray.length > 0) {
				wordAliasList = Arrays.asList(wordAliasArray);
				for (String wordAlias : wordAliasList) {
					if (!CommonLibWordDAO.exist(standardWordId, wordAlias)) {
						CommonLibWordDAO.insertOtherWord(wordAlias, standardWordId, wordClassId,
								user.getIndustryOrganizationApplication());
					}
				}
				jsonObj.put("success", true);
				return jsonObj;
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 更新别名
	 * 
	 * @param wordAlias 别名
	 * @param wordId    别名ID
	 * @param wordClass 词类名称
	 * @return
	 */
	public static Object updateWordAlias(String wordAlias, String wordId, String wordClass) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibSynonymDAO.update(user, "", wordAlias, "", "其他别名", wordId, "", wordClass);
		if (count > 0) {
			jsonObj.put("success", true);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 删除别名
	 * 
	 * @param wordClassId    词类ID
	 * @param wordClass      词类名称
	 * @param standardWordId 词类ID
	 * @param wordItem       词条名称
	 * @param wordIds        别名ID集合，逗号分割
	 * @param wordAliases    别名集合，逗号分割
	 * @return
	 */
	public static Object deleteWordAlias(String wordClassId, String wordClass, String standardWordId, String wordItem,
			String wordIds, String wordAliases) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibSynonymDAO.delete(user, wordIds, wordAliases, wordItem, wordClass);
		if (count > 0) {
			jsonObj.put("success", true);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 批量保存词条
	 * 
	 * @param scenariosid 场景ID
	 * @param wordIds     词条ID
	 * @param wordItems   词条名称
	 * @param wordClassId 词类ID
	 * @return
	 */
	public static Object saveWordItems(String scenariosid, String wordIds, String wordItems, String wordClassId) {
		JSONObject jsonObj = new JSONObject();
		if (StringUtils.isNotBlank(wordItems)) {
			String[] wordItemsArray = wordItems.split("\\,");
			try {
				for (int i = 0; i < wordItemsArray.length; i++) {
					if (StringUtils.isNotBlank(wordIds) && wordIds.split("\\,").length > 0
							&& StringUtils.isNotBlank(wordIds.split("\\,")[i])) {
						// 更新词条
						updateWordItem(scenariosid, wordItemsArray[i], wordIds.split("\\,")[i], wordClassId);
					} else {
						insertWordItem(wordItemsArray[i], wordClassId);
					}
				}
				jsonObj.put("success", true);
				return jsonObj;
			} catch (Exception e) {
				logger.error("保存词条异常" + e.getStackTrace());
				jsonObj.put("success", false);
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 保存别名
	 * 
	 * @param scenariosid    场景ID
	 * @param wordClassId    词类ID
	 * @param wordClass      词类名称
	 * @param standardWordId 词条ID
	 * @param wordItem       词条名称
	 * @param wordIds        别名ID集合，逗号分割
	 * @param synonyms       别名集合，逗号分割
	 * @return
	 */
	public static Object saveWordAlias(String scenariosid, String wordClassId, String wordClass, String standardWordId,
			String wordItem, String wordIds, String synonyms) {
		JSONObject jsonObj = new JSONObject();
		if (StringUtils.isNotBlank(synonyms)) {
			String[] synonymsArray = synonyms.split("\\,");
			try {
				for (int i = 0; i < synonymsArray.length; i++) {
					if (StringUtils.isNotBlank(wordIds) && wordIds.split("\\,").length > 0
							&& StringUtils.isNotBlank(wordIds.split("\\,")[i])) {
						// 更新别名
						updateWordAlias(synonymsArray[i], wordIds.split("\\,")[i], wordClass);
					} else {
						insertWordAlias(synonymsArray[i], wordClassId, wordClass, standardWordId, wordItem);
					}
				}
				jsonObj.put("success", true);
				return jsonObj;
			} catch (Exception e) {
				logger.error("保存别名异常" + e.getStackTrace());
				jsonObj.put("success", false);
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 分页查询词类
	 * 
	 * @param scenariosid 场景ID
	 * @param wordClass   词类名称
	 * @param currentPage 当前页码
	 * @param pageSize    分页条数
	 * @return
	 */
	public static Object listPagingWordClass(String scenariosid, String wordClass, int currentPage, int pageSize) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int totalCount = CommonLibWordclassDAO.getCount(user, wordClass, false, "", "基础");
		JSONArray rows = CommonLibWordclassDAO.select(user, wordClass, false, "", "基础", (currentPage - 1) * pageSize,
				currentPage * pageSize);
		jsonObj.put("success", true);
		jsonObj.put("total", totalCount);
		jsonObj.put("rows", rows);
		return jsonObj;
	}

	/**
	 * 插入词类
	 * 
	 * @param scenariosid   场景ID
	 * @param wordClassList 词类集合
	 * @return
	 */
	public static Object insertWordClass(String scenariosid, List<String> wordClassList) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibWordclassDAO.insertWithSource(user, wordClassList, "", "基础");
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 更新词类
	 * 
	 * @param scenariosid 场景ID
	 * @param wordClassId 词类ID
	 * @param wordClass   词类名称
	 * @return
	 */
	public static Object updateWordClass(String scenariosid, String wordClassId, String wordClass) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibWordclassDAO.update(user, wordClassId, "", wordClass, "", "", "基础");
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 删除词类
	 * 
	 * @param scenariosid 场景ID
	 * @param wordClassId 词类ID
	 * @param wordClass   词类名称
	 * @return
	 */
	public static Object deleteWordClass(String scenariosid, String wordClassId, String wordClass) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibWordclassDAO.delete(user, wordClassId, wordClass, "", "基础");
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 批量保存词类
	 * 
	 * @param scenariosid   场景ID
	 * @param wordClassIds  词类ID集合
	 * @param wordClassList 词类集合
	 * @return
	 */
	public static Object batchSaveWordClass(String scenariosid, String[] wordClassIds, String[] wordClassList) {
		JSONObject jsonObj = new JSONObject();
		List<String> newWordClassList = new ArrayList<String>();
		if (wordClassList != null && wordClassList.length > 0) {
			try {
				for (int i = 0; i < wordClassList.length; i++) {
					String wordClass = wordClassList[i];
					if (wordClassIds != null && wordClassIds.length > 0 && StringUtils.isNotBlank(wordClassIds[i])) {
						if (!newWordClassList.contains(wordClass)) {
							newWordClassList.add(wordClass);
						}
					} else {
						updateWordClass(scenariosid, wordClassIds[i], wordClass);
					}
				}
				if (!newWordClassList.isEmpty()) {
					insertWordClass(scenariosid, newWordClassList);
				}
				jsonObj.put("success", true);
				return jsonObj;
			} catch (Exception e) {
				logger.error("批量保存词类异常：" + e.getStackTrace());
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 根据业务ID查询标准问
	 */
	public static Result listNormalQueryByserviceId(String serviceId) {
		String sql = "select k.KBDATAID,k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.serviceid = ?";
		Result result = Database.executeQuery(sql, serviceId);
		return result;
	}

	/**
	 * 分页查询标准问题
	 * 
	 * @param serviceId   业务ID
	 * @param normalQuery 标准问题
	 * @param currentPage 当前页码
	 * @param pageSize    分页条数
	 * @return
	 */
	public static Object listPagingNormalQuery(String serviceId, String normalQuery, int currentPage, int pageSize) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		Result rs = CommonLibQueryManageDAO.selectNormalQuery(serviceId, normalQuery, "", "", currentPage, pageSize);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject row = new JSONObject();
				row.put("serviceId", serviceId);
				row.put("service", rs.getRows()[0].get("SERVICE").toString());
				row.put("kbdataid", rs.getRows()[0].get("KBDATAID").toString());
				row.put("abstract", rs.getRows()[0].get("ABSTRACT").toString());
				rows.add(row);
			}
			jsonObj.put("success", true);
			jsonObj.put("rows", rows);
			jsonObj.put("total", rs.getRowCount());
			return jsonObj;
		}
		jsonObj.put("total", 0);
		jsonObj.put("success", true);
		return jsonObj;
	}

	/**
	 * 插入标准问题
	 * 
	 * @param serviceId     业务ID
	 * @param normalQuery   标准问题
	 * @param customerQuery 客户问题
	 * @return
	 */
	public static Object insertNormalQuery(String serviceId, String normalQuery, String customerQuery) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibQueryManageDAO.addNormalQueryAndCustomerQuery(serviceId, normalQuery, customerQuery, "全国",
				user, "全国", "全国");
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 更新标准问题
	 * 
	 * @param serviceId     业务ID
	 * @param normalQuery   标准问题
	 * @param customerQuery 客户问题
	 * @return
	 */
	public static Object updateNormalQuery(String serviceId, String service, String normalQuery, String kbdataId) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibQueryManageDAO._updateQuery("全国", service, normalQuery, "", "", "", "", kbdataId, "", "",
				"", "", "全国", user.getIndustryOrganizationApplication(), user);
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 删除标准问题
	 * 
	 * @param serviceId         业务ID
	 * @param normalQueryIdList 标准问题ID集合
	 * @return
	 */
	public static Object deleteNormalQuery(String serviceId, List<String> normalQueryIdList) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		int count = CommonLibQueryManageDAO._deleteNormalQuery(normalQueryIdList, user);
		jsonObj.put("success", count > 0);
		return jsonObj;
	}

	/**
	 * 查询识别规则业务
	 * 
	 * @param sceneType       场景类型：外呼|呼入
	 * @param recognitionType 识别规则业务类型：用户意图|信息收集
	 * @param sceneName       场景名称
	 * @param normalQuery     标准问名称
	 * @param scenariosid     场景ID
	 * @return
	 */
	public static Object listRecognitionRuleNormalQuery(String sceneType, String recognitionType, String sceneName,
			String normalQuery, String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		Set<String> normalQuerySet = new HashSet<String>();
		// 查询公共识别规则业务
		Result result = null;
		if (RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)) {
			result = ScenariosDAO.queryPublicUserIntention();
		}
		if (RegnitionRuleTypeConsts.COLLETION_INTENTION.equals(recognitionType)) {
			result = ScenariosDAO.queryPublicCollectionIntention();
		}
		String abstractStr = null;
		String kbdataid = null;
		String normalQueryStr = null;
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				abstractStr = (String) result.getRows()[i].get("abs");
				kbdataid = String.valueOf(result.getRows()[i].get("kbdataid"));
				normalQueryStr = abstractStr.substring(abstractStr.indexOf(">") + 1);
				if (StringUtils.isNotBlank(normalQuery)) {
					if (normalQueryStr.indexOf(normalQuery) > -1) {
						if (!normalQuerySet.contains(normalQueryStr)) {
							normalQuerySet.add(normalQueryStr);
							JSONObject row = buildRegnitionRuleNormalQueryRow(kbdataid, normalQueryStr, abstractStr, false);
							rows.add(row);
						}
					}
				} else {
					if (!normalQuerySet.contains(normalQueryStr)) {
						normalQuerySet.add(normalQueryStr);
						JSONObject row = buildRegnitionRuleNormalQueryRow(kbdataid, normalQueryStr, abstractStr, false);
						rows.add(row);
					}
				}
			}
		}
		// 查询自定义识别规则业务
		if (RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)
				|| RegnitionRuleTypeConsts.COLLETION_INTENTION.equals(recognitionType)) {
			String serviceId = ScenariosDAO.getRegnitionRuleServiceId(sceneType, sceneName);
			result = ScenariosDAO.listNormalQueryByserviceId(serviceId);
			if (result != null && result.getRowCount() > 0) {
				for (int i = 0; i < result.getRowCount(); i++) {
					abstractStr = (String) result.getRows()[i].get("abs");
					kbdataid = String.valueOf(result.getRows()[i].get("kbdataid"));
					normalQueryStr = abstractStr.substring(abstractStr.indexOf(">") + 1);
					if (StringUtils.isNotBlank(normalQuery)) {
						if (normalQueryStr.indexOf(normalQuery) > -1) {
							if (!normalQuerySet.contains(normalQueryStr)) {
								normalQuerySet.add(normalQueryStr);
								JSONObject row = buildRegnitionRuleNormalQueryRow(kbdataid, normalQueryStr, abstractStr, true);
								rows.add(row);
							}
						}
					} else {
						if (!normalQuerySet.contains(normalQueryStr)) {
							normalQuerySet.add(normalQueryStr);
							JSONObject row = buildRegnitionRuleNormalQueryRow(kbdataid, normalQueryStr, abstractStr, true);
							rows.add(row);
						}
					}
				}
			}
		}
		if (!normalQuerySet.isEmpty()) {
			jsonObj.put("success", true);
			jsonObj.put("total", rows.size());
			jsonObj.put("rows", rows);
		} else {
			jsonObj.put("total", 0);
			jsonObj.put("success", false);
			jsonObj.put("rows", rows);
		}
		return jsonObj;
	}

	/**
	 * 识别规则业务信息
	 * 
	 * @param kbdataid    标准问ID
	 * @param normalQuery 标准问
	 * @param abstractStr 摘要
	 * @param deleteFlag  允许删除
	 * @return
	 */
	private static JSONObject buildRegnitionRuleNormalQueryRow(String kbdataid, String normalQuery, String abstractStr,
			boolean deleteFlag) {
		JSONObject row = new JSONObject();
		row.put("abstract", abstractStr);
		row.put("normalQuery", normalQuery);
		row.put("kbdataid", kbdataid);
		row.put("deleteFlag", deleteFlag);
		return row;
	}

	/**
	 * 批量保存意图
	 * 
	 * @param sceneType           场景类型
	 * @param recognitionType     用户意图或信息收集
	 * @param scenariosName       场景名称
	 * @param normalQueries       标准问名称
	 * @param scenariosid         场景ID
	 * @param returnKeyValueJsons 词模返回值JSON
	 * @return
	 */
	public static Object saveRecognitionRuleNormalQuery(String sceneType, String recognitionType, String scenariosName,
			List<String> kbdataids, List<String> normalQueries, List<String> returnKeyValueJsons, String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		if (normalQueries != null && normalQueries.size() > 0) {
			try {
				for (int i = 0; i < normalQueries.size(); i++) {
					String normalQuery = normalQueries.get(i);
					jsonObj = (JSONObject) ScenariosDAO.listRecognitionRuleNormalQuery(sceneType, recognitionType,
							scenariosName, normalQuery, scenariosid);
					if (StringUtils.isBlank(kbdataids.get(i))) {
						// 不存在则新增
						ScenariosDAO.insertRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName,
								normalQuery, returnKeyValueJsons.get(i), scenariosid);
					} else {
						// 存在的话更新
						ScenariosDAO.updateRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName,
								kbdataids.get(i), normalQuery, scenariosid);
					}
				}
				jsonObj.put("success", true);
				return jsonObj;
			} catch (Exception e) {
				logger.error("批量保存意图异常：" + e);
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 添加识别规则业务标准问
	 * 
	 * @param sceneType             场景类型：外呼|呼入
	 * @param recognitionType       识别规则业务类型：用户意图|信息收集
	 * @param sceneName             场景名称
	 * @param normalQuery           标准问名称
	 * @param returnKeyValueJsonStr 词模返回值JSON
	 * @param scenariosid           场景ID
	 * @return
	 */
	public static Object insertRecognitionRuleNormalQuery(String sceneType, String recognitionType, String sceneName,
			String normalQuery, String returnKeyValueJsonStr, String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		// 查询识别规则业务ID
		String serviceId = ScenariosDAO.getRegnitionRuleServiceId(sceneType, sceneName);
		if (StringUtils.isBlank(serviceId)) {
			return jsonObj;
		}
		// 添加识别规则业务标准问
		jsonObj = (JSONObject) ScenariosDAO.insertNormalQueryWithReturnValues(scenariosid, sceneType, serviceId,
				normalQuery, returnKeyValueJsonStr);
		if (!jsonObj.getBooleanValue("success")) {
			return jsonObj;
		}
		// 用户回答或信息收集词类新增词条
		if (RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)
				|| RegnitionRuleTypeConsts.COLLETION_INTENTION.equals(recognitionType)) {
			String wordClassName = RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)
					? "sys" + sceneName + "用户回答父类"
					: "sys" + sceneName + "信息收集父类";
			jsonObj = (JSONObject) listPagingWordClass(scenariosid, wordClassName, 1, 10);
			if (jsonObj.getIntValue("total") == 0) {
				List<String> wordClassList = new ArrayList<String>();
				wordClassList.add(wordClassName);
				jsonObj = (JSONObject) ScenariosDAO.insertWordClass(scenariosid, wordClassList);
				jsonObj = (JSONObject) listPagingWordClass(scenariosid, wordClassName, 1, 10);
			}
			if (jsonObj.getIntValue("total") > 0) {
				jsonObj = (JSONObject) listPagingWordClass(scenariosid, wordClassName, 1, 10);
				JSONObject row = jsonObj.getJSONArray("rows").getJSONObject(0);
				String wordClassId = row.getString("wordclassid");
				return ScenariosDAO.insertWordItem(normalQuery, wordClassId);
			}
		}
		return jsonObj;
	}

	/**
	 * 更新识别规则业务标准问
	 * 
	 * @param sceneType       场景类型：外呼|呼入
	 * @param recognitionType 识别规则业务类型：用户意图|信息收集
	 * @param sceneName       场景名称
	 * @param normalQuery     标准问名称
	 * @param scenariosid     场景ID
	 * @return
	 */
	public static Object updateRecognitionRuleNormalQuery(String sceneType, String recognitionType, String sceneName,
			String kbdataId, String normalQuery, String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		// 查询识别规则业务ID
		String recognitionServiceId = ScenariosDAO.getRegnitionRuleServiceId(sceneType, sceneName);
		;
		if (StringUtils.isBlank(recognitionServiceId)) {
			return jsonObj;
		}
		// 更新识别规则业务标准问
		return ScenariosDAO.updateNormalQuery(recognitionServiceId, "识别规则业务", normalQuery, kbdataId);
	}

	/**
	 * 删除识别规则业务标准问
	 * 
	 * @param sceneType       场景类型：外呼|呼入
	 * @param recognitionType 识别规则业务类型：用户意图|信息收集
	 * @param sceneName       场景名称
	 * @param kbdataid        标准问ID
	 * @param normalQuery     标准问名称
	 * @param scenariosid     场景ID
	 * @return
	 */
	public static Object deleteRecognitionRuleNormalQuery(String sceneType, String recognitionType, String sceneName,
			String kbdataid, String normalQuery, String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		// 查询识别规则业务ID
		String recognitionServiceId = ScenariosDAO.getRegnitionRuleServiceId(sceneType, sceneName);
		if (StringUtils.isBlank(recognitionServiceId)) {
			return jsonObj;
		}
		// 删除识别规则业务标准问
		List<String> normalQueryIdList = new ArrayList<String>();
		normalQueryIdList.add(kbdataid);
		jsonObj = (JSONObject) deleteNormalQuery(recognitionServiceId, normalQueryIdList);
		if (!jsonObj.getBooleanValue("success")) {
			return jsonObj;
		}
		// 用户回答或信息收集词类删除词条
		if (RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)
				|| RegnitionRuleTypeConsts.COLLETION_INTENTION.equals(recognitionType)) {
			String wordClassName = RegnitionRuleTypeConsts.CUSTOMER_INTENTION.equals(recognitionType)
					? "sys" + sceneName + "用户回答父类"
					: "sys" + sceneName + "信息收集父类";
			jsonObj = (JSONObject) listPagingWordClass(scenariosid, wordClassName, 1, 10);
			if (jsonObj.getIntValue("total") > 0) {
				JSONObject row = jsonObj.getJSONArray("rows").getJSONObject(0);
				String wordClassId = row.getString("wordclassid");
				String wordClass = row.getString("wordclass");
				jsonObj = (JSONObject) ScenariosDAO.listPagingWordItem(scenariosid, wordClassId, wordClass, normalQuery,
						1, 10);
				if (jsonObj.getIntValue("total") > 0) {
					row = jsonObj.getJSONArray("rows").getJSONObject(0);
					String wordId = row.getString("wordid");
					return ScenariosDAO.deleteWordItem(wordId, wordClass, normalQuery);
				}
			}
		}
		jsonObj.put("success", true);
		return jsonObj;
	}

	/**
	 * 保存词类
	 * 
	 * @param scenariosid     场景ID
	 * @param wordClassIdList 词类ID集合
	 * @param wordClassList   词类集合
	 * @return
	 */
	public static Object saveWordClasses(String scenariosid, List<String> wordClassIdList, List<String> wordClassList) {
		JSONObject jsonObj = new JSONObject();
		if (wordClassList != null && !wordClassList.isEmpty()) {
			List<String> newWordClassList = new ArrayList<String>();
			try {
				for (int i = 0; i < wordClassList.size(); i++) {
					String wordClassId = wordClassIdList.get(i);
					String wordClass = wordClassList.get(i);
					if (StringUtils.isNotBlank(wordClassId)) {
						// 更新词类
						ScenariosDAO.updateWordClass(scenariosid, wordClassId, wordClass);
					} else {
						if (!newWordClassList.contains(wordClass)) {
							newWordClassList.add(wordClass);
						}
					}
				}
				// 新增词类
				if (!newWordClassList.isEmpty()) {
					ScenariosDAO.insertWordClass(scenariosid, newWordClassList);
				}
				jsonObj.put("success", true);
				return jsonObj;
			} catch (Exception e) {
				logger.error("保存词类异常" + e.getStackTrace());
			}
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 添加带返回的标准问
	 * 
	 * @param scenariosid           场景ID
	 * @param sceneType             场景类型，外呼|呼入
	 * @param serviceId             业务ID
	 * @param normalQuery           标准问名称
	 * @param returnKeyValueJsonStr 返回值JSON[{"returnKey":"",returnValue:""}]
	 * @return
	 */
	public static Object insertNormalQueryWithReturnValues(String scenariosid, String sceneType, String serviceId,
			String normalQuery, String returnKeyValueJsonStr) {
		JSONObject jsonObj = new JSONObject();
		String cityCode = ScenariosDAO.getCityCode(scenariosid, sceneType); // 地市编码
		User user = (User) GetSession.getSessionByKey("accessUser");
		StringBuffer returnValues = new StringBuffer();
		List<ReturnKeyValue> returnKeyValueList = JSONObject.parseArray(returnKeyValueJsonStr, ReturnKeyValue.class);
		for (ReturnKeyValue returnKeyValue : returnKeyValueList) {
			returnValues.append(returnKeyValue.getReturnKey()).append("=").append(returnKeyValue.getReturnValue())
					.append("&");
		}
		jsonObj = (JSONObject) ScenariosDAO.listPagingNormalQuery(serviceId, normalQuery, 1, 10);
		if (jsonObj.getIntValue("total") == 0) {
			// 不存在插入标准问
			int count = CommonLibQueryManageDAO.addNormalQueryAndCustomerQueryByScene(serviceId, normalQuery, cityCode,
					user, "全国", "全国", returnValues.substring(0, returnValues.length() - 1));
			if (count == 0) {
				jsonObj.put("msg", "添加场景要素值标准问失败");
				jsonObj.put("success", false);
				return jsonObj;
			}
		}
		jsonObj.put("success", true);
		return jsonObj;
	}

	/**
	 * 获取客户问页面跳转地址
	 * 
	 * @param scenariosid           场景ID
	 * @param sceneType             场景类型
	 * @param scenariosName         场景名称
	 * @param normalQuery           标准问
	 * @param customerQuery         客户问
	 * @param returnKeyValueJsonStr 返回值JSON[{"returnKey":"",returnValue:""}]
	 * @return
	 */
	public static Object getCustomerQueryPageUrl(String scenariosid, String sceneType, String scenariosName,
			String normalQuery, String customerQuery, String returnKeyValueJsonStr) {
		JSONObject jsonObj = new JSONObject();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("菜单地址配置", "场景配置之跳转客户问");
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			String customerQueryPageUrl = (String) rsConfig.getRows()[0].get("name");
			User user = (User) GetSession.getSessionByKey("accessUser");
			String userId = user.getUserID();
			String serviceType = user.getIndustryOrganizationApplication();
			String serviceId = ScenariosDAO.getRegnitionRuleServiceId(sceneType, scenariosName); // 业务ID
			String cityCode = ScenariosDAO.getCityCode(scenariosid, sceneType); // 地市编码
			StringBuffer returnValues = new StringBuffer();
			List<ReturnKeyValue> returnKeyValueList = JSONObject.parseArray(returnKeyValueJsonStr,
					ReturnKeyValue.class);
			for (int i = 0; i < returnKeyValueList.size(); i++) {
				ReturnKeyValue returnKeyValue = returnKeyValueList.get(i);
				returnValues.append(returnKeyValue.getReturnKey()).append("#").append(returnKeyValue.getReturnValue());
				if (i < returnKeyValueList.size() - 1) {
					returnValues.append("@@");
				}
			}
			StringBuffer customerQueryParams = new StringBuffer();
			customerQueryParams.append("userid=").append(userId).append("&");
			customerQueryParams.append("ioa=").append(serviceType).append("&");
			customerQueryParams.append("serviceid=").append(serviceId).append("&");
			customerQueryParams.append("normalquery=").append(normalQuery).append("&");
			customerQueryParams.append("customerquery=").append(customerQuery).append("&");
			customerQueryParams.append("returnvalues=").append(returnValues.toString()).append("&");
			customerQueryParams.append("citycode=").append(cityCode);
			jsonObj.put("customerQueryPageUrl", customerQueryPageUrl + "?" + customerQueryParams.toString());
			jsonObj.put("success", true);
			return jsonObj;
		}
		jsonObj.put("msg", "未配置客户问页面跳转地址，请检查");
		jsonObj.put("success", false);
		return jsonObj;
	}

}
