package com.knowology.km.pojo;

/**
 * 其他回答
 */
public class OtherResponse {

	/**
	 * 变量名
	 */
	private String otherResponseName;
	/**
	 * 变量值
	 */
	private String otherResponseValue;

	public OtherResponse() {}

	public String getOtherResponseName() {
		return otherResponseName;
	}

	public void setOtherResponseName(String otherResponseName) {
		this.otherResponseName = otherResponseName;
	}

	public String getOtherResponseValue() {
		return otherResponseValue;
	}

	public void setOtherResponseValue(String otherResponseValue) {
		this.otherResponseValue = otherResponseValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OtherResponse [otherResponseName=");
		builder.append(otherResponseName);
		builder.append(", otherResponseValue=");
		builder.append(otherResponseValue);
		builder.append("]");
		return builder.toString();
	}

}
