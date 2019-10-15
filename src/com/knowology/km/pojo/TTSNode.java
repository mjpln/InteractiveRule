package com.knowology.km.pojo;

import java.util.List;

/**
 * 外呼TTS节点
 */
public class TTSNode extends NodeData {

	/**
	 * 话术内容
	 */
	private String wordsContent;

	/**
	 * 录音文件
	 */
	private String code;
	
	/**
	 * 交互类型, 菜单询问|系统反问
	 */
	private String interactiveType;

	/**
	 * 菜单询问开始语
	 */
	private String menuStartWords;

	/**
	 * 菜单询问菜单选项
	 */
	private String menuOptions;

	/**
	 * 菜单询问结束语
	 */
	private String menuEndWords;

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
	
	public TTSNode() {}

	public TTSNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getWordsContent() {
		return wordsContent;
	}

	public void setWordsContent(String wordsContent) {
		this.wordsContent = wordsContent;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getInteractiveType() {
		return interactiveType;
	}

	public void setInteractiveType(String interactiveType) {
		this.interactiveType = interactiveType;
	}

	public String getMenuStartWords() {
		return menuStartWords;
	}

	public void setMenuStartWords(String menuStartWords) {
		this.menuStartWords = menuStartWords;
	}

	public String getMenuOptions() {
		return menuOptions;
	}

	public void setMenuOptions(String menuOptions) {
		this.menuOptions = menuOptions;
	}

	public String getMenuEndWords() {
		return menuEndWords;
	}

	public void setMenuEndWords(String menuEndWords) {
		this.menuEndWords = menuEndWords;
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
		builder.append("TTSNode [wordsContent=");
		builder.append(wordsContent);
		builder.append(", code=");
		builder.append(code);
		builder.append(", interactiveType=");
		builder.append(interactiveType);
		builder.append(", menuStartWords=");
		builder.append(menuStartWords);
		builder.append(", menuOptions=");
		builder.append(menuOptions);
		builder.append(", menuEndWords=");
		builder.append(menuEndWords);
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
