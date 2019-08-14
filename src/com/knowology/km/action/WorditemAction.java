package com.knowology.km.action;
import com.alibaba.fastjson.JSONObject;
import com.knowology.km.action.WorditemAction;
import com.knowology.km.bll.WorditemDAO;





public class WorditemAction
{
  private int start;
  private int limit;
  private String worditem;
  private Boolean worditemprecise;
  private Boolean iscurrentwordclass;
  private String worditemtype;
  private String curwordclass;
  private String oldworditem;
  private String newworditem;
  private String oldtype;
  private String newtype;
  private String wordclassid;
  private String wordid;
  private String curwordclassid;
  private Boolean isstandardword;
  private String curwordclasstype;
  private String cityname;
  private String citycode;
  private String m_request;
  private String action;
  private Object m_result;
  
  public String execute() {
    if (!"".equals(this.m_request) && this.m_request != null) {
      
      JSONObject json = JSONObject.parseObject(this.m_request);
      this.curwordclass = json.getString("curwordclass");
      this.wordid = json.getString("wordid");
      this.cityname = json.getString("cityname");
      this.citycode = json.getString("citycode");
      this.citycode = this.citycode.replace("\"", "").replace("[", "").replace("]", "");
      this.action = json.getString("action");
    } 


    
    if ("select".equals(this.action)) {
      this.m_result = WorditemDAO.select(this.start, this.limit, this.worditem, 
          this.worditemprecise, this.iscurrentwordclass, this.worditemtype, 
          this.curwordclass);
    } else if ("update".equals(this.action)) {
      this.m_result = WorditemDAO.update(this.oldworditem, this.newworditem, this.oldtype, 
          this.newtype, this.wordclassid, this.wordid, this.curwordclass, this.curwordclasstype);
    } else if ("insert".equals(this.action)) {
      this.m_result = WorditemDAO.insert(this.worditem, this.curwordclass, 
          this.curwordclassid, this.curwordclasstype, this.isstandardword);
    } else if ("delete".equals(this.action)) {
      this.m_result = WorditemDAO.delete(this.wordid, this.curwordclass, this.curwordclasstype, this.worditem);
    } else if ("selectWordCity".equals(this.action)) {
      this.m_result = WorditemDAO.selectWordCity(this.curwordclass, this.wordid);
    } else if ("updateWordCity".equals(this.action)) {
      this.m_result = WorditemDAO.updateWordCity(this.curwordclass, this.wordid, this.cityname, this.citycode);
    } 
    return "success";
  }
  
  public int getStart() { return this.start; }


  
  public void setStart(int start) { this.start = start; }


  
  public int getLimit() { return this.limit; }


  
  public void setLimit(int limit) { this.limit = limit; }


  
  public String getWorditem() { return this.worditem; }


  
  public void setWorditem(String worditem) { this.worditem = worditem; }


  
  public Boolean getWorditemprecise() { return this.worditemprecise; }


  
  public void setWorditemprecise(Boolean worditemprecise) { this.worditemprecise = worditemprecise; }


  
  public Boolean getIscurrentwordclass() { return this.iscurrentwordclass; }


  
  public void setIscurrentwordclass(Boolean iscurrentwordclass) { this.iscurrentwordclass = iscurrentwordclass; }


  
  public String getWorditemtype() { return this.worditemtype; }


  
  public void setWorditemtype(String worditemtype) { this.worditemtype = worditemtype; }


  
  public String getCurwordclass() { return this.curwordclass; }


  
  public void setCurwordclass(String curwordclass) { this.curwordclass = curwordclass; }


  
  public String getOldworditem() { return this.oldworditem; }


  
  public void setOldworditem(String oldworditem) { this.oldworditem = oldworditem; }


  
  public String getNewworditem() { return this.newworditem; }


  
  public void setNewworditem(String newworditem) { this.newworditem = newworditem; }


  
  public String getOldtype() { return this.oldtype; }


  
  public void setOldtype(String oldtype) { this.oldtype = oldtype; }


  
  public String getNewtype() { return this.newtype; }


  
  public void setNewtype(String newtype) { this.newtype = newtype; }


  
  public String getWordclassid() { return this.wordclassid; }


  
  public void setWordclassid(String wordclassid) { this.wordclassid = wordclassid; }


  
  public String getWordid() { return this.wordid; }


  
  public void setWordid(String wordid) { this.wordid = wordid; }


  
  public String getCurwordclassid() { return this.curwordclassid; }


  
  public void setCurwordclassid(String curwordclassid) { this.curwordclassid = curwordclassid; }


  
  public Boolean getIsstandardword() { return this.isstandardword; }


  
  public void setIsstandardword(Boolean isstandardword) { this.isstandardword = isstandardword; }


  
  public String getAction() { return this.action; }


  
  public void setAction(String action) { this.action = action; }


  
  public Object getM_result() { return this.m_result; }


  
  public void setM_result(Object mResult) { this.m_result = mResult; }


  
  public String getCurwordclasstype() { return this.curwordclasstype; }


  
  public void setCurwordclasstype(String curwordclasstype) { this.curwordclasstype = curwordclasstype; }


  
  public String getCityname() { return this.cityname; }


  
  public void setCityname(String cityname) { this.cityname = cityname; }


  
  public String getCitycode() { return this.citycode; }


  
  public void setCitycode(String citycode) { this.citycode = citycode; }


  
  public String getM_request() { return this.m_request; }


  
  public void setM_request(String mRequest) { this.m_request = mRequest; }
}
