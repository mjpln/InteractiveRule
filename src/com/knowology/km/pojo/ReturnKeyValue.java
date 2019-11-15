package com.knowology.km.pojo;

/**
 * 词模返回值
 */
public class ReturnKeyValue {

	private String returnKey;
	
	private String returnValue;
	
	public ReturnKeyValue() {}

	public String getReturnKey() {
		return returnKey;
	}

	public void setReturnKey(String returnKey) {
		this.returnKey = returnKey;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReturnKeyValue [returnKey=");
		builder.append(returnKey);
		builder.append(", returnValue=");
		builder.append(returnValue);
		builder.append("]");
		return builder.toString();
	}
}
