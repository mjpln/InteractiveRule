package com.knowology.km.pojo;

import java.util.List;

/**
 * 节点数据
 */
public class NodeData {

	/**
	 * 节点标识
	 */
	private String key;
	/**
	 * 节点种类
	 */
	private String category;
	/**
	 * 节点名称
	 */
	private String name;
	/**
	 * 节点文本
	 */
	private String text;
	/**
	 * 是否末梢编码
	 */
	private String endFlag;
	
	/**
	 * 节点顶部连线
	 */
	private List<LinkData> fromLinks;

	/**
	 * 节点底部连线
	 */
	private List<LinkData> toLinks;
	
	public NodeData() {}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getEndFlag() {
		return endFlag;
	}

	public void setEndFlag(String endFlag) {
		this.endFlag = endFlag;
	}

	public List<LinkData> getFromLinks() {
		return fromLinks;
	}

	public void setFromLinks(List<LinkData> fromLinks) {
		this.fromLinks = fromLinks;
	}

	public List<LinkData> getToLinks() {
		return toLinks;
	}

	public void setToLinks(List<LinkData> toLinks) {
		this.toLinks = toLinks;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeData [key=");
		builder.append(key);
		builder.append(", category=");
		builder.append(category);
		builder.append(", name=");
		builder.append(name);
		builder.append(", text=");
		builder.append(text);
		builder.append(", endFlag=");
		builder.append(endFlag);
		builder.append(", fromLinks=");
		builder.append(fromLinks);
		builder.append(", toLinks=");
		builder.append(toLinks);
		builder.append("]");
		return builder.toString();
	}

}
