package com.knowology.km.action;

import java.sql.SQLException;

import com.knowology.km.bll.QuestionUploadDao;

public class QuestionUploadAction {
	
	private Object m_result;
	private String id;
	
	private int page;
	private int rows;
	
	private String question;
	private String other;
	private String starttime;
	private String endtime;
	private String username;
	private String status;
	private String hot;
	private String hot2;
	private String selProvince;
	private String selCity;
	private int sid;
	private Integer pid;
	private String pid2;
	private String reason;
	private String solution;
	private String fileName;
	
	private String attrArr;
	private String url;
	
	private String ids;
	private String local;
	
	public String deleteOther(){
		m_result = QuestionUploadDao.deleteOther(ids);
		return "success";
	}
	
	public String updateQueName(){
		m_result = QuestionUploadDao.updateQueName(pid,question,sid,other,selProvince,selCity);
		return "success";
	}
	
	public String delOther() {
		m_result = QuestionUploadDao.delOther(sid);
		return "success";
	}
	
	public String getSession(){
		m_result = QuestionUploadDao.getSession();
		return "success";
	}
	
	public String getProvince(){
		m_result = QuestionUploadDao.selProvince();
		return "success";
	}
	
	public String getCity(){
		m_result = QuestionUploadDao.getCity(id);
		return "success";
	}
	
	public String gethotquestion(){
		m_result = QuestionUploadDao.gethotquestion(page,rows,question,other,starttime,endtime,username,status,selProvince,selCity,hot,hot2,pid,ids);
		return "success";
	}
	public String gethotquestion2(){
		m_result = QuestionUploadDao.gethotquestion2(page,rows,question,starttime,endtime,status,selProvince,selCity,hot,hot2,pid);
		return "success";
	}
	
	public String setAttr(){
		m_result = QuestionUploadDao.setAttr(ids);
		return "success";
	}
	
	public String exportxls(){
		m_result = QuestionUploadDao.ExportExcel();
		
		return "success";
		
	}
	
	public String ExportExcel(){
		m_result = QuestionUploadDao.ExportExcel(ids);
		return "success";
	}
	
	public String ExportExcel2(){
		m_result = QuestionUploadDao.ExportExcel2(question,other,starttime,endtime,username,status,selProvince,selCity);
		return "success";
	}

	public String doSaveReport(){
		m_result = QuestionUploadDao.doSaveReport(ids,reason,solution);
		return "success";
	}

	public String importxls(){
		m_result = QuestionUploadDao.ImportExcel(fileName);
		return "success";
	}
	
	
	public String getsonquestion(){
		m_result = QuestionUploadDao.getsonquestion(question,pid2,status,rows,page);
		return "success";
	}
	
	public String insertother(){
		m_result = QuestionUploadDao.insertother(question,pid,selProvince,selCity);
		return "success";
	}
	
	public String selLocal(){
		m_result = QuestionUploadDao.selLocal(local);
		return "success";
	}
	public String getCityTree(){
//		m_result = WorditemDAO.getCityTree(local);//省市县三级
		m_result = QuestionUploadDao.getCityTree(local);//省市二级
		return "success";
	}
	
	public String getCityTreeByLoginInfo(){
//		m_result = WorditemDAO.getCityTree(local);//省市县三级
		m_result = QuestionUploadDao.getCityTreeByLoginInfo(local);//省市二级
		return "success";
	}
	
	public String exportexample(){
		m_result = QuestionUploadDao.exportexample();
		return "success";
	}
	
	public String exsitfile(){
		m_result = QuestionUploadDao.exsitfile();
		return "success";
	}
	
	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getSelCity() {
		return selCity;
	}

	public void setSelCity(String selCity) {
		this.selCity = selCity;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getSelProvince() {
		return selProvince;
	}

	public void setSelProvince(String selProvince) {
		this.selProvince = selProvince;
	}

	public String getHot() {
		return hot;
	}

	public void setHot(String hot) {
		this.hot = hot;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getHot2() {
		return hot2;
	}

	public void setHot2(String hot2) {
		this.hot2 = hot2;
	}

	public String getAttrArr() {
		return attrArr;
	}

	public void setAttrArr(String attrArr) {
		this.attrArr = attrArr;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getPid2() {
		return pid2;
	}

	public void setPid2(String pid2) {
		this.pid2 = pid2;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
