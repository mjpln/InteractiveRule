package com.knowology.km.pojo;

public class Link {
	private String from;//连接线起始节点key
	private String to;//连接线结束位置key
	private String text;//连接线上面的值
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
