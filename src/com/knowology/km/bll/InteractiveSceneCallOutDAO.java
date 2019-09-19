package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.knowology.km.constant.CallOutSceneElementConsts;
import com.knowology.km.constant.CollectionStatusConsts;
import com.knowology.km.constant.CollectionTypeConsts;
import com.knowology.km.constant.ParamTypeConsts;
import com.knowology.km.enums.ComparisionRelationEnum;
import com.knowology.km.pojo.AndCondition;
import com.knowology.km.pojo.CollectionNode;
import com.knowology.km.pojo.ConditionNode;
import com.knowology.km.pojo.LinkData;
import com.knowology.km.pojo.NodeData;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.util.getConfigValue;

public class InteractiveSceneCallOutDAO {
	
	private static Logger logger = Logger.getLogger("InteractiveSceneCallOutDAO");


	/**
	 * 信息收集组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param collectionNode 信息收集节点
	 * @param nextNode       跳转节点
	 * @param conditionValue 条件值
	 * @param weight         优先级
	 * @param sceneRules     规则列表
	 * @return
	 */
	public static List<SceneRule> generateCollectionNodeSceneRules(String scenariosid, CollectionNode collectionNode,
			NodeData nextNode, String conditionValue, List<SceneRule> sceneRules) {
		SceneRule sceneRule = null;
		String ruleResponse = null;
		List<String> others = null;
		Map<String, String> setItems = null;
		String collectionVariable = CollectionTypeConsts.SYSTEM_ANY.equals(collectionNode.getCollectionType()) ? "query"
				: collectionNode.getCollectionType();
		// 信息收集成功规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_SUCCESS, nextNode);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(), collectionNode.getKey(), null, null,
				null, null, null, CollectionStatusConsts.COLLCETION_SUCCESS, null, conditionValue, ruleResponse);
		sceneRules.add(sceneRule);
		// 信息收集失败规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_FAIL, null);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(), collectionNode.getKey(), null, null,
				null, null, null, CollectionStatusConsts.COLLCETION_FAIL, null, conditionValue, ruleResponse);
		sceneRules.add(sceneRule);
		// 信息收集跳出规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_OUT, nextNode);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(), collectionNode.getKey(), null, null,
				null, null, null, CollectionStatusConsts.COLLCETION_OUT, null, conditionValue, ruleResponse);
		sceneRules.add(sceneRule);
		// 收集任意类型时，不生成语义理解规则
		if (!CollectionTypeConsts.SYSTEM_ANY.equals(collectionNode.getCollectionType())) {
			sceneRule = InteractiveSceneCallDAO.generateRegnitionRule(scenariosid, null, collectionNode.getKey(),
					null, null, null, null, collectionNode.getCollectionType(), null);
			sceneRules.add(sceneRule);
		}
		// 没有收集到信息，收集次数加1
		String condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + collectionNode.getKey();
		condition += " and " + collectionVariable + "=" + "\"\"";
		condition += " and " + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + "<="
				+ collectionNode.getCollectionTimes();
		others = new ArrayList<String>();
		others.add("ADD(\"" + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + "\",\"1\")");
		String result = ScenariosDAO.getRuleResponse(null, others);
		sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);
		// 收集到信息，设置信息收集状态为成功
		condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + collectionNode.getKey();
		condition += " and " + collectionVariable + "!=" + "\"\"";
		others = new ArrayList<String>();
		setItems = new HashMap<String, String>();
		setItems.put(CallOutSceneElementConsts.COLLECTION_STATUS_ELEMENT_NAME,
				CollectionStatusConsts.COLLCETION_SUCCESS);
		others.add("Input(\"" + collectionNode.getCollectionParam() + "\",\"@" + collectionVariable + "\")");
		others.add("Input(\"" + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + "\",\"\")");
		result = ScenariosDAO.getRuleResponse(setItems, others);
		sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);
		// 没有收集到信息，设置信息收集状态为失败
		condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + collectionNode.getKey();
		condition += " and " + collectionVariable + "=" + "\"\"";
		condition += " and " + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + "<="
				+ collectionNode.getCollectionTimes();
		setItems = new HashMap<String, String>();
		setItems.put(CallOutSceneElementConsts.COLLECTION_STATUS_ELEMENT_NAME, CollectionStatusConsts.COLLCETION_FAIL);
		result = ScenariosDAO.getRuleResponse(setItems, null);
		sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);
		// 收集次数达到限制，设置信息收集状态为跳出
		condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + collectionNode.getKey();
		condition += " and " + collectionVariable + "=" + "\"\"";
		condition += " and " + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + ">"
				+ collectionNode.getCollectionTimes();
		setItems = new HashMap<String, String>();
		setItems.put(CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME, "");
		setItems.put(CallOutSceneElementConsts.COLLECTION_STATUS_ELEMENT_NAME, CollectionStatusConsts.COLLCETION_OUT);
		result = ScenariosDAO.getRuleResponse(setItems, null);
		sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 条件组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       条件节点
	 * @param toNode         跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules      规则集合
	 * @return
	 */
	public static List<SceneRule> generateConditionNodeSceneRules(String scenariosid, ConditionNode fromNode,
			NodeData toNode, String conditionValue, List<SceneRule> sceneRules) {
		if (StringUtils.isBlank(conditionValue)) {
			return sceneRules;
		}
		int conditionIndex = Integer.parseInt(conditionValue.substring(conditionValue.length() - 1));
		if (null == fromNode.getConditions() || fromNode.getConditions().isEmpty()) {
			return sceneRules;
		}
		if (null == fromNode.getConditions().get(conditionIndex)
				|| fromNode.getConditions().get(conditionIndex).isEmpty()) {
			return sceneRules;
		}
		
		List<LinkData> fromLinks = fromNode.getFromLinks();
		if (null != fromLinks && !fromLinks.isEmpty()) {
			for (LinkData fromLink : fromLinks) {
				NodeData fromConditionNode = fromLink.getFromNode();
				String fromNodeText = fromLink.getFromPort().getText();
				// 条件跳转
				sceneRules = InteractiveSceneCallDAO.getLinkSceneRules(scenariosid, fromConditionNode, toNode, fromNodeText, conditionValue, sceneRules);
				// 设置条件值
				String condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + fromConditionNode.getKey();
				condition += " and " +getAndConditions(fromNode.getConditions().get(conditionIndex));
				String result = "SET(\"" + CallOutSceneElementConsts.CONDITION_VALUE_ELEMENT_NAME + "\",\"" + conditionValue
						+ "\")";
				SceneRule sceneRule = InteractiveSceneCallDAO.generateOtherRule(scenariosid, condition, result);
				sceneRules.add(sceneRule);
			}
		}
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
				condition.append(andCondition.getParamName());
				condition.append(ComparisionRelationEnum.getEnum(andCondition.getParamRelation()).getValue());
				if (ParamTypeConsts.STRING.equals(andCondition.getParamType())) {
					condition.append("\"" + andCondition.getParamValue() + "\"");
				}
				if (ParamTypeConsts.INTEGER.equals(andCondition.getParamType())) {
					condition.append(Integer.parseInt(andCondition.getParamValue()));
				}
				if (ParamTypeConsts.VARIABLE.equals(andCondition.getParamType())) {
					condition.append("<@" + andCondition.getParamValue() + ">");
				}
				if (i < andConditions.size() - 1) {
					condition.append(" and ");
				}
			}
		}
		return condition.toString();
	}

	/**
	 * 获取信息收集回复
	 * 
	 * @param collectionNode   信息收集节点
	 * @param collectionStatus 信息收集状态
	 * @param nextNode         跳转节点
	 * @return 回复内容
	 */
	public static String getCollectionRuleResponse(CollectionNode collectionNode, String collectionStatus,
			NodeData nextNode) {
		String result = "";
		Map<String, String> setItems = null;
		List<String> others = new ArrayList<String>();
		if (StringUtils.isBlank(collectionStatus)) {
			// 跳转到信息收集节点
			setItems = new HashMap<String, String>();
			setItems.put("节点名", collectionNode.getKey());
			setItems.put("TTS", collectionNode.getCollectionWords());
			setItems.put("code", "T" + collectionNode.getCollectionWords());
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
			setItems = new HashMap<String, String>();
			setItems.put("节点名", collectionNode.getKey());
			setItems.put("TTS", collectionNode.getCollectionWords());
			setItems.put("code", "T" + collectionNode.getCollectionWords());
			setItems.put("action", ""); // action动作置为空
			setItems.put("actionParams", ""); // action参数置为空
			result += ScenariosDAO.getRuleResponse(setItems);
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
		setItems.put("节点名", conditionNode.getKey());
		setItems.put("TTS", getConfigValue.conditionTTS);
		setItems.put("code", "T" + getConfigValue.conditionTTS);
		setItems.put("action", ""); // action动作置为空
		setItems.put("actionParams", ""); // action参数置为空
		ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
	}

}