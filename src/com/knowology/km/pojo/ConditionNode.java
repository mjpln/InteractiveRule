package com.knowology.km.pojo;

import java.util.List;

/**
 * 条件组件
 */
public class ConditionNode extends NodeData {

	/**
	 * 组件名称
	 */
	private String conditionNodeName;
	
	/**
	 * 条件集合
	 */
	private List<ConditionInfo> conditions;

	public ConditionNode() {}

	public ConditionNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getConditionNodeName() {
		return conditionNodeName;
	}

	public void setConditionNodeName(String conditionNodeName) {
		this.conditionNodeName = conditionNodeName;
	}

	public List<ConditionInfo> getConditions() {
		return conditions;
	}

	public void setConditions(List<ConditionInfo> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConditionNode [conditionNodeName=");
		builder.append(conditionNodeName);
		builder.append(", conditions=");
		builder.append(conditions);
		builder.append("]");
		return builder.toString();
	}

}
