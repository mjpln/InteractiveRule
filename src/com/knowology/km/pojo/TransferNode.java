package com.knowology.km.pojo;

/**
 * 转人工组件
 */
public class TransferNode extends NodeData {

	/**
	 * 转人工名称
	 */
	private String transferName;

	/**
	 * 转人工号码
	 */
	private String transferNumber;

	public TransferNode() {

	}

	public TransferNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getTransferName() {
		return transferName;
	}

	public void setTransferName(String transferName) {
		this.transferName = transferName;
	}

	public String getTransferNumber() {
		return transferNumber;
	}

	public void setTransferNumber(String transferNumber) {
		this.transferNumber = transferNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransferNode [transferName=");
		builder.append(transferName);
		builder.append(", transferNumber=");
		builder.append(transferNumber);
		builder.append("]");
		return builder.toString();
	}

}
