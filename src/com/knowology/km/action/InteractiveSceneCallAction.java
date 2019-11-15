  package com.knowology.km.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.knowology.km.bll.InteractiveSceneCallDAO;
import com.knowology.km.bll.ScenariosDAO;

public class InteractiveSceneCallAction extends BaseAction {

	private String sceneJson;
	private String interfaceData;
	private Object m_result;
	private String type;

	private String scenariosid;
	private String scenariosName;
	private String robotId;
	private String robotName;
	private String autoWordPattern;
	private String collectionIntention;
	private String customeranswer;
	private String simpleWordPattern;
	private String wordPatternType;
	private String ioa;
	private String actionUrl;
	private String actionName;
	private String sceneElementId;
	private String sceneElementName;
	private String sceneElementValue;
	private String sceneType;
	private String interfaceName;

	private int page;
	private int rows;
	private String wordClassId;
	private String wordClasses;
	private String wordClass;
	private String wordIds;
	private String wordId;
	private String newWordItem;
	private String wordItems;
	private String wordItem;
	private String wordAliases;
	private String standardWordIds;
	private String standardWordId;
	private String synonyms;
	private String wordAlias;
	private String normalQuery;
	private String recognitionType;
	private String wordClassIds;
	private String returnKeyValueJsonStr;
	private String customerQuery;
	private String normalQueries;
	private String kbdataIds;
	private String kbdataId;
	private String returnKeyValueJsons;
	private String serviceId;
	

	public Object execute() {
		if ("saveConfig".equals(type)) { // 解析并保存流程图
			m_result = InteractiveSceneCallDAO.saveSceneRules(sceneJson, scenariosid, sceneType);
		}
		if ("loadConfigData".equals(type)) {// 加载场景配置数据
			m_result = ScenariosDAO.loadData(scenariosid);
		}
		if ("configRobot".equals(type)) {// 配置机器人信息
			m_result = ScenariosDAO.configRobot(robotId, robotName, scenariosid);
		}
		if ("getUrl".equals(type)) { // 要打开的场景页面
			m_result = ScenariosDAO.getUrl(ioa);
		}
		if ("autoWordPattern".equals(type)) {// 生成词模
			m_result = InteractiveSceneCallDAO.autoGenerateWordpat(scenariosid, autoWordPattern);
		}
		if ("querySmsTemplate".equals(type)) {// 查询短信模板
			m_result = InteractiveSceneCallDAO.querySmsTemplate(scenariosid);
		}
		if ("queryPhoneAttributeNames".equals(type)) {// 查询号码属性
			m_result = InteractiveSceneCallDAO.queryPhoneAttributeNames();
		}
		if ("testUrl".equals(type)) {// 测试URL
			m_result = InteractiveSceneCallDAO.testURLAction(actionUrl);
		}
		if ("checkUrlActionName".equals(type)) {// 校验接口名称是否重复
			m_result = InteractiveSceneCallDAO.checkUrlActionName(actionName);
		}
		if ("listAllSceneElement".equals(type)) {// 查询全部场景要素
			m_result = InteractiveSceneCallDAO.listAllElementName(scenariosid, sceneElementName);
		}
		if ("listPagingSceneElement".equals(type)) {// 分页查询场景要素
			m_result = InteractiveSceneCallDAO.listPagingSceneElement(scenariosid, sceneElementName, page, rows);
		}
		if ("listAllElementValue".equals(type)) {// 查询场景要素值
			m_result = InteractiveSceneCallDAO.listAllElementValue(scenariosid, sceneElementName);
		}
		if ("addSceneElementWordPattern".equals(type)) {// 新增场景要素词模
			m_result = InteractiveSceneCallDAO.addSceneElementWordPattern(scenariosid, scenariosName, sceneType, normalQuery, simpleWordPattern, wordPatternType, returnKeyValueJsonStr, super.httpRequest);
		}
		if ("addNewInterface".equals(type)) {// 添加新接口
			m_result = InteractiveSceneCallDAO.addNewInterface(scenariosid, interfaceData);
		}
		if ("loadInterfaceName".equals(type)) {// 加载接口列表
			m_result = InteractiveSceneCallDAO.loadInterfaceName(scenariosid);
		}
		if ("queryInterfaceInfo".equals(type)) {// 查询接口信息
			m_result = InteractiveSceneCallDAO.queryInterfaceInfo(scenariosid, interfaceName);
		}
		if ("createwordclasscombobox".equals(type)) {// 创建词类下拉框
			m_result = InteractiveSceneCallDAO.createwordclasscombobox();
		}
		if ("listPagingWordItem".equals(type)) {// 分页查询词条
			m_result = ScenariosDAO.listPagingWordItem(scenariosid, wordClassId, wordClass, wordItem, page, rows);
		}
		if ("listPagingWordAlias".equals(type)) {// 分页查询词条别名
			m_result = ScenariosDAO.listPagingWordAlias(scenariosid, wordItem, wordClass, wordAlias, page, rows);
		}
		if ("updateWordItem".equals(type)) {// 插入词条
			m_result = ScenariosDAO.updateWordItem(scenariosid, newWordItem, wordId, wordClassId);
		}
		if ("insertWordItem".equals(type)) {// 更新词条
			m_result = ScenariosDAO.insertWordItem(wordItems, wordClassId);
		}
		if ("saveWordItems".equals(type)) {// 批量保存词条
			m_result = ScenariosDAO.saveWordItems(scenariosid, wordIds, wordItems, wordClassId);
		}
		if ("saveWordAlias".equals(type)) {// 批量保存别名
			m_result = ScenariosDAO.saveWordAlias(scenariosid, wordClassId, wordClass, standardWordId, wordItem, wordIds, synonyms);
		}
		if ("deleteWordItem".equals(type)) {// 删除词条
			m_result = ScenariosDAO.deleteWordItem(wordId, wordClass, wordItem);
		}
		if ("updateWordAlias".equals(type)) {// 更新别名
			m_result = ScenariosDAO.updateWordAlias(wordAlias, wordId, wordClass);
		}
		if ("insertWordAlias".equals(type)) {// 插入别名
			m_result = ScenariosDAO.insertWordAlias(wordAliases, wordClassId, wordClass, standardWordId, wordItem);
		}
		if ("deleteWordAlias".equals(type)) {// 删除别名
			m_result = ScenariosDAO.deleteWordAlias(wordClassId, wordClass, standardWordId, wordItem, wordIds, wordAliases);
		}
		if ("listPagingWordClass".equals(type)) {// 分页查询词类
			m_result = ScenariosDAO.listPagingWordClass(scenariosid, wordClass, page, rows);
		}
		if ("saveWordClasses".equals(type)) {// 批量保存词类
			List<String> wordClassIdList = new ArrayList<String>();
			List<String> wordClassList = new ArrayList<String>();
			if(StringUtils.isNotBlank(wordClasses)) {
				for(int i = 0; i < wordClasses.split("\\,").length; i++) {
					String wordClassId = wordClassIds.split("\\,")[i];
					String wordClass = wordClasses.split("\\,")[i];
					wordClassIdList.add(wordClassId);
					wordClassList.add(wordClass);
				}
				m_result = ScenariosDAO.saveWordClasses(scenariosid, wordClassIdList, wordClassList);
			}
		}
		if ("insertWordClass".equals(type)) {// 新增词类
			if(StringUtils.isNotBlank(wordClasses)) {
				List<String> wordClassList = new ArrayList<String>();
				for(String wordClass : wordClasses.split("\\,")) {
					wordClassList.add(wordClass);
				}
				m_result = ScenariosDAO.insertWordClass(scenariosid, wordClassList);
			}
		}
		if ("updateWordClass".equals(type)) {// 更新词类
			m_result = ScenariosDAO.updateWordClass(scenariosid, wordClassId, wordClass);
		}
		if ("deleteWordClass".equals(type)) {// 删除词类
			m_result = ScenariosDAO.deleteWordClass(scenariosid, wordClassId, wordClass);
		}
		if ("listRecognitionRuleNormalQuery".equals(type)) {// 查询识别规则业务
			m_result = ScenariosDAO.listRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName, normalQuery, scenariosid);
		}
		if ("saveRecognitionRuleNormalQuery".equals(type)) {// 保存识别规则业务
			if(StringUtils.isNotBlank(normalQueries)) {
				List<String> kbdataIdList = new ArrayList<String>();
				List<String> normalQueryList = new ArrayList<String>();
				List<String> returnKeyValueJsonList =  new ArrayList<String>();
				for(int i=0;i<normalQueries.split("\\@@").length;i++) {
					String normalQuery = normalQueries.split("\\@@")[i];
					String kbdataId = kbdataIds.split("\\@@")[i];
					String returnKeyValueJson = returnKeyValueJsons.split("\\@@")[i];
					normalQueryList.add(normalQuery);
					kbdataIdList.add(kbdataId);
					returnKeyValueJsonList.add(returnKeyValueJson);
				}
				m_result = ScenariosDAO.saveRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName, kbdataIdList, normalQueryList, returnKeyValueJsonList, scenariosid);
			}
		}
		if ("insertRecognitionRuleNormalQuery".equals(type)) {// 添加识别规则业务
			m_result = ScenariosDAO.insertRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName, normalQuery, returnKeyValueJsonStr, scenariosid);
		}
		if ("updateRecognitionRuleNormalQuery".equals(type)) {// 更新识别规则业务
			m_result = ScenariosDAO.updateRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName, kbdataId, normalQuery, scenariosid);
		}
		if ("deleteRecognitionRuleNormalQuery".equals(type)) {// 删除识别规则业务
			m_result = ScenariosDAO.deleteRecognitionRuleNormalQuery(sceneType, recognitionType, scenariosName, kbdataId, normalQuery, scenariosid);
		}
		if ("addNormalQueryWithReturnValues".equals(type)) {// 添加标准问和返回值
			m_result = ScenariosDAO.insertNormalQueryWithReturnValues(scenariosid, sceneType, scenariosName, normalQuery, returnKeyValueJsonStr);
		}
		if ("getCustomerQueryPageUrl".equals(type)) {// 获取客户问页面跳转地址
			m_result = ScenariosDAO.getCustomerQueryPageUrl(serviceId, scenariosid, sceneType, scenariosName, normalQuery,
					customerQuery, returnKeyValueJsonStr);
		}
		return "success";
	}


	public String getSceneJson() {
		return sceneJson;
	}


	public void setSceneJson(String sceneJson) {
		this.sceneJson = sceneJson;
	}


	public String getInterfaceData() {
		return interfaceData;
	}


	public void setInterfaceData(String interfaceData) {
		this.interfaceData = interfaceData;
	}


	public Object getM_result() {
		return m_result;
	}


	public void setM_result(Object m_result) {
		this.m_result = m_result;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getScenariosid() {
		return scenariosid;
	}


	public void setScenariosid(String scenariosid) {
		this.scenariosid = scenariosid;
	}


	public String getScenariosName() {
		return scenariosName;
	}


	public void setScenariosName(String scenariosName) {
		this.scenariosName = scenariosName;
	}


	public String getRobotId() {
		return robotId;
	}


	public void setRobotId(String robotId) {
		this.robotId = robotId;
	}


	public String getRobotName() {
		return robotName;
	}


	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}


	public String getAutoWordPattern() {
		return autoWordPattern;
	}


	public void setAutoWordPattern(String autoWordPattern) {
		this.autoWordPattern = autoWordPattern;
	}


	public String getCollectionIntention() {
		return collectionIntention;
	}


	public void setCollectionIntention(String collectionIntention) {
		this.collectionIntention = collectionIntention;
	}


	public String getCustomeranswer() {
		return customeranswer;
	}


	public void setCustomeranswer(String customeranswer) {
		this.customeranswer = customeranswer;
	}


	public String getSimpleWordPattern() {
		return simpleWordPattern;
	}


	public void setSimpleWordPattern(String simpleWordPattern) {
		this.simpleWordPattern = simpleWordPattern;
	}


	public String getWordPatternType() {
		return wordPatternType;
	}


	public void setWordPatternType(String wordPatternType) {
		this.wordPatternType = wordPatternType;
	}


	public String getIoa() {
		return ioa;
	}


	public void setIoa(String ioa) {
		this.ioa = ioa;
	}


	public String getActionUrl() {
		return actionUrl;
	}


	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}


	public String getActionName() {
		return actionName;
	}


	public void setActionName(String actionName) {
		this.actionName = actionName;
	}


	public String getSceneElementId() {
		return sceneElementId;
	}


	public void setSceneElementId(String sceneElementId) {
		this.sceneElementId = sceneElementId;
	}


	public String getSceneElementName() {
		return sceneElementName;
	}


	public void setSceneElementName(String sceneElementName) {
		this.sceneElementName = sceneElementName;
	}


	public String getSceneElementValue() {
		return sceneElementValue;
	}


	public void setSceneElementValue(String sceneElementValue) {
		this.sceneElementValue = sceneElementValue;
	}


	public String getSceneType() {
		return sceneType;
	}


	public void setSceneType(String sceneType) {
		this.sceneType = sceneType;
	}


	public String getInterfaceName() {
		return interfaceName;
	}


	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}


	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}


	public int getRows() {
		return rows;
	}


	public void setRows(int rows) {
		this.rows = rows;
	}


	public String getWordClassId() {
		return wordClassId;
	}


	public void setWordClassId(String wordClassId) {
		this.wordClassId = wordClassId;
	}


	public String getWordClasses() {
		return wordClasses;
	}


	public void setWordClasses(String wordClasses) {
		this.wordClasses = wordClasses;
	}


	public String getWordClass() {
		return wordClass;
	}


	public void setWordClass(String wordClass) {
		this.wordClass = wordClass;
	}


	public String getWordIds() {
		return wordIds;
	}


	public void setWordIds(String wordIds) {
		this.wordIds = wordIds;
	}


	public String getWordId() {
		return wordId;
	}


	public void setWordId(String wordId) {
		this.wordId = wordId;
	}


	public String getNewWordItem() {
		return newWordItem;
	}


	public void setNewWordItem(String newWordItem) {
		this.newWordItem = newWordItem;
	}


	public String getWordItems() {
		return wordItems;
	}


	public void setWordItems(String wordItems) {
		this.wordItems = wordItems;
	}


	public String getWordItem() {
		return wordItem;
	}


	public void setWordItem(String wordItem) {
		this.wordItem = wordItem;
	}


	public String getWordAliases() {
		return wordAliases;
	}


	public void setWordAliases(String wordAliases) {
		this.wordAliases = wordAliases;
	}


	public String getStandardWordIds() {
		return standardWordIds;
	}


	public void setStandardWordIds(String standardWordIds) {
		this.standardWordIds = standardWordIds;
	}


	public String getStandardWordId() {
		return standardWordId;
	}


	public void setStandardWordId(String standardWordId) {
		this.standardWordId = standardWordId;
	}


	public String getSynonyms() {
		return synonyms;
	}


	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}


	public String getWordAlias() {
		return wordAlias;
	}


	public void setWordAlias(String wordAlias) {
		this.wordAlias = wordAlias;
	}


	public String getNormalQuery() {
		return normalQuery;
	}


	public void setNormalQuery(String normalQuery) {
		this.normalQuery = normalQuery;
	}


	public String getRecognitionType() {
		return recognitionType;
	}


	public void setRecognitionType(String recognitionType) {
		this.recognitionType = recognitionType;
	}


	public String getWordClassIds() {
		return wordClassIds;
	}


	public void setWordClassIds(String wordClassIds) {
		this.wordClassIds = wordClassIds;
	}


	public String getReturnKeyValueJsonStr() {
		return returnKeyValueJsonStr;
	}


	public void setReturnKeyValueJsonStr(String returnKeyValueJsonStr) {
		this.returnKeyValueJsonStr = returnKeyValueJsonStr;
	}


	public String getCustomerQuery() {
		return customerQuery;
	}


	public void setCustomerQuery(String customerQuery) {
		this.customerQuery = customerQuery;
	}


	public String getNormalQueries() {
		return normalQueries;
	}


	public void setNormalQueries(String normalQueries) {
		this.normalQueries = normalQueries;
	}


	public String getKbdataIds() {
		return kbdataIds;
	}


	public void setKbdataIds(String kbdataIds) {
		this.kbdataIds = kbdataIds;
	}


	public String getKbdataId() {
		return kbdataId;
	}


	public void setKbdataId(String kbdataId) {
		this.kbdataId = kbdataId;
	}


	public String getReturnKeyValueJsons() {
		return returnKeyValueJsons;
	}


	public void setReturnKeyValueJsons(String returnKeyValueJsons) {
		this.returnKeyValueJsons = returnKeyValueJsons;
	}


	public String getServiceId() {
		return serviceId;
	}


	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
}