package com.knowology.km.pojo;

/**
 * 信息收集
 */
public class CollectionNode extends NodeData {

	/**
	 * 参数名称
	 */
	private String collectionParam;

	/**
	 * 收集类型
	 */
	private String collectionType;

	/**
	 * 重复次数
	 */
	private String collectionTimes;

	/**
	 * 反问话术
	 */
	private String collectionWords;

	public CollectionNode() {}

	public CollectionNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getCollectionParam() {
		return collectionParam;
	}

	public void setCollectionParam(String collectionParam) {
		this.collectionParam = collectionParam;
	}

	public String getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	public String getCollectionTimes() {
		return collectionTimes;
	}

	public void setCollectionTimes(String collectionTimes) {
		this.collectionTimes = collectionTimes;
	}

	public String getCollectionWords() {
		return collectionWords;
	}

	public void setCollectionWords(String collectionWords) {
		this.collectionWords = collectionWords;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CollectionNode [collectionParam=");
		builder.append(collectionParam);
		builder.append(", collectionType=");
		builder.append(collectionType);
		builder.append(", collectionTimes=");
		builder.append(collectionTimes);
		builder.append(", collectionWords=");
		builder.append(collectionWords);
		builder.append("]");
		return builder.toString();
	}

}
