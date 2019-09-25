package com.knowology.km.pojo;

/**
 * 信息收集
 */
public class CollectionNode extends NodeData {

	/**
	 * 参数名称
	 */
	private String collectionParam;

	/**
	 * 采集类型
	 */
	private String collectionType;

	/**
	 * 重复次数
	 */
	private String collectionTimes;

	/**
	 * 反问话术
	 */
	private String collectionWords;

	/**
	 * 关联要素
	 */
	private String collectionElement;

	/**
	 * 关联意图
	 */
	private String collectionIntention;

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

	public CollectionNode() {
	}

	public CollectionNode(NodeData nodeData) {
		super.setKey(nodeData.getKey());
		super.setCategory(nodeData.getCategory());
		super.setName(nodeData.getName());
		super.setText(nodeData.getText());
		super.setFromLinks(nodeData.getFromLinks());
		super.setToLinks(nodeData.getToLinks());
	}

	public String getCollectionParam() {
		return collectionParam;
	}

	public void setCollectionParam(String collectionParam) {
		this.collectionParam = collectionParam;
	}

	public String getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	public String getCollectionTimes() {
		return collectionTimes;
	}

	public void setCollectionTimes(String collectionTimes) {
		this.collectionTimes = collectionTimes;
	}

	public String getCollectionWords() {
		return collectionWords;
	}

	public void setCollectionWords(String collectionWords) {
		this.collectionWords = collectionWords;
	}

	public String getCollectionElement() {
		return collectionElement;
	}

	public void setCollectionElement(String collectionElement) {
		this.collectionElement = collectionElement;
	}

	public String getCollectionIntention() {
		return collectionIntention;
	}

	public void setCollectionIntention(String collectionIntention) {
		this.collectionIntention = collectionIntention;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CollectionNode [collectionParam=");
		builder.append(collectionParam);
		builder.append(", collectionType=");
		builder.append(collectionType);
		builder.append(", collectionTimes=");
		builder.append(collectionTimes);
		builder.append(", collectionWords=");
		builder.append(collectionWords);
		builder.append(", collectionElement=");
		builder.append(collectionElement);
		builder.append(", collectionIntention=");
		builder.append(collectionIntention);
		builder.append(", interactiveType=");
		builder.append(interactiveType);
		builder.append(", menuStartWords=");
		builder.append(menuStartWords);
		builder.append(", menuOptions=");
		builder.append(menuOptions);
		builder.append(", menuEndWords=");
		builder.append(menuEndWords);
		builder.append("]");
		return builder.toString();
	}

}
