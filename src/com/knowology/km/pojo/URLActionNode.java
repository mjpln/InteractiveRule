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
	 * 调用方式：HTTP|WEBSERVICE
	 */
	private String invocationWay;

	/**
	 * 命名空间
	 */
	private String namespace;

	/**
	 * 请求方法, HTTP-GET|HTTP-POST
	 */
	private String httpMethod;

	/**
	 * 函数名称, WEBSERVICE调用函数
	 */
	private String functionName;

	/**
	 * 入参
	 */
	private List<URLActionParam> inParams;

	/**
	 * 出参
	 */
	private List<URLActionParam> outParams;

	public URLActionNode() {
	}

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

	public String getInvocationWay() {
		return invocationWay;
	}

	public void setInvocationWay(String invocationWay) {
		this.invocationWay = invocationWay;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public List<URLActionParam> getInParams() {
		return inParams;
	}

	public void setInParams(List<URLActionParam> inParams) {
		this.inParams = inParams;
	}

	public List<URLActionParam> getOutParams() {
		return outParams;
	}

	public void setOutParams(List<URLActionParam> outParams) {
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
		builder.append(", invocationWay=");
		builder.append(invocationWay);
		builder.append(", namespace=");
		builder.append(namespace);
		builder.append(", httpMethod=");
		builder.append(httpMethod);
		builder.append(", functionName=");
		builder.append(functionName);
		builder.append(", inParams=");
		builder.append(inParams);
		builder.append(", outParams=");
		builder.append(outParams);
		builder.append("]");
		return builder.toString();
	}

}
