package com.knowology.km.access;

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.knowology.Bean.Role;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibDayUserNumDAO;
import com.knowology.bll.CommonLibFaqDAO;
import com.knowology.bll.CommonLibIndustryApplicationToServicesDAO;
import com.knowology.bll.CommonLibMenuDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibOperationlogDAO;
import com.knowology.bll.CommonLibQueryElementDAO;
import com.knowology.bll.CommonLibQuestionDAO;
import com.knowology.bll.CommonLibRegressTestDAO;
import com.knowology.bll.CommonLibReportDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibStandardkeyDAO;
import com.knowology.bll.CommonLibStandardvalueDAO;
import com.knowology.bll.CommonLibSynonymDAO;
import com.knowology.bll.CommonLibTopicDAO;
import com.knowology.bll.CommonLibUserDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.bll.CommonLibWordclassDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.km.util.GetSession;

/**
 * 用户操作类
 * @author xsheng
 */
public class UserOperResource {
	/**
	 * 用户登陆后是否有查看资源的权限
	 * @param roleList
	 * @return
	 */
	public static boolean isAccessOnServiceTreeCheck(User user) {
		boolean flag = RoleAccessOper.isAccessOnServiceTreeCheck("service", user.getRoleList());
		return flag;
	}
	
	/**
	 * 根据资源id和操作类型查是否有操作该资源的权限
	 * @param user 登陆用户
	 * @param resourceType 资源类型
	 * @param resourceID 资源id
	 * @param operateType 操作类型
	 * @param brand 根业务名
	 */
	public static boolean isAccessByResourceID(User user, String resourceType,String resourceID, String operateType, String brand) {
		if ("个性化业务".equals(brand) || brand.endsWith("主题")) {// 当为个性化业务时，不做权限判断
			return RoleAccessOper.isAccessForCommonResource(resourceType, operateType, user.getRoleList());
		}
		if(brand.equals("知识库")) {// 管理员才能对‘知识库’下一级进行操作
			for (Role role : user.getRoleList()) {
				if(role.getRoleName().endsWith("管理员")) {
					return true;
				}
			}
			return false;
		}
		boolean flag = RoleAccessOper.isAccessByResourceID(resourceType, resourceID, operateType, user.getRoleList());
		return flag;
	}
	
	/**
	 * 获得最大的业务根节点
	 * @return
	 */
	public static Result getRootBrand() {
		Result rs = ResourceOper.getRootBrand();
		return rs;
	}
	
	/**
	 *@description  通过行业商家组织获得业务根 
	 *@return 
	 *@returnType Result 
	 */
	public static Result getServiceRoot(String industry,String organization,String application){
		Result rs = ResourceOper.getServiceRoot(industry, organization, application);
		return rs;
	}
	/**
	 * 获得最大根业务对应的子业务
	 * @param serviceString 根业务
	 * @return
	 */
	public static Result getChildByRoot(User user ,String serviceString) {
		Result rs = ResourceOper.getChildByRoot(user.getRoleList(),serviceString);
		return rs;
	}
	
	/**
	 * 根据业务名搜索业务
	 * @param user 用户信息
	 * @param serviceString 业务根集合
	 * @param service 用户录入的业务名信息
	 * @return
	 */
	public static Result getServiceForDDL(User user,String serviceString,String service) {
		Result rs = null;
		rs = ResourceOper.getServiceForDDL(user.getRoleList(),serviceString,service);
		return rs;
	}
	
	/**
	 * 根据业务名查询业务树
	 * @param user 用户
	 * @param parentID 父业务id
	 * @param brand 业务根
	 * @param service 业务名
	 * @return
	 */
	public static Result getServiceForTree(User user,String parentID, String brand, String service) {
		Result rs = null;
		rs = ResourceOper.getServiceForTree(user.getRoleList(),parentID, brand, service);
		return rs;
	}
	
	/**
	 * 通过业务名获得业务
	 * @param service
	 * @return
	 */
	public static Result getServiceIDByService(String service,String brand) {
		Result rs = null;
		rs = ResourceOper.getServiceIDByService(service, brand);
		return rs;
	}
	
	/**
	 * 通过根业务获得业务
	 * @param service
	 * @return
	 */
	public static Result getServiceIDByBrand(String service,String brand) {
		Result rs = null;
		rs = ResourceOper.getServiceIDByBrand(service, brand);
		return rs;
	}
	
	/**
	 * 根据父业务id查询子业务
	 * @param user 当前登录用户
	 * @param serviceID 父业务id
	 * @param brand 根业务名
	 * @return
	 */
	public static Result getChildServiceByParentID(User user, String serviceID, String brand) {
		Result rs = null;
		rs = ResourceOper.getChildServiceByParentID(serviceID, user.getRoleList(), brand);
		return rs;
	}
	
	/**
	 * 根据父业务id查询子业务
	 * @param user 当前登录用户
	 * @param serviceID 父业务id
	 * @param brand 根业务名
	 * @param childService 子业务名
	 * @return
	 */
	public static Result getChildServiceByParentID(User user, String serviceID, String brand, String childService) {
		Result rs = null;
		rs = ResourceOper.getChildServiceByParentID(serviceID, user.getRoleList(), brand, childService);
		return rs;
	}
	
	/**
	 * 根据业务名称集合获取对应的业务id集合
	 * 
	 * @param serviceList参数业务名称集合
	 * @return 业务id集合
	 */
	public  static Result getServiceIDByServiceList(
			List<String> serviceList){
		Result rs = CommonLibServiceDAO.getServiceIDByServiceList(serviceList);
		return rs;
	}
	
	/**
	 * 判断父业务下是否有子业务
	 * 用于添加业务时，判断父业务下是否有子业务，上面两个同名方法是用于显示时使用
	 * @param serviceID 父业务id
	 * @return
	 */
	public static Result getChildServiceByParentID(String serviceID) {
		Result rs = null;
		rs = ResourceOper.getChildServiceByParentID(serviceID);
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
		rs = ResourceOper.getSameCalssServiceByserviceID(serviceID);
		return rs;
	}
	
	/**
	 * 根据业务id查看该业务下能够查看的摘要
	 * @param user 用户
	 * @param serviceID 业务id
	 * @param rows 每页能看到的行
	 * @param page 页码
	 * @return
	 */
	public static Map<String, Result> getKbdataByServiceID(User user, String serviceID, int rows, int page) {
		Map<String, Result> resultMap = null;
		resultMap = ResourceOper.getKbdataByServiceID(serviceID, user.getRoleList(), rows, page);
		return resultMap;
	}
	
	/**
	 * 删除业务，以及业务对应的摘要，问题，词模。。。以及资源属性表(Resourceacessmanager)对应的数据
	 * @param params
	 * @return
	 */
	public static int deleteAllServiceByID(List<Map<String,List<String>>> params) {
		int count = 0;
		count = ResourceOper.deleteAllServiceByID(params);
		return count;
	}
	
	/**
	 * 直接删除根业务，以及业务对应的摘要，问题，词模。。。以及资源属性表(Resourceacessmanager)对应的数据
	 * @param params
	 * @return
	 */
	public static int deleteAllServiceByID(List<Map<String,List<String>>> params,String customer) {
		int count = 0;
		count = ResourceOper.deleteAllServiceByID(params, customer);
		return count;
	}
	
	/**
	 * 根据子业务id查找父业务名
	 * @param serviceID 子业务id
	 * @return 
	 */
	public static Result getParentNameByChildID(String serviceID) {
		Result rs = null;
		rs = ResourceOper.getParentNameByChildID(serviceID);
		return rs;
	}
	
	/**
	 * 在知识库下添加根业务
	 * @param params
	 * @param customer 用户登陆后压入的四层机构信息
	 * @return
	 */
	public static int insertRootService(List<List<Map<String,List<String>>>> params,String customer) {
		int count = 0;
		count = ResourceOper.insertRootService(params,customer);
		return count;
	}
	
	/**
	 * 在知识库下添加子业务
	 * @param params
	 * @return
	 */
	public static int insertChildService(List<List<Map<String,List<String>>>> params) {
		int count = 0;
		count = ResourceOper.insertChildService(params);
		return count;
	}
	/**
	 * 修改根业务
	 * @param params
	 * @param customer 用户登陆后压入的四层机构信息
	 * @return
	 */
	public static int updateService(List<Map<String,List<String>>> params,String customer) {
		int count = 0;
		count = ResourceOper.updateService(params,customer);
		return count;
	}
	
	/**
	 * 修改子业务
	 * @param params
	 * @return
	 */
	public static int updateService(List<Map<String,List<String>>> params) {
		int count = 0;
		count = ResourceOper.updateService(params);
		return count;
	}	
	
	/**
	 * 添加摘要
	 * @param userip用户IP
	 * @param userid用户ID
	 * @param username用户名
	 * @param abs 摘要集合
	 * @param serviceid 业务id
	 * @param topic 主题
	 * @param service 业务名
	 * @param brand 品牌
	 * @param isInsertIntoKbdataid_brand
	 * @return
	 */
	public static boolean addKbdata
	(String userip,String userid,String username,List<String> abs,String serviceid, String topic, String service,String brand,String isInsertIntoKbdataid_brand,String serviceType) {
		boolean result = ResourceOper.addKbdata(userip, userid, username, abs, serviceid, topic, service, brand, isInsertIntoKbdataid_brand,serviceType);
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
		result = ResourceOper.addPublicAbstract(userip, userid, username, abs, serviceid, topic, service, brand,serviceType);
		return result;
	}

	/**
	 * 查询当前业务下的所有的主题
	 * 
	 * @param erviceid参数业务id
	 * @return 数据源
	 */
	public static Result GetTopicByServiceid(String serviceid){
		Result rs = CommonLibTopicDAO.GetTopicByServiceid(serviceid);
		return rs;
		
	}

	/**
	 *@description  查询配置表当前行业下的所有的主题
	 *@param servicetype 行业标识
	 *@return 
	 *@returnType Result 
	 */
	public static Result GetTopicConfig(String servicetype){
		Result rs = CommonLibTopicDAO.GetTopicConfig(servicetype);
		return rs;
	}
	
	/**
	 *描述：@description  获取菜单结果集
	 *参数：@return
	 *返回值类型：@returnType Result
	 */
	public static Result getMenuinfo(String containsMenuName){
		Result rs = CommonLibMenuDAO.getMenuinfo(containsMenuName);
		return rs;
	} 
	
	/**
	 *描述：@description  通过员工ID 查询员工基本信息
	 *参数：@param workerid
	 *参数：@return
	 *返回值类型：@returnType Result
	 */
	public static Result getUserInfo(String workerid){
		Result rs = CommonLibUserDAO.getUserInfo(workerid);
		return rs;
	}
	/**
	 *描述：@description 通过员工ID 查询员工角色信息
	 *参数：@param workerid
	 *参数：@return
	 *返回值类型：@returnType Result
	 */
	public static Result getUserRoleInfo(String workerid){
		Result rs = CommonLibUserDAO.getUserRoleInfo(workerid);
		return rs;
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
		Result rs = ResourceOper.getAbstracts(start, limit, key, service, brand, topic, cityid, serviceid, industryOrganizationApplication);
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
		int rs = ResourceOper.getAbstractCount(key, service, brand, topic, cityid, serviceid);
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
		List<String> rs = ResourceOper.isHave(service, brand, topic, _abstract, serviceid);
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
		result = ResourceOper.updateAbstract(userip, userid, username, newabstract, oldabstract, serviceid, topic, service, brand, container, kbdataid);
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
		result = ResourceOper.deleteAbstract(userip, userid, username, abs, serviceid, topic, service, brand, kbdataid, container);
		return result;
	}
	
	
	/**
	 * 根据条件分页查询词类
	 * 
	 * @param wordclass 参数词类
	 * @param wordclassprecise  是否精确查找
	 * @param wordclasstype 行业归属
	 * @param container  词类所属
	 * @return int 数据量 
	 */
	public static int getWordclassCount(User user,String wordclass, Boolean wordclassprecise,
			String wordclasstype,String container) {
		int i = CommonLibWordclassDAO.getCount(user,wordclass, wordclassprecise, wordclasstype, container);
		return i;
	}
	
	/**
	 * 根据条件分页查询词类
	 * 
	 * @param wordclass参数词类
	 * @param wordclassprecise 是否精确查找
	 * @param wordclasstype 行业归属
	 * @param container 词类所属
	 * @param start参数起始条数
	 * @param limit参数每页条数
	 * @return Result 数据源
	 */
	public static JSONArray getWordclass(User user,String wordclass, Boolean wordclassprecise,
			String wordclasstype,String container, int start, int limit) {
		JSONArray rs = CommonLibWordclassDAO.select(user,wordclass, wordclassprecise,wordclasstype,container, start, limit);
		return rs;
	}
	
	/** 
	 *@description  插入词类
	 *@param user 用户登录信息
	 *@param lstWordclass 词类集合
	 *@param wordcalsstype 词类归属
	 *@param container 
	 *@return 
	 *@returnType int 
	 */
	public static int insertWordcalss(User user, List<String> lstWordclass,String wordclasstype,String container,String serviceType){
	 int rs = CommonLibWordclassDAO.insert(user, lstWordclass, wordclasstype, container,serviceType);
	 return rs;
	}


	
	/**
	 * 根据资源类型和资源id查询资源所对应的属性
	 * @param resourceType 资源类型
	 * @param resourceID 资源id
	 * @return Map<String,Object>:<属性名，属性值> 例如<地市，1001,1002,1003>
	 */
	public static Map<String,Object> getResourceAttrs(String resourceType,String resourceID) {
		Map<String,Object> map = ResourceOper.getResourceAttrs(resourceType, resourceID);
		return map;
	}
	
	/**
	 * 更新词类的具体操作
	 * @param user 用户登录信息
	 * @param id参数id
	 * @param oldvalue参数原有词类
	 * @param newvalue参数新的词类
	 * @param oldwordclasstype  旧词类归属
	 * @param newwordclasstype  新词类归属
	 * @return int
	 */
	public static int updateWordcalss(User user,String id, String oldvalue, String newvalue,String oldwordclasstype,String newwordclasstype,String container){
		int  rs  = CommonLibWordclassDAO.update(user, id, oldvalue, newvalue, oldwordclasstype, newwordclasstype, container);
		return rs;
	}
	
	/**
	 * 删除词类
	 * @param user 用户登录信息
	 * @param wordclassid参数词类id
	 * @param wordclass参数词类
	 * @param wordclass 词类归属
	 * @param container 词库类型
	 * @return int
	 */
	public static int  deleteWordclass(User user, String wordclassid, String wordclass,String wordclasstype,String container){
		int  rs  =  CommonLibWordclassDAO.delete(user, wordclassid, wordclass, wordclasstype,container);
		return rs;
	}
	
	/**
	 * 词条添加操作
	 * @param curwordclassid参数词类id
	 * @param curwordclass参数词类名称
	 * @param worditemList参数词条集合
	 * @param curwordclasstype 词类归属
	 * @param type参数词条类型
	 * @param container 词库类型
	 * @return int
	 */
	public static int insertWord(User user, String curwordclassid, 
			String curwordclass, String curwordclasstype,
			List<String> worditemList, String type, String container) {
		int rs = CommonLibWordDAO.insertByLoginInfo(user, curwordclassid, curwordclass, curwordclasstype, worditemList, type, container);
	    return rs;
	}
	
	/**
	 * 删除词条
	 * 
	 * @param user
	 *            用户信息
	 * @param wordid参数词条id
	 * @param curwordclass参数词类名称
	 * @param curwordclasstype
	 *            词类归属
	 * @param worditem参数词条名称
	 * @param container
	 *            词库类型
	 * @return int
	 */
	public static int deleteWord(User user, String wordid, String curwordclass,
			String curwordclasstype, String worditem, String container){
		int rs = CommonLibWordDAO.delete(user, wordid, curwordclass, curwordclasstype, worditem, container);
		return rs;
		
	}
	
	
	/**
	 *@description  查询词条city
	 *@param wordclass 词类名称
	 *@param wordid 词条ID
	 *@return 
	 *@returnType Result 
	 */
	public static Result selectWordCity(String wordclass,String wordid){
		Result rs = CommonLibWordDAO.selectWordCity(wordclass,wordid);
		return rs;
		
	}
	/**
	 *@description  更新词条city
	 *@param wordclass 词类
	 *@param wordid 词条ID
	 *@param cityNme 城市名称
	 *@param cityCode 城市代码
	 *@return 
	 *@returnType int 
	 */
	public static int updateWordCity(String wordclass ,String wordid,String cityNme,String cityCode){
		return  CommonLibWordDAO.updateWordCity(wordclass,wordid,cityNme,cityCode);
	
	}
	
	/**
	 * 修改词条操作
	 * @param oldworditem参数原有词条
	 * @param newworditem参数新的词条
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数词条id
	 * @param curwordclass参数词类名
	 * @param curwordclasstype 参数词类归属 
	 * @param container 参数词库类型
	 * @return int 修改返回的结果 
	 */
	public static int updateWord(User user,String oldworditem, String newworditem,
			String oldtype, String newtype, String wordid,String wordclassid,String curwordclass,String curwordclasstype,String container ){
		int c = CommonLibWordDAO.update(user, oldworditem, newworditem, oldtype, newtype, wordid, wordclassid, curwordclass, curwordclasstype, container);
	    return c;
	}
	
	/**
	 * 判断词类是否重复
	 * 
	 * @param oldwordclass参数旧词类
	 * @param newWordclass参数新词类
	 * @return 是否重复 Boolean
	 */
	public static Boolean isExistWordclass(String oldWordclass ,String newWordclass){
		boolean rs = CommonLibWordclassDAO.exist(oldWordclass, newWordclass);
		return rs;
	}
	
	/**
	 * 判断词条是否重复
	 * 
	 * @param curwordclassid参数当前词类id
	 * @param worditem参数词条名称
	 * @param newtype参数词条类型
	 * @return 是否重复 Boolean
	 */
	public static Boolean isExistWord(String curwordclassid, String worditem,
			String newtype) {
		boolean rs  = CommonLibWordDAO.exist(curwordclassid, worditem, newtype);
		return rs;
	}
	
    /**
     *@description 判断词条下是否存在别名
     *@param wordid 词条ID
     *@return 
     *@returnType Boolean 
     */
    public static Boolean isHaveOtherName(String wordid){
    	boolean rs = CommonLibWordDAO.isHaveOtherName(wordid);
    	return rs;
    }
    
    /**
	 * 获取词条数量

	 * @param worditem参数词条
	 * @param worditemprecise参数是否精确
	 * @param iscurrentwordclass参数是否当前词类
	 * @param worditemtype参数词条类型
	 * @param curwordclass参数当前词类名称
	 * @return Result
	 */
	public static int getWordCount(String worditem,
			Boolean worditemprecise, Boolean iscurrentwordclass,
			String worditemtype, String curwordclass,String contatiner){
		int  rs = CommonLibWordDAO.getWordCount(worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, contatiner);
		return rs;
	}
	
	/**
	 * 带分页的查询满足条件的词条信息
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param worditem参数词条
	 * @param worditemprecise参数是否精确
	 * @param iscurrentwordclass参数是否当前词类
	 * @param worditemtype参数词条类型
	 * @param curwordclass参数当前词类名称
	 * @return Result
	 */
	public static Result selectWord(int start, int limit, String worditem,
			Boolean worditemprecise, Boolean iscurrentwordclass,
			String worditemtype, String curwordclass, String contatiner) {
	Result rs = CommonLibWordDAO.select(start, limit, worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, contatiner);
	return rs;
	}
	
	/**
	 * 带分页的查询满足条件的别名名称
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param synonym参数别名名称
	 * @param isprecise参数是否精确查询
	 * @param iscurrentworditem参数是否当前词条
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return Result
	 */
	public static Result selectSynonym(int start, int limit, String synonym,
			Boolean isprecise, Boolean iscurrentworditem, String type,
			String curworditem, String curwordclass,String wordclassType){
	Result rs = CommonLibSynonymDAO.select(start, limit, synonym, isprecise, iscurrentworditem, type, curworditem, curwordclass,wordclassType);
	return rs;
	}
	
	/**
	 * 查询别名记录数
	 * 
	 * @param synonym参数别名名称
	 * @param isprecise参数是否精确查询
	 * @param iscurrentworditem参数是否当前词条
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return Result
	 */
	public static Result getSynonymCount(String synonym, Boolean isprecise,
			Boolean iscurrentworditem, String type, String curworditem,
			String curwordclass,String wordclassType){
		Result rs = CommonLibSynonymDAO.getSynonymCount(synonym, isprecise, iscurrentworditem, type, curworditem, curwordclass,wordclassType);
		return rs;
	}
	/**
	 * 判断别名是否重复
	 * 
	 * @param stdwordid参数词条id
	 * @param synonym参数别名名称
	 * @return Boolean 是否重复
	 */
	public static Boolean isExistSynonym(String stdwordid, String synonym){
		boolean  rs = CommonLibSynonymDAO.exist(stdwordid, synonym);
		return rs;
	}
	
	/**
	 * 更新别名
	 * @param User 用户信息
	 * @param oldsynonym参数原有别名
	 * @param newsynonym参数新的别名
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数别名id
	 * @param stdwordid参数词条id
	 * @return int 更新记录数
	 */
	public static int updateSynonym(User user,String oldsynonym, String newsynonym,
			String oldtype, String newtype, String wordid, String stdwordid,String curwordclass){
	   int c = CommonLibSynonymDAO.update(user, oldsynonym, newsynonym, oldtype, newtype, wordid, stdwordid, curwordclass);
	   return c;
	}
	
	/**
	 * 新增别名的操作
	 * @param User 用户信息
	 * @param wordclassid参数词类id
	 * @param lstSynonym参数别名集合
	 * @param stdwordid参数词条id
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return int 新增返回的结果
	 */
	public static int insertSynonym(User user,String wordclassid, List<String> lstSynonym,
			String stdwordid, String type, String curworditem,
			String curwordclass) {
		int rs = CommonLibSynonymDAO.insert(user, wordclassid, lstSynonym, stdwordid, type, curworditem, curwordclass);
	    return rs;
	}
	
 	/**
 	 * 删除别名
 	 * @param User 用户信息
 	 * @param stdwordid参数别名id
 	 * @param synonym参数别名名称
 	 * @param curworditem参数词条名称
 	 * @param curwordclass参数词类名称
 	 * @return int 
 	 */
 	public static int deleteSynonyms(User user,String stdwordid, String synonym,
 			String curworditem, String curwordclass){
 		int rs = CommonLibSynonymDAO.delete(user, stdwordid, synonym, curworditem, curwordclass);
 		return rs;
 	}
 	
	/**
	 * 查询答案记录数
	 * @param User 用户信息
	 * @param kbdataid 摘要ID
	 * @return int
	 */
	public static int getAnswerCount(User user, String kbdataid) {
		 int rs = CommonLibFaqDAO.getAnswerCount(user, kbdataid);
		 return rs;
	}
	/**
	 * 查询答案信息
	 * 
	 * @param User
	 *            用户信息
	 * @param kbdataid
	 *            摘要ID
	 * @param start
	 *            起始条数
	 * @param limit
	 *            间隔条数
	 * @return Result
	 */
	public static Result selectAnswer(User user, String kbdataid, String start,
			String limit){
		Result rs =  CommonLibFaqDAO.select(user, kbdataid, start, limit);
		return rs;
	}
	
	/**
	 *@description  判断相同条件下的答案是否存在
	 *@param Operationtype 操作类型
	 *@param kbdataid 摘要ID
	 *@param kbansvaliddateid  有效期ID
	 *@param channel q渠道
	 *@param servicetype 四层结构
	 *@param customertype 客户类型
	 *@param starttime 开始时间
	 *@param endtime 结束时间
	 *@param answerType 答案类型
	 *@return 
	 *@returnType Result 
	 */
	public static  Result isExistAnswer(String Operationtype,String kbdataid,String kbansvaliddateid,String channel,String servicetype, String userid, String customertype,String starttime,String endtime){
		Result rs = CommonLibFaqDAO.exist(Operationtype, kbdataid, kbansvaliddateid, channel, servicetype, userid, customertype, starttime, endtime);
		return rs;
	}
	
	/**
	 *@description  答案新增修改操作
	 *@param user 用户信息
	 *@param Operationtype 操作类型
	 *@param kbdataid 摘要ID
	 *@param kbansvaliddateid  有效期ID
	 *@param channel q渠道
	 *@param servicetype 四层结构
	 *@param customertype 客户类型
	 *@param starttime 开始时间
	 *@param endtime 结束时间
	 *@param answerType 答案类型
	 *@param brand 品牌
	 *@param service 业务
	 *@return 
	 *@returnType int 
	 */
    public static int insertOrUpdateAnswer(User user,String Operationtype,String kbdataid,String kbansvaliddateid,String [] channels,String servicetype ,String customertype,String starttime,String endtime,String answer,String answerType,String brand,String service, String city,String userid, Map channelPersonalityMap){
    	int rs = CommonLibFaqDAO.insertOrUpdate(user, Operationtype, kbdataid, kbansvaliddateid, channels, servicetype, customertype, starttime, endtime, answer, answerType, brand, service, city, userid, channelPersonalityMap);
    	return rs;
    }
    
    /**
     *@description  删除答案
     *@param user  用户信息
     *@param kbansvaliddateid 有效期ID
     *@param answer 答案
     *@param service 业务
     *@param brand 品牌
     *@return 
     *@returnType int 
     */
    public static  int deleteAnswer(User user,String kbansvaliddateid,String answer,String service,String brand){
    	int c = CommonLibFaqDAO.delete(user, kbansvaliddateid, answer, service, brand);
    	return c;
    }
    
    /**
	 * 根据条件分页查询配置名
	 * 
	 * @param metafieldmapping参数配置名
	 * @param start参数起始条数
	 * @param limit参数每页条数
	 * @return json字符串
	 */
    public static Object selectMetafieldMapping(String metafieldmapping, int start, int limit,String show) {
		Object m_result = null;
		m_result = CommonLibMetafieldmappingDAO.select(metafieldmapping, start, limit,show);
		return m_result;
	}
	
	/**
	 * 更新配置名
	 * 
	 * @param metafieldmappingid参数配置名id
	 * @param oldvalue参数旧的配置名
	 * @param newvalue参数新的配置名
	 * @return 更新返回的json串
	 */
	public static int updateMetafieldMapping(User user,String metafieldmappingid, String oldvalue, String newvalue) {
		int count = CommonLibMetafieldmappingDAO.update(user,metafieldmappingid,oldvalue,newvalue);
		return count;
	}
	
	/**
	 * 添加配置名
	 * @param user 用户名集合
	 * @param metafieldmapping 参数配置名集合
	 * @return 新增返回的json串
	 */
	public static int insertMetafieldMapping(User user,List<String> lstName) {
		int count = CommonLibMetafieldmappingDAO.insert(user,lstName);
		return count;
	}
	
	/**
	 * 删除配置名
	 * 
	 * @param metafieldmappingid参数配置名id
	 * @param metafieldmapping参数配置名
	 * @return 删除返回的json串
	 */
	public static int deleteMetafieldMapping(User user,String metafieldmappingid,String metafieldmapping) {
		int count = CommonLibMetafieldmappingDAO.delete(user,metafieldmappingid,metafieldmapping);
		return count;
	}
	
	/**
	 * 查询配置键
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param standardkey参数配置键
	 * @param standardkeyprecise参数是否精确查询
	 * @param iscurrentmetafieldmapping参数是否当前配置名
	 * @param curmetafieldmapping参数配置名
	 * @return 返回json串
	 */
	public static Object selectStandardkey(int start, int limit, String standardkey,
			Boolean standardkeyprecise, Boolean iscurrentmetafieldmapping,
			String curmetafieldmapping) {
		Object m_result = CommonLibStandardkeyDAO.select(start, limit, standardkey, standardkeyprecise, iscurrentmetafieldmapping, curmetafieldmapping);
		return m_result;
	}
	
	/**
	 * 更新配置键
	 * @param user 当前用户
	 * @param oldstandardkey参数旧的配置键
	 * @param newstandardkey参数新的配置键
	 * @param metafieldid参数配置键id
	 * @return 更新返回的json串
	 */
	public static int updateStandardkey(User user,String oldstandardkey, String newstandardkey,String metafieldid) {
		int count = CommonLibStandardkeyDAO.update(user,oldstandardkey, newstandardkey, metafieldid);
		return count;
	}
	
	/**
	 * 新增配置键
	 * 
	 * @param standardkey参数配置键
	 * @param curmetafieldmapping参数配置名
	 * @param curmetafieldmappingid参数配置名id
	 * @param logSql 日志sql
	 * @param lstlstpara 日志sql参数
	 * @return 新增返回的json串
	 */
	public static int insertStandardkey(User user,String curmetafieldmappingid,
			String curmetafieldmapping, List<String> lstStandardkey) {
		
		int count = CommonLibStandardkeyDAO.insert(user, curmetafieldmappingid, curmetafieldmapping, lstStandardkey);
		return count;
	}
	
	/**
	 * 删除配置键
	 * @param user 当前用户
	 * @param metafieldid参数配置键id
	 * @param curmetafieldmapping参数配置名
	 * @param standardkey参数配置键
	 * @param logSql 日志sql
	 * @param logParam 日志sql对应的参数
	 * @return 删除返回的json串
	 */
	public static int deleteStandardkey(User user,String metafieldid, String curmetafieldmapping,String 
			standardkey) {
		int count = CommonLibStandardkeyDAO.delete(user,metafieldid, curmetafieldmapping, standardkey);
		return count;
	}
	
	/**
	 * 带分页的查询满足条件的别名名称
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param standardvalue参数配置值
	 * @param isprecise参数是否精确查询
	 * @param iscurrentstandardkey参数是否当前配置键
	 * @param curstandardkey参数配置键
	 * @param curmetafieldmapping参数配置名
	 * @return 返回json串
	 */
	public static Object selectStandardvalue(int start, int limit, String standardvalue,
			Boolean isprecise, Boolean iscurrentstandardkey,
			String curstandardkey, String curmetafieldmapping) {
		Object m_result = CommonLibStandardvalueDAO.select(start, limit, standardvalue, isprecise, iscurrentstandardkey, curstandardkey, curmetafieldmapping);
		return m_result;
	}
	
	/**
	 * 更新配置值
	 * @param user 当前用户
	 * @param oldstandardvalue参数旧的配置值
	 * @param newstandardvalue参数新的配置值
	 * @param metafieldid参数配置值id
	 * @param stdmetafieldid参数配置键id
	 * @param logSql 日志sql
	 * @param logParam 日志sql对应的参数
	 * @return 更新返回的json串
	 */
	public static int updateStandardvalue(User user,String oldstandardvalue,
			String newstandardvalue, String metafieldid, String stdmetafieldid) {
		int count = CommonLibStandardvalueDAO.update(user,oldstandardvalue, newstandardvalue, metafieldid, stdmetafieldid);
		return count;
	}
	
	/**
	 * 删除配置值
	 * @param user 当前用户
	 * @param stdmetafieldid参数配置值id
	 * @param standardvalue参数配置值名称
	 * @param curstandardkey参数配置键名称
	 * @param curmetafieldmapping参数配置名名称
	 * @return 删除返回的json串
	 */
	public static int deleteStandardvalue(User user,String stdmetafieldid, String standardvalue,
			String curstandardkey, String curmetafieldmapping) {
		int count = CommonLibStandardvalueDAO.delete(user,stdmetafieldid, standardvalue, curstandardkey,curmetafieldmapping);
		return count;
	}
	
	/**
	 * 新增配置值
	 * @param user 当前用户
	 * @param curmetafieldmappingid参数配置名id
	 * @param standardvalue参数配置值
	 * @param stdmetafieldid参数配置键id
	 * @param curstandardkey参数配置键名称
	 * @param curmetafieldmapping参数配置名名称
	 * @return 新增返回的json串
	 */
	public static int insertStandardvalue(User user,String curmetafieldmappingid,
			List<String> lstStandardvalue, String stdmetafieldid,
			String curstandardkey, String curmetafieldmapping) {
		int count = CommonLibStandardvalueDAO.insert(user,curmetafieldmappingid, lstStandardvalue,stdmetafieldid, curstandardkey, curmetafieldmapping);
		return count;
	}
	
	/**
	 * 获取日志操作对象
	 * @param sql 执行的sql语句
	 * @return
	 */
	public static Result getOperateTable(String sql) {
		Result rs  = CommonLibOperationlogDAO.table(sql);
		return rs;
	}
	
	/**
	 * 分页查询满足条件的日志信息
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param starttime参数开始时间
	 * @param endtime参数截止时间
	 * @param workername参数操作人
	 * @param queryname参数是否精确查询操作人
	 * @param operation参数操作类型
	 * @param table参数操作对象
	 * @param object参数操作内容
	 * @param queryobject参数是否精确查询操作内容
	 * @return json串
	 */
	public static Object selectOperateLog(int start, int limit, String starttime,String endtime, String workername, String queryname,
			String operation, String table, String object, String queryobject) {
		Object m_result = null;
		m_result = CommonLibOperationlogDAO.select(start, limit, starttime, endtime, workername, queryname, operation, table, object, queryobject);
		return m_result;
	}
	

	/**
	 *@description 查询模板是否存在
	 *@param brand 品牌
	 *@param service 业务
	 *@param kbdataid 摘要ID
	 *@param wordpat 简单词模
	 *@param serviceRoot 业务根
	 *@return 
	 *@returnType Result 
	 */
	public static Result isExistWordpat(String brand,String service,String kbdataid,String wordpat,String serviceRoot) {
		Result  rs = CommonLibWordpatDAO.exist(brand, service, kbdataid, wordpat, serviceRoot);
		return rs;
    }
	
	/**
	 *@description 判断当前子句下词模是否存在
	 *@param wordpat 词模
	 *@param abs 摘要
	 *@return 
	 *@returnType Boolean 
	 */
	public static Boolean isExistZijuWordpat(String wordpat,String abs) {
		Boolean rs = CommonLibWordpatDAO.isExistZijuWordpat(wordpat, abs);
		return rs;
	}
	
	/**
	 *@description 新增词模
	 *@param user 用户信息
	 *@param service 业务
	 *@param brand 品牌
	 *@param kbdataid 摘要ID
	 *@param wordpat  词模
	 *@param simplewordpat 简单词模
	 *@param wordpattype 词模类型
	 *@return 
	 *@returnType int 
	 */
	public static int insertWordpat(User user,String service,String brand,String kbdataid,String wordpat,String simplewordpat,String wordpattype) {
		int rs = CommonLibWordpatDAO.insert(user, service, brand, kbdataid, wordpat, simplewordpat, wordpattype);
		return rs;
	}
	
	/**
	 *@description  删除词模
	 *@param user   用户信息
	 *@param brand  品牌
	 *@param service 业务
	 *@param wordpatid 词模ID
	 *@param wordpat 词模
	 *@param simpleWordpat 简单词模
	 *@return 
	 *@returnType int 
	 */
	public static int deleteWordpat(User user,String brand,String service,String wordpatid,String wordpat,String simpleWordpat) {
		int rs = CommonLibWordpatDAO.delete(user, brand, service, wordpatid, wordpat, simpleWordpat);
		return rs;
	}

	
	/**
	 *@description  更新词模
	 *@param user   用户信息
	 *@param service 业务
	 *@param brand 品牌
	 *@param kbdataid 摘要ID
	 *@param oldWordpatid 旧词模ID
	 *@param newWordpat 新词模
	 *@param autosendswitch 
	 *@param wordpattype 词模类型
	 *@param oldsimplewordpat 旧简单词模
	 *@param simplewordpat 简单词模
	 *@return 
	 *@returnType int 
	 */
	public static int updateByinsertAndelete(User user,String service,String brand,String kbdataid,String oldWordpatid,String newWordpat,String autosendswitch,String wordpattype,String oldsimplewordpat,String simplewordpat) {
		int rs = CommonLibWordpatDAO.updateByinsertAndelete(user, service, brand, kbdataid, oldWordpatid, newWordpat, autosendswitch, wordpattype, oldsimplewordpat, simplewordpat);
		return rs;
	}
	
	/**
	 *@description 查询模板记录数 
	 *@param brand 品牌
	 *@param service 业务
	 *@param kbdataid 摘要ID
	 *@param selectWordpat 查询词模内容
	 *@return 
	 *@returnType Result 
	 */
	public static Result getWordpatCount(String brand,String service,String kbdataid,String selectWordpat){
		Result rs = CommonLibWordpatDAO.getWordpatCount(brand, service, kbdataid, selectWordpat);
		return rs;
	}
	
	/**
	 *@description 查询模板数据
	 *@param brand  品牌
	 *@param service 业务
	 *@param kbdataid 摘要ID
	 *@param selectWordpat 查询词模内容
	 *@param start 开始记录数
	 *@param limit 间隔记录数
	 *@return 
	 *@returnType Result 
	 */
	public static Result selectWordpat(String brand,String service,String kbdataid,String selectWordpat,String start,String limit) {
		// 定义条件的SQL语句
	 Result rs = CommonLibWordpatDAO.select(brand, service, kbdataid, selectWordpat, start, limit);
	 return rs;
	 }
	
	/**
	 *@description 查询满足条件的相似问题的数
	 *@param kbdataid
	 *            摘要ID
	 *@param question
	 *            模糊查询问题
	 *@param isbzquestion
	 *            标准问题标识
	 *@return
	 *@returnType int
	 */
	public static int getQuestionCount(String kbdataid, String question,
			Object isbzquestion) {
		int rs = CommonLibQuestionDAO.getCount(kbdataid, question, isbzquestion);
		return rs;
	}
	
	/**
	 *@description 查询满足条件的相似问题数据
	 *@param kbdataid 摘要ID
	 *@param question 模糊查询问题
	 *@param isbzquestion 标准问题
	 *@param start 开始记录数据
	 *@param limit 间隔记录数
	 *@return 
	 *@returnType Result 
	 */
	public static Result selectQuestion(String kbdataid, String question,
			Object isbzquestion, String start, String limit) {
		Result rs = CommonLibQuestionDAO.select(kbdataid, question, isbzquestion, start, limit);
		return rs;
	}
	/**
	 * 判断问题是否存在
	 * 
	 * @param question参数问题
	 * @param kbdataid参数摘要id
	 * @param questiontype参数问题类型
	 * @param serviceid参数业务id
	 * @return 是否存在
	 */
	public static int isExistQuestion(String question, String kbdataid,
			String questiontype, String serviceid){
		int rs = CommonLibQuestionDAO.exist(question, kbdataid, questiontype, serviceid);
		return rs;
	}
	
	/**
	 *@description  修改相似问题
	 *@param user 用户信息
	 *@param oldquestion 旧问题
	 *@param question 新问题
	 *@param questiontype 问题类型
	 *@param questionid 问题ID
	 *@return 
	 *@returnType int 
	 */
	public static int updateQuestion(User user,String oldquestion,String question,String questiontype,String questionid) {
		int rs = CommonLibQuestionDAO.update(user, oldquestion, question, questiontype, questionid);
		return rs;
	}
	
	/**
	 *@description 删除相似问题
	 *@param user 用户信息
	 *@param questionid 问题ID
	 *@param oldquestion 旧问题
	 *@param question 新问题
	 *@param service 业务名
	 *@param brand 品牌
	 *@param kbdataid 摘要ID
	 *@return 
	 *@returnType int 
	 */
	public static int deleteQuestion(User user,String questionid,String question,String service,String brand,String kbdataid) {
		int rs = CommonLibQuestionDAO.delete(user, questionid, question, service, brand, kbdataid);
	    return rs;
	}
	
	/**
	 *@description  插入多条相似问题
	 *@param user   用户信息
	 *@param kbdataid 摘要ID
	 *@param abs 摘要名
	 *@param questionList 相似问题集合
	 *@param questiontype 相似问题类型
	 *@param brand 品牌
	 *@return 
	 *@returnType int 
	 */
	public static int insertQuestion(User user,String kbdataid,String abs,List<String> questionList,String questiontype,String brand){
		int rs = CommonLibQuestionDAO.insert(user, kbdataid, abs, questionList, questiontype, brand);
		return rs;
	}
	
	/**
	 *@description 通过问题类型摘要Id查询问题
	 *@param questiontype 问题类型
	 *@param kbdataid 摘要ID
	 *@return 
	 *@returnType Result 
	 */
	public static Result getQuestion(String questiontype, String kbdataid) {
		Result rs = CommonLibQuestionDAO.getQuestion(questiontype, kbdataid);
		return rs;
	}
	
	   /**
	 *@description 获得参数配置表具体值数据源
	 *@param name  配置参数名
	 *@param key   配置参数名对应key
	 *@return 
	 *@returnType Result 
	 */
	public static Result getConfigValue(String name ,String key){
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue(name, key);
		return rs;
	}
	
	/**
	 *@description  获得四层结构信息
	 *@return 
	 *@returnType Result 
	 */
	public static Result getIndustryapplicationToServicesInfo() {
		Result rs = CommonLibIndustryApplicationToServicesDAO.getIndustryapplicationToServicesInfo();
		return rs;
	}
	
	/**
	 *@description  通过商家组织应用获得四层结构信息
	 *@return 
	 *@returnType Result 
	 */
	public static Result getIndustryapplicationToServicesInfo(String industry ,String organization, String application ) {
		Result rs = CommonLibIndustryApplicationToServicesDAO.getIndustryapplicationToServicesInfo(industry, organization, application);
	    return rs;
	}
	
	/**
	 * 查询满足条件的问题要素信息记录数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param name参数问题要素名称
	 * @return int
	 */
	public static int getElementNameCount(String kbdataid, String kbcontentid,
			String name) {
		int count = CommonLibQueryElementDAO.getElementNameCount(kbdataid, kbcontentid, name);
		return  count;
	}
	
	
	/**
	 * 分页查询满足条件的问题要素信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param name参数问题要素名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return Result
	 */
	public static Result getElementName(String kbdataid, String kbcontentid,
			String name, int page, int rows) {
		Result rs = CommonLibQueryElementDAO.getElementName(kbdataid, kbcontentid, name, page, rows);
		return rs;
	}
	
	/**
	 * 查询当前问题元素下的优先级
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @return Result
	 */
	public static Result getWeight(String kbdataid, String kbcontentid) {
		Result rs = CommonLibQueryElementDAO.getWeight(kbdataid, kbcontentid);
		return rs;
	}
	/**
	 * 添加问题要素
	 * 
	 * @param name参数问题要素
	 * @param kbdataid参数摘要id
	 * @param weight参数优先级
	 * @param wordclass参数词类名称
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int insertElementName(String name, String kbdataid,
			String kbcontentid, String weight, String wordclass, String abs, String serviceType) {
		int rs = CommonLibQueryElementDAO.insertElementName(name, kbdataid, kbcontentid, weight, wordclass, abs, serviceType);
		return rs;
	}
	
	/**
	 * 删除问题要素
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param elementnameid参数问题要素id
	 * @param weight参数优先级
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int deleteElementName(String kbdataid, String kbcontentid,
			String elementnameid, String weight, String name, String abs) {
		int rs = CommonLibQueryElementDAO.deleteElementName(kbdataid, kbcontentid, elementnameid, weight, name, abs);
		return rs;
	}
	
	/**
	 * 分页查询满足条件的问词条数
	 * 
	 * @param wordclassid参数词类id
	 * @param name参数问题要素值名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static int getElementWordCount(String wordclassid, String name) {
		int rs = CommonLibWordDAO.getWordCount(wordclassid, name);
		return rs;
	}

	/**
	 * 分页查询满足条件的问题要素值(词条)
	 * 
	 * @param wordclassid参数词类id
	 * @param name参数问题要素值名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Result getElementWord(String wordclassid, String name, int page,
			int rows) {
		Result rs = CommonLibWordDAO.select(wordclassid, name, page, rows);
		return rs;
	}
	
	/**
	 *@description   查询属性值是否重复
	 *@param name参数问题要素值
	 *@param wordclassid参数词类名称id
	 *@return 
	 *@returnType boolean 
	 */
	public static boolean isExitElementValue(String name, String wordclassid) {
		boolean rs  = CommonLibWordDAO.exist(name, wordclassid);
		return rs;
	}
	
	/**
	 * 新增问题要素值
	 * 
	 * @param name参数问题要素值
	 * @param wordclassid参数词类名称id
	 * @param wordclass参数词类名称
	 * @return 新增返回的json串
	 */
	public static int insertElementValue(String name, String wordclassid,
			String wordclass) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		int rs = CommonLibWordDAO.insert(name, wordclassid, user);
		return rs;
	}
	
	/**
	 * 删除问题要素值(词条),并更新对应的数据和规则
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param elementvalueid参数问题要素值id
	 * @param weight参数问题要素的优先级
	 * @param name参数问题要素值名称
	 * @param wordclass参数词类名称
	 * @return int
	 */
	public static int deleteElementValue(String kbdataid,
			String kbcontentid, String elementvalueid, String weight,
			String name, String wordclass) {
	int rs = CommonLibQueryElementDAO.deleteElementValue(kbdataid, kbcontentid, elementvalueid, weight, name, wordclass);
	return rs;
	}
	
	/**
	 * 查询当前摘要下的问题要素和关联词条组合信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 */
	public static Result queryElementAndWord(String kbdataid, String kbcontentid) {
		Result rs = CommonLibQueryElementDAO.queryElementAndWord(kbdataid, kbcontentid);
		return rs;
	}
	
	/**
	 * 查询问题要素信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @return 问题要素组合的json串
	 */
	public static Result queryElement(String kbdataid, String kbcontentid) {
		Result rs  = CommonLibQueryElementDAO.queryElement(kbdataid, kbcontentid);
		return rs;
	}
	
    /**
     *@description 通过 kbanswerid 查询答案内容
     *@param kbanswerid  答案ID
     *@return 
     *@returnType Result 
     */
    public static Result getAnswercontent(String kbanswerid){
    	Result rs = CommonLibFaqDAO.select(kbanswerid);
    	return rs;
    }
    
	/**
	 * 查询满足条件的带分页的问题要素数据信息数据源
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合条件
	 * @param returntxttype参数答案类型
	 * @param status参数状态
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return Result
	 */
	public static Result getConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String status, int page, int rows) {
		Result rs = CommonLibQueryElementDAO.getConditionCombToReturnTxt(kbdataid, kbcontentid, conditions, returntxttype, status, page, rows);
	    return rs;
	}
	
	
	/**
	 * 查询问题要素数据信息记录数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合条件
	 * @param returntxttype参数答案类型
	 * @param status参数状态
	 * @return int
	 */
	public static int getConditionCombToReturnTxtCount(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String status) {
		int rs = CommonLibQueryElementDAO.getConditionCombToReturnTxtCount(kbdataid, kbcontentid, conditions, returntxttype, status);
	    return rs;
	}
	
	/**
	 * 确认问题元素值组合，将待审核变成已审核 ,返回确认条数
	 * 
	 * @param combitionid参数combitionid
	 * @return int
	 */
	public static int ConfirmConditionCombToReturnTxt(String combitionid) {
		int rs = CommonLibQueryElementDAO.confirmConditionCombToReturnTxt(combitionid);
		return rs;
	}
	
	/**
	 * 确认问题元素值组合，将待审核变成已审核 ,返回确认条数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @return int
	 */
	public static int confirmAllConditionCombToReturnTxt(String kbdataid,
			String kbcontentid) {
		int rs  = CommonLibQueryElementDAO.confirmAllConditionCombToReturnTxt(kbdataid, kbcontentid);
		return rs;
	}
	
	
	/**
	 * 根据数据id删除相应的数据，并返回执行条数
	 * 
	 * @param combitionid参数数据id
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int deleteConditionCombToReturnTxt(String combitionid,
			String abs) {
		int rs = CommonLibQueryElementDAO.deleteConditionCombToReturnTxt(combitionid, abs);
		return rs;
	}
	
	/**
	 * 全量删除数据,并返回执行条数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int deleteAllConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String abs) {
		int rs = CommonLibQueryElementDAO.deleteAllConditionCombToReturnTxt(kbdataid, kbcontentid, abs);
		return rs;
	}
	
	/**
	 * 将回复模板保存到答案表中，并返回执行条数
	 * 
	 * @param answer参数回复模板
	 * @param kbanswerid参数kbanswerid
	 * @return int
	 */
	public static int saveModel(String answer, String kbanswerid) {
		int rs = CommonLibQueryElementDAO.saveModel(answer, kbanswerid);
		return rs;
	}
	
	/**
	 * 判断问题要素信息数据库中是否存在
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param returntxttype参数答案类型
	 * @param returntxt参数答案内容
	 * @param abs参数摘要名称
	 * @return boolean
	 */
	public static  boolean  isExitConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String returntxt) {
		boolean rs = CommonLibQueryElementDAO.isExitConditionCombToReturnTxt(kbdataid, kbcontentid, conditions, returntxttype, returntxt);
	    return rs;
	}
	
	/**
	 * 将问题要素信息添加到数据库中，并返回执行条数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param returntxttype参数答案类型
	 * @param returntxt参数答案内容
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int  insertConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String returntxt, String abs, String serviceType) {
		int rs = CommonLibQueryElementDAO.insertConditionCombToReturnTxt(kbdataid, kbcontentid, conditions, returntxttype, returntxt, abs, serviceType);
	    return rs;
	}

	/**
	 * 更新当前数据中需要修改的值，并将状态改为未审核，并返回执行条数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param returntxttype参数答案类型
	 * @param returntxt参数答案内容
	 * @param combitionid参数数据id
	 * @return int
	 */
	public static int updateConditionCombToReturnTxt(String kbdataid,
			String kbcontentid, String conditions, String returntxttype,
			String returntxt, String combitionid) {
		int rs = CommonLibQueryElementDAO.updateConditionCombToReturnTxt(kbdataid, kbcontentid, conditions, returntxttype, returntxt, combitionid);
	    return rs;
	}
	
	/**
	 * 判断规则是否存在
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param ruletype参数规则类型
	 * @param ruleresponse参数规则回复内容
	 * @return boolean
	 */
	public static boolean isExitSceneRules(String kbdataid, String kbcontentid,
			String conditions,  String ruletype,
			String ruleresponse,String weight) {
		boolean rs = CommonLibQueryElementDAO.isExitSceneRules(kbdataid, kbcontentid, conditions, ruletype, ruleresponse,weight);
		return rs;
	}
	
	/**
	 * 将规则添加到规则表中，并返回相应的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param weight参数规则优先级
	 * @param ruletype参数规则类型
	 * @param ruleresponse参数规则回复内容
	 * @param abs参数摘要名称
	 * @return 添加后返回的json串
	 */
	public static int insertSceneRules(String kbdataid, String kbcontentid,
			String conditions, String weight, String ruletype,
			String ruleresponse, String abs, String serviceType) {
		int rs = CommonLibQueryElementDAO.insertSceneRules(kbdataid, kbcontentid, conditions, weight, ruletype, ruleresponse, abs, serviceType);
	    return rs;
	}
	
	/**
	 * 根据不同的条查询满足条件的规则信息记录数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param ruletype参数规则类型
	 * @param weight参数规则优先级
	 * @return int
	 */
	public static int getSceneRulesCount(String kbdataid, String kbcontentid,
			String conditions, String ruletype, String weight){
		int count = CommonLibQueryElementDAO.getSceneRulesCount(kbdataid, kbcontentid, conditions, ruletype, weight);
		return count;
	}
	
	/**
	 * 根据不同的条件分页查询满足条件的规则信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param ruletype参数规则类型
	 * @param weight参数规则优先级
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return Result
	 */
	public static Result getSceneRules(String kbdataid, String kbcontentid,
			String conditions, String ruletype, String weight, int page,
			int rows){
		Result rs = CommonLibQueryElementDAO.getSceneRules(kbdataid, kbcontentid, conditions, ruletype, weight, page, rows);
		return rs;
	}
	
	/**
	 * 根据规则id删除规则信息，并返回执行记录数
	 * 
	 * @param ruleid参数规则id
	 * @param abs参数摘要名称
	 * @return int
	 */
	public static int deleteSceneRules(String ruleid, String abs){
		int rs = CommonLibQueryElementDAO.deleteSceneRules(ruleid, abs);
		return rs;
	}
	
	/**
	 * 更新规则中需要修改的值，并返回执行记录数
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param conditions参数问题要素组合
	 * @param weight参数规则优先级
	 * @param ruletype参数规则类型
	 * @param ruleresponse参数规则回复内容
	 * @param ruleid参数规则id
	 * @return int
	 */
	public static int updateSceneRules(String kbdataid, String kbcontentid,
			String conditions, String weight, String ruletype,
			String ruleresponse, String ruleid) {
		int rs = CommonLibQueryElementDAO.updateSceneRules(kbdataid, kbcontentid, conditions, weight, ruletype, ruleresponse, ruleid);
	    return rs;
	}
	
	 /**
     *@description 通过业务查询关联答案知识记录数
     *@param sevicetype 四层结构串
     *@param content 查询内容
     *@param sevicecontainer 业务归属标识
     *@param subjectTreeName 主题树名称
     *@param serviceRoot 业务根串
     *@return 
     *@returnType int 
     */
    public static int getKnowledgeByServiceCount(String sevicetype,String content, String serviceRoot, /*List<String> permitServices*/ String userId){
    	int rs = CommonLibFaqDAO.getKnowledgeByServiceCount(sevicetype, content, serviceRoot, userId);
        return rs;
    }
    
    /**
     *@description 通过业务查询关联答案知识
     *@param sevicetype 四层结构串
     *@param content 查询内容
     *@param serviceRoot 业务根串
     *@param sevicecontainer 业务归属标识
     *@param subjectTreeName 主题树名称
     *@param start 起始记录数
     *@param limit 间隔记录数
     *@return 
     *@returnType Result 
     */
    public static Result getKnowledgeByService(String sevicetype,String content, String serviceRoot, /*List<String> permitServices*/ String userId, int start, int limit) {
    	Result rs = CommonLibFaqDAO.getKnowledgeByService(sevicetype, content, serviceRoot, userId, start, limit);
        return rs;
    }
    
	/**
	 *@description  通过摘要查询关联答案知识记录数
	 *@param sevicetype 四层机构串
	 *@param content    模糊查询内容
	 *@param brandStr 品牌组合串
	 *@param abs 
	 *@return 
	 *@returnType int 
	 */
	public static int getKnowledgeByAbstractCount(String sevicetype,
			String content,String brandStr,String abs, /*List<String> permitServices*/String userId){
	int rs = CommonLibFaqDAO.getKnowledgeByAbstractCount(sevicetype, content, brandStr, abs, userId);
	return rs;
	}
	
	/**
	 *@description  通过摘要查询关联答案知识
	 *@param sevicetype 四层机构串
	 *@param content    模糊查询内容
	 *@param brandStr   品牌组合串
	 *@param abs 摘要串
	 *@param start      起始记录数
	 *@param limit      间隔记录数
	 *@return 
	 *@returnType Result 
	 */
	public static Result getKnowledgeByAbstract(String sevicetype,
			String content,String brandStr,String abs, /*List<String> permitServices*/String userId, int start, int limit){
		Result rs = CommonLibFaqDAO.getKnowledgeByAbstract(sevicetype, content, brandStr, abs, userId, start, limit);
	    return rs;
	}
	
	/**
	 *@description  通过答案内容查询关联知识记录数
	 *@param sevicetype 四层结构串
	 *@param content    模糊查询内容
	 *@param serviceRoot 业务根串
	 *@param sevicecontainer 业务归属标识
	 *@return 
	 *@returnType int 
	 */
	public static int getKnowledgeByAnswerCount(String sevicetype,
			String content,String serviceRoot, /*List<String> permitServices*/String userId){
		int count = CommonLibFaqDAO.getKnowledgeByAnswerCount(sevicetype, content, serviceRoot, userId);
		return count;
	}
	
	/**
	 *@description  通过答案内容查询关联知识
	 *@param sevicetype 四层结构串
	 *@param content    模糊产寻内容
	 *@param start      起始记录数
	 *@param limit      间隔记录数
	 *@param serviceRoot 业务根串
	 *@param sevicecontainer 业务归属标识
	 *@return  
	 *@returnType Result 
	 */
	public static Result getKnowledgeByAnswer(String sevicetype,
			String content, int start, int limit,String serviceRoot, /*List<String> permitServices*/String userId){
		Result rs = CommonLibFaqDAO.getKnowledgeByAnswer(sevicetype, content, start, limit, serviceRoot, userId);
		return rs;
	}
	
	/**
	 * 通过扩展问获取关联知识
	 * @param content
	 * @param sevicetype
	 * @param rootServices
	 * @param start
	 * @param limit
	 * @return
	 */
	public static int getKnowledgeByExtendQueryCount(String content, String sevicetype, String rootServices, String userId){
		return CommonLibFaqDAO.getKnowledgeByExtendQueryCount(content, sevicetype, rootServices, userId);
	}
	
	/**
	 * 通过扩展问获取关联知识
	 * @param content
	 * @param sevicetype
	 * @param rootServices
	 * @param permitServices
	 * @param start
	 * @param limit
	 * @return
	 */
	public static Result getKnowledgeByExtendQuery(String content, String sevicetype, String rootServices, String userId, int start, int limit){
		return CommonLibFaqDAO.getKnowledgeByExtendQuery(content, sevicetype, rootServices, userId, start, limit);
	}
	
	/**
	 * 分页查询满足条件的数据
	 * 
	 * @param question参数标准问题
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Result selectQuery(String servicetype, String question, int page, int rows, int sqlno){
		return CommonLibRegressTestDAO.selectQuery(servicetype, question, page, rows, sqlno);
	}
	
	/**
	 * 删除回归问题
	 * 
	 * @param extendquestion参数扩展问题
	 * @param question参数标准问题
	 * @return 删除返回的json串
	 */
	public static int deleteRegress(String extendquestion, String question){
		return CommonLibRegressTestDAO.deleteRegress(extendquestion, question);
	}
	
	/**
	 * 分页查询满足条件的数据
	 * 
	 * @param starttime参数开始时间
	 * @param endtime参数结束时间
	 * @param question参数标准问题
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Result selectQueryResult(String starttime, String endtime,
			String question, int page, int rows, int sqlno){
		return CommonLibRegressTestDAO.selectQueryResult(starttime, endtime,
				question, page, rows, sqlno);
	}
	
	/**
	 * 导入回归问题到数据库中
	 * 
	 * @param filename参数文件名称
	 * @return
	 */
	public static int importFile(List<List<String>> info, String servicetype){
		return CommonLibRegressTestDAO.importFile(info, servicetype);
	}
	
	/**
	 * 分析的错误结果记录到表regressqueryresult
	 * 
	 * @param lstLstRegress参数错误的回归问题集合
	 * @return 事务处理结果
	 */
	public static int insertRegressqueryresult(List<List<Object>> lstLstRegress) {
		return CommonLibRegressTestDAO.insertRegressqueryresult(lstLstRegress);
	}
	
	/**
	 * 查询所有回归问题
	 * 
	 * @param servicetype参数业务类型
	 * @return
	 */
	public static Result selectRegressquery(String servicetype) {
		return CommonLibRegressTestDAO.selectRegressquery(servicetype);
	}
	
	/**
	 * 删除当天的回归测试数据
	 */
	public static void DeleteRegressqueryresult(String nowTime){
		CommonLibRegressTestDAO.DeleteRegressqueryresult(nowTime);
	}
	
	/**
	 * @description获取每日用户量数据
	 * @param hotType
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static Result getData(String startTime, String endTime,String channel) {
		return CommonLibDayUserNumDAO.getData(startTime, endTime, channel);
	}
	
	/**
	 * @description获取用户的信息
	 * @return
	 */
	public static Result getUserName(String workerid,String customer){
		return CommonLibReportDAO.getUserName(workerid,customer);
	}
	
	/**
	 * @description查询某段时间内员工工作量
	 * @param starttime
	 * @param endtime
	 * @param userArr
	 * @return
	 */
	public static Result queryAndexportWordload(String starttime, String endtime, Object[] userArr){
		return CommonLibReportDAO.queryAndexportWordload(starttime, endtime, userArr);
	}
	
	/**
	 * @description获取渠道
	 * @return
	 */
	public static Result getChannel(){
		return CommonLibMetafieldmappingDAO.getConfigValue("渠道参数配置", "渠道");
	}
	
	/**
	 * @description查询某段时间内所有的PV量
	 * @param starttime
	 * @param endtime
	 * @param channelArr
	 * @return
	 */
	public static Result getAndExportPvAll(String starttime, String endtime, Object[] channelArr){
		return CommonLibReportDAO.getAndExportPvAll(starttime, endtime, channelArr);
	}
	
	/**
	 * @description查询某段时间内运行详情
	 * @param starttime
	 * @param endtime
	 * @param channelArr
	 * @param sqlno
	 * @return
	 */
	public static Result getRobotRate(String starttime, String endtime, Object[] channelArr, int sqlno){
		return CommonLibReportDAO.getRobotRate(starttime, endtime, channelArr, sqlno);
	}
	
	/**
	 * @description获取未理解问题
	 * @param starttime
	 * @param endtime
	 * @param channelArr
	 * @return
	 */
	public static Result getRobotUnMatched(String starttime, String endtime, Object[] channelArr){
		return CommonLibReportDAO.getRobotUnMatched(starttime, endtime, channelArr);
	}
	
	/**
	 * @description读取数据库,获取未识别问题,生成Excel文件,返回文件的路径
	 * @param starttime
	 * @param endtime
	 * @param channelArr
	 * @return
	 */
	public static Result exportUnMatched(String starttime, String endtime, Object[] channelArr){
		return CommonLibReportDAO.exportUnMatched(starttime, endtime, channelArr);
	}
	
	/**
	 * 读取数据库，获取全部咨询，生成Excel文件，返回文件的路径
	 * 
	 * @param params
	 * @return
	 */
	public static Result exportRobotDetailAll(String starttime, String endtime, Object[] channelArr){
		return CommonLibReportDAO.exportRobotDetailAll(starttime, endtime, channelArr);
	}

	public static Result getKnowledgeByStatus(String servicetype,
			String content, String serviceRoot, String sevicecontainer) {
		return CommonLibReportDAO.getKnowledgeByStatus(servicetype,content,serviceRoot,sevicecontainer);
	}

	public static Result getKnowledgeByStatus(String servicetype,
			String content, int start, int limit, String serviceRoot,
			String sevicecontainer) {
		return CommonLibReportDAO.getKnowledgeByStatus(servicetype,content,start,limit,serviceRoot,sevicecontainer);
	}
}
