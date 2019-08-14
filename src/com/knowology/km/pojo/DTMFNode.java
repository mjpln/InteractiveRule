package com.knowology.km.pojo;

/**
 * DTMF按键节点
 */
public class DTMFNode extends NodeData {

	/**
	 * DTMF名称
	 */
	private String dtmfName;

	/**
	 * DTMF别名
	 */
	private String dtmfAlias;

	/**
	 * DTMF话术
	 */
	private String dtmfAnswer;

	/**
	 * 按键类型，0-按键 1-收号
	 */
	private String pressType;

	/**
	 * 收号最小长度
	 */
	private String minLength;

	/**
	 * 收号最大长度
	 */
	private String maxLength;

	/**
	 * 结束按键
	 */
	private String endPressNumber;

	/**
	 * 按键值，逗号分隔
	 */
	private String pressNumbers;

	/**
	 * 尝试次数
	 */
	private String attemptLimit;

	/**
	 * 按键超时时间，单位秒
	 */
	private String pressTimeOut;

	/**
	 * 按键超时话术
	 */
	private String pressTimeOutAnswer;
	
	public DTMFNode() {}
	
	public DTMFNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getDtmfName() {
		return dtmfName;
	}

	public void setDtmfName(String dtmfName) {
		this.dtmfName = dtmfName;
	}

	public String getDtmfAlias() {
		return dtmfAlias;
	}

	public void setDtmfAlias(String dtmfAlias) {
		this.dtmfAlias = dtmfAlias;
	}

	public String getDtmfAnswer() {
		return dtmfAnswer;
	}

	public void setDtmfAnswer(String dtmfAnswer) {
		this.dtmfAnswer = dtmfAnswer;
	}

	public String getPressType() {
		return pressType;
	}

	public void setPressType(String pressType) {
		this.pressType = pressType;
	}

	public String getMinLength() {
		return minLength;
	}

	public void setMinLength(String minLength) {
		this.minLength = minLength;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getEndPressNumber() {
		return endPressNumber;
	}

	public void setEndPressNumber(String endPressNumber) {
		this.endPressNumber = endPressNumber;
	}

	public String getPressNumbers() {
		return pressNumbers;
	}

	public void setPressNumbers(String pressNumbers) {
		this.pressNumbers = pressNumbers;
	}

	public String getAttemptLimit() {
		return attemptLimit;
	}

	public void setAttemptLimit(String attemptLimit) {
		this.attemptLimit = attemptLimit;
	}

	public String getPressTimeOut() {
		return pressTimeOut;
	}

	public void setPressTimeOut(String pressTimeOut) {
		this.pressTimeOut = pressTimeOut;
	}

	public String getPressTimeOutAnswer() {
		return pressTimeOutAnswer;
	}

	public void setPressTimeOutAnswer(String pressTimeOutAnswer) {
		this.pressTimeOutAnswer = pressTimeOutAnswer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DTMFNode [dtmfName=");
		builder.append(dtmfName);
		builder.append(", dtmfAlias=");
		builder.append(dtmfAlias);
		builder.append(", dtmfAnswer=");
		builder.append(dtmfAnswer);
		builder.append(", pressType=");
		builder.append(pressType);
		builder.append(", minLength=");
		builder.append(minLength);
		builder.append(", maxLength=");
		builder.append(maxLength);
		builder.append(", endPressNumber=");
		builder.append(endPressNumber);
		builder.append(", pressNumbers=");
		builder.append(pressNumbers);
		builder.append(", attemptLimit=");
		builder.append(attemptLimit);
		builder.append(", pressTimeOut=");
		builder.append(pressTimeOut);
		builder.append(", pressTimeOutAnswer=");
		builder.append(pressTimeOutAnswer);
		builder.append("]");
		return builder.toString();
	}

}
