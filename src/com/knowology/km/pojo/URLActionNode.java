package com.knowology.km.pojo;

import java.util.List;

/**
 * 动作组件
 */
public class URLActionNode extends NodeData {

	/**
	 * 节点名称
	 */
	private String actionName;
	
	/**
	 * 接口名称
	 */
	private String interfaceName;

	/**
	 * 接口地址
	 */
	private String actionUrl;

	/**
	 * 请求方法, Http-get/Http-post
	 */
	private String actionMethod;

	/**
	 * 入参
	 */
	private List<URLActionInParam> inParams;

	/**
	 * 出参
	 */
	private List<URLActionOutParam> outParams;
	
	public URLActionNode() {}

	public URLActionNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getActionUrl() {
		return actionUrl;
	}

	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}

	public String getActionMethod() {
		return actionMethod;
	}

	public void setActionMethod(String actionMethod) {
		this.actionMethod = actionMethod;
	}

	public List<URLActionInParam> getInParams() {
		return inParams;
	}

	public void setInParams(List<URLActionInParam> inParams) {
		this.inParams = inParams;
	}

	public List<URLActionOutParam> getOutParams() {
		return outParams;
	}

	public void setOutParams(List<URLActionOutParam> outParams) {
		this.outParams = outParams;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("URLActionNode [actionName=");
		builder.append(actionName);
		builder.append(", interfaceName=");
		builder.append(interfaceName);
		builder.append(", actionUrl=");
		builder.append(actionUrl);
		builder.append(", actionMethod=");
		builder.append(actionMethod);
		builder.append(", inParams=");
		builder.append(inParams);
		builder.append(", outParams=");
		builder.append(outParams);
		builder.append("]");
		return builder.toString();
	}

}
