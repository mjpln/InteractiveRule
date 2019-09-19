package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.km.constant.CallOutSceneElementConsts;
import com.knowology.km.constant.CollectionStatusConsts;
import com.knowology.km.constant.InteracviteTypeConsts;
import com.knowology.km.constant.ParamTypeConsts;
import com.knowology.km.constant.ResponseTypeConsts;
import com.knowology.km.enums.ComparisionRelationEnum;
import com.knowology.km.pojo.AndCondition;
import com.knowology.km.pojo.CallInCollectionNode;
import com.knowology.km.pojo.ConditionNode;
import com.knowology.km.pojo.NodeData;
import com.knowology.km.pojo.SceneElement;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.util.GetSession;

public class InteractiveSceneCallInDAO {
	
	/**
	 * 信息收集组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param collectionNode 信息收集节点
	 * @param nextNode       跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则列表
	 * @param weight         优先级
	 * @return
	 */
	public static List<SceneRule> generateCollectionNodeSceneRules(String scenariosid,
			CallInCollectionNode collectionNode, NodeData nextNode, String conditionValue, 
			List<SceneRule> sceneRules) {
		SceneRule sceneRule = null;
		String ruleResponse = null;
		// 信息收集成功规则
		String interactiveType = collectionNode.getInteractiveType();
		String responseType = ResponseTypeConsts.WRITTEN_RESPONSE_RULE;
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_SUCCESS, nextNode);
		List<SceneElement> sceneElementValues = getCollectionSceneElements(scenariosid, collectionNode.getKey(),
				collectionNode.getCollectionElement(), "已选");
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				responseType);
		sceneRules.add(sceneRule);
		// 信息收集失败规则
		responseType = InteracviteTypeConsts.MENU_OPTIONS.equals(interactiveType)
				? ResponseTypeConsts.MENU_RESPONSE_RULE
				: ResponseTypeConsts.WRITTEN_RESPONSE_RULE;
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_FAIL, null);
		sceneElementValues = getCollectionSceneElements(scenariosid, collectionNode.getKey(), collectionNode.getCollectionElement(),
				"交互");
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				responseType);
		sceneRules.add(sceneRule);
		// 信息收集语义理解规则
		sceneRule = InteractiveSceneCallDAO.generateRegnitionRule(scenariosid, collectionNode.getKey(),
				collectionNode.getKey(), null, null, null, null, collectionNode.getCollectionType(),
				collectionNode.getCollectionElement());
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 信息收集场景要素
	 * 
	 * @param aboveNodeName 上文节点名
	 * @param collectionElementName 关联要素名
	 * @param collectionElementValue 关联要素值
	 * @param scenariosid 
	 * @return 信息收集场景要素集合
	 */
	private static List<SceneElement> getCollectionSceneElements(String scenariosid, String aboveNodeName, String collectionElementName, String collectionElementValue) {
		List<SceneElement> sceneElementValues = new ArrayList<SceneElement>();
		// 上文节点名
		SceneElement sceneElement = new SceneElement();
		sceneElement.setElementName(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME);
		sceneElement.setElementValue(aboveNodeName);
		sceneElementValues.add(sceneElement);
		// 机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		sceneElement = new SceneElement();
		sceneElement.setElementName(CallOutSceneElementConsts.ROBOT_ID_ELEMENT_NAME);
		sceneElement.setElementValue(robotId);
		sceneElementValues.add(sceneElement);
		// 关联要素
		sceneElement = new SceneElement();
		sceneElement.setElementName(collectionElementName);
		sceneElement.setElementValue(collectionElementValue);
		sceneElementValues.add(sceneElement);
		return sceneElementValues;
	}

	/**
	 * 条件组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       条件节点
	 * @param toNode         跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则集合
	 * @return
	 */
	public static List<SceneRule> generateConditionNodeSceneRules(String scenariosid, ConditionNode fromNode,
			NodeData toNode, String conditionValue, List<SceneRule> sceneRules) {
		if (StringUtils.isBlank(conditionValue)) {
			return sceneRules;
		}
		int conditionIndex = Integer.parseInt(conditionValue.substring(conditionValue.length() - 1));
		if (null == fromNode.getConditions() || fromNode.getConditions().isEmpty()
				|| null == fromNode.getConditions().get(conditionIndex)
				|| fromNode.getConditions().get(conditionIndex).isEmpty()) {
			return sceneRules;
		}
		/**
		 * 条件跳转
		 */
		String ruleResponse = InteractiveSceneCallDAO.getCallOutRuleResponse(toNode); // 回复内容
		List<SceneElement> sceneElementValues = new ArrayList<SceneElement>(); // 规则条件
		// 上文节点名
		SceneElement sceneElement = new SceneElement();
		sceneElement.setElementName(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME);
		sceneElement.setElementValue(fromNode.getKey());
		sceneElementValues.add(sceneElement);
		// 机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		sceneElement = new SceneElement();
		sceneElement.setElementName(CallOutSceneElementConsts.ROBOT_ID_ELEMENT_NAME);
		sceneElement.setElementValue(robotId);
		sceneElementValues.add(sceneElement);
		// 条件值
		sceneElement = new SceneElement();
		sceneElement.setElementName(CallOutSceneElementConsts.CONDITION_VALUE_ELEMENT_NAME);
		sceneElement.setElementValue(conditionValue);
		sceneElementValues.add(sceneElement);
		SceneRule sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				ResponseTypeConsts.WRITTEN_RESPONSE_RULE);
		sceneRules.add(sceneRule);
		// 设置条件值
		String condition = getAndConditions(fromNode.getConditions().get(conditionIndex));
		String result = "SET(\"" + CallOutSceneElementConsts.CONDITION_VALUE_ELEMENT_NAME + "\",\"" + conditionValue
				+ "\")";
		sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 获取条件值
	 * 
	 * @param andConditions AND条件集合
	 * @return 条件值
	 */
	private static String getAndConditions(ArrayList<AndCondition> andConditions) {
		StringBuffer condition = new StringBuffer();
		if (!andConditions.isEmpty()) {
			for (int i = 0; i < andConditions.size(); i++) {
				AndCondition andCondition = andConditions.get(i);
				if (StringUtils.isNotBlank(andCondition.getParamValue())) {
					String paramName = andCondition.getParamName();
					String paramRelation = andCondition.getParamRelation();
					String paramType = andCondition.getParamType();
					condition.append(getAndCondition(paramName, paramRelation, paramType, andCondition.getParamValue()));
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
	 * 获取信息收集回复
	 * 
	 * @param collectionNode   信息收集节点
	 * @param collectionStatus 信息收集状态
	 * @param nextNode         跳转节点
	 * @return 回复内容
	 */
	public static String getCollectionRuleResponse(CallInCollectionNode collectionNode, String collectionStatus,
			NodeData nextNode) {
		String result = "";
		Map<String, String> setItems = new HashMap<String, String>();
		List<String> others = new ArrayList<String>();
		if (StringUtils.isBlank(collectionStatus)) {
			// 跳转到信息收集节点
			setItems.put("上文:节点名", collectionNode.getKey());
			setItems.put("节点名", collectionNode.getKey());
			setItems.put("action", ""); // action动作置为空
			setItems.put("actionParams", ""); // action参数置为空
			result = ScenariosDAO.getRuleResponse(setItems, others);
		}
		if (CollectionStatusConsts.COLLCETION_OUT.equals(collectionStatus)
				|| CollectionStatusConsts.COLLCETION_SUCCESS.equals(collectionStatus)) {
			// 信息收集成功或跳出，跳转
			result = InteractiveSceneCallDAO.getCallOutRuleResponse(nextNode);
		}
		if (CollectionStatusConsts.COLLCETION_FAIL.equals(collectionStatus)) {
			// 信息收集失败，重复话术
			String interactiveType = collectionNode.getInteractiveType();
			if (InteracviteTypeConsts.MENU_OPTIONS.equals(interactiveType)) {
				result = ScenariosDAO.getMenuRuleResponseTemplate(collectionNode.getMenuStartWords(),
						collectionNode.getMenuOptions(), collectionNode.getMenuEndWords());
			}
			if (InteracviteTypeConsts.WORD_PATTERN.equals(interactiveType)) {
				setItems.put("TTS", collectionNode.getCollectionWords());
				setItems.put("action", ""); // action动作置为空
				setItems.put("actionParams", ""); // action参数置为空
				result = ScenariosDAO.getRuleResponse(setItems, others);
			}
		}
		return result;
	}

	/**
	 * 获取条件回复
	 * 
	 * @param conditionNode 条件节点
	 * @return 回复内容
	 */
	public static String getConditionRuleResponse(ConditionNode conditionNode) {
		String ruleResponse = "";
		Map<String, String> setItems = new HashMap<String, String>();
		setItems.put("上文:节点名", conditionNode.getKey());
		setItems.put("节点名", conditionNode.getKey());
		setItems.put("action", ""); // action动作置为空
		setItems.put("actionParams", ""); // action参数置为空
		ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
	}

	/**
	 * 新增信息类型
	 * 
	 * @param scenariosid       场景ID
	 * @param collectionType    信息类型
	 * @param simpleWordPattern 简单词模
	 * @param wordPatternType   词模类型
	 * @param request
	 * @return
	 */
	public static Object saveCollectionType(String scenariosid, String collectionType, String simpleWordPattern,
			String wordPatternType, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 信息类型是否已存在
		HashMap<String, String> collectionQuestions = InteractiveSceneCallDAO.queryCollectionQuestions(scenariosid);
		if (!collectionQuestions.isEmpty()) {
			if (collectionQuestions.containsKey(collectionType)) {
				jsonObj.put("checkInfo", "信息类型已存在");
				return jsonObj;
			}
		}
		// 获取场景信息
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosid);
		if (result == null || result.getRowCount() == 0) {
			jsonObj.put("checkInfo", "场景信息未查询到");
			return jsonObj;
		}
		// 获取父场景信息
		String parentScenariosId = result.getRows()[0].get("PARENTID") + "";
		result = CommonLibServiceDAO.getServiceInfoByserviceid(parentScenariosId);
		if (result == null || result.getRowCount() == 0) {
			jsonObj.put("checkInfo", "父场景信息未查询到");
			return jsonObj;
		}
		String parentScenariosName = result.getRows()[0].get("service") + "";
		// 识别规则业务ID
		String serviceId = InteractiveSceneCallDAO.getRegnitionRuleServiceId(parentScenariosName.trim());
		// 获取机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		String cityCode = ScenariosDAO.getRobotCityCode(robotId);
		// 识别业务规则新增标准问
		QuestionManageDAO.createNormalQuery(user, serviceId, collectionType);
		List<String> serviceIds = new ArrayList<String>();
		serviceIds.add(serviceId);
		String kbdataid = InteractiveSceneCallDAO.getCustomerAnswerKbdataId(serviceId, collectionType);
		// 刷新信息类型
		InteractiveSceneCallDAO.initCollectionType(scenariosid);
		// 标准问新增词模
		return InteractiveSceneCallDAO.saveWordpat(kbdataid, collectionType, simpleWordPattern, wordPatternType,
				cityCode, request);
	}

	/**
	 * 查询全部场景要素
	 * 
	 * @param scenariosId      场景ID
	 * @param sceneElementName 场景要素名称
	 * @return
	 */
	public static Object listAllElementName(String scenariosId, String sceneElementName) {
		JSONObject jsonObj = (JSONObject) SceneElementDAO.listAllElementName(scenariosId, sceneElementName);
		if (jsonObj.getIntValue("total") > 0) {
			JSONArray newRows = new JSONArray();
			JSONArray rows = jsonObj.getJSONArray("rows");
			for (int i = 0; i < rows.size(); i++) {
				JSONObject row = rows.getJSONObject(i);
				if (!checkIfSystemSceneElements(row.getString("name"))) {
					newRows.add(row);
				}
			}
			jsonObj.put("rows", newRows);
			jsonObj.put("total", newRows.size());
		}
		return jsonObj;
	}

	/**
	 * 分页查询场景要素
	 * 
	 * @param scenariosId      场景ID
	 * @param sceneElementName 场景要素名称
	 * @param currentPage      当前页码
	 * @param pageSize         分页条数
	 * @return
	 */
	public static Object listPagingSceneElement(String scenariosId, String sceneElementName, int currentPage,
			int pageSize) {
		JSONObject jsonObj = new JSONObject();
		JSONArray newRows = new JSONArray();
		int totalCount = CommonLibInteractiveSceneDAO.getElementNameCount(scenariosId, sceneElementName);
		if (totalCount > 10) {
			int totalPage = totalCount / pageSize + 1;
			while (currentPage <= totalPage) {
				jsonObj = (JSONObject) SceneElementDAO.listPagingSceneElements(scenariosId, sceneElementName,
						currentPage, pageSize);
				JSONArray rows = jsonObj.getJSONArray("rows");
				if (rows.size() > 0) {
					for (int i = 0; i < rows.size(); i++) {
						JSONObject row = rows.getJSONObject(i);
						if (!checkIfSystemSceneElements(row.getString("name"))) {
							newRows.add(row);
						}
					}
					if (newRows.size() > 0) {
						break;
					}
					listPagingSceneElement(scenariosId, sceneElementName, ++currentPage, pageSize);
				}
			}
		}
		jsonObj.put("rows", newRows);
		jsonObj.put("total", newRows.size());
		return jsonObj;
	}

	/**
	 * 判断系统场景要素
	 * 
	 * @param sceneElementName 场景要素名称
	 * @return true 是 false 否
	 */
	private static boolean checkIfSystemSceneElements(String sceneElementName) {
		Set<String> systemSceneElements = CallOutSceneElementConsts.getAllSceneElements();
		if (systemSceneElements.contains(sceneElementName)) {
			return true;
		}
		return false;
	}

	/**
	 * 查询场景要素值
	 * 
	 * @param sceneElementName 场景要素名称
	 * @param scenariosid      场景ID
	 * @return 场景要素值集合
	 */
	public static Object listAllElementValue(String scenariosid, String sceneElementName) {
		JSONObject jsonObj = (JSONObject) InteractiveSceneDAO.queryElement(scenariosid);
		if (jsonObj != null && jsonObj.getJSONArray("rows") != null) {
			JSONArray seneElements = jsonObj.getJSONArray("rows");
			for (int i = 0; i < seneElements.size(); i++) {
				JSONObject sceneElement = seneElements.getJSONObject(i);
				if (sceneElementName.equals(sceneElement.getString("name"))) {
					return sceneElement.getJSONArray("elementvalue");
				}
			}
		}
		return null;
	}

}