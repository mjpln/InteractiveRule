package com.knowology.km.action;

import com.knowology.km.bll.InteractiveSceneGoJSDAO;

public class InteractiveSceneGoJSAction extends BaseAction {
	private String m_request;
	private Object m_result;
	private String type;

	private String scenariosid;
	private String scenariosName;
	private String name;
	private String weight;
	private String oldweight;
	private String wordclassid;
	private String wordclass;
	private String city;
	private String cityname;
	private String infotalbepath;
	private String itemmode;
	private String container;
	private String interpat;
	private String scenarioselementid;
	
	private String ioa;
	
	public Object execute() {
		if ("saveConfig".equals(type)) {// 解析json并向数据库中插入数据
			m_result = InteractiveSceneGoJSDAO.parseJsonFromStrAndInsert(m_request, scenariosid);
		}
		if ("getscenarioselementname".equals(type)) {// 获取场景要素名
			m_result = InteractiveSceneGoJSDAO.selectElementName(scenariosid, name);
		}
		if ("loadConfigData".equals(type)) {// 加载场景配置数据
			m_result = InteractiveSceneGoJSDAO.loadData(scenariosid);
		}
		if ("insertscenarioselement".equals(type)) {// 新增场景要素信息
			m_result = InteractiveSceneGoJSDAO.insertScenariosElement(scenariosid, scenariosName, name, weight, wordclass, city, cityname, 
					infotalbepath, itemmode, container, interpat);
		}
		if ("updatescenarioselement".equals(type)) {// 编辑场景要素信息
			m_result = InteractiveSceneGoJSDAO.updateScenariosElement(scenariosid, scenariosName, name, weight, oldweight, wordclass, city, cityname, 
					infotalbepath, itemmode, container, interpat, scenarioselementid);
		}
		if ("deletescenarioselement".equals(type)) {// 删除场景要素信息
			m_result = InteractiveSceneGoJSDAO.deleteScenariosElement(scenariosid, scenariosName, name, weight, scenarioselementid);
		}
		if ("getUrl".equals(type)) {// 要打开的页面
			m_result = InteractiveSceneGoJSDAO.getUrl(ioa);
		}
		return "success";
	}

	public String getM_request() {
		return m_request;
	}

	public void setM_request(String mRequest) {
		m_request = mRequest;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScenariosid() {
		return scenariosid;
	}

	public void setScenariosid(String scenariosid) {
		this.scenariosid = scenariosid;
	}

	public String getScenariosName() {
		return scenariosName;
	}

	public void setScenariosName(String scenariosName) {
		this.scenariosName = scenariosName;
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

	public String getOldweight() {
		return oldweight;
	}

	public void setOldweight(String oldweight) {
		this.oldweight = oldweight;
	}

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public String getWordclass() {
		return wordclass;
	}

	public void setWordclass(String wordclass) {
		this.wordclass = wordclass;
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

	public String getInfotalbepath() {
		return infotalbepath;
	}

	public void setInfotalbepath(String infotalbepath) {
		this.infotalbepath = infotalbepath;
	}

	public String getItemmode() {
		return itemmode;
	}

	public void setItemmode(String itemmode) {
		this.itemmode = itemmode;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getInterpat() {
		return interpat;
	}

	public void setInterpat(String interpat) {
		this.interpat = interpat;
	}

	public String getScenarioselementid() {
		return scenarioselementid;
	}

	public void setScenarioselementid(String scenarioselementid) {
		this.scenarioselementid = scenarioselementid;
	}

	public String getIoa() {
		return ioa;
	}

	public void setIoa(String ioa) {
		this.ioa = ioa;
	}
}