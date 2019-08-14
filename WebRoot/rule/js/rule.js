var kbdataid = "";
var kbcontentid = "";
var kbanswerid = "";
var _abstract = "";
var flag;
var publicscenariosid;
var publicwordclassid;
var ruleresponsetemplate;
var s_ruleresponsetemplate;
var combitionArr = []; 
var rule0Arr = [];
var rule1Arr = [];
var excludedcity ;
var publiccitycode;
var publicrulesid;
var publicscenariosname;
var insertorupdate_combition = 0;
var insertorupdate_rule0 = 0;
var insertorupdate_rule1 = 0;
var insertorupdate_rule2 = 0; 
var dnonameDefaultText = "";
var currentcitycode;
var userid;
var ioa;
var accessUser;
var customer;
var robot = -1;

//设置rule的列数，qby,20180921
var MAX_RULE_COUNT=11;

$(function() {
	var urlparams = new UrlParams();// 所有url参数
	//获得url中场景ID
	publicscenariosid = decodeURI(urlparams.scenariosid);
	publicscenariosname = decodeURI(urlparams.scenariosname);
	if (urlparams.userid != null && urlparams.userid != ""){
		userid = decodeURI(urlparams.userid);
		ioa =  decodeURI(urlparams.ioa);
		setid(userid, ioa);
	}
	
	//加载场景关系列表
	searchRelation();
	
	//加载业务树	  
    createServiceTree('service','kbdata');
	
  //加载场景要素
    loadElementName();
	loadWeightCombobox(""); 
	loadElementValue(); 
    //加载交互规则
	loadRule0Combobox();
	loadRule0();
	createServiceInfoCombobox2();
	//添加回复类型onselect 事件
	responseTypeOnSelect();
	//加载city树
	getSearchCityTree('search_city','');
	getSearchCityTree('edit_city','edit');
	getSearchCityTree2('edit_excludedcity','edit');
	//添加city树onselect
	cityOnSelect('search_city');
	//关闭编辑框
	closeEditWindow();
	
//	$("#elementdialog").dialog('close');
	
//	$('#aa').accordion( {
//		border : true,
//		onSelect : function(title, index) {
//		if(title=="业务摘要对应关系"){
//			//加载场景关系列表
//			searchRelation();
//			//加载业务树	  
//		    createServiceTree();
//		}else if(title=="场景要素"){
//			 //加载场景要素
//		    loadElementName();
//			loadWeightCombobox();
//			loadElementValue(); 
//			
//		}else if(title=="交互规则"){
//			 //加载交互规则
//			loadRule0Combobox();
//			loadRule0(); 
//		}
//		}
//	});
	
	//李习凤代码
	getSearchCityTree('kw_search_city','edit');
	createKnoTypeCombobox();
	
    
});

String.prototype.endWith=function(str){
	if(str==null||str==""||this.length==0||str.length>this.length)
	  return false;
	if(this.substring(this.length-str.length)==str)
	  return true;
	else
	  return false;
	return true;
}


function setid(id, ioa) {
	$.ajax( {
		url : '../interactiveSceneLogin.action',
		type : "post",
		data : {
			type : 'getcustomer',
			userid:id,
			ioa:ioa
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			accessUser = data.accessUser;
			customer = data.customer;
//			alert(accessUser+"1");
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});

}


//关闭规则编辑框
function closeEditWindow(){
	if (ioa!='电信行业->电信集团->IVR机器人' && ioa!='在线教育->尚德机构->多渠道应用'){
		$('#addOtherResponse_btn').hide();
	}
	$('#rule0_edit').panel('close');
	
}

//加载场景关系
function searchRelation(){
	loadSceneRelation();
}


//构造场景树
function createServiceTree() {
	$("#service").combotree({
		url:'../interactiveScene.action?type=createservicetree&a='+ Math.random(),
		editable:false,
		onBeforeExpand:function(node,param){ 
			$('#service').combotree('tree').tree("options").url = "../interactiveScene.action?type=createservicetree&serviceid="+node.id +'&a='+ Math.random();
		}, 
		onClick:function(rec){
			createAbsCombobox();
		}
	});
}


//获得地市信息comboxtree
function getCityTree(cityname){
//	var city = "南京市,合肥市,江苏省,北京市";
	$('#city').combotree({
		url:'../getCityTree.action',
		editable:false, 
		multiple:true,
		queryParams:{
			local : cityname
		}
	}); 
}

//添加city onselect事件
function cityOnSelect(id){ 
	var id ="#"+id;
	$(id).combotree({
		 onSelect : function(node){
		var city = node.id;
		var conditions = [];
		for ( var i = 1; i < MAX_RULE_COUNT; i++) {
			var con = $("#selrule0value" + i).combobox("getText");
			if (con == "(空)") {
				con = "";
			}
			conditions.push(con);
		}
//		var rt = $("#selrule0type").combobox("getValue");
//		if(rt=="全部"){
//			rt="";
//		}
		var issue = $("#issueChoose").combobox('getValue');
		$('#rule0datagrid').datagrid('load', {
			type : 'selectrule',
			scenariosid : publicscenariosid,
			conditions : conditions.join("@"),
			ruletype : "",
			weight : "", 
			city:city,
//			belong:"allindustry"
			issue : issue
			
		});

		
	}
	}); 
}

//根据业务信息列下拉框
function createServiceInfoCombobox() {
	$('#infotalbepath').combobox({    
	    url:'../interactiveScene.action?type=createserviceinfocombobox&scenariosid='+publicscenariosid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
//	    editable:false
	});  
}

//根据业务信息列下拉框
function createServiceInfoCombobox2() {
	$('#serviceinfo').combobox({    
	    url:'../interactiveScene.action?type=createserviceinfocombobox&scenariosid='+publicscenariosid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    onSelect: function(rec) {
		var _serviceid = rec.id;
	if (_serviceid == "" || _serviceid == null) {
		$.messager.alert('提示', "请选择信息表名称!", "warning");
		return;
	}
	//selectSemanticsKeyword(_serviceid);

	//加载属性名称下拉数据
	createAttrCombobox(_serviceid);

	// 选择时，重置信息表模板
	var serviceinfo = rec.text;
	var content = '查询("' + serviceinfo + '","","");';
	$("#serviceinfotemplate").textbox('setValue', content);
	$("#serviceinfotemplate").data('serviceinfo', {
		'serviceid': _serviceid,
		'service': serviceinfo
	}); // 绑定当前信息表到信息表模板对象上

	$('#attrvalue').combobox('setValue', '');
},
onChange: function(newVal, oldVal) {
	if (!newVal || newVal.trim() === '') {
		// 选择时，清空信息表模板
		clearServiceinfotemplate();
		$('#attrname').combobox('loadData', []).combobox('setValue', '');
		$('#attrvalue').combobox('setValue', '');
		createAttrLabelCombobox('', '', '');
	}
}
}); 
}

function clearServiceinfotemplate() {
$("#serviceinfotemplate").textbox('setValue', '').data('serviceinfo', null);
}

//加载模板路径下拉数据
function createAttrCombobox(serviceid) {
var _url = '../interactiveScene.action?type=createattrnamecombobox&serviceid=' + serviceid + '&a=' + Math.random();
$('#attrname').combobox({
	url: _url,
	valueField: 'id',
	textField: 'text',
	onSelect: function(rec) {
		if (rec.text != '') {
			// 选择时，清空信息表模板
			clearServiceinfotemplate();
			$('#attrvalue').textbox('setValue', '');
			createAttrLabelCombobox(serviceid, rec.text, rec.id);
			
			serviceinfo = $("#serviceinfo").combobox('getText');
			//查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
			var content = '查询("' + serviceinfo + '","","' + rec.text + '|' + '<@' + rec.text + '>' + '");';
			$("#serviceinfotemplate").textbox('setValue', content);
			$("#serviceinfotemplate").data('serviceinfo', {
				'serviceid': serviceid,
				'service': serviceinfo
			}); // 绑定当前信息表到信息表模板对象上

//			var answer = UM.getEditor('myEditor').getContentTxt();
//			serviceinfoanswer = content + answer;
		}

	}
})
}
//加载模板内容数据
function createAttrLabelCombobox(serviceid, attrname, colnum) {
var city = "all";
//加载属性标签内容下拉数据
var _url = '../interactiveScene.action?type=createattrvaluescombobox&serviceid=' + serviceid + '&column=' + colnum + '&city=' + city + '&a=' + Math.random();
$('#attrvalue').combobox({
	url: _url,
	valueField: 'id',
	textField: 'text',
	groupField: 'service',
	onSelect: function(rec) {
		var serviceinfo, serviceid;
		if (rec.attrname) { // 判断属性内容是选择得到，还是全部加载得到
			serviceinfo = rec.service;
			attrname = rec.attrname;
			serviceid = rec.serviceid;
		} else {
			serviceinfo = $("#serviceinfo").combobox('getText');
			serviceid = $('#serviceinfo').combobox('getValue');
		}
		var attrvalue = rec.text;
		//查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
		var content = '查询("' + serviceinfo + '","","' + attrname + '|' + attrvalue + '");';
		if(flag!=0) {
			$("#serviceinfotemplate").textbox('setValue', content);
		} else {
			if($("#attrvalue").combobox('getValue')=="") {
				$("#serviceinfotemplate").textbox('setValue', content);
			}
			flag=1;
		}
		$("#serviceinfotemplate").data('serviceinfo', {
			'serviceid': serviceid,
			'service': serviceinfo
		}); // 绑定当前信息表到信息表模板对象上

//		var answer = UM.getEditor('myEditor').getContentTxt();
//		serviceinfoanswer = content + answer;
	}
})
}


//查询信息表下列对应语义关键字
function selectSemanticsKeyword(_serviceid, attrname){
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : "selectsemanticskeyword",
			serviceid:_serviceid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				var keyword =data.name;
				if(keyword!=null&&keyword!=""){
					docname = keyword;
				}else{
					docname = "docname";
				}
				
				//加载知识名称下拉数据
				var _url = '../interactiveScene.action?type=createknonamecombobox&serviceid='+_serviceid+'&attrname='+attrname+'&a='+ Math.random(); 
				createCombobox("knoname",_url,false, true,dnonameDefaultText);
				dnonameDefaultText = "";
				
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//创建Combobox
function createCombobox(id,url,multiple,editable,defaultText) {
	$('#'+id).combobox({
	    url:url,
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : multiple, // 支持多选
		separator :',',// 多选的时候用“,”分隔
	    editable:editable,
	    onLoadSuccess:function(){
			if(defaultText){
				var defaultValue = "";
				var data = $(this).combobox('getData');
				for(var i=0; i < data.length; i++){
					if(defaultText === data[i].text){
						defaultValue = data[i].id;
					}
				}
				$(this).combobox('select', defaultValue);
			}
		}
	});

}

//知识名称 onselcet事件
function  knoNameOnSelect(){
   $("#knoname").combobox({
       onSelect: function (res) { 
		      var serviceinfo = $("#serviceinfo").combobox('getText');
		      var kname = res.text;
		      //查询("信息表名称","","标准名称|docname");提示用户("答案文本")
		      var content =  '查询("'+serviceinfo+'","","'+docname+'|'+kname+'");'; 
		      $("#serviceinfotemplate").textbox('setValue',content);
		      var answer = UM.getEditor('myEditor').getContentTxt();
		      serviceinfoanswer =content+answer;
		      //docname = "docName";
    	}
   });
}

//根据业务构造摘要下拉框
function createAbsCombobox() {
	// 获取树形结构选中的业务
	var serviceid = $('#service').combotree("getValue"); 
	$('#kbdata').combobox({    
	    url:'../interactiveScene.action?type=createabstractcombobox&serviceid='+serviceid,    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
	    //editable:false 
	});  
}

//建立wordclass父类下拉框
function createWordClassCombobox() {
	$('#wordclasstextbox').combobox({    
	    url:'../interactiveScene.action?type=createwordclasscombobox&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
	    //editable:false 
	});  
}

//根据构造场景要素下拉框
function createColumnCombobox(type,id) {
	var docid = "#"+id
	$(docid).combobox({    
	    url:'../interactiveScene.action?type='+type+'&scenariosid='+publicscenariosid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : true, // 支持多选
		separator : '|' ,// 多选的时候用“|”分隔
		editable:false 
	});  
}

//根据交互选项下拉框
function createInteractiveOptionsCombobox(wordclassid) {
	$("#interactiveoptions").combobox({    
	    url:'../interactiveScene.action?type=interactiveoptions&wordclassid='+wordclassid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : true, // 支持多选
		separator : '|' ,// 多选的时候用“|”分隔
		editable:true 
	});  
}


//保存保存场景业务对应关系
function saveRelation(){
	var  absid  = $('#kbdata').combobox('getValue');
	var  abs = $('#kbdata').combobox('getText');
	var  sid = $('#service').combotree("getValue");
	var  ser = $('#service').combotree("getText");
//	var  uquery = replaceSpace($("#userquery").val());
	if( sid==""){
		$.messager.alert('提示', "请选择业务!", "warning");
		return false;
	}
	
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addrelation',
			scenariosid:publicscenariosid,
			name: publicscenariosname,
			kbdataid : absid,
			abs:abs,
			serviceid:sid,
			service:ser,
//			query:uquery,
			resourcetype:'querymanage',
			operationtype:'A',
			resourceid:sid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#servicekbdatadatagrid").datagrid('load');	
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//加载业务摘要对应关系
function loadSceneRelation(){ 
$("#servicekbdatadatagrid")
.datagrid(
		{
			title : '业务摘要对应关系显示区',
			url : '../interactiveScene.action',
			width : 1000,
			height : 395,
			toolbar : "#servicekbdatadatagrid_tb",
			pagination : true,
			rownumbers : true,
			queryParams : {
				type : 'selectservicekbdatada',
				kbdataid : $('#kbdata').combobox('getValue'),
				serviceid :$('#service').combotree("getValue"),
//				query : replaceSpace($("#userquery").val()),
				scenariosid:publicscenariosid 
			},
			pageSize : 10,
			striped : true,
			singleSelect : true,
			columns : [ [
					{
						field : 'name',
						title : '场景名称',
						width : 230
					},
					{
						field : 'scenerelationid',
						title : '场景关系ID',
						width : 180,
						hidden:true
					},
					{
						field : 'service',
						title : '所在目录',
						width : 280
					},
					{
						field : 'serviceid',
						title : '业务ID',
						width : 50,
						hidden:true
					},
					{
						field : 'abstract',
						title : '标准问题',
						width : 300,
						formatter : function(value, row, index) {
							if (value != "" && value != null) {
								value = value.replace(/\</g, "&lt;");
								value = value.replace(/\>/g, "&gt;");
								var val = "<a title='" + value + "'>" + value
										+ "</a>";
								return val;
							} else {
								return "";
							}
						}
					},
					{
						field : 'city',
						title : '所属省份',
						width :120,
						formatter : function(value, row, index) {
							if (value != "" && value != null) {
								value = value.replace(/\</g, "&lt;");
								value = value.replace(/\>/g, "&gt;");
								var val = "<a title='" + value + "'>" + value
										+ "</a>";
								return val;
							} else {
								return "";
							}
						}
					},
					{
						field : 'abstractid',
						title : '摘要ID',
						width : 400,
						hidden:true
					},
					{
						field : 'userquery',
						title : '用户问题',
						width : 350,
						hidden:true					
					},
					{
						field : "delete",
						title : '删除',
						width : 35,
						align : 'center',
						formatter : function(value, row, index) {
							var id = row["scenerelationid"];
							var abs = row["abstract"];
							var service = row["service"];
							var serviceid = row["serviceid"];
							return '<a class="icon-delete btn_a" title="删除" onclick="deleteSceneRelation(event,'+id+',\''+abs+'\',\''+service+'\',\''+serviceid+'\')"></a>';
						}
					} ] ]
		});
		$("#servicekbdatadatagrid").datagrid('getPager').pagination( {
				showPageList : false
		});
}



// 查询问题要素
function searchElementName() {
	$('#elementnamedatagrid').datagrid('load', {
		type : 'selectelementname',
		name : replaceSpace($("#selelementname").val()),
		scenariosid:publicscenariosid
	});
}

// 加载场景要素列表
function loadElementName() {
	$("#elementnamedatagrid")
			.datagrid(
					{
						title : '场景要素显示区',
						url : '../interactiveScene.action',
						width : 950,
						height : 395,
						toolbar : "#elementnamedatagrid_tb",
						pagination : true,
						rownumbers : true,
						queryParams : {
							type : 'selectelementname',
							name : replaceSpace($("#selelementname").val()),
							scenariosid:publicscenariosid
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
										{
											field : 'name',
											title : '场景要素',
											width : 120,
											formatter : function(value, row, index) {
												if (value != "" && value != null) {
													value = value.replace(/\</g, "&lt;");
													value = value.replace(/\>/g, "&gt;");
													if(value.indexOf("_知识名称")!=-1){
														value = value.split("_")[1];
													}
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}

											}
										},
										{
											field : 'weight',
											title : '优先级',
											width : 50,
											align : 'center'
										},
										{
											field : 'wordclass',
											title : '对应词类',
											width : 160,
											formatter : function(value, row, index) {
												if (value != "" && value != null) {
													value = value.replace(/\</g, "&lt;");
													value = value.replace(/\>/g, "&gt;");
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}

											}
										},
//										{
//											field : 'infotalbepath',
//											title : '对应信息表',
//											width : 160,
//											formatter : function(value, row, index) {
//												if (value != "" && value != null) {
//													value = value.replace(/\</g, "&lt;");
//													value = value.replace(/\>/g, "&gt;");
//													var val = "<a title='" + value + "'>" + value
//															+ "</a>";
//													return val;
//												} else {
//													return "";
//												}
//
//											}
//										},
										{
											field : 'interpat',
											title : '交互口径',
											width : 200,
											formatter : function(value, row, index) {
												if (value != "" && value != null) {
													value = value.replace(/\</g, "&lt;");
													value = value.replace(/\>/g, "&gt;");
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}

											}
										},
										{
											field : 'city',
											title : '地市',
											width : 180,
											hidden:true
										},
										{
											field : 'cityname',
											title : '地市名称',
											width : 100,
											formatter : function(value, row, index) {
												if (value != ""  && value != null) {
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}
											}
										},
										{
											field : 'container',
											title : '归属',
											width : 100,
											formatter : function(value, row, index) {
												if (value != "" && value != null) {
													value = value.replace(/\</g, "&lt;");
													value = value.replace(/\>/g, "&gt;");
													if (value == '词模匹配'){
														value='系统反问';
													} else if (value == '键值补全'){
														value = '菜单询问用户';
													}
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}

											}
										},
										{
											field : 'itemmode',
											title : '填写方式',
											width : 100,
											formatter : function(value, row, index) {
												if (value != "" && value != null) {
													value = value.replace(/\</g, "&lt;");
													value = value.replace(/\>/g, "&gt;");
													var val = "<a title='" + value + "'>" + value
															+ "</a>";
													return val;
												} else {
													return "";
												}

											}
										},
										{
											field : 'isshare',
											title : '是否共享',
											width : 60,
											hidden:true
										},
										{
											field : 'scenarioselementid',
											title : 'ID',
											width : 100,
											hidden:true
										},
										{
											field : 'wordclassid',
											title : '词类ID',
											width : 100,
											hidden:true
										},
										{
											field : "delete",
											title : '删除',
											width : 35,
											align : 'center',
											formatter : function(value, row, index) {
											var id = row["scenarioselementid"];
											var weight = row["weight"];
											var name = row["name"];
											return '<a class="icon-delete btn_a" title="删除" onclick="deleteElementName(event,'
													+ id
													+ ','
													+ weight
													+ ',\''
													+ name + '\')"></a>';
											}
//										},
//										{
//											field : "edit",
//											title : '编辑要素值',
//											width : 70,
//											align : 'center',
//											formatter : function(value, row, index) {
//												var id = row["wordclassid"];
//												return '<a class="icon-edit btn_a" title="编辑要素值" onclick="openElementValueWindow(event,'+id+')"></a>';
//											}
										}
										
										] ],
						onClickRow : function(rowIndex, rowData) {
							$('#elementvaluedatagrid').datagrid('load', {
								type : 'selectword',
								wordclassid : rowData.wordclassid,
								name : replaceSpace($("#selelementvalue").val())
							});
						}
					});
	$("#elementnamedatagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
			flag ="save";
			$('#elementedit_w').window('open');
//			getCityTree("");
			getSearchCityTree2('city','edit');
			createServiceInfoCombobox();
			createWordClassCombobox();
			loadWeightCombobox("");
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
			flag ="update";
			editElement(); 
			}
		}, "-"]
	});
}

//编辑场景要素
function editElement(){
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if(row){
		$('#elementedit_w').window('open');
		getCityTree(row.cityname);
		createServiceInfoCombobox();
		createWordClassCombobox();	
		loadWeightCombobox(row.weight);
		$("#elementnametextbox").textbox('setValue', row.name);
		$("#wordclasstextbox").combobox('setValue',row.wordclass);
		$("#weightcombobox").combobox('setValue',row.weight);
		$("#infotalbepath").combobox('setValue',row.infotalbepath);
		$("#itemmode").combobox('setValue',row.itemmode);
		$("#interpat").combobox('setValue',row.interpat);
		$("#container").combobox('setValue',row.container);
		
	}else {
		$.messager.alert('提示', "请选择需编辑行!", "warning");
		return;
	}
	
}



// 加载问题要素页面的优先级下拉框
function loadWeightCombobox(weight) {
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getweight',
			scenariosid:publicscenariosid,
			weight:weight
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$('#weightcombobox').combobox( {
				valueField : 'id',
				textField : 'text',
				required : true,
				missingMessage : '对应列不能为空!',
				editable : false,
				data : data.rows,
				onLoadSuccess : function() { // 加载完成后,设置选中第一项
					var val = $(this).combobox("getData");
					if (val.length > 0) {
						for ( var item in val[0]) {
							if (item == "id") {
								$(this).combobox("select", val[0][item]);
							}
						}
					}
				}
			});
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 提交场景要素表单
function submitElementNameForm() {
	$('#elementnameform').form('submit', {
		onSubmit : function() {
			var isValid = $(this).form('enableValidation').form('validate');
			if (isValid) {
				editElementName(flag);
			}
			return false;
		}
	});
}

// 清空场景要素表单
function clearElementNameForm() {
	$("#elementnametextbox").textbox('setValue', "");
	$("#wordclasstextbox").combobox('setValue', "");
	$("#infotalbepath").combobox('setValue', "");
	$("#city").combotree('clear');
	$("#itemmode").combobox('setValue', "勾选");
	$("#isshare").combobox('setValue', "否");
	$("#interpat").val("");
	//loadWeightCombobox("");
}
//清空场景业务摘要对应关系
function clearRelation(){
	$("#service").combotree("clear");
//	$("#userquery").textbox('setValue', "");
	$("#kbdata").combobox('setValue', "");
}
// 编辑问题要素
function editElementName(flag) {
	var type ;
	var scenarioselementid; 
	var oldweight;
	var operationtype;
	if(flag=="save"){//新增操作
		type ='insertelementname';
		operationtype='A';
	}else{//修改操作
		type ='updartelementname';
		operationtype='U';
		row = $('#elementnamedatagrid').datagrid('getSelected');
		scenarioselementid = row.scenarioselementid;
		oldweight =  row.weight;
	}
	var name = replaceSpace($("#elementnametextbox").val());
	var wordclass = replaceSpace($("#wordclasstextbox").combobox('getText'));
	var weight = $("#weightcombobox").combobox('getText');
	var infotalbepath = replaceSpace($("#infotalbepath").combobox('getText'));
	var cityname = $("#city").combotree('getText');
	var city = $("#city").combotree('getValues');
	var itemmode = $("#itemmode").combobox('getText');
	var container = $("#container").combobox('getValue');
	
	//对场景名进行特殊字符验证
	if(name.indexOf('=')>-1 || name.indexOf('/')>-1 || name.indexOf('%')>-1
			|| name.indexOf('>')>-1 || name.indexOf('<')>-1 || name.indexOf('*')>-1
			|| name.indexOf('Contain')>-1 || name.indexOf('@')>-1 
			|| name.indexOf('!')>-1 || name.indexOf('$')>-1 || name.indexOf('包含')>-1){
		$.messager.alert('系统提示', "存在非法字符串!", "info");
		return;
	}
	
//	alert(container);
//	return;
	var interpat = $("#interpat").val();
	var request = {
			type :type,
			name : name,
			weight : weight,
			oldweight:oldweight,
			wordclass : wordclass,
			scenariosid : publicscenariosid,
			scenariosName : publicscenariosname,
			infotalbepath : infotalbepath,
			city:city+"",
			cityname:cityname,
			itemmode:itemmode,
			interpat:interpat,
			scenarioselementid:scenarioselementid,
			container:container,
			operationtype:operationtype,
			resourcetype:'scenariosrules',
			resourceid:publicscenariosid
			
		};
		
		var dataStr = {
			m_request : JSON.stringify(request)
		}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : request,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#elementnamedatagrid").datagrid("reload");
				clearElementNameForm();
				$('#elementedit_w').window('close');
				   //加载交互规则
				loadRule0Combobox();
				loadRule0();
				
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//删除场景关系
function deleteSceneRelation(event, id, abs, service,serviceid) {
//	alert("dd");
//	if (event.stopPropagation) {// Mozilla and Opera
//		event.stopPropagation();
//	} else if (window.event) {// IE
//		window.event.cancelBubble = true;
//	}
	$.messager.confirm('提示', '确定删除该对应关系吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deletescenerelation',
					scenerelationid : id,
					abs : abs,
					service : service,
					name : publicscenariosname,
					resourcetype:'querymanage',
					resourceid:serviceid,
					operationtype:'D'
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#servicekbdatadatagrid").datagrid("reload");
					}
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 删除场景要素
function deleteElementName(event,id,weight,name) {
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	$.messager.confirm('提示', '确定删除场景要素信息吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deleteelementname',
					name : name,
					scenarioselementid : id,
					weight:weight,
					scenariosid:publicscenariosid,
					scenariosName:publicscenariosname,
					resourcetype:'scenariosrules',
					operationtype:'D',
					resourceid:publicscenariosid
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#elementnamedatagrid").datagrid("reload");
						loadWeightCombobox("");
						   //加载交互规则
						loadRule0Combobox();
						loadRule0();
						
					}
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

//打开问题要素值页面
function openElementValueWindow(wordclassid){
	if(wordclassid!=""&& wordclassid!=null){
	 $('#elementvalueedit_w').window('open');	
	}else{
		$.messager.alert('提示', "请配置对应词类!", "warning");
		return;	
	}
	
}


// 查询场景要素值
function searchElementValue() {
	var rows = $("#elementnamedatagrid").datagrid('getSelections');
	if (rows.length == 1) {
		$('#elementvaluedatagrid').datagrid('load', {
			type : 'selectword',
			wordclassid : rows[0].wordclassid,
			name : replaceSpace($("#selelementvalue").val())
		});
	} else {
		$.messager.alert('提示', "请选择一行问题要素!", "info");
	}
}

// 加载问题要素值的列表
function loadElementValue() {
	$("#elementvaluedatagrid")
			.datagrid(
					{
						title : '场景要素值显示区',
						url : '../interactiveScene.action',
						width : 500,
						height : 395,
						toolbar : "#elementvaluedatagrid_tb",
						pagination : true,
						rownumbers : true,
						queryParams : {
							type : 'selectword',
							wordclassid : "",
							name : ""
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
								{
									field : 'word',
									title : '问题要素值',
									width : 350
								},
								{
									field : "delete",
									title : '删除',
									width : 35,
									align : 'center',
									formatter : function(value, row, index) {
										var id = row["wordid"];
										var name = row["word"];
										return '<a class="icon-delete btn_a" title="删除" onclick="deleteElementValue(event,'
												+ id
												+ ',\''
												+ name
												+ '\')"></a>';
									}
								} ] ]
					});
	$("#elementvaluedatagrid").datagrid('getPager').pagination( {
		showPageList : false
	});
}

// 提交场景要素值表单
function submitElementValueForm() {
	$('#elementvalueform').form('submit', {
		onSubmit : function() {
			var isValid = $(this).form('enableValidation').form('validate');
			if (isValid) {
				insertElementValue();
			}
			return false;
		}
	});
}

// 清空新增属性值input
function clearElementValueForm() {
	$("#selelementvalue").textbox('setValue', "");
}

// 新增属性值
function insertElementValue() {
	var name = $.trim($("#selelementvalue").val());
	if (name == '') {
		$.messager.alert('提示', "场景素值不能为空,请确认填写!", "warning");
		return;
	}
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if (!row) {
		$.messager.alert('提示', "请选择场景要素列表中的任意一行!", "warning");
		return;
	}
	var wordclassid = row.wordclassid;
	var wordclass = row.wordclass;
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'insertelementvalue',
			name : name,
			wordclassid : wordclassid,
			wordclass : wordclass
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#elementvaluedatagrid").datagrid("reload");
				clearElementValueForm();
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 删除属性值
function deleteElementValue(event, id, name) {
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if (!row) {
		$.messager.alert('提示', "请选择场景要素值对应的问题要素!", "warning");
		return;
	}
	var wordclass = row.wordclass;
	var weight = row.weight;
	$.messager.confirm('提示', '确定删除该记录吗?', function(r) {
		if (r) {
			$.ajax( {
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deleteelementvalue',
					kbdataid : kbdataid,
					kbcontentid : kbcontentid,
					weight : weight,
					elementvalueid : id,
					name : name,
					wordclass : wordclass
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$('#elementvaluedatagrid').datagrid('reload');
					}
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

// 加载数据的查询和添加下拉框
function loadCombitionCombobox() {
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		$("#selelementname" + i).html("");
		$("#sel" + i).hide();
		$("#addelementname" + i).html("");
		$("#add" + i).hide();
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'queryelement',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			$("#kbanswer").val(data.answer);
			combitionArr = [];
			combitionArr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#selelementname" + weight).html(name + ":");
				$("#sel" + weight).show();
				$("#selelementvalue" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				$("#selelementvalue" + weight).combobox("setText", "");
				$("#addelementname" + weight).html(name + ":");
				$("#add" + weight).show();
				$("#addelementvalue" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				combitionArr.push( {
					field : "condition" + weight,
					title : name,
					align : 'center'
				});
			}
			combitionArr.push( {
				field : "type",
				title : "答案类型",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '普通文本';
					} else if (value == '1') {
						return '知识点映射';
					} else {
						return '普通文本';
					}
				}
			});
			combitionArr.push( {
				field : "status",
				title : "状态    ",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '<span style="color:red;">未审核</span>';
					} else if (value == '1') {
						return '<span style="color:blue;">已审核</span>';
					} else {
						return '<span style="color:red;">未审核</span>';
					}
				}
			});
			combitionArr
					.push( {
						field : "returntxt",
						title : "答案内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								return '<div title="' + value + '">' + value
										+ '</div>';
							} else {
								return value;
							}
						}
					});
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedate = [ {
		"id" : "",
		"text" : ""
	}, {
		"id" : "0",
		"text" : "普通文本"
	}, {
		"id" : "1",
		"text" : "知识点映射"
	} ];
	$("#seltype").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedate
	});
	$("#addtype").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedate
	});
	$("#selstatus").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : [ {
			"id" : "",
			"text" : ""
		}, {
			"id" : "0",
			"text" : "待审核"
		}, {
			"id" : "1",
			"text" : "已审核"
		} ]
	});
	clearCombitionForm();
}

// 查询数据的信息
function searchCombition() {
	var conditions = [];
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var con = $("#selelementvalue" + i).combobox("getText");
		if (con == "(空)") {
			con = "";
		}
		conditions.push(con);
	}
	var returntxttype = $("#seltype").combobox("getValue");
	var status = $("#selstatus").combobox("getValue");
	$('#combitiondatagrid').datagrid('load', {
		type : 'selectcombition',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		conditions : conditions.join("@"),
		returntxttype : returntxttype,
		status : status
	});
}

// 加载数据的列表
function loadCombition() {
	$("#combitiondatagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			returntxttype : "",
			status : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ combitionArr ]
	});
	$("#combitiondatagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearCombitionForm();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editCombition();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteCombition();
			}
		}, "-", {
			text : "确认(批量)",
			iconCls : "icon-ok",
			handler : function() {
				confirmCombition();
			}
		}, "-", {
			text : "全量删除",
			iconCls : "icon-no",
			handler : function() {
				deleteAllCombition();
			}
		}, "-", {
			text : "全量确认",
			iconCls : "icon-confirmall",
			handler : function() {
				confirmAllCombition();
			}
		}, "-" ]
	});
}

// 保存回复模板
function saveModel() {
	var answer = $.trim($("#kbanswer").val());
	if (answer === null || answer === "") {
		$.messager.alert('提示', "回复模板不能为空!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'savemodel',
			answer : answer,
			kbanswerid : kbanswerid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 修改数据操作，将值放入数据编辑区
function editCombition() {
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length == 1) {
		for ( var i = 1; i < combitionArr.length - 3; i++) {
			var field = combitionArr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addelementvalue" + weight).combobox("setValue", con);
		}
		$("#addtype").combobox("setValue", rows[0].type.toString());
		$("#addreturntext").val(rows[0].returntxt);
		$("#combitionid").val(rows[0].id);
		insertorupdate_combition = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)数据
function deleteCombition() {
	var combitionid = [];
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			combitionid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deletecombition',
			combitionid : combitionid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 确认(批量)数据，将状态改为已审核
function confirmCombition() {
	var combitionid = [];
	var rows = $("#combitiondatagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			combitionid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'confirmcombition',
			combitionid : combitionid.join(",")
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 全量删除数据
function deleteAllCombition() {
	var delconfirm = "<font size=2 style='color:red;'>";
	delconfirm += "确定要全量删除吗?<br/>";
	delconfirm += "该操作将清空所有数据!<br/>";
	delconfirm += "请慎重点击 [确定] 按钮!</font>";
	$.messager.confirm("提示", delconfirm, function(r) {
		if (r) {
			var endconfirm = "<font size=2 style='color:red;'>";
			endconfirm += "最后一次确认是否全量删除<br/>";
			endconfirm += "该操作将清空所有数据!<br/>";
			endconfirm += "请慎重点击 [确定] 按钮!</font>";
			$.messager.confirm("提示", endconfirm, function(y) {
				if (y) {
					$.ajax( {
						url : '../queryelement.action',
						type : "post",
						data : {
							type : 'deleteallcombition',
							kbdataid : kbdataid,
							kbcontentid : kbcontentid
						},
						async : false,
						dataType : "json",
						success : function(data, textStatus, jqXHR) {
							$.messager.alert('提示', data.msg, "info");
							if (data.success == true) {
								$("#combitiondatagrid").datagrid("reload");
							}
//						},
//						error : function(jqXHR, textStatus, errorThrown) {
//							$.messager.alert('系统异常', "请求数据失败!", "error");
						}
					});
				}
			});
		}
	});
}

// 全量确认数据
function confirmAllCombition() {
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'confirmallcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空数据表单
function clearCombitionForm() {
	var answer = $("#kbanswer").val();
	$('#combitionform').form('clear');
	$("#kbanswer").val(answer);
	$("#addtype").combobox("setValue", "");
	$("#addreturntext").focus();
	insertorupdate_combition = 0;
}

// 新增数据
function saveCombition() {
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addelementvalue" + i).combobox("getText");
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if (count == 10) {
		$.messager.alert('提示', "请至少选择一个问题要素!", "warning");
		return;
	}
	var returntxttype = $("#addtype").combobox("getValue");
	if (returntxttype === null || returntxttype === "") {
		$.messager.alert('提示', "请选择答案类型!", "warning");
		return;
	}
	var returntxt = $.trim($("#addreturntext").val());
	var data = {};
	if (insertorupdate_combition == 0) {
		data = {
			type : 'insertcombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			returntxttype : returntxttype,
			returntxt : returntxt,
			abs : _abstract
		};
	} else {
		data = {
			type : 'updatecombition',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			returntxttype : returntxttype,
			returntxt : returntxt,
			combitionid : $("#combitionid").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
				clearCombitionForm();
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadCombition() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadcombition').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadcombitionform").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelCombition(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadcombition').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadcombition').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelCombition(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 0
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#combitiondatagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelCombition() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 0,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 加载交互规则的查询和添加下拉框
function loadRule0Combobox() {
	var robotMap = {};
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getrobotconfig'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info  = data.rows;
			for(var i = 0; i < info.length; i++){
				robotMap[info[i]["id"]] = info[i]["text"];
			}
		}
	});
	
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		$("#selrule0name" + i).html("");
		$("#selrule0" + i).hide();
		$("#addrule0name" + i).html("");
		$("#addrule0" + i).hide();
		$(".td-display" + i).css("display","none");
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'queryelement',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			rule0Arr = [];
			rule0Arr.push( {
				field : 'ck',
				checkbox : true
			});
			
			rule0Arr
			.push( {
				field : "userquestion",
				title : "用户问题",
				width : 200
			});	
			
			
			rule0Arr.push( {
				field : "weight",
				title : "规则优先级",
//				align : 'center',
				width : 100,
				hidden:true
			});
			
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				if (name=='robotid'){
					robot=i;
				}
				if(name.indexOf("_知识名称")!=-1){
					name = name.split("_")[1];
				}
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#selrule0name" + weight).html(name + ":");
				$("#selrule0" + weight).show();
				$("#selrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				$("#selrule0value" + weight).combobox("setText", "");
//				$("#addrule0name" + weight).html(name + ":");
				$(".td-display" + weight).css("display","");
				$("#addrule0name" + weight).html(name);
				$("#addrule0" + weight).show();
				if (name != "知识名称" && name != "常见问题"){
					$("#addrule0name" + weight).css("color","green");
				}
				$("#addrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				rule0Arr.push( {
					field : "condition" + weight,
					title : name,
//					align : 'center',
					width : 150,
					formatter : function(value, row, index) {
						if (value == '交互') {
							return '<用户未选或未告知,系统提示输入>';
						} else if (value == '已选') {
							return '<用户告知或已选择，系统可获知>';
						} else if (value == '缺失'){
							return '<用户未选或未告知>';
						} else if (value.indexOf('ROBOT_') > -1){
							return robotMap[value];
						} else{
							return value;
						}
					}
				});
			}
			rule0Arr.push( {
				field : "type",
				title : "规则类型",
//				align : 'center',
				width : 150,
				hidden:true,
				formatter : function(value, row, index) {
					if (value  == '0') {
						return '缺失补全规则';
					} else if (value == '1') {
						return '问题要素冲突判断规则';
					} else if (value == '3'){
						return '识别规则';
					}else if (value == '4'){
						return '数据';
					}
					else{
						return '其他规则';
					}
				}
				
			});
			rule0Arr.push( {
				field : "citycode",
				title : "地市编码",
//				align : 'center',
				width : 100,
				hidden:true
			});
			rule0Arr.push( {
				field : "excludedcity",
				title : "排除地市编码",
//				align : 'center',
				width : 100,
				hidden:true
				
			});
			rule0Arr.push({
				field : "cityname",
				title : "地市名称",
//				align : 'center',
				width : 100,
//				hidden:true,
				formatter : function(value, row, index) {
					if (value == null || value=="") {
						return "全国";
					} else {
					 return value;
					}
				}
			});
			rule0Arr.push( {
				field : "excludedcityName",
				title : "失效地市",
//				align : 'center',
				width : 100
//				hidden:true
			}); 
			
			rule0Arr.push( {
				field : "responsetype",
				title : "回复文本类型",
//				align : 'center',
				width : 100, 
				hidden:true
				
			});
			rule0Arr.push( {
				field : "ruleresponsetemplate",
				title : "答案模板",
//				align : 'center',
				width : 100,
				hidden:true,
				formatter: function(value, row, index){
					if (value != null){
						if (value.length > 200){
							return '<a href="javascript:void(0)" title="查看详情"  onclick="detail(event,' + index + ')">查看详情</a>'
						} else {
							return '<div>' + value + '</div>';
						}
					}
				}
			});
			rule0Arr
					.push( {
						field : "response",
						title : "回复内容",
						width : 300,
						formatter: function(value, row, index){
						if (value != null){
//							value = value.replace(';###;','<br/>其他形式答案：<br/>');
							if (value.length > 200){
//								if ('1'==row.isedit){
//									return '<a href="javascript:void(0)" title="查看详情" style="color:red;" onclick="detail(event,' + index + ')">查看详情</a>'
//								}
								return '<a href="javascript:void(0)" title="查看详情"  onclick="detail(event,' + index + ')">查看详情</a>'
							} else {
//								if ('1'==row.isedit){
//									return '<div style="color:red;">' + value + '</div>';
//								}
								return '<div>' + value + '</div>';
							}
						}
				}
//						formatter : function(value, row, index) {
//							if (value != null) {
//								value = value.replace(/</g, "&lt;").replace(
//										/>/g, "&gt;");
//								return "<div title='" + value + "'>" + value
//										+ "</div>";
//							} else {
//								return value;
//							}
//						}
					});
			
//			rule0Arr
//			.push(
//					{
//						field : "edit",
//						title : '编辑交互选项',
//						width : 80,
//						align : 'center',
//						formatter : function(value, row, index) {
//							var id = row["wordclassid"];
//							return '<a class="icon-edit btn_a" title="编辑交互选项" onclick="editMenuItems(event,'+id+')"></a>';
//						}
//					}		
//			
//			);
	
			
			
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedata = [ 
	                {
	             	 "id" : "全部",
	             	 "text" : "全部"
	             	 } ,                
	           	{
	           		"id" : "0",
	           		"text" : "缺失补全规则"
	           	}
	           	,{
	           		"id" : "4",
	           		"text" : "数据"
	           	}
	           	
	           	];
	
	$("#selrule0type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "全部"
	});
//	$("#selrule0type").combobox("disable");
//	$("#addrule0type").combobox( {
//		valueField : 'id',
//		textField : 'text',
//		editable : false,
//		panelHeight : 'auto',
//		data : typedata,
//		value : "0"
//	});
//	$("#addrule0type").combobox("disable");
	clearRule0Form();
}

//加载语义规则的查询和添加下拉框
function s_loadRule0Combobox() {
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		$("#s_selrule0name" + i).html("");
		$("#s_selrule0" + i).hide();
		$("#s_addrule0name" + i).html("");
		$("#s_addrule0" + i).hide();
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'queryelement',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			rule0Arr = [];
			rule0Arr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#s_selrule0name" + weight).html(name + ":");
				$("#s_selrule0" + weight).show();
				$("#s_selrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				$("#s_selrule0value" + weight).combobox("setText", "");
				$("#s_addrule0name" + weight).html(name + ":");
				$("#s_addrule0" + weight).show();
				$("#s_addrule0value" + weight).combobox( {
					valueField : 'id',
					textField : 'text',
					editable : false,
					data : elementvalue
				});
				rule0Arr.push( {
					field : "condition" + weight,
					title : name,
//					align : 'center',
					width : 150
					
				});
			}
			rule0Arr.push( {
				field : "type",
				title : "规则类型",
//				align : 'center',
				width : 150,
				hidden:true,
				formatter : function(value, row, index) {
					if (value == '0') {
						return '缺失补全规则';
					} else if (value == '1') {
						return '问题要素冲突判断规则';
					} else if (value == '3'){
						return '识别规则';
					}else{
						return '其他规则';
					}
				}
				
			});
			rule0Arr.push( {
				field : "weight",
				title : "规则优先级",
//				align : 'center',
				width : 100
			});
			rule0Arr.push( {
				field : "cityname",
				title : "地市名称",
//				align : 'center',
				width : 100
				
			});
			rule0Arr.push( {
				field : "citycode",
				title : "地市编码",
//				align : 'center',
				width : 100,
				hidden:true
			});
			rule0Arr.push( {
				field : "excludedcity",
				title : "排除地市编码",
//				align : 'center',
				width : 100,
				hidden:true
				
			});
			rule0Arr
					.push( {
						field : "response",
						title : "回复内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								value = value.replace(/</g, "&lt;").replace(
										/>/g, "&gt;");
								return "<div title='" + value + "'>" + value
										+ "</div>";
							} else {
								return value;
							}
						}
					});
			
//			rule0Arr
//			.push(
//					{
//						field : "edit",
//						title : '编辑交互选项',
//						width : 80,
//						align : 'center',
//						formatter : function(value, row, index) {
//							var id = row["wordclassid"];
//							return '<a class="icon-edit btn_a" title="编辑交互选项" onclick="editMenuItems(event,'+id+')"></a>';
//						}
//					}		
//			
//			);
			
			
			
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedata = [ {
		"id" : "0",
		"text" : "缺失补全规则"
	}, {
		"id" : "1",
		"text" : "问题要素冲突判断规则"
	}, {
		"id" : "2",
		"text" : "其他规则"
	},{
		"id" : "3",
		"text" : "识别规则"
	}
	
	];
	$("#s_selrule0type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "3"
	});
	$("#s_selrule0type").combobox("disable");
	$("#s_addrule0type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "3"
	});
	$("#s_addrule0type").combobox("disable");
	clearRule0Form();
}

// 查询缺失补全规则的信息
function searchRule0() {
	var conditions = [];
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var con = $("#selrule0value" + i).combobox("getValue");
		if (con == "(空)") {
			con = "";
		}
		conditions.push(con);
	}
	
	var city = $("#search_city").combotree("getValue");
	if(city == ""|| city == null|| city == undefined){
		city ="全国";
	}
	
	var search_ruleresponse = $("#search_ruleresponse").textbox("getText");
//	var weight = $("#selrule0weight").numberbox("getValue");
//	var rt = $("#selrule0type").combobox("getValue");
//	if(rt=="全部"){
//		rt="";
//	} 
	var issue = $("#issueChoose").combobox('getValue');
//	alert(issue);
	$('#rule0datagrid').datagrid('load', {
		type : 'selectrule',
		scenariosid : publicscenariosid,
		conditions : conditions.join("@"),
		ruletype : "",
		weight : "",
		city:city,
		ruleresponse:search_ruleresponse,
		issue:issue
		
	});
}

// 加载缺失补全规则的列表
function loadRule0() {
	
//	alert("加载规则");
	$("#rule0datagrid").datagrid( {
		url : '../interactiveScene.action',
		height : 300,
		width:1000,
		pagination : true,
		rownumbers : true,
		toolbar : '#rule0datagridTools',
		queryParams : {
			type : 'selectrule',
			scenariosid : publicscenariosid,
			conditions : "",
			ruletype : "",
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : false,
		fitColumns : true,
		singleSelect : false,
		columns : [ rule0Arr ],
		rowStyler: function(index,row){
			if (row.isedit=='1'){
				return 'background-color:#F08080;';
			} else if (row.isedit=='-1'){
				return 'background-color:#6495ED;';
			}
		}
	});
	$("#rule0datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
//			$('#w').window('open');
				clearRule0Form(); 
				$('#rule0_edit').panel({title:"新增规则",iconCls:'icon-add'});
				$('#rule0_edit').panel('open');
				$('#rule0_edit').panel('expand',true);
				insertorupdate_rule0 = 0;
//				createServiceInfoCombobox2();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				
				editRule0();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule0();
			}
//		}, "-", {
//			text : "新增列",
//			iconCls : "icon-add",
//			handler : function() {
//			addColumn();
//			}
//		}, "-", {
//			text : "场景元素编辑",
//			iconCls : "icon-edit",
//			handler : function() {
//			elementdialog();
//			}
//		}, "-",{
//			text : "删除列",
//			iconCls : "icon-remove",
//			handler : function() {
//			$('#column_delete').window('open');
//			createColumnCombobox('createdeletecolumncombobox','columncom_delete');	
//			}
		}
//		,"-", {
//			text : "编辑交互选项",
//			iconCls : "icon-edit",
//			handler : function() {
//			editMenuItems();
//			}
//		}
		, "-" , {
			text : "上移",
			iconCls : "icon-upload",
			handler : function() {
			moveRule();
			}
		},"-", {
			text : "复制",
			iconCls : "icon-add",
			handler : function() {
			copyRule();
			} 
		},"-"] 

	});

	// 发布状态选择
	$("#issueChoose").combobox({
		valueField : 'id',
		textField : 'text',
		width : 100,
		panelHeight : 'auto',
//		data : [
//		    {
//			id : '0',
//			text : '未发布'
//		    },{
//		    id : '1',
//			text : '已发布'
//		}]
		url : '../interactiveScene.action?type=getIssueData',
		onLoadSuccess : function (){
		$("#issueChoose").combobox('setValue','线下规则');
		},
		onChange : function(newValue){
			if (newValue == 'scenariosrules'){
//				alert("未发布");
				$("#rule0datagrid").datagrid('getPager').pagination( {
					showPageList : false,
					buttons : [ {
						text : "新增",
						iconCls : "icon-add",
						handler : function() {
//						$('#w').window('open');
							clearRule0Form(); 
							$('#rule0_edit').panel({title:"新增规则",iconCls:'icon-add'});
							$('#rule0_edit').panel('open');
							$('#rule0_edit').panel('expand',true);
							insertorupdate_rule0 = 0;
						}
					}, "-", {
						text : "修改",
						iconCls : "icon-edit",
						handler : function() {
							editRule0();
						}
					}, "-", {
						text : "删除(批量)",
						iconCls : "icon-remove",
						handler : function() {
							deleteRule0();
						}
					}, "-", {
						text : "场景元素编辑",
						iconCls : "icon-edit",
						handler : function() {
						elementdialog();
						}
//					}, "-", {
//						text : "新增列",
//						iconCls : "icon-add",
//						handler : function() {
//						addColumn();
//						}
//					}, "-",{
//						text : "删除列",
//						iconCls : "icon-remove",
//						handler : function() {
//						$('#column_delete').window('open');
//						createColumnCombobox('createdeletecolumncombobox','columncom_delete');	
//						}
					}
//					,"-", {
//						text : "编辑交互选项",
//						iconCls : "icon-edit",
//						handler : function() {
//						editMenuItems();
//						}
//					}
					, "-" , {
						text : "上移",
						iconCls : "icon-upload",
						handler : function() {
						moveRule();
						}
					},"-", {
						text : "复制",
						iconCls : "icon-add",
						handler : function() {
						copyRule();
						} 
					},"-"] 

				});
				searchRule0();
			}else if(newValue == 'scenariosrules_online'){
//				alert("已发布");
				$("#rule0datagrid").datagrid('getPager').pagination( {
					showPageList : false,
					buttons : [{
						text : "下线",
						iconCls : "icon-no",
						handler : function() {
									deleteOnlineRule0();
								}
						},"-"]
				});
				searchRule0();
			}
		}
	});
	
	$("#elementdialog").dialog({
		top : 20,
		left : 50,
		width : 1000,    
	    height : 480,
		closed : true,
		modal : true
	});
}

// 场景要素编辑
function elementdialog(){
	$("#elementdialog").dialog('open');
	
	loadElementName();
}

//加载语义规则的列表
function s_loadRule0() {
//	alert("加载规则");
	$("#s_rule0datagrid").datagrid( {
		url : '../interactiveScene.action',
		height : 300,
		width:1000,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			scenariosid : publicscenariosid,
			conditions : "",
			ruletype : 3,
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ rule0Arr ]
	});
	$("#s_rule0datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增规则",
			iconCls : "icon-add",
			handler : function() {
//			$('#w').window('open');
				clearRule0Form(); 
				$('#s_rule0_edit').panel({title:"新增规则",iconCls:'icon-add'});
				$('#s_rule0_edit').panel('open');
				$('#s_rule0_edit').panel('expand',true);
				insertorupdate_rule0 = 0;
			}
		}, "-", {
			text : "修改规则",
			iconCls : "icon-edit",
			handler : function() {
				editRule0();
			}
		}, "-", {
			text : "删除规则(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule0();
			}
		}, "-", {
			text : "新增列",
			iconCls : "icon-add",
			handler : function() {
			addColumn();
			}
		}, "-",{
			text : "删除列",
			iconCls : "icon-remove",
			handler : function() {
			$('#column_delete').window('open');
			createColumnCombobox('createdeletecolumncombobox','columncom_delete');	
			}
		},"-", {
			text : "编辑交互选项",
			iconCls : "icon-edit",
			handler : function() {
			editMenuItems();
			}
		}, "-" ]
	});
}



//添加列
function addColumn(){
	$('#column_add').window('open');
	createColumnCombobox('createcolumncombobox','columncom');
}

//删除列
function deleteColumn(){

	var columns = $('#columncom_delete').combobox("getText");
	if(columns==""){
		return false;
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'deletecolumn',
			scenariosid :publicscenariosid,
			columns : columns,
			resourcetype:'scenariosrules',
			operationtype:'D',
			resourceid:publicscenariosid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				 //加载交互规则
				loadRule0Combobox();
				loadRule0();
				//刷新元素列表
				$("#elementnamedatagrid").datagrid("reload");
				//关闭窗口
				$('#column_delete').window('close');
				//清空列选项值
				$("#columncom_delete").combobox("setValue", "");
				
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});

}

//清空添加列选项内容

function clearColumn(){
	 $('#columncom').combobox("setValue","");
}
//清空删除选项列内容
function clearDeleteColumn(){
	 $('#columncom_delete').combobox("setValue","");
}

//保存添加列
function saveColumn(){
	var columns = $('#columncom').combobox("getText");
	if(columns==""){
		return false;
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addcolumn',
			scenariosid :publicscenariosid,
			columns : columns,
			resourcetype:'scenariosrules',
			operationtype:'A',
			resourceid:publicscenariosid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				 //加载交互规则
				loadRule0Combobox();
				loadRule0();
				//刷新元素列表
				$("#elementnamedatagrid").datagrid("reload");
				//关闭窗口
				$('#column_add').window('close');
				//清空列选项值
				$("#columncom").combobox("setValue", "");
				
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 修改缺失补全规则操作，将值放入编辑区
function editRule0() {
	
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		excludedcity = rows[0].excludedcity;
		if (excludedcity==null){
			excludedcity='';
		}
		publiccitycode = rows[0].citycode;
		ruleresponsetemplate =  rows[0].ruleresponsetemplate;
		var responsetype = rows[0].responsetype;
		var userquestion = rows[0].userquestion;
		$("#responsetype").combobox("setValue", responsetype);
		if(responsetype=="2"){
			$("#templatecolumn_btn").hide();
			$("#triggerAction_btn").hide();
			$("#hitquestion_btn").hide();
			$(".serviceinfo_tr").css("display","none");
			$("#interactiveoptions_btn").show();
		}else{
			$("#templatecolumn_btn").show();
			$("#triggerAction_btn").show();
			$("#hitquestion_btn").show();
			$(".serviceinfo_tr").css("display","");
			$("#interactiveoptions_btn").hide();
		}
		
		for ( var i = 3; i < rule0Arr.length - 7; i++) {
			var field = rule0Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule0value" + weight).combobox("setValue", con);
		}
		$("#span_addrule0weight").hide();
		$("#input_addrule0weight").hide();
		$("#addrule0weight").combobox("setValue", rows[0].weight);
		$('#adduserquestion').textbox('setValue',userquestion);
		var answer =rows[0].response;
//		var ruleresponsetemplate = rows[0].ruleresponsetemplate;
		if(answer == null){
			answer ="";
		}
		//给富文本框赋值，不追加
		UM.getEditor('myEditor').setContent(answer, false);
		$("#rule0id").val(rows[0].id);
		insertorupdate_rule0 = 1;
		
		var cityCode  = rows[0].citycode;
		if(cityCode=="全国"){
		  cityCode =="";
		}
		currentcitycode = cityCode;
		$('#edit_excludedcity').combotree('setValues',excludedcity);
		$('#edit_city').combotree('setValue', cityCode);
		$('#edit_city').combotree('setValue', cityCode);
		$('#rule0_edit').panel({title:"修改规则",iconCls:'icon-edit'});
		$('#rule0_edit').panel('open');
		$('#rule0_edit').panel('expand',true);
		if (responsetype=="0" || responsetype=="-1"){
			var content="";
			var serviceinfo = "";
			var knoname = "";
			var serviceinfotemplate = "";
			var attrvalue = "";
			if (ruleresponsetemplate.indexOf(";")!=-1){
				var arry = ruleresponsetemplate.split(";");
				if(arry[0].indexOf("信息表")!=-1){
					//给富文本框赋值，不追加
					UM.getEditor('myEditor').setContent(answer.substring(answer.indexOf(";")+1, answer.length ), false);
					if(arry.length>1){
						content = arry[0]+";"; 
						serviceinfotemplate = arry[0];
						var tmp = content.substring(content.indexOf("(")+1, content.lastIndexOf(")"));
						serviceinfo = tmp.split(",")[0];
						knoname = tmp.split(",")[2];
						serviceinfo = serviceinfo.substring(1,serviceinfo.length-1);
						knoname = knoname.substring(1,knoname.length-1);
						attrname = knoname.split("|")[0];
						attrvalue = knoname.split("|")[1];
					}
					var serviceinfoData = $("#serviceinfo").combobox("getData");
					var serviceid = "";
					for(var i = 0; i < serviceinfoData.length; i++){
						if(serviceinfoData[i].text === serviceinfo){
							serviceid = serviceinfoData[i].id;
						}
					}
					$("#serviceinfo").combobox("select",serviceid);
					$("#attrname").combobox("setValue",attrname);
					$("#attrvalue").combobox("setValue",attrvalue);
					$("#serviceinfotemplate").textbox("setValue",content);
					
//					var attrnameData = $("#attrname").combobox("getData");
//					var attrnameid = "";
//					for(var i = 0; i < attrnameData.length; i++){
//						if(attrnameData[i].text === attrname){
//							attrnameid = attrnameData[i].id;
//						}
//					}
//					$("#attrname").combobox("select",attrnameid);
//					
//					var attrvalueData = $("#attrvalue").combobox("getData");
//					var attrvalueid = "";
//					for(var i = 0; i < attrvalueData.length; i++){
//						if(attrvalueData[i].text === attrvalue){
//							attrvalueid = attrvalueData[i].id;
//						}
//					}
//					$("#attrvalue").combobox("select",attrvalueid);
					
					
				}
			}
//			$('#serviceinfo').combobox('setText',serviceinfo);
//			$('#serviceinfotemplate').textbox('setText',serviceinfotemplate);
		}
		if(responsetype=="2"){
			 UM.getEditor('myEditor').setDisabled('fullscreen');
			 disableBtn("enable");
		}else{
			UM.getEditor('myEditor').setEnabled();
		    enableBtn();
		}
//		$('#w').window('open');
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
	
}


//修改缺失补全规则操作，将值放入编辑区
function editRule0_2() {
	
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		excludedcity = rows[0].excludedcity;
		publiccitycode = rows[0].citycode;
		for ( var i = 3; i < rule0Arr.length - 7; i++) {
			var field = rule0Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule0value" + weight).combobox("setValue", con);
		}

		$("#addrule0weight").combobox("setValue", rows[0].weight);
		var answer =rows[0].response;
		if(answer == null){
			answer ="";
		}
		//给富文本框赋值，不追加
		UM.getEditor('myEditor').setContent(answer, false);
		$("#rule0id").val(rows[0].id);
		insertorupdate_rule0 = 1;
	
	}
	
}



function editMenuItems() {
	var arr = [];
	var l ;
	
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		editRule0_2();
		excludedcity = rows[0].excludedcity;
		publiccitycode = rows[0].citycode;
		for ( var i = 1; i < rule0Arr.length - 6; i++) {
			var field = rule0Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
		if(con=="交互"){
		 arr.push(con);
		 l=weight;
		}
	}
     if(arr.length<1){
       $.messager.alert('提示', "当前规则列不存在<用户未选或未告知,系统提示输入>值!", "warning"); 
       return false;
     }
     clearMenuItems();
    
     publicrulesid = rows[0].id;
     $("#interactivetemplate_div").hide();
     
     
     
     var ruleresponsetemplate = rows[0].ruleresponsetemplate;
     var interactiveoptionsstr="";
     var responsetype = rows[0].responsetype;
     if(responsetype=="2"){
    	   if(ruleresponsetemplate!=""&&ruleresponsetemplate!=null){
    	    	 interactiveoptionsstr = ruleresponsetemplate.split('","')[1].split('")')[0];
    	} 
     }
     
 	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getmenuitemsinfo',
			weight:l,
			scenariosid :publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var wordclassid = data.wordclassid;
			if(wordclassid==""|| wordclassid==null ){
				  $.messager.alert('提示', "当前规则交互列未配置对应词类!", "warning"); 
			       return false;
			}else{
				 $('#menuItems_edit').window('open');
				$("#correspondingwordclass_div").hide();
				$("#correspondingwordclass").val(wordclassid);
				var itemmode = data.itemmode; 
				if(itemmode=="勾选+自定义"){
				 $("#interactivetemplate_div").show();	
				}else if(itemmode=="自定义"){
				 $("#interactivetemplate_div").show();	
				}
				createInteractiveOptionsCombobox(wordclassid); 
				$('#interactiveoptions').combobox('setValues',interactiveoptionsstr.split('||'));
				$('#interactionname').textbox('setValue',data.name);
				$('#interactivetemplate').val(data.interpat);
				$('#customvalue').val('多条值以|分隔开');
			} 
			
			
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
		
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
	
}


function saveMenuItems(){
	
//
	
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addrule0value" + i).combobox("getText");
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
	}

	var weight = $("#addrule0weight").combobox("getValue");
	var content = UM.getEditor('myEditor').getContent();
	var response = $.trim(content);

	
	
	
	var  interactiveoptions  = $('#interactiveoptions').combobox('getText');
	var interpat  = $("#interactivetemplate").val();
	var customvalue = $("#customvalue").val();
	if(customvalue == "多条值以|分隔开"){
		customvalue="";
	}
	
	if(customvalue=="" && interactiveoptions==""){
		 $.messager.alert('提示', "请设置交互项选项值!", "warning"); 
	       return false;
	}
	var wordclassid = $("#correspondingwordclass").val();
	
		$.ajax( {
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'savemenuitemsinfo',
				scenariosid :publicscenariosid,
				ruleid:publicrulesid,
				interpat:interpat,
				interactiveoptions:interactiveoptions,
				customvalue:customvalue,
				
				conditions : conditions.join("@"),
				weight : weight,
				ruletype : 0,
				ruleresponse : response,
				excludedcity :excludedcity,
				city:publiccitycode,
				wordclassid:wordclassid,
				resourcetype:'scenariosrules',
				operationtype:'A',
				resourceid:publicscenariosid
				
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示', data.msg, "info");
				if (data.success == true) { 
					clearMenuItems();
					clearRule0Form();
					$('#menuItems_edit').window('close');
					$("#rule0datagrid").datagrid("reload");
				}
//			},
//			error : function(jqXHR, textStatus, errorThrown) {
//				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});

}

function clearMenuItems(){
	$('#interactionname').textbox('setValue','');
	 $('#interactiveoptions').combobox('setValue',"");
	$('#interactivetemplate').val('');
	$("#customvalue").val('');
	
	
}



// 删除(批量)缺失补全规则
function deleteRule0() {
	
	$.messager.confirm('提示', '确定删除规则吗?', function(r) {
		if (r) {
			var ruleid = [];
			var cityArry =[];
			var excludedcityArry =[];
			var rows = $("#rule0datagrid").datagrid("getSelections");
			if (rows.length > 0) {
				for ( var i = 0; i < rows.length; i++) {
					ruleid.push(rows[i].id);
					var ccode = rows[i].citycode;
					if(ccode==""||ccode==null){
						ccode ="@";	
					}
					cityArry.push(ccode);
					var ecity = rows[i].excludedcity;
					if(ecity==""||ecity==null){
						ecity ="@";	
					}
					
					excludedcityArry.push(ecity);
				}
			} else {
				$.messager.alert('提示', "请至少选择一行!", "warning");
				return;
			}
			$.ajax( {
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deleterule',
					ruleid : ruleid.join(","),
					scenariosName : publicscenariosname,
					currentcitycode:cityArry.join(","),
					excludedcity:excludedcityArry.join(",")
//					resourcetype:'scenariosrules',
//					operationtype:'D',
//					resourceid:publicscenariosid
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#rule0datagrid").datagrid("reload");
					}
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
			

      }

	});

}

// 清空缺失补全规则表单
function clearRule0Form() {
	dnonameDefaultText = "";
//	$('#rule0form').form('clear');
//	$("#addrule0type").combobox("setValue", "0");
//	$("#addrule0weight").numberbox("setValue", "");
//	$("#addrule0response").focus();
//	insertorupdate_rule0 = 0;
	
	$('#rule0form').form('clear');
//	$("#addrule0type").combobox("setValue", "0");
	$("#responsetype").combobox("setValue", "0");
	
	$("#serviceinfo").combobox("clear");
//	$("#knoname").combobox("clear");
//	$("#serviceinfotemplate").textbox("setValue", "");
	
	
	$("#interactiveoptions_btn").hide();
	$("#templatecolumn_btn").show();
	$("#triggerAction_btn").show();
	$("#hitquestion_btn").show();
	$(".serviceinfo_tr").css("display","");
	$("#addrule0weight").combobox("setValue", "");
	$("#addrule0response").focus();
	UM.getEditor('myEditor').setContent("", false);
	 UM.getEditor('myEditor').setEnabled();
//	insertorupdate_rule0 = 0;
	
	$("#span_addrule0weight").hide();
	$("#input_addrule0weight").hide();
	$('#adduserquestion').textbox('setValue','');
	
}

// 新增或修改缺失补全规则
function saveRule0(flag) {
//	alert($("#rule0id").val());
	var conditions = [];
	var arr =[];
	var count = 0;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addrule0value" + i).combobox("getValue");
		if(cond=="交互"){
		arr.push(cond); 
		}
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if(arr.length>1){
		$.messager.alert('提示', "规则列只允许一条存在<用户未选或未告知,系统提示输入>值!", "warning");
		return;	
	}
//	if (count == 10) {
//		$.messager.alert('提示', "请至少选择一个场景要素!", "warning");
//		return;
//	}
	var weight = $("#addrule0weight").combobox("getValue");
	var  userquestion = $('#adduserquestion').textbox('getValue');
//	if (weight === null || weight === "") {
//		$.messager.alert('提示', "请输入优先级!", "warning");
//		return;
//	}
//	var content = UM.getEditor('myEditor').getContent();
	
	
	var response = UM.getEditor('myEditor').getContent().replaceAll('<p>', "").replaceAll("</p>","");
	var responsetype = $("#responsetype").combobox("getValue");
	
	if(responsetype=="0"  && flag != 'copy'){
		response = response.replaceAll(',', "，");
	}
	
	if(response.endWith("<br/>")){
		response = response.substring(0,response.lastIndexOf("<br/>"));
	}
//	var content = UM.getEditor('myEditor').getPlainTxt();
//	var response = $.trim(content);
//	if(response.indexOf("\r\n")!= -1){
//		response = response.replace(new RegExp(/\r\n/g),'<br/>');
//	}else if(response.indexOf("\n")!= -1){
//		response = response.replace(new RegExp(/\n/g),'<br/>');
//	}else if(response.indexOf("\r") != -1){
//		response = response.replace(new RegExp(/\r/g),'<br/>');
//	} 
	
        
		if(responsetype=="0" || responsetype=="-1"){
//			ruleresponsetemplate = '提示用户("'+response+'")';
			response =  $('#serviceinfotemplate').textbox('getText')+$.trim(response);
			ruleresponsetemplate =  $('#serviceinfotemplate').textbox('getText')+$.trim(response);
		}
		else if(responsetype=="1"){
			content = UM.getEditor('myEditor').getContentTxt();
			response = $.trim(content);
			ruleresponsetemplate = '提示用户("'+response+'")';
		}
		else if(responsetype=="2"){
			if(ruleresponsetemplate.indexOf("菜单询问")==-1){
				$.messager.alert('提示', "回复类型和答案格式不一致，请重新选择回复类型!", "warning");
				return;	
			}
		}
		if(responsetype=="3"){
			content = UM.getEditor('myEditor').getContentTxt();
			response = $.trim(content);
			ruleresponsetemplate = response;
		}
		
		var cityCode = 	$('#edit_city').combotree('getValue');
		var copyCity =  $('#search_city').combotree('getValue');
		var excludedcitys = $('#edit_excludedcity').combotree('getValues');
		var sexcludedcity = excludedcitys.join(',');
		
		var robotid;
		 if(robot>-1){
			 robotid=conditions[robot];
		 }
		
		var data = {};
	if (insertorupdate_rule0 == 0) {
		data = {
			type : 'insertrule',
			scenariosid : publicscenariosid,
			scenariosName : publicscenariosname,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 0,
			ruleresponse : response,
			ruleresponsetemplate:ruleresponsetemplate,
			responsetype:responsetype,
			city:cityCode,
			excludedcity:sexcludedcity,
			flag:flag,
			copycity:copyCity,
			userquestion:userquestion
//			resourcetype:'scenariosrules',
//			operationtype:'A',
//			resourceid:publicscenariosid,
//			robotid:robotid
			
		};
	} else {
		data = {
			type : "updaterule",
			scenariosid : publicscenariosid,
			scenariosName : publicscenariosname,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 0,
			ruleresponse : response,
			ruleid : $("#rule0id").val(),
			excludedcity :sexcludedcity,
			city:cityCode,
			ruleresponsetemplate:ruleresponsetemplate,
			responsetype:responsetype,
			userquestion:userquestion,
			currentcitycode:currentcitycode
//			resourcetype:'scenariosrules',
//			operationtype:'U',
//			resourceid:publicscenariosid,
//			robotid:robotid
			
		};
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule0datagrid").datagrid("reload");
				clearRule0Form();
				$('#rule0_edit').panel('close');
//				$('#w').window('close');
				 //加载交互规则
				loadRule0Combobox();
				loadRule0();
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 更新业务规则
function updateRuleNLP() {
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : "updaterulenlp"
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadRule0() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadrule0').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadrule0form").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelRule0(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadrule0').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadrule0').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelRule0(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 1
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule0datagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelRule0() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 1,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 加载问题要素冲突判断规则的查询和添加下拉框
function loadRule1Combobox() {
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		$("#selrule1name" + i).html("");
		$("#selrule1" + i).hide();
		$("#addrule1name" + i).html("");
		$("#addrule1" + i).hide();
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'queryelement',
			scenariosid : publicscenariosid
			
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			var info = data.rows;
			rule1Arr = [];
			rule1Arr.push( {
				field : 'ck',
				checkbox : true
			});
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				var weight = info[i]["weight"];
				$("#selrule1name" + weight).html(name + ":");
				$("#selrule1" + weight).show();
				$("#selrule1value" + weight).textbox("setValue", "");
				$("#addrule1name" + weight).html(name + ":");
				$("#addrule1" + weight).show();
				$("#addrule1value" + weight).textbox("setValue", "");
				rule1Arr.push( {
					field : "condition" + weight,
					title : name,
					align : 'center'
				});
			}
			rule1Arr.push( {
				field : "type",
				title : "规则类型",
				align : 'center',
				formatter : function(value, row, index) {
					if (value == '0') {
						return '缺失补全规则';
					} else if (value == '1') {
						return '问题要素冲突判断规则';
					} else {
						return '其他规则';
					}
				}
			});
			rule1Arr.push( {
				field : "weight",
				title : "规则优先级",
				align : 'center'
			});
			rule1Arr
					.push( {
						field : "response",
						title : "回复内容",
						width : 300,
						formatter : function(value, row, index) {
							if (value != null) {
								value = value.replace(/</g, "&lt;").replace(
										/>/g, "&gt;");
								return "<div title='" + value + "'>" + value
										+ "</div>";
							} else {
								return value;
							}
						}
					});
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	var typedata = [ {
		"id" : "0",
		"text" : "缺失补全规则"
	}, {
		"id" : "1",
		"text" : "问题要素冲突判断规则"
	}, {
		"id" : "2",
		"text" : "其他规则"
	} ];
	$("#selrule1type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "1"
	});
	$("#selrule1type").combobox("disable");
	$("#addrule1type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata,
		value : "1"
	});
	$("#addrule1type").combobox("disable");
	clearRule1Form();
}

// 查询问题要素冲突判断规则的信息
function searchRule1() {
	var conditions = [];
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var con = $("#selrule1value" + i).textbox("getValue");
		conditions.push(con);
	}
	var weight = $("#selrule1weight").numberbox("getValue");
	$('#rule1datagrid').datagrid('load', {
		type : 'selectrule',
		kbdataid : kbdataid,
		kbcontentid : kbcontentid,
		conditions : conditions.join("@"),
		ruletype : 1,
		weight : weight
	});
}

// 加载问题要素冲突判断规则的列表
function loadRule1() {
	$("#rule1datagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			ruletype : 1,
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ rule1Arr ]
	});
	$("#rule1datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearRule1Form();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule1();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule1();
			}
		}, "-" ]
	});
}

// 修改问题要素冲突判断规则操作，将值放入编辑区
function editRule1() {
	var rows = $("#rule1datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		for ( var i = 1; i < rule1Arr.length - 3; i++) {
			var field = rule1Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule1value" + weight).textbox("setValue", con);
		}

		$("#addrule1weight").numberbox("setValue", rows[0].weight);
		$("#addrule1response").val(rows[0].response);
		$("#rule1id").val(rows[0].id);
		insertorupdate_rule1 = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)问题要素冲突判断规则
function deleteRule1() {
	var ruleid = [];
	var rows = $("#rule1datagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			ruleid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deleterule',
			ruleid : ruleid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空问题要素冲突判断规则表单
function clearRule1Form() {
	$('#rule1form').form('clear');
	$("#addrule1type").combobox("setValue", "1");
	$("#addrule1weight").numberbox("setValue", "");
	$("#addrule1response").focus();
	insertorupdate_rule1 = 0;
}

// 新增或修改问题要素冲突判断规则
function saveRule1() {
	var conditions = [];
	var count = 0;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $.trim($("#addrule1value" + i).textbox("getValue"));
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	if (count == 10) {
		$.messager.alert('提示', "请至少填写一个问题要素!", "warning");
		return;
	}
	var weight = $("#addrule1weight").numberbox("getValue");
	if (weight === null || weight === "") {
		$.messager.alert('提示', "请输入优先级!", "warning");
		return;
	}
	var response = $.trim($("#addrule1response").val());
	var data = {};
	if (insertorupdate_rule1 == 0) {
		data = {
			type : 'insertrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 1,
			ruleresponse : response,
			abs : _abstract
		};
	} else {
		data = {
			type : "updaterule",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : conditions.join("@"),
			weight : weight,
			ruletype : 1,
			ruleresponse : response,
			ruleid : $("#rule1id").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
				clearRule1Form();
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 对上传文件进行校验和上传
function uploadRule1() {
	// 得到上传文件的全路径
	var fileName = $('#fileuploadrule1').filebox('getValue');
	// 进行基本校验
	if (fileName == "") {
		$.messager.alert('提示', '请选择上传文件!', 'warning');
	} else {
		// 对文件格式进行校验
		var filetype = /\.[^\.]+$/.exec(fileName.toLowerCase());
		if (filetype == ".xls" || filetype == ".xlsx") {
			// 使表单成为ajax提交
			$("#uploadrule1form").form(
					"submit",
					{
						url : "../file/upload",
						success : function(data) {
							var info = $.parseJSON(data);
							var state = info["state"];
							if (state == "success") {
								var name = info["names"][0];
								importExcelRule1(name);
							} else {
								$.messager.alert('提示', info["message"]
										+ " 请重新上传!", 'warning');
							}
							$('#fileuploadrule1').filebox('setValue', '');
						}
					});
		} else {
			var alertinfo = "上传的文件格式不正确,请选择<br/>";
			alertinfo += "1997-2003Excel文件(*.xls)<br/>";
			alertinfo += "或者是2007Excel文件(*.xlsx)!";
			$.messager.alert('信息提示', alertinfo, 'warning');
			$('#fileuploadrule1').filebox('setValue', '');
		}
	}
}

// 将Excel文件中的数据导入到数据库中
function importExcelRule1(name) {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : "importxls",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			filename : name,
			importtype : 2
		},
		async : false,
		dataType : "json",
		timeout : 1800000,
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule1datagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 读取数据库并生成Excel2003文件，并提供下载
function exportExcelRule1() {
	$.ajax( {
		type : "post",
		url : "../queryelement.action",
		data : {
			type : 'exportxls',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			exporttype : 2,
			abs : _abstract
		},
		async : false,
		dataType : "json",
		timeout : 180000,
		success : function(data, textStatus, jqXHR) {
			if (data.success == true) {
				location = "../file/download?filename=" + data.path;
			} else {
				$.messager.alert('提示', "下载文件失败!", "warning");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 加载其他规则的下拉框
function loadRule2Combobox() {
	var typedata = [ {
//		"id" : "0",
//		"text" : "缺失补全规则"
//	}, {
//		"id" : "1",
//		"text" : "问题要素冲突判断规则"
//	}, {
		"id" : "2",
		"text" : "其他规则"
	}, {
		"id" : "3",
		"text" : "识别规则"
	} ];
	$("#addrule2type").combobox( {
		valueField : 'id',
		textField : 'text',
		editable : false,
		panelHeight : 'auto',
		data : typedata  
	//	value : "2"
	});
	//$("#addrule2type").combobox("disable");
	clearRule2Form();
}

// 加载其他规则的列表
function loadRule2() {
	$("#rule2datagrid").datagrid( {
		url : '../queryelement.action',
		height : 335,
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			ruletype : '2,5',
			weight : ""
		},
		pageSize : 10,
		striped : true,
		nowrap : true,
		fitColumns : true,
		singleSelect : false,
		columns : [ [ {
			field : 'ck',
			checkbox : true
		}, {
			field : "type",
			title : "规则类型",
			align : 'center',
			formatter : function(value, row, index) {
				if (value == '0') {
					return '缺失补全规则';
				} else if (value == '1') {
					return '问题要素冲突判断规则';
				} else if (value == '2'){
					return '其他规则'; 
				} else {
					return '识别规则';
				}
			}
		}, {
			field : "weight",
			title : "规则优先级",
			align : 'center'
		}, {
			field : "response",
			title : "回复内容",
			width : 300,
			formatter : function(value, row, index) {
				if (value != null) {
					value = value.replace(/</g, "&lt;").replace(/>/g, "&gt;");
					return "<div title='" + value + "'>" + value + "</div>";
				} else {
					return value;
				}
			}
		} ] ]
	});
	$("#rule2datagrid").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				clearRule2Form();
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				editRule2();
			}
		}, "-", {
			text : "删除(批量)",
			iconCls : "icon-remove",
			handler : function() {
				deleteRule2();
			}
		}, "-" ]
	});
}

// 修改其他规则操作，将值放入编辑区
function editRule2() {
	var rows = $("#rule2datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		$("#addrule2weight").numberbox("setValue", rows[0].weight);
		var str = rows[0].response;
		var info = [];
		if (str.indexOf("==>") > -1) {
			info = str.split("==>");
			$("#addrule2qianti").textbox("setValue", info[0]);
			$("#addrule2response").val(info[1]);
		} else {
			$("#addrule2qianti").textbox("setValue", "");
			$("#addrule2response").val(str);
		}
		$("#rule2id").val(rows[0].id);
		insertorupdate_rule2 = 1;
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}

// 删除(批量)其他规则
function deleteRule2() {
	var ruleid = [];
	var rows = $("#rule2datagrid").datagrid("getSelections");
	if (rows.length > 0) {
		for ( var i = 0; i < rows.length; i++) {
			ruleid.push(rows[i].id);
		}
	} else {
		$.messager.alert('提示', "请至少选择一行!", "warning");
		return;
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : {
			type : 'deleterule',
			ruleid : ruleid.join(","),
			abs : _abstract
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule2datagrid").datagrid("reload");
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 清空其他规则表单
function clearRule2Form() {
	$('#rule2form').form('clear');
	$("#addrule2type").combobox("setValue", "");
	$("#addrule2response").focus();
	insertorupdate_rule2 = 0;
}

// 新增或修改其他规则
function saveRule2() {
	var ruletype = $("#addrule2type").combobox("getValue");
	var qianti = $.trim($("#addrule2qianti").textbox("getValue"));
	if (qianti === null || qianti === "") {
		$.messager.alert('提示', "请填写规则前提!", "warning");
		return;
	}
	var weight = $("#addrule2weight").numberbox("getValue");
	if (weight === null || weight === "") {
		$.messager.alert('提示', "请输入优先级!", "warning");
		return;
	}
	var response = $.trim($("#addrule2response").val());
	var data = {};
	if (insertorupdate_rule2 == 0) {
		data = {
			type : 'insertrule',
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			weight : weight,
			ruletype : ruletype,
			ruleresponse : qianti + "==>" + response,
			abs : _abstract
		};
	} else {
		data = {
			type : "updaterule",
			kbdataid : kbdataid,
			kbcontentid : kbcontentid,
			conditions : "",
			weight : weight,
			ruletype : ruletype,
			ruleresponse : qianti + "==>" + response,
			ruleid : $("#rule2id").val()
		};
	}
	$.ajax( {
		url : '../queryelement.action',
		type : "post",
		data : data,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#rule2datagrid").datagrid("reload");
				clearRule2Form();
			}
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//替换字符串中所有空格
function replaceSpace(str){
	str = str.replace(new RegExp(' ','g'),''); 
	return str;
}

//地址栏参数
function UrlParams() { 
	var name, value;
	var str = location.href; // 取得整个地址栏
	var num = str.indexOf("?");
	str = str.substr(num + 1); // 取得所有参数
	var arr = str.split("&"); // 各个参数放到数组里
	for ( var i = 0; i < arr.length; i++) {
		num = arr[i].indexOf("=");
		if (num > 0) {
			name = arr[i].substring(0, num);
			value = arr[i].substr(num + 1);
			this[name] = value;
		}
	}
}
//获取回复类型
function loadResponseType(){
	var resTypeData=[{
				"id":"0",
				"text":"文本型"
			},{
				"id":"2",
				"text":"菜单询问"
			},{
				"id":"-1",
				"text":"手写规则"
			}
		];
	return resTypeData;
}

//添加回复类型onselect 事件
function responseTypeOnSelect(){
	$("#responsetype").combobox({ 
	  editable:false ,
	  valueField:'id',    
	  textField:'text',
	  data:loadResponseType(),
	  onSelect : function(){
		var selectOption = $("#responsetype").combobox('getValue');
		if(selectOption== "2"){
			$("#interactiveoptions_btn").show();
			$("#templatecolumn_btn").hide();
			$("#triggerAction_btn").hide();
			$("#hitquestion_btn").hide(); 
			$(".serviceinfo_tr").css("display","none");
			 UM.getEditor('myEditor').setDisabled('fullscreen');
		     disableBtn("enable"); 
		}else{
			$("#interactiveoptions_btn").hide();
			$("#templatecolumn_btn").show();
			$("#triggerAction_btn").show();
			$("#hitquestion_btn").show(); 
			$(".serviceinfo_tr").css("display","");
			  UM.getEditor('myEditor').setEnabled();
		       enableBtn();
		}
		
	}
	
	});
}


//获得地市信息comboxtree
function getSearchCityTree(id,city){
//	var city = "南京市,合肥市,江苏省,北京市";
	var id ="#"+id;
	$(id).combotree({
		url:'../getCityTreeByLoginInfo.action',
		editable:false, 
		multiple:false,
		queryParams:{
			local : city
		}
	}); 
}
//获得地市信息comboxtree(复选)
function getSearchCityTree2(id,city){
//	var city = "南京市,合肥市,江苏省,北京市";
	var id ="#"+id;
	$(id).combotree({
		url:'../getCityTreeByLoginInfo.action',
		editable:false, 
		multiple:true,
		checkbox:true,
		queryParams:{
			local : city
		}
	}); 
}

//插入模板列
function saveTemplateColumn(){
	var templateServiceid = $('#serviceinfo').combobox('getValue');
	createTemplateColumnCombobox(templateServiceid); 
	$('#add_templatecolumn').window('open');
	$("#templatecolumn").combobox("setValue", "");

	
}

//插入触发动作
function saveTriggerAction(){
	createtriggerActionNameCombobox(); 
	$('#add_triggerAction').window('open');
	$("#triggerActionName").combobox("setValue", "");
}

//构造触发动作下拉
function createtriggerActionNameCombobox() {
	$('#triggerActionName').combobox({    
	    url:'../interactiveScene.action?type=createtriggeractionnamecombobox&scenariosid='+publicscenariosid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
//	    editable:false
	});  
}


//构造模板列下拉框
function createTemplateColumnCombobox(templateServiceid) {
	
	$('#templatecolumn').combobox({    
	    url:'../interactiveScene.action?type=createtemplatecolumncombobox&scenariosid='+templateServiceid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
//	    editable:false
	});  
	
}


//插入模板列
function add_templateColumn(){
	var templatecolumn = $("#templatecolumn").combobox('getText');
	if(templatecolumn==""||templatecolumn==null){
	 return; 	
	}
	var content = UM.getEditor('myEditor').getContent();
	var response = $.trim(content);
	response = response+"<@"+templatecolumn+">";
	UM.getEditor('myEditor').setContent(response, false);
	$('#add_templatecolumn').window('close');
	$("#templatecolumn").combobox("setValue", "");
}


//插入触发动作
function add_triggerAction(){
	var triggerAction = $("#triggerActionName").combobox('getText');
	if(triggerAction==""||triggerAction==null){
	 return; 	
	}
	var content = UM.getEditor('myEditor').getContent();
	var response = $.trim(content);
	response = response+triggerAction+"()";
	UM.getEditor('myEditor').setContent(response, false);
	$('#add_triggerAction').window('close');
	$("#triggerActionName").combobox("setValue", "");
}

//清空模板列
function add_clearTemplateColumn(){
	$('#add_templatecolumn').window('close');
	$("#templatecolumn").combobox("setValue", "");
}

//清空触发动作
function clearTriggerAction(){
	$('#add_triggerAction').window('close');
	$("#triggerActionName").combobox("setValue", "");
}


//打开命中问题window
function saveHitquestion(){
	//加载省份下拉列表
	createProvinceCombobox();
	$('#hit_city').combobox('clear'); 
	provinceChange("全国");
	$('#hitquestion').val('');
	$("#hitquestionresult").val('');
	var conditions = [];
	var arr =[];
	var count = 0;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addrule0value" + i).combobox("getValue");
		if(cond=="已选"){
		arr.push(cond); 
		}
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	
	if(arr.length>0){
		$('#hitquestion').val('<@列名>**列名');
		$('#hitquestion').css('color','#ACA899');
	} 
	
	
	$('#add_hitquestion').window('open');
}

//命中问题输入框Onfocus事件
function hitquestionOnfocus(id){
	var hitquestion = $('#'+id).val();
	if(hitquestion=="<@列名>**列名"){
		 $('#'+id).val('');
		 $('#'+id).css('color','##000000');
	}
}
//命中问题输入框Onblur事件
function hitquestionOnblur(){
	var arr =[];
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addrule0value" + i).combobox("getValue");
		if(cond=="已选"){
		arr.push(cond); 
		}
	}
	if(arr.length>0){
		var question = $('#hitquestion').val();
		if(question=="<@列名>**列名"||question==""){
			$('#hitquestion').val('<@列名>**列名');
		}
		$('#hitquestion').css('color','#ACA899');
		
	} 
}

//生成命中问题格式回复文本
function add_hitquestion(){
	var hitquestion = $('#hitquestion').val();
	var hitquestionresult = $('#hitquestionresult').val();
	if(hitquestion=='<@列名>**列名'||hitquestion==""){
		return;
	}else{
//		if(hitquestionresult==""||hitquestionresult=="理解失败"){
//			$.messager.alert('提示', "当前命中问题理解失败，请重新输入或联系语义维护人员！", "warning");
//			return ;
//		}
		
		   var response  = '命中问题("'+hitquestion+'")';
			UM.getEditor('myEditor').setContent(response, false);
			$('#add_hitquestion').window('close');
			$('#hitquestion').val();
	}
}

//命中问题取消操作
function clearHitquestion(){
	$('#hitquestion').val();
	$('#add_hitquestion').window('close');
}

////插入其他形式答案
function addOtherResponse(){
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		$("#otherResponse0name" + i).html("");
		$("#otherResponse0" + i).hide();
		$(".btd-display" + i).css("display","none");
	}
	 $.ajax({
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'getResConfig'
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				var info = data.rows;
				for ( var i = 0; i < info.length; i++) {
					var name = info[i]["key"];
					var weight = info[i]["weight"];
					var words = info[i]["words"];
					$(".btd-display" + weight).css("display","");
					$("#otherResponse0" + weight).show();
					var type = info[i]["value"];
					if ('自定义' == type){
						$("#otherResponse0name" + weight).html(name + "：");
						$("#otherResponse0value" + weight).textbox({
							multiline: true,
							width: 200,
							height:100
						});
						$("#otherResponse0value" + weight).textbox('clear');
					} else {
						$("#otherResponse0name" + weight).html(name + ":");
						$("#otherResponse0value" + weight).combobox({
							width: 200,
							valueField: 'id',    
					        textField: 'text',
//					        editable:false,
					        data : words
						});
						$("#otherResponse0value" + weight).combobox('clear');
					}
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	$('#otherResponse_add').window('open');
}


function addToResponse(){
	var conditions = [];
	var con;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		if ($("#otherResponse0name" + i).html() == "" ){
			break;
		}
		if ($("#otherResponse0name" + i).html().indexOf(":") > -1){
			con = $("#otherResponse0value" + i).combobox('getValue');
		}else{
			con = $("#otherResponse0value" + i).textbox('getText');
		}
		if (undefined  == con || '' == con){
			continue;
		}
		conditions.push('SET("' + $("#otherResponse0name" + i).html().replace(":","").replace("：","") + '","' + con + '")');
	}
	var cond = conditions.join(';');
	
	if (UM.getEditor('myEditor').getContent().indexOf('###;')!=-1){
		UM.getEditor('myEditor').setContent(cond + ';###;' + UM.getEditor('myEditor').getContent().split(';###;')[1], false);
	}else{
		UM.getEditor('myEditor').setContent(cond + ';###;' + UM.getEditor('myEditor').getContent(), false);
	}
	if (ruleresponsetemplate.indexOf(';###;')!=-1){
		ruleresponsetemplate =cond + ';###;'+ ruleresponsetemplate.split('###;')[1];
	}else{
		ruleresponsetemplate =cond  +';###;'+ ruleresponsetemplate;
	}
	$('#otherResponse_add').window('close');
}

function saveInteractiveOoptions(){
	var conditions = [];
	var arr =[];
	var count = 0;
	var w = 0 ;
	for ( var i = 1; i < MAX_RULE_COUNT; i++) {
		var cond = $("#addrule0value" + i).combobox("getValue");
		if(cond=="交互"){
		arr.push(cond); 
		w =i;
		}
		if (cond == "(空)") {
			cond = "";
		}
		conditions.push(cond);
		if (cond === null || cond === "") {
			count++;
		}
	}
	
	add_clearMenuItems();
	
	if(arr.length==1){
	    $("#add_interactivetemplate_div").hide();
	 	$.ajax( {
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'getmenuitemsinfo',
				weight:w,
				scenariosid :publicscenariosid
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				
				var wordclassid = data.wordclassid;
				publicwordclassid = wordclassid;
				var name = data.name;
				if(wordclassid==""||wordclassid==null){
					  $.messager.alert('提示', "当前规则交互列未配置对应词类!", "warning"); 
				       return false;
				}else{
					$("#add_interactionname_div").show();
					$("#add_correspondingwordclass_div").hide();
					$("#add_correspondingwordclass").val(wordclassid);
					$('#add_menuItems_edit').window('open');
					var itemmode = data.itemmode; 
					if(itemmode=="勾选+自定义"){
					 $("#add_interactivetemplate_div").show();	
					 $("#add_interactiveoptions_div").show();
					}else if(itemmode=="自定义"){
					 $("#add_interactivetemplate_div").show();	
					 $("#add_interactiveoptions_div").hide();
					}
					
					var knoElementName;
					if(publicscenariosname.endWith("场景")){
						knoElementName = publicscenariosname+"_知识名称";
					}else{
						knoElementName = publicscenariosname+"场景_知识名称";
					}
					if(name==knoElementName){
						$('#add_interactionname').textbox('setValue','知识名称');	
						$("#add_interactiveoptions").combobox('clear');
						$('#img_add').show();
						createInteractiveOptionsCombobox2(wordclassid);
						imgClick();
					}else{
						$('#img_add').hide();
						$('#add_interactionname').textbox('setValue',data.name);	
						createInteractiveOptionsCombobox2(wordclassid);
					}
					
					$('#add_interactivetemplate').val(data.interpat);
					$('#add_customvalue').val('多条值以|分隔开');
					$('#add_customvalue').css('color','#ACA899');
					
					if(insertorupdate_rule0 == 1){//修改时填充交互选项值
				    	 var rows = $("#rule0datagrid").datagrid("getSelections");
				         var ruleresponsetemplate = rows[0].ruleresponsetemplate;
				         var ruleresponse = rows[0].response;
				         var interactiveoptionsstr="";
				         var responsetype = rows[0].responsetype;
				    	    if(responsetype=="2"){
				    	    	   if(ruleresponsetemplate!=""&&ruleresponsetemplate!=null){
				    	    	    	 interactiveoptionsstr = ruleresponsetemplate.split('","')[1].split('")')[0];
				    	    	    	 $('#add_interactiveoptions').combobox('setValues',interactiveoptionsstr.split('||'));
				    	    	    	 $('#add_interactivetemplate').val(ruleresponse.split('[')[0]);
				    	    	    	 $('#add_interactivetemplateend').val(ruleresponsetemplate.split('","')[0].split('<@选项文本>')[1]);
				    	    	} 
				    	     }	 
				     }
					
					
				} 
				
				
//			},
//			error : function(jqXHR, textStatus, errorThrown) {
//				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
		
		
	}else{
		if(arr.length>1){
			$.messager.alert('提示', "请选择一条<用户未选或未告知,系统提示输入>值!", "warning");
			return;	
		}else{
			$("#add_correspondingwordclass").val("");
			$("#add_interactiveoptions_div").hide();
			$("#add_correspondingwordclass_div").hide();
			$("#add_interactionname_div").hide();
			$("#add_interactivetemplate_div").show();
			$('#add_customvalue').val('多条值以|分隔开');
			$('#add_customvalue').css('color','#ACA899');
			$('#add_menuItems_edit').window('open');
		}
		
	} 
	
//	if (count == 10) {
//		$.messager.alert('提示', "请至少选择一个场景要素!", "warning");
//		return;
//	}
	

		
}


function add_saveMenuItems(){

	var  interactiveoptions  = $('#add_interactiveoptions').combobox('getText');
	var interpat  = $("#add_interactivetemplate").val().replaceAll(',', '，');
	var interpatend = $("#add_interactivetemplateend").val().replaceAll(',', '，');
	
	var customvalue = $("#add_customvalue").val().replaceAll(',', '，');
	if(customvalue=="多条值以|分隔开"){
		customvalue="";	
	}
	if(customvalue=="" && interactiveoptions==""){
		 $.messager.alert('提示', "请设置交互项选项值!", "warning"); 
	       return false;
	}
	
	var wordclassid = $("#add_correspondingwordclass").val();
	
	if(wordclassid==""|| wordclassid==null){
		var answer = interpat;
		interactiveoptions = interactiveoptions + "|" + customvalue;
		var options = interactiveoptions.split("|");
		var k = 0;
		var items="";
		var template="";
		var opt ="";
		for ( var i = 0; i < options.length; i++) {
			if (options[i]=="") {
				continue;
			}
			k++;
			items = items +"["+ k + "]" + options[i] + ".<br/>";
			template = template +"["+ k + "]" + options[i]+"."; 
			opt = opt+ options[i]+"||";
		}
//		template = template.substring(0,template.lastIndexOf("."));
		template = answer +template;
		answer =  answer + "<br/>" + items + interpatend;
		
		opt = opt.substring(0,opt.lastIndexOf("||"));
		
//		ruleresponsetemplate ='菜单询问("'+template+'","'+opt+'")';
		ruleresponsetemplate ='菜单询问("'+interpat+'<@选项文本>'+interpatend+'","'+opt+'")';
		
		if(answer == null){
			answer ="";
		}
		//给富文本框赋值，不追加 
		UM.getEditor('myEditor').setContent(answer, false);
		$('#add_menuItems_edit').window('close');
		
		
		
	}else{
		$.ajax( { 
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'insertmenuitems',
				wordclassid:wordclassid,
				customvalue:customvalue
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				if(data.success){

//					var answer = interpat.split("<")[0];
					var answer = interpat;
					interactiveoptions = interactiveoptions + "|" + customvalue;
					var options = interactiveoptions.split("|");
					var k = 0;
					var items="";
					var template="";
					var opt ="";
					for ( var i = 0; i < options.length; i++) {
						if (options[i]=="") {
							continue;
						}
						k++;
						items = items +"["+ k + "]" + options[i] + ".<br/>";
						template = template +"["+ k + "]" + options[i]+"."; 
						opt = opt+ options[i]+"||";
					}
//					template = template.substring(0,template.lastIndexOf("."));
					template = answer +template;
					answer =  answer + "<br/>" + items + interpatend;
					
					opt = opt.substring(0,opt.lastIndexOf("||"));
					
//					ruleresponsetemplate ='菜单询问("'+template+'","'+opt+'")';
					ruleresponsetemplate ='菜单询问("'+interpat+'<@选项文本>'+interpatend+'","'+opt+'")';
					if(answer == null){
						answer ="";
					}
					//给富文本框赋值，不追加 
					UM.getEditor('myEditor').setContent(answer, false);
					$('#add_menuItems_edit').window('close');
					
				}else{
					 $.messager.alert('提示', data.msg, "warning"); 
					
				}
			
				
				
//			},
//			error : function(jqXHR, textStatus, errorThrown) {
//				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	}

}

//清空菜单选项
function add_clearMenuItems(){
	$('#add_interactionname').textbox('setValue','');
	$('#add_interactiveoptions').combobox('setValue',"");
	$('#add_interactivetemplate').val('');
	$('#add_interactivetemplateend').val('');
	$("#add_customvalue").val('');
}

//根据交互选项下拉框
function createInteractiveOptionsCombobox2(wordclassid) {
	$("#add_interactiveoptions").combobox({    
	    url:'../interactiveScene.action?type=interactiveoptions&wordclassid='+wordclassid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : true, // 支持多选
		separator : '|' ,// 多选的时候用“|”分隔
		editable:true 
	});  
}

//富文本框操作方法

function disableBtn(str) {
    var div = document.getElementById('btns'); 
    var btns = domUtils.getElementsByTagName(div, "button");
    for (var i = 0, btn; btn = btns[i++];) {
        if (btn.id == str) {
            domUtils.removeAttributes(btn, ["disabled"]);
        } else {
            btn.setAttribute("disabled", "true");
        }
    }
}

function enableBtn() {
    var div = document.getElementById('btns');
    var btns = domUtils.getElementsByTagName(div, "button");
    for (var i = 0, btn; btn = btns[i++];) {
        domUtils.removeAttributes(btn, ["disabled"]);
    }
}


//上移规则优先级
function moveRule(){
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		var ruletype = rows[0].type;
		var cityCode  = $("#search_city").combotree("getValue");
		var w = rows[0].weight;
//		if(cityCode == ""|| cityCode == null|| cityCode == undefined){
//			cityCode ="全国";
//		}
		if(cityCode =="全国"){
			$.messager.alert('提示', "请在归属地市等于当前用户地市下迁移规则!", "warning");
			return;	
		}
		$.ajax( {
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'moverule',
				ruletype :ruletype,
				city : cityCode,
				weight:w,
				scenariosid:publicscenariosid
//				resourcetype:'scenariosrules',
//				operationtype:'U',
//				resourceid:publicscenariosid
				
			},
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				$.messager.alert('提示', data.msg, "info");
				if (data.success) {
					$("#rule0datagrid").datagrid("reload");	
				}
				
//			},
//			error : function(jqXHR, textStatus, errorThrown) {
//				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
		
		
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
}


//复制规则
function copyRule(){
	var rows = $("#rule0datagrid").datagrid("getSelections");
	if (rows.length == 1) {
		excludedcity = rows[0].excludedcity;
		publiccitycode = rows[0].citycode;
		ruleresponsetemplate =  rows[0].ruleresponsetemplate;
		var responsetype = rows[0].responsetype;
		var userquestion = rows[0].userquestion;
		var cityCode  = $("#search_city").combotree("getValue");
		if(cityCode =="全国"){
			$.messager.alert('提示', "请在归属地市等于当前用户地市下复制规则!", "warning");
			return;	
		}
		if (publiccitycode.indexOf("0000")==-1){
			$.messager.alert('提示', "只允许复制省级规则！", "warning");
			return;
		}
		$("#responsetype").combobox("setValue", responsetype);
		if(responsetype=="2"){
			$("#templatecolumn_btn").hide();
			$("#triggerAction_btn").hide();
			$(".serviceinfo_tr").css("display","none");
			$("#interactiveoptions_btn").show();
		}else{
			$("#templatecolumn_btn").show();
			$("#triggerAction_btn").show();
			$(".serviceinfo_tr").css("display","");
			$("#interactiveoptions_btn").hide();
		}
		
		for ( var i = 3; i < rule0Arr.length - 7; i++) {
			var field = rule0Arr[i].field;
			var con = rows[0][field];
			var weight = field.replace("condition", "");
			$("#addrule0value" + weight).combobox("setValue", con);
		}
		$("#span_addrule0weight").hide();
		$("#input_addrule0weight").hide();
		$("#addrule0weight").combobox("setValue", rows[0].weight);
		var answer =rows[0].response;
		if(answer == null){
			answer ="";
		}
		//给富文本框赋值，不追加
		UM.getEditor('myEditor').setContent(answer, false);
		$("#rule0id").val(rows[0].id);
		insertorupdate_rule0 = 0;
		
		var cityCode  = rows[0].citycode;
		if(cityCode==""||cityCode==null||cityCode == undefined){
			cityCode ="全国";
		}
		$('#edit_city').combotree('setValue', cityCode);
		 $('#adduserquestion').textbox('setValue',userquestion);
		saveRule0("copy");
	} else {
		$.messager.alert('提示', "请选择一行!", "warning");
		return;
	}
	

}




//添加插入知识称交互项图片点击事件
function imgClick(){
	 $("#img_add").click(function(){
		  $("#add_knoname").window('open'); 
		  getSearchCityTree("kw_search_city","edit");
		  loadKnoName();
		   
		});
}

//李习凤相关代码


//知识类别，下拉框
function createKnoTypeCombobox(){
	$('#kw_type').combobox({    
		  url:'../getKwTypes.action',    
		  valueField: 'serviceid',    
		  textField: 'service'  ,
		  onLoadSuccess: function () { 
		  	var datas = $('#kw_type').combobox('getData');
		      if (datas.length > 0) {
		          $('#kw_type').combobox('setValue', datas[0].serviceid);
		          loadKnoName();
		      } 
			}
		}); 	
}


//加载知识名称
function loadKnoName(){ 
	var docName   = $(".kw-wrap [name=keyword]").val(),
		serviceid = $('#kw_type').combobox("getValue"),
		city      = $('#kw_search_city').combotree("getValue");
	
	$("#kw-knowology-info")
			.datagrid(
					{
						url :  '../getKWDatas.action',
						width : 1000,
						height : 310,
						pagination : true,
						rownumbers : true,
						queryParams : {
							
						city: city == "全国" ? "":city,
						serviceid:  serviceid ,
						docName: docName ? $.trim(docName).replace(/ +/,"%") : ""
						},
						pageSize : 10,
						striped : true,
						singleSelect : true,
						columns : [ [
								{
									field : 'selected',
									title : '选择',
									width : 65,
									align : "center",
									formatter : function(value, row, index) {
										return "<input type='checkbox' />";
									}
								},
								{
									field : 'docName',
									title : '知识名称',
									width : 300
								},
								{
									field : 'service',
									title : '知识类别',
									width : 250
								},
								{
									field : 'city',
									title : '地市',
									width : 80
								},
								{
									field : 'cityCode',
									title : '地市编码',
									width : 80
								}
								] ]
					});
	$("#kw-knowology-info").datagrid('getPager').pagination( {
		showPageList : false
	});
}


//更新搜索条件，搜索答案
$(document).on("click", ".search-btn", function(){
	loadKnoName();
});

//保存数据
$(document).on("click", ".save", function(){
	
	var $kShowRows = $(".kw-wrap .kshow .datagrid-row");
	var $checkboxes = $kShowRows.find("input"),
		$docNames   = $kShowRows.find("[field=docName] div"),
		$services   = $kShowRows.find("[field=service] div"),
		$cities  	= $kShowRows.find("[field=city] div");
	    $cityCodes  = $kShowRows.find("[field=cityCode] div");
	
	//要抛出的数据
	var toSaveDatas = [],
		checkboxLen = $checkboxes.length;
	 var knoContent ="";
	for (var i = 0; i < checkboxLen; i++) {
		if ($($checkboxes[i]).is(":checked")) {
			var oneLineRecord = $($docNames[i]).text()+"@#@"+$($services[i]).text()+"@#@"+$($cities[i]).text()+"@#@"+$($cityCodes[i]).text();
			knoContent = knoContent+oneLineRecord+"&&";
			toSaveDatas.push($($docNames[i]).text());
		}
	}
	
	var newArry = unique(toSaveDatas);
	var knoStr ="";
	for(var i = 0; i < newArry.length; i++){
		knoStr = knoStr+newArry[i]+"||";
	}
	
	if(knoStr==""||knoStr==null){
		$.messager.alert('提示', "请选择勾选知识名称!", "warning");
		return;
	}
	knoStr = knoStr.substring(0, knoStr.length-2);
	var oldKnoStr =  $('#add_interactiveoptions').combobox('getText');
	if(oldKnoStr!=""&&oldKnoStr!=null){
	  oldKnoStr = oldKnoStr.replace(new RegExp('\\|', 'g'), '||');
	  knoStr=knoStr+"||"+oldKnoStr;
	}
 addKnoName(knoContent,knoStr);
	
});

//添加知识名称
function addKnoName(knoContent,knoStr){
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addknoname',
			name :publicscenariosname,
			knocontent : knoContent
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success) {
				createInteractiveOptionsCombobox2(publicwordclassid);
			}
			 $('#add_interactiveoptions').combobox('setValues',knoStr.split('||'));
			 $("#add_knoname").window('close');
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	
}


function closeKnonameWin(){
	 $("#add_knoname").window('close');
}
//数据组去重
function unique(arr){
  var tmpArr = [], hash = {};//hash为hash表
  for(var i=0;i<arr.length;i++){
    if(!hash[arr[i]]){//如果hash表中没有当前项
      hash[arr[i]] = true;//存入hash表
      tmpArr.push(arr[i]);//存入临时数组
    }
  }
  return tmpArr;
}



//构造业务信息列下拉框
function createProvinceCombobox(){
	$('#hit_province').combobox({    
	    url:'../interactiveScene.action?type=createprovincecombobox&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    editable:false,
	    onSelect : function(){
		var provincecode = $("#hit_province").combobox('getValue');
		provinceChange(provincecode);
	   },
	    onLoadSuccess: function () { 
		  	var datas = $('#hit_province').combobox('getData');
		      if (datas.length > 0) {
		          $('#hit_province').combobox('setValue', '全国');
		      } 
			}
	});  
}

//构造地市下拉列表
function provinceChange(provincecode) {
	$('#hit_city').combobox({    
		url:'../interactiveScene.action?type=createcitycombobox&province='+provincecode+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    editable:false,
	    onLoadSuccess: function () { 
		  	var datas = $('#hit_city').combobox('getData');
		      if (datas.length > 0) {
		          $('#hit_city').combobox('setValue', datas[0].id);
		      } 
			}
	  
	});  
}

//测试问题
function testHitQuestion(){
	$("#hitquestionresult").val("");
	var question = $("#hitquestion").val();
	if(question==""||question==null||question=="<@列名>**列名"){
		return ;
	}
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'testhitquestion',
			question:question,
			province:$("#hit_province").combobox('getText'),
			city : $('#hit_city').combobox('getText')
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
		$("#hitquestionresult").val(data.msg);
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	
}


//打开场景元素列添加区
function openElementWin(){
	$("#add_elementcolumn").window('open');
	createElementColumnCombobox();
}

//插入场景元素列
function add_elementColumn(){
	var elementcolumn = $("#elementcolumn").combobox('getText');
	if(elementcolumn==""||elementcolumn==null){
	 return; 	
	}
	elementcolumn = "<@"+elementcolumn+">";
	var content = $("#hitquestion").val()+elementcolumn;
	$("#hitquestion").val(content);
	$("#add_elementcolumn").window('close');
}

//场景元素列取消操作
function clearElementColumn(){
	$("#add_elementcolumn").window('close');
}


//构造场景元素列下拉框
function createElementColumnCombobox() {
	$('#elementcolumn').combobox({    
	    url:'../interactiveScene.action?type=createlementcolumncombobox&scenariosid='+publicscenariosid+'&a='+ Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px'
//	    editable:false
	});  
}


//字符串endWith
String.prototype.endWith=function(str){
	var reg=new RegExp(str+"$");
	return reg.test(this);
	} 
String.prototype.replaceAll  = function(s1,s2){     
    return this.replace(new RegExp(s1,"gm"),s2);     
}

//更新业务规则
function updateRuleNLP() {
	$.ajax( {
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : "updaterulenlp"
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

function detail(event,index){
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	
	var row = $('#rule0datagrid').datagrid('getData').rows[index];
	var ruleid = row.ruleid;
	var response = row.response;
	$('#detail').dialog({
		title:'详情',
	    width: 900,    
	    height: 450,
	    left:200,
	    top:50
	});
	document.getElementById('detail').innerHTML=response;
//	document.getElementById('detail').innerHTML=response.replace(';###;','<br/>其他形式答案：<br/>');
	} 

function issueRule0(){
	$.messager.confirm('确认','确认发布该场景?',function(r){
		if (r){
//			alert("issue");
			$.ajax( { 
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'issue',
					scenariosid:publicscenariosid,
					service:publicscenariosname
//					resourcetype:'scenariosrules',
//					operationtype:'A',
//					resourceid:publicscenariosid
				},
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					$('#rule0datagrid').datagrid('reload');
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	})
}

//单个更新场景
function reloadScenarios(){
	$.ajax( { 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'reloadScenarios',
			scenariosid:publicscenariosid
//			resourcetype:'scenariosrules',
//			operationtype:'A',
//			resourceid:publicscenariosid
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 线上规则下线
function deleteOnlineRule0(){
	$.messager.confirm('确认','确认删除线上规则?',function(r){
		if(r){
			$.ajax( { 
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deleteOnlineRule',
					scenariosid:publicscenariosid,
					service:publicscenariosname
				},
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					$('#rule0datagrid').datagrid('reload');
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	})
}
