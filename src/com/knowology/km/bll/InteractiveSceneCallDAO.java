package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPatternkeyDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.bll.CommonLibWordclassDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.dal.Database;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.constant.ActionTypeConsts;
import com.knowology.km.constant.CallOutNodeTypeConsts;
import com.knowology.km.constant.CallOutSceneElementConsts;
import com.knowology.km.constant.CollectionIntentionConsts;
import com.knowology.km.constant.CollectionStatusConsts;
import com.knowology.km.constant.InteracviteTypeConsts;
import com.knowology.km.constant.ParamTypeConsts;
import com.knowology.km.constant.ResponseTypeConsts;
import com.knowology.km.constant.RuleTypeConsts;
import com.knowology.km.constant.SceneTypeConsts;
import com.knowology.km.constant.UrlActionInvocationTypeConsts;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.enums.CollectionTypeEnum;
import com.knowology.km.enums.ComparisionRelationEnum;
import com.knowology.km.pojo.AndCondition;
import com.knowology.km.pojo.CollectionNode;
import com.knowology.km.pojo.ConditionInfo;
import com.knowology.km.pojo.ConditionNode;
import com.knowology.km.pojo.DTMFNode;
import com.knowology.km.pojo.LinkData;
import com.knowology.km.pojo.NodeData;
import com.knowology.km.pojo.NodePort;
import com.knowology.km.pojo.OtherResponse;
import com.knowology.km.pojo.SceneElement;
import com.knowology.km.pojo.SceneRule;
import com.knowology.km.pojo.TTSNode;
import com.knowology.km.pojo.TransferNode;
import com.knowology.km.pojo.URLActionNode;
import com.knowology.km.pojo.URLActionParam;
import com.knowology.km.util.CheckInput;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.HttpclientUtil;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ResponseData;
import com.knowology.km.util.SimpleString;
import com.knowology.km.util.getServiceClient;

public class InteractiveSceneCallDAO {

	private static Logger logger = Logger.getLogger("InteractiveSceneCallOutDAO");

	private static int weight;
	private static String sceneType;

	/**
	 * 保存流程图
	 * 
	 * @param sceneJson   流程图JSON数据
	 * @param scenariosid 场景ID
	 * @param sceneType   场景类型
	 * @return 保存结果
	 */
	public static Object saveSceneRules(String sceneJson, String scenariosid, String scenariosType) {
		queryRecognitionQuestions(scenariosid);
		queryCollectionQuestions(scenariosid);
		weight = 0;
		sceneType = scenariosType;
		// 保存数据
		boolean saveResult = ScenariosDAO.insertSceneJsonData(scenariosid, sceneJson);
		if (!saveResult) {
			return fail();
		}
		// 解析数据
		List<NodeData> nodeDataList = parseSceneJson(sceneJson);
		// 节点排序
		nodeDataList = sortNodeDataList(nodeDataList);
		// 查询场景要素
		List<SceneElement> scenariosElementList = ScenariosDAO.getSceneElements(scenariosid);
		if (scenariosElementList == null || scenariosElementList.isEmpty()) {
			logger.error("未查询到场景要素，场景ID=" + scenariosid);
			return fail();
		}
		// 查询机器人ID
		if (SceneTypeConsts.CALL_OUT.equals(sceneType)) {
			String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
			if (StringUtils.isBlank(robotId)) {
				logger.error("未查询到机器人ID，场景ID=" + scenariosid);
				return fail();
			}
		}
		// 生成规则
		List<SceneRule> sceneRules = generateSceneRules(scenariosid, nodeDataList);
		// 保存规则
		try {
			saveResult = ScenariosDAO.insertSceneRules(scenariosid, sceneRules);
			if (saveResult) {
				return success();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return fail();
		}
		return fail();
	}

	/**
	 * 节点排序
	 */
	private static List<NodeData> sortNodeDataList(List<NodeData> nodeDataList) {
		if (nodeDataList == null || nodeDataList.isEmpty()) {
			return nodeDataList;
		}
		ArrayList<NodeData> newNodeDataList = new ArrayList<NodeData>();
		LinkedHashSet<String> loopNodeKeys = new LinkedHashSet<String>();
		Map<String, NodeData> nodeDataKeys = new HashMap<String, NodeData>();
		for (NodeData nodeData : nodeDataList) {
			nodeDataKeys.put(nodeData.getKey(), nodeData);
		}
		// 开始节点
		NodeData startNode = nodeDataKeys.get(CallOutNodeTypeConsts.START_NODE);
		newNodeDataList.add(startNode);
		loopNodeKeys.add(CallOutNodeTypeConsts.START_NODE);
		LoopNodeData(startNode, nodeDataKeys, loopNodeKeys, newNodeDataList);
		return newNodeDataList;
	}

	private synchronized static List<NodeData> LoopNodeData(NodeData nodeData, Map<String, NodeData> nodeDataKeys,
			LinkedHashSet<String> loopNodeKeys, ArrayList<NodeData> newNodeDataList) {
		if (!CallOutNodeTypeConsts.END_NODE.equals(nodeData.getCategory())) {
			if (nodeData.getFromLinks() != null && !nodeData.getFromLinks().isEmpty()) {
				for (LinkData linkData : nodeData.getFromLinks()) {
					NodeData fromNode = linkData.getFromNode();
					if (!newNodeDataList.contains(fromNode)) {
						if (newNodeDataList.contains(nodeData)) {
							newNodeDataList.remove(nodeData);
						}
						newNodeDataList.add(fromNode);
						newNodeDataList.add(nodeData);
					}
				}
			}
			if (nodeData.getToLinks() != null && !nodeData.getToLinks().isEmpty()) {
				for (LinkData linkData : nodeData.getToLinks()) {
					NodeData toNode = linkData.getToNode();
					if (!newNodeDataList.contains(toNode)) {
						newNodeDataList.add(toNode);
					}
				}
				for (LinkData linkData : nodeData.getToLinks()) {
					NodeData toNode = linkData.getToNode();
					if (!loopNodeKeys.contains(toNode.getKey())) {
						loopNodeKeys.add(toNode.getKey());
						LoopNodeData(nodeDataKeys.get(toNode.getKey()), nodeDataKeys, loopNodeKeys, newNodeDataList);
					}
				}
			}
		}
		return newNodeDataList;
	}

	/**
	 * 生成规则
	 * 
	 * @param scenariosid  场景ID
	 * @param nodeDataList 节点数据
	 * @param sceneType    场景类型
	 * @return 规则列表
	 */
	private static List<SceneRule> generateSceneRules(String scenariosid, List<NodeData> nodeDataList) {
		List<SceneRule> sceneRules = new ArrayList<SceneRule>();
		SceneRule sceneRule = null;
		int weight = 0;
		for (NodeData nodaData : nodeDataList) {
			List<LinkData> toLinks = nodaData.getToLinks();
			for (int linkIndex = 0; linkIndex < toLinks.size(); linkIndex++) {
				LinkData toLink = toLinks.get(linkIndex);
				NodeData fromNode = toLink.getFromNode();
				NodeData toNode = toLink.getToNode();
				String fromPortText = toLink.getFromPort().getText();
				sceneRules = getLinkSceneRules(scenariosid, fromNode, toNode, linkIndex, fromPortText, sceneRules);
			}
		}
		// 跳出 语义理解规则
		sceneRule = generateJumpOutRegnitionRule(scenariosid, weight++);
		sceneRules.add(sceneRule);

		// 未理解 语义理解规则
		sceneRule = generateNotRegnitionRule(scenariosid, weight++);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 根据连线生成规则
	 * 
	 * @param scenariosid  场景ID
	 * @param fromNode     源节点
	 * @param toNode       目的节点
	 * @param fromPortText 源端口
	 * @param sceneRules   规则列表
	 * @return
	 */
	public static List<SceneRule> getLinkSceneRules(String scenariosid, NodeData fromNode, NodeData toNode,
			int linkIndex, String fromPortText, List<SceneRule> sceneRules) {
		String fromNodeCategory = fromNode.getCategory();
		// 开始组件
		if (CallOutNodeTypeConsts.START_NODE.equals(fromNodeCategory)) {
			sceneRules = generateStartSceneRules(scenariosid, toNode, sceneRules);
		}
		// 放音组件
		if (CallOutNodeTypeConsts.TTS_NODE.equals(fromNodeCategory)) {
			String customerAnswer = fromPortText;
			sceneRules = generateTTSSceneRules(scenariosid, (TTSNode) fromNode, toNode, customerAnswer, sceneRules);
		}
		// 信息收集组件
		if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(fromNodeCategory)) {
			sceneRules = generateCollectionNodeSceneRules(scenariosid, (CollectionNode) fromNode, toNode, sceneRules);
		}
		// DTMF按键组件
		if (CallOutNodeTypeConsts.DTMF_NODE.equals(fromNodeCategory)) {
			String isGetPressNumber = fromPortText;
			sceneRules = generateDTMFNodeSceneRules(scenariosid, (DTMFNode) fromNode, toNode, isGetPressNumber,
					sceneRules);
		}
		// 转人工组件
		if (CallOutNodeTypeConsts.TRANSFER_NODE.equals(fromNodeCategory)) {
			sceneRules = generateTransferNodeSceneRules(scenariosid, (TransferNode) fromNode, toNode, sceneRules);
		}
		// 动作组件
		if (CallOutNodeTypeConsts.URL_ACTION_NODE.equals(fromNodeCategory)) {
			sceneRules = generateURLActionNodeSceneRules(scenariosid, (URLActionNode) fromNode, toNode, sceneRules);
		}
		// 条件组件
		if (CallOutNodeTypeConsts.CONDITION_NODE.equals(fromNodeCategory)) {
			String conditionName = fromPortText;
			sceneRules = generateConditionNodeSceneRules(scenariosid, (ConditionNode) fromNode, toNode, conditionName,
					sceneRules);
		}
		return sceneRules;
	}

	/**
	 * 信息收集组件 生成规则
	 */
	private static List<SceneRule> generateCollectionNodeSceneRules(String scenariosid, CollectionNode collectionNode,
			NodeData nextNode, List<SceneRule> sceneRules) {
		switch (CollectionTypeEnum.getEnum(collectionNode.getCollectionType())) {
		case ELEMENT_COLLECTION:
			return InteractiveSceneCallDAO.generateElementCollectionNodeSceneRules(scenariosid, collectionNode,
					nextNode, sceneRules);
		case USER_INFO_COLLECTION:
			return InteractiveSceneCallDAO.generateUserInfoCollectionNodeSceneRules2(scenariosid, collectionNode,
					nextNode, sceneRules);
		default:
			break;
		}
		return sceneRules;
	}

	/**
	 * 开始组件 生成规则
	 * 
	 * @param scenariosid 场景ID
	 * @param toNode      连接组件
	 * @param weight      优先级
	 * @param sceneRules  规则集合
	 * @return
	 */
	private static List<SceneRule> generateStartSceneRules(String scenariosid, NodeData toNode,
			List<SceneRule> sceneRules) {
		String ruleResponse = "";
		if (SceneTypeConsts.CALL_OUT.equals(sceneType)) {
			ruleResponse += "业务信息获取(\"用户信息查询\")";
		}
		ruleResponse += getCallRuleResponse(toNode);
		SceneRule sceneRule = generateInteractiveRule(scenariosid, null, "交互", null, null, null, null, null, null, null,
				ruleResponse);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * DTMF按键组件 生成规则
	 * 
	 * @param scenariosid      场景ID
	 * @param fromNode         DTMF组件
	 * @param toNode           连接组件
	 * @param isGetPressNumber 是否获取按键值
	 * @param conditionValue   条件值
	 * @param sceneRules       规则集合
	 * @return
	 */
	private static List<SceneRule> generateDTMFNodeSceneRules(String scenariosid, DTMFNode fromNode, NodeData toNode,
			String isGetPressNumber, List<SceneRule> sceneRules) {
		// 设置是否获取到按键值标识:是
		String condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + fromNode.getKey();
		condition += " and query" + "!=" + "\"\"";
		String result = "SET(\"" + CallOutSceneElementConsts.DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME + "\",\"是\");";
		result += "SET(\"" + fromNode.getDtmfAlias() + "\",\"@query\");";
		SceneRule sceneRule = generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);

		// 设置是否获取到按键值标识:否
		condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + fromNode.getKey();
		condition += " and query" + "=" + "\"未获取到按键值\"";
		result = "SET(\"" + CallOutSceneElementConsts.DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME + "\",\"否\")";
		sceneRule = generateOtherRule(scenariosid, condition, result);
		sceneRules.add(sceneRule);

		// 未获取到按键值识别规则
		sceneRule = generateEmptyPressNumberRegnitionRule(scenariosid, fromNode.getKey());
		sceneRules.add(sceneRule);
		if ("获取到按键值".equals(isGetPressNumber)) {
			// 获取到按键值，跳转节点
			String ruleResponse = getCallRuleResponse(toNode);
			sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(), null, null, null, null, null,
					null, "是", ruleResponse);
			sceneRules.add(sceneRule);
		}
		if ("未获取到按键值".equals(isGetPressNumber)) {
			// 未获取到按键值，跳转节点
			String ruleResponse = getCallRuleResponse(toNode);
			sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(), null, null, null, null, null,
					null, "否", ruleResponse);
			sceneRules.add(sceneRule);
		}
		return sceneRules;
	}

	/**
	 * 放音组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       TTS组件
	 * @param toNode         连接组件
	 * @param fromPort       用户回答
	 * @param conditionValue 条件值
	 * @param weight         优先级
	 * @param sceneRules     规则集合
	 * @return
	 */
	private static List<SceneRule> generateTTSSceneRules(String scenariosid, TTSNode fromNode, NodeData toNode,
			String customerAnswer, List<SceneRule> sceneRules) {
		boolean isSetDifferentiated = isSetDifferentiated(toNode); // 是否设置区分节点
		String aboveNodeName = fromNode.getKey(); // 上文节点名
		String recognitionStatus = "跳出".equals(customerAnswer) ? "跳出" : null; // 理解状态
		String differentiatedNode = isSetDifferentiated ? toNode.getKey() : null; // 区分节点名
		// 意图交互规则
		if (!"跳出".equals(customerAnswer) && !"跳转".equals(customerAnswer)) {
			// 系统已选或已告知规则
			String ruleResponse = getCallRuleResponse(toNode);
			SceneRule sceneRule = generateInteractiveRule(scenariosid, toNode.getKey(), aboveNodeName, customerAnswer,
					recognitionStatus, null, differentiatedNode, null, null, null, ruleResponse);
			sceneRules.add(sceneRule);
		}
		if ("跳转".equals(customerAnswer)) {
			String ruleResponse = getCallRuleResponse(toNode);
			SceneRule sceneRule = generateInteractiveRule(scenariosid, toNode.getKey(), aboveNodeName, null, null, null,
					null, null, null, null, ruleResponse);
			sceneRules.add(sceneRule);
		}
		if ("跳出".equals(customerAnswer)) {
			// 当前节点跳出时，所有上文节点需要配置跳出规则
			for (LinkData fromLink : fromNode.getFromLinks()) {
				String ruleResponse = getCallRuleResponse(toNode);
				if (CallOutNodeTypeConsts.START_NODE.equals(fromLink.getFromNode().getCategory())) {
					SceneRule sceneRule = generateInteractiveRule(scenariosid, fromNode.getKey(), "交互", null, "跳出",
							null, null, null, null, null, ruleResponse);
					sceneRules.add(sceneRule);
				} else {
					SceneRule sceneRule = generateInteractiveRule(scenariosid, fromNode.getKey(),
							fromLink.getFromNode().getKey(), "已选", "跳出", null, fromNode.getKey(), null, null, null,
							ruleResponse);
					sceneRules.add(sceneRule);
				}
			}
		}
		// 意图识别规则
		if (!"跳出".equals(customerAnswer) && !"未理解".equals(customerAnswer) && !"跳转".equals(customerAnswer)) {
			SceneRule sceneRule = generateRegnitionRule(scenariosid, toNode.getKey(), aboveNodeName, customerAnswer,
					null, null, differentiatedNode, null, null);
			sceneRules.add(sceneRule);
		}
		return sceneRules;
	}

	/**
	 * 动作组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       动作组件
	 * @param toNode         跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则集合
	 * @return
	 */
	private static List<SceneRule> generateURLActionNodeSceneRules(String scenariosid, URLActionNode fromNode,
			NodeData toNode, List<SceneRule> sceneRules) {
		// 跳转节点
		String ruleResponse = getCallRuleResponse(toNode);
		SceneRule sceneRule = generateInteractiveRule(scenariosid, fromNode.getKey(), fromNode.getKey(), null, null,
				null, null, null, null, null, ruleResponse);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 条件组件 生成规则
	 * 
	 * @param scenariosid   场景ID
	 * @param fromNode      条件节点
	 * @param toNode        跳转节点
	 * @param conditionName 条件名
	 * @param sceneRules    规则集合
	 * @return
	 */
	public static List<SceneRule> generateConditionNodeSceneRules(String scenariosid, ConditionNode fromNode,
			NodeData toNode, String conditionName, List<SceneRule> sceneRules) {
		/**
		 * 条件跳转
		 */
		String ruleResponse = InteractiveSceneCallDAO.getCallRuleResponse(toNode); // 回复内容
		logger.info(conditionName + "------->" + ruleResponse);
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
		ArrayList<AndCondition> andConditions = getAndConditions(fromNode, conditionName);
		if (andConditions != null && !andConditions.isEmpty()) {
			for (int i = 0; i < andConditions.size(); i++) {
				AndCondition andCondition = andConditions.get(i);
				if (StringUtils.isNotBlank(andCondition.getParamValue())) {
					String paramName = andCondition.getParamName();
					String paramRelation = ComparisionRelationEnum.getEnum(andCondition.getParamRelation()).getValue();
					String paramType = andCondition.getParamType();
					String paramValue = andCondition.getParamValue();
					// 插入条件值要素
					insertOtherConditionElement(scenariosid, paramName);
					if (ParamTypeConsts.VARIABLE.equals(paramType)) {
						paramValue = "<@" + paramValue + ">";
					}
					sceneElement = new SceneElement();
					sceneElement.setElementName(paramName);
					sceneElement.setElementValue(paramRelation + paramValue);
					sceneElementValues.add(sceneElement);
				}
				// 最后一个AND条件满足才发生跳转
				if (i < andConditions.size() - 1) {
					SceneRule sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, "",
							sceneElementValues, ResponseTypeConsts.WRITTEN_RESPONSE_RULE);
					sceneRules.add(sceneRule);
				} else if (i == andConditions.size() - 1) {
					SceneRule sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse,
							sceneElementValues, ResponseTypeConsts.WRITTEN_RESPONSE_RULE);
					sceneRules.add(sceneRule);
				}
			}
		}
		return sceneRules;
	}

	/**
	 * 根据条件名获取AND条件
	 * 
	 * @param fromNode      条件节点
	 * @param conditionName 条件名
	 * @return AND条件集合
	 */
	private static ArrayList<AndCondition> getAndConditions(ConditionNode fromNode, String conditionName) {
		if (fromNode.getConditions() != null && !fromNode.getConditions().isEmpty()) {
			for (ConditionInfo condition : fromNode.getConditions()) {
				if (condition.getConditionName().equals(conditionName)) {
					return condition.getAndConditions();
				}
			}
		}
		return null;
	}

	/**
	 * 插入其他条件要素
	 */
	private static boolean insertOtherConditionElement(String scenariosid, String paramName) {
		List<SceneElement> scenariosElementList = ScenariosDAO.getSceneElements(scenariosid);
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosid);
		String scenariosName = result.getRows()[0].get("service") + "";
		String weight = String
				.valueOf(Integer.parseInt(scenariosElementList.get(scenariosElementList.size() - 1).getWeight()) + 1);
		JSONObject jsonObj = (JSONObject) InteractiveSceneDAO.insertElementName(scenariosid, scenariosName, "", "", "",
				"", paramName, "", weight, "", "");
		return jsonObj.getBooleanValue("success");
	}

	/**
	 * 转人工组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       转人工节点
	 * @param toNode         跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则集合
	 * @return
	 */
	private static List<SceneRule> generateTransferNodeSceneRules(String scenariosid, TransferNode fromNode,
			NodeData toNode, List<SceneRule> sceneRules) {
		// 跳转节点
		String ruleResponse = getCallRuleResponse(toNode);
		SceneRule sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(), null, null, null, null,
				null, null, null, ruleResponse);
		sceneRules.add(sceneRule);
		return sceneRules;
	}

	/**
	 * 信息收集组件 生成要素采集规则
	 * 
	 * @param scenariosid    场景ID
	 * @param collectionNode 信息收集节点
	 * @param nextNode       跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则列表
	 * @param weight         优先级
	 * @return
	 */
	public static List<SceneRule> generateElementCollectionNodeSceneRules(String scenariosid,
			CollectionNode collectionNode, NodeData nextNode, List<SceneRule> sceneRules) {
		SceneRule sceneRule = null;
		String ruleResponse = null;
		String interactiveType = collectionNode.getInteractiveType();
		String responseType = ResponseTypeConsts.WRITTEN_RESPONSE_RULE;
		String collectionIntention = collectionNode.getCollectionIntention();
		// 信息收集成功规则
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
		sceneElementValues = getCollectionSceneElements(scenariosid, collectionNode.getKey(),
				collectionNode.getCollectionElement(), "交互");
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				responseType);
		sceneRules.add(sceneRule);
		// 信息收集语义理解规则
		if (!CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention)) {
			sceneRule = InteractiveSceneCallDAO.generateRegnitionRule(scenariosid, collectionNode.getKey(),
					collectionNode.getKey(), null, null, null, null, collectionNode.getCollectionIntention(),
					collectionNode.getCollectionParam());
			sceneRules.add(sceneRule);
		} 
		return sceneRules;
	}

	/**
	 * 信息收集组件 用户信息采集规则
	 * 
	 * @param scenariosid    场景ID
	 * @param collectionNode 信息收集节点
	 * @param nextNode       跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules     规则列表
	 * @param weight         优先级
	 * @return
	 */
	public static List<SceneRule> generateUserInfoCollectionNodeSceneRules2(String scenariosid,
			CollectionNode collectionNode, NodeData nextNode, List<SceneRule> sceneRules) {
		SceneRule sceneRule = null;
		String ruleResponse = null;
		String interactiveType = collectionNode.getInteractiveType();
		String responseType = ResponseTypeConsts.WRITTEN_RESPONSE_RULE;
		String collectionIntention = collectionNode.getCollectionIntention();
		String collectionParam = collectionNode.getCollectionParam();
		// 插入信息收集要素
		insertOtherConditionElement(scenariosid, collectionParam);
		// 信息收集成功规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_SUCCESS, nextNode);
		List<SceneElement> sceneElementValues = getCollectionSceneElements(scenariosid, collectionNode.getKey(),
				collectionNode.getCollectionParam(), "已选");
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				responseType);
		sceneRules.add(sceneRule);
		// 信息收集失败规则
		responseType = InteracviteTypeConsts.MENU_OPTIONS.equals(interactiveType)
				? ResponseTypeConsts.MENU_RESPONSE_RULE
				: ResponseTypeConsts.WRITTEN_RESPONSE_RULE;
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_FAIL, null);
		sceneElementValues = getCollectionSceneElements(scenariosid, collectionNode.getKey(),
				collectionNode.getCollectionParam(), "交互");
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, ruleResponse, sceneElementValues,
				responseType);
		sceneRules.add(sceneRule);
		// 信息收集语义理解规则
		if (!CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention)) {
			sceneRule = InteractiveSceneCallDAO.generateRegnitionRule(scenariosid, collectionNode.getKey(),
					collectionNode.getKey(), null, null, null, null, collectionNode.getCollectionIntention(),
					collectionNode.getCollectionParam());
			sceneRules.add(sceneRule);
		}
		return sceneRules;
	}

	/**
	 * 信息收集组件 用户信息采集规则
	 * 
	 * @param scenariosid    场景ID
	 * @param collectionNode 信息收集节点
	 * @param nextNode       跳转节点
	 * @param conditionValue 条件值
	 * @param weight         优先级
	 * @param sceneRules     规则列表
	 * @return
	 */
	public static List<SceneRule> generateUserInfoCollectionNodeSceneRules(String scenariosid,
			CollectionNode collectionNode, NodeData nextNode, List<SceneRule> sceneRules) {
		SceneRule sceneRule = null;
		String ruleResponse = null;
		List<String> others = null;
		Map<String, String> setItems = null;
		String collectionIntention = collectionNode.getCollectionIntention();
		String collectionParam = collectionNode.getCollectionParam();
		String collectionVariable = CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention) ? "query"
				: collectionParam;
		// 信息收集成功规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_SUCCESS, nextNode);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(),
				collectionNode.getKey(), null, null, null, null, null, CollectionStatusConsts.COLLCETION_SUCCESS, null,
				ruleResponse);
		sceneRules.add(sceneRule);
		// 信息收集失败规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_FAIL, null);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(),
				collectionNode.getKey(), null, null, null, null, null, CollectionStatusConsts.COLLCETION_FAIL, null,
				ruleResponse);
		sceneRules.add(sceneRule);
		// 信息收集跳出规则
		ruleResponse = getCollectionRuleResponse(collectionNode, CollectionStatusConsts.COLLCETION_OUT, nextNode);
		sceneRule = InteractiveSceneCallDAO.generateInteractiveRule(scenariosid, collectionNode.getKey(),
				collectionNode.getKey(), null, null, null, null, null, CollectionStatusConsts.COLLCETION_OUT, null,
				ruleResponse);
		sceneRules.add(sceneRule);
		// 收集任意类型时，不生成语义理解规则
		if (!CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention)) {
			sceneRule = InteractiveSceneCallDAO.generateRegnitionRule(scenariosid, null, collectionNode.getKey(), null,
					null, null, null, collectionIntention, collectionNode.getCollectionParam());
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
		others.add("SET(\"" + CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME + "\",\"\")");
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
	 * 要素采集场景要素
	 * 
	 * @param aboveNodeName   上文节点名
	 * @param collectionParam 信息收集参数
	 * @param collectionValue 信息收集参数值
	 * @param scenariosid
	 * @return 信息收集场景要素集合
	 */
	private static List<SceneElement> getCollectionSceneElements(String scenariosid, String aboveNodeName,
			String collectionParam, String collectionValue) {
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
		// 信息收集参数
		sceneElement = new SceneElement();
		sceneElement.setElementName(collectionParam);
		sceneElement.setElementValue(collectionValue);
		sceneElementValues.add(sceneElement);
		return sceneElementValues;
	}

	/**
	 * 获取回复内容
	 */
	public static String getCallRuleResponse(NodeData toNode) {
		Map<String, String> setItems = new HashMap<String, String>();
		setItems.put("是否末梢编码", StringUtils.isBlank(toNode.getEndFlag()) ? "否" : toNode.getEndFlag());
		setItems.put("action", ""); // action动作置为空
		setItems.put("actionParams", ""); // action参数置为空
		String category = toNode.getCategory();
		String ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		if (CallOutNodeTypeConsts.TTS_NODE.equals(category) || CallOutNodeTypeConsts.END_NODE.equals(category)) {
			return ruleResponse + getTTSRuleResponse((TTSNode) toNode);
		}
		if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(category)) {
			return ruleResponse + getCollectionRuleResponse((CollectionNode) toNode, null, null);
		}
		if (CallOutNodeTypeConsts.DTMF_NODE.equals(category)) {
			return ruleResponse + getDTMFRuleResponse((DTMFNode) toNode);
		}
		if (CallOutNodeTypeConsts.CONDITION_NODE.equals(category)) {
			return ruleResponse + getConditionRuleResponse((ConditionNode) toNode);
		}
		if (CallOutNodeTypeConsts.TRANSFER_NODE.equals(category)) {
			return ruleResponse + getTransferResponse((TransferNode) toNode);
		}
		if (CallOutNodeTypeConsts.URL_ACTION_NODE.equals(category)) {
			return ruleResponse + getURLActionResponse((URLActionNode) toNode);
		}
		return null;
	}

	/**
	 * 获取动作组件回复内容
	 */
	private static String getURLActionResponse(URLActionNode toNode) {
		// 设置回复内容
		Map<String, String> setItems = new HashMap<String, String>();
		List<String> others = new ArrayList<String>();
		setItems.put(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, toNode.getKey());
		setItems.put("上文:节点名", toNode.getKey());
		setItems.put("节点名", toNode.getKey());
		String ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		ruleResponse += "业务信息获取(\"" + toNode.getInterfaceName() + "\")";
		return ruleResponse;
	}

	/**
	 * 获取转人工回复
	 */
	private static String getTransferResponse(TransferNode toNode) {
		String ruleResponse = "";
		String action = ActionTypeConsts.TRANSFER_ACTION;
		// 设置动作参数，转人工号码
		StringBuffer actionParams = new StringBuffer();
		actionParams.append(toNode.getTransferNumber());
		Map<String, String> setItems = new HashMap<String, String>();
		if (SceneTypeConsts.CALL_IN.equals(sceneType)) {
			setItems.put(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, toNode.getKey());
		}
		setItems.put("节点名", toNode.getKey());
		setItems.put("action", action);
		setItems.put("actionParams", actionParams.toString());
		ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
	}

	/**
	 * 获取TTS回复
	 * 
	 * @param ttsNode TTS节点
	 * @return 回复内容
	 */
	private static String getTTSRuleResponse(TTSNode ttsNode) {
		Map<String, String> setItems = new HashMap<String, String>();
		if (ttsNode.getOtherResponses() != null && !ttsNode.getOtherResponses().isEmpty()) {
			for (OtherResponse otherResponse : ttsNode.getOtherResponses()) {
				setItems.put(otherResponse.getOtherResponseName(), otherResponse.getOtherResponseValue());
			}
		}
		setItems.put("节点名", ttsNode.getKey());
		if (isJumpNode(ttsNode)) {
			// 跳转节点不走意图识别
			setItems.put("任意类型匹配", "跳转节点任意类型匹配");
		} else {
			// 非跳转节点走意图识别
			setItems.put("任意类型匹配", "");
			setItems.put("用户回答", "");
		}
		if (InteracviteTypeConsts.WORD_PATTERN.equals(ttsNode.getInteractiveType())) {
			setItems.put("TTS", StringUtils.isBlank(ttsNode.getWordsContent()) ? "" : ttsNode.getWordsContent());
		}
		setItems.put("code", StringUtils.isBlank(ttsNode.getCode()) ? "" : ttsNode.getCode());
		setItems.put("action", StringUtils.isBlank(ttsNode.getAction()) ? "" : ttsNode.getAction());
		setItems.put("actionParams", StringUtils.isBlank(ttsNode.getActionParams()) ? "" : ttsNode.getActionParams());
		String ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		if (InteracviteTypeConsts.MENU_OPTIONS.equals(ttsNode.getInteractiveType())) {
			String menuStartWords = ttsNode.getMenuStartWords();
			String menuOptions = ttsNode.getMenuOptions();
			String menuEndWords = ttsNode.getMenuEndWords();
			String menuItems = ScenariosDAO.getMenuRuleResponseTemplate(menuStartWords, menuOptions, menuEndWords);
			ruleResponse += menuItems;
		}
		return ruleResponse;
	}

	private static boolean isJumpNode(TTSNode ttsNode) {
		List<LinkData> toLinks = ttsNode.getToLinks();
		if (toLinks != null && !toLinks.isEmpty()) {
			for (LinkData linkData : toLinks) {
				if (linkData.getFromPort().getText().equals("跳转")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取DTMF回复
	 * 
	 * @param dtmfNode DTMF节点
	 * @return 回复内容
	 */
	private static String getDTMFRuleResponse(DTMFNode dtmfNode) {
		String ruleResponse = "";
		String action = ActionTypeConsts.DTMF_ACTION;
		// 设置动作参数，按键值|按键超时话术|按键最小个数|按键最大个数|按键超时时间|结束收号按键|采集失败重复次数|DTMF别名
		StringBuffer actionParams = new StringBuffer();
		actionParams.append(dtmfNode.getPressNumbers()).append("|");
		actionParams.append(dtmfNode.getPressTimeOutAnswer()).append("|");
		actionParams.append(dtmfNode.getMinLength()).append("|");
		actionParams.append(dtmfNode.getMaxLength()).append("|");
		actionParams.append(dtmfNode.getPressTimeOut()).append("|");
		actionParams.append(dtmfNode.getEndPressNumber()).append("|");
		actionParams.append(dtmfNode.getAttemptLimit()).append("|");
		actionParams.append(dtmfNode.getDtmfAlias());
		Map<String, String> setItems = new HashMap<String, String>();
		if (SceneTypeConsts.CALL_IN.equals(sceneType)) {
			setItems.put(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, dtmfNode.getKey());
		}
		setItems.put("节点名", dtmfNode.getKey());
		setItems.put("TTS", dtmfNode.getDtmfAnswer());
		setItems.put("code", "T" + dtmfNode.getDtmfAnswer()); // code以T开头
		setItems.put("action", action);
		setItems.put("actionParams", actionParams.toString());
		ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
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
		setItems.put(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, conditionNode.getKey());
		setItems.put("节点名", conditionNode.getKey());
		ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
	}

	/**
	 * 获取信息组件回复内容
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
		String collectionIntention = collectionNode.getCollectionIntention();
		if (StringUtils.isBlank(collectionStatus)) {
			// 跳转到信息收集节点
			setItems = new HashMap<String, String>();
			setItems.put(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, collectionNode.getKey());
			setItems.put("节点名", collectionNode.getKey());
			setItems.put(CallOutSceneElementConsts.COLLECTION_STATUS_ELEMENT_NAME, ""); // 信息收集状态置为空
			if (StringUtils.isNotBlank(collectionNode.getCollectionParam())) {
				setItems.put(collectionNode.getCollectionParam(), ""); // 关联要素值置为空
				setItems.put("任意类型匹配",
						CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention)
								? collectionNode.getCollectionParam()
								: ""); // 任意类型不匹配意图，直接把用户回答赋给信息收集的变量
			} else {
				setItems.put(collectionNode.getCollectionElement(), ""); // 关联要素值置为空
				setItems.put("任意类型匹配",
						CollectionIntentionConsts.SYSTEM_ANY.equals(collectionIntention)
								? collectionNode.getCollectionElement()
								: ""); // 任意类型不匹配意图，直接把用户回答赋给信息收集的变量
			}
			result = ScenariosDAO.getRuleResponse(setItems, others);
		}
		if (CollectionStatusConsts.COLLCETION_OUT.equals(collectionStatus)
				|| CollectionStatusConsts.COLLCETION_SUCCESS.equals(collectionStatus)) {
			// 信息收集成功或跳出，跳转
			result = getCallRuleResponse(nextNode);
		}
		if (CollectionStatusConsts.COLLCETION_FAIL.equals(collectionStatus)) {
			// 信息收集失败，重复话术
			String interactiveType = collectionNode.getInteractiveType();
			if (InteracviteTypeConsts.MENU_OPTIONS.equals(interactiveType)) {
				result = ScenariosDAO.getMenuRuleResponseTemplate(collectionNode.getMenuStartWords(),
						collectionNode.getMenuOptions(), collectionNode.getMenuEndWords());
			}
			if (InteracviteTypeConsts.WORD_PATTERN.equals(interactiveType)) {
				setItems = new HashMap<String, String>();
				setItems.put("TTS", collectionNode.getCollectionWords());
				result = ScenariosDAO.getRuleResponse(setItems, others);
			}
		}
		return result;
	}

	/**
	 * 场景交互规则
	 */
	public static SceneRule generateInteractiveRule(String scenariosid, String currentNodeName, String aboveNodeName,
			String customerAnswer, String recognitionStatus, String notRecognitionTimes, String differentiatedNode,
			String collectionTimes, String collectionStatus, String isGetPressNumber, String ruleResponse) {
		// 规则条件
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, aboveNodeName, customerAnswer,
				recognitionStatus, notRecognitionTimes, differentiatedNode, collectionTimes, collectionStatus,
				isGetPressNumber);
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		// 生成规则
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null, RuleTypeConsts.INTERACTIVE_RULE,
				conditions, null, null, ruleResponse, ResponseTypeConsts.WRITTEN_RESPONSE_RULE, (weight++) + "");
		return sceneRule;
	}

	/**
	 * 场景交互规则
	 * 
	 * @param scenariosid        场景ID
	 * @param ruleResponse       回复内容
	 * @param sceneElementValues 场景要素值
	 * @return
	 */
	public static SceneRule generateInteractiveRule(String scenariosid, String ruleResponse,
			List<SceneElement> sceneElementValues, String responseType) {
		// 规则条件
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		// 生成规则
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null, RuleTypeConsts.INTERACTIVE_RULE,
				conditions, null, null, ruleResponse, responseType, weight++ + "");
		return sceneRule;
	}

	/**
	 * 意图 语义理解规则
	 */
	public static SceneRule generateRegnitionRule(String scenariosid, String currentNodeName, String aboveNodeName,
			String customerAnswer, String recognitionStatus, String notRecognitionTimes, String differentiatedNode,
			String collectionIntention, String collectionVariable) {
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, aboveNodeName, customerAnswer,
				recognitionStatus, null, null, null, null, null); // 场景要素值
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues); // 规则条件
		String ruleResponse = ""; // 回复内容
		String questionObject = ""; // 问题对象
		String standardQuestion = ""; // 标准问题
		Map<String, String> setItems = new HashMap<String, String>();
		List<String> others = new ArrayList<String>();
		if (StringUtils.isNotBlank(differentiatedNode)) {
			setItems.put("区分节点", differentiatedNode);
			others = new ArrayList<String>();
			others.add("信息补全(\"用户回答|区分节点\",\"上文\")");
			ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		}
		if (StringUtils.isNotBlank(collectionIntention)) {
			others = new ArrayList<String>();
			if (SceneTypeConsts.CALL_OUT.equals(sceneType)) {
				others.add("信息补全(\"" + collectionIntention + "\",\"上文\")");
			}
			if (SceneTypeConsts.CALL_IN.equals(sceneType)) {
				others.add("信息补全(\"" + collectionIntention + "\",\"上文\",\"" + collectionIntention + "\")");
				others.add("信息补全(\"" + collectionIntention + "\",\"上文\",\"" + collectionVariable + "\")");
			}
			ruleResponse = ScenariosDAO.getRuleResponse(null, others);
			questionObject = "识别规则业务";
			HashMap<String, String> collectionQuestions = queryCollectionQuestions(scenariosid);
			standardQuestion = collectionQuestions.get(collectionIntention.trim());
		}
		if (StringUtils.isNotBlank(customerAnswer)) {
			questionObject = "识别规则业务";
			HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosid);
			standardQuestion = recognitionQuestions.get(customerAnswer.trim());
			others = new ArrayList<String>();
			setItems = new HashMap<String, String>();
			others.add("信息补全(\"用户回答\",\"上文\")");
			ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		}
		// 生成规则
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, currentNodeName,
				RuleTypeConsts.REGNITION_RULE, conditions, questionObject, standardQuestion, ruleResponse,
				ResponseTypeConsts.WRITTEN_RESPONSE_RULE, (weight++) + "");
		return sceneRule;
	}

	/**
	 * 跳出 语义理解规则
	 */
	private static SceneRule generateJumpOutRegnitionRule(String scenariosid, int weight) {
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, null, null, "跳出", null, null, null,
				null, null);
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		String ruleResponse = "";
		Map<String, String> setItems = new HashMap<String, String>();
		setItems.put("用户回答", "跳出");
		List<String> others = new ArrayList<String>();
		others.add("信息补全(\"用户回答\",\"上文\")");
		ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null, RuleTypeConsts.REGNITION_RULE,
				conditions, null, null, ruleResponse, ResponseTypeConsts.WRITTEN_RESPONSE_RULE, weight + "");
		return sceneRule;
	}

	/**
	 * 未理解 语义理解规则
	 */
	private static SceneRule generateNotRegnitionRule(String scenariosid, int weight) {
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, null, null, "未理解", null, null, null,
				null, null);
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		String ruleResponse = "";
		Map<String, String> setItems = new HashMap<String, String>();
		setItems.put("用户回答", "未理解");
		List<String> others = new ArrayList<String>();
		others.add("信息补全(\"用户回答\",\"上文\")");
		ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null, RuleTypeConsts.REGNITION_RULE,
				conditions, null, null, ruleResponse, ResponseTypeConsts.WRITTEN_RESPONSE_RULE, weight + "");
		return sceneRule;
	}

	/**
	 * 未获取到按键值 语义理解规则
	 */
	private static SceneRule generateEmptyPressNumberRegnitionRule(String scenariosid, String aboveNodeName) {
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, aboveNodeName, null, null, null,
				null, null, null, null);
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		Map<String, String> setItems = new HashMap<String, String>();
		setItems.put("query", "未获取到按键值");
		List<String> others = new ArrayList<String>();
		others.add("信息补全(\"query\",\"上文\")");
		String ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		String questionObject = "识别规则业务";
		HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosid);
		String standardQuestion = recognitionQuestions.get("未获取到按键值");
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null, RuleTypeConsts.REGNITION_RULE,
				conditions, questionObject, standardQuestion, ruleResponse, ResponseTypeConsts.WRITTEN_RESPONSE_RULE,
				(weight++) + "");
		return sceneRule;
	}

	/**
	 * 生成其他规则
	 */
	public static SceneRule generateOtherRule(String scenariosid, String condition, String result) {
		String ruleResponse = condition + "==>" + result;
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, "AUTO_ADD", RuleTypeConsts.OTHER_RULE,
				new String[101], null, null, ruleResponse, null, (weight++) + "");
		return sceneRule;
	}

	/**
	 * 是否设置区分节点
	 */
	private static boolean isSetDifferentiated(NodeData toNode) {
		List<LinkData> toLinks = toNode.getToLinks();
		if (toLinks != null) {
			for (LinkData link : toLinks) {
				if ("跳出".equals(link.getFromPort().getText())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取场景要素
	 */
	private static List<SceneElement> getSceneElementValues(String scenariosid, String aboveNodeName,
			String customerAnswer, String recognationStatus, String notRecognitionTimes, String differentiatedNode,
			String collectionTimes, String collectionStatus, String isGetPressNumber) {
		List<SceneElement> sceneElementValues = new ArrayList<SceneElement>();
		// 上文节点
		SceneElement sceneElementValue = ScenariosDAO
				.getSceneElementValue(CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME, aboveNodeName);
		sceneElementValues.add(sceneElementValue);

		// 机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.ROBOT_ID_ELEMENT_NAME, robotId);
		sceneElementValues.add(sceneElementValue);

		// 用户回答
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.CUSTOMER_ANSWER_ELEMENT_NAME,
				customerAnswer);
		sceneElementValues.add(sceneElementValue);

		// 理解状态
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.RECOGNITION_STATUS_ELEMENT_NAME,
				recognationStatus);
		sceneElementValues.add(sceneElementValue);

		// 连续未理解次数
		sceneElementValue = ScenariosDAO.getSceneElementValue(
				CallOutSceneElementConsts.NOT_RECOGNITION_TIMES_ELEMENT_NAME, notRecognitionTimes);
		sceneElementValues.add(sceneElementValue);

		// 区分节点
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.DIFFERENTIATED_ELEMENT_NAME,
				differentiatedNode);
		sceneElementValues.add(sceneElementValue);

		// 信息收集重复次数
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.COLLECTION_TIMES_ELEMENT_NAME,
				collectionTimes);
		sceneElementValues.add(sceneElementValue);

		// 信息收集状态
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.COLLECTION_STATUS_ELEMENT_NAME,
				collectionStatus);
		sceneElementValues.add(sceneElementValue);

		// 是否获取到按键值
		sceneElementValue = ScenariosDAO.getSceneElementValue(
				CallOutSceneElementConsts.DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME, isGetPressNumber);
		sceneElementValues.add(sceneElementValue);

		return sceneElementValues;
	}

	/**
	 * 解析数据
	 * 
	 * @param sceneJson 流程图JSON数据
	 * @return 连线集合
	 */
	private static List<NodeData> parseSceneJson(String sceneJson) {
		JSONObject json = JSONObject.parseObject(sceneJson);
		JSONArray nodeDataJsonArray = json.getJSONArray("nodeDataArray");
		JSONArray linkDataJsonArray = json.getJSONArray("linkDataArray");

		List<NodeData> nodeDataList = new ArrayList<NodeData>();
		List<LinkData> linkDataList = new ArrayList<LinkData>();
		Map<String, List<LinkData>> toNodeLinkMap = new HashMap<String, List<LinkData>>();
		Map<String, List<LinkData>> fromNodeLinkMap = new HashMap<String, List<LinkData>>();
		List<LinkData> toLinks = null;
		List<LinkData> fromLinks = null;
		NodeData nodeData = null;
		// 解析节点
		for (int i = 0; i < nodeDataJsonArray.size(); i++) {
			JSONObject nodeJsonObj = nodeDataJsonArray.getJSONObject(i);
			String category = nodeJsonObj.getString("category");
			toLinks = new ArrayList<LinkData>();
			fromLinks = new ArrayList<LinkData>();
			toNodeLinkMap.put(nodeJsonObj.getString("key"), toLinks);
			fromNodeLinkMap.put(nodeJsonObj.getString("key"), fromLinks);
			if (CallOutNodeTypeConsts.START_NODE.equals(category)) {
				// 开始节点
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), NodeData.class);
			}
			if (CallOutNodeTypeConsts.TTS_NODE.equals(category) || CallOutNodeTypeConsts.END_NODE.equals(category)) {
				// 放音组件|结束语
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), TTSNode.class);
			}
			if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(category)) {
				// 信息收集
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), CollectionNode.class);
			}
			if (CallOutNodeTypeConsts.DTMF_NODE.equals(category)) {
				// DTMF按键
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), DTMFNode.class);
			}
			if (CallOutNodeTypeConsts.CONDITION_NODE.equals(category)) {
				// 条件组件
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), ConditionNode.class);
			}
			if (CallOutNodeTypeConsts.TRANSFER_NODE.equals(category)) {
				// 转人工
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), TransferNode.class);
			}
			if (CallOutNodeTypeConsts.URL_ACTION_NODE.equals(category)) {
				// 动作组件
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), URLActionNode.class);
			}
			nodeDataList.add(nodeData);
		}
		// 解析连线
		for (int i = 0; i < linkDataJsonArray.size(); i++) {

			LinkData link = new LinkData();
			NodePort fromPort = new NodePort();
			NodePort toPort = new NodePort();

			String fromNodeKey = linkDataJsonArray.getJSONObject(i).getString("from");
			String toNodeKey = linkDataJsonArray.getJSONObject(i).getString("to");
			NodeData fromNode = getCallOutNodeDataByKey(fromNodeKey, nodeDataList);
			NodeData toNode = getCallOutNodeDataByKey(toNodeKey, nodeDataList);
			fromPort.setText(linkDataJsonArray.getJSONObject(i).getString("fromPort"));
			toPort.setText(linkDataJsonArray.getJSONObject(i).getString("toPort"));

			link.setFromNode(fromNode);
			link.setToNode(toNode);
			link.setFromPort(fromPort);
			link.setToPort(toPort);
			linkDataList.add(link);

			fromLinks = fromNodeLinkMap.get(toNodeKey);
			fromLinks.add(link);
			fromNodeLinkMap.put(toNodeKey, fromLinks);

			toLinks = toNodeLinkMap.get(fromNodeKey);
			toLinks.add(link);
			toNodeLinkMap.put(fromNodeKey, toLinks);
		}
		// 设置节点连线
		for (NodeData node : nodeDataList) {
			node.setToLinks(toNodeLinkMap.get(node.getKey()));
			node.setFromLinks(fromNodeLinkMap.get(node.getKey()));
		}
		return nodeDataList;
	}

	/**
	 * 查询用户意图
	 */
	public static HashMap<String, String> queryRecognitionQuestions(String scenariosId) {
		HashMap<String, String> recognitionQuestions = new HashMap<String, String>();
		/**
		 * 查询系统用户意图
		 */
		Result result = ScenariosDAO.queryPublicRegnitionRule();
		if (result != null && result.getRowCount() > 0) {
			if (result != null && result.getRowCount() > 0) {
				for (int i = 0; i < result.getRowCount(); i++) {
					String abstractStr = (String) result.getRows()[i].get("abs");
					String systemAnswer = abstractStr.substring(abstractStr.indexOf(">") + 1);
					recognitionQuestions.put(systemAnswer, abstractStr);
				}
			}
		}
		/**
		 * 查询自定义用户意图
		 */
		result = ScenariosDAO.queryBusinessRegnitionRule();
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				String abstractStr = (String) result.getRows()[i].get("abs");
				String customerAnswer = abstractStr.substring(abstractStr.indexOf(">") + 1);
				recognitionQuestions.put(customerAnswer, abstractStr);
			}
		}
		return recognitionQuestions;
	}

	/**
	 * 查询关联意图
	 */
	public static HashMap<String, String> queryCollectionQuestions(String scenariosId) {
		HashMap<String, String> collectionQuestions = new HashMap<String, String>();
		/**
		 * 查询公共关联意图
		 */
		Result result = ScenariosDAO.queryPublicCollectionIntention();
		if (result != null && result.getRowCount() > 0) {
			if (result != null && result.getRowCount() > 0) {
				for (int i = 0; i < result.getRowCount(); i++) {
					String abstractStr = (String) result.getRows()[i].get("abs");
					String collectionIntention = abstractStr.substring(abstractStr.indexOf(">") + 1);
					collectionQuestions.put(collectionIntention, abstractStr);
				}
			}
		}
		/**
		 * 查询自定义关联意图
		 */
		result = ScenariosDAO.queryBusinessRegnitionRule();
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				String abstractStr = (String) result.getRows()[i].get("abs");
				String collectionIntention = abstractStr.substring(abstractStr.indexOf(">") + 1);
				collectionQuestions.put(collectionIntention, abstractStr);
			}
		}
		return collectionQuestions;
	}

	/**
	 * 根据key获取节点
	 */
	private static NodeData getCallOutNodeDataByKey(String key, List<NodeData> nodeDataArray) {
		for (NodeData nodeData : nodeDataArray) {
			if (key.equals(nodeData.getKey())) {
				return nodeData;
			}
		}
		return null;
	}

	/**
	 * 自动生成词模
	 * 
	 * @param customeranswer
	 * @param scenariosid
	 * @param autowordpat
	 * @return
	 */
	public static Object autoGenerateWordpat(String scenariosid, String autowordpat) {
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype) || servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		// 根据场景ID获取摘要ID
		String kbdataid = "";
		Result absRs = CommonLibInteractiveSceneDAO.queryElementServiceAndAbstract(scenariosid);
		if (absRs != null && absRs.getRowCount() > 0) {
			kbdataid = absRs.getRows()[0].get("abstractid").toString();
		}
		String url = "";
		String queryCityCode = CommonLibKbDataDAO.getCityByAbstractid(kbdataid);
		String provinceCode = "全国";
		Map<String, Map<String, String>> provinceToUrl = GetLoadbalancingConfig.provinceToUrl;
		if ("全国".equals(queryCityCode) || "电渠".equals(queryCityCode) || "集团".equals(queryCityCode)
				|| "".equals(queryCityCode) || queryCityCode == null) {
			queryCityCode = "全国";
			url = provinceToUrl.get("默认").get("高级分析");
		} else {
			queryCityCode = queryCityCode.replace(",", "|");
			provinceCode = queryCityCode.split("\\|")[0];
			provinceCode = provinceCode.substring(0, 2) + "0000";
			if ("010000".equals(provinceCode) || "000000".equals(provinceCode)) {// 如何为集团、电渠编码去默认url
				url = provinceToUrl.get("默认").get("高级分析");
			} else {
				String province = GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode);
				if (provinceToUrl.containsKey(province)) {
					url = provinceToUrl.get(province).get("高级分析");
				} else {
					jsonObj.put("result", "ERROR:未找到【" + province + "】高级分析负载均衡服务器!");
					return jsonObj;
				}
			}
		}

		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "高级分析", "", false, provinceCode);
		// 获取高级分析接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(user.getUserID(), autowordpat, servicetype, serviceInfo);
		logger.info("生成词模高级分析【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口地址：" + url);
		logger.info("生成词模高级分析接口的输入串：" + queryObject);
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient(url);
		// 判断客户端是否为null
		if (NLPCaller4WSClient == null) {
			// 将错误信息放入jsonObj的result对象中
			jsonObj.put("result",
					"ERROR:生成词模高级分析【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口异常。");
			return jsonObj;
		}

		String result = "";
		try {
			// 调用接口的方法获取词模
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			logger.info("生成词模高级分析接口的输出串：" + result);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 将无放入jsonObj的result对象中
			jsonObj.put("result",
					"ERROR:生成词模高级分析【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口调用失败。");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}

		// 将结果转化为json对象
		// 定义返回值后面的编者
		String autor = "编者=\"" + user.getUserName() + "\"";
		jsonObj = (JSONObject) getResult(result, autor);
		result = jsonObj.getString("result").replaceAll("\\s*", "");
		jsonObj.put("result", result);
		return jsonObj;
	}

	/**
	 * 获得高级分析接口分词结果
	 * 
	 * @param result
	 * @param autor
	 * @return
	 */
	public static Object getResult(String result, String autor) {
		String rs = "";
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Map<String, String> map = new HashMap<String, String>();
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i).toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..( 和 nlp版本信息
					if (!segments.contains("...(") && !segments.startsWith("NLU-Version")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
					}
				}

				List<String> list = getAutoWordpat(map, autor);
				List<String> rsList = new ArrayList<String>(); // 正常的词模
				List<String> erList = new ArrayList<String>(); // 分词出的词模
				Set<String> oovList = new HashSet<String>(); // 出错的分词
				if (list.size() > 0) {
					for (String str : list) {// 检查词模处理信息
						if (str != null && str.startsWith("ERROR")) {// 分词出的词模
							erList.add(str.replaceFirst("ERROR##", ""));
						} else if (str != null && str.startsWith("OOV")) {// 出错的分词
							oovList.add(str.replaceFirst("OOV##", ""));
						} else {// 正常的词模
							rsList.add(str);
						}
					}
					if (!oovList.isEmpty()) { // 如果词模有OOV问题，则提示
						jsonObj.put("oovWord", StringUtils.join(oovList.toArray(), "$_$"));
					}
					rsList.addAll(erList);
					rs = StringUtils.join(rsList.toArray(), "$_$");
				} else {
					rs = "无";
				}
			}
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", rs);
		} catch (Exception e) {
			e.printStackTrace();
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "无");
		}
		return jsonObj;
	}

	/**
	 * 获取生成词模
	 * 
	 * @param map
	 * @param autr
	 * @return
	 */
	public static List<String> getAutoWordpat(Map<String, String> map, String autor) {
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String word = entry.getKey().replace(" ", "##");
			String wordArray[] = word.split("##");
			String wordpat = "";
			int flag = 0;// 词模处理结果 0 可用 1 分词中没有近类和父类 2 分词中包含OOV
			String _word = "";// 具体分词
			for (int i = 0; i < wordArray.length; i++) {
				String tempWord = wordArray[i];
				_word = tempWord.split("\\(")[0];
				if (!"".equals(tempWord) && !"".equals(_word.trim())) {// 分词本身不能为空
					String dealWrod = dealWrod(tempWord);
					if (dealWrod == null) {
						flag = 1;
						// 页面展示： word(OOV)
						dealWrod = _word + "(OOV)";
						// 记录当前词模中的OOV分词
						list.add("OOV##" + dealWrod);
					}
					String _tempWord = "<" + dealWrod + ">";
					wordpat = wordpat + _tempWord + "*";
				}
			}
			wordpat = wordpat.substring(0, wordpat.lastIndexOf("*")) + "@2#" + autor;
			wordpat = SimpleString.worpattosimworpat(wordpat);
			String newWordpat = wordpat.replace("近类", "");
			if (flag == 0) {
				list.add(newWordpat + "@_@" + wordpat);
			} else {
				list.add("ERROR##" + newWordpat + "@_@" + wordpat);
			}
		}
		return list;
	}

	/**
	 * 处理词类
	 * 
	 * @param word
	 * @return
	 */
	public static String dealWrod(String word) {
		String tempWord = word.split("\\(")[1].split("\\)")[0];
		String wordArray[] = tempWord.split("\\|");
		String newWord = "";
		if (tempWord.contains("近类") && tempWord.contains("父类")) {
			for (int i = 0; i < wordArray.length; i++) {
				String w = wordArray[i];
				if (!w.endsWith("父类") && !w.equals("模板词") && !w.endsWith("词类")) {
					newWord = newWord + w + "|";
				}
			}
		} else {
			for (int i = 0; i < wordArray.length; i++) {
				String w = wordArray[i];
				if (!w.equals("模板词") && !w.endsWith("词类")) {
					newWord = newWord + w + "|";
				}
			}
		}
		if (tempWord.equals("OOV")) {// 如果分词中存在OOV直接过滤
			return null;
		}
		if (newWord.contains("|")) {
			newWord = newWord.substring(0, newWord.lastIndexOf("|"));
			return newWord;
		}
		return null;
	}

	/**
	 * 保存词模
	 * 
	 * @param kbdataid
	 * @param customeranswer
	 * @param simplewordpat
	 * @param wordpattype
	 * @param cityId
	 * @param request
	 * @return
	 */
	public static Object saveWordpat(String kbdataid, String customeranswer, String simplewordpat, String wordpattype,
			String city, HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if (sre == null || "".equals(sre)) {
			// 将0放入jsonObj的total对象中
			jsonObj.put("success", false);
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User) sre;

		// 构造新增词模参数对象
		InsertOrUpdateParam param = new InsertOrUpdateParam();
		param.setSimplewordpat(simplewordpat);
		param.setWordpattype(wordpattype);
		param.setKbdataids(kbdataid);
		param.setCity(city);

		// 判断进度词模是否为空，null
		if (!"".equals(param.simplewordpat) && param.simplewordpat != null) {
			// 将简单词模按照回车符拆分
			String simplewordpatArry[] = param.simplewordpat.split("\n");
			String s_wordpat = "";
			String checkInfo = "";
			List<String> list = new ArrayList<String>();
			// 循环遍历简单词模数组
			for (int i = 0; i < simplewordpatArry.length; i++) {
				// 判断是否为空
				if ("".equals(simplewordpatArry[i])) {
					continue;
				}
				// 将第i个词模赋值给对象的属性
				param.simplewordpat = simplewordpatArry[i];
				// 将简单词模转化为普通词模，并返回转换结果
				s_wordpat = SimpleString.SimpleWordPatToWordPat(param.simplewordpat);
				// 判断转换结果是否含有checkInfo
				if (s_wordpat.indexOf("checkInfo") != -1) {
					// 将信息赋值给checkInfo变量
					checkInfo += "第" + (i + 1) + "条：" + s_wordpat.split("=>")[1] + "<br/>";
				} else {
					// 将简单词模和信息放入集合中
					list.add(simplewordpatArry[i] + "<=>" + s_wordpat);
				}
			}
			// 判断checkInfo是否为空
			if ("".equals(checkInfo)) {
				// 将集合赋值给对象的属性
				param.simplewordpatandwordpat = list;
				// 新增返回值的key=用户回复
				Result patternkeyRs = CommonLibPatternkeyDAO.InsertSelect("用户回答");
				if (!(patternkeyRs != null && patternkeyRs.getRowCount() > 0)) {
					CommonLibPatternkeyDAO.Insert("用户回答", user);
				}
				// 获取补充词模并赋值给对象的属性
				param.simplewordpatandwordpat = AddReturnValues(param, customeranswer);

				// 获取摘要相关信息
				String serviceid = "";
				Result serviceRs = CommonLibKbDataDAO.getServiceKbdataInfoByAbstractID(kbdataid);
				if (serviceRs != null && serviceRs.getRowCount() > 0) {
					serviceid = serviceRs.getRows()[0].get("serviceid").toString();
					String topic = serviceRs.getRows()[0].get("topic").toString();
					String brand = serviceRs.getRows()[0].get("brand").toString();
					param.setTopic(topic);
					param.setBrand(brand);
				}
				// 获取业务相关信息
				String service = CommonLibServiceDAO.getNameByserviceid(serviceid);
				param.setService(service);

				// 新增词模并返回json串
				return insertWordpat(param, request, user);
			} else {
				// 将信息放入jsonObj的checkInfo对象中
				jsonObj.put("success", false);
				jsonObj.put("checkInfo", checkInfo);
			}
		}
		return jsonObj;
	}

	/**
	 * 补充交互词模
	 * 
	 * @param param参数对象
	 * @return 集合
	 */
	public static List<String> AddReturnValues(InsertOrUpdateParam param, String customeranswer) {
		List<String> list = new ArrayList<String>();
		String simplewordpat = "";
		String wordpat = "";
		List<String> simplewordpatandwordpat = param.simplewordpatandwordpat;
		for (int i = 0; i < simplewordpatandwordpat.size(); i++) {
			simplewordpat = simplewordpatandwordpat.get(i).split("<=>")[0];
			wordpat = simplewordpatandwordpat.get(i).split("<=>")[1];
			if (param.container == null || "".equals(param.container)) {
				if ("flowchart".equals(param.chartaction) && !"".equals(param.queryorresponse)) {
					// &匹配要求=要求相等($摘要$,"《健康顾问场景》健康顾问业务.1_咨询健康顾问（上文摘要）")
					// &补全后咨询="健康顾问介绍（用户选择的摘要名称）"
					if ("应答".equals(param.queryorresponse)) {
						if (!"".equals(param.abs_name)) {
							String pre_abs = "《" + param.service + "》" + "咨询" + param.abs_name;// 上文摘要
							String returnvalues_1 = "&匹配要求=要求相等($摘要$,\"" + pre_abs + "\")";
							simplewordpat = simplewordpat + returnvalues_1;
							wordpat = wordpat + returnvalues_1;
						}
						if (!"".equals(param.next_abs_name)) {
							String abs = "咨询" + param.next_abs_name;
							String returnvalues_2 = "&补全后咨询=\"" + "《" + param.service + "》" + abs + "\"";
							simplewordpat = simplewordpat + returnvalues_2;
							wordpat = wordpat + returnvalues_2;
						}
					}
				}
			}
			list.add(simplewordpat + "<=>" + wordpat);
		}
		return list;
	}

	/**
	 * 新增词模的具体方法
	 * 
	 * @param param参数对象
	 * @param request参数request请求
	 * @return json串
	 */
	public static Object insertWordpat(InsertOrUpdateParam param, HttpServletRequest request, User user) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		// 词模检查结果字符串
		String checkInfo = "";
		List<String> patternList = param.simplewordpatandwordpat;
		// 词模检查结果
		Boolean checkflag = true;// 语法检查过程出现异常！
		String pattern = "";
		CheckInforef curcheckInfo = new CheckInforef();
		// 循环添加每条词模
		for (int i = 0; i < patternList.size(); i++) {
			// 递增词模索引
			pattern = patternList.get(i).split("<=>")[1];
			try {
				// 调用词模检查函数
				if (!CheckInput.CheckGrammer(path, pattern, 0, curcheckInfo))
					// 词模有误
					checkflag = false;
			} catch (Exception ex) {
				// 检查过程中出现异常，则报错
				checkflag = false;
				curcheckInfo.curcheckInfo = "模板语法有误！";
			}
			// 判断curcheckInfo
			if (!"".equals(curcheckInfo.curcheckInfo) && (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
				checkInfo += "第" + (i + 1) + "条：" + curcheckInfo.curcheckInfo + "<br>";
			}
		}

		// 词模检查失败，则报错
		if (!checkflag) {
			// 将信息放入jsonObj的checkInfo对象中
			jsonObj.put("success", false);
			jsonObj.put("checkInfo", checkInfo);
			return jsonObj;
		}

		// 循环添加每条词模
		for (int i = 0; i < patternList.size(); i++) {
			// 递增词模索引
			pattern = patternList.get(i);
			param._wordpat = pattern.split("<=>")[1];
			param.simplewordpat = pattern.split("<=>")[0];
			// 判断是否是当前行业主题树
			if (param.container == null || "".equals(param.container)) {
				// 判断是否已存在相同模板
				String returninfo = QuestionManageDAO.isExistsWordpat(user, param.service, param.brand, param.kbdataids,
						param._wordpat, param.simplewordpat);
				if (!"".equals(returninfo)) {
					checkInfo += "第" + (i + 1) + "条模板" + returninfo + "<br>";
					// 存在，则不更新
					continue;
				}
			}
			int c = CommonLibWordpatDAO.insert(user, param.service, param.brand, param.kbdataids, param._wordpat,
					param.simplewordpat, param.wordpattype, param.city);
			if (c <= 0) {
				checkInfo += "第" + (i + 1) + "条模板插入失败！" + "<br>";
			}
		}
		if ("".equals(checkInfo)) {
			jsonObj.put("success", true);
			jsonObj.put("checkInfo", "插入成功!");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("checkInfo", checkInfo);
		return jsonObj;
	}

	/**
	 * 新增用户回答
	 * 
	 * @param scenariosid       场景ID
	 * @param customerAnswer    用户回答
	 * @param simpleWordPattern 简单词模
	 * @param wordPatternType   词模类型
	 * @param request
	 * @return
	 */
	public static Object saveCustomerAnswer(String scenariosid, String customerAnswer, String simpleWordPattern,
			String wordPatternType, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		// 用户回答是否已存在
		HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosid);
		if (!recognitionQuestions.isEmpty()) {
			if (recognitionQuestions.containsKey(customerAnswer)) {
				jsonObj.put("success", false);
				jsonObj.put("checkInfo", "用户回答已存在");
				return jsonObj;
			}
		}
		// 获取场景名称
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosid);
		if (result == null || result.getRowCount() == 0) {
			jsonObj.put("success", false);
			jsonObj.put("checkInfo", "场景信息未查询到");
			return jsonObj;
		}
		String scenariosName = result.getRows()[0].get("service") + "";
		// 用户回答父类新增词条
		JSONObject res = (JSONObject) WordClassDAO.select("sys" + scenariosName + "用户回答父类", true, "全部", 0, 10);
		if (res.getInteger("total") == 0) {
			jsonObj.put("success", false);
			jsonObj.put("checkInfo", "sys" + scenariosName + "用户回答父类未查询到");
			return jsonObj;
		}
		String wordClassId = res.getJSONArray("root").getJSONObject(0).getString("wordclassid");
		res = (JSONObject) WorditemDAO.insert(customerAnswer, (String) res.get("wordclass"), wordClassId, "", true);
		if (!res.getBooleanValue("success")) {
			jsonObj.put("success", false);
			jsonObj.put("checkInfo", "sys" + scenariosName + "用户回答父类插入词条失败");
			return jsonObj;
		}
		// 新增用户回答
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid); // 机器人ID
		String cityCode = StringUtils.isNotBlank(robotId) ? ScenariosDAO.getRobotCityCode(robotId) : "全国"; // 地市ID
		jsonObj = (JSONObject) addRecognitionRule(customerAnswer, simpleWordPattern, wordPatternType, cityCode,
				request);
		if (!jsonObj.getBooleanValue("success")) {
			return jsonObj;
		}
		// 刷新用户回答
		initCustomerAnswer(scenariosid);
		jsonObj.put("success", true);
		jsonObj.put("checkInfo", "保存成功");
		return jsonObj;
	}

	/**
	 * 新增关联意图
	 * 
	 * @param scenariosid         场景ID
	 * @param collectionIntention 关联意图
	 * @param simpleWordPattern   简单词模
	 * @param wordPatternType     词模类型
	 * @param request
	 * @return
	 */
	public static Object saveCollectionIntention(String scenariosid, String collectionIntention,
			String simpleWordPattern, String wordPatternType, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		// 关联意图是否已存在
		HashMap<String, String> collectionQuestions = queryCollectionQuestions(scenariosid);
		if (!collectionQuestions.isEmpty()) {
			if (collectionQuestions.containsKey(collectionIntention)) {
				jsonObj.put("checkInfo", "关联意图已存在");
				return jsonObj;
			}
		}
		// 添加词模返回值
		if (simpleWordPattern.indexOf("&" + collectionIntention + "=") > -1) {
			Result patternkeyRs = CommonLibPatternkeyDAO.InsertSelect(collectionIntention);
			if (!(patternkeyRs != null && patternkeyRs.getRowCount() > 0)) {
				User user = (User) GetSession.getSessionByKey("accessUser");
				CommonLibPatternkeyDAO.Insert(collectionIntention, user);
			}
		}
		// 添加关联意图
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid); // 机器人ID
		String cityCode = StringUtils.isNotBlank(robotId) ? ScenariosDAO.getRobotCityCode(robotId) : "全国"; // 地市ID
		jsonObj = (JSONObject) addRecognitionRule(collectionIntention, simpleWordPattern, wordPatternType, cityCode,
				request);
		if (!jsonObj.getBooleanValue("success")) {
			return jsonObj;
		}
		// 刷新关联意图
		initCollectionIntention(scenariosid);
		jsonObj.put("success", true);
		jsonObj.put("checkInfo", "保存成功");
		return jsonObj;
	}

	/**
	 * 添加识别规则业务
	 * 
	 * @param standardQuestion  标准问
	 * @param simpleWordPattern 简单词模
	 * @param wordPatternType   词模类型
	 * @param cityCode          归属地市
	 * @param request
	 * @return
	 */
	public static Object addRecognitionRule(String standardQuestion, String simpleWordPattern, String wordPatternType,
			String cityCode, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
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
		// 新增标准问
		QuestionManageDAO.createNormalQuery(user, serviceId, standardQuestion);
		List<String> serviceIds = new ArrayList<String>();
		serviceIds.add(serviceId);
		String kbdataid = getCustomerAnswerKbdataId(serviceId, standardQuestion);
		// 新增词模
		return saveWordpat(kbdataid, standardQuestion, simpleWordPattern, wordPatternType, cityCode, request);
	}

	/**
	 * 初始化用户答案
	 */
	public static Object initCustomerAnswer(String scenariosId) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosId);
		if (recognitionQuestions.isEmpty()) {
			jsonObj.put("success", false);
		}
		for (Entry<String, String> recognitionQuestion : recognitionQuestions.entrySet()) {
			JSONObject temObj = new JSONObject();
			temObj.put("id", recognitionQuestion.getKey());
			temObj.put("text", recognitionQuestion.getKey());
			jsonArray.add(temObj);
		}
		jsonObj.put("success", true);
		jsonObj.put("rows", jsonArray);
		return jsonObj;
	}

	/**
	 * 初始化关联意图
	 */
	public static Object initCollectionIntention(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		HashMap<String, String> collectionQuestions = queryCollectionQuestions(scenariosid);
		if (collectionQuestions.isEmpty()) {
			jsonObj.put("success", false);
		}
		// 【收集任意类型】选项
		JSONObject temObj = new JSONObject();
		temObj.put("id", CollectionIntentionConsts.SYSTEM_ANY);
		temObj.put("text", CollectionIntentionConsts.SYSTEM_ANY);
		jsonArray.add(temObj);
		for (Entry<String, String> collectionQuestion : collectionQuestions.entrySet()) {
			temObj = new JSONObject();
			temObj.put("id", collectionQuestion.getKey());
			temObj.put("text", collectionQuestion.getKey());
			jsonArray.add(temObj);
		}
		jsonObj.put("success", true);
		jsonObj.put("rows", jsonArray);
		return jsonObj;
	}

	/**
	 * 获取用户回答摘要ID
	 * 
	 * @param serviceId      识别规则业务ID
	 * @param customerAnswer 用户回答
	 * @return 用户回答摘要ID
	 */
	public static String getCustomerAnswerKbdataId(String serviceId, String customerAnswer) {
		String sql = "select KBDATAID from kbdata k where k.SERVICEID=? and ABSTRACT like ?";
		Result rs = Database.executeQuery(sql, serviceId, "%<识别规则业务>" + customerAnswer + "%");
		if (rs != null && rs.getRowCount() > 0) {
			String kbdataid = rs.getRows()[0].get("KBDATAID") + "";
			return kbdataid;
		}
		return "";
	}

	/**
	 * 查询短信模板
	 */
	public static Object querySmsTemplate(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		try {
			String querySmsTemplateUrl = getQuerySmsTemplateUrl() + "?robotId=" + robotId;
			// 创建GET请求
			logger.info("查询短信模板接口，请求参数：" + robotId);
			String response = HttpclientUtil.get(querySmsTemplateUrl);
			@SuppressWarnings("rawtypes")
			ResponseData responseData = JSONObject.parseObject(response, ResponseData.class);
			logger.info("查询短信模板接口，响应参数：" + responseData.toString());
			if (responseData.getCode() == 0) {
				jsonObj.put("success", true);
				jsonObj.put("rows", responseData.getData());
			} else {
				jsonObj.put("success", false);
			}
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			jsonObj.put("success", false);
			return jsonObj;
		}
	}

	/**
	 * 查询号码属性
	 */
	public static Object queryPhoneAttributeNames() {
		JSONObject jsonObj = new JSONObject();
		try {
			String queryPhoneAttributeNamesUrl = getQueryAttributeNamesUrl();
			// 创建GET请求
			logger.info("查询号码属性接口，请求地址：" + queryPhoneAttributeNamesUrl);
			String response = HttpclientUtil.get(queryPhoneAttributeNamesUrl);
			@SuppressWarnings("rawtypes")
			ResponseData responseData = JSONObject.parseObject(response, ResponseData.class);
			logger.info("查询号码属性接口，响应参数：" + responseData.toString());
			if (responseData.getCode() == 0) {
				jsonObj.put("success", true);
				jsonObj.put("rows", responseData.getData());
			} else {
				jsonObj.put("success", false);
			}
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			jsonObj.put("success", false);
			return jsonObj;
		}
	}

	/**
	 * 获取查询短信模板URL
	 */
	private static String getQuerySmsTemplateUrl() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("第三方接口信息配制", serviceType + "::短信模板查询");
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			for (int i = 0; i < rsConfig.getRowCount(); i++) {
				String querySmsTemplateInfconfig = (String) rsConfig.getRows()[i].get("name").toString();
				if ("URL".equals(querySmsTemplateInfconfig.split(":=")[0])) {
					return querySmsTemplateInfconfig.split(":=")[1];
				}
			}
		}
		return null;
	}

	/**
	 * 获取查询全部号码属性URL
	 */
	private static String getQueryAttributeNamesUrl() {
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("第三方接口信息配制", serviceType + "::号码属性查询");
		if (rsConfig != null && rsConfig.getRowCount() > 0) {
			for (int i = 0; i < rsConfig.getRowCount(); i++) {
				String querySmsTemplateInfconfig = (String) rsConfig.getRows()[i].get("name").toString();
				if ("URL".equals(querySmsTemplateInfconfig.split(":=")[0])) {
					return querySmsTemplateInfconfig.split(":=")[1];
				}
			}
		}
		return null;
	}

	/**
	 * 测试URL
	 */
	public static Object testURLAction(String url) {
		JSONObject jsonObj = new JSONObject();
		try {
			boolean testResult = HttpclientUtil.testGet(url);
			jsonObj.put("success", testResult);
		} catch (Exception e) {
			jsonObj.put("success", false);
			e.printStackTrace();
		}
		return jsonObj;
	}

	/**
	 * 校验动作组件名称
	 */
	public static Object checkUrlActionName(String actionName) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("第三方接口信息配制", serviceType + "::" + actionName.trim());
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("success", true);
		} else {
			jsonObj.put("success", false);
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
	
	/**
	 * 分页查询场景要素值
	 * 
	 * @param sceneElementName 场景要素名称
	 * @param scenariosid      场景ID
	 * @return 场景要素值集合
	 */
	public static Object listPagingElementValue(String scenariosid, String wordclassid, int currentPage,
			int pageSize) {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		int totalCount = CommonLibWordDAO.getWordCount(wordclassid, "");
		Result rs = CommonLibWordDAO.select(wordclassid, "", currentPage, pageSize);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject row = new JSONObject();
				row.put("wordclassid", rs.getRows()[i].get("wordclassid").toString());
				row.put("worditem", rs.getRows()[i].get("word").toString());
				row.put("wordid", rs.getRows()[i].get("wordid").toString());
				row.put("type", rs.getRows()[i].get("type").toString());
				rows.add(row);
			}
		}
		jsonObj.put("rows", rows);
		jsonObj.put("total", totalCount);
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
	 * 返回失败
	 */
	private static Object fail() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		jsonObj.put("msg", "提交失败");
		return jsonObj;
	}

	/**
	 * 返回成功
	 */
	private static Object success() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", true);
		jsonObj.put("msg", "提交成功");
		return jsonObj;
	}

	/**
	 * 添加新接口
	 * 
	 * @param scenariosid
	 * @param interfaceData
	 * @return
	 */
	public static Object addNewInterface(String scenariosid, String interfaceData) {
		JSONObject jsonObj = new JSONObject();
		if (StringUtils.isBlank(interfaceData)) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "请填写接口信息");
			return jsonObj;
		}
		URLActionNode urlActionNode = JSONObject.parseObject(interfaceData, URLActionNode.class);
		// 接口名称是否重复
		JSONObject interfaceJson = JSONObject.parseObject(interfaceData);
		String saveOrUpdateInterfaceFlag = interfaceJson.getString("saveOrUpdateInterfaceFlag");
		if (saveOrUpdateInterfaceFlag.equals("save")) {
			jsonObj = (JSONObject) InteractiveSceneCallDAO.checkUrlActionName(urlActionNode.getInterfaceName());
			if (jsonObj.getBooleanValue("success")) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "接口名称已存在");
				return jsonObj;
			}
		}
		boolean saveResult = ScenariosDAO.configInterfaceInfo(urlActionNode);
		if (saveResult) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("msg", "保存失败");
		return jsonObj;
	}

	/**
	 * 加载接口信息
	 * 
	 * @param scenariosid
	 * @return
	 */
	public static Object loadInterfaceName(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigKey2("第三方接口信息配制");
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("success", true);
			JSONArray rows = new JSONArray();
			for (int i = 0; i < rs.getRowCount(); i++) {
				if (rs.getRows()[i].get("name").toString().indexOf(serviceType) > -1) {
					String interfaceName = rs.getRows()[i].get("name").toString().split("::")[1];
					JSONObject row = new JSONObject();
					row.put("id", interfaceName);
					row.put("text", interfaceName);
					rows.add(row);
				}
			}
			jsonObj.put("rows", rows);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 查询接口信息
	 * 
	 * @param scenariosid
	 * @param interfaceName
	 * @return
	 */
	public static Object queryInterfaceInfo(String scenariosid, String interfaceName) {
		JSONObject jsonObj = new JSONObject();
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("第三方接口信息配制", serviceType + "::" + interfaceName.trim());
		if (rs != null && rs.getRowCount() > 0) {
			URLActionNode interfaceInfo = new URLActionNode();
			interfaceInfo.setInterfaceName(interfaceName);
			List<URLActionParam> inParams = new ArrayList<URLActionParam>();
			List<URLActionParam> outParams = new ArrayList<URLActionParam>();
			for (int i = 0; i < rs.getRowCount(); i++) {
				String configValue = rs.getRows()[i].get("name").toString();
				String[] configValueSplitArray = configValue.split(":=");
				if (configValue.split(":=").length > 1) {
					if (configValue.indexOf("URL") > -1) {
						interfaceInfo.setActionUrl(configValueSplitArray[1]);
					}
					if (configValue.indexOf("NameSpace") > -1) {
						interfaceInfo.setInvocationWay(UrlActionInvocationTypeConsts.WEBSERVICE);
						interfaceInfo.setNamespace(configValueSplitArray[1]);
					}
					if (configValue.indexOf("CallFuncName") > -1) {
						interfaceInfo.setFunctionName(configValueSplitArray[1]);
					}
					if (configValue.indexOf("Inner2CalledParasMap") > -1) {
						String inParamPair = configValueSplitArray[1];
						String inParamName = inParamPair.split("<-")[0];
						String inParamValue = inParamPair.split("<-")[1];
						URLActionParam inParam = new URLActionParam();
						inParam.setParamName(inParamName);
						inParam.setParamValue(inParamValue);
						inParams.add(inParam);
					}
					if (configValue.indexOf("CalledRes2InnerResParas") > -1) {
						String outParamPair = configValueSplitArray[1];
						String outParamName = outParamPair.split("->")[0];
						String outParamValue = outParamPair.split("->")[1];
						URLActionParam outParam = new URLActionParam();
						outParam.setParamName(outParamName);
						outParam.setParamValue(outParamValue);
						outParams.add(outParam);
					}
					if (configValue.indexOf("CallType") > -1) {
						interfaceInfo.setInvocationWay(UrlActionInvocationTypeConsts.HTTP);
						interfaceInfo.setHttpMethod(configValueSplitArray[1]);
					}
				}
			}
			interfaceInfo.setInParams(inParams);
			interfaceInfo.setOutParams(outParams);
			jsonObj.put("success", true);
			jsonObj.put("interfaceInfo", interfaceInfo);
			return jsonObj;
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

	/**
	 * 创建词类下拉框
	 */
	public static Object createwordclasscombobox() {
		JSONObject jsonObj = new JSONObject();
		JSONArray rows = new JSONArray();
		Result rs = CommonLibWordclassDAO.getFWordclass();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject row = new JSONObject();
				@SuppressWarnings("unused")
				String wordclassid = rs.getRows()[i].get("wordclassid").toString();
				String wordclass = rs.getRows()[i].get("wordclass").toString();
				row.put("id", wordclassid);
				row.put("text", wordclass);
				rows.add(row);
			}
			jsonObj.put("success", true);
			jsonObj.put("rows", rows);
			return jsonObj;
		}
		jsonObj.put("success", false);
		return jsonObj;
	}

}