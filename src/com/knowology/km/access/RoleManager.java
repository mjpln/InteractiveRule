package com.knowology.km.access;

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.Bean.Role;
import com.knowology.Bean.RoleResourceAccessRule;

/**
 * 对角色的DML操作
 * @author knowology
 *
 */
public class RoleManager {
	/**
	 * 查询角色
	 * @param roleName 角色名称
	 * @param customer 角色所属的机构
	 * @param limit 每页限制的条数
	 * @param start 开始的条数
	 * @return Map<String, Result>：<"data/count","Result">数据类型，数据result
	 */
	public static Map<String, Result> selectRole(String roleName, String customer, int limit, int start) {
		Map<String, Result> map = com.knowology.bll.RoleManager.selectRole(roleName, customer, limit, start);
		return map;
	}
	
	/**
	 * 创建角色
	 * @param roleID 角色id
	 * @param roleName 角色名
	 * @param customer 所属组织机构
	 * @return
	 */
	public static int addRole(String roleID, String roleName, String customer) {
		int result = com.knowology.bll.RoleManager.addRole(roleID, roleName, customer);
		return result;
	} 
	
	/**
	 * 更新角色
	 * @param oldRoleID 原角色id
	 * @param newRoleID 新角色id
	 * @param roleName 角色名称
	 * @param customer 所属机构
	 * @return
	 */
	public static int updateRole(String oldRoleID, String newRoleID, String roleName, String customer) {
		int result = com.knowology.bll.RoleManager.updateRole(oldRoleID, newRoleID, roleName, customer);
		return result;
	}
	
	/**
	 * 删除角色
	 * @param roleID 角色id
	 * @return
	 */
	public static int deleteRole(String roleID) {
		int result = com.knowology.bll.RoleManager.deleteRole(roleID);
		return result;
	}
	
	/**
	 * 给角色设置逻辑表达式
	 * @param Map<String, String> 参数集合
	 * @return
	 */
	public static int setRuleToRole(List<Map<String, String>> params) {// 逻辑表达式格式：资源类型=xx&资源ID=xx,xx,xx...&属性类型=xx,xx,xx@属性类型=xx,xx&操作=xx,xx,xx&是否操作子业务=xxx; xx存放的都是编码
		int result = com.knowology.bll.RoleManager.setRuleToRole(params);
		return result;
	}
	
	/**
	 * 根据角色ID创建一个角色对象
	 * @param roleID
	 * @return
	 */
	public static Role constructRole(String roleID) {
		return com.knowology.bll.RoleManager.constructRole(roleID);
	}
	
	/**
	 * 查询包含指定资源操作权限的角色的规则
	 * @param roleList 角色集合
	 * @param resourceType 资源类型
	 * @param operateType 操作权限
	 * @return
	 */  
	public static List<RoleResourceAccessRule> getRolesRuleByOperate(List<Role> roleList, String resourceType,String operateType) {
		return com.knowology.bll.RoleManager.getRolesRuleByOperate(roleList, resourceType, operateType);
	}
	
	/**
	 * 构造角色树
	 * @param workerID 用户id
	 * @param customer 角色所属机构
	 * @return Map<String,Result>:<已有角色/所有角色,rs>
	 */
	public static Map<String,Result> constructRoleTree(String workerID, String customer) {
		return com.knowology.bll.RoleManager.constructRoleTree(workerID, customer);
	}
	
	/**
	 * 获得角色的规则信息
	 * @param roleID 角色id
	 * @param resourceType 资源类型
	 * @param limit 每页显示条数
	 * @param start 开始条数
	 * @return
	 */
	public static Map<String,Result> selectResourceAttr(String roleID, String resourceType, int limit, int start) {
		Map<String,Result> map = com.knowology.bll.RoleManager.selectResourceAttr(roleID, resourceType, limit, start);
		return map;
	}
	
	/**
	 *  解析逻辑表达式获得可操作的角色资源权限
	 * @param logic 逻辑表达式
	 * @return
	 */
	public static RoleResourceAccessRule getRoleResourceByLogic(String logic) {// 逻辑表达式格式：资源类型=xx&资源名=xx,xx,xx...&属性类型=xx,xx,xx@属性类型=xx,xx&操作权限=xx,xx,xx&是否关联子业务=xxx; 
		return com.knowology.bll.RoleManager.getRoleResourceByLogic(logic);
	}
}
