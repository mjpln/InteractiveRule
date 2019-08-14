package com.knowology.km.bll;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.dal.Database;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.bll.SeqDAO;
import com.knowology.km.bll.WorditemDAO;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.jstl.sql.Result;
public class WorditemDAO
{
  public static Map<String, String> cityMap = new LinkedHashMap();
  
  static  {
    Result cityRs = null;
    String citySql = "";
    citySql = "select t.name as id,min(s.name) as city from metafield t,metafield s,metafieldmapping a where a.name='地市编码配置' and t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and  t.name like '______' group by t.name order by id";
    try {
      cityRs = Database.executeQuery(citySql);
    } catch (Exception e) {
      
      e.printStackTrace();
    } 
    if ((((cityRs != null) ? 1 : 0) & ((cityRs.getRowCount() > 0) ? 1 : 0))!=0 ) {
      for (int i = 0; i < cityRs.getRowCount(); i++) {
        if (cityRs.getRows()[i].get("id").toString().length() == 6) {
          cityMap.put(cityRs.getRows()[i].get("id").toString(), cityRs.getRows()[i].get("city").toString());
        }
      } 
    }
  }
  
  public static Object getCityTree(String local) {
    String[] cityname = local.split(",");
    Map<String, String> map = new HashMap<String, String>();
    for (int m = 0; m < cityname.length; m++) {
      map.put(cityname[m], "");
    }
    JSONArray jsonAr = new JSONArray();
    
    Result rs = null;
    if (!cityMap.isEmpty()) {
      JSONObject allJsonObj = new JSONObject();
      allJsonObj.put("id", "全国");
      allJsonObj.put("text", "全国");
      if (map.containsKey("全国")) {
        allJsonObj.put("checked", Boolean.valueOf(true));
      }
      jsonAr.add(allJsonObj);
      for (Map.Entry<String, String> pro : cityMap.entrySet()) {
        if (((String)pro.getKey()).contains("0000")) {
          JSONObject jsonObj = new JSONObject();
          String id = (String)pro.getKey();
          String province = (String)pro.getValue();
          jsonObj.put("id", id);
          jsonObj.put("text", province);
          if (map.containsKey(province)) {
            jsonObj.put("checked", Boolean.valueOf(true));
          }
          if (province.indexOf("市") < 0) {
            String innerid = id.substring(0, 2);
            JSONArray jsonArrSon = new JSONArray();
            for (Map.Entry<String, String> proSon : cityMap.entrySet()) {
              if (((String)proSon.getKey()).startsWith(innerid) && ((String)proSon.getKey()).endsWith("00") && !((String)proSon.getKey()).endsWith("0000")) {
                JSONObject jsonObjSon = new JSONObject();
                jsonObjSon.put("id", proSon.getKey());
                jsonObjSon.put("text", proSon.getValue());
                
                String innerInnerId = ((String)proSon.getKey()).substring(0, 4);
                JSONArray jsonArrSonSon = new JSONArray();
                for (Map.Entry<String, String> proSonSon : cityMap.entrySet()) {
                  if (((String)proSonSon.getKey()).startsWith(innerInnerId) && !((String)proSonSon.getKey()).endsWith("00") && !((String)proSonSon.getKey()).endsWith("01")) {
                    JSONObject jsonObjSonSon = new JSONObject();
                    jsonObjSonSon.put("id", proSonSon.getKey());
                    jsonObjSonSon.put("text", proSonSon.getValue());
                    if (map.containsKey(proSonSon.getValue())) {
                      jsonObjSonSon.put("checked", Boolean.valueOf(true));
                    }
                    jsonArrSonSon.add(jsonObjSonSon);
                  } 
                } 
                
                if (!jsonArrSonSon.isEmpty()) {
                  jsonObjSon.put("children", jsonArrSonSon);
                  jsonObjSon.put("state", "closed");
                } 
                if (map.containsKey(proSon.getValue())) {
                  jsonObjSon.put("checked", Boolean.valueOf(true));
                }
                jsonArrSon.add(jsonObjSon);
              } 
            } 
            if (!jsonArrSon.isEmpty()) {
              jsonObj.put("children", jsonArrSon);
              jsonObj.put("state", "closed");
            } 
          } else {
            String innerid = id.substring(0, 2);
            JSONArray jsonArrSon = new JSONArray();
            for (Map.Entry<String, String> proSon : cityMap.entrySet()) {
              if (((String)proSon.getKey()).startsWith(innerid) && !((String)proSon.getKey()).endsWith("00")) {
                JSONObject jsonObjSon = new JSONObject();
                jsonObjSon.put("id", proSon.getKey());
                jsonObjSon.put("text", proSon.getValue());
                if (map.containsKey(proSon.getValue())) {
                  jsonObjSon.put("checked", Boolean.valueOf(true));
                }
                jsonArrSon.add(jsonObjSon);
              } 
            } 
            if (!jsonArrSon.isEmpty()) {
              jsonObj.put("children", jsonArrSon);
              jsonObj.put("state", "closed");
            } 
          } 
          jsonAr.add(jsonObj);
        } 
      } 
    } 
    
    return jsonAr;
  }
  
  public static Object select(int start, int limit, String worditem, Boolean worditemprecise, Boolean iscurrentwordclass, String worditemtype, String curwordclass) {
    JSONObject jsonObj = new JSONObject();
    JSONArray jsonArr = new JSONArray();
    int count = UserOperResource.getWordCount(worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础");
    
    if (count > 0) {
      
      jsonObj.put("total", Integer.valueOf(count));
      Result rs = UserOperResource.selectWord(start, limit, worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础");
      
      if (rs != null && rs.getRowCount() > 0)
      {
        for (int i = 0; i < rs.getRowCount(); i++) {
          
          JSONObject obj = new JSONObject();
          
          obj.put("id", Integer.valueOf(start + i + 1));
          
          obj.put("worditem", rs.getRows()[i].get("word"));
          
          obj.put("wordclass", rs.getRows()[i].get("wordclass"));
          
          obj.put("type", rs.getRows()[i].get("type"));
          
          obj.put("wordid", rs.getRows()[i].get("wordid"));
          
          obj.put("wordclassid", rs.getRows()[i]
              .get("wordclassid"));
          
          jsonArr.add(obj);
        } 
      }
      
      jsonObj.put("root", jsonArr);
    } else {
      
      jsonObj.put("total", Integer.valueOf(0));
      
      jsonArr.clear();
      
      jsonObj.put("root", jsonArr);
    } 
    
    return jsonObj;
  }
  
  public static Object update(String oldworditem, String newworditem, String oldtype, String newtype, String wordclassid, String wordid, String curwordclass, String curwordclasstype) {
    JSONObject jsonObj = new JSONObject();
    
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    if (UserOperResource.isExistWord(wordclassid, newworditem, newtype).booleanValue()) {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "词条已存在!");
      return jsonObj;
    } 
    
    if ((oldtype.equals(newtype) && "标准名称".equals(oldtype)) || (!oldtype.equals(newtype) && "标准名称".equals(oldtype)))
    {
      
      if (UserOperResource.isHaveOtherName(wordid).booleanValue()) {
        jsonObj.put("success", Boolean.valueOf(false));
        
        jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
        return jsonObj;
      } 
    }
    
    sre = GetSession.getSessionByKey("accessUser");
    if (sre == null || "".equals(sre)) {
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "登录超时,请注销后重新登录!");
      return jsonObj;
    } 
    
    int c = UserOperResource.updateWord(user, oldworditem, newworditem, oldtype, newtype, wordid, wordclassid, curwordclass, curwordclasstype, "基础");
    
    if (c > 0) {
      
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "修改成功!");
    }
    else {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "修改失败!");
    } 
    return jsonObj;
  }
  
  public static Boolean Exists(String curwordclassid, String worditem, String newtype) {
    List<String> lstpara = new ArrayList<String>();
    
    String sql = "select wordid from word where rownum<2 and wordclassid=? and word=? and type=? ";
    
    lstpara.add(curwordclassid);
    
    lstpara.add(worditem);
    
    lstpara.add(newtype);
    
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
  
  public static Object insert(String worditem, String curwordclass, String curwordclassid, String curwordclasstype, Boolean isstandardword) {
    JSONObject jsonObj = new JSONObject();
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    if (curwordclassid == null || "".equals(curwordclassid)) {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "请选择当前词类!");
      return jsonObj;
    } 
    
    List<String> lstWorditem = new ArrayList<String>(Arrays.asList(worditem.split("\n")));
    
    String type = "";
    if (isstandardword.booleanValue()) {
      type = "标准名称";
    } else {
      type = "普通词";
    } 
    
    String msg = "";
    List<String> listWord = new ArrayList<String>();
    for (int i = 0; i < lstWorditem.size(); i++) {
      String wd = (String)lstWorditem.get(i);
      
      if (Exists(curwordclassid, wd, type).booleanValue()) {
        if ("".equals(msg)) {
          msg = "第";
        }
        msg = String.valueOf(msg) + (i + 1) + ",";
      } else {
        listWord.add(wd);
      } 
    } 
    if (msg.length() > 1) {
      msg = msg.substring(0, msg.lastIndexOf(","));
      msg = String.valueOf(msg) + "条词条已存在!";
    } 
    
    int c = -1;
    if (listWord.size() > 0) {
      
      c = UserOperResource.insertWord(user, curwordclassid, curwordclass, curwordclasstype, listWord, type, "基础");
    } else {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", msg);
      return jsonObj;
    } 
    
    if (c > 0) {
      
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "保存成功!<br>" + msg);
    }
    else {
      
      jsonObj.put("success", Boolean.valueOf(false));
      
      jsonObj.put("msg", "保存失败!");
    } 
    return jsonObj;
  }
  
  public static Object delete(String wordid, String curwordclass, String curwordclasstype, String worditem) {
    JSONObject jsonObj = new JSONObject();
    
    Object sre = GetSession.getSessionByKey("accessUser");
    if (sre == null || "".equals(sre)) {
      jsonObj.put("success", Boolean.valueOf(true));
      
      jsonObj.put("msg", "登录超时,请注销后重新登录!");
      return jsonObj;
    } 
    User user = (User)sre;
    String customer = user.getCustomer();
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    int c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, worditem, "基础");
    
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
  
  public static Object selectWordCity(String wordclass, String wordid) {
    JSONObject jsonObj = new JSONObject();
    Result rs = UserOperResource.selectWordCity(wordclass, wordid);
    
    if (rs != null && rs.getRowCount() > 0) {
      
      jsonObj.put("cityname", (rs.getRows()[0].get("cityname") == null) ? "" : rs.getRows()[0].get("cityname"));
      jsonObj.put("citycode", (rs.getRows()[0].get("city") == null) ? "" : rs.getRows()[0].get("city"));
      jsonObj.put("success", Boolean.valueOf(true));
    } else {
      jsonObj.put("cityname", "");
      jsonObj.put("citycode", "");
      jsonObj.put("success", Boolean.valueOf(false));
    } 
    
    return jsonObj;
  }
  
  public static Object updateWordCity(String wordclass, String wordid, String cityNme, String cityCode) {
    JSONObject jsonObj = new JSONObject();
    
    Object sre = GetSession.getSessionByKey("accessUser");
    User user = (User)sre;
    String customer = user.getCustomer();
    
    if (!"全行业".equals(customer)) {
      jsonObj.put("success", Boolean.valueOf(false));
      jsonObj.put("msg", "无操作权限!");
      return jsonObj;
    } 
    
    int c = UserOperResource.updateWordCity(wordclass, wordid, cityNme, cityCode);
    
    if (c > 0) {
      
      jsonObj.put("msg", "更新成功!");
      jsonObj.put("success", Boolean.valueOf(true));
    } else {
      jsonObj.put("msg", "更新失败!");
      jsonObj.put("success", Boolean.valueOf(false));
    } 
    
    return jsonObj;
  }
}
