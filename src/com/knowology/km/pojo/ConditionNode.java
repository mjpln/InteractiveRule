package com.knowology.km.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件组件
 */
public class ConditionNode extends NodeData {

	/**
	 * 条件组件名称
	 */
	private String conditionName;

	/**
	 * 条件集合
	 */
	private List<ArrayList<AndCondition>> conditions;

	public ConditionNode() {
	}

	public ConditionNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public List<ArrayList<AndCondition>> getConditions() {
		return conditions;
	}

	public void setConditions(List<ArrayList<AndCondition>> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConditionNode [conditionName=");
		builder.append(conditionName);
		builder.append(", conditions=");
		builder.append(conditions);
		builder.append("]");
		return builder.toString();
	}

}
