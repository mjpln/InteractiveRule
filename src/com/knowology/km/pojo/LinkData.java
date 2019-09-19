package com.knowology.km.pojo;

/**
 * 连线数据
 */
public class LinkData {

	/**
	 * 源节点
	 */
	private NodeData fromNode;
	/**
	 * 目标节点
	 */
	private NodeData toNode;
	/**
	 * 源端口
	 */
	private NodePort fromPort;
	/**
	 * 目标端口
	 */
	private NodePort toPort;
	
	public LinkData() {}

	public NodeData getFromNode() {
		return fromNode;
	}

	public void setFromNode(NodeData fromNode) {
		this.fromNode = fromNode;
	}

	public NodeData getToNode() {
		return toNode;
	}

	public void setToNode(NodeData toNode) {
		this.toNode = toNode;
	}

	public NodePort getFromPort() {
		return fromPort;
	}

	public void setFromPort(NodePort fromPort) {
		this.fromPort = fromPort;
	}

	public NodePort getToPort() {
		return toPort;
	}

	public void setToPort(NodePort toPort) {
		this.toPort = toPort;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LinkData [fromNode=");
		builder.append(fromNode);
		builder.append(", toNode=");
		builder.append(toNode);
		builder.append(", fromPort=");
		builder.append(fromPort);
		builder.append(", toPort=");
		builder.append(toPort);
		builder.append("]");
		return builder.toString();
	}

}
