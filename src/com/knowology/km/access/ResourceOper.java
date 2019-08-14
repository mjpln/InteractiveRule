package com.knowology.km.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.Bean.Role;
import com.knowology.Bean.RoleResourceAccessRule;
import com.knowology.Bean.User;
import com.knowology.bll.AccessDao;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;
/**
 * 对资源的DML操作
 * @author xsheng
 * 部分方法移动到ServiceDAO
 */
public class ResourceOper {
	/**
	 * 获得最大的业务根节点信息
	 * @return
	 */
	public static Result getRootBrand() {
		Result rs = null;
		rs = CommonLibServiceDAO.getRootBrand();
		return rs;
	}
	
	/**
	 *@description  通过行业商家组织获得业务根 
	 *@return 
	 *@returnType Result 
	 */
	public static Result getServiceRoot(String industry,String organization,String application){
		Result rs = CommonLibServiceDAO.getServiceRoot(industry, organization, application);
		return rs;
	}
	
	/**
	 * 获得最大根业务对应的子业务
	 * @param roleList 角色集合
	 * @param serviceString 业务根集合
	 * @return
	 */
	public static Result getChildByRoot(List<Role> roleList, String serviceString) {
		Result rs = CommonLibServiceDAO.getChildByRoot(roleList, serviceString);
		return rs;
	}
	
	/**
	 * 根据业务名搜索业务
	 * @param user
	 * @param serviceString
	 * @param service
	 * @return
	 */
	public static Result getServiceForDDL(List<Role> roleList,String serviceString,String service) {
		// 返回值
		Result rs = null;
		rs = CommonLibServiceDAO.getServiceForDDL(roleList, serviceString, service);
		return rs;
	}
	
	/**
	 * 根据用户录入的业务信息重新加载业务树
	 * @param roleList 角色集合
	 * @param parentID 父业务id
	 * @param brand 业务根
	 * @param service 业务名
	 * @return
	 */
	public static Result getServiceForTree(List<Role> roleList,String parentID, String brand, String service) {
		Result rs = null;
		rs = CommonLibServiceDAO.getServiceForTree(roleList, parentID, brand, service);
		return rs;
	}
	
	/**
	 * 通过业务名获得业务
	 * @param service
	 * @return
	 */
	public static Result getServiceIDByService(String service, String brand) {
		Result rs = null;
		rs = CommonLibServiceDAO.getServiceIDByService(service, brand);
		return rs;
	}
	
	/**
	 * 通过根业务获得业务
	 * @param service
	 * @return
	 */
	public static Result getServiceIDByBrand(String service, String brand) {
		Result rs = null;
		rs = CommonLibServiceDAO.getServiceIDByBrand(service, brand);
		return rs;
	}
	
	/**
	 * 根据父业务id获得子业务
	 * @param user 当前登录用户
	 * @param serviceID 父业务id
	 * @param brand 根业务名
	 * @return
	 */
	public static Result getChildServiceByParentID(String serviceID, List<Role> roleList, String brand) {
		Result rs = CommonLibServiceDAO.getChildServiceByParentID(serviceID, roleList, brand);
		return rs;
	}
	
	/**
	 * 根据父业务id获得子业务
	 * @param user 当前登录用户
	 * @param serviceID 父业务id
	 * @param brand 根业务名
	 * @param childService 子业务名
	 * @return
	 */
	public static Result getChildServiceByParentID(String serviceID, List<Role> roleList, String brand, String childService) {
		// 返回值
		Result rs = null;
		rs = CommonLibServiceDAO.getChildServiceByParentID(serviceID, roleList, brand, childService);
		return rs;
	}
	
	/**
	 * 判断父业务下是否有子业务
	 * @param serviceID 父业务id
	 * @return
	 */
	public static Result getChildServiceByParentID(String serviceID) {
		Result rs = null;
		rs = CommonLibServiceDAO.getChildServiceByParentID(serviceID);
		return rs;
	}
	
	/**
	 * 根据子业务id查找父业务名
	 * @param serviceID 子业务id
	 * @return 
	 */
	public static Result getParentNameByChildID(String serviceID) {
		Result rs = null;
		rs = CommonLibServiceDAO.getParentNameByChildID(serviceID);
		return rs;
	}
	
	/**
	 * 判断父业务下是否有子业务
	 * 用于添加业务时，判断父业务下是否有子业务，上面两个同名方法是用于显示时使用
	 * @param serviceID 父业务id
	 * @return
	 */
	public static Result getSameCalssServiceByserviceID(String serviceID) {
		Result rs = null;
		rs = CommonLibServiceDAO.getSameCalssServiceByserviceID(serviceID);
		return rs;
	}
	
	/**
	 * 根据业务id查询该业务对应的摘要
	 * @param serviceID 资源id
	 * @param roleList 用户的角色集合
	 * @param rows 每页能看到的行
	 * @param page 页码
	 * @return
	 */
	public static Map<String, Result> getKbdataByServiceID(String serviceID, List<Role> roleList, int rows, int page) {
		// 返回值
		Map<String, Result> resultMap = new HashMap<String, Result>();
		// 获得该操作的所有资源规则
		List<RoleResourceAccessRule> ruleList = RoleManager.getRolesRuleByOperate(roleList, "kbdata", "S");
		if (!ruleList.isEmpty()) {// 该权限有对应的操作规则
			// 该操作类型用户能够操作的资源
			List<String> resourceIDList = new ArrayList<String>();
			for (RoleResourceAccessRule rule : ruleList) {
				// 根据属性得到能够操作的所有资源id
				List<String> serviceIDByAttr = ResourceAccessOper.searchResIDByAttrs(rule.getAccessResourceMap(), "service");
				if (!serviceIDByAttr.isEmpty()) {// 根据属性查询出相关资源
					resourceIDList.addAll(serviceIDByAttr);
				}
				// 压入用户指定的资源ID
				List<String> resourceNames = rule.getResourceNames();
				if (!resourceNames.isEmpty()) {// 用户指定资源名
					List<String> serviceIDByServiceName = ResourceAccessOper.getResourceIDByName(resourceNames.toArray(), "service");
					if (!serviceIDByServiceName.isEmpty()) {
						resourceIDList.addAll(serviceIDByServiceName);
					}
				}
				// 判断是否关联子业务
				if (rule.getIsRelateChild().equals("Y")) {// 关联子业务
					resourceIDList = ResourceAccessOper.getChildService(resourceIDList.toArray());
				}
			}
			// 去重
			resourceIDList = new ArrayList<String>(new HashSet<String>(resourceIDList));
			// 统计个数
			String sql_count = "select count(*) from kbdata where serviceID=" + serviceID + " and kbdataid in ("+org.apache.commons.lang.StringUtils.join(resourceIDList.toArray(),",")+")";
			try {
				Result rs = Database.executeQuery(sql_count);
				resultMap.put("count",rs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 具体内容
			String sql_data = "select * from kbdata where serviceID=" + serviceID + " and kbdataid in ("+org.apache.commons.lang.StringUtils.join(resourceIDList.toArray(),",")+")";
			sql_data = "select t.*,rownum rn from (" + sql_data + ") t where rownum<"+(rows+(page-1)*rows+1);
			sql_data = "select * from (" + sql_data +") where rn>"+(page-1)*rows;
			try {
				Result rs = Database.executeQuery(sql_data);
				resultMap.put("data",rs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
	
	/**
	 * 根据业务id删除业务，以及业务对应的摘要，问题，词模。。。以及资源属性表(Resourceacessmanager)对应的数据
	 * @param serviceID
	 * @return
	 */
	public static int deleteAllServiceByID(List<Map<String,List<String>>> params) {
		// 操作作用的行数
		int count = 0;
		count = CommonLibServiceDAO.deleteAllServiceByID(params);
		return count;
	}
	
	/**
	 * 根据业务id删除根业务，以及业务对应的摘要，问题，词模。。。以及资源属性表(Resourceacessmanager)对应的数据
	 * @param serviceID
	 * @return
	 */
	public static int deleteAllServiceByID(List<Map<String,List<String>>> params, String customer) {
		// 操作作用的行数
		int count = 0;
		count = CommonLibServiceDAO.deleteAllServiceByID(params, customer);
		return count;
	}
	
	
	/**
	 * 在知识库下添加根业务
	 * @param params
	 * @return
	 */
	public static int insertRootService(List<List<Map<String,List<String>>>> params,String customer) {
		int count = 0;
		count = CommonLibServiceDAO.insertRootService(params, customer);
		return count;
	}
	
	/**
	 * 在知识库下添加子业务
	 * @param params
	 * @return
	 */
	public static int insertChildService(List<List<Map<String,List<String>>>> params) {
		int count = 0;
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		count = CommonLibServiceDAO.insertChildService(params, user);
		return count;
	}
	
	/**
	 * 修改根业务
	 * @param params
	 * @return
	 */
	public static int updateService(List<Map<String,List<String>>> params,String customer) {
		int count = 0;
		count = CommonLibServiceDAO.updateService(params, customer);
		return count;
	}
	
	/**
	 * 修改子业务
	 * @param params
	 * @return
	 */
	public static int updateService(List<Map<String,List<String>>> params) {
		int count = 0;
		count = CommonLibServiceDAO.updateService(params);
		return count;
	}
	
	/**
	 * 添加摘要
	 * @param 
	 * @param abs 摘要集合
	 * @param serviceid 业务id
	 * @param topic 主题
	 * @param service 业务名
	 * @param brand 品牌
	 * @param isInsertIntoKbdataid_brand
	 * @return
	 */
	public static boolean addKbdata(String userip,String userid,String username,List<String> abs,String serviceid, String topic, String service,String brand,String isInsertIntoKbdataid_brand,String serviceType) {
		// 返回值
		boolean result;
		result = CommonLibKbDataDAO.addAbstract(userip,userid, username,abs,serviceid, topic, service, brand, isInsertIntoKbdataid_brand,serviceType);
		return result;
	}
	
	/**
	 * 通用主题下添加摘要
	 * @param userip用户IP
	 * @param userid用户ID
	 * @param username用户名
	 * @param abs参数摘要集合
	 * @param serviceid业务id
	 * @param topic参数主题
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @return 新增是否成功
	 */
	public static boolean addPublicAbstract(String userip, String userid,
			String username,List<String> abs,
			String serviceid, String topic, String service,
			String brand,String serviceType){
		boolean result; 
		result = CommonLibKbDataDAO.addPublicAbstract(userip, userid, username, abs, serviceid, topic, service, brand,serviceType);
		return result;
	}
	
	
	/**
	 * 分页查询满足条件的摘要数据源
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param key参数搜索摘要的关键字
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @param topic参数主题
	 * @param cityid参数id
	 * @param serviceid参数业务id
	 * @return 数据源
	 */
	public static Result getAbstracts(String start, String limit, String key,String service, String brand, String topic, String cityid,String serviceid,String industryOrganizationApplication){
		Result rs = CommonLibKbDataDAO.getAbstracts(start, limit, key, service, brand, topic, cityid, serviceid, industryOrganizationApplication);
		return rs;
	}
	
	/**
	 * @param key参数搜索摘要的关键字
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @param topic参数主题
	 * @param cityid参数地市id
	 * @param serviceid参数业务id
	 * @return 数量
	 */
	public static int getAbstractCount(String key, String service,String brand, String topic, String cityid, String serviceid){
		int rs = CommonLibKbDataDAO.getAbstractCount(key, service, brand, topic, cityid, serviceid);
		return rs;
	}
	
	/**
	 * 判断摘要是否存在
	 * 
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @param topic参数主题
	 * @param _abstract参数摘要
	 * @param t参数中间对象主要存放摘要集合
	 * @param serviceids参数业务ids
	 * @return 是否存在
	 */
	public static List<String> isHave(String service, String brand,String topic, String _abstract,  String serviceid){
		List<String> rs = CommonLibKbDataDAO.isHave(service, brand, topic, _abstract, serviceid);
		return rs;
	}
	
	/**
	 * 更新摘要
	 * @param userip用户IP
	 * @param userid用户ID
	 * @param username用户名 
	 * @param newabstract参数新摘要
	 * @param oldabstract参数旧摘要
	 * @param serviceid业务id
	 * @param topic参数主题
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @param container参数类型
	 * @param kbdataid参数摘要id
	 * @return 更新是否成功
	 */
	public static boolean updateAbstract(String userip, String userid,String username ,String newabstract,
			String oldabstract, String serviceid,
			String topic, String service, String brand, String container,
			String kbdataid){
		boolean result; 
		result = CommonLibKbDataDAO.updateAbstract(userip, userid, username, newabstract, oldabstract, serviceid, topic, service, brand, container, kbdataid);
		return result;
	}
	
	/**
	 * 删除摘要
	 * @param userip用户IP
	 * @param userid用户ID
	 * @param username用户名   
	 * @param abs参数摘要名称
	 * @param servceid 业务id
	 * @param topic参数主题
	 * @param service参数业务名称
	 * @param brand参数品牌
	 * @param kbdataids参数摘要ids
	 * @param container参数类型
	 * @return 删除是否成功
	 */
	public static boolean deleteAbstract(String userip, String userid,String username ,String abs,
			String serviceid, String topic, String service,
			String brand, String kbdataid, String container){
		boolean result; 
		result = CommonLibKbDataDAO.deleteAbstract(userip, userid, username, abs, serviceid, topic, service, brand, kbdataid, container);
		return result;
	}
	
	
	/**
	 * 获取资源以及资源的属性信息
	 * @param resourceType 资源类型
	 * @param resourceID 资源ID
	 * @param customer 所属机构
	 * @param limit 每页显示的个数
	 * @param start 开始的条数
	 * @return
	 */
	public static Map<String,Result> selectResourceAttr(String resourceType, String resourceID, String customer, List<String> columnList, int limit, int start) {
		Map<String,Result> map = AccessDao.selectResourceAttr(resourceType, resourceID, customer, columnList, limit, start);
		return map;
	}
		
	/**
	 * 更新资源的属性值
	 * @param columnArray 要更新的列
	 * @param columnValueArray 列对应的值
	 * @param serviceIDs 业务id集合
	 * @param fatherID 根业务ID
	 * @return
	 */
	public static int updateResourceAttrInfo(String[] columnArray, String[] columnValueArray, List<String> serviceIDs, String fatherID) {
		int result = AccessDao.updateResourceAttrInfo(columnArray, columnValueArray, serviceIDs, fatherID);
		return result;
	}
	
	/**
	 * 更新资源的属性值
	 * @param columns 要更新的列
	 * @param columnValue 列对应的值
	 * @param resourceType 资源类型
	 * @return
	 */
	public static int updateResourceAttrInfo(String columns, String columnValue, String resourceType) {
		int result = AccessDao.updateResourceAttrInfo(columns, columnValue, resourceType);
		return result;
	}
	
	/**
	 * 根据资源类型和资源id查询资源所对应的属性
	 * @param resourceType 资源类型
	 * @param resourceID 资源id
	 * @return
	 */
	public static Map<String,Object> getResourceAttrs(String resourceType,String resourceID) {
		Map<String,Object> map = AccessDao.getResourceAttrs(resourceType,resourceID);
		return map;
	}
}
