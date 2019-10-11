package com.knowology.km.enums;

public enum ComparisionRelationEnum {

	EQUAL("等于", ""),
	LESS_THAN("小于", "<"),
	GREATER_THAN("大于", ">"),
	NOT_EQUAL("不等于", "!="),
	LESS_THAN_OR_EQUAL("小于等于", "<="),
	GREATER_THAN_OR_EQUAL("大于等于", ">=");

	private ComparisionRelationEnum(String desc, String value) {
		this.value = value;
		this.desc = desc;
	}

	private String value;
	private String desc;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public static ComparisionRelationEnum getEnum(String desc) {
		ComparisionRelationEnum[] enums = ComparisionRelationEnum.values();
		for(ComparisionRelationEnum comparisionRelationEnum : enums) {
			if(comparisionRelationEnum.getDesc().equals(desc)) {
				return comparisionRelationEnum;
			}
		}
		return null;
	}

}
