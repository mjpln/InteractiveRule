package com.knowology.km.access;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.Bean.Role;
import com.knowology.Bean.RoleResourceAccessRule;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.UserManagerDAO;
import com.knowology.km.util.GetSession;

/**
 * 对用户的DML操作
 * @author xsheng
 */
public class UserManager {
	/**
	 * 查询用户信息
	 * @param workerName 页面查询项：用户名称
	 * @param department 页面查询项： 部门
	 * @param num 工号
	 * @param customer 用户的所属机构
	 * @param limit 每页限制的条数
	 * @param start 开始条数
	 * @return Map<String, Result>：<"data/count","Result">数据类型，数据result
	 */
	public static Map<String, Result> selectUser(String workerName, String department, String num, String customer,int limit,int start) {
		// 调用mysql的迁移类型
		Map<String, Result> resultMap = UserManagerDAO.selectUser(workerName, department, num, customer, limit, start);
		return resultMap;
	}
	
	/**
	 *  创建用户
	 * @param m_request
	 * @return
	 */
	public static int addUser(Map<String,String> params) {
		// 调用mysql的迁移类型
		int result = UserManagerDAO.addUser(params);
		return result;
	} 
	
	/**
	 * 根据用户id删除用户
	 * @param workerID 用户id
	 * @return
	 */
	public static int deleteUser(String workerID) {
		// 调用mysql的迁移类型
		int result = UserManagerDAO.deleteUser(workerID);
		return result;
	}
	
	/**
	 * 根据id查询对应的用户
	 * @param userID
	 * @return
	 */
	public static Result selectUserByID(String userID) {
		// 调用mysql的迁移类型
		Result rs = UserManagerDAO.selectUserByID(userID);
		return rs;
	}
	
	/**
	 * 修改用户密码
	 * @param workerID 用户id
	 * @param pwd 密码
	 * @param newPwd 新密码
	 * @return
	 */
	public static int updateUserPwd(String workerID, String oldPwd, String newPwd) {
		// 调用mysql的迁移类型
		int result = UserManagerDAO.updateUserPwd(workerID, oldPwd, newPwd);
		return result;
	}
	
	/**
	 * 更新用户
	 * @param params
	 * @return
	 */
	public static int updateUser(Map<String,String> params) {
		// 调用mysql的迁移类型
		int result = UserManagerDAO.updateUser(params);
		return result;
	}
	
	/**
	 * 给用户配置角色
	 * @param m_request
	 * @return
	 */
	public static int setRoleToWorker(String workerID, String roleIDs) {
		// 调用mysql的迁移类型
		int result = UserManagerDAO.setRoleToWorker(workerID, roleIDs);
		return result;
	}
	
	/**
	 * 构造登录用户对象
	 * @param userID 用户id
	 * @param userName 用户名
	 * @param password 密码
	 * @param customer 所属组织机构
	 * @return
	 */
	public static User constructLoginUser(String userIP,String userID, String userName, String password, String industryOrganizationApplication,String customer) {
		// 返回值
		User user = new User();
		// 获得用户所对应的角色
		List<Role> roleList = new ArrayList<Role>();
		// 通过用户id 获得用户的所有角色ID
		String sql = "select roleID from Workerrolerel where workerID='"+userID+"'";
		Result rs = UserManagerDAO.constructLoginUser(sql);
		try {
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String roleID = rs.getRows()[i].get("roleID") != null ? rs.getRows()[i].get("roleID").toString() : "";
					Role role = RoleManager.constructRole(roleID);
					roleList.add(role);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 设置user的属性
		user.setUserID(userID);
		user.setUserName(userName);
		user.setPassword(password);
		user.setIndustryOrganizationApplication(industryOrganizationApplication);
		user.setCustomer(customer);
		user.setRoleList(roleList);
		user.setUserIP(userIP);
		//获得角色加载菜单项
		Map<String,String> menuNameMap = new HashMap<String,String>();
        for(int i =0;i<roleList.size();i++){
        	Role r = roleList.get(i);
        	String menuName = r.getMenuName();
        	String lmn = user.getLoadMenuName();
        	if("".equals(lmn)||lmn==null ){
        		String loadMenuName = r.getLoadMenuName();
        		user.setLoadMenuName(loadMenuName);
        	}
            if(!"".equals(menuName)){
             String arry[] = 	menuName.split("\\|");
             for(int k=0;k<arry.length;k++){
            	 menuNameMap.put(arry[k], "");
             }
            }
        	List<RoleResourceAccessRule> rp = r.getRoleResourcePrivileges();
        	if(rp.size()>0){
            for(int aa =0;aa<rp.size();aa++){
            	Map<String,String> map = rp.get(aa).getAccessResourceMap();
            	//System.out.println(map);
            }
        	
        	}
        }
        String containsMenuName ="";
        for (String  key : menuNameMap.keySet()) {  
        	containsMenuName += "'"+key+"',"; 
        } 
        if(!"".equals(containsMenuName)){
        	containsMenuName = containsMenuName + "'系统管理','词库知识','通用问法复用','问答训练','知识管理','帮助'";
        }
        user.setMenuName(containsMenuName);
		
		return user;
	}
	
	
	/**
	 * 根据用户ID构造用户对象
	 * @param userID 用户id
	 * @return
	 */
	public static User constructLoginUser(String userID,String inOrgApp) {
		// 返回值
		User user = new User();
		Result rs = UserOperResource.getUserInfo(userID);
		user.setUserID(userID);
		user.setUserName(rs.getRows()[0].get("name").toString());
		
		InetAddress addr;
		String ip = " ";
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();
		} catch (UnknownHostException e) {
			
		}
		user.setUserIP(ip);
		
		rs = UserOperResource.getUserRoleInfo(userID);
		 String industryOrganizationApplication="";
		if (rs != null && rs.getRows().length != 0) {
			// 角色 customer
			String customer = rs.getRows()[0].get("customer") == null ? "" : rs.getRows()[0].get("customer").toString();
			Result customerRs = CommonLibMetafieldmappingDAO.getConfigValue("场景登录全行业配置",inOrgApp);
			Result customerRs02 = CommonLibMetafieldmappingDAO.getConfigValue("交互场景非全行业全国账号配置",inOrgApp);
			
			if(customerRs != null && customerRs.getRowCount() > 0){
				for (int i = 0 ; i < customerRs.getRowCount() ; i++){
					if(userID.equals(customerRs.getRows()[i].get("name").toString())){
						customer = "全行业";
						break;
					}
				}
			}
			
			if(customerRs02 != null && customerRs02.getRowCount() > 0){
				for (int i = 0 ; i < customerRs02.getRowCount() ; i++){
					if(userID.equals(customerRs02.getRows()[i].get("name").toString())){
						customer = "全行业";
						break;
					}
				}
			}
			
			if (!"全行业".equals(customer)) {
				industryOrganizationApplication = customer;

				user.setIndustryOrganizationApplication(industryOrganizationApplication);
				user.setCustomer(industryOrganizationApplication);
			} else {
				user.setIndustryOrganizationApplication(inOrgApp);
				user.setCustomer(customer);

			}

		} 
		return user;
	}
	
	/**
	 *@description 根据用户ID获取Customer
	 *@param userID
	 *@return 
	 *@returnType String 
	 */
	public static String  getCustomer(String userID ){
		Result rs = UserOperResource.getUserRoleInfo(userID);
		String customer="";
		if (rs != null && rs.getRows().length != 0) {
			// 角色 customer
			customer = rs.getRows()[0].get("customer") == null ? ""
					: rs.getRows()[0].get("customer").toString();
			}
		return customer;
	}
	
}
