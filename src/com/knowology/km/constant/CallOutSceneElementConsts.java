package com.knowology.km.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * 外呼场景要素名称
 */
public class CallOutSceneElementConsts {
	
	public static final String ABOVE_NODE_ELEMENT_NAME = "上文:节点名";
	public static final String ROBOT_ID_ELEMENT_NAME = "robotID";
	public static final String CUSTOMER_ANSWER_ELEMENT_NAME = "用户回答";
	public static final String RECOGNITION_STATUS_ELEMENT_NAME = "理解状态";
	public static final String NOT_RECOGNITION_TIMES_ELEMENT_NAME = "连续未理解次数";
	public static final String DIFFERENTIATED_ELEMENT_NAME = "区分节点";
	public static final String COLLECTION_TIMES_ELEMENT_NAME = "信息收集重复次数";
	public static final String COLLECTION_STATUS_ELEMENT_NAME = "信息收集状态";
	public static final String DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME = "是否获取到按键值";
	public static final String CONDITION_VALUE_ELEMENT_NAME = "条件值";
	
	public static Set<String> getAllSceneElements() {
		Set<String> sceneElements = new HashSet<String>();
		sceneElements.add(ABOVE_NODE_ELEMENT_NAME);
		sceneElements.add(ROBOT_ID_ELEMENT_NAME);
		sceneElements.add(CUSTOMER_ANSWER_ELEMENT_NAME);
		sceneElements.add(RECOGNITION_STATUS_ELEMENT_NAME);
		sceneElements.add(NOT_RECOGNITION_TIMES_ELEMENT_NAME);
		sceneElements.add(DIFFERENTIATED_ELEMENT_NAME);
		sceneElements.add(COLLECTION_TIMES_ELEMENT_NAME);
		sceneElements.add(COLLECTION_STATUS_ELEMENT_NAME);
		sceneElements.add(DTMF_IS_GET_PRESS_NUMBER_ELEMENT_NAME);
		sceneElements.add(CONDITION_VALUE_ELEMENT_NAME);
		return sceneElements;
	}

}
