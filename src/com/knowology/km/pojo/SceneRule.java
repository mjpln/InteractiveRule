package com.knowology.km.pojo;

/**
 * 场景规则
 */
public class SceneRule {

	/**
	 * 规则ID
	 */
	private String ruleId;
	/**
	 * 场景ID
	 */
	private String scenariosId;
	/**
	 * 用户问题
	 */
	private String userQuestion;
	/**
	 * 场景要素值1
	 */
	private String condition1;
	/**
	 * 场景要素值2
	 */
	private String condition2;

	/**
	 * 场景要素值3
	 */
	private String condition3;
	/**
	 * 场景要素值4
	 */
	private String condition4;
	/**
	 * 场景要素值5
	 */
	private String condition5;
	/**
	 * 场景要素值6
	 */
	private String condition6;
	/**
	 * 场景要素值7
	 */
	private String condition7;
	/**
	 * 场景要素值8
	 */
	private String condition8;
	/**
	 * 场景要素值9
	 */
	private String condition9;
	/**
	 * 场景要素值10
	 */
	private String condition10;
	/**
	 * 场景要素值11
	 */
	private String condition11;
	/**
	 * 场景要素值12
	 */
	private String condition12;
	/**
	 * 场景要素值13
	 */
	private String condition13;
	/**
	 * 场景要素值14
	 */
	private String condition14;
	/**
	 * 场景要素值15
	 */
	private String condition15;
	/**
	 * 场景要素值16
	 */
	private String condition16;
	/**
	 * 场景要素值17
	 */
	private String condition17;
	/**
	 * 场景要素值18
	 */
	private String condition18;
	/**
	 * 场景要素值19
	 */
	private String condition19;
	/**
	 * 场景要素值20
	 */
	private String condition20;
	/**
	 * 规则类型 0-缺失补全规则 1-问题要素冲突判断规则 2-其他规则 3-识别规则 4-数据 5-触发规则
	 */
	private String ruleType;
	/**
	 * 规则优先级
	 */
	private String weight;
	/**
	 * 地市(区域)编码
	 */
	private String cityCode;
	/**
	 * 地市(区域)名称
	 */
	private String cityName;
	/**
	 * 排除地市
	 */
	private String excludedCity;
	/**
	 * 上文问题对象
	 */
	private String aboveQuestionObecjt;
	/**
	 * 上文标准问题
	 */
	private String aboveStandardQuestion;
	/**
	 * 回复类型 0-文本 -1-手写规则 2-菜单询问
	 */
	private String responseType;
	/**
	 * 问题对象
	 */
	private String questionObject;
	/**
	 * 标准问题
	 */
	private String standardQuestion;
	/**
	 * 交互选项
	 */
	private String interactiveOptions;
	/**
	 * 回复内容
	 */
	private String ruleResponse;
	/**
	 * 回复内容模板
	 */
	private String ruleResponseTemplate;
	/**
	 * 业务ID
	 */
	private String relationServiceId;
	/**
	 * 是否允许修改
	 */
	private String isEdit;
	/**
	 * 当前节点值
	 */
	private String currentNode;
	
	public SceneRule() {}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getScenariosId() {
		return scenariosId;
	}

	public void setScenariosId(String scenariosId) {
		this.scenariosId = scenariosId;
	}

	public String getUserQuestion() {
		return userQuestion;
	}

	public void setUserQuestion(String userQuestion) {
		this.userQuestion = userQuestion;
	}

	public String getCondition1() {
		return condition1;
	}

	public void setCondition1(String condition1) {
		this.condition1 = condition1;
	}

	public String getCondition2() {
		return condition2;
	}

	public void setCondition2(String condition2) {
		this.condition2 = condition2;
	}

	public String getCondition3() {
		return condition3;
	}

	public void setCondition3(String condition3) {
		this.condition3 = condition3;
	}

	public String getCondition4() {
		return condition4;
	}

	public void setCondition4(String condition4) {
		this.condition4 = condition4;
	}

	public String getCondition5() {
		return condition5;
	}

	public void setCondition5(String condition5) {
		this.condition5 = condition5;
	}

	public String getCondition6() {
		return condition6;
	}

	public void setCondition6(String condition6) {
		this.condition6 = condition6;
	}

	public String getCondition7() {
		return condition7;
	}

	public void setCondition7(String condition7) {
		this.condition7 = condition7;
	}

	public String getCondition8() {
		return condition8;
	}

	public void setCondition8(String condition8) {
		this.condition8 = condition8;
	}

	public String getCondition9() {
		return condition9;
	}

	public void setCondition9(String condition9) {
		this.condition9 = condition9;
	}

	public String getCondition10() {
		return condition10;
	}

	public void setCondition10(String condition10) {
		this.condition10 = condition10;
	}

	public String getCondition11() {
		return condition11;
	}

	public void setCondition11(String condition11) {
		this.condition11 = condition11;
	}

	public String getCondition12() {
		return condition12;
	}

	public void setCondition12(String condition12) {
		this.condition12 = condition12;
	}

	public String getCondition13() {
		return condition13;
	}

	public void setCondition13(String condition13) {
		this.condition13 = condition13;
	}

	public String getCondition14() {
		return condition14;
	}

	public void setCondition14(String condition14) {
		this.condition14 = condition14;
	}

	public String getCondition15() {
		return condition15;
	}

	public void setCondition15(String condition15) {
		this.condition15 = condition15;
	}

	public String getCondition16() {
		return condition16;
	}

	public void setCondition16(String condition16) {
		this.condition16 = condition16;
	}

	public String getCondition17() {
		return condition17;
	}

	public void setCondition17(String condition17) {
		this.condition17 = condition17;
	}

	public String getCondition18() {
		return condition18;
	}

	public void setCondition18(String condition18) {
		this.condition18 = condition18;
	}

	public String getCondition19() {
		return condition19;
	}

	public void setCondition19(String condition19) {
		this.condition19 = condition19;
	}

	public String getCondition20() {
		return condition20;
	}

	public void setCondition20(String condition20) {
		this.condition20 = condition20;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getExcludedCity() {
		return excludedCity;
	}

	public void setExcludedCity(String excludedCity) {
		this.excludedCity = excludedCity;
	}

	public String getAboveQuestionObecjt() {
		return aboveQuestionObecjt;
	}

	public void setAboveQuestionObecjt(String aboveQuestionObecjt) {
		this.aboveQuestionObecjt = aboveQuestionObecjt;
	}

	public String getAboveStandardQuestion() {
		return aboveStandardQuestion;
	}

	public void setAboveStandardQuestion(String aboveStandardQuestion) {
		this.aboveStandardQuestion = aboveStandardQuestion;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String getQuestionObject() {
		return questionObject;
	}

	public void setQuestionObject(String questionObject) {
		this.questionObject = questionObject;
	}

	public String getStandardQuestion() {
		return standardQuestion;
	}

	public void setStandardQuestion(String standardQuestion) {
		this.standardQuestion = standardQuestion;
	}

	public String getInteractiveOptions() {
		return interactiveOptions;
	}

	public void setInteractiveOptions(String interactiveOptions) {
		this.interactiveOptions = interactiveOptions;
	}

	public String getRuleResponse() {
		return ruleResponse;
	}

	public void setRuleResponse(String ruleResponse) {
		this.ruleResponse = ruleResponse;
	}

	public String getRuleResponseTemplate() {
		return ruleResponseTemplate;
	}

	public void setRuleResponseTemplate(String ruleResponseTemplate) {
		this.ruleResponseTemplate = ruleResponseTemplate;
	}

	public String getRelationServiceId() {
		return relationServiceId;
	}

	public void setRelationServiceId(String relationServiceId) {
		this.relationServiceId = relationServiceId;
	}

	public String getIsEdit() {
		return isEdit;
	}

	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}

	public String getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(String currentNode) {
		this.currentNode = currentNode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SceneRule [ruleId=");
		builder.append(ruleId);
		builder.append(", scenariosId=");
		builder.append(scenariosId);
		builder.append(", userQuestion=");
		builder.append(userQuestion);
		builder.append(", condition1=");
		builder.append(condition1);
		builder.append(", condition2=");
		builder.append(condition2);
		builder.append(", condition3=");
		builder.append(condition3);
		builder.append(", condition4=");
		builder.append(condition4);
		builder.append(", condition5=");
		builder.append(condition5);
		builder.append(", condition6=");
		builder.append(condition6);
		builder.append(", condition7=");
		builder.append(condition7);
		builder.append(", condition8=");
		builder.append(condition8);
		builder.append(", condition9=");
		builder.append(condition9);
		builder.append(", condition10=");
		builder.append(condition10);
		builder.append(", condition11=");
		builder.append(condition11);
		builder.append(", condition12=");
		builder.append(condition12);
		builder.append(", condition13=");
		builder.append(condition13);
		builder.append(", condition14=");
		builder.append(condition14);
		builder.append(", condition15=");
		builder.append(condition15);
		builder.append(", condition16=");
		builder.append(condition16);
		builder.append(", condition17=");
		builder.append(condition17);
		builder.append(", condition18=");
		builder.append(condition18);
		builder.append(", condition19=");
		builder.append(condition19);
		builder.append(", condition20=");
		builder.append(condition20);
		builder.append(", ruleType=");
		builder.append(ruleType);
		builder.append(", weight=");
		builder.append(weight);
		builder.append(", cityCode=");
		builder.append(cityCode);
		builder.append(", cityName=");
		builder.append(cityName);
		builder.append(", excludedCity=");
		builder.append(excludedCity);
		builder.append(", aboveQuestionObecjt=");
		builder.append(aboveQuestionObecjt);
		builder.append(", aboveStandardQuestion=");
		builder.append(aboveStandardQuestion);
		builder.append(", responseType=");
		builder.append(responseType);
		builder.append(", questionObject=");
		builder.append(questionObject);
		builder.append(", standardQuestion=");
		builder.append(standardQuestion);
		builder.append(", interactiveOptions=");
		builder.append(interactiveOptions);
		builder.append(", ruleResponse=");
		builder.append(ruleResponse);
		builder.append(", ruleResponseTemplate=");
		builder.append(ruleResponseTemplate);
		builder.append(", relationServiceId=");
		builder.append(relationServiceId);
		builder.append(", isEdit=");
		builder.append(isEdit);
		builder.append(", currentNode=");
		builder.append(currentNode);
		builder.append("]");
		return builder.toString();
	}

}
