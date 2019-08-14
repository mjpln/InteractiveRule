package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

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
import com.knowology.km.pojo.SceneElement;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.util.GetSession;

public class ScenariosDAO {

	private static Logger logger = Logger.getLogger("ScenariosDAO");

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
		String[] conditions = new String[21];
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
		Result result = CommonLibInteractiveSceneDAO.getElementName(scenariosid, "", 1, 100);
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
	
	/**
	 * 查询信息收集类型
	 */
	public static Result queryCollectionType() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取行业根问题库
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(),
				serviceType.split("->")[0] + "->通用商家->多渠道应用", "", "全国", "");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and brand = ? and parentName='信息收集' ";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName);
			return rs;
		}
		return null;
	}
	
	/**
	 * 查询公共意图识别规则
	 */
	public static Result queryPublicRegnitionRule() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 获取行业根问题库
		Result result = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(),
				serviceType.split("->")[0] + "->通用商家->多渠道应用", "", "全国", "");
		if (result != null && result.getRowCount() > 0) {
			String commonQuestionServiceName = result.getRows()[0].get("service") + "";
			String commonQuestionServiceId = result.getRows()[0].get("serviceid") + "";
			String sql = "select k.abstract as abs from service s,kbdata k where s.serviceid = k.serviceid and s.service='识别规则业务' and brand = ? and parentid = ?";
			Result rs = Database.executeQuery(sql, commonQuestionServiceName, commonQuestionServiceId);
			return rs;
		}
		return null;
	}

}
