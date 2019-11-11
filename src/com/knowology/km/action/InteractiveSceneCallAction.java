  package com.knowology.km.action;

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
	private String sceneElementName;
	private String sceneElementId;
	private String sceneType;
	private String interfaceName;

	private int page;
	private int rows;
	private String wordItem;
	private String wordClass;
	private String newWordItem;
	private String wordId;
	private String wordClassId;
	private String wordItems;
	private String wordAlias;
	private String wordAliases;
	private String standardWordId;
	private String standardWordIds;
	private String wordIds;
	private String synonyms;

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
		if ("saveCustomerAnswer".equals(type)) {// 新增用户回答
			m_result = InteractiveSceneCallDAO.saveCustomerAnswer(scenariosid, customeranswer, simpleWordPattern,
					wordPatternType, super.httpRequest);
		}
		if ("queryCustomerAnswer".equals(type)) {// 查询用户回答
			m_result = InteractiveSceneCallDAO.initCustomerAnswer(scenariosid);
		}
		if ("querySmsTemplate".equals(type)) {// 查询短信模板
			m_result = InteractiveSceneCallDAO.querySmsTemplate(scenariosid);
		}
		if ("queryCollectionIntention".equals(type)) {// 查询关联意图
			m_result = InteractiveSceneCallDAO.initCollectionIntention(scenariosid);
		}
		if ("saveCollectionIntention".equals(type)) {// 新增关联意图
			m_result = InteractiveSceneCallDAO.saveCollectionIntention(scenariosid, collectionIntention, simpleWordPattern,
					wordPatternType, super.httpRequest);
		}
		if ("deleteCollectionIntention".equals(type)) {// 删除关联意图
			m_result = ScenariosDAO.deleteRegnitionRule(scenariosid, collectionIntention);
		}
		if ("deleteCustomerAnswer".equals(type)) {// 删除用户答案
			m_result = ScenariosDAO.deleteRegnitionRule(scenariosid, customeranswer);
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
		if ("listPagingElementValue".equals(type)) {// 分页查询场景要素值
			m_result = InteractiveSceneCallDAO.listPagingWordItem(scenariosid, wordClassId, wordClass, page, rows);
		}
		if ("listPagingElementAlias".equals(type)) {// 分页查询词条别名
			m_result = InteractiveSceneCallDAO.listPagingWordAlias(scenariosid, wordItem, wordClass, page, rows);
		}
		if ("updateWordItem".equals(type)) {// 插入词条
			m_result = InteractiveSceneCallDAO.updateWordItem(scenariosid, newWordItem, wordId, wordClassId);
		}
		if ("insertWordItem".equals(type)) {// 更新词条
			m_result = InteractiveSceneCallDAO.insertWordItem(wordItems, wordClassId);
		}
		if ("saveWordItems".equals(type)) {// 批量保存词条
			m_result = InteractiveSceneCallDAO.saveWordItems(scenariosid, wordIds, wordItems, wordClassId);
		}
		if ("saveWordAlias".equals(type)) {// 批量保存别名
			m_result = InteractiveSceneCallDAO.saveWordAlias(scenariosid, wordClassId, wordClass, standardWordId, wordItem, wordIds, synonyms);
		}
		if ("deleteWordItem".equals(type)) {// 删除词条
			m_result = InteractiveSceneCallDAO.deleteWordItem(wordId, wordClass, wordItem);
		}
		if ("updateWordAlias".equals(type)) {// 更新别名
			m_result = InteractiveSceneCallDAO.updateWordAlias(wordAlias, wordId, wordClass);
		}
		if ("insertWordAlias".equals(type)) {// 插入别名
			m_result = InteractiveSceneCallDAO.insertWordAlias(wordAliases, wordClassId, wordClass, standardWordId, wordItem);
		}
		if ("deleteWordAlias".equals(type)) {// 删除别名
			m_result = InteractiveSceneCallDAO.deleteWordAlias(wordClassId, wordClass, standardWordId, wordItem, wordIds, wordAliases);
		}
		
		return "success";
	}

	public String getSceneJson() {
		return sceneJson;
	}

	public void setSceneJson(String sceneJson) {
		this.sceneJson = sceneJson;
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

	public String getSceneElementName() {
		return sceneElementName;
	}

	public void setSceneElementName(String sceneElementName) {
		this.sceneElementName = sceneElementName;
	}
	
	public String getSceneElementId() {
		return sceneElementId;
	}

	public void setSceneElementId(String sceneElementId) {
		this.sceneElementId = sceneElementId;
	}

	public String getSceneType() {
		return sceneType;
	}

	public void setSceneType(String sceneType) {
		this.sceneType = sceneType;
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

	public String getInterfaceData() {
		return interfaceData;
	}

	public void setInterfaceData(String interfaceData) {
		this.interfaceData = interfaceData;
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getWordItem() {
		return wordItem;
	}

	public void setWordItem(String wordItem) {
		this.wordItem = wordItem;
	}

	public String getWordClass() {
		return wordClass;
	}

	public void setWordClass(String wordClass) {
		this.wordClass = wordClass;
	}

	public String getNewWordItem() {
		return newWordItem;
	}

	public void setNewWordItem(String newWordItem) {
		this.newWordItem = newWordItem;
	}

	public String getWordId() {
		return wordId;
	}

	public void setWordId(String wordId) {
		this.wordId = wordId;
	}

	public String getWordClassId() {
		return wordClassId;
	}

	public void setWordClassId(String wordClassId) {
		this.wordClassId = wordClassId;
	}

	public String getWordItems() {
		return wordItems;
	}

	public void setWordItems(String wordItems) {
		this.wordItems = wordItems;
	}

	public String getWordAlias() {
		return wordAlias;
	}

	public void setWordAlias(String wordAlias) {
		this.wordAlias = wordAlias;
	}

	public String getWordAliases() {
		return wordAliases;
	}

	public void setWordAliases(String wordAliases) {
		this.wordAliases = wordAliases;
	}

	public String getStandardWordId() {
		return standardWordId;
	}

	public void setStandardWordId(String standardWordId) {
		this.standardWordId = standardWordId;
	}

	public String getStandardWordIds() {
		return standardWordIds;
	}

	public void setStandardWordIds(String standardWordIds) {
		this.standardWordIds = standardWordIds;
	}
	
	public String getWordIds() {
		return wordIds;
	}

	public void setWordIds(String wordIds) {
		this.wordIds = wordIds;
	}
	
	public String getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}

}