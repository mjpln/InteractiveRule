package com.knowology.km.pojo;

import java.util.HashSet;
import java.util.List;

public class Node {
	private String key;// 节点key
	private String category;// 节点类别
	private String text;// 节点数据显示的值
	private String response;// 响应
	private String responsetype;// 相应类别
	private String ruleresponsetemplate;// 回复内容模板
	private HashSet<Node> childs;// 子节点
	private List<Link> links;// 子连接线
	private String combKey;

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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponsetype() {
		return responsetype;
	}

	public void setResponsetype(String responsetype) {
		this.responsetype = responsetype;
	}

	public String getRuleresponsetemplate() {
		return ruleresponsetemplate;
	}

	public void setRuleresponsetemplate(String ruleresponsetemplate) {
		this.ruleresponsetemplate = ruleresponsetemplate;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public HashSet<Node> getChilds() {
		return childs;
	}

	public void setChilds(HashSet<Node> childs) {
		this.childs = childs;
	}

	public String getCombKey() {
		return combKey;
	}

	public void setCombKey(String combKey) {
		this.combKey = combKey;
	}
}