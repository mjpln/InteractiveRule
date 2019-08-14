package com.knowology.km.action;
import com.knowology.km.action.WordclassAction;
import com.knowology.km.bll.WordClassDAO;

public class WordclassAction
{
  private String wordclass;
  private int start;
  private int limit;
  private String id;
  private String oldvalue;
  private String newvalue;
  private String oldwordclasstype;
  private String newwordclasstype;
  private Boolean wordclassprecise;
  private String wordclasstype;
  private String wordclassid;
  private String action;
  private Object m_result;
  
  public String execute() {
    if ("select".equals(this.action)) {
      this.m_result = WordClassDAO.select(this.wordclass, this.wordclassprecise, this.wordclasstype, this.start, this.limit);
    } else if ("update".equals(this.action)) {
      this.m_result = WordClassDAO.update(this.id, this.oldvalue, this.newvalue, this.oldwordclasstype, this.newwordclasstype);
    } else if ("insert".equals(this.action)) {
      this.m_result = WordClassDAO.insert(this.wordclass, this.wordclasstype);
    } else if ("insertInit".equals(this.action)) {
      this.m_result = insertInit(); 
    }else if ("delete".equals(this.action)) {
      this.m_result = WordClassDAO.delete(this.wordclassid, this.wordclass, this.wordclasstype);
    } 
    return "success";
  }

  
  private Object insertInit() {
	  WordClassDAO.insert(this.wordclass, this.wordclasstype);
	return null;
}


public String getWordclass() { return this.wordclass; }


  
  public void setWordclass(String wordclass) { this.wordclass = wordclass; }


  
  public int getStart() { return this.start; }


  
  public void setStart(int start) { this.start = start; }


  
  public int getLimit() { return this.limit; }


  
  public void setLimit(int limit) { this.limit = limit; }


  
  public Object getM_result() { return this.m_result; }


  
  public void setM_result(Object mResult) { this.m_result = mResult; }


  
  public String getAction() { return this.action; }


  
  public void setAction(String action) { this.action = action; }


  
  public String getId() { return this.id; }


  
  public void setId(String id) { this.id = id; }


  
  public String getOldvalue() { return this.oldvalue; }


  
  public void setOldvalue(String oldvalue) { this.oldvalue = oldvalue; }


  
  public String getNewvalue() { return this.newvalue; }


  
  public void setNewvalue(String newvalue) { this.newvalue = newvalue; }


  
  public String getWordclassid() { return this.wordclassid; }


  
  public void setWordclassid(String wordclassid) { this.wordclassid = wordclassid; }


  
  public Boolean isWordwodclassprecise() { return this.wordclassprecise; }


  
  public void setWordwodclassprecise(boolean wordclassprecise) { this.wordclassprecise = Boolean.valueOf(wordclassprecise); }


  
  public Boolean getWordclassprecise() { return this.wordclassprecise; }


  
  public void setWordclassprecise(Boolean wordclassprecise) { this.wordclassprecise = wordclassprecise; }


  
  public String getOldwordclasstype() { return this.oldwordclasstype; }


  
  public void setOldwordclasstype(String oldwordclasstype) { this.oldwordclasstype = oldwordclasstype; }


  
  public String getNewwordclasstype() { return this.newwordclasstype; }


  
  public void setNewwordclasstype(String newwordclasstype) { this.newwordclasstype = newwordclasstype; }


  
  public String getWordclasstype() { return this.wordclasstype; }


  
  public void setWordclasstype(String wordclasstype) { this.wordclasstype = wordclasstype; }
}
