package com.knowology.km.action;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.km.access.UserManager;
import com.knowology.km.bll.InteractiveSceneDAO;
import com.knowology.km.bll.QuestionManageDAO;
import com.knowology.km.bll.ScenariosDAO;
import com.knowology.km.bll.WordClassDAO;
import com.knowology.km.bll.WorditemDAO;

public class InteractiveSceneAction extends BaseAction {
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
	private String city;
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
	private String currentnode;
	private String currentcitycode;
	private String userid;
	private String ioa;
	private String knocontent;
	private String province;
	private String question;
	private String citySelect;
	private String oldName;// 场景修改前名称
	private String scenariosName;// 场景名称
	private String issue;// 发布状态
	private String attrname;
	private String moveway;// 上移方式

	private String sign;
	private String statisticsCount;
	private String statisticsObj;
	private String statisticsObjValue;
	private String column;
	private String minValue;
	private String maxValue;
	private String strategy;
	private String robotID;
	private String robotName;

	public String execute() {
		if (!"".equals(m_request) && m_request != null) {
			// 解析参数
			JSONObject json = JSONObject.parseObject(m_request);
			type = json.getString("type");
			name = json.getString("name");
			weight = json.getString("weight");
			wordclass = json.getString("wordclass");
			wordclassid = json.getString("wordclassid");
			city = json.getString("city");
			city = city.replace("\"", "").replace("[", "").replace("]", "");
			infotalbepath = json.getString("infotalbepath");
			cityname = json.getString("cityname");
			interpat = json.getString("interpat");
			scenariosid = json.getString("scenariosid");
			itemmode = json.getString("itemmode");
			scenarioselementid = json.getString("scenarioselementid");
			oldweight = json.getString("oldweight");
			container = json.getString("container");
			citySelect = json.getString("cityselect");
			scenariosName = json.getString("scenariosName");
			issue = json.getString("issue");
		}
		if ("selectelementname".equals(type)) {// 分页查询当前的场景元素信息
			m_result = InteractiveSceneDAO.selectElementName(scenariosid, name, page, rows);
		} else if ("getweight".equals(type)) {// 查询当前问题要素下的优先级
			m_result = InteractiveSceneDAO.getWeight(scenariosid, weight);
		} else if ("insertelementname".equals(type)) {// 添加场景要素，并返回相应的信息
			m_result = InteractiveSceneDAO.insertElementName(scenariosid, scenariosName, infotalbepath, city, cityname,
					itemmode, name, interpat, weight, wordclass, container);
		} else if ("updartelementname".equals(type)) {
			m_result = InteractiveSceneDAO.updateElementName(scenariosid, scenariosName, scenarioselementid,
					infotalbepath, city, cityname, itemmode, name, interpat, weight, oldweight, wordclass, container);
		} else if ("deleteelementname".equals(type)) {// 删除场景要素，并返回相应的信息
			m_result = InteractiveSceneDAO.deleteElementName(name, scenarioselementid, weight, scenariosid,
					scenariosName);
		} else if ("insertelementvalue".equals(type)) {// 新场景要素值(词条)
			m_result = InteractiveSceneDAO.insertElementValue(name, wordclassid, wordclass);
		} else if ("queryelement".equals(type)) {// 查询当前场景要素组合信息
			m_result = InteractiveSceneDAO.queryElement(scenariosid);
		} else if ("insertrule".equals(type)) {// 添加规则
			m_result = InteractiveSceneDAO.insertSceneRules(scenariosid, scenariosName, conditions, weight, ruletype,
					ruleresponse, service, abs, ruleresponsetemplate, responsetype, city, excludedcity, questionobject,
					standardquestion, flag, copycity, userquestion, currentnode);
		} else if ("selectrule".equals(type)) {// 查询规则
			m_result = InteractiveSceneDAO.selectSceneRules(scenariosid, conditions, ruletype, weight, page, rows, city,
					belong, ruleresponse, issue, strategy);
		} else if ("deleterule".equals(type)) {// 删除规则
			m_result = InteractiveSceneDAO.deleteSceneRules(ruleid, scenariosName, currentcitycode, excludedcity);
		} else if ("updaterule".equals(type)) {// 更新规则
			m_result = InteractiveSceneDAO.updateSceneRules(scenariosid, scenariosName, conditions, weight, ruletype,
					ruleresponse, ruleid, excludedcity, city, service, abs, responsetype, ruleresponsetemplate,
					questionobject, standardquestion, userquestion, currentcitycode, currentnode);
		} else if ("addcolumn".equals(type)) {// 新增列
			m_result = InteractiveSceneDAO.addColumn(scenariosid, columns);
		} else if ("deletecolumn".equals(type)) {// 删除列
			m_result = InteractiveSceneDAO.deleteColumn(scenariosid, columns, ruletype);
		} else if ("getmenuitemsinfo".equals(type)) {// 获得交互选项相关信息
			m_result = InteractiveSceneDAO.getMenuitemsInfo(scenariosid, weight);
		} else if ("savemenuitemsinfo".equals(type)) {// 保存交互选项信息
			m_result = InteractiveSceneDAO.updateMenuitems(scenariosid, ruleid, interpat, interactiveoptions,
					customvalue, conditions, weight, ruletype, ruleresponse, excludedcity, city, responsetype,
					wordclassid);
		} else if ("insertmenuitems".equals(type)) {// 添加自定义词条
			m_result = InteractiveSceneDAO.insertMenuitems(customvalue, wordclassid);
		} else if ("createinteractivescenetree".equals(type)) {// 构造场景树
			m_result = InteractiveSceneDAO.createInteractiveSceneTree(scenariosid, citySelect);
		} else if ("createinteractivescenetreebyname".equals(type)) {// 模糊查询场景树
			m_result = InteractiveSceneDAO.getJsonScenariosByName(name);
		} else if ("addmenu".equals(type)) {// 添加场景
			m_result = InteractiveSceneDAO.addMenu(scenariosid, name);
		} else if ("addmenuCallOut".equals(type)) {// 添加场景
			m_result = addMenuCallOut();
		} else if ("updatedocname".equals(type)) {// 修改文档名称
			m_result = InteractiveSceneDAO.updateDocname(scenariosid, name);
		} else if ("getdocpath".equals(type)) {// 获得文档路径
			m_result = InteractiveSceneDAO.getDocname(scenariosid);
		} else if ("deletemenu".equals(type)) {// 删除场景
			m_result = InteractiveSceneDAO.deleteMenu(scenariosid, name);
		} else if ("addknoname".equals(type)) {// 添加知识名称
			m_result = InteractiveSceneDAO.addKnoName(name, knocontent);
		} else if ("createservicetree".equals(type)) {// 构造业务树
			m_result = InteractiveSceneDAO.createServiceTree(serviceid);
		} else if ("createabstractcombobox".equals(type)) {// 构造摘要下拉列表
			m_result = InteractiveSceneDAO.createAbstractCombobox(serviceid);
		} else if ("createscenarioscombobox".equals(type)) {// 构造场景名称下拉列表
			m_result = InteractiveSceneDAO.createScenariosCombobox(citySelect);
		} else if ("createcolumncombobox".equals(type)) {// 构造场景列下拉列表
			m_result = InteractiveSceneDAO.createColumnCombobox(scenariosid);
		} else if ("createdeletecolumncombobox".equals(type)) {// 构造需移除场景列下拉列表
			m_result = InteractiveSceneDAO.createDeleteColumnCombobox(scenariosid);
		} else if ("interactiveoptions".equals(type)) {// 构造交互选项下拉
			m_result = InteractiveSceneDAO.createInteractiveOptionsCombobox(wordclassid);
		} else if ("addrelation".equals(type)) {// 添加场景业务摘要对应关系
			m_result = InteractiveSceneDAO.addScenarios2kbdataRelation(scenariosid, name, service, serviceid, kbdataid,
					abs, query);
		} else if ("selectservicekbdatada".equals(type)) {// 分页查询场景关系信息
			m_result = InteractiveSceneDAO.selectSceneRelation(scenariosid, serviceid, kbdataid, query, page, rows);
		} else if ("deletescenerelation".equals(type)) {// 删除场景业务摘要对应关系
			m_result = InteractiveSceneDAO.deleteSceneRelation(scenerelationid, abs, service, name);
		} else if ("createserviceinfocombobox".equals(type)) {// 构造对应信息下拉列表
			m_result = InteractiveSceneDAO.createServiceInfoCombobox(scenariosid);
		} else if ("createattrvaluescombobox".equals(type)) {// 获取信息表下相应属性对应的内容
			m_result = InteractiveSceneDAO.getAttrValues(serviceid, column, city);
		} else if ("createattrnamecombobox".equals(type)) { // 获取属性名
			m_result = InteractiveSceneDAO.getAttrName(serviceid);
		} else if ("createtemplatecolumncombobox".equals(type)) {// 构造对应模板列下拉列表
			m_result = InteractiveSceneDAO.createTemplateColumnCombobox(scenariosid);
		} else if ("createtriggeractionnamecombobox".equals(type)) {// 构造触发动作下拉列表
			m_result = InteractiveSceneDAO.createTriggeractionNamecombobox(scenariosid);
		} else if ("createwordclasscombobox".equals(type)) {// 构造父类下拉框
			m_result = InteractiveSceneDAO.createWordclassCombobox();
		} else if ("moverule".equals(type)) {// 上移规则优先级
			m_result = InteractiveSceneDAO.moveRule(ruletype, city, weight, scenariosid, moveway);
		} else if ("createprovincecombobox".equals(type)) {// 获得省份下拉框
			m_result = InteractiveSceneDAO.getProvince();
		} else if ("createcitycombobox".equals(type)) {// 获得地市下拉框
			m_result = InteractiveSceneDAO.getCity(province);
		} else if ("testhitquestion".equals(type)) {
			m_result = InteractiveSceneDAO.testHitQuestion(question, province, city);
		} else if ("createlementcolumncombobox".equals(type)) {// 获得场景元素列下拉框
			m_result = InteractiveSceneDAO.createEmentColumnCombobox(scenariosid);
		} else if ("updaterulenlp".equals(type)) {
			m_result = InteractiveSceneDAO.UpdateRule();// 更新场景知识
		} else if ("editmenu".equals(type)) {
			m_result = InteractiveSceneDAO.editmenu(scenariosid, name, oldName);
		} else if ("getcustomer".equals(type)) {// 获得行业归属
//			m_result = InteractiveSceneDAO.getCustomer(userid,ioa);
			JSONObject jsonObj = new JSONObject();
			User user = UserManager.constructLoginUser(userid, ioa);
			session.put("accessUser", user);
			jsonObj.put("customer", user.getCustomer());
			m_result = jsonObj;
		} else if ("issue".equals(type)) {// 发布
			m_result = InteractiveSceneDAO.issue(scenariosid, service, city);
		} else if ("getIssueData".equals(type)) {
			m_result = InteractiveSceneDAO.getIssueData();
		} else if ("selectsemanticskeyword".equals(type)) {// 获得信息表列元素对应的语义关键字
			m_result = InteractiveSceneDAO.getSemanticsKeyWordName(serviceid);
		} else if ("createknonamecombobox".equals(type)) {// 获取信息表下docname
			m_result = InteractiveSceneDAO.getKnoName(serviceid, attrname);
		} else if ("reloadScenarios".equals(type)) {// 单独更新某个场景
			m_result = InteractiveSceneDAO.reloadScenarios(scenariosid);
		} else if ("deleteOnlineRule".equals(type)) {
			m_result = InteractiveSceneDAO.deleteOnlineRule(scenariosid, service, city);
		} else if ("getrobotconfig".equals(type)) {
			m_result = InteractiveSceneDAO.getrobotconfig();
		} else if ("getResConfig".equals(type)) {// 获取其他答案类型
			m_result = InteractiveSceneDAO.getResConfig();
		} else if ("savestatisticinfo".equals(type)) {// 保存统计关联
			m_result = InteractiveSceneDAO.savestatisticinfo(serviceid, sign, column, statisticsCount, statisticsObj,
					statisticsObjValue, minValue, maxValue);
		} else if ("deleteColumnStatisticInfo".equals(type)) {// 删除统计关联
			m_result = InteractiveSceneDAO.deleteColumnStatisticInfo(serviceid, sign, column);
		} else if ("getColumnStatisticInfo".equals(type)) {// 查看统计关联
			m_result = InteractiveSceneDAO.getColumnStatisticInfo(serviceid, sign, column);
		} else if ("getInteractiveElement".equals(type)) {// 获取交互要素下拉列表
			m_result = InteractiveSceneDAO.getInteractiveElement();
		} else if ("saveinteractiveelement".equals(type)) {// 保存交互要素
			m_result = InteractiveSceneDAO.saveinteractiveelement(scenariosid, scenariosName, wordclassid, name, weight,
					city, cityname);
		} else if ("getUrl".equals(type)) {// 要打开的页面
			m_result = InteractiveSceneDAO.getUrl(ioa);
		} else if ("getResponseType".equals(type)) {
			m_result = CommonLibMetafieldmappingDAO.getConfigValue("场景业务根对应关系配置", ioa);
		} else if ("copyrules".equals(type)) {// 批量复制规则
			m_result = InteractiveSceneDAO.copyrules(ruleid);
		} else if ("getStrategyType".equals(type)) {// 获取策略
			m_result = InteractiveSceneDAO.getStrategyType();
		}
		return "success";
	}
	
	private Object addMenuCallOut() {
		city="全国";
		//添加场景根节点
		JSONObject addMenu = new JSONObject();
		JSONObject res = (JSONObject) InteractiveSceneDAO.addMenu(scenariosid, name);
		if(res.getBoolean("success") != true)
		{
			addMenu.put("success", false);
			addMenu.put("msg", "添加场景根节点失败!");
			return addMenu;
		};	
		//添加场景子节点
		String fatherId= (String) res.get("id");
		String[] names = { name+"流程",name+"长时间未回复"};
		String[] childIds = new String[2];
		for (int i = 0; i < childIds.length; i++) 
		{
			res = (JSONObject)  InteractiveSceneDAO.addMenu(fatherId, names[i]);
			if(res.getBoolean("success") != true)
			{
				addMenu.put("success", false);
				addMenu.put("msg", "添加场景子节点失败!");
				return addMenu;
			};		
		   childIds[i] = res.getString("id");
		}
		
		addMenu.put("childIds", childIds);
		addMenu.put("childNames", names);
		
		if(createScenarioDatas(names[0],childIds[0]).getString("errorCode")!="0")
		{
			addMenu.put("success", false);
			addMenu.put("msg", res.getString("msg"));
			return addMenu;
		};
		res = CreateQuestions(childIds[0],name);
		if(res.getString("errorCode") != "0")
		{
			addMenu.put("success", false);
			addMenu.put("msg", res.getString("msg"));
			return addMenu;
		};	
		
		addMenu.put("success", true);
		addMenu.put("msg", "创建外呼场景成功!");
		return addMenu;
	}

	private JSONObject createScenarioDatas(String scName, String scenarioID) {
		JSONObject createSD = new JSONObject();
		JSONObject res = new JSONObject();
		if(StringUtils.isNotBlank(robotID) && StringUtils.isNotBlank(robotName)) {
			//实体机器人ID父类
			res  = (JSONObject) WordClassDAO.select("实体机器人ID父类", true, "全部"  , 0, 10);
			if(res.getInteger("total")<1)
			{
				createSD.put("errorCode", "300001");
				createSD.put("msg", "查询\"实体机器人ID父类\" classID 失败");
				return createSD;
			}
	
	  	    String wrdClassID = res.getJSONArray("root").getJSONObject(0).getString("wordclassid");
			res = (JSONObject) WorditemDAO.insert(robotID, (String)res.get("wordclass"), wrdClassID, "", true);
			if(res.getBooleanValue("success")!=true)
			{
				createSD.put("errorCode", "300002");
				createSD.put("msg", "查询\"实体机器人ID父类\" classID 失败");
				return createSD;
			}
			
			//添加参数配置
			res= (JSONObject) ScenariosDAO.configRobot(robotID,robotName,scenarioID);
			if(res.getBooleanValue("success")!=true)
			{
				createSD.put("errorCode", "300003");
				createSD.put("msg", "参数配置\"实体机器人ID\" 失败");
				return createSD;
			}
		}
		
		//添加基础词类
		StringBuffer wordclasses = new StringBuffer();
		wordclasses.append("sys"+scName+"上文:节点名父类").append("\n");
		wordclasses.append("sys"+scName+"用户回答父类").append("\n");
		wordclasses.append("sys"+scName+"信息收集父类");
		res= (JSONObject) WordClassDAO.insert(wordclasses.toString(), "当前商家");
		if(res.getBooleanValue("success")!=true)
		{
			createSD.put("errorCode", "300004");
			createSD.put("msg",  "添加基础词类:"+ wordclasses +"失败");
			return createSD;
		}
		
		//添加场景要素
		String[][] sceElements ={{"上文:节点名","sys"+scName+"上文:节点名父类"},
				{"用户回答","sys"+scName+"用户	回答父类"},
				{"理解状态","理解状态父类"},
				{"连续未理解次数","连续未理解次数父类"},
				{"robotID","实体机器人ID父类"},
				{"区分节点","sys"+scName+"上文:节点名父类"},
				{"信息收集重复次数","信息收集重复次数父类"},
				{"信息收集状态","信息收集状态父类"},
				{"是否获取到按键值","是否获取到按键值父类"},
				{"条件值","条件值父类"}};
		for (int i = 0; i < sceElements.length; i++) {			
			res = (JSONObject) InteractiveSceneDAO.insertElementName(scenarioID, scName, "", "全国", "全国", "勾选+自定义", 
					sceElements[i][0], "",(i+1)+"", sceElements[i][1].replace("\t", ""), "键值补全");
			if(res.getBooleanValue("success")!=true)
			{
				createSD.put("errorCode", "300005");
				createSD.put("msg",  "添加场景要素:"+ sceElements[i][0] +"失败");
				return createSD;
			}
			
		}		
		
		//添加场景其他规则
		String[][] otherrules= {{"true","SET(\"场景类型\",\"主场景\")","2"},
				{"连续未理解次数<6 and 用户回答=\"未理解\"","ADD(\"连续未理解次数\",\"1\")","2"},
				{"用户回答!=\"未理解\" and 连续未理解次数!=\"\"","SET(\"连续未理解次数\",\"\")","2"},
				{"理解状态=\"跳出\" and now是否末梢编码!=\"是\"","返回上一级()","5"}};

		for (int j = 0; j < otherrules.length; j++) {
			String resp = otherrules[j][0]  + "==>" + otherrules[j][1];
			res = (JSONObject) InteractiveSceneDAO.insertSceneRules(scenarioID, scName,
							conditions, weight, otherrules[j][2], resp, service, abs,
							resp, responsetype, city, excludedcity, questionobject,
							standardquestion, flag, copycity, userquestion,currentnode);
			if(res.getBooleanValue("success")!=true)
			{
				createSD.put("errorCode", "300006");
				createSD.put("msg", "添加场景其他规则:"+ resp +"失败");
				return createSD;
			}
		}
		
		createSD.put("errorCode", "0");
		createSD.put("msg", "插入场景规则相关数据成功!");
		return createSD;
	}

	private JSONObject CreateQuestions(String sceid,String presceName)
	{		
		JSONObject obj = new JSONObject();
		String robotId = ScenariosDAO.getSceneRobotID(sceid);
		String cityCode = StringUtils.isNotBlank(robotId) ? ScenariosDAO.getRobotCityCode(robotId):"全国";
		//创建问题库
		JSONArray root=(JSONArray) InteractiveSceneDAO.createServiceTree("");
		String rootid = root.getJSONObject(0).getString("id");
		JSONObject questionRoot = (JSONObject) QuestionManageDAO.createScenarios(rootid, presceName+"问题库");
		if(questionRoot.getBooleanValue("success") != true)
		{
			obj.put("errorCode", "200001");
			obj.put("msg", "添加"+ presceName+"问题库"+"失败");
			return obj;
		}
		
		String qsrootId = questionRoot.getString("serviceid");
		
		for(String serviceName : new String[]{"识别规则业务",presceName+"常见问题"}) 
		{
			JSONObject objtemp = (JSONObject) QuestionManageDAO.createScenarios(qsrootId, serviceName);
			if(objtemp.getBooleanValue("success") != true)
			{
				obj.put("errorCode", "200002");
				obj.put("msg", "添加"+ serviceName+"问题库"+"失败");
				return obj;
			}
		}
		
		// 创建标准问
		String serviceName = presceName + "流程";
		String qsServceid = "";
		JSONObject createSQ = new JSONObject();
		HashMap<String, String> questions = new HashMap<String, String>();
		//创建开场语问题库和标准问
		questionRoot = (JSONObject) QuestionManageDAO.createScenarios(qsrootId, serviceName);
		if(questionRoot.getBooleanValue("success") != true)
		{
			obj.put("errorCode", "200003");
			obj.put("msg", "添加"+ serviceName+"问题库"+"失败");
			return obj;
		}
		if(StringUtils.isNotBlank(robotId)) {
			//创建开场语标准问
			qsServceid = questionRoot.getString("serviceid");
			questions.put("开场语", "开启近类*场近类*[话语近类|消息近类]#无序#编者=\"自动添加"+presceName+"\"&针对问题=\"开场语\""+"&人工地市=\"是\"");
			createSQ = (JSONObject) QuestionManageDAO.createStandardQuestion(qsServceid,serviceName,questions,cityCode,this.httpRequest);
			if(createSQ.getBooleanValue("success") != true)
			{
				obj.put("errorCode", "200004");
				obj.put("msg", "添加"+ serviceName+"问题库标准问"+"失败");
				return obj;
			}
			//添加场景语义对应关系
			kbdataid = QuestionManageDAO.getKbdataId(qsServceid, serviceName, "开场语");
			abs = "<" + service + ">" + "开场语";
			JSONObject createSKR = (JSONObject) InteractiveSceneDAO.addScenarios2kbdataRelation(sceid, serviceName, serviceName,qsServceid, kbdataid,abs, "");
			if(createSKR.getBooleanValue("success") != true)
			{
				obj.put("errorCode", "200005");
				obj.put("msg", "添加场景语义对应关系失败");
				return obj;
			}
		}
		//创建长时间未回复问题库和标准问
		serviceName = "长时间未回复";
		questionRoot = (JSONObject) QuestionManageDAO.createScenarios(qsrootId, serviceName);
		if(questionRoot.getBooleanValue("success") != true)
		{
			obj.put("errorCode", "200006");
			obj.put("msg", "添加"+ serviceName+"问题库失败");
			return obj;
		}
		qsServceid = questionRoot.getString("serviceid");
		questions.clear();
		if(StringUtils.isNotBlank(robotId)) {
			questions.put("长时间未回复", "长近类*时间近类*没有近类*回复近类#无序#编者=\"自动添加"+presceName+"\"&针对问题=\"长时间未回复\"");
			createSQ = (JSONObject) QuestionManageDAO.createStandardQuestion(qsServceid,"长时间未回复",questions,cityCode,this.httpRequest);
			if(createSQ.getBooleanValue("success") != true)
			{
				obj.put("errorCode", "200007");
				obj.put("msg", "添加"+ serviceName+"问题库标准问失败");
				return obj;
			}
		}
		obj.put("errorCode", "0");
		obj.put("msg", "添加问题相关数据成功");
		return obj;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public String getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(String kbdataid) {
		this.kbdataid = kbdataid;
	}

	public String getKbcontentid() {
		return kbcontentid;
	}

	public void setKbcontentid(String kbcontentid) {
		this.kbcontentid = kbcontentid;
	}

	public String getWordclass() {
		return wordclass;
	}

	public void setWordclass(String wordclass) {
		this.wordclass = wordclass;
	}

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getElementnameid() {
		return elementnameid;
	}

	public void setElementnameid(String elementnameid) {
		this.elementnameid = elementnameid;
	}

	public String getElementvalueid() {
		return elementvalueid;
	}

	public void setElementvalueid(String elementvalueid) {
		this.elementvalueid = elementvalueid;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public String getReturntxttype() {
		return returntxttype;
	}

	public void setReturntxttype(String returntxttype) {
		this.returntxttype = returntxttype;
	}

	public String getReturntxt() {
		return returntxt;
	}

	public void setReturntxt(String returntxt) {
		this.returntxt = returntxt;
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

	public void setRows(int rows) {
		this.rows = rows;
	}

	public String getCombitionid() {
		return combitionid;
	}

	public void setCombitionid(String combitionid) {
		this.combitionid = combitionid;
	}

	public String getRuletype() {
		return ruletype;
	}

	public void setRuletype(String ruletype) {
		this.ruletype = ruletype;
	}

	public String getRuleresponse() {
		return ruleresponse;
	}

	public void setRuleresponse(String ruleresponse) {
		this.ruleresponse = ruleresponse;
	}

	public String getRuleid() {
		return ruleid;
	}

	public void setRuleid(String ruleid) {
		this.ruleid = ruleid;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getKbanswerid() {
		return kbanswerid;
	}

	public void setKbanswerid(String kbanswerid) {
		this.kbanswerid = kbanswerid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getImporttype() {
		return importtype;
	}

	public void setImporttype(String importtype) {
		this.importtype = importtype;
	}

	public String getExporttype() {
		return exporttype;
	}

	public void setExporttype(String exporttype) {
		this.exporttype = exporttype;
	}

	public String getScenariosid() {
		return scenariosid;
	}

	public void setScenariosid(String scenariosid) {
		this.scenariosid = scenariosid;
	}

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getScenerelationid() {
		return scenerelationid;
	}

	public void setScenerelationid(String scenerelationid) {
		this.scenerelationid = scenerelationid;
	}

	public String getInfotalbepath() {
		return infotalbepath;
	}

	public void setInfotalbepath(String infotalbepath) {
		this.infotalbepath = infotalbepath;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getItemmode() {
		return itemmode;
	}

	public void setItemmode(String itemmode) {
		this.itemmode = itemmode;
	}

	public String getIsshare() {
		return isshare;
	}

	public void setIsshare(String isshare) {
		this.isshare = isshare;
	}

	public String getInterpat() {
		return interpat;
	}

	public void setInterpat(String interpat) {
		this.interpat = interpat;
	}

	public String getM_request() {
		return m_request;
	}

	public void setM_request(String mRequest) {
		m_request = mRequest;
	}

	public String getScenarioselementid() {
		return scenarioselementid;
	}

	public void setScenarioselementid(String scenarioselementid) {
		this.scenarioselementid = scenarioselementid;
	}

	public String getOldweight() {
		return oldweight;
	}

	public void setOldweight(String oldweight) {
		this.oldweight = oldweight;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public String getExcludedcity() {
		return excludedcity;
	}

	public void setExcludedcity(String excludedcity) {
		this.excludedcity = excludedcity;
	}

	public String getInteractiveoptions() {
		return interactiveoptions;
	}

	public void setInteractiveoptions(String interactiveoptions) {
		this.interactiveoptions = interactiveoptions;
	}

	public String getCustomvalue() {
		return customvalue;
	}

	public void setCustomvalue(String customvalue) {
		this.customvalue = customvalue;
	}

	public String getRuleresponsetemplate() {
		return ruleresponsetemplate;
	}

	public void setRuleresponsetemplate(String ruleresponsetemplate) {
		this.ruleresponsetemplate = ruleresponsetemplate;
	}

	public String getResponsetype() {
		return responsetype;
	}

	public void setResponsetype(String responsetype) {
		this.responsetype = responsetype;
	}

	public String getBelong() {
		return belong;
	}

	public void setBelong(String belong) {
		this.belong = belong;
	}

	public String getQuestionobject() {
		return questionobject;
	}

	public void setQuestionobject(String questionobject) {
		this.questionobject = questionobject;
	}

	public String getStandardquestion() {
		return standardquestion;
	}

	public void setStandardquestion(String standardquestion) {
		this.standardquestion = standardquestion;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getCopycity() {
		return copycity;
	}

	public void setCopycity(String copycity) {
		this.copycity = copycity;
	}

	public String getUserquestion() {
		return userquestion;
	}

	public void setUserquestion(String userquestion) {
		this.userquestion = userquestion;
	}

	public String getCurrentcitycode() {
		return currentcitycode;
	}

	public void setCurrentcitycode(String currentcitycode) {
		this.currentcitycode = currentcitycode;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getIoa() {
		return ioa;
	}

	public void setIoa(String ioa) {
		this.ioa = ioa;
	}

	public String getKnocontent() {
		return knocontent;
	}

	public void setKnocontent(String knocontent) {
		this.knocontent = knocontent;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getCitySelect() {
		return citySelect;
	}

	public void setCitySelect(String citySelect) {
		this.citySelect = citySelect;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getScenariosName() {
		return scenariosName;
	}

	public void setScenariosName(String scenariosName) {
		this.scenariosName = scenariosName;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getAttrname() {
		return attrname;
	}

	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getStatisticsCount() {
		return statisticsCount;
	}

	public void setStatisticsCount(String statisticsCount) {
		this.statisticsCount = statisticsCount;
	}

	public String getStatisticsObj() {
		return statisticsObj;
	}

	public void setStatisticsObj(String statisticsObj) {
		this.statisticsObj = statisticsObj;
	}

	public String getStatisticsObjValue() {
		return statisticsObjValue;
	}

	public void setStatisticsObjValue(String statisticsObjValue) {
		this.statisticsObjValue = statisticsObjValue;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	public String getCurrentnode() {
		return currentnode;
	}

	public void setCurrentnode(String currentnode) {
		this.currentnode = currentnode;
	}

	public String getMoveway() {
		return moveway;
	}

	public void setMoveway(String moveway) {
		this.moveway = moveway;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getRobotID() {
		return robotID;
	}

	public void setRobotID(String robotID) {
		this.robotID = robotID;
	}

	public String getRobotName() {
		return robotName;
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

}
