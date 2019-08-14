package com.knowology.km.bll;
import com.knowology.dal.Database;
import com.knowology.km.util.getConfigValue;
import javax.servlet.jsp.jstl.sql.Result;

public class SeqDAO
{
  public static int GetNextVal(String seqName) {
    String sql = null;
    
    if (getConfigValue.isMySQL) {
      
      sql = "select nextval('" + seqName + "') as seq";
    } else {
      sql = "select " + seqName + ".nextval  seq from dual";
    } 
    
    Result rs = null;
    try {
      rs = Database.executeQuery(sql);
    } catch (Exception e) {
      e.printStackTrace();
    } 
    if (rs == null || rs.getRows().length == 0) {
      return Integer.MAX_VALUE;
    }
    return Integer.parseInt(rs.getRows()[0].get("seq").toString());
  }






  
  public static String GetNextVal2(String seqName) {
    String sql = null;
    String bussinessFlag = "1";
    seqName = seqName.replace("_SEQ", "").replace("_ID", "").replace("SEQ_", "");
    sql = "select nextval('" + seqName + "') as seq";
    Result rs = null;
    rs = Database.executeQuery(sql);
    String id = rs.getRows()[0].get("seq").toString();
    if (!"".equals(bussinessFlag)) {
      id = String.valueOf(id) + "." + bussinessFlag;
    }
    return id;
  }
}
