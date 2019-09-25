package com.knowology.km.enums;

public enum CollectionTypeEnum {

	ELEMENT_COLLECTION("elementCollection", "要素采集"),
	USER_INFO_COLLECTION("userInfoCollection", "用户信息采集");

	private CollectionTypeEnum(String value, String desc) {
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
	
	public static CollectionTypeEnum getEnum(String value) {
		CollectionTypeEnum[] enums = CollectionTypeEnum.values();
		for(CollectionTypeEnum collectionTypeEnum : enums) {
			if(collectionTypeEnum.getValue().equals(value)) {
				return collectionTypeEnum;
			}
		}
		return null;
	}

}
