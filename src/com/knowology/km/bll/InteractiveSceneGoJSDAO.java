package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import oracle.sql.CLOB;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.pojo.Link;
import com.knowology.km.pojo.Node;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

public class InteractiveSceneGoJSDAO {
	private static Logger logger = Logger.getLogger("InteractiveSceneGoJSDAO");
	private static JSONArray nodeDataArray;// 存放节点数据
	private static JSONArray linkDataArray;// 存放节点关系数据
	private static Map<String, String> standardQuestionMap;// 存放标准问题
	private static Map<String, Node> nodes = new HashMap<String, Node>();// 节点对象集合
	private static List<Link> links = new ArrayList<Link>();// 线对象集合

	/**
	 * 定义全局 city字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();

	/**
	 * 定义全局 cityNameToCityCode 字典
	 */
	public static Map<String, String> cityNameToCityCode = new HashMap<String, String>();

	/**
	 * 创建字典
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
				cityNameToCityCode.put(key, value);
			}
		}

	}

	/**
	 * 将字符串转化为json格式解析json串
	 * 
	 * @param m_request
	 *            json格式的字符串
	 * @return json格式的数据
	 */
	public static Object parseJsonFromStrAndInsert(String m_request,
			String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONObject json = JSONObject.parseObject(m_request);
		nodeDataArray = json.getJSONArray("nodeDataArray");
		linkDataArray = json.getJSONArray("linkDataArray");
		

		// 初始化节点对象
		initNodeAndLink();

		// 解析数据(新)
		List<List<Node>> relationNodes = new ArrayList<List<Node>>();
		for (int i = 0; i < nodeDataArray.size(); i++) {
			String category = nodeDataArray.getJSONObject(i).getString("category");			
			if (category.equals("Normal") || category.equals("Start")) {
				List<Node> nodeLists = new ArrayList<Node>();
				String key = nodeDataArray.getJSONObject(i).getString("key");
				Node node = nodes.get(key);
				parseData_(node, nodeLists, relationNodes, node.getKey());
			}
		}
		
		// 对数据进行排序
		List<List<Node>> relationNodesSort = new ArrayList<List<Node>>();
		for (List<Node> list : relationNodes) {
			Node node = list.get(list.size() - 1);
			String nodeName = list.get(0).getKey();
			if (nodeName.equals("Start")) {
				relationNodesSort.add(list);
				sortRelationNodes(node, relationNodes, relationNodesSort);
			}
		}
		
		// 上文节点有多个时，上文节点名通过【|||】连接起来
		List<List<Node>> relationNodesList = new ArrayList<List<Node>>();
		int sameNodeIndex = -1;
		// 错位循环节点集合
		for (int i = 0; i < relationNodesSort.size(); i++) {
			List<Node> prev = relationNodesSort.get(i);
			Node prevNode1 = prev.get(0);
			Node prevNode2 = prev.get(1);
			Node prevNode3 = prev.get(2);
			for (int j = i + 1; j < relationNodesSort.size(); j++) {
				List<Node> next = relationNodesSort.get(j);
				Node nextNode1 = next.get(0);
				Node nextNode2 = next.get(1);
				Node nextNode3 = next.get(2);
				if (!prevNode1.getKey().equals(nextNode1.getKey())
						&& prevNode2.getKey().equals(nextNode2.getKey())
						&& prevNode3.getKey().equals(nextNode3.getKey())) {
					// 节点key
					Node newNode = prevNode1;
					List<String> keyList = new ArrayList<String>();
					keyList.add(prevNode1.getKey());
					keyList.add(nextNode1.getKey());
					newNode.setCombKey(StringUtils.join(keyList, "|||"));
					prev.set(0, newNode);
					sameNodeIndex = j;// 获取当前有多个上文节点的节点下标
				}
			}
			if (i != sameNodeIndex) {// 有多个上文节点时去掉多出来的节点
				relationNodesList.add(prev);
			}
		}
		
		// 查找当前场景是否有数据
		jsonObj = (JSONObject) loadData(scenariosid);
		boolean update = false;
		// 有数据先删除在新增
		if (jsonObj.getBooleanValue("success")) {
			List<String> listSqls = new ArrayList<String>();
			List<List<?>> listParams = new ArrayList<List<?>>();
			String deleteSql = "delete from scenariosrules where currentnode like 'Normal%' and relationserviceid=?";
			listSqls.add(deleteSql);
			List<String> Params = new ArrayList<String>();
			Params.add(scenariosid);
			listParams.add(Params);
			Database.executeNonQueryTransaction(listSqls, listParams);
			update = true;
		}
		if (relationNodesList.size() > 0) {
			jsonObj.clear();
			jsonObj = (JSONObject) insertRuleNew(relationNodesList, scenariosid);
		}
		if (jsonObj.getBooleanValue("success")) {
			jsonObj.clear();
			// 有数据修改，没数据新增
			if (update) {
				jsonObj = (JSONObject) updataRuleData(m_request, scenariosid);
			} else {
				jsonObj = (JSONObject) addRuleData(m_request, scenariosid);
			}
		}
		return jsonObj;
	}

	/**
	 * @function 解析数据
	 * @param node
	 *            节点
	 * @param nodeLists
	 *            关系节点
	 * @param relationNodes
	 *            所有关系节点
	 */
	public static void parseData_(Node node, List<Node> nodeLists,
			List<List<Node>> relationNodes, String currentNode) {
		// 添加节点集合
		nodeLists.add(node);
		// 节点为一标识
		String key = node.getKey();
		if (key.contains("Normal") && !currentNode.equals(key)) {
			relationNodes.add(nodeLists);
			return;
		}
		// 子节点集合
		HashSet<Node> childNodes = node.getChilds();
		// 子节点不为空
		if (childNodes.size() > 0) {
			// 遍历子节点
			Iterator<Node> childNodeIter = childNodes.iterator();
			int i = 0;
			while (childNodeIter.hasNext()) {
				// 子节点
				Node childNode = childNodeIter.next();
				List<Node> nodeListsCopy = new ArrayList<Node>();
				nodeListsCopy.addAll(nodeLists);
				// 递归
				parseData_(childNode, nodeListsCopy, relationNodes, currentNode);

				if (i > 0) {
					nodeLists.remove(nodeLists.size() - 1);
				}
				i++;
			}
		} else {
			// 没有子节点跳出
			return;
		}
	}

	/**
	 * 添加规则
	 * 
	 * @param m_request
	 * @return
	 */
	public static Object addRuleData(String m_request, String scenariosid) {
		JSONObject jsonO = new JSONObject();
		String sql = "insert into scene_configuration(scenejsondata, RELATIONSERVICEID) values(?,?)";

		int result = Database.executeNonQuery(sql, m_request, scenariosid);
		if (result > 0) {
			jsonO.put("success", true);
			jsonO.put("message", "提交成功");
		} else {
			jsonO.put("success", false);
			jsonO.put("message", "提交失败");
		}
		return jsonO;
	}

	/**
	 * 修改规则
	 * 
	 * @param m_request
	 * @return
	 */
	public static Object updataRuleData(String m_request, String scenariosid) {
		JSONObject jsonO = new JSONObject();
		List<String> listSqls = new ArrayList<String>();
		List<List<?>> listParams = new ArrayList<List<?>>();
		String sql = "update scene_configuration set scenejsondata = ? where relationserviceid = ?";
		listSqls.add(sql);
		List<String> params = new ArrayList<String>();
		params.add(m_request);
		params.add(scenariosid);
		listParams.add(params);
		int result = Database.executeNonQueryTransaction(listSqls, listParams);
		if (result > 0) {
			jsonO.put("success", true);
			jsonO.put("message", "提交成功");
		} else {
			jsonO.put("success", false);
			jsonO.put("message", "提交失败");
		}
		return jsonO;
	}

	/**
	 * @function 拼接sql语句和准备数据执行插入数据
	 * @param paseDatas流程图关系数据
	 * @param scenariosid场景id
	 * @return
	 */
	public static Object insertRuleNew(List<List<Node>> paseDatas,
			String scenariosid) {
		// 返回数据
		JSONObject jsonObj = new JSONObject();
		List<String> listSqls = new ArrayList<String>();
		List<List<?>> listParams = new ArrayList<List<?>>();
		StringBuffer sql = new StringBuffer();
		String valueStr = "";
		// 获取用户
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取服务类别
		String serviceType = user.getIndustryOrganizationApplication();

		// 存放场景要素表中的数据
		List<List<String>> scenariosElementName = new ArrayList<List<String>>();

		// 查询用户回答和标准问题
		getStandardQuestion();

		// 查询场景要素
		Result rs = CommonLibInteractiveSceneDAO.getElementName(scenariosid, "", 1, 100);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				List<String> dataList = new ArrayList<String>();
				dataList.add((String) rs.getRows()[i].get("name"));
				dataList.add((String) rs.getRows()[i].get("weight").toString());
				scenariosElementName.add(dataList);
			}
		}
		rs = null;
		// 查询地市信息
		String city = "";
		String cityName = "";
		String sqlStr = "select * from service where serviceid =" + scenariosid;
		logger.info("查询地市信息的sql=" + sqlStr);
		rs = Database.executeQuery(sqlStr);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				city = (String) rs.getRows()[i].get("city");
			}
		}
		cityName = cityCodeToCityName.get(city);

		// 获取weight值
		// weights用来保存paseDatas对应的值
		List<Integer> weights = new ArrayList<Integer>();
		// key用来保存paseDatas对应的key
		List<String> key = new ArrayList<String>();
		for (int i = 0; i < paseDatas.size(); i++) {
			List<Node> list = paseDatas.get(i);
			// 如果类别为Start则weights保存0
			if (list.get(0).getKey().contains("Start")) {
				// 保存0
				weights.add(0);
				// 保存对应节点key
				key.add(list.get(list.size() - 1).getKey());
			} else if (list.get(0).getKey().contains("Normal")) {// 如果类别为Normal则找到他的上文节点对应的weights进行加一
				for (int j = 0; j < key.size(); j++) {
					if (list.get(0).getKey().equals(key.get(j))) {
						int weight = weights.get(j);
						weight += 1;
						// 保存weight
						weights.add(weight);
						// 保存当前节点key
						key.add(list.get(list.size() - 1).getKey());
						break;
					}
				}
			}
		}
		int index = 0;
		for (List<Node> list : paseDatas) {
			// 用于标识是否是语义理解规则
			boolean isSemanticUnderstandingRules = false;
			String standardquestion = "";
			// 遍历数据如果存在用户回答设置isSemanticUnderstandingRules为true
			for (int k = 0; k < list.size(); k++) {
				Node combData = list.get(k);
				if (combData.getKey().contains("Judge")) {
					if (combData.getText().equals("用户回答")) {
						isSemanticUnderstandingRules = true;
						Node childNode = list.get(k + 1);
						List<Link> link = combData.getLinks();
						String to = "";
						for (Link lk : link) {
							to = lk.getTo();
							if (childNode.getKey().equals(to)) {
								standardquestion = lk.getText();
							}
						}
						break;
					}
				}
			}
			List<Object> listParam = new ArrayList<Object>();
			// 记录参数个数
			int paramCount = 0;
			sql.append("insert into SCENARIOSRULES (ruleid,relationserviceid");
			// 获取ruleid对应的值
			String ruleid = "";
			String bussinessFlag = CommonLibMetafieldmappingDAO
					.getBussinessFlag(serviceType);
			if (GetConfigValue.isOracle) {
				ruleid = ConstructSerialNum.GetOracleNextValNew(
						"seq_scenariosrules_id", bussinessFlag);
			} else if (GetConfigValue.isMySQL) {
				ruleid = ConstructSerialNum.getSerialIDNew("scenariosrules",
						"ruleid", bussinessFlag);
			}
			listParam.add(ruleid);
			listParam.add(scenariosid);
			int i = 0;
			// 用于标识ruletype对应的list位置
			int ruletypeIndex = 0;
			int standquestionIndex = 0;
			int questionobectIndex = 0;
			int ruleresponseIndex = 0;
			int ruleresponseTemplateIndex = 0;
			String ruleResponse = "";
			String ruleNodeName = "";
			for (Node data : list) {
				if (data.getKey().contains("Start")) {
					sql.append(",userquestion");
					listParam.add(data.getText());
					paramCount++;
				}
				if (data.getKey().contains("Judge")) {
					String elementName = data.getText();
					for (int j = 0; j < scenariosElementName.size(); j++) {
						String name = scenariosElementName.get(j).get(0);
						if (name.equals(elementName)) {
							sql.append(",condition" + scenariosElementName.get(j).get(1));
						}
					}
					Node childNode = list.get(i + 1);
					List<Link> links = data.getLinks();
					for (Link link : links) {
						if (link.getTo().equals(childNode.getKey())) {
							String text = link.getText();
							if (text.equals("<用户未选或未告知,系统提示输入>")) {
								text = "交互";
							} else if (text.equals("<用户告知或已选择，系统可获知>")) {
								text = "已选";
							} else if (text.equals("<用户未选或未告知>")) {
								text = "缺失";
							}
							listParam.add(text);
							break;
						}
					}
					paramCount++;
				}
				if (data.getKey().contains("Normal")) {
					ruleNodeName = data.getKey();
					if (i == 0) {// 获取上文节点数据
						int count = 0;
						for (int j = 0; j < scenariosElementName.size(); j++) {// 遍历scenariosElementName获取上文：节点名对应的列名
							String name = scenariosElementName.get(j).get(0);
							if (name.equals("上文:节点名")) {// 存在上文：节点名，拼sql
								sql.append(",condition" + scenariosElementName.get(j).get(1));
							} else {
								count++;
							}
						}
						if (count == scenariosElementName.size()) {
							jsonObj.put("success", false);
							jsonObj.put("message", "场景要素缺失【上文:节点名】，请补全");
							return jsonObj;
						}
						String combNormalKey = data.getCombKey();// 获取对应的上文节点名
						if (StringUtils.isEmpty(combNormalKey)) {
							combNormalKey = data.getKey();
						}
						listParam.add(combNormalKey);// 插入列对应的参数
						paramCount++;
					} else {// 拼回复内容sql，获取参数
												
						// 如果是语义理解规则ruletype = 3
						if (isSemanticUnderstandingRules) {
							sql.append(",ruleresponse");
							listParam.add("信息补全(\"用户回答\",\"上文\")");
							paramCount++;
							ruleresponseIndex = paramCount + 1;
							ruleResponse = data.getResponse();
							
							sql.append(",ruletype");
							listParam.add(3);
							paramCount++;
							ruletypeIndex = paramCount + 1;
														
							sql.append(",standardquestion");
							listParam.add("<识别规则业务>"+standardquestion);
							paramCount++;
							standquestionIndex = paramCount + 1;
							sql.append(",questionobject");
							listParam.add("识别规则业务");
							paramCount++;
							questionobectIndex = paramCount + 1;
							
							sql.append(",ruleresponsetemplate");
							listParam.add("信息补全(\"用户回答\",\"上文\")");
							paramCount++;
							ruleresponseTemplateIndex = paramCount + 1;
						} else {
							sql.append(",ruleresponse");
							listParam.add("SET(\"节点名\",\""+ruleNodeName+"\");"+data.getResponse());
							paramCount++;
							
							sql.append(",ruletype");
							listParam.add(0);
							paramCount++;
							
							sql.append(",ruleresponsetemplate");
							listParam.add("SET(\"节点名\",\""+ruleNodeName+"\");"+data.getRuleresponsetemplate());
							paramCount++;
						}
						// 添加当前结点
						sql.append(",currentnode");
						listParam.add(data.getKey());					
						paramCount++;
						sql.append(",responsetype");
						listParam.add(data.getResponsetype());
						paramCount++;						
					}
				}
				i++;
			}
			// 添加weight和值
			sql.append(",weight");
			listParam.add(weights.get(index));
			paramCount++;
			// 添加city，cityname和值
			sql.append(",city,cityname,isedit");
			listParam.add(city);
			paramCount++;
			listParam.add(cityName);
			paramCount++;
			listParam.add(1);
			paramCount++;
			sql.append(") values(?,?");
			for (int j = 0; j < paramCount; j++) {
				sql.append(",?");
			}
			sql.append(")");
			listSqls.add(sql.toString());
			listParams.add(listParam);
			// 如果这条数据是语义理解规则，则复制一份数据插入到场景配置中
			if (isSemanticUnderstandingRules) {
				List<Object> listParamCopy = new ArrayList<Object>();
				for (Object object : listParam) {
					listParamCopy.add(object);
				}
				if (GetConfigValue.isOracle) {
					ruleid = ConstructSerialNum.GetOracleNextValNew(
							"seq_scenariosrules_id", bussinessFlag);
				} else if (GetConfigValue.isMySQL) {
					ruleid = ConstructSerialNum.getSerialIDNew(
							"scenariosrules", "ruleid", bussinessFlag);
				}
				listSqls.add(sql.toString());
				// 将ruletype值设置为0
				listParamCopy.set(ruletypeIndex, 0);
				listParamCopy.set(standquestionIndex, null);
				listParamCopy.set(questionobectIndex, null);
				listParamCopy.set(ruleresponseIndex, "SET(\"节点名\",\""+ruleNodeName+"\");"+ruleResponse); 
				listParamCopy.set(ruleresponseTemplateIndex, "SET(\"节点名\",\""+ruleNodeName+"\");"+ruleResponse); 
				
				// 给ruleid赋新值
				listParamCopy.set(0, ruleid);				
				listParams.add(listParamCopy);
			}
			logger.info("场景配置插入数据sql=" + sql.toString() + "参数=" + listParam);
			// 清空sql
			sql = new StringBuffer();
			index++;
		}
		int excuteResult = Database.executeNonQueryTransaction(listSqls,
				listParams);
		if (excuteResult > 0) {
			jsonObj.put("success", true);
			jsonObj.put("message", "数据插入成功！");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("message", "抱歉数据插入失败！");
		}
		return jsonObj;
	}

	/**
	 * 分页查询满足条件的问题要素信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param kbcontentid参数kbcontentid
	 * @param name参数问题要素名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object selectElementName(String scenariosid, String name) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		List<Object> lstpara = new ArrayList<Object>();
		// 定义分页查询满足条件的场景要素名称SQL语句
		sql.append("select q.*,(select wordclass from wordclass where wordclassid=q.wordclassid) wordclass from scenarioselement q where q.relationserviceid = ?  ");
		// 定义绑定参数集合
		lstpara = new ArrayList<Object>();
		// 绑定摘要id参数
		lstpara.add(scenariosid);
		// 判断问题要素名称是否为null，空
		if (name != null && !"".equals(name)) {
			// 加上问题要素名称查询条件
			sql.append(" and q.name = ? ");
			// 绑定问题要素名称参数
			lstpara.add(name);
		}
		sql.append(" order by q.weight asc");
		// 执行SQL语句，获取相应的数据源
		Result rs = Database.executeQuery(sql.toString(), lstpara.toArray());
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				obj.put("id", rs.getRows()[i].get("name"));
				obj.put("text", rs.getRows()[i].get("name"));
				// 生成id对象
				obj.put("scenarioselementid", rs.getRows()[i].get("scenarioselementid"));
				// 生成name对象
				obj.put("name", rs.getRows()[i].get("name"));
				// 生成weight对象
				obj.put("weight", rs.getRows()[i].get("weight"));
				// 生成wordclassid对象
				obj.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				// 生成wordclass对象
				obj.put("wordclass", rs.getRows()[i].get("wordclass"));
				// 生成信息表列名对应对象
				obj.put("infotalbepath", rs.getRows()[i].get("infotalbepath"));
				// 生成是否共享对象
				obj.put("isshare", rs.getRows()[i].get("isshare"));
				// 生成城市对象
				obj.put("city", rs.getRows()[i].get("city"));
				obj.put("cityname", rs.getRows()[i].get("cityname"));

				// 生成交互模板对象
				obj.put("interpat", rs.getRows()[i].get("interpat"));
				// 生成选项填写方式对象
				obj.put("itemmode", rs.getRows()[i].get("itemmode"));
				// 生成场景ID对象
				obj.put("scenariosid", rs.getRows()[i].get("relationserviceid"));
				obj.put("container", rs.getRows()[i].get("container"));
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		// 将jsonArr数组放入jsonObj的root对象中
		jsonObj.put("rows", jsonArr);
		return jsonArr;
	}

	/**
	 * 加载场景配置数据初始化流程图
	 * 
	 * @param scenariosid
	 *            场景id
	 * @return 返回场景数据
	 */
	public static Object loadData(String scenariosid) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		String sql = "select * from scene_configuration where relationserviceid="
				+ scenariosid;
		logger.info("加载数据执行sql=" + sql);
		Result result = Database.executeQuery(sql);
		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				obj.put("id", result.getRows()[i].get("sceneid"));
				obj.put("relationserviceid",
						result.getRows()[i].get("relationserviceid"));
				String sceneJsonData = MyUtil.oracleClob2Str((CLOB) result
						.getRows()[i].get("scenejsondata"));
				obj.put("scenejsondata", sceneJsonData);
				jsonArr.add(obj);
			}
			jsonObj.put("success", true);
			jsonObj.put("rowdata", jsonArr);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}
	
	/**
	 * 新增场景要素信息
	 * 
	 * @param scenariosid 场景ID
	 * @param scenariosName 场景名称
	 * @param name 场景要素名称
	 * @param weight 优先级
	 * @param wordclass 词类名称
	 * @param city 地市编码
	 * @param cityname 地市名称
	 * @param infotalbepath 对应信息表
	 * @param itemmode 选项填写方式
	 * @param container 归属
	 * @param interpat 交互模板
	 * @return
	 */
	public static Object insertScenariosElement(String scenariosid, String scenariosName, String name, String weight, String wordclass, String city, String cityname, 
			String infotalbepath, String itemmode, String container, String interpat) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		
		int c = CommonLibInteractiveSceneDAO.insertElementName(user, scenariosid,
				 scenariosName, infotalbepath, city, cityname, itemmode, name, interpat,
				 weight, wordclass, serviceType, container);
		// 判断事务处理的结果
		if (c == -2) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "对应词类库中不存在!");
		} else if (c == -3) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景要素名称已存在!");
		} else {
			if (c > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "添加成功!");
			} else {
				jsonObj.put("success", true);
				jsonObj.put("msg", "添加失败!");
			}
		}
		return jsonObj;
	}

	/**
	 * 编辑场景要素信息
	 * 
	 * @param scenariosid 场景ID
	 * @param scenariosName 场景名称
	 * @param name 场景要素名称
	 * @param weight 优先级
	 * @param oldweight 旧优先级
	 * @param wordclass 词类名称
	 * @param city 地市编码
	 * @param cityname 地市名称
	 * @param infotalbepath 对应信息表
	 * @param itemmode 选项填写方式
	 * @param container 归属
	 * @param interpat 交互模板
	 * @param scenarioselementid 场景元素ID
	 * @return
	 */
	public static Object updateScenariosElement(String scenariosid, String scenariosName, String name, String weight, String oldweight, String wordclass, String city, String cityname, 
			String infotalbepath, String itemmode, String container, String interpat, String scenarioselementid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.updateElementName(user, scenariosid,
				scenariosName, scenarioselementid, infotalbepath, city, cityname, 
				itemmode, name, interpat, weight, oldweight, wordclass, serviceType,
				container);
		// 判断事务处理的结果
		if (c == -2) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "对应词类库中不存在!");
		} else if (c == -3) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "场景要素名称已存在!");
		} else {
			if (c > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "修改成功!");
			} else {
				jsonObj.put("success", true);
				jsonObj.put("msg", "修改失败!");
			}
		}
		return jsonObj;
	}
	
	/**
	 * 删除场景要素信息
	 * 
	 * @param scenariosid 场景ID
	 * @param scenariosName 场景名称
	 * @param name 场景要素名称
	 * @param weight 优先级
	 * @param scenarioselementid 场景元素ID
	 * @return
	 */
	public static Object deleteScenariosElement(String scenariosid, String scenariosName, String name, String weight, String scenarioselementid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int c = CommonLibInteractiveSceneDAO.deleteElementName(user, serviceType, name, scenarioselementid, weight, scenariosid, scenariosName);
		// 判断事务处理的结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将场景要素删除成功放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将场景要素删除失败放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
	
	/**
	 * 获取用户回答和标准问题
	 * 
	 * @return
	 */
	public static Object getStandardQuestion() {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		// 获取用户
		Object obj = GetSession.getSessionByKey("accessUser");
		User user = (User) obj;
		// 获取商家
		String brand = user.getBrand();
		brand = brand.replace("问题库", "");
		brand = "%" + brand + "%";
		String sql = "select k.abstract as abs from service s,kbdata k where s.service='识别规则业务' and brand like ? and s.serviceid = k.serviceid";
		Result rs = Database.executeQuery(sql, brand);
		standardQuestionMap = new HashMap<String, String>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义json对象
				JSONObject jsonO = new JSONObject();
				String abstractStr = (String) rs.getRows()[i].get("abs");
				String userAnswer = abstractStr.substring(abstractStr
						.indexOf(">") + 1);
				jsonO.put("abstractStr", abstractStr);
				standardQuestionMap.put(userAnswer, abstractStr);
				jsonArr.add(jsonO);
			}
			jsonObj.put("success", true);
			jsonObj.put("rows", jsonArr);
		} else {
			jsonObj.put("success", false);
			jsonObj.put("message", "抱歉，没查到数据！");
		}
		return jsonObj;
	}

	public static void initNodeAndLink() {
		// 初始化节点对象
		nodes = new HashMap<String, Node>();
		for (int i = 0; i < nodeDataArray.size(); i++) {
			Node node = new Node();
			node.setKey(nodeDataArray.getJSONObject(i).getString("key"));
			node.setCategory(nodeDataArray.getJSONObject(i).getString("category"));
			node.setResponse(nodeDataArray.getJSONObject(i).getString("response"));
			node.setResponsetype(nodeDataArray.getJSONObject(i).getString("responsetype"));
			node.setRuleresponsetemplate(nodeDataArray.getJSONObject(i).getString("ruleresponsetemplate"));
			node.setText(nodeDataArray.getJSONObject(i).getString("text"));
			// 添加节点对象
			nodes.put(node.getKey(), node);
		}
		// 初始化连接线对象
		links = new ArrayList<Link>();
		for (int i = 0; i < linkDataArray.size(); i++) {
			Link link = new Link();
			link.setFrom(linkDataArray.getJSONObject(i).getString("from"));
			link.setTo(linkDataArray.getJSONObject(i).getString("to"));
			link.setText(linkDataArray.getJSONObject(i).getString("text"));
			// 添加连接线对象
			links.add(link);
		}
		Iterator<String> iter = nodes.keySet().iterator();
		for (; iter.hasNext();) {
			HashSet<Node> childs = new HashSet<Node>();
			List<Link> childLinks = new ArrayList<Link>();
			String key = iter.next();
			for (int j = 0; j < links.size(); j++) {
				Link link = links.get(j);
				String from = link.getFrom();
				String to = link.getTo();
				if (from.equals(key)) {
					childs.add(nodes.get(to));
					childLinks.add(link);
				}
			}
			Node node = nodes.get(key);
			node.setChilds(childs);
			node.setLinks(childLinks);
		}
	}

	public static void getRelationList() {
		Iterator<String> iterator = nodes.keySet().iterator();
		for (; iterator.hasNext();) {
			Node node = nodes.get(iterator.next());
		}
	}

	/**
	 * @function 关系数据排序
	 * @param relationNodesSort
	 *            排序后的关系数据
	 */
	public static void sortRelationNodes(Node node,
			List<List<Node>> relationNodes, List<List<Node>> relationNodesSort) {
		for (List<Node> nodeLists : relationNodes) {
			Node firstNode = nodeLists.get(0);
			Node endNode = nodeLists.get(nodeLists.size() - 1);
			if (node == firstNode) {
				if (!relationNodesSort.contains(nodeLists)) {
					relationNodesSort.add(nodeLists);
				}
				sortRelationNodes(endNode, relationNodes, relationNodesSort);
			}
		}
	}
	
	/**
	 * 规则检查
	 * 
	 * @param response 规则
	 * @return
	 */
	public static String responseCheck(String response) {
		if (response.indexOf("命中问题") > -1 && response.indexOf(")<!--") > -1) {
			response = response.substring(0, response.lastIndexOf(")<!--") + 1);
		}
		response = response.trim();
		return response;
	}
	
	/**
	 * 获取流程图URL地址
	 * 
	 * @param ioa
	 * @return
	 */
	public static Object getUrl(String ioa) {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("场景页面配置TEST", ioa);
		if (rs != null && rs.getRowCount() > 0 ){
			jsonObj.put("success", true);
			jsonObj.put("url", rs.getRows()[0].get("name").toString());
		}else {
			jsonObj.put("success", true);
			jsonObj.put("url", "./scenariosByGoJS.html.html");
		}
		return jsonObj;
	}
}
