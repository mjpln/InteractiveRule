package com.knowology.km.pojo;

/**
 * 动作组件参数
 */
public class URLActionParam {

	/**
	 * 入参名称
	 */
	private String paramName;

	/**
	 * 入参值
	 */
	private String paramValue;
	
	public URLActionParam() {}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("URLActionInParam [paramName=");
		builder.append(paramName);
		builder.append(", paramValue=");
		builder.append(paramValue);
		builder.append("]");
		return builder.toString();
	}

}
