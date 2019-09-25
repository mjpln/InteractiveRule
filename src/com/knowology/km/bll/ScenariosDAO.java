package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.constant.UrlActionInvocationTypeConsts;
import com.knowology.km.pojo.SceneElement;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.pojo.URLActionNode;
import com.knowology.km.pojo.URLActionParam;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

import oracle.sql.CLOB;

public class ScenariosDAO {

	private static Logger logger = Logger.getLogger("ScenariosDAO");
	
	private static final int SceneElementCount = 100;
	
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
				String sceneJsonData = MyUtil.oracleClob2Str((CLOB) result.getRows()[i].get("scenejsondata"));
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
	public static boolean insertSceneRules(String scenariosid, List<SceneRule> sceneRules) {
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
			for (int i = 1; i < 21; i++) {
				insertSql.append(",condition" + i);
			}
			params.add(sceneRule.getCondition1());
			params.add(sceneRule.getCondition2());
			params.add(sceneRule.getCondition3());
			params.add(sceneRule.getCondition4());
			params.add(sceneRule.getCondition5());
			params.add(sceneRule.getCondition6());
			params.add(sceneRule.getCondition7());
			params.add(sceneRule.getCondition8());
			params.add(sceneRule.getCondition9());
			params.add(sceneRule.getCondition10());
			params.add(sceneRule.getCondition11());
			params.add(sceneRule.getCondition12());
			params.add(sceneRule.getCondition13());
			params.add(sceneRule.getCondition14());
			params.add(sceneRule.getCondition15());
			params.add(sceneRule.getCondition16());
			params.add(sceneRule.getCondition17());
			params.add(sceneRule.getCondition18());
			params.add(sceneRule.getCondition19());
			params.add(sceneRule.getCondition20());
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
		sceneRule.setCondition1(conditions[1] == null ? null : conditions[1].trim());
		sceneRule.setCondition2(conditions[2] == null ? null : conditions[2].trim());
		sceneRule.setCondition3(conditions[3] == null ? null : conditions[3].trim());
		sceneRule.setCondition4(conditions[4] == null ? null : conditions[4].trim());
		sceneRule.setCondition5(conditions[5] == null ? null : conditions[5].trim());
		sceneRule.setCondition6(conditions[6] == null ? null : conditions[6].trim());
		sceneRule.setCondition7(conditions[7] == null ? null : conditions[7].trim());
		sceneRule.setCondition8(conditions[8] == null ? null : conditions[8].trim());
		sceneRule.setCondition9(conditions[9] == null ? null : conditions[9].trim());
		sceneRule.setCondition10(conditions[10] == null ? null : conditions[10].trim());
		sceneRule.setCondition11(conditions[11] == null ? null : conditions[11].trim());
		sceneRule.setCondition12(conditions[12] == null ? null : conditions[12].trim());
		sceneRule.setCondition13(conditions[13] == null ? null : conditions[13].trim());
		sceneRule.setCondition14(conditions[14] == null ? null : conditions[14].trim());
		sceneRule.setCondition15(conditions[15] == null ? null : conditions[15].trim());
		sceneRule.setCondition16(conditions[16] == null ? null : conditions[16].trim());
		sceneRule.setCondition17(conditions[17] == null ? null : conditions[17].trim());
		sceneRule.setCondition18(conditions[18] == null ? null : conditions[18].trim());
		sceneRule.setCondition19(conditions[19] == null ? null : conditions[19].trim());
		sceneRule.setCondition20(conditions[20] == null ? null : conditions[20].trim());
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
				if (scenariosid.equals(seneRobotIDConfig.split("::")[0])) {
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
			return ruleResponse.append("###").append(";").toString();
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
		if(StringUtils.isNotBlank(menuOptions)) {
			String[] options = menuOptions.split("\\|");
			for(int i = 0; i < options.length; i++) {
				ruleResponse.append("["+(i+1)+"]").append(options[i]).append("<br/>");
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
		if(StringUtils.isNotBlank(menuOptions)) {
			String[] options = menuOptions.split("\\|");
			for(int i = 0; i < options.length; i++) {
				ruleResponseTemplate.append(options[i]);
				if(i<options.length-1) {
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
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), businessServiceType, "", "全国", "");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and s.parentName='信息收集' and s.brand = ? ";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName);
			return rs;
		}
		return null;
	}
	
	/**
	 * 查询公共用户意图
	 */
	public static Result queryPublicRegnitionRule() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取X行业->通用商家->多渠道应用根问题库
		String businsessServiceType = serviceType.split("->")[0] + "->通用商家->多渠道应用";
		// 获取行业根问题库
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), businsessServiceType, "", "全国", "");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and s.parentName='用户意图' and s.brand = ? ";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName);
			return rs;
		}
		return null;
	}
	
	/**
	 * 获取识别规则业务ID
	 * 
	 * @param parentName 父业务名
	 * @return 识别规则业务ID
	 */
	public static String getRegnitionRuleServiceId(String parentName) {
		String sql = "select SERVICEID from service s where s.service='识别规则业务' and PARENTNAME like ?";
		Result rs = Database.executeQuery(sql, parentName);
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
	 * @param interfaceName 接口名称
	 * @return
	 */
	private static JSONObject insertInterfaceConfigInfo(List<String> standardValues, String interfaceName) {
		User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
		String serviceType = user.getIndustryOrganizationApplication(); // 获取行业
		String standardKey = serviceType + "::" + interfaceName;
		// 存在则先删除
		String standardKeyId = MetafieldDao.getStandardKeyId("第三方接口信息配制", standardKey);
		if(StringUtils.isNotBlank(standardKeyId)) {
			MetafieldDao.deleteKey(standardKeyId, "第三方接口信息配制", standardKey);
		}
		// 插入接口配置键
		List<String> standardKeys = new ArrayList<String>();
		standardKeys.add(standardKey);
		JSONObject jsonObj = (JSONObject) MetafieldDao.insertKey("第三方接口信息配制", standardKeys);
		if(jsonObj.getBooleanValue("success")) {
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
		if(StringUtils.isNotBlank(nameSpace)) {
			standardValues.add("NameSpace:=" + nameSpace);
		}
		if(StringUtils.isNotBlank(actionUrl)) {
			standardValues.add("URL:=" + actionUrl);
		}
		standardValues.add("ParasType:=Json_KeyValue");
		standardValues.add("ReturnParasType:=Json_KeyValue");
		standardValues.add("BufferType:=close");
		if (UrlActionInvocationTypeConsts.WEBSERVICE.equals(invocationWay)) {
			if(StringUtils.isNotBlank(functionName)) {
				standardValues.add("CallFuncName:=" + functionName);
			}
		}
		if (UrlActionInvocationTypeConsts.HTTP.equals(invocationWay)) {
			if(StringUtils.isNotBlank(httpMethod)) {
				standardValues.add("CallType:=" + httpMethod);
			}
		}
		if(StringUtils.isNotBlank(paramOrder)) {
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
		if(!outParams.isEmpty()) {
			for(URLActionParam param : outParams) {
				String outerParamName = param.getParamName();
				String innerParamName = param.getParamValue();
				if(StringUtils.isNotBlank(innerParamName) && StringUtils.isNotBlank(outerParamName)) {
					calledRes2InnerResParams.add("CalledRes2InnerResParas:="+outerParamName+"->"+innerParamName);
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
		if(!inParams.isEmpty()) {
			for(URLActionParam param : inParams) {
				String outerParamName = param.getParamName();
				String innerParamName = param.getParamValue();
				if(StringUtils.isNotBlank(innerParamName) && StringUtils.isNotBlank(outerParamName)) {
					inner2calledParams.add("Inner2CalledParasMap:="+outerParamName+"<-"+innerParamName);
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
		if(!inParams.isEmpty()) {
			for(int i=0; i < inParams.size(); i++) {
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
	 * 查询场景识别规则
	 */
	public static Result querySceneRegnitionRule(String scenariosId) {
		// 获取场景信息
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosId);
		if (result == null || result.getRowCount() == 0) {
			return null;
		}
		// 获取父场景信息
		String parentScenariosId = result.getRows()[0].get("PARENTID") + "";
		result = CommonLibServiceDAO.getServiceInfoByserviceid(parentScenariosId);
		if (result == null || result.getRowCount() == 0) {
			return null;
		}
		String parentScenariosName = result.getRows()[0].get("service") + "";
		// 识别规则业务
		String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service = '识别规则业务' and s.parentname = ?";
		result = Database.executeQuery(sql, parentScenariosName+"问题库");
		return result;
	}
	
	/**
	 * 查询商家识别规则
	 */
	public static Result queryBusinessRegnitionRule() {
		// 获取商家根问题库
		User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
		String serviceType = user.getIndustryOrganizationApplication(); // 获取商家
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), serviceType, "", "全国", "");
		if (result == null || result.getRowCount() == 0) {
			return null;
		}
		String rootServiceName = result.getRows()[0].get("service") + "";
		// 识别规则业务ID
		String serviceId = ScenariosDAO.getRegnitionRuleServiceId(rootServiceName.trim());
		String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.serviceid = ?";
		result = Database.executeQuery(sql, serviceId);
		return result;
	}
	
	/**
	 * 删除识别规则业务
	 * 
	 * @param scenariosid 场景ID
	 * @param normalQuery 标准问
	 * @return
	 */
	public static Object deleteRegnitionRule(String scenariosid, String normalQuery) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		jsonObj.put("msg", "操作失败");
		// 获取商家根问题库
		User user = (User) GetSession.getSessionByKey("accessUser"); // 获取用户
		String serviceType = user.getIndustryOrganizationApplication(); // 获取商家
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), serviceType, "", "全国", "");
		if (result == null || result.getRowCount() == 0) {
			return jsonObj;
		}
		String rootServiceName = result.getRows()[0].get("service") + "";
		// 识别规则业务ID
		String serviceId = ScenariosDAO.getRegnitionRuleServiceId(rootServiceName.trim());
		result = CommonLibQueryManageDAO.selectNormalQuery(serviceId, normalQuery, "", "", 1, 10);
		if (result == null || result.getRowCount() == 0) {
			return jsonObj;
		}
		List<String> list = new ArrayList<String>();
		list.add(result.getRows()[0].get("kbdataid") + "");
		int deleteCount = CommonLibQueryManageDAO._deleteNormalQuery(list , user);
		if(deleteCount == 0) {
			return jsonObj;
		}
		jsonObj.put("success", true);
		jsonObj.put("msg", "操作成功");
		return jsonObj;
	}
	
}
