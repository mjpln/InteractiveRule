package com.knowology.km.bll;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.dal.Database;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.bll.SeqDAO;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.jsp.jstl.sql.Result;
public class WordClassDAO
{
  public static Object select(String wordclass, Boolean wordclassprecise, String wordclasstype, int start, int limit) {
    JSONObject jsonObj = new JSONObject();
    JSONArray jsonArr = new JSONArray();
    Object sre = GetSession.getSessionByKey("accessUser");
    if (sre == null || "".equals(sre)) {
      
      jsonObj.put("total", Integer.valueOf(0));
      
      jsonArr.clear();
      
      jsonObj.put("root", jsonArr);
      return jsonObj;
    } 
    User user = (User)sre;
    String idustry = user.getCustomer().split("->")[0];
    
    int count = UserOperResource.getWordclassCount(user, wordclass, wordclassprecise, wordclasstype, "基础");
    
    if (count > 0) {
      
      jsonObj.put("total", Integer.valueOf(count));
      jsonArr = UserOperResource.getWordclass(user, wordclass, wordclassprecise, wordclasstype, "基础", start, limit);
      
      if (jsonArr != null && jsonArr.size() > 0)
      {
        jsonObj.put("root", jsonArr);
      }
    }
    else {
      
      jsonObj.put("total", Integer.valueOf(0));
      
      jsonArr.clear();
      
      jsonObj.put("root", jsonArr);
    } 
    
    return jsonObj;
  }

  public static Object update(String id, String oldvalue, String newvalue, String oldwordclasstype, String newwordclasstype) {
    JSONObject jsonObj = new JSONObject();
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    if (UserOperResource.isExistWordclass(oldvalue, newvalue).booleanValue()) {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "该词类词库中已存在!");
    }
    else {
      
      sre = GetSession.getSessionByKey("accessUser");
      if (sre == null || "".equals(sre)) {
        
        jsonObj.put("success", Boolean.valueOf(false));
        
        jsonObj.put("msg", "登录超时，请注销后重新登录");
        return jsonObj;
      } 
      
      int c = UserOperResource.updateWordcalss(user, id, oldvalue, newvalue, oldwordclasstype, newwordclasstype, "基础");
      
      if (c > 0) {
        
        jsonObj.put("success", Boolean.valueOf(true));
        
        jsonObj.put("msg", "修改成功!");
      }
      else {
        
        jsonObj.put("success", Boolean.valueOf(false));
        
        jsonObj.put("msg", "修改失败!");
      } 
    } 
    return jsonObj;
  }
  
  public static Boolean Exists(String wordclass) {
    String sql = "select * from wordclass where wordclass= ? ";
    
    List<String> lstpara = new ArrayList<String>();
    
    lstpara.add(wordclass);
    
    try {
      Result rs = Database.executeQuery(sql, lstpara.toArray());
      
      if (rs != null && rs.getRowCount() > 0)
      {
        return Boolean.valueOf(true);
      }
      
      return Boolean.valueOf(false);
    }
    catch (Exception e) {
      e.printStackTrace();
      
      return Boolean.valueOf(false);
    } 
  }
  
  public static Object insert(String wordclass, String wordclasstype) {
    JSONObject jsonObj = new JSONObject();
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    List<String> lstWordclass = Arrays.asList(wordclass.split("\n"));
    for (int i = 0; i < lstWordclass.size(); i++) {
      
      if (UserOperResource.isExistWordclass("", (String)lstWordclass.get(i)).booleanValue()) {
        
        jsonObj.put("success", Boolean.valueOf(false));
        
        jsonObj.put("msg", "第" + (i + 1) + "条词库中已存在!");
        return jsonObj;
      } 
    } 
    
    sre = GetSession.getSessionByKey("accessUser");
    if (sre == null || "".equals(sre)) {
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "登录超时,请注销后重新登录!");
    } 
    
    int c = UserOperResource.insertWordcalss(user, lstWordclass, wordclasstype, "基础", user.getCustomer());
    
    if (c > 0) {
      
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "保存成功!");
    }
    else {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "保存失败!");
    } 
    return jsonObj;
  }
  public static Object delete(String wordclassid, String wordclass, String wordclasstype) {
    JSONObject jsonObj = new JSONObject();
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    sre = GetSession.getSessionByKey("accessUser");
    if (sre == null || "".equals(sre)) {
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "登录超时,请注销后重新登录!");
    } 
    
    int c = UserOperResource.deleteWordclass(user, wordclassid, wordclass, wordclasstype, "基础");
    
    if (c > 0) {
      
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "删除成功!");
    }
    else {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "删除失败!");
    } 
    return jsonObj;
  }
}
