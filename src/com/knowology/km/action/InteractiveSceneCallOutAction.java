package com.knowology.km.action;

import javax.servlet.http.HttpServletRequest;

import com.knowology.km.bll.InteractiveSceneCallOutDAO;

public class InteractiveSceneCallOutAction extends BaseAction {
	private String sceneJson;
	private Object m_result;
	private String type;

	private HttpServletRequest request;

	private String scenariosid;
	private String robotId;
	private String robotName;
	private String autowordpat;
	private String customeranswer;
	private String simplewordpat;
	private String wordpattype;
	private String ioa;
	private String actionUrl;
	private String actionName;

	public Object execute() {
		if ("saveConfig".equals(type)) { // 解析并保存流程图
			m_result = InteractiveSceneCallOutDAO.saveSceneRules(sceneJson, scenariosid);
		}
		if ("loadConfigData".equals(type)) {// 加载场景配置数据
			m_result = InteractiveSceneCallOutDAO.loadData(scenariosid);
		}
		if ("configRobot".equals(type)) {// 配置机器人信息
			m_result = InteractiveSceneCallOutDAO.configRobot(robotId, robotName, scenariosid);
		}
		if ("getUrl".equals(type)) { // 要打开的场景页面
			m_result = InteractiveSceneCallOutDAO.getUrl(ioa);
		}
		if ("autowordpat".equals(type)) {// 生成词模
			m_result = InteractiveSceneCallOutDAO.autoGenerateWordpat(scenariosid, autowordpat);
		}
		if ("saveCustomerAnswer".equals(type)) {// 新增用户回答
			m_result = InteractiveSceneCallOutDAO.saveCustomerAnswer(scenariosid, customeranswer, simplewordpat,
					wordpattype, request);
		}
		if ("queryCustomerAnswer".equals(type)) {// 查询用户回答
			m_result = InteractiveSceneCallOutDAO.initCustomerAnswer(scenariosid);
		}
		if ("querySmsTemplate".equals(type)) {// 查询短信模板
			m_result = InteractiveSceneCallOutDAO.querySmsTemplate(scenariosid);
		}
		if ("queryCollectionType".equals(type)) {// 查询信息收集类型
			m_result = InteractiveSceneCallOutDAO.initCollectionType(scenariosid);
		}
		if ("queryPhoneAttributeNames".equals(type)) {// 查询号码属性
			m_result = InteractiveSceneCallOutDAO.queryPhoneAttributeNames();
		}
		if ("testUrl".equals(type)) {// 测试URL
			m_result = InteractiveSceneCallOutDAO.testURLAction(actionUrl);
		}
		if ("checkUrlActionName".equals(type)) {// 校验接口名称是否重复
			m_result = InteractiveSceneCallOutDAO.checkUrlActionName(actionName);
		}	
		return "success";
	}

	public String getM_request() {
		return sceneJson;
	}

	public void setM_request(String m_request) {
		this.sceneJson = m_request;
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

	public String getCustomeranswer() {
		return customeranswer;
	}

	public void setCustomeranswer(String customeranswer) {
		this.customeranswer = customeranswer;
	}

	public String getAutowordpat() {
		return autowordpat;
	}

	public void setAutowordpat(String autowordpat) {
		this.autowordpat = autowordpat;
	}

	public String getSimplewordpat() {
		return simplewordpat;
	}

	public void setSimplewordpat(String simplewordpat) {
		this.simplewordpat = simplewordpat;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getWordpattype() {
		return wordpattype;
	}

	public void setWordpattype(String wordpattype) {
		this.wordpattype = wordpattype;
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
}