package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.knowology.GlobalValue;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.dal.Database;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.util.CheckInput;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.SimpleString;

public class QuestionManageDAO {

	/**
	 * 创建场景关联问题库
	 * 
	 * @param parentName上级场景名称ID
	 * @param scenarioName场景名称
	 * @return
	 */
	public static Object createScenarios(String preServiceId, String scenarioName) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		// 查询当前业务ID
		String brand = "";
		Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(preServiceId);
		if (rs != null && rs.getRowCount() > 0) {
			Object obj = rs.getRows()[0].get("brand");
			if (obj == null || "".equals(obj)) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "业务根不存在！");
				return jsonObj;
			}
			brand = obj.toString();
		}

		String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(serviceType);

		if (CommonLibQueryManageDAO.isExistServiceNameNew(preServiceId, scenarioName, brand)) {// 判断是否存在相同名称业务
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "业务名称已存在!");
			return jsonObj;
		}

		String serviceId = CommonLibQueryManageDAO.insertService(preServiceId, scenarioName, brand, bussinessFlag,
				user);
		if (serviceId != null) {
			jsonObj.put("success", true);
			jsonObj.put("serviceid", serviceId);
			jsonObj.put("msg", "新业务添加成功");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "新业务添加失败");
		}
		return jsonObj;
	}

	/**
	 * 创建场景子业务的标准问
	 * 
	 * @param serviceid        场景名ID
	 * @param service          场景名
	 * @param questionAndWords key：标准问题；value:词模
	 * @param city             业务地市
	 * @return
	 */
	public static Object createStandardQuestion(String serviceid, String service, Map<String, String> questionAndWords,
			String city, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		List<String> infoList = new ArrayList<String>();
		int i = 1;
		for (String key : questionAndWords.keySet()) {
			String normalQuery = key;
			// 1.新增标准问题
			int rt = createNormalQuery(user, serviceid, normalQuery);
			if (rt > 0) {
				// 2.查询摘要ID
				String kbdataid = getKbdataId(serviceid, service, normalQuery);
				if (!StringUtils.isBlank(kbdataid)) {
					// 3.新增词模
					String info = createWordpat(user, service, kbdataid, questionAndWords.get(key), city, request);
					if (!StringUtils.isBlank(info)) {
						infoList.add("第" + i + "条" + info);
					}
				} else {
					infoList.add("第" + i + "条摘要不存在!");
				}
			} else {
				if (rt == -2) {
					infoList.add("第" + i + "条标准问题已存在!");
				} else {
					infoList.add("第" + i + "条标准问题保存失败!");
				}
			}
			i++;
		}
		if (infoList.size() == 0) {
			jsonObj.put("success", true);
			jsonObj.put("message", "保存成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("message", StringUtils.join(infoList, "\n"));
		}
		return jsonObj;
	}

	/**
	 * 新增标准问题
	 * 
	 * @param user
	 * @param serviceid场景业务ID
	 * @param normalQuery标准问题
	 * @return
	 */
	public static int createNormalQuery(User user, String serviceid, String normalQuery) {
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		cityList = resourseMap.get("地市");
		String userCityCode = "";
		if (cityList.size() > 0) {
			userCityCode = StringUtils.join(cityList.toArray(), ",");
		}

		// 业务地市
		List<String> serviceCityList = new ArrayList<String>();
		String serviceCityCode = "";
		Result scityRs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (scityRs != null && scityRs.getRowCount() > 0) {
			String city = scityRs.getRows()[0].get("city").toString();
			serviceCityList = Arrays.asList(city.split(","));
		}
		if (serviceCityList.size() > 0) {
			serviceCityCode = StringUtils.join(serviceCityList.toArray(), ",");
		}

		int rs = CommonLibQueryManageDAO.addNormalQueryAndCustomerQuery(serviceid, normalQuery, "", "", user,
				userCityCode, serviceCityCode);

		return rs;
	}

	/**
	 * 查询摘要ID
	 * 
	 * @param serviceid
	 * @param service
	 * @param normalQuery
	 * @return
	 */
	public static String getKbdataId(String serviceid, String service, String normalQuery) {
		String kbdataid = "";
		String abs = "<" + service + ">" + normalQuery;
		// 定义SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 定义查询满足条件的总条数的SQL语句
		sql = "select * from kbdata where abstract = ? and serviceid = ? and topic = ?";
		// 绑定类型参数
		lstpara.add(abs);
		lstpara.add(serviceid);
		lstpara.add("常见问题");
		//文件日志
		GlobalValue.myLog.info( sql + "#" + lstpara );
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			kbdataid = rs.getRows()[0].get("kbdataid").toString();
		}
		return kbdataid;
	}

	/**
	 * 新增词模
	 * 
	 * @param user
	 * @param service
	 * @param brand
	 * @param kbdataid
	 * @param simplewordpat
	 * @param city 
	 * @return
	 */
	public static String createWordpat(User user, String service, String kbdataid, String simplewordpat,
			String city, HttpServletRequest request) {
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		// 将简单词模转换为普通词模
		String wordpat = SimpleString.SimpleWordPatToWordPat(simplewordpat);
		// 词模检查结果字符串
		String checkInfo = "";
		List<String> patternList = new ArrayList<String>();

		patternList.add(simplewordpat);
		// 词模检查结果
		Boolean checkflag = true;// 语法检查过程出现异常！
		CheckInforef curcheckInfo = new CheckInforef();
		try {
			// 调用词模检查函数
			if (!CheckInput.CheckGrammer(path, wordpat, 0, curcheckInfo))
				// 词模有误
				checkflag = false;
		} catch (Exception ex) {
			// 检查过程中出现异常，则报错
			checkflag = false;
			curcheckInfo.curcheckInfo = "模板语法有误！";
		}
		// 判断curcheckInfo
		if (!"".equals(curcheckInfo.curcheckInfo) && (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
			checkInfo = curcheckInfo.curcheckInfo;
		}
		// 词模检查失败，则报错
		if (!checkflag) {
			return checkInfo;
		}

		// 判断是否已存在相同模板
		String returninfo = isExistsWordpat(user, service, user.getBrand(), kbdataid, wordpat, simplewordpat);
		if (StringUtils.isBlank(returninfo)) {
			int c = CommonLibWordpatDAO.insert(user, service, user.getBrand(), kbdataid, wordpat, simplewordpat, "0", city);
			if (c <= 0) {
				returninfo = "词模插入失败！";
			}
		}
		return returninfo;
	}

	/**
	 * 查询模板是否存在
	 * 
	 * @param user
	 * @param service
	 * @param brand
	 * @param kbdataid
	 * @param wordpat
	 * @param simplewordpat
	 * @return
	 */
	public static String isExistsWordpat(User user, String service, String brand, String kbdataid, String wordpat,
			String simplewordpat) {
		String checkInfo = "";

		// 获取行业
		String serviceRoot = "'";
		if (user.getServiceRoot() != null) {
			for (int i = 0; i < user.getServiceRoot().length; i++) {
				if (i == user.getServiceRoot().length - 1) {
					serviceRoot += user.getServiceRoot()[i];
				} else {
					serviceRoot += user.getServiceRoot()[i] + "','";
				}
			}
			serviceRoot += "'";
		}

		// 将模板按照#拆分
		String pattern[] = wordpat.split("#");
		// 将返回值按照&拆分，获取返回值数组
		String returnvalue[] = pattern[1].split("&");

		// 执行SQL语句，获取相应的数据源
		Result rs = CommonLibWordpatDAO.exist(brand, service, kbdataid, wordpat, serviceRoot);
		// 判断数据源为null或者数据量为0
		if (rs == null || rs.getRowCount() == 0) {
			return checkInfo;
		} else {
			// 定义存放模板的集合
			List<String> ls = new ArrayList<String>();
			List<String> lservice = new ArrayList<String>();
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取模板
				String wordpatstr = rs.getRows()[i].get("wordpat").toString();
				String servicestr = rs.getRows()[i].get("service").toString();
				// 将模板按照#拆分
				String patternarry[] = wordpatstr.split("#");
				// 获取返回值，并将返回值按照&拆分
				String returnvaluearry[] = patternarry[1].split("&");
				// 判断返回值的数组长度是否相等
				if (returnvalue.length == returnvaluearry.length) {
					// 将当前模板放入集合中
					ls.add(wordpatstr);
					lservice.add(servicestr);
				}
			}
			// 判断集合的个数是否大于0
			if (ls.size() > 0) {
				checkInfo = "知识文档：(" + lservice.get(0) + ") 下已存在";
				return checkInfo;
			} else {
				return checkInfo;
			}
		}
	}
}
