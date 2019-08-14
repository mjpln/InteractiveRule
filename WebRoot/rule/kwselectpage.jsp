<%
	/*
	*	create by lxf
	*/
%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

com.alibaba.fastjson.JSONArray types = null;
if(request.getAttribute("types") != null){
  	types = com.alibaba.fastjson.JSONArray.parseArray(request.getAttribute("types").toString());
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title></title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=basePath%>/easyui/jquery-easyui-1.4.1/themes/default/easyui.css" />
	<link type="text/css" rel="stylesheet" href="<%=basePath%>/easyui/jquery-easyui-1.4.1/themes/icon.css" />
	<link type="text/css" rel="stylesheet" href="<%=basePath%>/css/kw.css" />
	

  </head> 
  
  <body>
     <div class="kw-wrap">
     	<div class="ct search-box">
     		<div class="blk mr-15 w-28">
	            <label for="keyword" class="mr-5">关键词:</label>
	            <input class="easyui-textbox keyword"
								style="width: 148px;" name="keyword">
	        </div>
	        <div class="blk mr-15 w-28" style="height: 22px;">
	            <label for="types" class="mr-5">知识类别(必选):</label>
	        	<select id="kw_type" class="easyui-combobox " name="types" style="width:148px;">
					
				</select>
	        </div>
	        <div class="blk mr-15 w-28">
	            <label for="loc" class="mr-5">地市:</label>
	        	<input id="kw_search_city" class="easyui-combotree"
											style="width: 150px;" type="text" name='loc'></input>
	        </div>
	        <div class="blk mr-15" >
	            <a href="#" class="easyui-linkbutton search-btn" data-options="iconCls:'icon-search'">搜索</a>
	        </div>
	        
     	</div>
     	<div class="ct  mt-20 kshow">
     		<table id="kw-knowology-info"></table>
     		
     	</div>
     	<div class="ct mt-20 talign-r">
     		<a href="#" class="easyui-linkbutton mr-10 save" data-options="iconCls:'icon-save'">确认</a>
     		<a href="#" class="easyui-linkbutton cancel" data-options="iconCls:'icon-undo'">取消</a>
     	</div>
     </div>
     
    <script type="text/javascript" src="<%=basePath%>/easyui/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="<%=basePath%>/easyui/jquery-easyui-1.4.1/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="<%=basePath%>/easyui/jquery-easyui-1.4.1/locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="<%=basePath%>/script/json2.js"></script>
	<script type="text/javascript" src="<%=basePath%>/script/publicJqueryAjaxComplete.js"></script>
	<script>
		var basePath = "<%=basePath%>";
	</script>
	<script type="text/javascript" src="<%=basePath%>/rule/js/kw.js"></script>
  </body>
</html>
