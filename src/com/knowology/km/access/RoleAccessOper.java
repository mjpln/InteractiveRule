package com.knowology.km.access;

import java.util.List;

import com.knowology.Bean.Role;
import com.knowology.Bean.RoleResourceAccessRule;
import com.str.NewEquals;

/**
 * 角色权限操作
 * @author knowology
 */
public class RoleAccessOper {
	/**
	 * 用户登陆后是否有查看资源的权限
	 * @param resourceType
	 * @param roleList
	 * @return
	 */
	public static boolean isAccessOnServiceTreeCheck(String resourceType, List<Role> roleList) {
		// 遍历所有的角色
		for (Role role : roleList) {
			if (role.getBelongCom().equals("全行业") || role.getRoleName().endsWith("管理员")) {
				return true;
			}

			// 获取角色的资源权限
			List<RoleResourceAccessRule> ruleList = role.getRoleResourcePrivileges();
			for (RoleResourceAccessRule ra : ruleList) {
				if(ra.getResourceType().equals(resourceType)) {// 判断该规则是否有operateType操作权限				
					if (ra.getOperateLimit().contains("S")) {// 如果该规则有operateType操作权限
						return true;
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断是否有对某个资源的操作权限
	 * @param resourceType 资源类型 
	 * @param resourceID 资源id
	 * @param operateType 页面执行的操作
	 * @param roleList 角色集合
	 */
	public static boolean isAccessByResourceID(String resourceType, String resourceID,String operateType,List<Role> roleList) {
		// 遍历所有的角色
		for (Role role : roleList) {
			if (role.getBelongCom().equals("全行业") || role.getRoleName().endsWith("管理员")) {
				return true;
			} 
			// 获取角色的资源权限
			List<RoleResourceAccessRule> ruleList = role.getRoleResourcePrivileges();
			for (RoleResourceAccessRule ra : ruleList) {
				if(ra.getResourceType().equals(resourceType)) {// 判断该规则是否有operateType操作权限				
					if (ra.getOperateLimit().contains(operateType)) {// 如果该规则有operateType操作权限
						// 如果是新增根业务操作 2015/09/17 添加
						if (operateType.equals("A") && NewEquals.equals(resourceID,"0")) {
							return true;
						}
						
						// 通过属性判断是否对该资源有权限
						if(ResourceAccessOper.isAccess(resourceType, resourceID, ra.getAccessResourceMap())){
							return true;
						}
						// 获得是否关联子业务
						String isRelateChild = ra.getIsRelateChild();
						if (isRelateChild.equals("Y") && resourceType.equals("service")) {// 关联子业务
							// 资源id
							List<String> list = ResourceAccessOper.getResourceIDByName(ra.getResourceNames().toArray(),ra.getResourceType());
							list = ResourceAccessOper.getChildService(list.toArray());
							// 判断是否包含在用户指定的资源id中
							if (list.contains(resourceID)) {// 如果用户指定的资源中包含该
								return true;
							}
						} else {// 不关联子业务
							// 资源id
							List<String> list = ResourceAccessOper.getResourceIDByName(ra.getResourceNames().toArray(),ra.getResourceType());
							// 判断是否包含在用户指定的资源id中
							if (list.contains(resourceID)) {// 如果用户指定的资源中包含该
								return true;
							}
						}
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
		}
		return false;
	}
	
	/**
	 * 针对个性化业务和主题，根据角色的操作类型判断是否有操作权限
	 * @param resourceType 资源类型
	 * @param operateType 操作类型
	 * @param roleList 角色集合
	 * @return
	 */
	public static boolean isAccessForCommonResource(String resourceType,String operateType,List<Role> roleList) {
		// 遍历所有的角色
		for (Role role : roleList) {
			if (role.getBelongCom().equals("全行业") || role.getRoleName().endsWith("管理员")) {
				return true;
			} 
			// 获取角色的资源权限
			List<RoleResourceAccessRule> ruleList = role.getRoleResourcePrivileges();
			for (RoleResourceAccessRule ra : ruleList) {//ghj update  not sure
				if(NewEquals.equals(ra.getResourceType(),resourceType)) {// 判断该规则是否有operateType操作权限				
					if (ra.getOperateLimit().contains(operateType)) {// 如果该规则有operateType操作权限
						return true;
					} else {
						continue;
					}
				} else {
					continue;
				}
			}
		}
		return false;
	}
}