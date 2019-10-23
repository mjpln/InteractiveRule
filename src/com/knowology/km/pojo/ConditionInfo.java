package com.knowology.km.pojo;

import java.util.ArrayList;

public class ConditionInfo {
	
	/**
	 * 条件名称
	 */
	String conditionName;
	
	/**
	 * AND条件集合
	 */
	ArrayList<AndCondition> andConditions;
	
	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public ArrayList<AndCondition> getAndConditions() {
		return andConditions;
	}

	public void setAndConditions(ArrayList<AndCondition> andConditions) {
		this.andConditions = andConditions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConditionInfo [conditionName=");
		builder.append(conditionName);
		builder.append(", andConditions=");
		builder.append(andConditions);
		builder.append("]");
		return builder.toString();
	}
	
}
