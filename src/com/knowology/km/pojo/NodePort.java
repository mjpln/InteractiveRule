package com.knowology.km.pojo;

/**
 * 节点端口
 */
public class NodePort {

	/**
	 * 端口数据
	 */
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodePort [text=");
		builder.append(text);
		builder.append("]");
		return builder.toString();
	}
	
}
