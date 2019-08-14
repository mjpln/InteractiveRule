package com.knowology.km.pojo;

import java.util.List;

/**
 * 外呼TTS节点
 */
public class TTSNode extends NodeData {

	/**
	 * TTS
	 */
	private String tts;

	/**
	 * 录音文件
	 */
	private String code;

	/**
	 * 动作，sms-发送短信
	 */
	private String action;

	/**
	 * 动作参数
	 */
	private String actionParams;

	/**
	 * 其他
	 */
	private List<OtherResponse> otherResponses;

	public TTSNode() {
	}

	public TTSNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getTts() {
		return tts;
	}

	public void setTts(String tts) {
		this.tts = tts;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getActionParams() {
		return actionParams;
	}

	public void setActionParams(String actionParams) {
		this.actionParams = actionParams;
	}

	public List<OtherResponse> getOtherResponses() {
		return otherResponses;
	}

	public void setOtherResponses(List<OtherResponse> otherResponses) {
		this.otherResponses = otherResponses;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TTSNode [tts=");
		builder.append(tts);
		builder.append(", code=");
		builder.append(code);
		builder.append(", action=");
		builder.append(action);
		builder.append(", actionParams=");
		builder.append(actionParams);
		builder.append(", otherResponses=");
		builder.append(otherResponses);
		builder.append("]");
		return builder.toString();
	}

}
