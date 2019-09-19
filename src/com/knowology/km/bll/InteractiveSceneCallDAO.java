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
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.dal.Database;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.constant.ActionTypeConsts;
import com.knowology.km.constant.CallOutNodeTypeConsts;
import com.knowology.km.constant.CallOutSceneElementConsts;
import com.knowology.km.constant.CollectionTypeConsts;
import com.knowology.km.constant.ResponseTypeConsts;
import com.knowology.km.constant.RuleTypeConsts;
import com.knowology.km.constant.SceneTypeConsts;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.pojo.CallInCollectionNode;
import com.knowology.km.pojo.CollectionNode;
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
	 * @param sceneType 场景类型
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
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		if (StringUtils.isBlank(robotId)) {
			logger.error("未查询到机器人ID，场景ID=" + scenariosid);
			return fail();
		}
		// 生成规则
		List<SceneRule> sceneRules = generateSceneRules(scenariosid, nodeDataList);
		// 保存规则
		saveResult = ScenariosDAO.insertSceneRules(scenariosid, sceneRules);
		if (!saveResult) {
			return fail();
		}
		return success();
	}
	
	/**
	 * 节点排序
	 */
	private static List<NodeData> sortNodeDataList(List<NodeData> nodeDataList) {
		if(nodeDataList == null || nodeDataList.isEmpty()) {
			return nodeDataList;
		}
		Map<String, NodeData> nodeDataKeys = new HashMap<String, NodeData>();
		for(NodeData nodeData : nodeDataList) {
			nodeDataKeys.put(nodeData.getKey(), nodeData);
		}
		// 开始节点
		List<NodeData> newNodeDataList = new ArrayList<NodeData>();
		Set<String> newNodeDataKeys = new LinkedHashSet<String>();
		NodeData startNode = nodeDataKeys.get(CallOutNodeTypeConsts.START_NODE);
		newNodeDataList.add(startNode);
		newNodeDataKeys.add(CallOutNodeTypeConsts.START_NODE);
		LoopNodeData(startNode, nodeDataKeys, newNodeDataKeys, newNodeDataList);
		return newNodeDataList;
	}
	private static List<NodeData> LoopNodeData(NodeData nodeData, Map<String, NodeData> nodeDataKeys,
			Set<String> newNodeDataKeys, List<NodeData> newNodeDataList) {
		if (!CallOutNodeTypeConsts.END_NODE.equals(nodeData.getCategory())) {
			if (nodeData.getToLinks() != null && !nodeData.getToLinks().isEmpty()) {
				for (LinkData linkData : nodeData.getToLinks()) {
					NodeData toNode = linkData.getToNode();
					if (!newNodeDataKeys.contains(toNode.getKey())) {
						newNodeDataKeys.add(toNode.getKey());
						newNodeDataList.add(toNode);
						LoopNodeData(nodeDataKeys.get(toNode.getKey()), nodeDataKeys, newNodeDataKeys, newNodeDataList);
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
	 * @param sceneType 场景类型
	 * @return 规则列表
	 */
	private static List<SceneRule> generateSceneRules(String scenariosid, List<NodeData> nodeDataList) {
		List<SceneRule> sceneRules = new ArrayList<SceneRule>();
		SceneRule sceneRule = null;
		int weight = 0;
		for (NodeData nodaData : nodeDataList) {
			List<LinkData> toLinks = nodaData.getToLinks();
			for (LinkData toLink : toLinks) {
				NodeData fromNode = toLink.getFromNode();
				NodeData toNode = toLink.getToNode();
				String toNodeCategory = toNode.getCategory();
				String fromPortText = toLink.getFromPort().getText();
				if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
					if (!CallOutNodeTypeConsts.CONDITION_NODE.equals(toNodeCategory)) {
						sceneRules = getLinkSceneRules(scenariosid, fromNode, toNode, fromPortText, null, sceneRules);
					}
				} 
				if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
					sceneRules = getLinkSceneRules(scenariosid, fromNode, toNode, fromPortText, null, sceneRules);
				}
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
	 * @param scenariosid    场景ID
	 * @param fromNode       源节点
	 * @param toNode         目的节点
	 * @param fromPortText   源端口
	 * @param conditionValue 条件值
	 * @param sceneRules     规则列表
	 * @return
	 */
	public static List<SceneRule> getLinkSceneRules(String scenariosid, NodeData fromNode, NodeData toNode, String fromPortText, String conditionValue, List<SceneRule> sceneRules) {
		String fromNodeCategory = fromNode.getCategory();
		// 开始组件
		if (CallOutNodeTypeConsts.START_NODE.equals(fromNodeCategory )) {
			sceneRules = generateStartSceneRules(scenariosid, toNode, sceneRules, null);
		}
		// 放音组件
		if (CallOutNodeTypeConsts.TTS_NODE.equals(fromNodeCategory)) {
			String customerAnswer = fromPortText;
			sceneRules = generateTTSSceneRules(scenariosid, (TTSNode) fromNode, toNode, customerAnswer, conditionValue,
					sceneRules);
		}
		// 信息收集组件
		if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(fromNodeCategory)) {
			if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
				sceneRules = InteractiveSceneCallOutDAO.generateCollectionNodeSceneRules(scenariosid, (CollectionNode) fromNode, toNode,
						conditionValue, sceneRules);
			}
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				sceneRules = InteractiveSceneCallInDAO.generateCollectionNodeSceneRules(scenariosid, (CallInCollectionNode) fromNode, toNode,
						conditionValue, sceneRules);
			}
		}
		// DTMF按键组件
		if (CallOutNodeTypeConsts.DTMF_NODE.equals(fromNodeCategory)) {
			String isGetPressNumber = fromPortText;
			sceneRules = generateDTMFNodeSceneRules(scenariosid, (DTMFNode) fromNode, toNode, isGetPressNumber,
					conditionValue, sceneRules);
		}
		// 转人工组件
		if (CallOutNodeTypeConsts.TRANSFER_NODE.equals(fromNodeCategory)) {
			sceneRules = generateTransferNodeSceneRules(scenariosid, (TransferNode) fromNode, toNode, conditionValue,
					sceneRules);
		}
		// 动作组件
		if (CallOutNodeTypeConsts.URL_ACTION_NODE.equals(fromNodeCategory)) {
			sceneRules = generateURLActionNodeSceneRules(scenariosid, (URLActionNode) fromNode, toNode, conditionValue,
					sceneRules);
		}
		// 条件组件
		if (CallOutNodeTypeConsts.CONDITION_NODE.equals(fromNodeCategory)) {
			conditionValue = fromPortText;
			if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
				sceneRules = InteractiveSceneCallOutDAO.generateConditionNodeSceneRules(scenariosid, (ConditionNode) fromNode, toNode,
						conditionValue, sceneRules);
			}
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				sceneRules = InteractiveSceneCallInDAO.generateConditionNodeSceneRules(scenariosid, (ConditionNode) fromNode, toNode,
						conditionValue, sceneRules);
			}
		}
		return sceneRules;
	}

	/**
	 * 开始组件 生成规则
	 * 
	 * @param scenariosid 场景ID
	 * @param toNode      连接组件
	 * @param weight      优先级
	 * @param sceneRules   规则集合
	 * @return
	 */
	private static List<SceneRule> generateStartSceneRules(String scenariosid, NodeData toNode,
			List<SceneRule> sceneRules, String conditionValue) {
		String ruleResponse = "业务信息获取(\"用户信息查询\");";
		ruleResponse += getCallOutRuleResponse(toNode);
		SceneRule sceneRule = generateInteractiveRule(scenariosid, null, "交互", null, null,
				null, null, null, null, null, conditionValue, ruleResponse);
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
	 * @param sceneRules        规则集合
	 * @return
	 */
	private static List<SceneRule> generateDTMFNodeSceneRules(String scenariosid, DTMFNode fromNode, NodeData toNode,
			String isGetPressNumber, String conditionValue, List<SceneRule> sceneRules) {
		if(StringUtils.isBlank(conditionValue)) {
			// 设置是否获取到按键值标识:是
			String condition = CallOutSceneElementConsts.ABOVE_NODE_ELEMENT_NAME + "=" + fromNode.getKey();
			condition += " and query" + "!=" + "\"\"";
			String result = "SET(\"" + CallOutSceneElementConsts.DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME + "\",\"是\");";
			result += "Input(\"" + fromNode.getDtmfAlias() + "\",\"@query\")";
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
		}
		if ("获取到按键值".equals(isGetPressNumber)) {
			// 获取到按键值，跳转节点
			String ruleResponse = getCallOutRuleResponse(toNode);
			SceneRule sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(), null,
					null, null, null, null, null, "是", conditionValue, ruleResponse);
			sceneRules.add(sceneRule);
		}
		if ("未获取到按键值".equals(isGetPressNumber)) {
			// 未获取到按键值，跳转节点
			String ruleResponse = getCallOutRuleResponse(toNode);
			SceneRule sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(), null,
					null, null, null, null, null, "否", conditionValue, ruleResponse);
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
	 * @param sceneRules      规则集合
	 * @return
	 */
	private static List<SceneRule> generateTTSSceneRules(String scenariosid, TTSNode fromNode, NodeData toNode,
			String customerAnswer, String conditionValue, List<SceneRule> sceneRules) {
		boolean isSetDifferentiated = isSetDifferentiated(toNode); // 是否设置区分节点
		String aboveNodeName = fromNode.getKey(); // 上文节点名
		customerAnswer = "跳出".equals(customerAnswer) ? "已选" : customerAnswer.trim(); // 用户答案
		String recognitionStatus = "跳出".equals(customerAnswer) ? "跳出" : null; // 理解状态
		String differentiatedNode = isSetDifferentiated ? toNode.getKey() : null; // 区分节点名
		// 意图交互规则
		if (!"跳出".equals(customerAnswer)) {
			String ruleResponse = getCallOutRuleResponse(toNode);
			SceneRule sceneRule = generateInteractiveRule(scenariosid, toNode.getKey(), aboveNodeName, customerAnswer,
					recognitionStatus, null, differentiatedNode, null, null, null, conditionValue, ruleResponse);
			sceneRules.add(sceneRule);
		} else {
			// 当前节点跳出时，所有上文节点需要配置跳出规则
			for (LinkData fromLink : fromNode.getFromLinks()) {
				String category = toNode.getCategory();
				if (CallOutNodeTypeConsts.TTS_NODE.equals(category)
						|| CallOutNodeTypeConsts.END_NODE.equals(category)) {
					// 跳出替换回复内容
					TTSNode ttsNode = new TTSNode(fromNode);
					ttsNode.setTts(((TTSNode) toNode).getTts());
					ttsNode.setCode(((TTSNode) toNode).getCode());
					ttsNode.setAction(((TTSNode) toNode).getAction());
					ttsNode.setActionParams(((TTSNode) toNode).getActionParams());
					ttsNode.setCode(((TTSNode) toNode).getCode());
					ttsNode.setOtherResponses(((TTSNode) toNode).getOtherResponses());
					String ruleResponse = getTTSRuleResponse(ttsNode);
					if (CallOutNodeTypeConsts.START_NODE.equals(fromLink.getFromNode().getCategory())) {
						SceneRule sceneRule = generateInteractiveRule(scenariosid, fromNode.getKey(), "交互", null, "跳出",
								null, null, null, null, null, conditionValue, ruleResponse);
						sceneRules.add(sceneRule);
					} else {
						SceneRule sceneRule = generateInteractiveRule(scenariosid, fromNode.getKey(),
								fromLink.getFromNode().getKey(), "已选", "跳出", null, fromNode.getKey(), null, null, null,
								conditionValue, ruleResponse);
						sceneRules.add(sceneRule);
					}
				}
			}
		}
		// 意图识别规则
		if (!"跳出".equals(customerAnswer) && !"未理解".equals(customerAnswer)) {
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
	 * @param sceneRules      规则集合
	 * @return
	 */
	private static List<SceneRule> generateURLActionNodeSceneRules(String scenariosid, URLActionNode fromNode,
			NodeData toNode, String conditionValue, List<SceneRule> sceneRules) {
		// 接口名称:场景名称+节点名称
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosid);
		String scenariosName = result.getRows()[0].get("service") + "";
		String interfaceName = scenariosName + fromNode.getActionName().trim();
		fromNode.setInterfaceName(interfaceName);
		// 配置接口信息
		ScenariosDAO.configInterfaceInfo(fromNode);
		// 跳转节点
		List<LinkData> fromLinks = fromNode.getFromLinks();
		if (null != fromLinks && !fromLinks.isEmpty()) {
			for (LinkData fromLink : fromLinks) {
				NodeData fromConditionNode = fromLink.getFromNode();
				String fromNodeText = fromLink.getFromPort().getText();
				sceneRules = getLinkSceneRules(scenariosid, fromConditionNode, toNode, fromNodeText, null, sceneRules);
			}
		}
		return sceneRules;
	}

	

	/**
	 * 转人工组件 生成规则
	 * 
	 * @param scenariosid    场景ID
	 * @param fromNode       转人工节点
	 * @param toNode         跳转节点
	 * @param conditionValue 条件值
	 * @param sceneRules      规则集合
	 * @return
	 */
	private static List<SceneRule> generateTransferNodeSceneRules(String scenariosid, TransferNode fromNode,
			NodeData toNode, String conditionValue, List<SceneRule> sceneRules) {
		// 跳转节点
		String ruleResponse = getCallOutRuleResponse(toNode);
		SceneRule sceneRule = generateInteractiveRule(scenariosid, null, fromNode.getKey(),
				null, null, null, null, null, null, null, conditionValue, ruleResponse);
		sceneRules.add(sceneRule);
		return sceneRules;
	}
	
	/**
	 * 获取回复内容
	 */
	public static String getCallOutRuleResponse(NodeData toNode) {
		String category = toNode.getCategory();
		if (CallOutNodeTypeConsts.TTS_NODE.equals(category) || CallOutNodeTypeConsts.END_NODE.equals(category)) {
			return getTTSRuleResponse((TTSNode) toNode);
		}
		if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(category)) {
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				return InteractiveSceneCallInDAO.getCollectionRuleResponse((CallInCollectionNode) toNode, null, null);
			}
			if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
				return InteractiveSceneCallOutDAO.getCollectionRuleResponse((CollectionNode) toNode, null, null);
			}
		}
		if (CallOutNodeTypeConsts.DTMF_NODE.equals(category)) {
			return getDTMFRuleResponse((DTMFNode) toNode);
		}
		if (CallOutNodeTypeConsts.CONDITION_NODE.equals(category)) {
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				return InteractiveSceneCallInDAO.getConditionRuleResponse((ConditionNode) toNode);
			}
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				return InteractiveSceneCallOutDAO.getConditionRuleResponse((ConditionNode) toNode);
			}
		}
		if (CallOutNodeTypeConsts.TRANSFER_NODE.equals(category)) {
			return getTransferResponse((TransferNode) toNode);
		}
		if (CallOutNodeTypeConsts.URL_ACTION_NODE.equals(category)) {
			return getURLActionResponse((URLActionNode) toNode);
		}
		return null;
	}

	/**
	 * 获取动作组件回复内容
	 */
	private static String getURLActionResponse(URLActionNode toNode) {
		String interfaceName = toNode.getInterfaceName();
		// 设置回复内容
		List<String> others = new ArrayList<String>();
		if (null != toNode.getInParams() && !toNode.getInParams().isEmpty()) {
			// 接口输入参数赋值
			for (URLActionParam inParam : toNode.getInParams()) {
				if (StringUtils.isNoneBlank(inParam.getParamName())) {
					others.add("Input(" + inParam.getParamName() + "," + inParam.getParamValue() + ")");
				}
			}
		}
		others.add("业务信息获取(\"" + interfaceName + "\")");
		String ruleResponse = ScenariosDAO.getRuleResponse(null, others);
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
		if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
			setItems.put("上文:节点名", toNode.getKey());
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
		if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
			setItems.put("上文:节点名", ttsNode.getKey());
		}
		setItems.put("节点名", ttsNode.getKey());
		setItems.put("是否末梢编码", CallOutNodeTypeConsts.END_NODE.equals(ttsNode.getCategory()) ? "是" : "否");
		setItems.put("TTS", StringUtils.isBlank(ttsNode.getTts()) ? "" : ttsNode.getTts());
		setItems.put("code", StringUtils.isBlank(ttsNode.getCode()) ? "" : ttsNode.getCode());
		setItems.put("action", StringUtils.isBlank(ttsNode.getAction()) ? "" : ttsNode.getAction());
		setItems.put("actionParams", StringUtils.isBlank(ttsNode.getActionParams()) ? "" : ttsNode.getActionParams());
		String ruleResponse = ScenariosDAO.getRuleResponse(setItems);
		return ruleResponse;
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
		if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
			setItems.put("上文:节点名", dtmfNode.getKey());
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
	 * 场景交互规则
	 */
	public static SceneRule generateInteractiveRule(String scenariosid, String currentNodeName, String aboveNodeName,
			String customerAnswer, String recognitionStatus, String notRecognitionTimes, String differentiatedNode,
			String collectionTimes, String collectionStatus, String isGetPressNumber, String conditionValue,
			String ruleResponse) {
		// 规则条件
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, aboveNodeName, customerAnswer,
				recognitionStatus, notRecognitionTimes, differentiatedNode, collectionTimes, collectionStatus,
				isGetPressNumber, conditionValue);
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues);
		// 生成规则
		SceneRule sceneRule = ScenariosDAO.buildSceneRuleInfo(scenariosid, null,
				RuleTypeConsts.INTERACTIVE_RULE, conditions, null, null, ruleResponse,
				ResponseTypeConsts.WRITTEN_RESPONSE_RULE, (weight++) + "");
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
			String collectionType, String collectionElement) {
		List<SceneElement> sceneElementValues = getSceneElementValues(scenariosid, aboveNodeName, customerAnswer,
				recognitionStatus, null, null, null, null, null, null); // 场景要素值
		String[] conditions = ScenariosDAO.getSceneConditions(scenariosid, sceneElementValues); // 规则条件
		String ruleResponse = ""; // 回复内容
		String questionObject = ""; // 问题对象
		String standardQuestion = ""; // 标准问题
		List<String> others = new ArrayList<String>();
		others.add("信息补全(\"用户回答\",\"上文\")");
		ruleResponse = ScenariosDAO.getRuleResponse(null, others);
		if (StringUtils.isNotBlank(differentiatedNode)) {
			Map<String, String> setItems = new HashMap<String, String>();
			setItems.put("区分节点", differentiatedNode);
			others = new ArrayList<String>();
			others.add("信息补全(\"用户回答|区分节点\",\"上文\")");
			ruleResponse = ScenariosDAO.getRuleResponse(setItems, others);
		}
		if (StringUtils.isNotBlank(collectionType)) {
			others = new ArrayList<String>();
			if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
				others.add("信息补全(\"" + collectionType + "\",\"上文\")");
			}
			if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
				others.add("信息补全(\"" + collectionType + "\",\"上文\",\"" + collectionElement + "\")");
			}
			ruleResponse = ScenariosDAO.getRuleResponse(null, others);
			questionObject = "识别规则业务";
			HashMap<String, String> collectionQuestions = queryCollectionQuestions(scenariosid);
			standardQuestion = collectionQuestions.get(collectionType.trim());
		}
		if (StringUtils.isNotBlank(customerAnswer)) {
			questionObject = "识别规则业务";
			HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosid);
			standardQuestion = recognitionQuestions.get(customerAnswer.trim());
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
				null, null, null);
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
				null, null, null);
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
				null, null, null, null, null);
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
				new String[21], null, null, ruleResponse, null, (weight++) + "");
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
			String collectionTimes, String collectionStatus, String isGetPressNumber, String conditionValue) {
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

		// 条件值
		sceneElementValue = ScenariosDAO.getSceneElementValue(CallOutSceneElementConsts.CONDITION_VALUE_ELEMENT_NAME,
				conditionValue);
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
				nodeDataList.add(nodeData);
			}
			if (CallOutNodeTypeConsts.TTS_NODE.equals(category) || CallOutNodeTypeConsts.END_NODE.equals(category)) {
				// 放音组件|结束语
				nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), TTSNode.class);
			}
			if (CallOutNodeTypeConsts.COLLECTION_NODE.equals(category)) {
				// 信息收集
				if(SceneTypeConsts.CALL_OUT.equals(sceneType)) {
					nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), CollectionNode.class);
				}
				if(SceneTypeConsts.CALL_IN.equals(sceneType)) {
					nodeData = JSONObject.parseObject(nodeDataJsonArray.getString(i), CallInCollectionNode.class);
				}
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
		result = queryPrivateRegnitionRule(scenariosId);
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
	 * 查询信息收集类型
	 */
	public static HashMap<String, String> queryCollectionQuestions(String scenariosId) {
		HashMap<String, String> collectionQuestions = new HashMap<String, String>();
		/**
		 * 查询公共信息类型
		 */
		Result result = ScenariosDAO.queryPublicCollectionType();
		if (result != null && result.getRowCount() > 0) {
			if (result != null && result.getRowCount() > 0) {
				for (int i = 0; i < result.getRowCount(); i++) {
					String abstractStr = (String) result.getRows()[i].get("abs");
					String collectionType = abstractStr.substring(abstractStr.indexOf(">") + 1);
					collectionQuestions.put(collectionType, abstractStr);
				}
			}
		}
		/**
		 * 查询自定义信息类型
		 */
		result = queryPrivateRegnitionRule(scenariosId);
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				String abstractStr = (String) result.getRows()[i].get("abs");
				String collectionType = abstractStr.substring(abstractStr.indexOf(">") + 1);
				collectionQuestions.put(collectionType, abstractStr);
			}
		}
		return collectionQuestions;
	}

	/**
	 * 查询自定义识别规则
	 */
	public static Result queryPrivateRegnitionRule(String scenariosId) {
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
	 * 获取识别规则业务ID
	 * 
	 * @param scenariosName 场景名
	 * @return 识别规则业务ID
	 */
	public static String getRegnitionRuleServiceId(String scenariosName) {
		String sql = "select SERVICEID from service s where s.service='识别规则业务' and PARENTNAME like ?";
		Result rs = Database.executeQuery(sql, scenariosName + "问题库%");
		if (rs != null && rs.getRowCount() > 0) {
			String serviceId = rs.getRows()[0].get("SERVICEID") + "";
			return serviceId;
		}
		return "";
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
			// 自动添加意图名
			simplewordpat = simplewordpat + "&用户回答=\"" + customeranswer + "\"";
			wordpat = wordpat + "&用户回答=\"" + customeranswer + "\"";

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
			checkInfo = "插入成功!";
		}
		// 将信息放入jsonObj的checkInfo对象中
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
		// 获取用户
		User user = (User) GetSession.getSessionByKey("accessUser");
		// 用户回答是否已存在
		HashMap<String, String> recognitionQuestions = queryRecognitionQuestions(scenariosid);
		if (!recognitionQuestions.isEmpty()) {
			if (recognitionQuestions.containsKey(customerAnswer)) {
				jsonObj.put("checkInfo", "用户回答已存在");
				return jsonObj;
			}
		}
		// 获取场景名称
		Result result = CommonLibServiceDAO.getServiceInfoByserviceid(scenariosid);
		if (result == null || result.getRowCount() == 0) {
			jsonObj.put("checkInfo", "场景信息未查询到");
			return jsonObj;
		}
		String scenariosName = result.getRows()[0].get("service") + "";
		// 获取父场景名称
		String parentScenariosId = result.getRows()[0].get("PARENTID") + "";
		result = CommonLibServiceDAO.getServiceInfoByserviceid(parentScenariosId);
		if (result == null || result.getRowCount() == 0) {
			jsonObj.put("checkInfo", "父场景信息未查询到");
			return jsonObj;
		}
		String parentScenariosName = result.getRows()[0].get("service") + "";
		// 识别规则业务ID
		String serviceId = getRegnitionRuleServiceId(parentScenariosName.trim());
		// 获取机器人ID
		String robotId = ScenariosDAO.getSceneRobotID(scenariosid);
		String cityCode = ScenariosDAO.getRobotCityCode(robotId);
		// 用户回答父类新增词条
		JSONObject res = (JSONObject) WordClassDAO.select("sys" + scenariosName + "用户回答父类", true, "全部", 0, 10);
		if (res.getInteger("total") == 0) {
			jsonObj.put("checkInfo", "sys" + scenariosName + "用户回答父类未查询到");
			return jsonObj;
		}
		String wordClassId = res.getJSONArray("root").getJSONObject(0).getString("wordclassid");
		res = (JSONObject) WorditemDAO.insert(customerAnswer, (String) res.get("wordclass"), wordClassId, "", true);
		if (res.getBooleanValue("success") != true) {
			jsonObj.put("checkInfo", "sys" + scenariosName + "用户回答父类插入词条失败");
			return jsonObj;
		}
		// 识别业务规则新增标准问
		QuestionManageDAO.createNormalQuery(user, serviceId, customerAnswer);
		List<String> serviceIds = new ArrayList<String>();
		serviceIds.add(serviceId);
		String kbdataid = getCustomerAnswerKbdataId(serviceId, customerAnswer);
		// 刷新用户回答
		initCustomerAnswer(scenariosid);
		// 用户回答标准问新增词模
		return saveWordpat(kbdataid, customerAnswer, simpleWordPattern, wordPatternType, cityCode, request);
	}

	/**
	 * 查询用户答案
	 * 
	 * @param scenariosid 场景ID
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
	 * 初始化信息收集类型
	 */
	public static Object initCollectionType(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		HashMap<String, String> collectionQuestions = queryCollectionQuestions(scenariosid);
		if (collectionQuestions.isEmpty()) {
			jsonObj.put("success", false);
		}
		// 【收集任意类型】选项
		JSONObject temObj = new JSONObject();
		temObj.put("id", CollectionTypeConsts.SYSTEM_ANY);
		temObj.put("text", CollectionTypeConsts.SYSTEM_ANY);
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
	 * @param scenariosId 场景ID
	 * @param sceneElementName 场景要素名称
	 * @return
	 */
	public static Object listAllElementName(String scenariosId, String sceneElementName) {
		JSONObject jsonObj = (JSONObject) SceneElementDAO.listAllElementName(scenariosId, sceneElementName);
		if(jsonObj.getIntValue("total") > 0) {
			JSONArray newRows = new JSONArray();
			JSONArray rows = jsonObj.getJSONArray("rows");
			for(int i = 0; i < rows.size(); i++) {
				JSONObject row = rows.getJSONObject(i);
				if(!checkIfSystemSceneElements(row.getString("name"))) {
					newRows.add(row);
				}
			}
			jsonObj.put("rows", newRows);
			jsonObj.put("total", newRows.size());
		}
		return jsonObj.getJSONArray("rows");
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
		if(totalCount > 10) {
			int totalPage = totalCount / pageSize + 1;
			while (currentPage <= totalPage) {
				jsonObj = (JSONObject) SceneElementDAO.listPagingSceneElements(scenariosId, sceneElementName, currentPage,
						pageSize);
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
		if(systemSceneElements.contains(sceneElementName)) {
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

}