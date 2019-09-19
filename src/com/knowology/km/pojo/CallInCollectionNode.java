package com.knowology.km.pojo;

/**
 * 呼入信息收集
 *
 */
public class CallInCollectionNode extends CollectionNode {

	/**
	 * 关联场景要素
	 */
	private String collectionElement;
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
	
	public CallInCollectionNode() {}

	public CallInCollectionNode(NodeData nodeData) {
		super(nodeData);
	}

	public String getCollectionElement() {
		return collectionElement;
	}

	public void setCollectionElement(String collectionElement) {
		this.collectionElement = collectionElement;
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
		builder.append("CallInCollectionNode [collectionElement=");
		builder.append(collectionElement);
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
