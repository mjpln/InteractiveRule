  package com.knowology.km.action;

import com.knowology.km.bll.InteractiveSceneCallInDAO;

public class InteractiveSceneCallInAction extends BaseAction {

	private String sceneJson;
	private Object m_result;
	private String type;

	private String scenariosid;
	private String scenariosName;
	private String robotId;
	private String robotName;
	private String autoWordPattern;
	private String collectionType;
	private String customeranswer;
	private String simpleWordPattern;
	private String wordPatternType;
	private String ioa;
	private String actionUrl;
	private String actionName;
	private String sceneElementName;
	private String sceneElementId;

	private int page;
	private int rows;

	public Object execute() {
		if ("saveConfig".equals(type)) { // 解析并保存流程图
			m_result = InteractiveSceneCallInDAO.saveSceneRules(sceneJson, scenariosid);
		}
		if ("loadConfigData".equals(type)) {// 加载场景配置数据
			m_result = InteractiveSceneCallInDAO.loadData(scenariosid);
		}
		if ("configRobot".equals(type)) {// 配置机器人信息
			m_result = InteractiveSceneCallInDAO.configRobot(robotId, robotName, scenariosid);
		}
		if ("getUrl".equals(type)) { // 要打开的场景页面
			m_result = InteractiveSceneCallInDAO.getUrl(ioa);
		}
		if ("autoWordPattern".equals(type)) {// 生成词模
			m_result = InteractiveSceneCallInDAO.autoGenerateWordpat(scenariosid, autoWordPattern);
		}
		if ("saveCustomerAnswer".equals(type)) {// 新增用户回答
			m_result = InteractiveSceneCallInDAO.saveCustomerAnswer(scenariosid, customeranswer, simpleWordPattern,
					wordPatternType, super.httpRequest);
		}
		if ("queryCustomerAnswer".equals(type)) {// 查询用户回答
			m_result = InteractiveSceneCallInDAO.initCustomerAnswer(scenariosid);
		}
		if ("querySmsTemplate".equals(type)) {// 查询短信模板
			m_result = InteractiveSceneCallInDAO.querySmsTemplate(scenariosid);
		}
		if ("saveCollectionType".equals(type)) {// 新增信息收集类型
			m_result = InteractiveSceneCallInDAO.saveCollectionType(scenariosid, collectionType, simpleWordPattern,
					wordPatternType, super.httpRequest);
		}
		if ("queryCollectionType".equals(type)) {// 查询信息收集类型
			m_result = InteractiveSceneCallInDAO.initCollectionType(scenariosid);
		}
		if ("queryPhoneAttributeNames".equals(type)) {// 查询号码属性
			m_result = InteractiveSceneCallInDAO.queryPhoneAttributeNames();
		}
		if ("testUrl".equals(type)) {// 测试URL
			m_result = InteractiveSceneCallInDAO.testURLAction(actionUrl);
		}
		if ("checkUrlActionName".equals(type)) {// 校验接口名称是否重复
			m_result = InteractiveSceneCallInDAO.checkUrlActionName(actionName);
		}
		if ("listAllSceneElement".equals(type)) {// 查询全部场景要素
			m_result = InteractiveSceneCallInDAO.listAllElementName(scenariosid, sceneElementName);
		}
		if ("listPagingSceneElement".equals(type)) {// 分页查询场景要素
			m_result = InteractiveSceneCallInDAO.listPagingSceneElement(scenariosid, sceneElementName, page, rows);
		}
		if ("listAllElementValue".equals(type)) {// 查询场景要素值
			m_result = InteractiveSceneCallInDAO.listAllElementValue(scenariosid, sceneElementName);
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

	public String getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
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

}