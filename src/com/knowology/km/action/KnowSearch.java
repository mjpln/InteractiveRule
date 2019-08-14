package com.knowology.km.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.dal.Database;
import com.knowology.km.bll.QuestionUploadDao;
import com.knowology.km.util.GetSession;

public class KnowSearch extends BaseAction  {
	private String type;
	private Object m_result;

	private String abs;
	private String kbcontentid;
	private String kbdataid;
	private String wordclass;
	private String wordclassid;
	private String name;
	private String weight;
	private String oldweight;
	private String elementnameid;
	private String elementvalueid;
	private String conditions;
	private String returntxttype;
	private String returntxt;
	private int page;
	private int rows;
	private String combitionid;
	private String ruletype;
	private String ruleresponse;
	private String ruleid;
	private String answer;
	private String kbanswerid;
	private String status;
	private String filename;
	private String importtype;
	private String exporttype;

	private String scenariosid;
	private String serviceid;
	private String service;
	private String query;
	private String scenerelationid;

	private String infotalbepath;
	private static String city;
	private String cityname;
	private String itemmode;
	private String isshare;
	private String interpat;
	private String m_request;
	

	private String scenarioselementid;
	private String columns;
	private String excludedcity;
	private String interactiveoptions;
	private String customvalue;
	private String ruleresponsetemplate;
	private String responsetype;
	private String belong;

	private String questionobject;
	private String standardquestion;

	private String container;
	private String flag;
	private String copycity;
	private String userquestion;
	private String currentcitycode;
	private String userid;
	private String ioa;
	private String docName;

	
	public String getM_request() {
		return m_request;
	}
	public void setM_request(String m_request) {
		this.m_request = m_request;
	}
	
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public String getServiceid() {
		return serviceid;
	}
	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int row) {
		this.rows = row;
	}
	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	
	/**
	 * 定义全局 city字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();
	/**
	 *创建字典
	 */

	static {
		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");

		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
			}
		}

	}
	
	public static String getCities() {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		String cityCode = "";
		String cityName = "";
		String userCityCode ="";
		if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
			if(!"".equals(city) && city!=null){//判断页面传入city
				if("全国".equals(city)){
					cityCode=city;
					HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
					.resourseAccess(user.getUserID(), "scenariosrules", "S");
			      // 该操作类型用户能够操作的资源
			       cityList = resourseMap.get("地市");
			     if (cityList != null) {
			    	 userCityCode = cityList.get(0);
			     }	
				}else{
					cityCode= city;	
					cityName = cityCodeToCityName.get(city);	
				}
					
			}else{
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "scenariosrules", "S");
		      // 该操作类型用户能够操作的资源
		       cityList = resourseMap.get("地市");
		     if (cityList != null) {
			     cityCode = cityList.get(0);
			    cityName = cityCodeToCityName.get(cityList.get(0));
		     }	
		}
			
			
			
		}else{
			if(!"".equals(city)&&city!=null){
		    if(!"全国".equals(city)){
		    	cityCode= city;	
				cityName = cityCodeToCityName.get(city);	
		    }
			
			}
			
			
		}
		return "success";
	}
	
	//获取知识类型
	public String getKwTypes() {
		StringBuilder sql = new StringBuilder();
		sql.append("select serviceid,service from SERVICEATTRNAME2COLNUM where name='docName'");
		Result rs = Database.executeQuery(sql.toString());
		
		JSONArray types = new JSONArray();
		
	
		if (rs != null && rs.getRowCount() > 0) {
			
			int rowCount = rs.getRowCount();
			// 循环遍历数据源
			for (int i = 0; i < rowCount; i++) {
				
				// 定义json对象
				JSONObject obj = new JSONObject();
				SortedMap row = rs.getRows()[i];
				// 生成attrid对象
				obj.put("service", row.get("service").toString().replace("信息表", ""));
				obj.put("serviceid", row.get("serviceid").toString());
				// 将生成的对象放入jsonArr数组中
				types.add(obj);
			}
		}
		
		m_result = types;
		return "success";
	}
	
	//获取地市树
	public static Object getCityTree(String local) {
		String cityname[] = local.split(",");
		Map<String, String> map = new HashMap<String, String>();
		for (int m = 0; m < cityname.length; m++) {
			map.put(cityname[m], "");
		}
		JSONArray jsonAr = new JSONArray();

		Result rs = null;
		rs = CommonLibQuestionUploadDao.selProvince();
		if (null != rs && rs.getRowCount() > 0) {
			JSONObject allJsonObj = new JSONObject();
			allJsonObj.put("id", "全国");
			allJsonObj.put("text", "全国");
			if (map.containsKey("全国")) {
				allJsonObj.put("checked", true);
			}
			jsonAr.add(allJsonObj);
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String id = rs.getRows()[i].get("id").toString();
				String province = rs.getRows()[i].get("province").toString();
				Result innerRs = null;
				Result innerRs2 = null;
				if (province.indexOf("市") < 0) {
					innerRs = CommonLibQuestionUploadDao.getCityByTree(id);
				}
				// else {
				// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
				// }
				if (map.containsKey(province)) {
					jsonObj.put("checked", true);
				}
				JSONArray jsonArr = new JSONArray();
				if (null != innerRs && innerRs.getRowCount() > 0) {
					for (int j = 0; j < innerRs.getRowCount(); j++) {

						String cityId = innerRs.getRows()[j].get("id")
								.toString();
						// Result sinnerRs =
						// CommonLibQuestionUploadDao.getScity(cityId);
						// JSONArray sJsonArr = new JSONArray();
						JSONObject innerJsonObj = new JSONObject();
						// if (sinnerRs != null && sinnerRs.getRowCount() > 0){
						// for (int k = 0 ; k < sinnerRs.getRowCount() ; k++){
						// JSONObject sInnerJsonObj = new JSONObject();
						// sInnerJsonObj.put("id",
						// sinnerRs.getRows()[k].get("id"));
						// sInnerJsonObj.put("text",
						// sinnerRs.getRows()[k].get("city"));
						// if
						// (map.containsKey(sinnerRs.getRows()[k].get("city"))){
						// sInnerJsonObj.put("checked", true);
						// }
						// sJsonArr.add(sInnerJsonObj);
						// }
						// innerJsonObj.put("state", "closed");
						// }
						innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
						innerJsonObj.put("text", innerRs.getRows()[j]
								.get("city"));
						// innerJsonObj.put("children", sJsonArr);
						// if (local.equals(innerRs.getRows()[j].get("city"))){
						// innerJsonObj.put("checked", true);
						// }

						if (map.containsKey(innerRs.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
					for (int j = 0; j < innerRs2.getRowCount(); j++) {

						JSONArray sJsonArr = new JSONArray();

						JSONObject innerJsonObj = new JSONObject();
						innerJsonObj.put("id", innerRs2.getRows()[j].get("id"));
						innerJsonObj.put("text", innerRs2.getRows()[j]
								.get("city"));
						innerJsonObj.put("children", sJsonArr);
						if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				}
				jsonObj.put("id", rs.getRows()[i].get("id"));
				jsonObj.put("text", rs.getRows()[i].get("province"));
				jsonObj.put("children", jsonArr);
				jsonAr.add(jsonObj);
			}
		}
		// System.out.println(jsonAr);
		return jsonAr;
	}
	
	//获取分页数据
	public String getKWDatas() {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		List<String> cityList = new ArrayList<String>();
		String cityCode = "";
		if(!"".equals(city)&&city!=null){
			cityCode = city;
		}else{
			if (!"全行业".equals(customer)) {// 非全行业用户需通过地市查询所有的列新信息
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "scenariosrules", "S");
				// 该操作类型用户能够操作的资源
				cityList = resourseMap.get("地市");
				if (cityList != null) {
					cityCode = cityList.get(0);
				}
				
			}
		}

//		docName="漫游";
//		serviceid = "1831404";
//		city="140000";
		
		int start = (page - 1) * rows,
		    end   = page * rows;
		//获取docname 的attr id
		StringBuilder getDocNameAttrIdSql = new StringBuilder();
		getDocNameAttrIdSql.append("select COLUMNNUM from SERVICEATTRNAME2COLNUM where name='docName' and serviceid='"+serviceid+"'");
		Result docNameIdObj = Database.executeQuery(getDocNameAttrIdSql.toString());
		
		String docNameId = "";
		if (docNameIdObj != null && docNameIdObj.getRowCount() > 0) {
			docNameId = docNameIdObj.getRows()[0].get("COLUMNNUM").toString();
		}
		
		//由cityid查cityid 的attr id
		StringBuilder getCityIdAttrIdSql = new StringBuilder();
		getCityIdAttrIdSql.append("select COLUMNNUM from SERVICEATTRNAME2COLNUM where name='cityId' and serviceid='"+serviceid+"'");
		Result cityIdObj = Database.executeQuery(getCityIdAttrIdSql.toString());
		
		String cityId = "";
		if (cityIdObj != null && cityIdObj.getRowCount() > 0) {
			cityId = cityIdObj.getRows()[0].get("COLUMNNUM").toString();
		}
		
		//由city查cityName 的attr id
		StringBuilder getCityAttrIdSql = new StringBuilder();
		getCityAttrIdSql.append("select COLUMNNUM from SERVICEATTRNAME2COLNUM where name='cityName' and serviceid='"+serviceid+"'");
		Result cityObj = Database.executeQuery(getCityAttrIdSql.toString());
		
		String cityName = "";
		if (cityObj != null && cityObj.getRowCount() > 0) {
			cityName = cityObj.getRows()[0].get("COLUMNNUM").toString();
		}
	
		// 根据attr id查出 attr[id]和service
		StringBuilder getKwDataSql= new StringBuilder();
		getKwDataSql.append("select distinct attr"+docNameId+",service,attr"+cityName+",attr"+cityId+" from (select SI.attr"+docNameId+",S.service,SI.attr"+cityName+",SI.attr"+cityId+", ROWNUM RN  from SERVICEORPRODUCTINFO SI,service S where SI.serviceid='" + serviceid + "' and SI.serviceid = S.serviceid");///ghj  !!!
		//getKwDataSql.append("select distinct attr"+docNameId+",service,attr"+cityName+",attr"+cityId+" from (select SI.attr"+docNameId+",S.service,SI.attr"+cityName+",SI.attr"+cityId+" from SERVICEORPRODUCTINFO SI,service S where SI.serviceid='" + serviceid + "' and SI.serviceid = S.serviceid");

		if (!"".equals(cityCode) && cityCode != null) {
			getKwDataSql.append( " and SI.attr"+cityId+"='"+cityCode+"'");
		}
		if (!"".equals(docName) && docName != null) {
			getKwDataSql.append(" and SI.attr"+docNameId+" like '%"+docName+"%'");
		}
		getKwDataSql.append(") BIEMING");///ghj update
		
		JSONObject result = new JSONObject();
		
		Result rsTotal = Database.executeQuery(getKwDataSql.toString());
		
		result.put("total", rsTotal.getRowCount());
		
		getKwDataSql.append(" where rn>"+start+" and rn<="+end);
		
		Result rs = Database.executeQuery(getKwDataSql.toString());
		
		
		JSONArray rlts = new JSONArray();
		int rc =  rs.getRowCount();
		if (rs != null && rc > 0) {
			
			int rowCount = rc;
			// 循环遍历数据源
			for (int i = 0; i < rowCount; i++) {
				
				// 定义json对象
				JSONObject obj = new JSONObject();
				SortedMap row = rs.getRows()[i];
				// 生成attrid对象

				obj.put("docName", row.get("attr" + docNameId).toString());
				obj.put("service", row.get("service").toString().replace("信息表", ""));
				obj.put("city", row.get("attr" + cityName).toString());
				obj.put("cityCode", row.get("attr" + cityId).toString());
				// 将生成的对象放入jsonArr数组中
				rlts.add(obj);
			}
		}
		
		result.put("rows", rlts);
		//System.out.print(rlts);
		m_result = result;
		//地市
		return "success";
	}

	
}
