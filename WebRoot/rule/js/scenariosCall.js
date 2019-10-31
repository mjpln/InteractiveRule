var userid;
var publicscenariosid;
var publicscenariosname;
var ioa;
var strategy;
var sceneType;
var public_thisGraphObj; // 鼠标事件触发的对象
var scenarioselementidForUpdate; // 用于场景要素编辑中获取选择对象id
var MAX_SCENARIO_COUNT = 21;
var customerAnswerSelectId;

var otherResponseNames=[];
var otherResponseValues=[];

// 短信模板
var smsTemplates;

// 号码属性
var phoneAttributeNames;

var sceneElementIdForUpdate;
var oldSceneElementName;
var oldWeight;
var saveOrUpdateElementFlag;
var saveOrUpdateInterfaceFlag;
var addWordPatternFlag;
var autoWordPattern;

//用于定义模板的简洁性
var $GO = go.GraphObject.make;

// 定义图表对象
var myDiagram;

$(function() {
	
	// 所有URL参数
	var urlparams = new UrlParams(); 
	// 用户ID
	userid = decodeURI(urlparams.userid);
	// 场景ID
	publicscenariosid = decodeURI(urlparams.scenariosid); 
	// 场景名称
	publicscenariosname = decodeURI(urlparams.scenariosname); 
	// 行业
	ioa = decodeURI(urlparams.ioa); 
	strategy = decodeURI(urlparams.strategy);
	sceneType = decodeURI(urlparams.sceneType);
	if(publicscenariosname.indexOf('【') > -1 && publicscenariosname.indexOf('】') > -1) {
		sceneType = 'callIn';
	} else {
		sceneType = 'callOut';
	}
	
	// 设置页面布局
	diagramStyle();

	// 制作流程图
	makeGraphObject();
	
	// 初始化流程
	loadDiagramData();
	
	// 定义左侧画板
	makePalette();
	
	// 添加监听事件
	addListenerEvents();
	
	// 按钮点击事件
	buttonClick();
	
	// 初始化交互类型
	initInteractiveType();
	
	// 初始化放音组件
	initTTSNode();
	
	// 初始化信息收集组件
	initCollection();
	
	// 初始化DTMF按键收集
	initDTMFPress();

	// 初始化转人工组件
	initTransfer();

	// 初始化动作组件
	initURLAction();
	
	// 初始化条件组件
	initCondition();
	
});

function initTTSNode() {
	
	// 初始化编辑页面
	initTTSEditPage()
	
	// 初始化用户回答
	initCustomerAnswer();
	
	// 初始化短信模板
	//initSmsTemplate();
	   
	// 初始化节点类型
	$.each($("input[name='ttsNodeType']"),function (index) {
        if(index==0) {
        	$("#customer-answer-table").fadeIn('slow');
            $(this).radiobutton({
                label: '意图节点',
                labelPosition:"after",
                value: "0",
                checked: true,
                onChange: function (checked) {
                    if(checked)
                    {
                    	$("#customer-answer-table").fadeIn('slow');
                    }
                }
            });
        }
        else
        {
        	$("#customer-answer-table").fadeOut('slow');
            $(this).radiobutton({
                label: '跳转节点',
                value: "1",
                labelPosition:"after",
                checked: false,
                onChange: function (checked) {
                    if(checked)
                    {
                    	$("#customer-answer-table").fadeOut('slow');
                    }
                }
            });
        }
    });
}

// 初始化信息收集组件
function initCollection() {
	
	$('#collectionform-collectionWords-tr').hide();
	$('#collectionform-collectionParam-tr').show();
	$('#collectionform-collectionTimes-tr').hide();
	$('#collectionform-collectionElement-tr').hide();
	$('#collectionform-interactiveType-tr').show();
	$('#collectionform-menuItems-tr').hide();
	
	// 初始化采集类型
	initCollectionType();
	
	// 初始化关联意图
	initCollectionIntention();
	
	// 初始化重复次数
	initCollectionTimes();
	
	// 初始化场景要素
	initSceneElements();
	
}

// 初始化收集类型
function initCollectionType() {
	$('#collectionType').combobox({
		valueField : 'id',
		textField : 'text',
		data : [{
    		"id":"elementCollection",
    		"text":"场景要素采集"
    	},{
    		"id":"userInfoCollection",
    		"text":"用户信息采集"
    	}],
		onChange: function(newVal, oldVal) {
			if(newVal == 'elementCollection') {
				$('#collectionform-collectionParam-tr').show();
				$('#collectionform-collectionTimes-tr').hide();
				$('#collectionform-collectionElement-tr').show();
			}
			if(newVal == 'userInfoCollection') {
				$('#collectionform-collectionParam-tr').show();
				$('#collectionform-collectionTimes-tr').show();
				$('#collectionform-collectionElement-tr').hide();
			}
        } 
	});
}

//初始化交互类型
function initInteractiveType() {
	$('.words').hide();
	$('.menus').hide();
	var interactiveTypeCombobox = $('#myNormalEditDiv').find('#interactiveType');
	interactiveTypeCombobox.combobox({
		valueField : 'id',
		textField : 'text',
		data : [{'text':'菜单询问','id':'键值补全'},{'text':'系统反问','id':'词模匹配'}],
		onChange: function(newVal, oldVal) {
			if(newVal == '词模匹配') { // 系统反问
				$('.words').show();
				$('.menus').hide();
			} else if (newVal == '键值补全'){ // 菜单询问
				$('.words').hide();
				$('.menus').show();
			}
        } 
	});
	interactiveTypeCombobox = $('#collectionform').find('#interactiveType');
	interactiveTypeCombobox.combobox({
		valueField : 'id',
		textField : 'text',
		data : [{'text':'菜单询问','id':'键值补全'},{'text':'系统反问','id':'词模匹配'}],
		onChange: function(newVal, oldVal) {
			if(newVal == '词模匹配') { // 系统反问
				$('#collectionform-collectionWords-tr').show();
				$('#collectionform-menuItems-tr').hide();
			} else if (newVal == '键值补全'){ // 菜单询问用户
				$('#collectionform-collectionWords-tr').hide();
				$('#collectionform-menuItems-tr').show();
			}
        } 
	});
}

// 初始化关联要素下拉框
function initSceneElements() {
	// 加载关联要素
	loadCollectionElement();
	// 关联要素添加按钮
	$("#addSceneElementBtn").bind("click",function () {
		openElement();
	});
	// 保存场景要素
	$("#sceneElementEditDiv #saveElement").click(function() {
		$('#sceneElementEditForm').form('submit', {
			onSubmit : function() {
				var isValid = $(this).form('enableValidation').form('validate');
				if (isValid) {
					editElement(saveOrUpdateElementFlag);
				}
				return false;
			}
		});
	});
	// 添加词类编辑页面
	$("#addWordClass").bind("click",function () {
		$("#wordClassEditDiv").window('open');
	});
	// 关闭之前触发的事件
	$('#wordClassEditDiv').window({
        onBeforeClose: function () { 
        	// 刷新词类下拉框
        	createWordClassCombobox();
        }
    });
	$('#sceneElementDiv').window({
        onBeforeClose: function () { 
        	// 刷新关联要素下拉框
        	loadCollectionElement();
        	// 刷新关联意图下拉框
    		loadCollectionIntention();
        	$('#interactiveType').combobox('setValue','');
        	$('#collectionform-collectionWords-tr').hide();
			$('#collectionform-menuItems-tr').hide();
        }
    });
}
// 加载关联要素
function loadCollectionElement() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'listAllSceneElement',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.total > 0) {
				$('#collectionElement').combobox({
					valueField : 'id',
					textField : 'text',
					data : data.rows,
					onChange: function(newVal, oldVal) {
						var rows = data.rows;
			        	for(var i=0; i<rows.length;i++) {
			        		var row = rows[i];
			        		if(row.name == newVal) {
			        			$('#interactiveType').combobox('setValue',row.container);
			        			if(row.container == '词模匹配') { // 系统反问
			        				$('#collectionform-collectionWords-tr').show();
			        				$('#collectionform-menuItems-tr').hide();
			        			} else if (row.container == '键值补全'){ // 菜单询问用户
			        				$('#collectionform-collectionWords-tr').hide();
			        				$('#collectionform-menuItems-tr').show();
								}
			        		}
			        	}
			        } 
				});
			}
		}
	});
}
// 打开场景要素编辑区
function openElement() {
	$('#sceneElementDiv').window('open');
	$("#sceneElementQueryForm-sceneElementName").textbox('setValue', "");
	loadElementName();
}

// 查询场景要素
function searchElementName() {
	var sceneElementName = $("#sceneElementQueryForm-sceneElementName").val();
	$('#sceneElementTable').datagrid('load', {
		type : 'listPagingSceneElement',
		sceneElementName : replaceSpace(sceneElementName),
		scenariosid : publicscenariosid
	});
}

// 加载场景要素列表
function loadElementName() {
	var sceneElementName = $("#sceneElementQueryForm-sceneElementName").val();
	$("#sceneElementTable").datagrid({
		title : '场景要素显示区',
		url : '../interactiveSceneCall.action',
		width : 900,
		height : 395,
		toolbar : "#sceneElementTableDiv",
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'listPagingSceneElement',
			sceneElementName : replaceSpace(sceneElementName),
			scenariosid : publicscenariosid
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
							if (value.indexOf("_知识名称") != -1) {
								value = value.split("_")[1];
							}
							var val = "<a title='" + value + "'>" + value + "</a>";
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
					width : 110,
					formatter : function(value, row, index) {
						if (value != "" && value != null) {
							value = value.replace(/\</g, "&lt;");
							value = value.replace(/\>/g, "&gt;");
							var val = "<a title='" + value + "'>" + value + "</a>";
							return val;
						} else {
							return "";
						}

					}
				},
				{
					field : 'interpat',
					title : '交互口径',
					width : 200,
					formatter : function(value, row, index) {
						if (value != "" && value != null) {
							value = value.replace(/\</g, "&lt;");
							value = value.replace(/\>/g, "&gt;");
							var val = "<a title='" + value + "'>" + value + "</a>";
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
					hidden: true
				},
				{
					field : 'cityname',
					title : '区域名称',
					width : 100,
					formatter : function(value, row, index) {
						if (value != ""  && value != null) {
							var val = "<a title='" + value + "'>" + value + "</a>";
							return val;
						} else {
							return "";
						}
					}
				},
				{
					field : 'container',
					title : '交互类型',
					width : 60,
					formatter : function(value, row, index) {
						if (value != "" && value != null) {
							value = value.replace(/\</g, "&lt;");
							value = value.replace(/\>/g, "&gt;");
							if (value == '词模匹配'){
								value='系统反问';
							} else if (value == '键值补全'){
								value='菜单询问';
							}
							var val = "<a title='" + value + "'>" + value + "</a>";
							return val;
						} else {
							return "";
						}
					}
				},
				{
					field : 'itemmode',
					title : '填写方式',
					width : 60,
					formatter : function(value, row, index) {
						if (value != "" && value != null) {
							value = value.replace(/\</g, "&lt;");
							value = value.replace(/\>/g, "&gt;");
							var val = "<a title='" + value + "'>" + value + "</a>";
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
					hidden : true
				},
				{
					field : 'scenarioselementid',
					title : 'ID',
					width : 100,
					hidden : true
				},
				{
					field : 'wordclassid',
					title : '词类ID',
					width : 100,
					hidden : true
				},
				{
					field : "delete",
					title : '操作',
					width : 50,
					align : 'center',
					formatter : function(value, row, index) {
					var id = row["scenarioselementid"];
					var weight = row["weight"];
					var name = row["name"];
					var a = '<a class="icon-delete btn_a" title="删除" onclick="deleteElement(event,'
						+ id
						+ ','
						+ weight
						+ ',\''
						+ name + '\')"></a>';
					return a;
					}
				}
			] ]
	});
	$("#sceneElementTable").datagrid('getPager').pagination( {
		showPageList : false,
		buttons : [ {
			text : "新增",
			iconCls : "icon-add",
			handler : function() {
				saveOrUpdateElementFlag = "save";
				toEditElementPage(saveOrUpdateElementFlag); 
			}
		}, "-", {
			text : "修改",
			iconCls : "icon-edit",
			handler : function() {
				saveOrUpdateElementFlag ="update";
				toEditElementPage(saveOrUpdateElementFlag); 
			}
		}]
	});
}

// 加载优先级下拉框
function loadWeightCombobox(weight) {
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getweight',
			scenariosid : publicscenariosid,
			weight : weight
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$("#sceneElementEditForm-weight").combobox({
				valueField : 'id',
				textField : 'text',
				required : true,
				missingMessage : '优先级不能为空!',
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
		}
	});
}

// 加载词类下拉框
function createWordClassCombobox() {
	$('#sceneElementEditForm-wordClass').combobox({
		url : '../interactiveScene.action?type=createwordclasscombobox&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 构造业务信息列下拉框
function createServiceInfoCombobox() {
	$('#sceneElementEditForm-infoTalbePath').combobox({
		url : '../interactiveScene.action?type=createserviceinfocombobox&scenariosid=' + publicscenariosid + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 获得地市信息下拉框
function getCityTree(cityname) {
	$('#sceneElementEditForm-city').combotree({
		url : '../getCityTreeByLoginInfo.action',
		editable : false,
		multiple : true,
		queryParams : {
			local : cityname
		}
	});
}

// 编辑场景要素页面
function toEditElementPage(saveOrUpdateElementFlag){
	if(saveOrUpdateElementFlag == "update") {
		var row = $('#sceneElementTable').datagrid('getSelected');
		if (row) {
			$('#sceneElementEditDiv').panel({ title : "修改场景要素" });
			$('#sceneElementEditDiv').window('open');
			getCityTree(row.cityname);
			createServiceInfoCombobox();
			createWordClassCombobox();	
			loadWeightCombobox(row.weight);
			$("#sceneElementEditForm-sceneElementName").textbox('setValue', row.name);
			$("#sceneElementEditForm-wordClass").combobox('setValue', row.wordclass);
			$("#sceneElementEditForm-weight").combobox('setValue', row.weight);
			$("#sceneElementEditForm-infoTalbePath").combobox('setValue', row.infotalbepath);
			$("#sceneElementEditForm-itemMode").combobox('setValue', row.itemmode);
			$("#sceneElementEditForm-container").combobox('setValue', row.container);
			$("#sceneElementEditForm-interPattern").val(row.interpat);
			sceneElementIdForUpdate = row.scenarioselementid;
			oldSceneElementName = row.name;
			oldWeight = row.weight;
		} else {
			$.messager.alert('提示', "请选择需编辑行!", "warning");
			return;
		}
	}
	if(saveOrUpdateElementFlag == "save") {
		$('#sceneElementEditDiv').panel({ title : "新增场景要素" });
		$('#sceneElementEditDiv').window('open');
		getCityTree("");
		createServiceInfoCombobox();
		createWordClassCombobox();
		loadWeightCombobox("");
	}
}

// 编辑场景要素
function editElement(saveOrUpdateElementFlag) {
	var type;
	var sceneElementId;
	if (saveOrUpdateElementFlag == "save") {// 新增操作
		type = 'insertelementname';
		oldWeight = '';
	} else if (saveOrUpdateElementFlag == "update") {// 修改操作
		type = 'updartelementname';
		sceneElementId = sceneElementIdForUpdate;
	}
	var name = replaceSpace($("#sceneElementEditForm-sceneElementName").val());
	var wordclass = $("#sceneElementEditForm-wordClass").combobox('getText');
	var weight = $("#sceneElementEditForm-weight").combobox('getText');
	var infotalbepath = replaceSpace($("#sceneElementEditForm-infoTalbePath").combobox('getText'));
	var cityname = $("#sceneElementEditForm-city").combotree('getText');
	var city = $("#sceneElementEditForm-city").combotree('getValues');
	var itemmode = $("#sceneElementEditForm-itemMode").combobox('getText');
	var container = $("#sceneElementEditForm-container").combobox('getValue');
	var interpat = $("#sceneElementEditForm-interPattern").val();

	// 对场景名进行特殊字符验证
	if (name.indexOf('=') > -1 || name.indexOf('/') > -1
			|| name.indexOf('%') > -1 || name.indexOf('>') > -1
			|| name.indexOf('<') > -1 || name.indexOf('*') > -1
			|| name.indexOf('Contain') > -1 || name.indexOf('@') > -1
			|| name.indexOf('!') > -1 || name.indexOf('$') > -1
			|| name.indexOf('包含') > -1) {
		$.messager.alert('系统提示', "存在非法字符串!", "info");
		return;
	}

	var dataStr = {
		type : type,
		scenariosid : publicscenariosid,
		scenariosName : publicscenariosname,
		scenarioselementid : sceneElementId,
		infotalbepath : infotalbepath,
		city : city + "",
		cityname : cityname,
		itemmode : itemmode,
		name : name,
		interpat : interpat,
		weight : weight,
		oldweight : oldWeight,
		wordclass : wordclass,
		container : container
	}

	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : dataStr,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				// 自动添加场景要素词模
				var elementName = name;
				var wordClassName = wordclass;
				saveElementWordPattern(elementName, wordClassName);
				clearElementEditForm();
				$('#sceneElementEditDiv').window('close');
				$("#sceneElementTable").datagrid("reload");
			}
		}
	});
}
// 添加场景要素词模
function saveElementWordPattern(elementName, wordClassName) {
	var simpleWordPattern = wordClassName + "#无序#编者=\"场景要素词模\""+"&针对问题=\""+elementName+"\""+"&"+elementName+"="+"<!"+wordClassName+">"+"&置信度=\"1.1\"";
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			collectionIntention : elementName,
			wordPatternType : 0,
			simpleWordPattern : simpleWordPattern,
			type : "saveCollectionIntention",
		},
		success : function(data) {
			loadCollectionElement();
    		loadCollectionIntention();
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "添加场景要素词模,请求数据失败!", "error");
		}
	});
}

// 删除场景要素
function deleteElement(event, id, weight, name) {
	if (event.stopPropagation) { // Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) { // IE
		window.event.cancelBubble = true;
	}
	$.messager.confirm('提示', '确定删除场景要素吗?', function(r) {
		if (r) {
			$.messager.confirm('提示', '删除不可恢复，请确认是否删除?', function(r) {
				if (r) {
					$.ajax({
						url : '../interactiveScene.action',
						type : "post",
						data : {
							type : 'deleteelementname',
							name : name,
							scenarioselementid : id,
							weight : weight,
							scenariosid : publicscenariosid,
							scenariosName : publicscenariosname,
							resourcetype : 'scenariosrules',
							operationtype : 'D',
							resourceid : publicscenariosid
						},
						async : false,
						dataType : "json",
						success : function(data, textStatus, jqXHR) {
							if (data.success == true) {
								loadWeightCombobox("");
								$("#sceneElementTable").datagrid("reload");
								var collectionIntention = name;
								deleteCollectionIntention(collectionIntention);
							}
							$.messager.alert('提示', data.msg, "info");
						}
					});
				}
			});
		}
	});
}

// 清空场景要素表单
function clearElementEditForm() {
	$("#sceneElementEditForm-sceneElementName").textbox('setValue', "");
	var weightData = $('#sceneElementEditForm-weight').combobox('getData');
	if (weightData.length > 0) {
		$('#sceneElementEditForm-weight').combobox('select', weightData[0].text);
	} else {
		$('#sceneElementEditForm-weight').combobox('select', "");
	}
	$("#sceneElementEditForm-wordClass").combobox('setValue', "");
	$("#sceneElementEditForm-infoTalbePath").combobox('setValue', "");
	$("#sceneElementEditForm-city").combotree('clear');
	var itemmodeData = $('#sceneElementEditForm-itemMode').combobox('getData');
	$('#sceneElementEditForm-itemMode').combobox('select', itemmodeData[0].value);
	var containerData = $('#sceneElementEditForm-container').combobox('getData');
	$('#sceneElementEditForm-container').combobox('select', containerData[0].value);
	$("#sceneElementEditForm-interPattern").val("");
}

// 初始化TTS编辑页面
function initTTSEditPage() {
	for ( var i = 1; i < MAX_SCENARIO_COUNT; i++) {
		$("#btd-display" + i).hide();
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
			MAX_SCENARIO_COUNT = info.length;
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["key"];
				var weight = info[i]["weight"];
				var words = info[i]["words"];		
				var type = info[i]["value"];
				var responseTrName = name.indexOf('&&') > -1 ? name.split("&&")[0] : name;
				if(responseTrName != 'TTS' && responseTrName != 'code' && responseTrName != '节点名' && responseTrName != '是否末梢编码') {
					$("#btd-display" + weight).show();
					if ('自定义' == type){
						$("#otherResponse0name" + weight).html(responseTrName + "：");
						$("#otherResponse0value" + weight).textbox({
							multiline: true,
							width: 240,
							height:50
						});
						$("#otherResponse0value" + weight).textbox('clear');
					} else {
						$("#otherResponse0name" + weight).html(responseTrName + ":");
						$("#otherResponse0value" + weight).combobox({
							width: 240,
							valueField: 'id',    
					        textField: 'text',
					        data : words
						});
						$("#otherResponse0value" + weight).combobox('clear');
					}
				}
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 设置页面布局
function diagramStyle() {
	var _PageHeight = window.innerHeight;
	$('#myPaletteDiv').css("height", _PageHeight * 0.4);
	$('#myOverViewDiv').height(_PageHeight * 0.2);
	$('#myDiagramDiv').height(_PageHeight);
	$('#saveBtSubmit').linkbutton('resize', {
		width : '100%',
		height : 32
	});
	$('#undoBtSubmit').linkbutton('resize', {
		width : '100%',
		height : 32
	});
	$('#searchBtSubmit').linkbutton('resize', {
		width : '100%',
		height : 32
	});
	$('#editElementBtn').linkbutton('resize', {
		width : '100%',
		height : 32
	});
}

// 初始化用户回答
function initCustomerAnswer() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'queryCustomerAnswer',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				var infos = data.rows;
				var customerAnswerHtml = '';
				for(var i=0; i<infos.length; i++) {
					customerAnswerHtml += '</br>&nbsp;&nbsp;<input name="customerAnswer" type="checkbox" value="'+infos[i].id+'"/>'+infos[i].id;												
				}
				customerAnswerHtml += '</br>&nbsp;&nbsp;<input name="customerAnswer" type="checkbox" value="跳出"/>跳出';
				$("#customerAnswerSpan").html('');
				$("#customerAnswerSpan").html('</br>'+customerAnswerHtml);
			}
		}
	});
}

// 初始化短信模板
function initSmsTemplate() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'querySmsTemplate',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				smsTemplates = data.rows;
				if(smsTemplates != null && smsTemplates.length > 0) {
					// 短信模板下拉框
					$("#templateId").combobox({
						width: 240,
						valueField: 'templateId',    
				        textField: 'templateName',
				        data : smsTemplates,
				        onChange: function(newVal, oldVal) {
				        	// 短信模板变量
				        	$("#sms-varibales-span").empty();
							if(smsTemplates != null && smsTemplates.length > 0) {
								for(var i=0; i<smsTemplates.length; i++) {
									if(newVal == smsTemplates[i].templateId) {
										var variableNames = smsTemplates[i].variableNames;
										for(var j=0; j<variableNames.length; j++) {
											var variableInputName = variableNames[j];
											$("#sms-varibales-span").append(variableInputName+":<br/>");
											var variableInputHtml = '<input type="text" id="'+variableInputName+'" name="'+variableInputName+'" style="width: 240px;"/>';
											$("#sms-varibales-span").append(variableInputHtml);
											$("#sms-varibales-span").append("<br/>");
										}
									}
								}
							}
				        } 
					});
				}
			}
		}
	});
}

// 初始化号码属性
function initPhoneAttributeNames() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'queryPhoneAttributeNames'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				phoneAttributeNames = data.rows;
			}
		}
	});
}

// 初始化关联意图
function initCollectionIntention() {
	// 添加信息关联意图页面
	$('#toAddCollectionIntentionPageBtn').click(function() {
		var collectionType = $('#collectionType').combobox('getValue');
		if(collectionType == 'elementCollection') {
			var elementName = $('#collectionElement').combobox('getValue');
			if(elementName == undefined || elementName == '') {
				$.messager.alert('提示', "请选择关联要素", "info");
				return;
			}
			$('#collectionIntentionAdd-title').textbox('setValue', elementName);
			$('#collectionIntentionAdd-title').textbox('disable'); 
		}
		$('#collectionIntentionAdd-wordclasses').val('');
		$('#collectionIntentionAddPage').window('open');
	});
	// 添加关联意图
	$('#collectionIntentionAdd-saveBtn').click(function() {
		saveCollectionIntention();
	});
	// 关闭关联意图
	$('#collectionIntentionAdd-closeBtn').click(function() {
		$('#collectionIntentionAddPage').window('close');
	});
	// 生成词模
	$('#collectionIntentionAdd-analyzeBtn').click(function() {	
		var collectionIntentionKeyWords = $.trim($("#collectionIntentionAdd-keywords").textbox('getValue'));
		addWordPatternFlag = 'collectionIntention';
		autoGenerateWordPattern(collectionIntentionKeyWords);
	});
	// 加载关联意图
	loadCollectionIntention();
}
// 加载关联意图
function loadCollectionIntention() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'queryCollectionIntention',
			scenariosid: publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				var collectionIntentions = data.rows;
				// 关联意图下拉框
				$("#collectionIntention").combobox({
					width: 240,
					valueField: 'id',    
			        textField: 'text',
			        data : collectionIntentions
				});
			}
		}
	});
}

// 初始化重复次数
function initCollectionTimes() {
	$("#collectionTimes").combobox({
		width: 240,
		valueField: 'id',    
        textField: 'text',
        data : [{
    		"id":"1",
    		"text":"1次"
    	},{
    		"id":"2",
    		"text":"2次"
    	}]
	});
}

function initDTMFPress()
{
    $.each($("input[name='pressType']"),function (index) {
        if(index==0) {
            $("#gather_numbers").fadeOut('slow');
            $("#press_numbers").fadeIn('slow');
            $(this).radiobutton({
                label: '按键',
                labelPosition:"after",
                value: "0",
                checked: true,
                onChange: function (checked) {
                    if(checked)
                    {
                        $("#gather_numbers").fadeOut('slow');
                        $("#press_numbers").fadeIn('slow');
                    }
                }
            });
        }
        else
        {
            $(this).radiobutton({
                label: '收号',
                value: "1",
                labelPosition:"after",
                checked: false,
                onChange: function (checked) {
                    if(checked)
                    {
                        $("#gather_numbers").fadeIn('slow');
                        $("#press_numbers").fadeOut('slow');
                    }
                }
            });
        }
    });

}


function initTransfer()
{
}

var condition = "<div class=\"form-div and-condition-div\">" +
"                            <div class=\"form-div\"> " +
"								 <input class=\"easyui-combobox\" name=\"variable_type\" data-options=\"prompt:'变量类型'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <a style='width: 20px;height: 10px' href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-remove'\">+移除</a>" +
"							 </div> " +
"                            <div class=\"form-div\"> " +
"								 <input class=\"easyui-textbox\" name=\"param_name_other\" data-options=\"prompt:'其他变量'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <input class=\"easyui-combobox\" name=\"param_name_attr\" data-options=\"prompt:'客户属性'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <input class=\"easyui-combobox\" name=\"param_name_element\" data-options=\"prompt:'关联要素'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <input class=\"easyui-combobox\" name=\"interface_name\" data-options=\"prompt:'接口名称'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <input class=\"easyui-combobox\" name=\"interface_param\" data-options=\"prompt:'接口参数'\" style=\"width: 200px; height: 25px;\"/>" +
"							 </div>" +
"                            <div class=\"form-div\"> <input class=\"easyui-combobox\" name=\"param_relation\" data-options=\"prompt:'比较关系',valueField: 'id',textField: 'text',data: [{id: '大于', text: '大于'},{ id: '小于', text: '小于'},{ id: '等于', text: '等于'},{ id: '不等于', text: '不等于'},{ id: '大于等于', text: '大于等于'},{ id: '小于等于', text: '小于等于'}]\" style=\"width: 200px; height: 25px;\"/></div>" +
"                            <div class=\"form-div\"> <input class=\"easyui-combobox\" name=\"param_type\" data-options=\"prompt:'比较值类型',valueField: 'id',textField: 'text',data: [{id: 'String', text: 'String'},{ id: 'Integer', text: 'Integer'},{ id: 'Variable', text: 'Variable'}]\" style=\"width: 200px; height: 25px;\"/></div>" +
"                            <div class=\"form-div\"> " +
"								 <input class=\"easyui-combobox\" name=\"element_value\" data-options=\"prompt:'关联要素值'\" style=\"width: 200px; height: 25px;\"/>" +
"								 <a href=\"javascript:void(0)\" name=\"element_value_insert_btn\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-save'\">插入</a>" +
"							 </div>" +
"                            <div class=\"form-div\"> <input class=\"easyui-textbox\" name=\"param_value\" multiline=\"true\" data-options=\"prompt:'比较值'\" style=\"width: 200px; height: 50px;\"/></div>" +
"                            <div class=\"form-div updown\">" +
"                                <div style=\"padding:3px;float:left;width: 20%\"> <input type=\"checkbox\" name=\"isUpDown\"  style=\"width:30%\"><span style=\"width: 70%\">浮动</span></div>" +
"                                <div style=\"width: 80%\">" +
"                                    <input class=\"easyui-combobox\" name=\"upDownType\" data-options=\"prompt:'上下浮动',valueField: 'id',textField: 'text',data: [{id: '0', text: '上浮'},{ id: '1', text: '下浮'}]\" style=\"width: 30%\"/>" +
"                                    <input class=\"easyui-textbox\" name=\"updownRatio\" style=\"width:30%\" data-options=\"prompt:'18'\" buttonText=\"%\">" +
"                                </div>" +
"                            </div>" +
"                        </div>";


var addAnd = " <div class=\"form-div condition-div\" style=\"padding-top: 5px;background: #E2E2E2;\">" +
"                        <div class=\"form-div\">" +
"							 <input class=\"easyui-textbox\" name=\"condition_name\" data-options=\"prompt:'条件名'\" style=\"width: 200px; height: 25px;\"/>" +
"                        </div>"+
"                        <div add=\"andconditions\">" +
"                            " +
"                        </div>"+
"                        <div id=\"addAndCondition\">" +
"                            <a href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-add'\">+新增(AND)条件</a>" +
"                            <a style='float: right;padding-right: 15px;'  href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-delete'\"></a>" +
"                        </div>" +
"                    </div>";
// 添加条件
function addOneCondition(conditionIdx, variableIdx) {
	var cpAddAnd = $($.parseHTML(addAnd));//$(addAnd);
	var conditionNameInput = "condition_name" + "_cdt"+conditionIdx;
	cpAddAnd.find("input[name='condition_name']").attr("name", conditionNameInput);
	cpAddAnd.find("input[name='"+conditionNameInput+"']").attr("id", conditionNameInput);
	var nodetmp = $("#conditions").find("div").eq(0).append(cpAddAnd);
	$.parser.parse(cpAddAnd);
	
	// 添加AND条件
	function addVariable(conditionIdx, variableIdx) {
		var cpCondition = $($.parseHTML(condition));//$($.parseHTML(this.linkTemplate))
		var cphr=$("</br></br><hr style=\"width:200px;height:1px;border:none;border-top:1px dashed #FFFFFF;\" />");
	    if(variableIdx==0) { 
	    	cpCondition.find("a").eq(0).remove();
	    } else {
	    	cpCondition.find("a").eq(0).bind("click",function () {
	    		cphr.remove();
	    		cpCondition.remove();
	    	})
	    }

	    var cpUpdown = cpCondition.find("input[name='isUpDown']");
	    cpUpdown.parent().parent().find("div").eq(1).hide();
	    cpUpdown.bind("click",function () {
	       if(!$(this).is(":checked"))
	       {
	    	   $(this).parent().parent().find("div").eq(1).fadeOut("slow");
	       }
	       else
	       {
	    	   $(this).parent().parent().find("div").eq(1).fadeIn("slow");
	       }
	    });
	    cpUpdown.prop("checked",false);
	    
	    var variableTypeInput = "variable_type" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramNameAttrInput = "param_name_attr" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramNameElementInput = "param_name_element" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramNameOtherInput = "param_name_other" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramRelationInput = "param_relation" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramTypeInput = "param_type" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var elementValueInput = "element_value" + "_cdt"+conditionIdx + "_and"+variableIdx; 
	    var elementValueInsertBtn = "element_value_insert_btn" + "_cdt"+conditionIdx + "_and"+variableIdx; 
	    var paramValueInput = "param_value" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var isUpDownInput = "isUpDown" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var upDownTypeInput = "upDownType" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var updownRatioInput = "updownRatio" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var interfaceNameInput = "interface_name" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var interfaceParamInput = "interface_param" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    
	    
	    cpCondition.find("input[name='variable_type']").attr("name", variableTypeInput);
	    cpCondition.find("input[name='param_name_attr']").attr("name", paramNameAttrInput);
	    cpCondition.find("input[name='param_name_element']").attr("name", paramNameElementInput);
	    cpCondition.find("input[name='param_name_other']").attr("name", paramNameOtherInput);
	    cpCondition.find("input[name='param_relation']").attr("name", paramRelationInput);
	    cpCondition.find("input[name='param_type']").attr("name", paramTypeInput);
	    cpCondition.find("input[name='element_value']").attr("name", elementValueInput);
	    cpCondition.find("a[name='element_value_insert_btn']").attr("name", elementValueInsertBtn);
	    cpCondition.find("input[name='param_value']").attr("name", paramValueInput);
	    cpCondition.find("input[name='isUpDown']").attr("name", isUpDownInput);
	    cpCondition.find("input[name='upDownType']").attr("name", upDownTypeInput);
	    cpCondition.find("input[name='updownRatio']").attr("name", updownRatioInput);
	    cpCondition.find("input[name='interface_name']").attr("name", interfaceNameInput);
	    cpCondition.find("input[name='interface_param']").attr("name", interfaceParamInput);
	    
	    cpCondition.find("input[name='"+variableTypeInput+"']").attr("id", variableTypeInput);
	    cpCondition.find("input[name='"+paramNameAttrInput+"']").attr("id", paramNameAttrInput);
	    cpCondition.find("input[name='"+paramNameElementInput+"']").attr("id", paramNameElementInput);
	    cpCondition.find("input[name='"+paramNameOtherInput+"']").attr("id", paramNameOtherInput);
	    cpCondition.find("input[name='"+paramRelationInput+"']").attr("id", paramRelationInput);
	    cpCondition.find("input[name='"+paramTypeInput+"']").attr("id", paramTypeInput);
	    cpCondition.find("input[name='"+paramValueInput+"']").attr("id", paramValueInput);
	    cpCondition.find("input[name='"+elementValueInput+"']").attr("id", elementValueInput);
	    cpCondition.find("a[name='"+elementValueInsertBtn+"']").attr("id", elementValueInsertBtn);
	    cpCondition.find("input[name='"+isUpDownInput+"']").attr("id", isUpDownInput);
	    cpCondition.find("input[name='"+upDownTypeInput+"']").attr("id", upDownTypeInput);
	    cpCondition.find("input[name='"+updownRatioInput+"']").attr("id", updownRatioInput);
	    cpCondition.find("input[name='"+interfaceNameInput+"']").attr("id", interfaceNameInput);
	    cpCondition.find("input[name='"+interfaceParamInput+"']").attr("id", interfaceParamInput);
	    
	    var andcd = cpAddAnd.find("div[add='andconditions']");
	    var nodeCondition =andcd.append(cpCondition);
	    var nodeHr =andcd.append(cphr);
	    $.parser.parse(cpCondition);
		$.parser.parse(cphr);
		
		$("#"+paramNameAttrInput).combobox({
			valueField: 'attributeName',    
	        textField: 'attributeDesc',
	        data : phoneAttributeNames
		});
		
		$(".updown").hide();
		$("#"+paramNameAttrInput).next().hide();
		$("#"+paramNameElementInput).next().hide();
		$("#"+paramNameOtherInput).next().hide();
		$("#"+elementValueInput).next().hide();
		$("#"+elementValueInsertBtn).hide();
		$("#"+interfaceNameInput).next().hide();
		$("#"+interfaceParamInput).next().hide();
		
		// 初始化变量类型
		$("#"+variableTypeInput).combobox({
			valueField: 'id',    
	        textField: 'text',
	        data : [{'text': '号码属性', 'id': 'attr'},{'text': '关联要素', 'id': 'element'},{'text': '接口参数', 'id': 'interface'},{'text': '其他变量', 'id': 'other'}],
	        onChange: function(newVal, oldVal) {
				if(newVal == "attr") {
					$("#"+paramNameAttrInput).next().show();
					$("#"+paramNameElementInput).next().hide();
					$("#"+paramNameOtherInput).next().hide();
					$("#"+elementValueInput).next().hide();
					$("#"+elementValueInsertBtn).hide();
					$("#"+paramValueInput).next().show();
					$("#"+interfaceNameInput).next().hide();
					$("#"+interfaceParamInput).next().hide();
				} 
				if(newVal == "element") {
					$("#"+paramNameAttrInput).next().hide();
					$("#"+paramNameElementInput).next().show();
					$("#"+paramNameOtherInput).next().hide();
					$("#"+elementValueInput).next().show();
					$("#"+elementValueInsertBtn).hide();
					$("#"+paramValueInput).next().hide();
					$("#"+interfaceNameInput).next().hide();
					$("#"+interfaceParamInput).next().hide();
				}
				if(newVal == "interface") {
					$("#"+paramNameAttrInput).next().hide();
					$("#"+paramNameElementInput).next().hide();
					$("#"+paramNameOtherInput).next().hide();
					$("#"+elementValueInput).next().hide();
					$("#"+elementValueInsertBtn).hide();
					$("#"+paramValueInput).next().show();
					$("#"+interfaceNameInput).next().show();
					$("#"+interfaceParamInput).next().show();
				}
				if(newVal == "other") {
					$("#"+paramNameAttrInput).next().hide();
					$("#"+paramNameElementInput).next().hide();
					$("#"+paramNameOtherInput).next().show();
					$("#"+elementValueInput).next().hide();
					$("#"+elementValueInsertBtn).hide();
					$("#"+paramValueInput).next().show();
					$("#"+interfaceNameInput).next().hide();
					$("#"+interfaceParamInput).next().hide();
				}
	        } 
		});
		// 插入比较值
		$("#"+elementValueInsertBtn).bind("click",function () {
			var paramValue = $("#"+paramValueInput).textbox('getValue');
			var elementValue = $("#"+elementValueInput).combobox('getValue');
			if(!(paramValue.indexOf(elementValue) > -1)) {
				if(paramValue == '') {
					$("#"+paramValueInput).textbox('setValue',elementValue);
				} else {
					$("#"+paramValueInput).textbox('setValue',paramValue+'|'+elementValue);
				}
			}
		});
		
		// 初始化关联要素
		initConditionElement(paramNameElementInput, elementValueInput);
		
		// 初始化接口名称
		initConditionInterface(interfaceNameInput, interfaceParamInput);
	};

	var cphr=$("<hr style=\"width:240px;height:1px;border:none;border-top:2px dashed #0066CC;\" />");
	nodetmp = $("#conditions").find("div").eq(0).append(cphr);
	$.parser.parse(cphr);

	var cpAction = cpAddAnd.find("#addAndCondition").find("a");
	cpAction.eq(0).attr("id","andBtn" + "_cdt"+conditionIdx);
	cpAction.eq(0).bind("click",function (){
		addVariable(conditionIdx, variableIdx++);
	});

	if(0==conditionIdx) {
		cpAction.eq(1).remove();
	} else {
		cpAction.eq(1).bind("click",function () {
			$(this).parent().parent().remove();
			cphr.remove();
		})
	}
	addVariable(conditionIdx, variableIdx++);
}
// 初始化关联要素
function initConditionElement(paramNameElementInput, elementValueInput) {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'listAllSceneElement',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.total > 0) {
				$("#"+paramNameElementInput).combobox({
					valueField: 'id',    
			        textField: 'text',
			        data : data.rows,
			        onChange: function(newVal, oldVal) {
			        	// 加载场景要素值
	        			$("#"+elementValueInput).combobox({
	        				url : '../interactiveSceneCall.action?type=listAllElementValue&scenariosid='+publicscenariosid+'&sceneElementName='+newVal,
							valueField: 'id',    
					        textField: 'text'
						});
			        } 
				});
			}
		}
	});
}
// 初始化接口名称
function initConditionInterface(interfaceNameInput, interfaceParamInput) {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'loadInterfaceName',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				$("#"+interfaceNameInput).combobox({
					valueField: 'id',    
			        textField: 'text',
			        data : data.rows,
			        onChange: function(newVal, oldVal) {
			        	// 加载接口参数
			        	$.ajax({
			    			url : '../interactiveSceneCall.action',
			    			type : "post",
			    			data : {
			    				type : 'queryInterfaceInfo',
			    				scenariosid : publicscenariosid,
			    				interfaceName : newVal
			    			},
			    			async : false,
			    			dataType : "json",
			    			success : function(data, textStatus, jqXHR) {
			    				if(data.success) {
			    					var interfaceInfo = data.interfaceInfo;
			    					var inParams = interfaceInfo.inParams;
			    					var outParams = interfaceInfo.outParams;
			    					var interfaceParams = [];
			    					for(var i = 0; i < inParams.length;i++) {
			    						var interfaceParam = {};
			    						interfaceParam.paramName = inParams[i].paramName;
			    						interfaceParam.paramValue = inParams[i].paramValue;
			    						interfaceParams.push(interfaceParam);
			    					}
			    					for(var i = 0; i < outParams.length;i++) {
			    						var interfaceParam = {};
			    						interfaceParam.paramName = outParams[i].paramName;
			    						interfaceParam.paramValue = outParams[i].paramValue;
			    						interfaceParams.push(interfaceParam);
			    					}
			    					$("#"+interfaceParamInput).combobox({
			    						valueField: 'paramName',    
			    				        textField: 'paramName',
			    				        data : interfaceParams,
			    					});
			    				}
			    			}
			    		});
			        } 
				});
			}
		}
	});
}
// 初始化条件组件
function initCondition(){
	
	// 初始化号码属性
	//initPhoneAttributeNames();
	
	$("#addCondition").bind("click",function () {
		var conditionDivs = $("#conditions").find("div").eq(0).find('.condition-div');
		var conditionCount = conditionDivs.length;
		addOneCondition(conditionCount, 0);
	});
}

var inKeyDiv = "<div class=\"form-div in-key\">" +
					"<input name=\"in_key\" style=\"float:left;width: 30%;height:25px;border:1px solid  #C3D1DF;\">" +
					"<input name=\"in_value\" style=\"float:left;width: 30%;height:25px;border:1px solid  #C3D1DF;\"> " +
					"<a href=\"javascript:addInKeyDiv()\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-add'\"></a>" +
					"<a href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-remove'\"></a>" +
				"</div>";
var outKeyDiv = "<div class=\"form-div out-key\">" +
					"<input name=\"out_key\" style=\"float:left;width: 30%;height:25px;border:1px solid  #C3D1DF;\">" +
					"<input name=\"out_value\" style=\"float:left;width: 30%;height:25px;border:1px solid  #C3D1DF;\"> " +
					"<a href=\"javascript:addOutKeyDiv()\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-add'\"></a>" +
					"<a href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-remove'\"></a>" +
				"</div>";

function addInKeyDiv() {
	var inKey = $(inKeyDiv);
	var inKeyDivs = $("#in_key_vals_div").find('.in-key');
	var inKeyIndex = inKeyDivs.length;
	
	var inKeyNameInput = "in_key_"+inKeyIndex;
	inKey.find("input[name='in_key']").attr("name", inKeyNameInput);
	inKey.find("input[name='"+inKeyNameInput+"']").attr("id", inKeyNameInput);
	
	var inValueNameInput = "in_value_"+inKeyIndex;
	inKey.find("input[name='in_value']").attr("name", inValueNameInput);
	inKey.find("input[name='"+inValueNameInput+"']").attr("id", inValueNameInput);
	
	inKey.find('a').eq(1).bind('click', function () {
		this.parentNode.remove();
	});
	if(0==inKeyIndex) {
		inKey.find('a').eq(1).remove();
	} 
	var append = $("#in_key_vals_div").append(inKey);
	$.parser.parse(append);
}

function addOutKeyDiv() {
	var outKey = $(outKeyDiv);
	var outKeyDivs = $("#out_key_vals_div").find('.out-key');
	var outKeyIndex = outKeyDivs.length;
	
	var outKeyNameInput = "out_key_"+outKeyIndex;
	outKey.find("input[name='out_key']").attr("name", outKeyNameInput);
	outKey.find("input[name='"+outKeyNameInput+"']").attr("id", outKeyNameInput);
	
	var outValueNameInput = "out_value_"+outKeyIndex;
	outKey.find("input[name='out_value']").attr("name", outValueNameInput);
	outKey.find("input[name='"+outValueNameInput+"']").attr("id", outValueNameInput);
	
	outKey.find('a').eq(1).bind('click', function () {
		this.parentNode.remove();
	});
	if(0==outKeyIndex) {
		outKey.find('a').eq(1).remove();
	}
	var append = $("#out_key_vals_div").append(outKey);
	$.parser.parse(append);
}

function loadInterfaceName() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : "post",
		data : {
			type : 'loadInterfaceName',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				$('#URLActionform #interfaceName').combobox({
					valueField : 'id',
					textField : 'text',
					data : data.rows,
					onChange: function(newVal, oldVal) {
						
			        } 
				});
			} 
		}
	});
}

function initURLAction(){
	addInKeyDiv();
	addOutKeyDiv();
	
	// 接口名称
	loadInterfaceName();
	// 测试URL
	$('#testAction').click(function() {
		var actionUrl = $("#actionUrl").textbox("getValue");
		$.ajax({
			url : '../interactiveSceneCall.action',
			type : "post",
			data : {
				type : 'testUrl',
				actionUrl : actionUrl
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				if(data.success) {
					$.messager.alert("提示","测试成功","info");
				} else {
					$.messager.alert("提示","测试失败","info");
				}
			}
		});
	});
	// 调用方式
	$('#invocationWay').combobox({
		valueField : 'id',
		textField : 'text',
		data : [{'text':'http','id':'http'},{'text':'webservice','id':'webservice'}],
		onChange: function(newVal, oldVal) {
			if(newVal == 'http') {
				$('.http').show();
				$('.webservice').hide();
			}
			if(newVal == 'webservice') {
				$('.http').hide();
				$('.webservice').show();
			}
        } 
	});
	// 接口编辑区
	$('#addInterfaceBtn').click(function() {
		saveOrUpdateInterfaceFlag = 'save';
		$('#in_key_vals_div').find(".in-key").remove();
		$('#out_key_vals_div').find(".out-key").remove();
		var inKeyDivs = $("#in_key_vals_div").find('.in-key');
		var outKeyDivs = $("#out_key_vals_div").find('.out-key');
		if(inKeyDivs.length==0) {
			addInKeyDiv();
		}
		if(outKeyDivs.length==0) {
			addOutKeyDiv();
		}
		$('.http').hide();
		$('.webservice').hide();
		$('#editInterfaceForm').form('clear');
		$('#editInterfaceDiv').window('open');
	});
	$('#editInterfaceBtn').click(function() {
		saveOrUpdateInterfaceFlag = 'update';
		$('#editInterfaceForm').form('clear');
		// 查询接口信息
		var interfaceName = $("#URLActionform #interfaceName").combobox('getValue');
		if(interfaceName == undefined || interfaceName == '') {
			$.messager.alert('提示', "请选择接口", "info");
			return;
		}
		$.ajax({
			url : '../interactiveSceneCall.action',
			type : 'post',
			dataType : 'json',
			data : {
				type: 'queryInterfaceInfo',
				interfaceName: interfaceName
			},
			success : function(data) {
				if (data.success) {
					var interfaceInfo = data.interfaceInfo;
					var inKeys = interfaceInfo.inParams;
					var outKeys = interfaceInfo.outParams;
					$('#editInterfaceForm').form('load',interfaceInfo);
					$('#in_key_vals_div').find(".in-key").remove();
					$('#out_key_vals_div').find(".out-key").remove();
					for(var i=0; i < inKeys.length; i++) {
						var inKey = inKeys[i];
						addInKeyDiv();
						$("#in_key_"+i).val(inKey.paramName);
						$("#in_value_"+i).val(inKey.paramValue);
					}
					for(var j=0; j < outKeys.length; j++) {
						var outKey = outKeys[j];
						addOutKeyDiv();
						$("#out_key_"+j).val(outKey.paramName);
						$("#out_value_"+j).val(outKey.paramValue);
					}
					if(inKeys.length==0) {
						addInKeyDiv();
					}
					if(outKeys.length==0) {
						addOutKeyDiv();
					}
					if(interfaceInfo.invocationWay == 'http') {
						$('.http').show();
						$('.webservice').hide();
					}
					if(interfaceInfo.invocationWay == 'webservice') {
						$('.http').hide();
						$('.webservice').show();
					}
					$('#editInterfaceDiv').window('open');
				} 
			},
			error : function(xhr, status, error) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	});
	// 保存接口
	$('#saveNewInterface').click(function() {
		var array = $('#editInterfaceForm').serializeArray();
		var interfaceData={};
		interfaceData.saveOrUpdateInterfaceFlag = saveOrUpdateInterfaceFlag;
		$.each(array,function(index, value){
			interfaceData[this.name] = this.value;
		});
		if(interfaceData.interfaceName == '') {
			$.messager.alert("提示","请填写接口名称","info");
			return;
		}
		if(interfaceData.actionUrl == '') {
			$.messager.alert("提示","请填写接口地址","info");
			return;
		}
		if(interfaceData.invocationWay == '') {
			$.messager.alert("提示","请选择调用方式","info");
			return;
		}
		if(interfaceData.invocationWay == 'http' && interfaceData.httpMethod == '') {
			$.messager.alert("提示","请选择请求方法","info");
			return;
		}
		if(interfaceData.invocationWay == 'webservice' && interfaceData.namespace == '') {
			$.messager.alert("提示","请填写命名空间","info");
			return;
		}
		if(interfaceData.invocationWay == 'webservice' && interfaceData.functionName == '') {
			$.messager.alert("提示","请填写调用函数","info");
			return;
		}
		if(!checkUrl(interfaceData.actionUrl)) {
			$.messager.alert("提示","请检查接口地址","info");
			return;
		}
		// 设置数据
		var inKeyDivs = $("#in_key_vals_div").find('.in-key');
		var outKeyDivs = $("#out_key_vals_div").find('.out-key');
		var inKeys = [];
		var outKeys = [];
		for(var i=0; i<inKeyDivs.length; i++) {
			inKeys[i]={};
		}
		for(var j=0; j<outKeyDivs.length; j++) {
			outKeys[j]={};
		}
		$.each(array,function(index, value){
			if(this.name.indexOf("in_key") > -1) {
				var inKeySliptArray = this.name.split("_");
				var inKeyIndex = inKeySliptArray[inKeySliptArray.length-1];
				inKeys[inKeyIndex].paramName = this.value;
			} else if(this.name.indexOf("in_value") > -1) {
				var inValueSliptArray = this.name.split("_");
				var inValueIndex = inValueSliptArray[inValueSliptArray.length-1];
				inKeys[inValueIndex].paramValue = this.value;
			} else if(this.name.indexOf("out_key") > -1) {
				var outKeySliptArray = this.name.split("_");
				var outKeyIndex = outKeySliptArray[outKeySliptArray.length-1];
				outKeys[outKeyIndex].paramName = this.value;
			} else if(this.name.indexOf("out_value") > -1) {
				var outValueSliptArray = this.name.split("_");
				var outValueIndex = outValueSliptArray[outValueSliptArray.length-1];
				outKeys[outValueIndex].paramValue = this.value;
			} 
		});
		interfaceData.inParams = inKeys;
		interfaceData.outParams = outKeys;
		$.ajax({
			url : '../interactiveSceneCall.action',
			type : 'post',
			dataType : 'json',
			data : {
				type: 'addNewInterface',
				interfaceData: JSON.stringify(interfaceData)
			},
			success : function(data) {
				if (data.success) {
					$.messager.alert('提示', "保存成功", "info");
					$('#editInterfaceDiv').window('close');
					loadInterfaceName();
				} else {
					$.messager.alert('提示', data.msg, "info");
				}
			},
			error : function(xhr, status, error) {
				$.messager.alert('系统异常', "请求数据失败!", "error");
			}
		});
	});
}

// 制作流程图
function makeGraphObject() {
	var _thisGraphObj = null;
	
	myDiagram = $GO(go.Diagram, "myDiagramDiv", { // 必须命名或引用DIV HTML元素
		initialContentAlignment: go.Spot.Center, // 居中显示Diagram内容
        allowDrop: true, // 允许拖拽
        allowCopy: false, // 禁止复制
        scrollsPageOnFocus: true,// 滚动页焦点
        isReadOnly: false,// 禁止编辑
        // "draggingTool.dragsLink": true, // 拖动工具拖动链接
        "draggingTool.isGridSnapEnabled": true, // 拖动工具已启用网格捕捉
        "animationManager.isEnabled":false, // 取消加载动画
        "linkingTool.isUnconnectedLinkValid": false, // 链接工具未连接链接有效
        "linkingTool.portGravity": 200, // 链接工具端口重力
        "relinkingTool.isUnconnectedLinkValid": true, // 重新连接工具未连接链接有效
        "relinkingTool.portGravity": 250, // 重新连接工具端口重力
        "relinkingTool.fromHandleArchetype":
            $GO(go.Shape, "Diamond", {
                segmentIndex: 0,
                cursor: "pointer",
                desiredSize: new go.Size(8, 8),
                fill: "tomato",
                stroke: "darkred",
            }),
        "relinkingTool.toHandleArchetype":
            $GO(go.Shape, "Diamond", {
                segmentIndex: -1,
                cursor: "pointer",
                desiredSize: new go.Size(8, 8),
                fill: "darkred",
                stroke: "tomato"
            }),
        "linkReshapingTool.handleArchetype":
            $GO(go.Shape, "Diamond", {
                desiredSize: new go.Size(7, 7),
                fill: "lightblue",
                stroke: "deepskyblue"
            }),
        "rotatingTool.snapAngleMultiple": 15,//角度
        "rotatingTool.snapAngleEpsilon": 15,
        "undoManager.isEnabled": true//键盘事件
	});

	// 制作端口
	function makePort(name, align, spot, output, input) {
		var horizontal = align.equals(go.Spot.Top) || align.equals(go.Spot.Bottom);
		return $GO(go.Shape, "Rectangle", {
			fill : "transparent", // 在MouseEnter事件处理程序中更改为颜色
			strokeWidth : 0, // 无描边
			desiredSize : new go.Size(50, 50),
			width : horizontal ? NaN : 8, // 如果不是水平伸展，只有8宽
			height : !horizontal ? NaN : 8, // 如果不是垂直拉伸，只有8高
			alignment : align, // 对齐主形状上的端口
			stretch : (horizontal ? go.GraphObject.Horizontal : go.GraphObject.Vertical),
			portId : name, // 将此对象声明为“端口”
			fromSpot : spot, // 声明链接可以在此端口连接的位置
			fromLinkable : output, // 声明用户是否可以从此处绘制链接
			toSpot : go.Spot.Top, // 声明链接可以在此端口连接的位置
			toLinkable : input, // 声明用户是否可以在此绘制链接
			cursor : "pointer", // 显示不同的光标以指示潜在的链接点
		 mouseEnter : function(e, port) {
			if (!e.diagram.isReadOnly)
				port.fill = "rgba(255,0,255,0.5)";
		 },
		 mouseLeave : function(e, port) {
			 port.fill = "transparent";
		 }
		});
	}

	// 定义常规节点模板
	myDiagram.nodeTemplateMap.add("Normal",
		$GO(go.Node, "Table", nodeStyle(),
			$GO(go.Panel, "Auto", $GO(go.Shape, "RoundedRectangle", {
					portId: "",
	                fromLinkable: false,
	                toLinkable: false,
	                cursor: "move",
	                fill: "white",
	                stroke: '#9b9b9b',
	                strokeWidth: 1,								
				}, 
				new go.Binding("figure", "figure")),
				$GO(go.TextBlock, textStyle(), {
                    font: "12px sans-serif",
                    maxSize : new go.Size(300, 200),
                    maxLines: 5,
                    verticalAlignment: go.Spot.Center,
                    editable: false,
                    alignment: go.Spot.Center,
                    overflow: go.TextBlock.OverflowEllipsis,
                    stroke: '#4c5667'
                }, 
                new go.Binding("text").makeTwoWay(),
                new go.Binding("width", "width"),
				new go.Binding("height", "height"),
				new go.Binding("margin", "margin")), 
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "wordsContent").makeTwoWay()), 
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "code").makeTwoWay()), 
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "otherResponse").makeTwoWay()), 
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "action").makeTwoWay()),
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "actionParams").makeTwoWay()),
				$GO(go.Panel, 'Horizontal',
		                {
		                    alignment: go.Spot.Bottom,
		                    margin: new go.Margin(10, 0, 0, 0)			                     
		                },
		                $GO(go.Panel, 'Horizontal',
		                    new go.Binding("itemArray", "bottomArray"),
		                    {
		                        itemTemplate:
		                        	$GO(go.Panel,
		                                {
		                                    margin: 10,
		                                    fromLinkable: true,
		                                    toLinkable: true,
		                                    fromLinkableDuplicates: true,
		                                    toLinkableDuplicates: true,
		                                    fromSpot: go.Spot.Bottom,
		                                    toSpot: go.Spot.Bottom,
		                                    fromMaxLinks: 1,
		                                    toMaxLinks: 0,
		                                    cursor: "crosshair"
		                                }, new go.Binding('portId', 'text'),
		                                $GO(go.Panel, 'Auto',
		                                    $GO(go.Shape, "Rectangle",
		                                        {
		                                            fill: '#DCEAFC',
		                                            stroke: null,
		                                        }),
		                                    $GO(go.TextBlock,
		                                        {
		                                            margin: 5,
		                                            stroke: "#1883D7",
		                                            font: "12px sans-serif"
		                                        },
		                                        new go.Binding('text', 'text')
		                                    )
		                                )
		                            )
		                    }
		                )			                           
		            )
				),
				makePort("T", go.Spot.Top, go.Spot.TopSide, false, true)));

	// 定义开始节点模板
	myDiagram.nodeTemplateMap.add("Start", 
			$GO(go.Node, "Table", nodeStyle(), 
					$GO(go.Panel, "Auto", 
						$GO(go.Shape, "RoundedRectangle",
		                    {
			                portId: "",
			                fromLinkable: false,
			                toLinkable: false,
			                cursor: "move",
			                fill: "#01AAED",
			                stroke: '#9b9b9b',
			                strokeWidth: 1,
			            },
			            new go.Binding("figure"),
			            new go.Binding("fill")), 
			            $GO(go.TextBlock, "Start", {
			            	font: "12px sans-serif",
	                        maxSize: new go.Size(210, 100),
	                        wrap: go.TextBlock.None,
	                        verticalAlignment: go.Spot.Center,
	                        alignment: go.Spot.Center,
	                        editable: false,
	                        stroke: '#000'
		            	}, 
		            	new go.Binding("text"),
		            	new go.Binding("width", "width"),
						new go.Binding("height", "height"),
						new go.Binding("margin", "margin"))), 
		            makePort("B", go.Spot.Bottom,go.Spot.Bottom, true, false)),
					$GO("TreeExpanderButton"));

	// 定义结束节点模板
	myDiagram.nodeTemplateMap.add("End", 
			$GO(go.Node, "Table", nodeStyle(),
				$GO(go.Panel, "Auto", 
					$GO(go.Shape, "RoundedRectangle",
						{
	                        portId: "",
	                        fromLinkable: false,
	                        toLinkable: false,
	                        cursor: "move",
	                        fill: "#FFCCCC",
	                        stroke: '#9b9b9b',
	                        strokeWidth: 1,
	                    },
	                    new go.Binding("figure"),
	                    new go.Binding("stroke", 'border'),
	                    new go.Binding("fill")),
					$GO(go.TextBlock, "End", {
						font: "12px sans-serif",
	                    maxSize : new go.Size(200, 200),
	                    maxLines: 10,
	                    verticalAlignment: go.Spot.Center,
	                    editable: false,
	                    alignment: go.Spot.Center,
	                    overflow: go.TextBlock.OverflowEllipsis,
	                    stroke: '#000'
					},
					new go.Binding("text").makeTwoWay(),
					new go.Binding("width", "width"),
					new go.Binding("height", "height"),
					new go.Binding("margin", "margin"))), 
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "wordsContent").makeTwoWay()), 
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "code").makeTwoWay()), 
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "otherResponse").makeTwoWay()), 
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "action").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "actionParams").makeTwoWay()),
					makePort("T", go.Spot.Top, go.Spot.Top, false, true)));
	
	// 定义信息收集模板
	myDiagram.nodeTemplateMap.add("Collection", $GO(go.Node, "Table", nodeStyle(),
			$GO(go.Panel, "Auto", 
					$GO(go.Shape, "RoundedRectangle",
						{
	                        portId: "",
	                        fromLinkable: false,
	                        toLinkable: false,
	                        cursor: "move",
	                        fill: "white",
	                        stroke: '#9b9b9b',
	                        strokeWidth: 1,
	                    },
	                    new go.Binding("figure"),
	                    new go.Binding("stroke", 'border'),
	                    new go.Binding("fill")),
					$GO(go.TextBlock, {
						font: "12px sans-serif",
	                    maxSize : new go.Size(200, 200),
	                    maxLines: 10,
	                    verticalAlignment: go.Spot.Top,
	                    editable: false,
	                    alignment: go.Spot.Top,
	                    overflow: go.TextBlock.OverflowEllipsis,
	                    stroke: '#000'
					},
					new go.Binding("width", "width"),
					new go.Binding("height", "height"),
					new go.Binding("margin", "collectionNameMargin"),
					new go.Binding("text", "collectionName").makeTwoWay()),
					$GO(go.TextBlock, {
						font: "8px sans-serif",
	                    maxSize : new go.Size(200, 200),
	                    maxLines: 10,
	                    verticalAlignment: go.Spot.Left,
	                    editable: false,
	                    alignment: go.Spot.Left,
	                    overflow: go.TextBlock.OverflowEllipsis,
	                    stroke: '#000'
					},
					new go.Binding("margin", "collectionTextMargin"),
					new go.Binding("text", "collectionText").makeTwoWay())),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionParam").makeTwoWay()), 
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionType").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionTimes").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionWords").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionElement").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "collectionIntention").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "interactiveType").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "menuStartWords").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "menuOptions").makeTwoWay()),
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "menuEndWords").makeTwoWay()),
					makePort("T", go.Spot.Top, go.Spot.Top, false, true),
					makePort("B", go.Spot.Bottom, go.Spot.Bottom, true, false)));
	
	// 定义DTMF按键模板
	myDiagram.nodeTemplateMap.add("DTMFPress", $GO(go.Node, "Table", nodeStyle(),
			$GO(go.Panel, "Auto", 
					$GO(go.Shape, "RoundedRectangle",
						{
	                        portId: "",
	                        fromLinkable: false,
	                        toLinkable: false,
	                        cursor: "move",
	                        fill: "white",
	                        stroke: '#9b9b9b',
	                        strokeWidth: 1,
	                    },
	                    new go.Binding("figure"),
	                    new go.Binding("stroke", 'border'),
	                    new go.Binding("fill")),
					$GO(go.TextBlock, "DTMFPress", {
						font: "12px sans-serif",
	                    maxSize : new go.Size(200, 200),
	                    maxLines: 10,
	                    verticalAlignment: go.Spot.Center,
	                    editable: false,
	                    alignment: go.Spot.Center,
	                    overflow: go.TextBlock.OverflowEllipsis,
	                    stroke: '#000'
					},
					new go.Binding("text").makeTwoWay(),
					new go.Binding("width", "width"),
					new go.Binding("height", "height"),
					new go.Binding("margin", "margin"))), 
					$GO(go.Panel, 'Horizontal',
			                {
			                    alignment: go.Spot.Bottom,
			                    margin: new go.Margin(10, 0, 0, 0)			                     
			                },
			                $GO(go.Panel, 'Horizontal',
			                    new go.Binding("itemArray", "bottomArray"),
			                    {
			                        itemTemplate:
			                        	$GO(go.Panel,
			                                {
			                                    margin: 10,
			                                    fromLinkable: true,
			                                    toLinkable: true,
			                                    fromLinkableDuplicates: true,
			                                    toLinkableDuplicates: true,
			                                    fromSpot: go.Spot.Bottom,
			                                    toSpot: go.Spot.Bottom,
			                                    fromMaxLinks: 1,
			                                    toMaxLinks: 0,
			                                    cursor: "crosshair"
			                                }, new go.Binding('portId', 'text'),
			                                $GO(go.Panel, 'Auto',
			                                    $GO(go.Shape, "Rectangle",
			                                        {
			                                            fill: '#DCEAFC',
			                                            stroke: null,
			                                        }),
			                                    $GO(go.TextBlock,
			                                        {
			                                            margin: 5,
			                                            stroke: "#1883D7",
			                                            font: "12px sans-serif"
			                                        },
			                                        new go.Binding('text', 'text')
			                                    )
			                                )
			                            )
			                    }
			                )			                           
			            ),
					makePort("T", go.Spot.Top, go.Spot.Top, false, true)));
	
	
	// 定义转人工模板
	myDiagram.nodeTemplateMap.add("Transfer", $GO(go.Node, "Table", nodeStyle(),
			$GO(go.Panel, "Auto", 
					$GO(go.Shape, "RoundedRectangle",
						{
	                        portId: "",
	                        fromLinkable: false,
	                        toLinkable: false,
	                        cursor: "move",
	                        fill: "white",
	                        stroke: '#9b9b9b',
	                        strokeWidth: 1,
	                    },
	                    new go.Binding("figure"),
	                    new go.Binding("stroke", 'border'),
	                    new go.Binding("fill")),
					$GO(go.TextBlock, "End", {
						font: "12px sans-serif",
	                    maxSize : new go.Size(200, 200),
	                    maxLines: 10,
	                    verticalAlignment: go.Spot.Center,
	                    editable: false,
	                    alignment: go.Spot.Center,
	                    overflow: go.TextBlock.OverflowEllipsis,
	                    stroke: '#000'
					},
					new go.Binding("text").makeTwoWay(),
					new go.Binding("width", "width"),
					new go.Binding("height", "height"),
					new go.Binding("margin", "margin"))), 
					makePort("T", go.Spot.Top, go.Spot.Top, false, true)));


		// 定义动作模板
		myDiagram.nodeTemplateMap.add("URLAction", $GO(go.Node, "Table", nodeStyle(),
		$GO(go.Panel, "Auto", 
				$GO(go.Shape, "RoundedRectangle",
					{
						portId: "",
						fromLinkable: false,
						toLinkable: false,
						cursor: "move",
						fill: "white",
						stroke: '#9b9b9b',
						strokeWidth: 1,
					},
					new go.Binding("figure"),
					new go.Binding("stroke", 'border'),
					new go.Binding("fill")),
				$GO(go.TextBlock, "End", {
					font: "12px sans-serif",
					maxSize : new go.Size(200, 200),
					maxLines: 10,
					verticalAlignment: go.Spot.Center,
					editable: false,
					alignment: go.Spot.Center,
					overflow: go.TextBlock.OverflowEllipsis,
					stroke: '#000'
				},
				new go.Binding("text").makeTwoWay(),
				new go.Binding("width", "width"),
				new go.Binding("height", "height"),
				new go.Binding("margin", "margin"))), 
				makePort("T", go.Spot.Top, go.Spot.Top, false, true),
				makePort("B", go.Spot.Bottom, go.Spot.Bottom, true, false)));
		
		// 定义条件模板
		myDiagram.nodeTemplateMap.add("Condition", $GO(go.Node, "Table", nodeStyle(),
		$GO(go.Panel, "Auto", 
				$GO(go.Shape, "RoundedRectangle",
					{
						portId: "",
						fromLinkable: false,
						toLinkable: false,
						cursor: "move",
						fill: "white",
						stroke: '#9b9b9b',
						strokeWidth: 1,
					},
					new go.Binding("figure"),
					new go.Binding("stroke", 'border'),
					new go.Binding("fill")),
				$GO(go.TextBlock, "Condition", 
					{
						font: "12px sans-serif",
						maxSize : new go.Size(200, 200),
						maxLines: 10,
						verticalAlignment: go.Spot.Center,
						editable: false,
						alignment: go.Spot.Center,
						overflow: go.TextBlock.OverflowEllipsis,
						stroke: '#000'
					},
				new go.Binding("text").makeTwoWay(),
				new go.Binding("width", "width"),
				new go.Binding("height", "height"),
				new go.Binding("margin", "margin")),
				$GO(go.Panel, 'Horizontal',
		                {
		                    alignment: go.Spot.Bottom,
		                    margin: new go.Margin(10, 0, 0, 0)			                     
		                },
		                $GO(go.Panel, 'Horizontal',
		                    new go.Binding("itemArray", "bottomArray"),
		                    {
		                        itemTemplate:
		                        	$GO(go.Panel,
		                                {
		                                    margin: 10,
		                                    fromLinkable: true,
		                                    toLinkable: true,
		                                    fromLinkableDuplicates: true,
		                                    toLinkableDuplicates: true,
		                                    fromSpot: go.Spot.Bottom,
		                                    toSpot: go.Spot.Bottom,
		                                    fromMaxLinks: 1,
		                                    toMaxLinks: 0,
		                                    cursor: "crosshair"
		                                }, new go.Binding('portId', 'text'),
		                                $GO(go.Panel, 'Auto',
		                                    $GO(go.Shape, "Rectangle",
		                                        {
		                                            fill: '#DCEAFC',
		                                            stroke: null,
		                                        }),
		                                    $GO(go.TextBlock,
		                                        {
		                                            margin: 5,
		                                            stroke: "#1883D7",
		                                            font: "12px sans-serif"
		                                        },
		                                        new go.Binding('text', 'text')
		                                    )
		                                )
		                            )
		                    }
		                )			                           
		            )),
				makePort("T", go.Spot.Top, go.Spot.Top, false, true)));
	
	// 链接模板
	function ParallelRouteLink() {
        go.Link.call(this);
    }
	
    go.Diagram.inherit(ParallelRouteLink, go.Link);
    ParallelRouteLink.prototype.computePoints = function () {
        var result = go.Link.prototype.computePoints.call(this);
        if (!this.isOrthogonal && this.curve !== go.Link.Bezier && this.hasCurviness()) {
            var curv = this.computeCurviness();
            if (curv !== 0) {
                var num = this.pointsCount;
                var pidx = 0;
                var qidx = num - 1;
                if (num >= 4) {
                    pidx++;
                    qidx--;
                }
                var frompt = this.getPoint(pidx);
                var topt = this.getPoint(qidx);
                var dx = topt.x - frompt.x;
                var dy = topt.y - frompt.y;
                var mx = frompt.x + dx * 1 / 8;
                var my = frompt.y + dy * 1 / 8;
                var px = mx;
                var py = my;
                if (-0.01 < dy && dy < 0.01) {
                    if (dx > 0) py -= curv; else py += curv;
                } else {
                    var slope = -dx / dy;
                    var e = Math.sqrt(curv * curv / (slope * slope + 1));
                    if (curv < 0) e = -e;
                    px = (dy < 0 ? -1 : 1) * e + mx;
                    py = slope * (px - mx) + my;
                }

                mx = frompt.x + dx * 7 / 8;
                my = frompt.y + dy * 7 / 8;
                var qx = mx;
                var qy = my;
                if (-0.01 < dy && dy < 0.01) {
                    if (dx > 0) qy -= curv; else qy += curv;
                } else {
                    var slope = -dx / dy;
                    var e = Math.sqrt(curv * curv / (slope * slope + 1));
                    if (curv < 0) e = -e;
                    qx = (dy < 0 ? -1 : 1) * e + mx;
                    qy = slope * (qx - mx) + my;
                }

                this.insertPointAt(pidx + 1, px, py);
                this.insertPointAt(qidx + 1, qx, qy);
            }
        }
        return result;
    };
    var linkSelectionAdornmentTemplate =
        $GO(go.Adornment, "Link",
            $GO(go.Shape,
                {isPanelMain: true, fill: null, stroke: "red", strokeWidth: 0})  // 使用选择对象的strokeWidth
        );

    myDiagram.linkTemplate =
        $GO(ParallelRouteLink,
            {selectable: true, selectionAdornmentTemplate: linkSelectionAdornmentTemplate},
            {relinkableFrom: true, relinkableTo: true, reshapable: true},
            {
            	//routing : go.Link.AvoidsNodes,
    			curve : go.Link.JumpOver,
    			corner : 5,
    			toShortLength : 4
            },
            new go.Binding("points").makeTwoWay(),
            $GO(go.Shape,
                {isPanelMain: true, strokeWidth: 2, stroke: '#00af84'}, new go.Binding('stroke', 'color')),//连接颜色
            $GO(go.Shape,
                {toArrow: "Standard", stroke: null}),
            $GO(go.Panel, "Auto",
                new go.Binding("visible", "isSelected").ofObject(),//隐藏连接中的text
                new go.Binding("hit"),
                $GO(go.Shape, "RoundedRectangle",
                    {fill: "#F8F8F8", stroke: null}),//text边框
                $GO(go.TextBlock,
                    {
                        textAlign: "center",
                        font: "10pt helvetica, arial, sans-serif",
                        stroke: "#333333",
                        margin: 1,
                        maxSize: new go.Size(NaN, NaN),
                        editable: false//中间文本是否修改
                    },
                    new go.Binding('text', 'name')
                )
            )
        );	
}

// 初始化流程图
function loadDiagramData() {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			type : "loadConfigData"
		},
		success : function(data) {
			if (data.success) {
				var ivrFlowData = data.rowdata[0].scenejsondata;
				myDiagram.model = go.Model.fromJson(JSON.parse(ivrFlowData));
			} else {
				var json = '{ "class": "go.GraphLinksModel","linkFromPortIdProperty": "fromPort","linkToPortIdProperty": "toPort","nodeDataArray": [],"linkDataArray": []}';
				myDiagram.model = go.Model.fromJson(json);
				var nodeDataArray = [ {
					"key" : 'Start',
					"category" : "Start",
					"loc" : "1 0",
					"text" : "开始",
					"width": 50,
					"height": NaN,
					"margin": new go.Margin(5, 10, 5, 30)
				}];
				myDiagram.model.nodeDataArray = nodeDataArray;
			}
		}
	});
}

// 定义左侧画板
function makePalette() {	
	var myPalette = $GO(go.Palette, "myPaletteDiv", {
		nodeTemplateMap : myDiagram.nodeTemplateMap,
		model : new go.GraphLinksModel([ {
			key : "Start",
			category : "Start",
			text : "开始",
			width: 50,
			height: 20,
			margin: 0,
		}, 			
		{
			key : "Normal",
			category : "Normal",
			text : "放音组件",
			width: 50,
			height: 20,
			margin: 0, 
			wordsContent: '',
			code:'',
			otherResponses: [],
			bottomArray : [],
			action: '',
			actionParams: ''
		}, {
			key : "Collection",
			category : "Collection",
			width: 50,
			height: 20,
			collectionNameMargin: 0,
			collectionTextMargin: 0,
			collectionName: '信息收集',
			collectionText: '',
			collectionParam: '',
			collectionType: '',
			collectionTimes: '',
			collectionWords: '',
			collectionElement: '',
			collectionIntention: '',
			interactiveType: '',
			menuStartWords: '',
			menuOptions: '',
			menuEndWords: ''
		},{
			key : "DTMFPress",
			category : "DTMFPress",
			text : "DTMF",
			width: 50,
			height: 20,
			margin: 0,
			dtmfName: '',
			dtmfAlias: '',
			dtmfAnswer: '',
			pressType: '',
			minLength: '',
			maxLength: '',
			endPressNumber: '',
			pressNumbers: '',
			pressTimeOut: '',
			pressTimeOutAnswer: '',
			attemptLimit: '',
			bottomArray : [],
		},{
			key : "URLAction",
			category : "URLAction",
			text : "动作组件",
			width: 50,
			height: 20,
			margin: 0,
			interfaceName:'',
			actionName:'',
			actionUrl:'',
			actionMethod:'',
			inParams:[],
			outParams:[]
		},{
			key : "Condition",
			category : "Condition",
			text : "条件组件",
			width: 50,
			height: 20,
			margin: 0,
			conditionNodeName: '',
			conditions: []
		},{
			key : "Transfer",
			category : "Transfer",
			text : "转人工",
			width: 50,
			height: 20,
			margin: 0,
			transferName: '',
			transferNumber: ''
		},{
			key : "End",
			category : "End",
			text : "结束语",
			width: 50,
			height: 20,
			margin: 0,
			wordsContent: '',
			code:'',
			otherResponses: [],
			bottomArray : [],
			action: '',
			actionParams: ''
		}])
	});
	
	// 概括内容
	var myOverView = $GO(go.Overview, "myOverViewDiv", {
		observed : myDiagram
	});
}	

// 添加监听事件
function addListenerEvents() {
	
	// 添加拖动事件
	myDiagram.addDiagramListener("ExternalObjectsDropped", function(e) {
		var subject = e.subject;
		var parameter = e.parameter;
		if (subject instanceof go.Set) {
			var node = subject.first();
			if (node instanceof go.GraphObject) {
				var nodeData = node.part.data;
				if (nodeData.category == "Start") {
					onlyNode(myDiagram, "Start", "Start节点只能有一个");
					myDiagram.model.setDataProperty(nodeData, 'width', 50);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', new go.Margin(5, 10, 5, 30));
				} 
				if (nodeData.category == "Normal") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', new go.Margin(5, 10, 40, 10));
				}
				if (nodeData.category == "Collection") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'collectionNameMargin', new go.Margin(5, 10, 40, 10));
					myDiagram.model.setDataProperty(nodeData, 'collectionTextMargin', new go.Margin(30, 10, 2, 10));
				}
				if (nodeData.category == "DTMFPress") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', new go.Margin(5, 10, 40, 10));
					myDiagram.model.setDataProperty(nodeData, 'bottomArray', [{'text':'获取到按键值'},{'text':'未获取到按键值'}]);
				}
				if (nodeData.category == "Condition") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', new go.Margin(5, 10, 40, 10));
					myDiagram.model.setDataProperty(nodeData, 'bottomArray', []);
				}
				if (nodeData.category == "Transfer") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', 10);
				}
				if (nodeData.category == "URLAction") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', 10);
				}
				if (nodeData.category == "End") {
					myDiagram.model.setDataProperty(nodeData, 'width', 200);
					myDiagram.model.setDataProperty(nodeData, 'height', NaN);
					myDiagram.model.setDataProperty(nodeData, 'margin', 10);
				}
			} 
		}
	});
	
	// 添加连线完成事件
	myDiagram.addDiagramListener("LinkDrawn", function(e) {
		var subject = e.subject;
		var nodeData = subject.part.data;
		var from = nodeData.from;
		var to = nodeData.to;
		if (from.match("Condition") && to.match("Condition")) {
			//myDiagram.commandHandler.deleteSelection();
			//$.messager.alert('警告', "不允许条件组件到条件组件的直连", "error");
		}
	});
	
	// 监听双击事件
	myDiagram.addDiagramListener("ObjectDoubleClicked", function(e) {
		var subject = e.subject;
		var nodeData = subject.part.data;
		public_thisGraphObj = subject;
		if (nodeData.category == "Normal") {
			$('#otherResponseHideBtn').trigger("click");
			// 回复编辑内容赋值
			setTTSResponseValue();
			$('#customer-answer-table').show();
			$('#tts-node-type-div').show();
			$('#myNormalEditDiv').show();
			$('#myConditionDiv').hide();
			$('#myCollectionEditDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myURLActionDiv').hide();
			$('#myTransferDiv').hide();
		} 
		if (nodeData.category == "End") {
			// 回复编辑内容赋值
			setTTSResponseValue();
			$('#customer-answer-table').hide();
			$('#tts-node-type-div').hide();
			$('#myNormalEditDiv').show();
			$('#myConditionDiv').hide();
			$('#myCollectionEditDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myURLActionDiv').hide();
			$('#myTransferDiv').hide();
		}
		if (nodeData.category == "Collection") {
			// 信息收集内容赋值
			$('#collectionform').form('clear');
			$('#collectionform').form('load',nodeData);
			if(nodeData.collectionType == 'elementCollection') {
				$('#collectionform-collectionParam-tr').show();
				$('#collectionform-collectionTimes-tr').hide();
				$('#collectionform-collectionElement-tr').show();
			}
			if(nodeData.collectionType == 'userInfoCollection') {
				$('#collectionform-collectionParam-tr').show();
				$('#collectionform-collectionTimes-tr').show();
				$('#collectionform-collectionElement-tr').hide();
			}
			if(nodeData.interactiveType == '词模匹配') { // 系统反问
				$('#collectionform-collectionWords-tr').show();
				$('#collectionform-menuItems-tr').hide();
			}
			if(nodeData.interactiveType == '键值补全') { // 菜单询问
				$('#collectionform-collectionWords-tr').hide();
				$('#collectionform-menuItems-tr').show();
			}
			$('#myCollectionEditDiv').show();
			$('#myNormalEditDiv').hide();
			$('#myConditionDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myURLActionDiv').hide();
			$('#myTransferDiv').hide();
		}
		if (nodeData.category == "DTMFPress") {
			$('#dTMFPressform').form('clear');
			$('#dTMFPressform').form('load',nodeData);
			if(nodeData.presstype == 0) {
				$("input:radio[name='pressType']").eq(0).attr("checked",'checked');
				$("#gather_numbers").fadeOut('slow');
	            $("#press_numbers").fadeIn('slow');
			} 
			if(nodeData.presstype == 1) {
				$("input:radio[name='pressType']").eq(1).attr("checked",'checked');
				$("#gather_numbers").fadeIn('slow');
	            $("#press_numbers").fadeOut('slow');
			} 
			$("#attemptLimit").combobox({
			    width:240,
			    valueField:'value',
			    textField:'text',
			    data: [{'text': '1次', 'value': '1'},{'text': '2次', 'value': '2'}],
			    onLoadSuccess: function(){
			        $(this).combobox('setValue',nodeData.attemptLimit);
			    } 
			});
			$("#endPressNumber").combobox({
			    width:240,
			    valueField:'value',
			    textField:'text',
			    data: [{'text': '按*号键', 'value': '*'},{'text': '按#号键', 'value': '#'}],
			    onLoadSuccess: function(){
			        $(this).combobox('setValue',nodeData.endPressNumber);
			    } 
			});
			$('#myDTMFPressDiv').show();
			$('#myNormalEditDiv').hide();
			$('#myConditionDiv').hide();
			$('#myCollectionEditDiv').hide();
			$('#myURLActionDiv').hide();
			$('#myTransferDiv').hide();
			$.parser.parse();
		}
		if (nodeData.category == "Condition") {
			$('#Conditionform').form('clear');
			$('#Conditionform').form('load',nodeData);
			var bottomArray = nodeData.bottomArray;
			// 节点数据
			if(nodeData.conditions == "" || nodeData.conditions == null || nodeData.conditions == undefined) {
				nodeData.conditions = [];
			}
			// 条件个数
			var conditionCount = nodeData.conditions.length; 
			// 清空条件
			$('#myConditionDiv').find("#conditions").find("div").eq(0).empty();
			// 设置组件名称
			$("#conditionNodeName").textbox('setValue', nodeData.conditionNodeName);
			// 设置条件内容
			var conditions = nodeData.conditions;
			for(var i = 0; i < conditions.length; i++) {
				var andConditions = [];
				if(conditions[i].andConditions == null || conditions[i].andConditions == undefined) {
					andConditions = conditions[i];
				} else {
					andConditions = conditions[i].andConditions;
				}
				addOneCondition(i,0);
				$("#condition_name"+"_cdt" +i).textbox('setValue', bottomArray[i].text);
				for(var j = 0; j < andConditions.length; j++) {
					if(j > 0) {
						$("#andBtn" + "_cdt"+i).trigger("click");
					}
					var andCondition = andConditions[j];
					var variableType = andCondition.variableType;
					$("#variable_type_cdt"+i+"_and"+j).combobox('setValue', andCondition.variableType);
					if(variableType == "attr") {
						$("#param_name_attr_cdt"+i+"_and"+j).next().show();
						$("#param_name_element_cdt"+i+"_and"+j).next().hide();
						$("#param_name_other_cdt"+i+"_and"+j).next().hide();
						$("#element_value_cdt"+i+"_and"+j).next().hide();
						$("#element_value_insert_btn_cdt"+i+"_and"+j).hide();
						$("#param_value_cdt"+i+"_and"+j).next().show();
						$("#interface_name_cdt"+i+"_and"+j).next().hide();
						$("#interface_param_cdt"+i+"_and"+j).next().hide();
						$("#param_name_attr_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramName);
						$("#param_value_cdt"+i+"_and"+j).textbox('setValue', andCondition.paramValue);
					} 
					if(variableType == "element") {
						$("#param_name_attr_cdt"+i+"_and"+j).next().hide();
						$("#param_name_element_cdt"+i+"_and"+j).next().show();
						$("#param_name_other_cdt"+i+"_and"+j).next().hide();
						$("#element_value_cdt"+i+"_and"+j).next().show();
						$("#element_value_insert_btn_cdt"+i+"_and"+j).hide();
						$("#param_value_cdt"+i+"_and"+j).next().hide();
						$("#interface_name_cdt"+i+"_and"+j).next().hide();
						$("#interface_param_cdt"+i+"_and"+j).next().hide();
						$("#param_name_element_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramName);
						$("#element_value_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramValue);
					}
					if(variableType == "interface") {
						$("#param_name_attr_cdt"+i+"_and"+j).next().hide();
						$("#param_name_element_cdt"+i+"_and"+j).next().hide();
						$("#param_name_other_cdt"+i+"_and"+j).next().hide();
						$("#element_value_cdt"+i+"_and"+j).next().hide();
						$("#element_value_insert_btn_cdt"+i+"_and"+j).hide();
						$("#param_value_cdt"+i+"_and"+j).next().show();
						$("#interface_name_cdt"+i+"_and"+j).next().show();
						$("#interface_param_cdt"+i+"_and"+j).next().show();
						$("#interface_name_cdt"+i+"_and"+j).combobox('setValue', andCondition.interfaceName);
						$("#interface_param_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramName);
						$("#param_value_cdt"+i+"_and"+j).textbox('setValue', andCondition.paramValue);
					}
					if(variableType == "other") {
						$("#param_name_attr_cdt"+i+"_and"+j).next().hide();
						$("#param_name_element_cdt"+i+"_and"+j).next().hide();
						$("#param_name_other_cdt"+i+"_and"+j).next().show();
						$("#element_value_cdt"+i+"_and"+j).next().hide();
						$("#element_value_insert_btn_cdt"+i+"_and"+j).hide();
						$("#param_value_cdt"+i+"_and"+j).next().show();
						$("#interface_name_cdt"+i+"_and"+j).next().hide();
						$("#interface_param_cdt"+i+"_and"+j).next().hide();
						$("#param_name_other_cdt"+i+"_and"+j).textbox('setValue', andCondition.paramName);
						$("#param_value_cdt"+i+"_and"+j).textbox('setValue', andCondition.paramValue);
					}
					$("#param_relation_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramRelation);
					$("#param_type_cdt"+i+"_and"+j).combobox('setValue', andCondition.paramType);
				}
			}
			$('#myNormalEditDiv').hide();
			$('#myConditionDiv').show();
			$('#myCollectionEditDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myTransferDiv').hide();
			$('#myURLActionDiv').hide();
		}
		if (nodeData.category == "URLAction") {
			$('#URLActionform').form('clear');
			$('#URLActionform').form('load',nodeData);
			$('#in_key_vals_div').find(".in-key").remove();
			$('#out_key_vals_div').find(".out-key").remove();
			// 入参
			if(nodeData.inParams == "" || nodeData.inParams == null || nodeData.inParams == undefined) {
				nodeData.inParams = [];
			}
			// 出参
			if(nodeData.outParams == "" || nodeData.outParams == null || nodeData.outParams == undefined) {
				nodeData.outParams = [];
			}
			$('#myURLActionDiv').show();
			$('#myNormalEditDiv').hide();
			$('#myConditionDiv').hide();
			$('#myCollectionEditDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myTransferDiv').hide();
		}
		if (nodeData.category == "Transfer") {
			$('#Transferform').form('clear');
			$('#Transferform').form('load',nodeData);
			$('#myTransferDiv').show();
			$('#myNormalEditDiv').hide();
			$('#myConditionDiv').hide();
			$('#myCollectionEditDiv').hide();
			$('#myDTMFPressDiv').hide();
			$('#myURLActionDiv').hide();
		}
	});
}

// 添加回复内容
function addToTTSResponse(t){
	var action = '';
	var actionParams = '';
	var otherResponses = [];
	for ( var i = 1; i <= MAX_SCENARIO_COUNT; i++) {
		var otherResponseName = '';
		var otherResponseValue = '';
		var otherResponse = new Object();
		if ($("#otherResponse0name" + i).html() == "" ){
			continue;
		}
		if ($("#otherResponse0name" + i).html().indexOf(":") > -1){
			otherResponseValue = undefined != $("#otherResponse0value" + i).combobox('getValue') ? 
					$("#otherResponse0value" + i).combobox('getValue') : $("#otherResponse0value" + i).textbox('getText');
			if ('' != $("#otherResponse0value" + i).combobox('getText') && (undefined  == condition || '' == condition)){
				$.messager.alert('提示', "您添加的" + $("#otherResponse0name" + i).html().replace(":","") + "不在列中，请联系管理员添加后才能使用", "warning");
				return;
			}
		}
		otherResponseName = $("#otherResponse0name" + i).html().replace(":","").replace("：","");
		otherResponse.otherResponseName = otherResponseName;
		otherResponse.otherResponseValue = otherResponseValue;
		otherResponses.push(otherResponse);
	}
	if($('#sms-checkbox').is(':checked')) {
		// 发送短信
		actionParams = $("#templateId").combobox('getValue')+"|";
		if($('#sms-varibales-span').find("input").length > 0) {
			$('#sms-varibales-span').find("input").each(function() {
				actionParams += $(this).attr("id")+'='+$(this).val()+'&';
			});
			actionParams = actionParams.substring(0, actionParams.length-1);
		}
		action = 'SMS';
	} 	
    t.action = action;
    t.actionParams = actionParams;
    t.otherResponses = otherResponses;
	// 设置TTS节点
    setTTSResponseNode(t);
	// 关闭回复表单
	$('#closeRule0').trigger("click");
}

// 设置TTS节点
function setTTSResponseNode(t) {
	var nodeData = public_thisGraphObj.part.data;
	$.each(t,function(index,value){
    	myDiagram.model.setDataProperty(nodeData, index, value);
    });
	if (nodeData.category == "Normal") {
		if(t.ttsNodeType == '0') {
			myDiagram.model.setDataProperty(nodeData, 'bottomArray', t.checkedArray);
		}
		if(t.ttsNodeType == '1') {
			myDiagram.model.setDataProperty(nodeData, 'bottomArray', [{'text':'跳转'}]);
		}
	}
	if(t.interactiveType == '词模匹配') {
		myDiagram.model.setDataProperty(nodeData, 'text', t.wordsContent);
	}
	if(t.interactiveType == '键值补全') {
		var menuItemText = ''; 
		if(t.menuOptions != undefined && t.menuOptions != '') {
			var menuItems = t.menuOptions.split('|');
			if(t.menuOptions.split('|').length > 0) {
				for(var i=0;i<menuItems.length;i++) {
					menuItemText += '['+(i+1)+']'+menuItems[i]+'\n';
				}
			} 
		}
		myDiagram.model.setDataProperty(nodeData, 'text', t.menuStartWords+':'+'\n'+menuItemText);
	}
}

//TTS编辑页面赋值
function setTTSResponseValue() {
	var nodeData = public_thisGraphObj.part.data;
	$('#myNormalEditForm').form('clear');
	$('#myNormalEditForm').form('load',nodeData);
	if(nodeData.ttsNodeType == "0") {
		$("input:radio[name='ttsNodeType']").eq(0).prop("checked",'checked');
        $("#customer-answer-table").show();
	} 
	if(nodeData.ttsNodeType == "1") {
		$("input:radio[name='ttsNodeType']").eq(1).prop("checked",'checked');
        $("#customer-answer-table").hide();
	} 
	if(nodeData.interactiveType == '词模匹配') { // 系统反问
		$('.words').show();
		$('.menus').hide();
	}
	if(nodeData.interactiveType == '键值补全') { // 菜单询问
		$('.words').hide();
		$('.menus').show();
	}
	var otherResponses = nodeData.otherResponses;
	if(otherResponses && otherResponses.length > 0) {
		for(var i=0; i<otherResponses.length; i++) {
			var otherResponse = otherResponses[i];
			for ( var i = 1; i <= MAX_SCENARIO_COUNT; i++) {
				if ($("#otherResponse0name" + i).html() == "" ){
					continue;
				}
				if ($("#otherResponse0name" + i).html().indexOf(":") > -1){
					var otherResponseTrName = $("#otherResponse0name" + i).html().replace(":","").replace("：","");
					if(otherResponseTrName == otherResponse.otherResponseName) {
						if($("#otherResponse0value" + i).combobox('getValue') == undefined) {
							$("#otherResponse0value" + i).textbox('setText', otherResponse.otherResponseValue);
						} else {
							$("#otherResponse0value" + i).combobox('setValue', otherResponse.otherResponseValue);
						}
					}
				}
			}
		}
	}
	if(nodeData.action == 'SMS') {
		$("#sms-checkbox").prop({checked:true}); 
		$('#sms-template-span').show();
		$('#sms-varibales-span').show();
		var actionParams = nodeData.actionParams;
		var templateId = actionParams.split('|')[0];
		$("#templateId").combobox('setValue', templateId);
		var varibales = actionParams.split('|')[1].split('&');
		if(varibales.length > 0) {
			for(var i=0; i<varibales.length; i++) {
				var varibaleName = varibales[i].split('=')[0];
				var varibaleValue = varibales[i].split('=')[1];
				$('#sms-varibales-span').find("input[name="+varibaleName+"]").val(varibaleValue);
			}
		}
	} else {
		$("#sms-checkbox").prop({checked:false}); 
		$('#templateId').combobox('setValue', '');
		$('#sms-varibales-span').empty();
		$('#sms-template-span').hide();
		$('#sms-varibales-span').hide();
	}
	var bottomArray = nodeData.bottomArray;
	if(nodeData.ttsNodeType == "0" && bottomArray.length > 0) {
		$("#customerAnswerSpan").find("input:checkbox").prop({checked:false});
		for(var i=0;i<bottomArray.length;i++) {
			$("#customerAnswerSpan").find("input:checkbox[value='"+bottomArray[i].text+"']").prop({checked:true}); 
		}
	}
}

// 鼠标点击事件	
function buttonClick() {
	
	// 添加用户答案页面
	$('#customerAnswerAdd-toPageBtn').click(function() {
		$('#customerAnswerAdd-wordclasses').val('');
		$('#customerAnswerAddPage').window('open');
		
	});
	
	// 关闭用户答案页面
	$('#customerAnswerAdd-closeBtn').click(function() {
		$('#customerAnswerAddPage').window('close');
		
	});
	
	// 生成词模
	$('#customerAnswerAdd-analyzeBtn').click(function() {
		var customerAnswerKeywords = $.trim($("#customerAnswerAdd-keywords").textbox('getValue'));
		addWordPatternFlag = 'customerAnswer';
		autoGenerateWordPattern(customerAnswerKeywords);
	});
	
	// 保存用户答案
	$('#customerAnswerAdd-saveBtn').click(function() {
		saveCustomerAnswer();
	});
	
	// 保存自学习词模
	$('#autoworrdpat-saveBtn').click(function() {
		addAutoWordpat();
		if(addWordPatternFlag == 'customerAnswer') {
			$('#customerAnswerAdd-wordclasses').val(autoWordPattern);
		}
		if(addWordPatternFlag == 'collectionIntention') {
			var collectionType = $('#collectionType').combobox('getValue');
			if(collectionType == 'elementCollection') {
				var elementName = $('#collectionElement').combobox('getValue');
				$('#collectionIntentionAdd-wordclasses').val(autoWordPattern+"&"+elementName+"=");
			} 
			if(collectionType == 'userInfoCollection') {
				$('#collectionIntentionAdd-wordclasses').val(autoWordPattern);
			}
		}
	});
	
	// 关闭自学习词模页面
	$('#autoworrdpat-closeBtn').click(function() {
		$('#autoworrdpat').window('close');
	});
	
	// 保存流程图
	$('#saveBtSubmit').click(function() {
		savaAndSubmitNoVerification();
	});

	// 保存回复内容
	$('#saveRule0').click(function() {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#myNormalEditForm').serializeArray();
		var t={};
		$.each(array,function(index, value){
			t[this.name] = this.value;
		});
		if(t.interactiveType == '') {
			$.messager.alert('提示', "请填写交互类型", "info");
			return;
		}
		if(t.endFlag == '') {
			$.messager.alert('提示', "请选择是否为末梢节点", "info");
			return;
		}
		if(t.interactiveType == '词模匹配' && t.wordsContent == '') {
			$.messager.alert('提示', "请填写话术文字", "info");
			return;
		}
		if(t.interactiveType == '键值补全' && t.menuStartWords == '') {
			$.messager.alert('提示', "请填写开始话语", "info");
			return;
		}
		if(t.interactiveType == '键值补全' && t.menuOptions == '') {
			$.messager.alert('提示', "请填写菜单选项", "info");
			return;
		}
		if(nodeData.category == "Normal" && t.ttsNodeType == undefined || t.ttsNodeType == '') {
			$.messager.alert('提示', "请选择【意图节点】或【跳转节点】", "info");
			return;
		}
		// 获取已选的用户回答
	    var checkedArray = [];   
	    $('[name=customerAnswer]:checkbox:checked').each(function() {
	    	checkedArray.push({'text':$(this).val()});
	    });
	    if(nodeData.category == "Normal" && t.ttsNodeType == "0" && checkedArray.length == 0) {
			$.messager.alert('提示', "请选择用户回答", "info");
			return;
		}
	    t.checkedArray = checkedArray;
		addToTTSResponse(t);
	});
	
	// 关闭回复内容
	$('#closeRule0').click(function() {
		$('#myNormalEditDiv').hide();
	});
	
	// 保存结束语
	$('#saveConclusion').click(function() {
		var nodeData = public_thisGraphObj.part.data;
		var conclusion = $("#conclusion").textbox('getValue');
		myDiagram.model.setDataProperty(nodeData, 'text', conclusion);
	});
	
	// 关闭结束语
	$('#closeConclusion').click(function() {
		$('#myEndEditDiv').hide();
	});
	
	// 流程图撤销操作
	$('#undoBtSubmit').click(function() {
		myDiagram.model.undoManager.undo();
	});

	// 打开其他回复内容
	$('#otherResponseShowBtn').click(function() {
		$('#otherResponseHideBtn').show();
		$('#otherResponseShowBtn').hide();
		$('#other-response-table').show();
	});
	
	// 收起其他回复内容
	$('#otherResponseHideBtn').click(function() {
		$('#otherResponseHideBtn').hide();
		$('#otherResponseShowBtn').show();
		$('#other-response-table').hide();
	});
	
	// 发送短信复选框点击事件
	$('#sms-checkbox').click(function() {
		if($('#sms-checkbox').is(':checked')) {
			$('#sms-template-span').show();
			$('#sms-varibales-span').show();
		} else {
			$('#sms-template-span').hide();
			$('#sms-varibales-span').hide();
		}
	});
	
	// 保存信息收集
	$('#saveCollection').click(function() {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#collectionform').serializeArray();
		var t={};
		$.each(array,function(index, value){
			t[this.name] = this.value;
		});
		if(t.collectionName == '') {
			$.messager.alert('提示', "请填写信息名称", "info");
			return;
		}
		if(t.collectionType == '') {
			$.messager.alert('提示', "请选择采集类型", "info");
			return;
		}
		if(t.collectionIntention == '') {
			$.messager.alert('提示', "请选择关联意图", "info");
			return;
		}
		if(t.interactiveType == '') {
			$.messager.alert('提示', "请选择交互类型", "info");
			return;
		}
		if(t.collectionType == 'elementCollection' && t.collectionElement == '') {
			$.messager.alert('提示', "请选择关联要素", "info");
			return;
		}
		if(t.interactiveType == '词模匹配' && t.collectionWords == '') {
			$.messager.alert('提示', "请填写反问话术", "info");
			return;
		}
		if(t.interactiveType == '键值补全' && t.menuStartWords == '') {
			$.messager.alert('提示', "请填写开始话语", "info");
			return;
		}
		if(t.interactiveType == '键值补全' && t.menuOptions == '') {
			$.messager.alert('提示', "请填写菜单选项", "info");
			return;
		}
		if(t.interactiveType == '键值补全' && t.menuEndWords == '') {
			//$.messager.alert('提示', "请填写结束话语", "info");
			//return;
		}
		var collectionText = "";
		if(t.interactiveType == '词模匹配') {
			collectionText += "询问文本：" + t.collectionWords + "\n";
			collectionText += "参数名称：" + t.collectionParam + "\n";
		}
		if(t.interactiveType == '键值补全') {
			collectionText += "询问文本：" + t.menuStartWords + "\n";
			collectionText += "参数名称：" + t.collectionParam + "\n";
			collectionText += "菜单选项：" + t.menuOptions + "\n";
		}
		if(t.collectionType == 'elementCollection') {
			collectionText += "关联要素：" + t.collectionElement + "\n";
		}
		collectionText += "关联意图：" + t.collectionIntention + "\n";
		myDiagram.model.setDataProperty(nodeData, 'collectionText', collectionText);
		$.each(t,function(index,value){
	    	myDiagram.model.setDataProperty(nodeData, index, value);
	    });
		$('#myCollectionEditDiv').hide();
	});
	
	// 关闭信息收集
	$('#closeCollection').click(function() {
		$('#myCollectionEditDiv').hide();
	});
	
	// DTMF按键收集按钮
	$("#saveDTMFPress").bind("click",function () {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#dTMFPressform').serializeArray();
		var t={};
		$.each(array,function(index, value){
			if(this.name == "pressType" || this.name=="pressTimeOut"
				|| this.name=="maxLength" || this.name=="minLength") {
				t[this.name] = parseInt(this.value);
				return true;
			}
		});
		$.each(array,function(index, value){
			t[this.name] = this.value;
		});
		if(t.dtmfName == '') {
			$.messager.alert('提示', "请填写DTMF名称", "info");
			return;
		}
		if(t.dtmfAlias == '') {
			$.messager.alert('提示', "请填写DTMF别名", "info");
			return;
		}
		if(t.dtmfAnswer == '') {
			$.messager.alert('提示', "请填写DTMF话术", "info");
			return;
		}
		if(isNaN(t.pressType)) {
			$.messager.alert('提示', "请选择DTMF设置", "info");
			return;
		}
		if(t.attemptLimit == '') {
			$.messager.alert('提示', "请选择尝试次数", "info");
			return;
		}
		if(t.pressTimeOut == '') {
			$.messager.alert('提示', "请填写超时时间", "info");
			return;
		}
		if(t.pressTimeOutAnswer == '') {
			$.messager.alert('提示', "请填写超时话术", "info");
			return;
		}
		if(!isEnglish(t.dtmfAlias)) {
			$.messager.alert('提示', "DTMF别名请填写英文", "info");
			return;
		}
		if(isNaN(t.pressTimeOut)) {
			$.messager.alert("提示","超时时间，格式不合法","info");
			return;
		}
		if(t.pressType==0 && t.pressNumbers == '') {
			$.messager.alert("提示","请输入按键值","info");
			return;
		}
		if(t.pressType==0 && !checkPressNumber(t.pressNumbers)) {
			$.messager.alert("提示","按键值不合法","info");
			return;
		}
		if(t.pressType==1 && (isNaN(t.maxLength) || isNaN(t.minLength))) {
			$.messager.alert("提示","最短，最大长度，格式不合法","info");
			return;
		}
		if(t.pressType==1 && t.endPressNumber == '') {
			$.messager.alert("提示","请选择结束按键","info");
			return;
		}
		if(t.pressType == 0) {
			t.maxLength = 1;
			t.minLength = 1;
			t.endPressNumber = "";
		}
        $.each(t,function(index,value){
        	myDiagram.model.setDataProperty(nodeData, index, value);
        });
        myDiagram.model.setDataProperty(nodeData, "text", t.dtmfName);
        $("#closeDTMFPress").trigger("click");
    })
    $("#closeDTMFPress").bind("click",function () {
        $('#myDTMFPressDiv').hide();
	});
	
	$("#saveURLAction").bind("click", function () {		
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#URLActionform').serializeArray();
		var t={};
		$.each(array,function(index, value){
			t[this.name] = this.value;
		});
		$.each(t,function(index,value){
        	myDiagram.model.setDataProperty(nodeData, index, value);
        });
		// 校验格式
		if(t.actionName == '') {
			$.messager.alert("提示","请填写组件名称","info");
			return;
		}
		if(t.interfaceName == '') {
			$.messager.alert("提示","请选择调用接口","info");
			return;
		}
		myDiagram.model.setDataProperty(nodeData, "text", t.actionName);
		$('#myURLActionDiv').hide();
	});
	
	$("#saveTransfer").bind("click", function () {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#Transferform').serializeArray();
		$.each(array,function(index, value){
			myDiagram.model.setDataProperty(nodeData, this.name, this.value);
		});
		if(nodeData.transferName == '') {
			$.messager.alert("提示","请填写转人工名称","info");
			return;
		}
		if(nodeData.transferNumber == '') {
			$.messager.alert("提示","请填写转人工号码","info");
			return;
		}
		if(!checkPhoneNumber(nodeData.transferNumber) && !checkTelePhone(nodeData.transferNumber)) {
			$.messager.alert("提示","转人工号码格式不正确","info");
			return;
		}
		myDiagram.model.setDataProperty(nodeData, "text", nodeData.transferName);
		$('#myTransferDiv').hide();
	});

	$("#saveCondition").bind("click",function () {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#Conditionform').serializeArray();
		var conditions = [];
		var bottomArray = [];
		var conditionDivs = $("#conditions").find("div").eq(0).find('.condition-div');
		var conditionCount = conditionDivs.length;
		for(var conditionIdx=0; conditionIdx < conditionCount; conditionIdx++) {
			var andConditionDivs = $("#conditions").find("div").eq(0).find('.condition-div').eq(conditionIdx).find("div[add='andconditions']").find('.and-condition-div');
			var andConditionCount = andConditionDivs.length;
			var conditionInfo = {};
			var andConditions = [];
			var conditionName = $('#condition_name'+'_cdt'+conditionIdx).textbox('getValue');
			bottomArray[conditionIdx] = {'text':conditionName};
			for(var variableIdx=0; variableIdx < andConditionCount; variableIdx++) {
				var andCondition = {};
				var variableTypeInput = "variable_type" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var paramNameAttrInput = "param_name_attr" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var paramNameElementInput = "param_name_element" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var paramNameOtherInput = "param_name_other" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var paramRelationInput = "param_relation" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var paramTypeInput = "param_type" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var elementValueInput = "element_value" + "_cdt"+conditionIdx + "_and"+variableIdx; 
			    var elementValueInsertBtn = "element_value_insert_btn" + "_cdt"+conditionIdx + "_and"+variableIdx; 
			    var paramValueInput = "param_value" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var isUpDownInput = "isUpDown" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var upDownTypeInput = "upDownType" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var updownRatioInput = "updownRatio" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var interfaceNameInput = "interface_name" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var interfaceParamInput = "interface_param" + "_cdt"+conditionIdx + "_and"+variableIdx;
			    var variableType = $('#'+variableTypeInput).combobox('getValue');
				if(variableType == "attr") {
					andCondition.paramName = $("#"+paramNameAttrInput).combobox("getValue");
					andCondition.paramValue = $("#"+paramValueInput).textbox("getValue");
				} 
				if(variableType == "element") {
					andCondition.paramName = $("#"+paramNameElementInput).combobox("getValue");
					andCondition.paramValue = $("#"+elementValueInput).combobox("getValue");
				} 
				if(variableType == "interface") {
					andCondition.interfaceName = $("#"+interfaceNameInput).combobox("getValue");
					andCondition.paramName = $("#"+interfaceParamInput).combobox("getValue");
					andCondition.paramValue = $("#"+paramValueInput).textbox("getValue");
				} 
				if(variableType == "other") {
					andCondition.paramName = $("#"+paramNameOtherInput).textbox("getValue");
					andCondition.paramValue = $("#"+paramValueInput).textbox("getValue");
				} 
				andCondition.variableType = variableType;
				andCondition.paramRelation = $("#"+paramRelationInput).combobox("getValue");
				andCondition.paramType = $("#"+paramTypeInput).combobox("getValue");
				andConditions[variableIdx] = andCondition;
			}
			conditionInfo.conditionName = conditionName;
			conditionInfo.andConditions = andConditions;
			conditions[conditionIdx] = conditionInfo;
		}
		$.each(array,function(index, value){
			myDiagram.model.setDataProperty(nodeData, this.name, this.value);
		});
		if(nodeData.conditionNodeName == '') {
			$.messager.alert("提示","请填写组件名称","info");
			return;
		}
		if(bottomArray.length == 0) {
			$.messager.alert("提示","请填写条件","info");
			return;
		}
		myDiagram.model.setDataProperty(nodeData, "text", nodeData.conditionNodeName);
		myDiagram.model.setDataProperty(nodeData, "conditionNodeName", nodeData.conditionNodeName);
		myDiagram.model.setDataProperty(nodeData, "conditions", conditions);
		myDiagram.model.setDataProperty(nodeData, "bottomArray", bottomArray);
		$('#myConditionDiv').hide();
	});
	
    $("#closeURLAction").bind("click",function () {
        $('#myURLActionDiv').hide();
	});
    $("#closeTransfer").bind("click",function () {
        $('#myTransferDiv').hide();
	});
    $("#closeCondition").bind("click",function () {
        $('#myConditionDiv').hide();
	});	
}

// 显示为赋值隐藏的链接
function showLinkLabel(e) {
	var label = e.subject.findObject("LABEL");
	if (label !== null)
		label.visible = (e.subject.fromNode.data.category === "Judge");
}
// 显示已赋值隐藏的链接
function showLinkLabel1(subject) {
	var label = subject.part.findObject("LABEL");
	if (label !== null)
		label.visible = (subject.part.fromNode.data.category === "Judge");
	label = subject.part.findObject("LABELForWarning");
	label.visible = false;
}

// 保存和提交数据（提交之前验证数据格式）
function savaAndSubmit() {
	var modelAsText = myDiagram.model.toJson();
	// 验证数据
	var iscorrectdata = VerificationData();
	if (iscorrectdata) {
		if (window.confirm('你确定要提交已修改的数据吗？')) {
			commitRevisedData(modelAsText);
			return true;
		} else {
			return false;
		}
	} else {
		$.messager.alert('警告', '数据有问题，请重新编辑！！！', "error");
		return false;
	}
}

//保存和提交数据
function savaAndSubmitNoVerification() {
	var modelAsText = myDiagram.model.toJson();
	// 验证数据
	var iscorrectdata = VerificationData();
	if (iscorrectdata) {
		$.messager.confirm('提示', '你确定要提交已修改的数据吗?', function(r) {
			if (r) {
				commitRevisedData(modelAsText);
				return true;
			} else {
				return false;
			}
		});
	}
}

// 遍历图验证数据不对标黄
function VerificationData() {
	var modeldata = myDiagram.model;
	var nodeDataArray = modeldata.nodeDataArray;
	var linkDataArray = modeldata.linkDataArray;
	var isPassVerification = true;
	if(nodeDataArray.length==0) {
		$.messager.alert('提示', '请添加组件', "info");
		return false;
	}
	if(linkDataArray.length==0) {
		$.messager.alert('提示', '请添加连线', "info");
		return false;
	}
	if (isPassVerification) {
		return true;
	}
	return false;
}

// 提交已修改的数据
function commitRevisedData(jsonStr) {
	console.log(jsonStr);
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			scenariosName : publicscenariosname,
			type : "saveConfig",
			sceneJson : jsonStr,
			sceneType: sceneType
		},
		success : function(data) {
			$.messager.alert('系统提示', data.msg, "info");
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 文本样式
function textStyle() {
	return {
		font : "bold 11pt Helvetica, Arial, sans-serif",
		stroke : "whitesmoke"
	}
}

// 节点样式
function nodeStyle() {
	return [
		new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
        {
			locationSpot : go.Spot.Center,
            selectable: true,
            isShadowed: true,
            shadowBlur: 10,
            shadowOffset: new go.Point(1, 1)
        }];
}

// 判断开始或结束节点的唯一性
function onlyNode(myDiagram, name, msg) {
	var nodeArray = myDiagram.model.nodeDataArray;
	var arr = [];
	$.each(nodeArray, function(index, element) {
		if (element.category == name) {
			arr.push(this.element);
		}
	});
	if (arr.length > 1) {
		myDiagram.commandHandler.deleteSelection();
		$.messager.alert('系统提示', msg, "info");
	}
}

// 地址栏参数
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

// 隐藏浏览器自带的鼠标右键菜单
function doNothing() {
	window.event.returnValue = false;
	return false;
}

// 替换字符串中所有空格
function replaceSpace(str) {
	if (str != null && str != '') {
		str = str.replace(new RegExp(' ', 'g'), '');
	}
	return str;
}

function autoGenerateWordPattern(wordPattern) {
	if (wordPattern == '' || wordPattern == null) {
		$.messager.alert('提示', "请在生成词模的输入框填写内容！", "warning");
		return;
	}
	
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			autoWordPattern : wordPattern,
			type : "autoWordPattern",
		},
		success : function(data) {
			var resultStr = data.result;
			if (resultStr == "无") {
                $.messager.alert('系统提示', "生成失败！", "warning");
            } else if (resultStr.length > 0 && startWith('ERROR', resultStr)) {
            	$.messager.alert('系统提示', resultStr, "warning");
            } else {
                openWin(resultStr, data.oovWord);
            }
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

function openWin(resultStr, errorStr) {
	var tableHtml = '<table style="width:100%;">';
	var next_abs_namessarry = resultStr.split('$_$');
	var start = 0;
    for(var i = 0;i < next_abs_namessarry.length; i++) {
        var w  = next_abs_namessarry[i].split('@_@');
        // 过滤nlp版本
        if (w != null && w.length > 0 && startWith('OOV', w)) {
            start++;
            continue;
        }
        tableHtml += '<tr><td><input type=\"radio\" name=\"autoworrdpat-radio\" ';
        if (i == start) {
            tableHtml += ' checked=\"checked\" ';
        }
        tableHtml += '/><span style=\"display:none;\" >' + w[1] + ' </span><label>' + w[0] + '</label></td></tr>';
    }
    tableHtml += '</table>';
    $('#autoworrdpat-nextabs-div').html(tableHtml);
    $("#autoworrdpat-targetedquery").textbox("setValue", $("#customerAnswerAdd-keywords").textbox('getValue'));
    if (errorStr != null && errorStr != '') {
        var errorStrArr = errorStr.split('$_$');
        var word = [];
        for (var i = 0; i < errorStrArr.length; i++) {
        	word.push(errorStrArr[i].replace('(OOV)',''));
		}
        var errorHtml = '<b>系统提示：</b>分词中（' + word.join('，') + '）的近类或父类不存在,需要添加对应的词类；如已存在，则需要在词模体中手动修改。';
        $("#autoworrdpat-errorInfo").html(errorHtml);
    }
	$('#autoworrdpat').window('open');
}

function addAutoWordpat() {
	var targetedquery = $.trim($("#autoworrdpat-targetedquery").textbox('getValue'));
    var matchcount = $.trim($("#autoworrdpat-matchcount").textbox('getValue'));
    if (targetedquery == '') {
        $.messager.alert('提示', "请填写针对问题！", "warning");
        return;
    }
    var rvalue;
    if (matchcount != "" && matchcount != null) {
        rvalue = '&针对问题="' + targetedquery + '"&最大未匹配字数="' + matchcount + '"';
    } else {
        rvalue = '&针对问题="' + targetedquery + '"';
    }
    var inputValue = $("#autoworrdpat-nextabs-div input[name='autoworrdpat-radio']:checked").next("span").text();
    autoWordPattern = inputValue.replace(/\s*/g,"") + rvalue;
    // 关闭自学习词模页面
	$('#autoworrdpat').window('close');
}

//判断字符串以str开始
function startWith(str, value) {
    var reg = new RegExp("^" + str);
    return reg.test(value);
}

//判断字符串以str结尾
function endWith(str, value) {    
	var reg = new RegExp(str + "$");    
	return reg.test(value);       
}

// 保存用户答案
function saveCustomerAnswer() {
	var customeranswer = $.trim($("#customerAnswerAdd-customerAnswer").textbox('getValue'));
	if (customeranswer == '' || customeranswer == null) {
		$.messager.alert('提示', "请在标题的输入框填写内容！", "warning");
		return;
	}
	var simpleWordPattern = $("#customerAnswerAdd-wordclasses").val().replace(new RegExp(' ', 'g'), '');
	if (simpleWordPattern === "") {
        $.messager.alert('提示', "词模不能为空！", "warning");
        return;
    } else if (simpleWordPattern.split('#')[0].indexOf("~") != -1) {
        $.messager.alert('提示', "词模中有非法字符 ~ 存在", "warning");
        return;
    } else if (simpleWordPattern.split('#')[0].indexOf("+") != -1) {
        $.messager.alert('提示', "词模中有非法字符 + 存在", "warning");
        return;
    }
	
	var wordPatternTypeName = $("#customerAnswerAdd-wordtype").numberbox('getText');
	if (wordPatternTypeName != "等于词模" && wordPatternTypeName == "普通词模") {
        if (simpleWordPattern.indexOf("类-") != -1 && simpleWordPattern.indexOf("类*") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        }
    }

    if (wordPatternTypeName == "等于词模") {
        if (simpleWordPattern.indexOf("类-") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        } else {
            if (simpleWordPattern.indexOf("类*") != -1) {
                $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
                return;
            }
        }
    }
    
    // 排除词模，如果只有一个必选项，并且该必选项是子句时，不允许添加
    if (wordPatternTypeName == "排除词模" && simpleWordPattern.indexOf("子句") != -1) {
        if (simpleWordPattern.indexOf("*") == -1 && simpleWordPattern.indexOf("-") == -1) {
            $.messager.alert('提示', "系统不支持单子句排除词模，建议附加另一个不带\"[]\"的近类或父类", "warning");
            return;
        }
    }

    if (simpleWordPattern.indexOf('(OOV)') != -1) {
        $.messager.alert('提示', "（OOV）为系统未识别词类，需新增对应词词类!", "warning");
        return;
    }

    if (simpleWordPattern.indexOf("@") != -1) {
        $.messager.alert('提示', "存在非法字符 '@'", "warning");
        return;
    }
    if (simpleWordPattern.indexOf("#无序#") == -1 && simpleWordPattern.indexOf("#有序#") == -1) {
        $.messager.alert('提示', "请输入正确格式序列：'#无序#' 或 '#有序#'", "warning");
        return;
    }

    var wordpattype = 0;
    if (wordPatternTypeName == "等于词模") {
        wordpattype = "1";
    } else if (wordPatternTypeName == "排除词模") {
        wordpattype = "2";
    } else if (wordPatternTypeName =="选择词模") {
        wordpattype = "3";
    } else if (wordPatternTypeName == "特征词模") {
        wordpattype = "4";
    } else if (wordPatternTypeName == "自学习词模") {
        wordpattype = "5";
    } else {
        wordpattype = "0";
    }
    
    simpleWordPattern = simpleWordPattern.replace(new RegExp("\r\n",'g'),"\n");
    simpleWordPattern = simpleWordPattern.replace(/^\n+|\n+$/g,"");
    var temp = simpleWordPattern.split('\n');

    temp = quchong(temp);
    simpleWordPattern = "";
    for (var i = 0; i < temp.length; i++) {
        if (temp[i] != '' && temp[i] != '\n') {
            if (endWith('&', temp[i])) {
                $.messager.alert('提示', "词模语法有误,勿以&结尾!", "warning");
                return;
            }

            if (wordPatternTypeName == "选择词模") {
                simpleWordPattern += "++*" + temp[i] + '\n';
            } else if (wordPatternTypeName == "排除词模") {
                simpleWordPattern += "~*" + temp[i] + '\n';
            } else if (wordPatternTypeName == "特征词模") {
                simpleWordPattern += "+*" + temp[i] + '\n';
            } else {
                simpleWordPattern += temp[i] + '\n';
            }
        }
    }

    simpleWordPattern = simpleWordPattern.substr(0, simpleWordPattern.length - 1).replace(/[\[\s+]\]/g,']');
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			customeranswer : customeranswer,
			wordpattype : wordpattype,
			simpleWordPattern : simpleWordPattern+"&用户回答="+customeranswer,
			type : "saveCustomerAnswer",
		},
		success : function(data) {
        	$.messager.alert('系统提示', data.checkInfo, "warning");
        	if (data.checkInfo == '插入成功!') {
	        	// 刷新用户答案复选框
	    		initCustomerAnswer();
	    		// 关闭用户答案页面
	    		$('#customerAnswerAddPage').window('close');
        	}
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

// 保存关联意图
function saveCollectionIntention() {
	var collectionIntention = $.trim($("#collectionIntentionAdd-title").textbox('getValue'));
	if (collectionIntention == '' || collectionIntention == null) {
		$.messager.alert('提示', "请在标题的输入框填写内容！", "warning");
		return;
	}
	var simpleWordPattern = $("#collectionIntentionAdd-wordclasses").val().replace(new RegExp(' ', 'g'), '');
	if (simpleWordPattern === "") {
        $.messager.alert('提示', "词模不能为空！", "warning");
        return;
    } else if (simpleWordPattern.split('#')[0].indexOf("~") != -1) {
        $.messager.alert('提示', "词模中有非法字符 ~ 存在", "warning");
        return;
    } else if (simpleWordPattern.split('#')[0].indexOf("+") != -1) {
        $.messager.alert('提示', "词模中有非法字符 + 存在", "warning");
        return;
    } 
	// 词模返回值必须添加关联要素
	if (!(simpleWordPattern.indexOf(collectionIntention+"=") > -1)) {
        $.messager.alert('提示', "词模返回值必须添加关联要素", "warning");
        return;
    } 
	var wordPatternTypeName = $("#collectionIntentionAdd-wordtype").numberbox('getText');
	if (wordPatternTypeName != "等于词模" && wordPatternTypeName == "普通词模") {
        if (simpleWordPattern.indexOf("类-") != -1 && simpleWordPattern.indexOf("类*") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        }
    }

    if (wordPatternTypeName == "等于词模") {
        if (simpleWordPattern.indexOf("类-") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        } else {
            if (simpleWordPattern.indexOf("类*") != -1) {
                $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
                return;
            }
        }
    }
    
    // 排除词模，如果只有一个必选项，并且该必选项是子句时，不允许添加
    if (wordPatternTypeName == "排除词模" && simpleWordPattern.indexOf("子句") != -1) {
        if (simpleWordPattern.indexOf("*") == -1 && simpleWordPattern.indexOf("-") == -1) {
            $.messager.alert('提示', "系统不支持单子句排除词模，建议附加另一个不带\"[]\"的近类或父类", "warning");
            return;
        }
    }

    if (simpleWordPattern.indexOf('(OOV)') != -1) {
        $.messager.alert('提示', "（OOV）为系统未识别词类，需新增对应词词类!", "warning");
        return;
    }

    if (simpleWordPattern.indexOf("@") != -1) {
        $.messager.alert('提示', "存在非法字符 '@'", "warning");
        return;
    }
    if (simpleWordPattern.indexOf("#无序#") == -1 && simpleWordPattern.indexOf("#有序#") == -1) {
        $.messager.alert('提示', "请输入正确格式序列：'#无序#' 或 '#有序#'", "warning");
        return;
    }

    var wordPatternType = 0;
    if (wordPatternTypeName == "等于词模") {
    	wordPatternType = "1";
    } else if (wordPatternTypeName == "排除词模") {
    	wordPatternType = "2";
    } else if (wordPatternTypeName =="选择词模") {
    	wordPatternType = "3";
    } else if (wordPatternTypeName == "特征词模") {
    	wordPatternType = "4";
    } else if (wordPatternTypeName == "自学习词模") {
    	wordPatternType = "5";
    } else {
    	wordPatternType = "0";
    }
    
    simpleWordPattern = simpleWordPattern.replace(new RegExp("\r\n",'g'),"\n");
    simpleWordPattern = simpleWordPattern.replace(/^\n+|\n+$/g,"");
    var temp = simpleWordPattern.split('\n');

    temp = quchong(temp);
    simpleWordPattern = "";
    for (var i = 0; i < temp.length; i++) {
        if (temp[i] != '' && temp[i] != '\n') {
            if (endWith('&', temp[i])) {
                $.messager.alert('提示', "词模语法有误,勿以&结尾!", "warning");
                return;
            }

            if (wordPatternTypeName == "选择词模") {
                simpleWordPattern += "++*" + temp[i] + '\n';
            } else if (wordPatternTypeName == "排除词模") {
                simpleWordPattern += "~*" + temp[i] + '\n';
            } else if (wordPatternTypeName == "特征词模") {
                simpleWordPattern += "+*" + temp[i] + '\n';
            } else {
                simpleWordPattern += temp[i] + '\n';
            }
        }
    }

    simpleWordPattern = simpleWordPattern.substr(0, simpleWordPattern.length - 1).replace(/[\[\s+]\]/g,']');
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			collectionIntention : collectionIntention,
			wordPatternType : wordPatternType,
			simpleWordPattern : simpleWordPattern,
			type : "saveCollectionIntention",
		},
		success : function(data) {
        	$.messager.alert('系统提示', data.checkInfo, "info");
        	if (data.success) {
	        	// 刷新关联要素下拉框
        		loadCollectionIntention();
	    		// 关闭用户答案页面
	    		$('#collectionIntentionAddPage').window('close');
        	}
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}
// 删除关联意图
function deleteCollectionIntention(collectionIntention) {
	$.ajax({
		url : '../interactiveSceneCall.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			collectionIntention : collectionIntention,
			type : "deleteCollectionIntention",
		},
		success : function(data) {
        	if (data.success) {
	        	// 刷新关联意图下拉框
        		loadCollectionIntention();
        		// 刷新关联要素下拉框
        		loadCollectionElement();
        	}
		},
		error : function(xhr, status, error) {
			$.messager.alert('系统异常', "删除关联意图,请求数据失败!", "error");
		}
	});
}

// 去重
function quchong(a) {
    var n = {}; // n为hash表
    var r = []; // r为临时数组
    for(var i = 0; i < a.length; i++){ //遍历当前数组
        if (!n[a[i]]) { //如果hash表中没有当前项
            n[a[i]] = true; //存入hash表
            r.push(a[i]); //把当前数组的当前项push到临时数组里面
        }
    }
    return r;
}