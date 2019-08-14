package com.knowology.km.access;

import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.bll.AccessDao;

/**
 * 属性管理类
 * @author xsheng
 */
public class AttributeManage {
	/**
	 * 查询所有的属性
	 * @param attrName 属性名称
	 * @param customer 所属机构
	 * @param limit 每页显示的个数
	 * @param start 开始条目
	 * @return
	 */
	public static Map<String,Result> selectAttr(String attrName, String customer, int limit, int start) {
		Map<String, Result> map = AccessDao.selectAttr(attrName, customer, limit, start);
		return map;
	}
	
	/**
	 * 新增属性
	 * @param resourceType 资源类型
	 * @param attrName 属性名
	 * @param dataType 数据类型
	 * @param shape 展现形式
	 * @param customer 所属机构
	 * @param columnNum 对应列
	 * @return
	 */
	public static int addAttr(String resourceType, String attrName, String dataType, String shape, String customer, String columnNum) {
		int count = AccessDao.addAttr(resourceType, attrName, dataType, shape, customer, columnNum);
		return count;
	}
	
	/**
	 * 构造属性对应列下拉框
	 * @param customer
	 * @return
	 */
	public static Result constructAttrCombobox(String customer) {
		Result rs = AccessDao.constructAttrCombobox(customer);
		return rs;
	}
	
	/**
	 * 修改属性值
	 * @param attrID 属性id
	 * @param attrName 属性名
	 * @return
	 */
	public static int updateAttr(String attrID, String attrName) {
		int count = AccessDao.updateAttr(attrID, attrName);
		return count;
	}
	
	/**
	 * 删除属性
	 * @param attrID  属性ID
	 * @return
	 */
	public static int deleteAttr(String attrID) {
		int count = AccessDao.deleteAttr(attrID);
		return count;
	}
	
	/**
	 * 构造属性值树 ztree 类型
	 * @param attrID 属性ID
	 * @param treeColumn 对应AttrValueInfo表的字段
	 * @param id 父节点id
	 * @param attrValue ResourceAcessManager的列对应的列值
	 * @return
	 */
	public static Result constructAttrTree(String attrID, String treeColumn, String id) {
		Result result = AccessDao.constructAttrTree_new(attrID, treeColumn, id);
		return result;
	}
	
	/**
	 * 给属性添加属性值
	 * @param attrID 属性ID
	 * @param fatherID 父id
	 * @param newAttrName 属性名 
	 * @param treeValue 对应AttrValueInfo表的字段
	 * @return
	 */
	public static int addAttrInfo(String attrID, String fatherID, String newAttrName, String treeValue) {
		int count = AccessDao.addAttrInfo_new(attrID, fatherID, newAttrName, treeValue);
		return count;
	}
	
	/**
	 * 修改属性的属性值
	 * @param attrID 属性id
	 * @param coding 属性值编码
	 * @param newAttrName 新的属性名
	 * @param treeValue 更新的列名
	 * @return
	 */
	public static int updateAttrInfo(String attrID, String coding, String newAttrName, String treeValue) {
		int count = AccessDao.updateAttrInfo(attrID, coding, newAttrName, treeValue);
		return count;
	}
	
	/**
	 * 删除属性值
	 * @param attrID 属性id
	 * @param coding 属性值编码
	 * @return
	 */
	public static int deleteAttrInfo(String attrID, String coding) {
		int count = AccessDao.deleteAttrInfo(attrID, coding);
		return count;
	}
	
	/**
	 * 构造资源配置表的列
	 * @param resourceType 资源类型
	 * @param customer 所属机构
	 * @return
	 */
	public static Result constructTableColumn(String resourceType, String customer) {
		Result rs = AccessDao.constructTableColumn(resourceType, customer);
		return rs;
	}
	
	/**
	 * 构造属性值下拉框
	 * @param id 属性id
	 * @param columnName 对应的列名
	 * @return
	 */
	public static Result constructAttrInfoCombobox(String id) {
		Result rs = AccessDao.constructAttrInfoCombobox(id);
		return rs;
	}
	
	/**
	 * 构造四层结构下拉框
	 * @return
	 */
	public static Result constructCombobox() {
		Result rs = AccessDao.constructCombobox();
		return rs;
	}
	
	/**
	 * 根据ResourceAcessManager属性对应的列名和属性的编码找到属性的名称
	 * @param column ResourceAcessManager属性对应的列名
	 * @param coding 属性的名称
	 * @return
	 */
	public static String getAttrNameForAttrToResource(String column, String coding) {
		String str = AccessDao.getAttrNameForAttrToResource(column, coding);
		return str;
	}
	
	public static String getAttrNameForAttrToResource(String name, String coding, String resourceType) {
		String str = AccessDao.getAttrNameForAttrToResource(name, coding, resourceType);
		return str;
	}
}
