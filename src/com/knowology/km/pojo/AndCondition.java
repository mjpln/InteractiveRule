package com.knowology.km.pojo;

/**
 * AND条件
 * 
 * 如：参数1 等于 参数2
 */
public class AndCondition {

	/**
	 * 参数1
	 */
	private String paramName;

	/**
	 * 比较关系，等于|大于|小于|不等于|大于等于|小于等于
	 */
	private String paramRelation;

	/**
	 * 参数2类型，String|Integer|Variable
	 */
	private String paramType;

	/**
	 * 参数2值
	 */
	private String paramValue;

	/**
	 * 是否上浮
	 */
	private String updownType;

	/**
	 * 上浮值
	 */
	private String updownRation;

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamRelation() {
		return paramRelation;
	}

	public void setParamRelation(String paramRelation) {
		this.paramRelation = paramRelation;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getUpdownType() {
		return updownType;
	}

	public void setUpdownType(String updownType) {
		this.updownType = updownType;
	}

	public String getUpdownRation() {
		return updownRation;
	}

	public void setUpdownRation(String updownRation) {
		this.updownRation = updownRation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AndCondition [paramName=");
		builder.append(paramName);
		builder.append(", paramRelation=");
		builder.append(paramRelation);
		builder.append(", paramType=");
		builder.append(paramType);
		builder.append(", paramValue=");
		builder.append(paramValue);
		builder.append(", updownType=");
		builder.append(updownType);
		builder.append(", updownRation=");
		builder.append(updownRation);
		builder.append("]");
		return builder.toString();
	}

}
