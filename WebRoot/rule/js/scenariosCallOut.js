var userid;
var publicscenariosid;
var publicscenariosname;
var ioa;
var strategy;
var flag;
var public_thisGraphObj; // 鼠标事件触发的对象
var scenarioselementidForUpdate; // 用于场景要素编辑中获取选择对象id
var MAX_SCENARIO_COUNT = 21;
var customerAnswerSelectId;

var otherResponseNames=[];
var otherResponseValues=[];

var smsTemplates;

//用于定义模板的简洁性
var $GO = go.GraphObject.make;

// 定义图表对象
var myDiagram;

var condCnt=0;
var key_valCnt = 0;

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
	
	// 初始化回复内容编辑页面
	initResponseEditPage()
	
	// 初始化用户回答
	initCustomerAnswer();
	
	// 初始化短信模板
	//initSmsTemplate();
	
	// 初始化信息收集类型
	initCollectionType();
	
	// 初始化重复次数下拉框
	initCollectionTimes();
	
	// 初始化DTMF按键收集
	initDTMFPress();

	// 转人工组件
	initTransfer();

	// 动作组件
	initURLAction();
	
	// 条件组件
	initCondition();

});

// 初始化回复内容编辑页面
function initResponseEditPage() {
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
		url : '../saveConfiguration.action',
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
		url : '../saveConfiguration.action',
		type : "post",
		data : {
			type : 'querySmsTemplate'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				smsTemplates = data.rows;
				// 短信模板下拉框
				for(var i=0; i<smsTemplates.length; i++) {
					$("#templateId").html('');
					$("#templateId").append('<option value="'+smsTemplates[i].templateId+'">'+smsTemplates[i].templateName+'</option>');
				}
				// 短信模板变量
				$("#templateId").change(function(){  
					$("#sms-varibales-span").html('');
					if(smsTemplates != null && smsTemplates.length > 0) {
						for(var i=0; i<smsTemplates.length; i++) {
							if($("#templateId").val() == smsTemplates[i].templateId) {
								var variableNames = smsTemplates[i].variableNames;
								for(var i=0; i<variableNames.length; i++) {
									$("#sms-varibales-span").append('<br/>'+variableNames[j]+'：<input id="'+variableNames[j]+'" class="easyui-textbox" style="width: 200px;"/>');
								}
							}
						}
					}
			    });   
			}
		}
	});
}

// 初始化信息收集类型
function initCollectionType() {
	$.ajax({
		url : '../saveConfiguration.action',
		type : "post",
		data : {
			type : 'queryCollectionType'
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if(data.success) {
				var collectionTypes = data.rows;
				// 信息收集类型下拉框
				$("#collectionType").combobox({
					width: 240,
					valueField: 'id',    
			        textField: 'text',
			        data : collectionTypes
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
    $.each($("input[name='presstype']"),function (index) {
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

var condition = "<div class=\"form-div\">" +
"                            <div class=\"form-div\"> " +
"								 <input class=\"easyui-textbox\" name=\"param_name\" data-options=\"prompt:'name'\" style=\"width: 80%; height: 25px;\"/>" +
"								 <a style='width: 20px;height: 10px' href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-remove'\">+移除</a>" +
"							 </div>" +
"                            <div class=\"form-div\"> <input class=\"easyui-combobox\" name=\"param_relation\" data-options=\"prompt:'比较关系',valueField: 'id',textField: 'text',data: [{id: '大于', text: '大于'},{ id: '小于', text: '小于'},{ id: '等于', text: '等于'}]\" style=\"width: 80%; height: 25px;\"/></div>" +
"                            <div class=\"form-div\"> <input class=\"easyui-combobox\" name=\"param_type\" data-options=\"prompt:'类型',valueField: 'id',textField: 'text',data: [{id: 'String', text: 'String'},{ id: 'Integer', text: 'Integer'},{ id: 'Variable', text: 'Variable'}]\" style=\"width: 40%; height: 25px;\"/>" +
"                                <input class=\"easyui-textbox\" name=\"param_value\" data-options=\"prompt:'name'\" style=\"width: 40%; height: 25px;\"/></div>" +
"                            <div class=\"form-div\">" +
"                                <div style=\"padding:3px;float:left;width: 20%\"> <input type=\"checkbox\" name=\"isUpDown\"  style=\"width:30%\"><span style=\"width: 70%\">浮动</span></div>" +
"                                <div style=\"width: 80%\">" +
"                                    <input class=\"easyui-combobox\" name=\"upDownType\" data-options=\"prompt:'上下浮动',valueField: 'id',textField: 'text',data: [{id: '0', text: '上浮'},{ id: '1', text: '下浮'}]\" style=\"width: 30%\"/>" +
"                                    <input class=\"easyui-textbox\" name=\"updownRatio\" style=\"width:30%\" data-options=\"prompt:'18'\" buttonText=\"%\">" +
"                                </div>" +
"                            </div>" +
"                        </div>";


var addAnd = " <div class=\"form-div\" style=\"padding-top: 5px;background: #E2E2E2;\">" +
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
	var nodetmp = $("#conditions").find("div").eq(0).append(cpAddAnd);
	$.parser.parse(cpAddAnd);
	
	// 添加AND条件
	function addVariable(conditionIdx, variableIdx) {
		var cpCondition = $($.parseHTML(condition));//$($.parseHTML(this.linkTemplate))
		var cphr=$("</br></br><hr style=\"width:200px;height:1px;border:none;border-top:1px dashed #FFFFFF;\" />");
	    if(variableIdx==0) { 
	    	cpCondition.find("a").remove();
	    } else {
	    	cpCondition.find("a").bind("click",function () {
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
	    cpUpdown.prop("checked",false );

	    var paramNameInput = "param_name" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramRelationInput = "param_relation" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramTypeInput = "param_type" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var paramValueInput = "param_value" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var isUpDownInput = "isUpDown" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var upDownTypeInput = "upDownType" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    var updownRatioInput = "updownRatio" + "_cdt"+conditionIdx + "_and"+variableIdx;
	    
	    cpCondition.find("input[name='param_name']").attr("name", paramNameInput);
	    cpCondition.find("input[name='param_relation']").attr("name", paramRelationInput);
	    cpCondition.find("input[name='param_type']").attr("name", paramTypeInput);
	    cpCondition.find("input[name='param_value']").attr("name", paramValueInput);
	    cpCondition.find("input[name='isUpDown']").attr("name", isUpDownInput);
	    cpCondition.find("input[name='upDownType']").attr("name", upDownTypeInput);
	    cpCondition.find("input[name='updownRatio']").attr("name", updownRatioInput);
	    
	    cpCondition.find("input[name='"+paramNameInput+"']").attr("id", paramNameInput);
	    cpCondition.find("input[name='"+paramRelationInput+"']").attr("id", paramRelationInput);
	    cpCondition.find("input[name='"+paramTypeInput+"']").attr("id", paramTypeInput);
	    cpCondition.find("input[name='"+paramValueInput+"']").attr("id", paramValueInput);
	    cpCondition.find("input[name='"+isUpDownInput+"']").attr("id", isUpDownInput);
	    cpCondition.find("input[name='"+upDownTypeInput+"']").attr("id", upDownTypeInput);
	    cpCondition.find("input[name='"+updownRatioInput+"']").attr("id", updownRatioInput);
	    
	    var andcd = cpAddAnd.find("div[add='andconditions']");
	    var nodeCondition =andcd.append(cpCondition);
	    var nodeHr =andcd.append(cphr);
	    $.parser.parse(cpCondition);
		$.parser.parse(cphr);
	};

	var cphr=$("<hr style=\"width:240px;height:1px;border:none;border-top:2px dashed #0066CC;\" />");
	nodetmp = $("#conditions").find("div").eq(0).append(cphr);
	$.parser.parse(cphr );

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
			condCnt--;
		})
	}
	addVariable(conditionIdx, variableIdx++);
}

function initCondition(){
	$("#addCondition").bind("click",function () {
		addOneCondition(condCnt, 0);
		condCnt++;
	});
}
function initURLAction(){
	$("#addKey_val").bind("click", function () {
		var node = $("<div class=\"form-div\"><input style=\"float:left;width: 30%;height:25px;border:1px solid  #C3D1DF;\"></input><input style=\"height:25px;float:left;width: 30%;border:1px solid  #C3D1DF;\"> <a href=\"javascript:void(0)\" class=\"easyui-linkbutton\" data-options=\"iconCls:'icon-remove'\">删除</a></div>");
		key_valCnt++;
		node.find("input").each(function (index, ele) {
			if (index == 0) {
				$(ele).attr("name", "key_" + key_valCnt);
			}
			else {
				$(ele).attr("name", "val_" + key_valCnt);
			}
		});
		node.find("a").bind('click', function () {
			this.parentNode.remove();
		})
		var appd = $("#key_vals_div").append(node);
		$.parser.parse(appd);
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
                    maxSize : new go.Size(300, 100),
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
				}, new go.Binding("text", "tts").makeTwoWay()), 
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
					}, new go.Binding("text", "tts").makeTwoWay()), 
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
					$GO(go.TextBlock, "Collection", {
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
					}, new go.Binding("text", "collectionName").makeTwoWay()), 
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
					$GO(go.TextBlock, {
						margin : 0,
						maxSize : new go.Size(0, 0),
						editable : false,
						visible : false
					}, new go.Binding("text", "response").makeTwoWay()), 
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
				$GO(go.TextBlock, {
					margin : 0,
					maxSize : new go.Size(0, 0),
					editable : false,
					visible : false
				}, new go.Binding("text", "response").makeTwoWay()), 
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
		url : '../saveConfiguration.action',
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
					"text" : "开始"
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
			tts: '',
			code:'',
			otherResponses: [],
			bottomArray : [],
			action: '',
			actionParams: ''
		}, {
			key : "Collection",
			category : "Collection",
			text : "信息收集",
			width: 50,
			height: 20,
			margin: 0,
		},{
			key : "DTMFPress",
			category : "DTMFPress",
			text : "DTMF",
			width: 50,
			height: 20,
			margin: 0,
			bottomArray : [],
		},{
			key : "URLAction",
			category : "URLAction",
			text : "动作组件",
			width: 50,
			height: 20,
			margin: 0,
		},{
			key : "Condition",
			category : "Condition",
			text : "条件组件",
			width: 50,
			height: 20,
			margin: 0,
		},{
			key : "Transfer",
			category : "Transfer",
			text : "转人工",
			width: 50,
			height: 20,
			margin: 0,
		},{
			key : "End",
			category : "End",
			text : "结束语",
			width: 50,
			height: 20,
			margin: 0,
			tts: '',
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
					myDiagram.model.setDataProperty(nodeData, 'margin', 10);
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
				if (nodeData.category == "End") {
					myDiagram.model.setDataProperty(nodeData, 'width', 100);
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
		if (from.match("Normal") && to.match("Normal")) {
			//myDiagram.commandHandler.deleteSelection();
			//$.messager.alert('警告', "不允许矩形到矩形的直连", "error");
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
			$('#myNormalEditDiv').show();
		} 
		if (nodeData.category == "End") {
			// 回复编辑内容赋值
			setTTSResponseValue();
			$('#customer-answer-table').hide();
			$('#myNormalEditDiv').show();
		}
		if (nodeData.category == "Collection") {
			// 信息收集内容赋值
			$("#collectionName").textbox('setValue', nodeData.text);
			$("#collectionParam").textbox('setValue', nodeData.collectionParam);
			$("#collectionType").combobox('setValue', nodeData.collectionType);
			$("#collectionTimes").combobox('setValue', nodeData.collectionTimes);
			$("#collectionWords").val(nodeData.collectionWords);
			$('#myCollectionEditDiv').show();
		}
		if (nodeData.category == "DTMFPress") {
			$('#dTMFPressform').form('load',nodeData);
			$("#comb_attempt_limit").combobox({
			    width:240,
			    valueField:'value',
			    textField:'text',
			    data: [{'text': '1次', 'value': '1'},{'text': '2次', 'value': '2'}],
			    onLoadSuccess: function(){
			        $(this).combobox('setValue',nodeData.comb_attempt_limit);
			    } 
			});
			$("#comb_finish_press").combobox({
			    width:240,
			    valueField:'value',
			    textField:'text',
			    data: [{'text': '按*号键', 'value': '*'},{'text': '按#号键', 'value': '#'}],
			    onLoadSuccess: function(){
			        $(this).combobox('setValue',nodeData.comb_finish_press);
			    } 
			});
			$('#myDTMFPressDiv').show();
			$.parser.parse();
		}
		if (nodeData.category == "Condition") {
			// 节点数据
			var nodeData = public_thisGraphObj.part.data; 
			if(nodeData.conditions == "" || nodeData.conditions == null || nodeData.conditions == undefined) {
				nodeData.conditions = [];
			}
			// 条件个数
			condCnt = nodeData.conditions.length; 
			// 清空条件
			$('#myConditionDiv').find("#conditions").find("div").eq(0).empty();
			$('#myConditionDiv').show();
			// 设置条件名称
			$("#txt_condition_name").textbox('setValue', nodeData.txt_condition_name);
			// 设置条件内容
			var conditions = nodeData.conditions;
			for(var i = 0; i < conditions.length; i++) {
				var andConditions = conditions[i];
				addOneCondition(i,0);
				for(var j = 0; j < andConditions.length; j++) {
					if(j > 0) {
						$("#andBtn" + "_cdt"+i).trigger("click");
					}
					var andCondition = andConditions[j];
					$("#param_name_cdt"+i+"_and"+j).textbox('setValue', andCondition.param_name);
					$("#param_relation_cdt"+i+"_and"+j).combobox('setValue', andCondition.param_relation);
					$("#param_type_cdt"+i+"_and"+j).combobox('setValue', andCondition.param_type);
					$("#param_value_cdt"+i+"_and"+j).textbox('setValue', andCondition.param_value);
				}
			}
		}
		if (nodeData.category == "URLAction") {
			$('#myURLActionDiv').show();
			$.parser.parse();
		}
		if (nodeData.category == "Transfer") {
			$('#myTransferDiv').show();
			$.parser.parse();
		}
	});
}

// 添加回复内容
function addToResponse(){
	var tts = $("#tts").val();
	var code = $("#code").val();	
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
		actionParams = $("#templateId").val()+"|";
		if($('#sms-varibales-span').find(".easyui-textbox").length > 0) {
			$('#sms-varibales-span').find(".easyui-textbox").each(function() {
				actionParams += $(this).attr("id")+'='+$(this).textbox('getText')+'&';
			});
			actionParams = actionParams.substring(0, actionParams.length-1);
		}
		action = 'sms';
	} 	
	// 获取已选的用户回答
    var checkedArray = [];   
    $('[name=customerAnswer]:checkbox:checked').each(function() {
    	checkedArray.push({'text':$(this).val()});
    });
	// 设置TTS节点
    setTTSResponseNode(tts,code,action,actionParams,otherResponses,checkedArray);
	// 关闭回复表单
	$('#closeRule0').trigger("click");
}

// 设置TTS节点
function setTTSResponseNode(tts,code,action,actionParams,otherResponses,checkedArray) {
	var nodeData = public_thisGraphObj.part.data;
	if (nodeData.category == "Normal") {
		myDiagram.model.setDataProperty(nodeData, 'text', tts);
		myDiagram.model.setDataProperty(nodeData, 'tts', tts);
		myDiagram.model.setDataProperty(nodeData, 'code', code);
		myDiagram.model.setDataProperty(nodeData, 'action', action);
		myDiagram.model.setDataProperty(nodeData, 'actionParams', actionParams);
		myDiagram.model.setDataProperty(nodeData, 'otherResponses', otherResponses);
		myDiagram.model.setDataProperty(nodeData, 'bottomArray', checkedArray);
	}
	if (nodeData.category == "End") {
		myDiagram.model.setDataProperty(nodeData, 'text', tts);
		myDiagram.model.setDataProperty(nodeData, 'tts', tts);
		myDiagram.model.setDataProperty(nodeData, 'code', code);
		myDiagram.model.setDataProperty(nodeData, 'action', action);
		myDiagram.model.setDataProperty(nodeData, 'actionParams', actionParams);
		myDiagram.model.setDataProperty(nodeData, 'otherResponses', otherResponses);
	}
}

//TTS编辑页面赋值
function setTTSResponseValue() {
	var nodeData = public_thisGraphObj.part.data;
	$("#tts").val(nodeData.tts);
	$("#code").textbox("setValue", nodeData.code);
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
	if(nodeData.action == 'sms') {
		$("#sms-checkbox").prop({checked:true}); 
		$('#sms-templete-span').show();
		$('#sms-varibales-span').show();
		var actionParams = nodeData.actionParams;
		var templateId = actionParams.split('|')[0];
		$("#templateId").textbox('setText', templateId);
		var varibales = actionParams.split('|')[1].split('&');
		if(varibales.length > 0) {
			for(var i=0; i<varibales.length; i++) {
				var varibaleName = varibales[i].split('=')[0];
				var varibaleValue = varibales[i].split('=')[1];
				$("#"+varibaleName).textbox('setText', varibaleValue);
			}
		}
	} else {
		$("#sms-checkbox").prop({checked:false}); 
		$('#sms-templete-span').hide();
		$('#sms-varibales-span').hide();
	}
	var bottomArray = nodeData.bottomArray;
	$("#customerAnswerSpan").find("input:checkbox").prop({checked:false});
	if(bottomArray.length > 0) {
		for(var i=0;i<bottomArray.length;i++) {
			$("#customerAnswerSpan").find("input:checkbox[value='"+bottomArray[i].text+"']").prop({checked:true}); 
		}
	}
}

// 鼠标点击事件	
function buttonClick() {
	
	// 添加用户答案页面
	$('#customerAnswerAdd-toPageBtn').click(function() {
		$('#customerAnswerAddPage').window('open');
		
	});
	
	// 关闭用户答案页面
	$('#customerAnswerAdd-closeBtn').click(function() {
		$('#customerAnswerAddPage').window('close');
		
	});
	
	// 生成词模
	$('#customerAnswerAdd-analyzeBtn').click(function() {		
		autowordpat();
	});
	
	// 保存用户答案
	$('#customerAnswerAdd-saveBtn').click(function() {
		saveCustomerAnswer();
	});
	
	// 保存自学习词模
	$('#autoworrdpat-saveBtn').click(function() {
		addAutoWordpat();
	});
	
	// 关闭自学习词模页面
	$('#autoworrdpat-closeBtn').click(function() {
		$('#"autoworrdpat"').window('close');
	});
	
	// 保存流程图
	$('#saveBtSubmit').click(function() {
		savaAndSubmitNoVerification();
	});

	// 保存回复内容
	$('#saveRule0').click(function() {
		addToResponse();
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
			$('#sms-templete-span').show();
			$('#sms-varibales-span').show();
		} else {
			$('#sms-templete-span').hide();
			$('#sms-varibales-span').hide();
		}
	});
	
	// 保存信息收集
	$('#saveCollection').click(function() {
		var nodeData = public_thisGraphObj.part.data;
		var collectionName = $("#collectionName").textbox('getValue');
		var collectionParam = $("#collectionParam").textbox('getValue');
		var collectionType = $("#collectionType").combobox('getValue');
		var collectionTimes = $("#collectionTimes").combobox('getValue');
		var collectionWords = $("#collectionWords").val();
		myDiagram.model.setDataProperty(nodeData, 'text', collectionName);
		myDiagram.model.setDataProperty(nodeData, 'collectionParam', collectionParam);
		myDiagram.model.setDataProperty(nodeData, 'collectionType', collectionType);
		myDiagram.model.setDataProperty(nodeData, 'collectionTimes', collectionTimes);
		myDiagram.model.setDataProperty(nodeData, 'collectionWords', collectionWords);
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
			if(this.name == "presstype" || this.name=="txt_max_length"||this.name=="txt_press_timeout"||
				this.name=="txt_min_length")
			{
				t[this.name] = parseInt(this.value);
				return true;
			}
		});
		if(isNaN(t.txt_press_timeout))
		{
			$.messager.alert("警告","超时时间，格式不合法","warn");
			return;
		}
		if(t.presstype==1 && (isNaN(t.txt_max_length) ||  isNaN(t.txt_min_length)))
		{
			$.messager.alert("警告","最大，最短长度，格式不合法","warn");
			return;
		}    
		$.each(array,function(index, value){
			t[this.name] = this.value;
		});
		
        $.each(t,function(index,value){
        	myDiagram.model.setDataProperty(nodeData, index, value);
        });
        myDiagram.model.setDataProperty(nodeData, "text", t.txt_dtmf_name);
        $("#closeDTMFPress").trigger("click");
    })
    $("#closeDTMFPress").bind("click",function () {
        $('#dTMFPressform').form('clear');
        $('#myDTMFPressDiv').hide();
	});
	
	$("#saveURLAction").bind("click", function () {
		var array = $('#URLActionform').serializeArray();
		console.log(array);
		$.messager.alert("提示", JSON.stringify(array), "info")
	});

	$("#saveCondition").bind("click",function () {
		var nodeData = public_thisGraphObj.part.data;
		var array = $('#Conditionform').serializeArray();
		var conditions = [];
		var bottomArray = [];
		for(var i=0; i < condCnt; i++) {
			var andConditions = [];
			conditions[i] = andConditions;
			bottomArray[i] = {'text':'条件'+i};
		}
		$.each(array,function(index, value){
			if(this.name.indexOf("_and") > -1) {
				var nameSplitArray = this.name.split("_");
				var conditionIndexItem = nameSplitArray[nameSplitArray.length-2];
				var conditionIndex = conditionIndexItem.substring(conditionIndexItem.length-1);
				var andIndexItem = nameSplitArray[nameSplitArray.length-1];
				var andIndex = andIndexItem.substring(andIndexItem.length-1);
				var andConditions = conditions[conditionIndex];
				if(andConditions[andIndex] == "" || andConditions[andIndex] == null || andConditions[andIndex] == undefined) {
					andConditions[andIndex] = {}
				} 
				if(this.name.indexOf("param_name") > -1) {
					andConditions[andIndex].param_name = this.value;
				}
				if(this.name.indexOf("param_relation") > -1) {
					andConditions[andIndex].param_relation = this.value;
				}
				if(this.name.indexOf("param_type") > -1) {
					andConditions[andIndex].param_type = this.value;
				}
				if(this.name.indexOf("param_value") > -1) {
					andConditions[andIndex].param_value = this.value;
				}
			} else {
				myDiagram.model.setDataProperty(nodeData, this.name, this.value);
			}
		});
		myDiagram.model.setDataProperty(nodeData, "conditions", conditions);
		myDiagram.model.setDataProperty(nodeData, "bottomArray", bottomArray);
		$('#myConditionDiv').hide();
		//$.messager.alert("提示",JSON.stringify(array),"info");
		//$.messager.alert("提示",JSON.stringify(conditions),"info");
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
	var ispassverification = true;
	if (ispassverification) {
		return true;
	}
	return false;
}

// 提交已修改的数据
function commitRevisedData(jsonStr) {
	console.log(jsonStr);
	$.ajax({
		url : '../saveConfiguration.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			scenariosName : publicscenariosname,
			type : "saveConfig",
			m_request : jsonStr
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

function autowordpat() {
	var autowordpat = $.trim($("#customerAnswerAdd-keywords").textbox('getValue'));
	if (autowordpat == '' || autowordpat == null) {
		$.messager.alert('提示', "请在生成词模的输入框填写内容！", "warning");
		return;
	}
	
	$.ajax({
		url : '../saveConfiguration.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			autowordpat : autowordpat,
			type : "autowordpat",
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
    var wordpat = inputValue + rvalue;
    $("#customerAnswerAdd-wordclasses").val(wordpat);
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
	var simplewordpat = $("#customerAnswerAdd-wordclasses").val().replace(new RegExp(' ', 'g'), '');
	if (simplewordpat === "") {
        $.messager.alert('提示', "词模不能为空！", "warning");
        return;
    } else if (simplewordpat.split('#')[0].indexOf("~") != -1) {
        $.messager.alert('提示', "词模中有非法字符 ~ 存在", "warning");
        return;
    } else if (simplewordpat.split('#')[0].indexOf("+") != -1) {
        $.messager.alert('提示', "词模中有非法字符 + 存在", "warning");
        return;
    }
	
	var wordpattypename = $("#customerAnswerAdd-wordtype").numberbox('getText');
	if (wordpattypename != "等于词模" && wordpattypename == "普通词模") {
        if (simplewordpat.indexOf("类-") != -1 && simplewordpat.indexOf("类*") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        }
    }

    if (wordpattypename == "等于词模") {
        if (simplewordpat.indexOf("类-") == -1) {
            $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
            return;
        } else {
            if (simplewordpat.indexOf("类*") != -1) {
                $.messager.alert('提示', "请确认词模格式是否和词模类型一致", "warning");
                return;
            }
        }
    }
    
    // 排除词模，如果只有一个必选项，并且该必选项是子句时，不允许添加
    if (wordpattypename == "排除词模" && simplewordpat.indexOf("子句") != -1) {
        if (simplewordpat.indexOf("*") == -1 && simplewordpat.indexOf("-") == -1) {
            $.messager.alert('提示', "系统不支持单子句排除词模，建议附加另一个不带\"[]\"的近类或父类", "warning");
            return;
        }
    }

    if (simplewordpat.indexOf('(OOV)') != -1) {
        $.messager.alert('提示', "（OOV）为系统未识别词类，需新增对应词词类!", "warning");
        return;
    }

    if (simplewordpat.indexOf("@") != -1) {
        $.messager.alert('提示', "存在非法字符 '@'", "warning");
        return;
    }
    if (simplewordpat.indexOf("#无序#") == -1 && simplewordpat.indexOf("#有序#") == -1) {
        $.messager.alert('提示', "请输入正确格式序列：'#无序#' 或 '#有序#'", "warning");
        return;
    }

    var wordpattype = 0;
    if (wordpattypename == "等于词模") {
        wordpattype = "1";
    } else if (wordpattypename == "排除词模") {
        wordpattype = "2";
    } else if (wordpattypename =="选择词模") {
        wordpattype = "3";
    } else if (wordpattypename == "特征词模") {
        wordpattype = "4";
    } else if (wordpattypename == "自学习词模") {
        wordpattype = "5";
    } else {
        wordpattype = "0";
    }
    
    simplewordpat = simplewordpat.replace(new RegExp("\r\n",'g'),"\n");
    simplewordpat = simplewordpat.replace(/^\n+|\n+$/g,"");
    var temp = simplewordpat.split('\n');

    temp = quchong(temp);
    simplewordpat = "";
    for (var i = 0; i < temp.length; i++) {
        if (temp[i] != '' && temp[i] != '\n') {
            if (endWith('&', temp[i])) {
                $.messager.alert('提示', "词模语法有误,勿以&结尾!", "warning");
                return;
            }

            if (wordpattypename == "选择词模") {
                simplewordpat += "++*" + temp[i] + '\n';
            } else if (wordpattypename == "排除词模") {
                simplewordpat += "~*" + temp[i] + '\n';
            } else if (wordpattypename == "特征词模") {
                simplewordpat += "+*" + temp[i] + '\n';
            } else {
                simplewordpat += temp[i] + '\n';
            }
        }
    }

    simplewordpat = simplewordpat.substr(0, simplewordpat.length - 1).replace(/[\[\s+]\]/g,']');
	$.ajax({
		url : '../saveConfiguration.action',
		type : 'post',
		dataType : 'json',
		data : {
			scenariosid : publicscenariosid,
			customeranswer : customeranswer,
			wordpattype : wordpattype,
			simplewordpat : simplewordpat,
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