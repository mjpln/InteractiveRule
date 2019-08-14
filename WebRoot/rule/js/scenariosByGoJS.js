var userid;
var publicscenariosid;
var publicscenariosname;
var ioa;
var strategy;
var flag;
var public_thisGraphObj;// 鼠标事件触发的对象
var scenarioselementidForUpdate;// 用于场景要素编辑中获取选择对象id
var oldweightForUpdate;// 用于场景要素编辑中获取选择对象id
// 设置rule的列数，qby,20181012
var MAX_SCENARIO_COUNT = 21;
var publicDiagram;// 定义全局图表
var ruleresponsetemplate;
var oldscenarioselementname;

$(function() {
	// 根据屏幕大小设置页面布局
	diagramStayle();
	var urlparams = new UrlParams();// 所有url参数
	// 获得url中场景ID
	userid = decodeURI(urlparams.userid);
	publicscenariosid = decodeURI(urlparams.scenariosid);
	publicscenariosname = decodeURI(urlparams.scenariosname);
	ioa = decodeURI(urlparams.ioa);
	strategy = decodeURI(urlparams.strategy);
//	publicscenariosid = '1831783';
//	publicscenariosname = '测试场景';
//	ioa = '电信行业->电信集团->4G业务客服应用';
//	strategy = '';

	// 制作流程图
	makeGraphObject();

	/**
	 * 场景要素
	 */
	// 初始化场景要素名下拉框
	initScenariosElementCombobox();
	// 初始化场景要素优先级下拉框
	loadWeightCombobox("");
	// 初始化场景要素对应词类下拉框
	createWordClassCombobox();
	// 初始化场景要素对应信息表列下拉框
	createServiceInfoCombobox();
	// 初始化场景要素对应地市下拉树
	getCityTree("");
	
	// 初始化连接线combobox数据
//	getLineComboboxData();

	// 监听鼠标右键动作
	listeningRightclickMouseEvents();
});

// 样式
function diagramStayle() {
	var _PageHeight = window.innerHeight;
	$('#myPaletteDiv').css("height", _PageHeight * 0.5);
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

// 制作流程图
function makeGraphObject() {
	var _thisGraphObj = null;
	// 用于定义模板的简洁性
	var $GO = go.GraphObject.make;

	// 定义图表对象
	var myDiagram = $GO(go.Diagram, "myDiagramDiv", { // 必须命名或引用DIV HTML元素
		initialContentAlignment : go.Spot.Center,// 居中显示内容
		"undoManager.isEnabled" : true,// 启用撤消和恢复
		allowDrop : true,// 允许拖动
		scrollsPageOnFocus : false,// 滚动页焦点
		// layout : $GO(go.ForceDirectedLayout),
		"LinkDrawn" : showLinkLabel, // 下面定义了这个DiagramEvent监听器
	});

	publicDiagram = myDiagram;// 给全局图表对象赋值

	// 制作端口
	function makePort(name, align, spot, output, input) {
		var horizontal = align.equals(go.Spot.Top) || align.equals(go.Spot.Bottom);
		return $GO(go.Shape, "Rectangle", {
			fill : "transparent",// 在MouseEnter事件处理程序中更改为颜色
			strokeWidth : 0,// 无描边
			// desiredSize : new go.Size(50, 50),
			width : horizontal ? NaN : 8,// 如果不是水平伸展，只有8宽
			height : !horizontal ? NaN : 8,// 如果不是垂直拉伸，只有8高
			alignment : align,// 对齐主形状上的端口
			stretch : (horizontal ? go.GraphObject.Horizontal : go.GraphObject.Vertical),
			portId : name,// 将此对象声明为“端口”
			fromSpot : spot,// 声明链接可以在此端口连接的位置
			fromLinkable : output,// 声明用户是否可以从此处绘制链接
			toSpot : go.Spot.Top,// 声明链接可以在此端口连接的位置
			toLinkable : input,// 声明用户是否可以在此绘制链接
			cursor : "pointer",// 显示不同的光标以指示潜在的链接点
		 mouseEnter : function(e, port) {
			if (!e.diagram.isReadOnly)
				port.fill = "rgba(255,0,255,0.5)";
		 },
		 mouseLeave : function(e, port) {
			 port.fill = "transparent";
		 }
		});
	}

	// 添加模板
	addTemplateMap();

	function addTemplateMap() {
		// 定义通用模板
		myDiagram.nodeTemplateMap.add("Conditional", $GO(go.Node, "Table",
				nodeStyle(),
				// the main object is a Panel that surrounds a TextBlock with a
				// rectangular Shape
				$GO(go.Panel, "Auto", $GO(go.Shape, "Rectangle", {
					fill : "#00A9C9",
					strokeWidth : 0
				}), $GO(go.Panel, 'Vertical', {

				}, $GO(go.TextBlock, textStyle(), {
					margin : 2,
					minSize : new go.Size(140, NaN),// 定义文本框最小宽度
					wrap : go.TextBlock.WrapFit, // 块包裹文本的方式
					stretch : go.GraphObject.Fill,
					alignment : go.Spot.Center
				}, new go.Binding("text").makeTwoWay()), $GO(go.Panel, 'Horizontal', {// 底部连接端口
							alignment : go.Spot.Bottom,
							alignmentFocus : go.Spot.Bottom,
							margin : new go.Margin(0, 0, 0, 0)
						}, $GO(go.Panel, 'Horizontal', new go.Binding("itemArray", "bottomArray"), {
							itemTemplate : $GO(go.Panel, {
								margin : 10,
								fromLinkable : true,
								toLinkable : true,
								fromLinkableDuplicates : true,
								toLinkableDuplicates : true,
								fromSpot : go.Spot.Bottom,
								toSpot : go.Spot.Bottom,
								fromMaxLinks : 1,
								toMaxLinks : 0,
								cursor : "pointer"
							}, new go.Binding('portId', 'text'), 
									$GO(go.Panel, 'Auto', $GO(go.Shape, "Rectangle", {
										fill : '#DCEAFC',
										stroke : null,
									}), $GO(go.TextBlock, {
										margin : 5,
										stroke : "#1883D7",
										font : "12px sans-serif"
									}, new go.Binding('text', 'text'))))
						})))), makePort("T", go.Spot.TopCenter,
						go.Spot.TopCenter, false, true)));

		// 定义常规节点的节点模板
		myDiagram.nodeTemplateMap.add("Normal",
						$GO(go.Node, "Table", nodeStyle(), 
								$GO(go.Panel, "Auto", $GO(go.Shape, "RoundedRectangle", {
									fill : "#00A9C9",
									strokeWidth : 0
								}, new go.Binding("figure", "figure")),
								$GO(go.TextBlock, textStyle(), {
									margin : 8,
									maxSize : new go.Size(160, NaN),
									wrap : go.TextBlock.WrapFit,
									editable : false,
								// background :"blue"
								}, new go.Binding("text").makeTwoWay()), 
									$GO(go.TextBlock, {
											margin : 0,
											maxSize : new go.Size(0, 0),
											editable : false,
											visible : false
										}, new go.Binding("text", "response").makeTwoWay()), 
											$GO(go.TextBlock, {
											margin : 0,
											maxSize : new go.Size(0, 0),
											editable : false,
											visible : false
										}, new go.Binding("text", "responsetype").makeTwoWay()),
								$GO(go.TextBlock, {
									margin : 0,
									maxSize : new go.Size(0, 0),
									editable : false,
									visible : false
								}, new go.Binding("text", "ruleresponsetemplate").makeTwoWay())),
								makePort("T", go.Spot.Top, go.Spot.TopSide, false, true), 
								makePort("B", go.Spot.Bottom, go.Spot.BottomSide, true, false)));

		// 主对象是一个面板，它用矩形包围了一个文本块
		myDiagram.nodeTemplateMap.add("Judge", $GO(go.Node, "Table",
				nodeStyle(), $GO(go.Panel, "Auto", $GO(go.Shape, "Diamond", {
					fill : "#00A9C9",
					strokeWidth : 0
				}, new go.Binding("figure", "figure")), $GO(go.TextBlock,
						textStyle(), {
							margin : 8,
							maxSize : new go.Size(160, NaN),
							wrap : go.TextBlock.WrapFit
						}, new go.Binding("text").makeTwoWay())),
				// four named ports, one on each side:
				makePort("T", go.Spot.Top, go.Spot.Top, false, true), 
				makePort("L", go.Spot.Left, go.Spot.Left, true, false),
				makePort("R", go.Spot.Right, go.Spot.Right, true, false),
				makePort("B", go.Spot.Bottom, go.Spot.Bottom, true, false)));

		// 定义模板
		myDiagram.nodeTemplateMap.add("Start", $GO(go.Node, "Table",
				nodeStyle(), $GO(go.Panel, "Auto", $GO(go.Shape, "Ellipse", {
					minSize : new go.Size(40, 40),
					fill : "#79C900",
					strokeWidth : 0
				}), $GO(go.TextBlock, "Start", textStyle(), new go.Binding(
						"text"))), makePort("B", go.Spot.Bottom,
						go.Spot.Bottom, true, false)),
				$GO("TreeExpanderButton"));

		// 定义模板
		myDiagram.nodeTemplateMap.add("End", $GO(go.Node, "Table", nodeStyle(),
				$GO(go.Panel, "Auto", $GO(go.Shape, "Ellipse", {
					minSize : new go.Size(40, 40),
					fill : "#DC3C00",
					strokeWidth : 0
				}), $GO(go.TextBlock, "End", textStyle(),
						new go.Binding("text"))), makePort("T", go.Spot.Top,
						go.Spot.Top, false, true)));

		// 替换LinkTemplateMap中的默认链接模板
		myDiagram.linkTemplate = $GO(go.Link, {
			routing : go.Link.AvoidsNodes,
			curve : go.Link.JumpOver,
			corner : 5,
			toShortLength : 4,
			relinkableFrom : true,
			relinkableTo : true,
			reshapable : false,
			resegmentable : false,
			// mouse-overs subtly highlight links:
			mouseEnter : function(e, link) {
				link.findObject("HIGHLIGHT").stroke = "rgba(30,144,255,0.2)";
			},
			mouseLeave : function(e, link) {
				link.findObject("HIGHLIGHT").stroke = "transparent";
			},
			selectionAdorned : false
		}, new go.Binding("points").makeTwoWay(), $GO(go.Shape, {// 突出显示形状，通常是透明的
			isPanelMain : true,
			strokeWidth : 8,
			stroke : "transparent",
			name : "HIGHLIGHT"
		}), $GO(go.Shape, {
			isPanelMain : true,
			stroke : "gray",
			strokeWidth : 2
		}, new go.Binding("stroke", "isSelected", function(sel) {
			return sel ? "dodgerblue" : "gray";
		}).ofObject()), $GO(go.Shape, {
			toArrow : "standard",
			strokeWidth : 0,
			fill : "gray"
		}), $GO(go.Panel, "Auto", {
			visible : false,
			name : "LABEL",
			segmentIndex : 2,
			segmentFraction : 0.5
		}, new go.Binding("visible", "visible").makeTwoWay(), $GO(go.Shape, "RoundedRectangle", {
					fill : "#F8F8F8",
					strokeWidth : 0
				}, new go.Binding("fill", "color").makeTwoWay()), $GO(go.TextBlock, "场景要素值", {
			textAlign : "center",
			font : "10pt helvetica, arial, sans-serif",
			stroke : "#333333",
			editable : false
		}, new go.Binding("text", "text").makeTwoWay())));
	}

	// 初始化图标
	load();

	// 定义左侧可拖动画板
	makePalette();
	function makePalette() {
		// 定义左侧可拖动画板
		var myPalette = $GO(go.Palette, "myPaletteDiv", {
			nodeTemplateMap : myDiagram.nodeTemplateMap,
			model : new go.GraphLinksModel([ {
				key : "Start",
				category : "Start",
				text : "用户问题"
			}, {
				key : "Judge",
				category : "Judge",
				text : "场景要素"
			},
			// {
			// key : "Conditional",
			// category : "Conditional",
			// text : "条件节点",
			// bottomArray : [ {
			// "text" : "A"
			// }, {
			// "text" : "B"
			// }]
			// },
			{
				key : "Normal",
				category : "Normal",
				text : "回复内容"
			}, {
				key : "End",
				category : "End",
				text : "End"
			} ])
		});

	}

	function load() {
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
					myDiagram.model = go.Model.fromJson(data.rowdata[0].scenejsondata);
				} else {
					var json = '{ "class": "go.GraphLinksModel","linkFromPortIdProperty": "fromPort","linkToPortIdProperty": "toPort","nodeDataArray": [],"linkDataArray": []}';
					myDiagram.model = go.Model.fromJson(json);
					var nodeDataArray = [ {
						"key" : 'Start',
						"category" : "Start",
						"loc" : "175 0",
						"text" : "用户问题"
					} ];
					myDiagram.model.nodeDataArray = nodeDataArray;
				}
			}
		});
	}

	// 概括内容
	var myOverView = $GO(go.Overview, "myOverViewDiv", {
		observed : myDiagram
	});

	addDragEvents();
	function addDragEvents() {
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
					}
					if (nodeData.category == "End") {
						onlyNode(myDiagram, "End", "End节点只能有一个");
					}
				}
			}
		});
	}
	
	// 监听连线完成事件
	addLinkDeawn();
	function addLinkDeawn() {
		myDiagram.addDiagramListener("LinkDrawn", function(e) {
			var subject = e.subject;
			var nodeData = subject.part.data;
			var from = nodeData.from;
			var to = nodeData.to;
			if (from.match("Normal") && to.match("Normal")) {
				myDiagram.commandHandler.deleteSelection();
				$.messager.alert('警告', "不允许矩形到矩形的直连", "error");
			}
		});
	}

	// addDoubleClickEvents();
	function addDoubleClickEvents() {
		// 添加双击事件用于编辑选中对象
		myDiagram.addDiagramListener("ObjectDoubleClicked", function(e) {
			var subject = e.subject;
			var nodeData = subject.part.data;
			_thisGraphObj = subject;
			$('#myEditDiv').hide();
			$('#myDiamondEditDiv').hide();
			$('#myNormalEditDiv').hide();
			$('#myStartEditDiv').hide();
			if (nodeData.category == "Conditional") {
				$('#myEditDiv').show();
				// 添加端口文本修改输入框
				appendPortHtml(subject);
				$('#content').val(nodeData.text);
				$('#outputNum').val(nodeData.bottomArray.length);
			}
			if (nodeData.category == "Judge") {
				$('#myDiamondEditDiv').show();
				$('#myDiamondEditDiv #scenarioselementname').combobox("setValue", nodeData.text);
			}
			if (nodeData.category == "Normal") {
				$('#myNormalEditDiv').show();
				$('#myNormalEditDiv #content').val(nodeData.text);
			}
			if (nodeData.category == "Start") {
				$('#myStartEditDiv').show();
				$('#myStartEditDiv #content').val(nodeData.text);
			}
		});
	}

	addMouseRightClick();
	// 添加右键菜单事件
	function addMouseRightClick() {
		myDiagram.addDiagramListener("ObjectContextClicked", function(e) {
			var subject = e.subject;
			var nodeData = subject.part.data;
			public_thisGraphObj = subject;
			var event = window.event;
			x = event.clientX;
			y = event.clientY;
			if (subject.part instanceof go.Part) {
				var part = subject.part;
				var data = part.data;
				if (part instanceof go.Link) {
					if (data.from.match("Judge")) {
						$('#linkmenu').menu('show', {
							left : x,
							top : y
						});
					}
					if (data.from.match("Normal")) {
						$('#linkmenu1').menu('show', {
							left : x,
							top : y
						});
					}
				} else {
					if (data.category == 'Judge') {
						$('#mm').menu('show', {
							left : x,
							top : y
						});
					}
					if (data.category == 'Normal') {
						$('#normalmenu').menu('show', {
							left : x,
							top : y
						});
						$('#myNormalEditDiv #content').val(nodeData.text);
					}
					if (data.category == 'Start') {
						$('#startmenu').menu('show', {
							left : x,
							top : y
						});
						$('#myStartEditDiv #content').val(nodeData.text);
					}
				}
			}
		});
	}

	// 出口编辑框值改变
	$("#outputNum").bind("input propertychange", function() {
		var num = $('#outputNum').val();
		var nodedata = _thisGraphObj.part.data;
		var length = nodedata.bottomArray.length;
		if (num > length) {
			$('#nodePortEditer').empty();
			for ( var i = 0; i < length; i++) {
				var inputHtmlStr = '<input id="nodePort' + i + '" style="width:90%;margin-left: 10px" type="text" value="' + nodedata.bottomArray[i].text + '" />';
				$('#nodePortEditer').append(inputHtmlStr);
			}
			for ( var i = length; i < num; i++) {
				var inputHtmlStr = '<input id="nodePort' + i + '" style="width:90%;margin-left: 10px" type="text" value="part' + (i + 1) + '" />';
				$('#nodePortEditer').append(inputHtmlStr);
			}
		} else {
			$('#nodePortEditer').empty();
			for ( var i = 0; i < num; i++) {
				var inputHtmlStr = '<input id="nodePort' + i + '" style="width:90%;margin-left: 10px" type="text" value="' + nodedata.bottomArray[i].text + '" />';
				$('#nodePortEditer').append(inputHtmlStr);
			}
		}
	});

	// 出口数改变改变文本框的个数
	function isPortNumChanged(_thisGraphObj) {
		$('#nodePortEditer').empty();
		appendPortHtml(_thisGraphObj);
	}

	// 添加文本输入框
	function appendPortHtml(subject) {
		$('#nodePortEditer').empty();
		var nodedata = subject.part.data;
		var length = nodedata.bottomArray.length;
		for ( var i = 0; i < length; i++) {
			var inputHtmlStr = '<input id="nodePort' + i + '" style="width:90%;margin-left: 10px" type="text" value="' + nodedata.bottomArray[i].text + '" />';
			$('#nodePortEditer').append(inputHtmlStr);
		}
	}

	// 更新节点
	function updateNode() {
		if (public_thisGraphObj == null)
			return;
		var node = public_thisGraphObj;
		if (!(node.part instanceof go.Link)) {
			var keys = public_thisGraphObj.part.key;
			var nodeData = myDiagram.model.findNodeDataForKey(keys);
			node = myDiagram.findNodeForKey(keys);
			if (nodeData.category == "Conditional") {
				$('#myEditDiv').hide();
				var content = $('#myEditDiv #content').val();
				myDiagram.model.setDataProperty(nodeData, 'text', content);
				var nodeOutputNum = $('#myEditDiv #outputNum').val();
				var bottomArray = [];
				var linkArr = node.findLinksOutOf();
				// if (nodeOutputNum != nodeData.bottomArray.length) {
				for ( var i = 0; i < nodeOutputNum; i++) {
					var htmlId = '#nodePort' + i;
					bottomArray.push({
						'text' : $(htmlId).val()
					});
				}
				myDiagram.model.setDataProperty(nodeData, 'bottomArray', bottomArray);
				// }
				// go.removeLinks(linkArr,nodeData);
			} else if (nodeData.category == "Judge") {
				$('#myDiamondEditDiv').window('close');
				var content = $('#scenarioselementname').combobox("getValue");
				myDiagram.model.setDataProperty(nodeData, 'text', content);
			} else if (nodeData.category == "Normal") {
				$('#myNormalEditDiv').window('close');
				var responsetype = $('#responsetype').combobox('getValue');
				var response = UM.getEditor('myEditor').getContent().replaceAll(/<p>/g, "").replaceAll(/<\/p>/g, "");
				if (response.endWith("<br/>")) {
					response = response.substring(0, response.lastIndexOf("<br/>"));
				}
				if (responsetype == "0" || responsetype == "-1") {// 文本型
					// ruleresponsetemplate = '提示用户("'+response+'")';
//					if(!response.match('命中问题')){
//						response='命中问题(&quot;/&lt;@'+response+'/&gt;&quot;)';
//					}
					response = $('#serviceinfotemplate').textbox('getText') + $.trim(response);
					ruleresponsetemplate = $('#serviceinfotemplate').textbox('getText') + $.trim(response);
					if (ioa == '在线教育->尚德机构->多渠道应用') {
						var index = response.lastIndexOf(");");
						if (index > -1) {
							response = response.substring(index + 2, response.length);
						}
					}
				} else if (responsetype == "1") {
					content = UM.getEditor('myEditor').getContentTxt();
					response = $.trim(content);
					ruleresponsetemplate = '提示用户("' + response + '")';
				} else if (responsetype == "2") {// 菜单询问
					if (ruleresponsetemplate.indexOf("菜单询问") == -1) {
						$.messager.alert('提示', "回复类型和答案格式不一致，请重新选择回复类型!", "warning");
						return;
					}
				}
				if (responsetype == "3") {
					content = UM.getEditor('myEditor').getContentTxt();
					response = $.trim(content);
					ruleresponsetemplate = response;
				}
				// 用作展示数据
				var responseForShow = response;
				// 特殊字符转化
				responseForShow = HTMLDecode(responseForShow);
				myDiagram.model.setDataProperty(nodeData, 'text', responseForShow);
				// 用作数据库插入数据
				myDiagram.model.setDataProperty(nodeData, 'response', response);
				myDiagram.model.setDataProperty(nodeData, 'responsetype', responsetype);
				myDiagram.model.setDataProperty(nodeData, 'ruleresponsetemplate', ruleresponsetemplate);
			} else if (nodeData.category == "Start") {
				$('#myStartEditDiv').window('close');
				var content = $('#myStartEditDiv #content').val();
				myDiagram.model.setDataProperty(nodeData, 'text', content);
			}
		}
		if (node.part instanceof go.Link) {
			// 获取线的from对应的节点key
			var key = node.part.data.from;
			// 找到相应的节点并获取节点值
			var nodedata = publicDiagram.model.findNodeDataForKey(key);
			// 获取节点内容
			var nodetext = nodedata.text;
			var content;
			if (node.part.data.from.match("Judge")) {
				$('#elementvalueedit_w').window('close');
				for ( var i = 1; i < MAX_SCENARIO_COUNT; i++) {
					var name = $("#selrule0name" + i).html();
					name = name.substring(0, name.length - 1);
					if (name == nodetext) {
						content = $("#selrule0value" + i).combobox('getText');
						break;
					}
				}
				if (content.indexOf('|||') > 0 || content.indexOf('and') > 0 || content.indexOf(':') > 0 || content.indexOf('：') > 0) {
					$.messager.alert('警告', "当前场景要素值不允许有【|||】、【and】、【:】!", "error");
					return;
				}
				myDiagram.model.setDataProperty(node.part.data, 'text', content);
			}
		}
	}

	buttonClick();
	function buttonClick() {
		// 保存
		$("#myEditDiv #saveBt").click(function() {
			updateNode();
		});

		// 取消
		$("#myEditDiv #cancelBt").click(function() {
			$("#myEditDiv").hide();
		});

		// 保存
		$("#myDiamondEditDiv #saveBt").click(function() {
			updateNode();
		});

		// 取消
		$("#myDiamondEditDiv #cancelBt").click(function() {
			$("#myDiamondEditDiv").window('close');
		});

		// 保存
		$("#myNormalEditDiv #saveBt").click(function() {
			updateNode();
		});

		// 取消
		$("#myNormalEditDiv #cancelBt").click(function() {
			$("#myNormalEditDiv").window('close');
		});
		
		// 保存
		$("#elementedit_w #saveBt").click(function() {
			var flag = submitElementNameForm();
			if (flag) {// 如果有必填项数据未填写，不更新节点
				updateNode();
			}
		});
		
		// 保存
		$("#myStartEditDiv #saveBt").click(function() {
			updateNode();
		});

		// 取消
		$("#myStartEditDiv #cancelBt").click(function() {
			$("#myStartEditDiv").window('close');
		});

		// 保存
		$('#linkbtnsave').click(function() {
			updateNode();
		});

		// 取消关闭窗口
		$('#linkbtncancel').click(function() {
			$("#elementvalueedit_w").window('close');
		});

		// 保存数据
		$('#saveRule0').click(function() {
			updateNode();
		});

		// 撤销操作
		$('#undoBtSubmit').click(function() {
			myDiagram.model.undoManager.undo();
		});
	}
	
	// 打开语义场景关系页面
	$('#searchBtSubmit').click(function() {
		openScenarios();
	});
	
	// 打开问题也是编辑页面
	$('#editElementBtn').click(function() {
		openElement();
	});
	
//	function openScenarios() {
//	    $("#scenarios_window").empty();
//	    var h = $(window).height() * 0.9;
//	    var w = $(window).width() * 0.9;
//	    $('#scenarios_window').panel('resize', {
//	        width : w,
//	        height : h,
//	    });
//	    var url = "./scenarios.html?scenariosid=" + publicscenariosid + '&scenariosname=' + publicscenariosname + '&ioa=' + ioa;
//	    $.ajax({
//			url : '../interactiveScene.action',
//			type : "post",
//			data : {
//				type : 'getcustomer',
//				userid : userid,
//				ioa : ioa
//			},
//			async : false,
//			dataType : "json",
//			success : function(data, textStatus, jqXHR) {
//				var url;
//				if (data.customer == "全行业") {
//					url = "./scenarios.html?scenariosid=" + publicscenariosid + '&scenariosname=' + publicscenariosname + '&ioa=' + ioa + '&strategy=' + strategy;
//				} else {//非全行业，查询配置
//					$.ajax( {
//						url : '../interactiveScene.action',
//						type : "post",
//						data : {
//							type : 'getUrl',
//							ioa : ioa
//						},
//						async : false,
//						dataType : "json",
//						success : function(data, textStatus, jqXHR) {
//							url = data.url + "?scenariosid=" + publicscenariosid + '&scenariosname=' + publicscenariosname + '&ioa=' + ioa;
//						}
//					});
//				}
//			}
//		});
//	    var content = '<iframe scrolling="auto" frameborder="0"  src="' + url + '" style="width:100%;height:100%;"></iframe>';
//	    var $win = $("#scenarios_window").window({
//	        modal : true,
//	        content : content,
//	        title : '场景语义对应关系',
//	        collapsible : false,
//	        maximizable : true,
//	        minimizable : false,
//	        onResize: function () {
//	            $(this).window('center');
//	        }
//	    });
//	    $win.window('open');
//	    loadSceneRelation();
//	}

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

	// 提交数据
	$('#saveBtSubmit').click(function() {
		//savaAndSubmit();
		savaAndSubmitNoVerification();
	});

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
		// 校验不允许矩形到矩形的直连
		for (var i = 0; i < linkDataArray.length; i++) {
			var from = linkDataArray[i].from;
			var to = linkDataArray[i].to;
			if (from.match("Normal") && to.match("Normal")) {
				var fromText = '';
				var toText = '';
				for ( var j = 0; j < nodeDataArray.length; j++) {
					var nodeData = nodeDataArray[j];
					var key = nodeData.key.toString();
					if (key == from) {
						fromText = nodeData.text
					}
					if (key == to) {
						toText = nodeData.text
					}
				}
				$.messager.alert('警告', "不允许矩形【" + fromText + "】到矩形【" + toText + "】的直连!", "error");
				return false;
			}
		}
		var ispassverification = true;
		for ( var i = 0; i < nodeDataArray.length; i++) {
			var nodeData = nodeDataArray[i];
			var key = nodeData.key.toString();
			var value = nodeData.text;
			if (key.match("Judge")) {
				// 获取节点值
				for ( var j = 1; j < MAX_SCENARIO_COUNT; j++) {
					// 获取下拉框名
					var lable = $('#selrule0name' + j).html();
					if (lable.match(value)) {
						var comboboxdatas = $('#selrule0value' + j).combobox("getData");
						// 保存下拉框数据
						var comboboxtexts = [];
						for ( var k = 0; k < comboboxdatas.length; k++) {
							comboboxtexts.push(comboboxdatas[k].text);
						}
						for ( var k = 0; k < linkDataArray.length; k++) {
							var from = linkDataArray[k].from;
							if (from == key) {
								var text = linkDataArray[k].text;
								var link = myDiagram.findLinkForData(linkDataArray[k]);
								// 如果下拉框中数据和线上的数据不相同返回错误提示
								if (!comboboxtexts.includes(text)) {
									if (link instanceof go.Link) {
										myDiagram.model.setDataProperty(link.data, "color", "red");
										ispassverification = false;
									}
								} else {
									myDiagram.model.setDataProperty(link.data, "color", "#F8F8F8");
								}
							}
						}
					}
				}
			}
		}
		if (ispassverification) {
			return true;
		}
		return false;
	}
}

// 提交已修改的数据
function commitRevisedData(jsonStr) {
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
			$.messager.alert('系统提示', data.message, "info");
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
	return [new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify), {
				locationSpot : go.Spot.Center,
			} ];
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

// 初始化场景要素下拉框
function initScenariosElementCombobox() {
	$('#scenarioselementname').combobox({
		url : '../saveConfiguration.action?type=getscenarioselementname&scenariosid=' + publicscenariosid,
	});
}

// 隐藏浏览器自带的鼠标右键菜单
function doNothing() {
	window.event.returnValue = false;
	return false;
}

// 监听鼠标右键事件
function listeningRightclickMouseEvents() {
	// 当用户问题右键菜单选择
	$('#startmenu').menu({
		onClick : function(item) {
			if (item.text == "编辑") {
				$('#myStartEditDiv').window('open');
			}
		}
	});
	
	// 当菱形右键菜单选择
	$('#mm').menu({
		onClick : function(item) {
			if (item.text == "编辑") {
				$('#myDiamondEditDiv').window('open');
				// 获取点击事件的节点
				var subject = public_thisGraphObj;
				// 获取节点名
				var name = subject.part.data.text;
				$('#myDiamondEditDiv #scenarioselementname').combobox("setValue", name);
			}
			if (item.text == "新增") {
				flag = 'save';
				clearElementNameForm();
				$('#elementedit_w').panel({ title : "新增场景要素" });
				$('#elementedit_w').window('open');
			}
			if (item.text == "修改") {
				flag = 'update';
				$('#elementedit_w').panel({ title : "修改场景要素" });
				$('#elementedit_w').window('open');
				editElement_gojs();
			}
			if (item.text == "删除") {
				deleteElement();
			}
			if (item.text == "添加交互要素") {
				addInteractiveElement();
			}
		}
	});

	// 当线右键菜单选择
	$('#linkmenu').menu({
		onClick : function(item) {
			if (item.text == "编辑") {
				flag = 'save';
				$('#elementvalueedit_w').window('open');
				// 加载交互规则的查询和添加下拉框
				loadRule0Combobox();
			}
		}
	});

	// 当回复内容右键菜单选择
	$('#normalmenu').menu({
		onClick : function(item) {
			var subject = public_thisGraphObj;
			if (item.text == "编辑") {
				$('#myNormalEditDiv').window('open');
				// 添加回复内容的回复类型下拉框onselect事件
				responseTypeOnSelect();
				createServiceInfoCombobox2();
				clearRule0Form();
				editRule0();
			}
		}
	});
}

// 加载问题要素页面的优先级下拉框
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
			$('#weightcombobox').combobox({
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

// 建立wordclass父类下拉框
function createWordClassCombobox() {
	$('#wordclasstextbox').combobox({
		url : '../interactiveScene.action?type=createwordclasscombobox&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 构造业务信息列下拉框
function createServiceInfoCombobox() {
	$('#infotalbepath').combobox({
		url : '../interactiveScene.action?type=createserviceinfocombobox&scenariosid=' + publicscenariosid + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 获得地市信息comboxtree
function getCityTree(cityname) {
	// var city = "南京市,合肥市,江苏省,北京市";
	$('#city').combotree({
		url : '../getCityTreeByLoginInfo.action',
		editable : false,
		multiple : true,
		queryParams : {
			local : cityname
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
	var weightData = $('#weightcombobox').combobox('getData');
	if (weightData.length > 0) {
		$('#weightcombobox').combobox('select', weightData[0].text);
	} else {
		$('#weightcombobox').combobox('select', "");
	}
	$("#wordclasstextbox").combobox('setValue', "");
	$("#infotalbepath").combobox('setValue', "");
	$("#city").combotree('clear');
	var itemmodeData = $('#itemmode').combobox('getData');
	$('#itemmode').combobox('select', itemmodeData[0].value);
	var containerData = $('#container').combobox('getData');
	$('#container').combobox('select', containerData[0].value);
	$("#interpat").val("");
}

function openElement() {
	$('#elementnamedatagridDiv').window('open');
	$("#selelementname").textbox('setValue', "");
	loadElementName();
}

//查询场景要素
function searchElementName() {
	$('#elementnamedatagrid').datagrid('load', {
		type : 'selectelementname',
		name : replaceSpace($("#selelementname").val()),
		scenariosid:publicscenariosid
	});
}

// 加载场景要素列表
function loadElementName() {
	$("#elementnamedatagrid").datagrid({
		title : '场景要素显示区',
		url : '../interactiveScene.action',
		width : 900,
		height : 395,
		toolbar : "#elementnamedatagrid_tb",
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectelementname',
			name : replaceSpace($("#selelementname").val()),
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
					title : '地市名称',
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
					title : '归属',
					width : 60,
					formatter : function(value, row, index) {
						if (value != "" && value != null) {
							value = value.replace(/\</g, "&lt;");
							value = value.replace(/\>/g, "&gt;");
							if (value == '词模匹配'){
								value='系统反问';
							} else if (value == '键值补全'){
								value = '菜单询问用户';
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
					var a = '<a class="icon-delete btn_a" title="删除" onclick="deleteElementName(event,'
						+ id
						+ ','
						+ weight
						+ ',\''
						+ name + '\')"></a>';
					var b = '<a href="javascript:void(0)" title="查看统计关联" class="icon-search btn_a" onclick="checkColumnStatisticInfo('
						+ weight
						+ ')"></a> ';
					return b+a;
					}
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
				flag = "save";
				$('#elementedit_w').window('open');
				getCityTree("");
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
		}, "-", {
			text : "添加交互要素",
			iconCls : "icon-add",
			handler : function() {
				addInteractiveElement(); 
			}
		}, "-"]
	});
}

//查看统计关联
function checkColumnStatisticInfo(column) {
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getColumnStatisticInfo',
			serviceid : publicscenariosid,
			sign : 'RELATIONSCENARIOSID',
			column : column
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$('#statisticInfo0Check').html(column);
			if ('空' != data.rows) {
				$('#statisticInfo1Check').html(data.rows[0].statisticsCount);
				$('#statisticInfo2Check').html(data.rows[0].statisticsObj);
				$('#statisticInfo3Check').html(data.rows[0].statisticsObjValue);
				$('#statisticInfo5Check').html(data.rows[0].minValue);
				$('#statisticInfo6Check').html(data.rows[0].maxValue);
			} else {
				$('#statisticInfo1Check').html(' ');
				$('#statisticInfo2Check').html(' ');
				$('#statisticInfo3Check').html(' ');
				$('#statisticInfo5Check').html(' ');
				$('#statisticInfo6Check').html(' ');
			}
			$('#statisticInfo_check').window('open');
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//增加统计关联
function addStatisticInfo2(){
	$('.btd-display3add').css("display","none");
	$('.btd-display4add').css("display","none");
	$('#statisticInfoValue12').combobox({
		valueField :'id',    
	    textField : 'text',
	    panelHeight : 'auto',
	    width: 200,
		height : 22,
	    editable : false,
	    data: [{
			id : 'add',
			text : '累加'
		}]
	});
	$('#statisticInfoValue52').numberbox({
		width : 200,
		height : 22,
		value : 0,
		min : 0
	});
	$('#statisticInfoValue62').numberbox({
		width : 200,
		height : 22,
		min : 0
	});
	$('#statisticInfoValue62').numberbox('clear');
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
			var statisticInfoValue2Data = [];
			for (var i = 0; i < info.length; i++) {
				var name = info[i]["key"];
				var weight = info[i]["weight"];
				var words = info[i]["words"];
				var type = info[i]["value"];
				var item = {};
				item.id = weight;
				item.text = name;
				statisticInfoValue2Data.push(item);
			}
			$('#statisticInfoValue22').combobox({
				valueField : 'id',    
			    textField : 'text',
			    panelHeight : 'auto',
			    width : 200,
				height : 22,
			    editable : false,
			    data : statisticInfoValue2Data,
			    onSelect:function(){
					var num = $('#statisticInfoValue22').combobox('getValue');
					if ('自定义' == info[num-1]["value"]) {
						$('.btd-display3add').css("display", "");
						$('.btd-display4add').css("display", "none");
						$("#statisticInfoValue32").textbox({
							multiline : true,
							width : 200,
							height : 100
						});
						$("#statisticInfoValue32").textbox('clear');
						$("#statisticInfoValue42").combobox('clear');
					} else {
						$('.btd-display3add').css("display", "none");
						$('.btd-display4add').css("display", "");
						$("#statisticInfoValue42").combobox({
							width : 200,
							height : 22,
							editable : false,
							valueField : 'id',    
					        textField : 'text',
					        data : words
						});
						$("#statisticInfoValue32").textbox('clear');
						$("#statisticInfoValue42").combobox('clear');
					}
				}
			});
			$("#statisticInfoValue32").textbox({
				multiline : true,
				width : 200,
				height : 100
			});
			$("#statisticInfoValue42").combobox({
				width : 200,
				height : 22,
				editable : false,
				valueField : 'id',    
		        textField : 'text'
			});
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
	$('#statisticInfo_add2').window('open');
}

//保存统计关联
function saveStatisticInfo2() {
	var columnnum = $("#statisticInfo0Check").html();
	var statisticsCount = $("#statisticInfoValue12").combobox('getText');
	var statisticsObj = $("#statisticInfoValue22").combobox('getText');
	var statisticsObjValue = $("#statisticInfoValue32").textbox('getText') + $("#statisticInfoValue42").combobox('getText');
	var minValue = $("#statisticInfoValue52").numberbox('getValue');
	var maxValue = $("#statisticInfoValue62").numberbox('getValue');
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			serviceid : publicscenariosid,
			type : 'savestatisticinfo',
			sign : 'RELATIONSCENARIOSID',
			column : columnnum,
			statisticsCount : statisticsCount,
			statisticsObj : statisticsObj,
			statisticsObjValue : statisticsObjValue,
			minValue : minValue,
			maxValue : maxValue
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$('#statisticInfo_check').window('close');
			$('#statisticInfo_add2').window('close');
			$.messager.alert('系统提示', "保存成功!", "info");
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}

//删除统计关联
function deleteColumnStatisticInfo() {
	var columnnum = $("#statisticInfo0Check").html();
	$.messager.confirm('提示', '确定删除该统计关联吗?', function(r) {
		if (r) {
			$.ajax({
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deleteColumnStatisticInfo',
					serviceid : publicscenariosid,
					column : columnnum,
					sign : 'RELATIONSCENARIOSID',
					resourcetype : 'scenariosrules',
					operationtype : 'D',
					resourceid : publicscenariosid
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$('#statisticInfo_check').window('close');
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
//						$("#attrnamedatagrid").datagrid("reload");
//						loadCombobox();
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					$.messager.alert('系统异常', "请求数据失败!", "error");
				}
			});
		}
	});
}

//编辑场景要素
function editElement(){
	var row = $('#elementnamedatagrid').datagrid('getSelected');
	if (row) {
		$('#elementedit_w').window('open');
		getCityTree(row.cityname);
		createServiceInfoCombobox();
		createWordClassCombobox();	
		loadWeightCombobox(row.weight);
		$("#elementnametextbox").textbox('setValue', row.name);
		$("#wordclasstextbox").combobox('setValue', row.wordclass);
		$("#weightcombobox").combobox('setValue', row.weight);
		$("#infotalbepath").combobox('setValue', row.infotalbepath);
		$("#itemmode").combobox('setValue', row.itemmode);
		$("#container").combobox('setValue', row.container);
		$("#interpat").val(row.interpat);
		scenarioselementidForUpdate = row.scenarioselementid;
		oldweightForUpdate = row.weight;
		oldscenarioselementname = row.name;
	} else {
		$.messager.alert('提示', "请选择需编辑行!", "warning");
		return;
	}
}

//编辑问题要素
function editElementName(flag) {
	var type;
	var scenarioselementid;
	if (flag == "save") {// 新增操作
		type = 'insertscenarioselement';
		oldweight = '';
	} else if (flag == "update") {// 修改操作
		type = 'updatescenarioselement';
		scenarioselementid = scenarioselementidForUpdate;
		oldweight = oldweightForUpdate;
	}
	var name = replaceSpace($("#elementnametextbox").val());
	var wordclass = $("#wordclasstextbox").combobox('getText');
	var weight = $("#weightcombobox").combobox('getText');
	var infotalbepath = replaceSpace($("#infotalbepath").combobox('getText'));
	var cityname = $("#city").combotree('getText');
	var city = $("#city").combotree('getValues');
	var itemmode = $("#itemmode").combobox('getText');
	var container = $("#container").combobox('getValue');
	var interpat = $("#interpat").val();

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
		name : name,
		weight : weight,
		oldweight : oldweight,
		wordclass : wordclass,
		infotalbepath : infotalbepath,
		city : city + "",
		cityname : cityname,
		itemmode : itemmode,
		container : container,
		interpat : interpat,
		scenarioselementid : scenarioselementid
	}

	$.ajax({
		url : '../saveConfiguration.action',
		type : "post",
		data : dataStr,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#elementnamedatagrid").datagrid("reload");
				clearElementNameForm();
				$('#elementedit_w').window('close');
				// 加载交互规则
//				loadRule0Combobox();
//				loadRule0();
//				s_loadRule0Combobox();
//				s_loadRule0();
				// 更新修改节点名称
				var nodeKey = '';
				var nodeDataArray = publicDiagram.model.nodeDataArray;
				for (var i = 0; i < nodeDataArray.length; i++) {
					var nodeData = nodeDataArray[i];
					var key = nodeData.key.toString();
					var value = nodeData.text;
					if (key.match("Judge") && value == oldscenarioselementname) {
						nodeKey = key; 
						break;
					}
				}
				if (nodeKey != '') {
					var nodeData = publicDiagram.model.findNodeDataForKey(nodeKey);
					publicDiagram.model.setDataProperty(nodeData, 'text', name);
				}
			}
		}
	});
}

// 编辑问题要素
function editElementName_gojs(flag) {
	var type;
	var scenarioselementid;
	if (flag == "save") {// 新增操作
		type = 'insertscenarioselement';
		oldweight = '';
	} else if (flag == "update") {// 修改操作
		type = 'updatescenarioselement';
		scenarioselementid = scenarioselementidForUpdate;
		oldweight = oldweightForUpdate;
	}
	var name = replaceSpace($("#elementnametextbox").val());
	var wordclass = $("#wordclasstextbox").combobox('getText');
	var weight = $("#weightcombobox").combobox('getText');
	var infotalbepath = replaceSpace($("#infotalbepath").combobox('getText'));
	var cityname = $("#city").combotree('getText');
	var city = $("#city").combotree('getValues');
	var itemmode = $("#itemmode").combobox('getText');
	var container = $("#container").combobox('getValue');
	var interpat = $("#interpat").val();

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
		name : name,
		weight : weight,
		oldweight : oldweight,
		wordclass : wordclass,
		infotalbepath : infotalbepath,
		city : city + "",
		cityname : cityname,
		itemmode : itemmode,
		container : container,
		interpat : interpat,
		scenarioselementid : scenarioselementid
	}

	$.ajax({
		url : '../saveConfiguration.action',
		type : "post",
		data : dataStr,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				clearElementNameForm();
				$('#elementedit_w').window('close');
				// 重新加载场景要素名下拉框
				$('#scenarioselementname').combobox("reload");
				$("#scenarioselementname").combobox('setValue', name);// 更新节点名称
			}
		}
	});
}

// 替换字符串中所有空格
function replaceSpace(str) {
	if (str != null && str != '') {
		str = str.replace(new RegExp(' ', 'g'), '');
	}
	return str;
}

// 根据场景要素名获取场景相关信息
function getScenariosElementByName(name) {
	$.ajax({
		url : '../saveConfiguration.action',
		type : 'post',
		dataType : 'json',
		data : {
			type : 'getscenarioselementname',
			scenariosid : publicscenariosid,
			name : name
		},
		success : function(data) {
			// 如果下拉列表中包含当前节点名继续否则抛出提示框返回
			if (data.length == 0) {
				$.messager.alert('提示', '此场景要素不存在，不能修改！', 'info');
				$('#elementedit_w').window('close');
				return;
			}
			var row = data[0];
			if (row) {
				$('#elementedit_w').window('open');
				getCityTree(row.cityname);
				createServiceInfoCombobox();
				createWordClassCombobox();
				loadWeightCombobox(row.weight);
				$("#elementnametextbox").textbox('setValue', row.name);
				$("#wordclasstextbox").combobox('setValue', row.wordclassid);
				$("#weightcombobox").combobox('setValue', row.weight);
				$("#infotalbepath").combobox('setValue', row.infotalbepath);
				$("#itemmode").combobox('setValue', row.itemmode);
				$("#container").combobox('setValue', row.container);
				$("#interpat").val(row.interpat);
				scenarioselementidForUpdate = row.scenarioselementid;
				oldweightForUpdate = row.weight;
			} else {
				$.messager.alert('提示', "请选择需编辑场景要素!", "warning");
				return;
			}
		}
	});
}

// 编辑场景要素
function editElement_gojs() {
	// 获取点击事件的节点
	var subject = public_thisGraphObj;
	// 获取节点名
	var name = subject.part.data.text;

	// 根据名称获取场景要素全部内容
	var ScenariosElementDatas = []
	getScenariosElementByName(name);
}

//删除场景要素
function deleteElementName(event, id, weight, name) {
	if (event.stopPropagation) {// Mozilla and Opera
		event.stopPropagation();
	} else if (window.event) {// IE
		window.event.cancelBubble = true;
	}
	$.messager.confirm('提示', '确定删除场景要素信息吗?', function(r) {
		if (r) {
			$.messager.confirm('提示', '删除不可恢复，请确认是否删除?', function(r) {
				if (r) {
					var flag = true;
					var nodeDataArray = publicDiagram.model.nodeDataArray;
					for (var i = 0; i < nodeDataArray.length; i++) {
						var nodeData = nodeDataArray[i];
						var key = nodeData.key.toString();
						var value = nodeData.text;
						if (key.match("Judge") && value == name) {
							flag = false;
							break;
						}
					}
					if (flag) {
						$.ajax({
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
								if (data.success == true) {
									$("#elementnamedatagrid").datagrid("reload");
									loadWeightCombobox("");
									// 加载交互规则
	//								loadRule0Combobox();
	//								loadRule0();
	//								s_loadRule0Combobox();
	//								s_loadRule0();
								}
								$.messager.alert('提示', data.msg, "info");
							}
						});
					} else {
						var msg = "当前场景要素【" + name + "】在流程图中被使用，请先解除当前场景要素【" + name + "】再删除!";
						$.messager.alert('提示', msg, "warning");
					}
				}
			});
		}
	});
}

function deleteElement() {
	// 获取点击事件的节点
	var subject = public_thisGraphObj;
	// 获取节点名
	var name = subject.part.data.text;
	if (name == "" || name == null) {
		$.messager.alert('提示', "当前场景要素为空，请重新选择场景要素!", "warning");
		return false;
	}
	// 先获取场景要素信息
	$.ajax({
		url : '../saveConfiguration.action',
		type : 'post',
		dataType : 'json',
		async : false,
		data : {
			type : 'getscenarioselementname',
			scenariosid : publicscenariosid,
			name : name
		},
		success : function(data) {
			// 如果下拉列表中包含当前节点名继续否则抛出提示框返回
			if (data.length == 0) {
				$.messager.alert('提示', '此场景要素不存在，不能删除！', 'info');
				return false;
			}
			var row = data[0];
			if (row) {
				scenarioselementidForUpdate = row.scenarioselementid;
				oldweightForUpdate = row.weight;
				$.messager.confirm('提示', '确定删除场景要素信息吗?', function(r) {
					if (r) {
						$.messager.confirm('提示', '删除不可恢复，请确认是否删除?', function(r) {
							if (r) {
								$.ajax({
									url : '../saveConfiguration.action',
									type : 'post',
									dataType : 'json',
									async : false,
									data : {
										type : 'deletescenarioselement',
										scenarioselementid : scenarioselementidForUpdate,
										name : name,
										weight : oldweightForUpdate,
										scenariosid : publicscenariosid,
										scenariosName : publicscenariosname
									},
									success : function(data) {
										$.messager.alert('提示', data.msg, "info");
										if (data.success == true) {
											// 重新加载场景要素名下拉框
											$('#scenarioselementname').combobox("reload");
											$("#scenarioselementname").combobox('setValue', '');// 更新节点名称
											loadWeightCombobox('');
											// 更新当前删除的 节点
											var keys = public_thisGraphObj.part.key;
											var nodeData = publicDiagram.model.findNodeDataForKey(keys);
											var content = $('#scenarioselementname').combobox("getValue");
											publicDiagram.model.setDataProperty(nodeData, 'text', content);
										}
									}
								});
							}
						});
					}
				});
			} else {
				$.messager.alert('提示', "请选择需删除场景要素!", "warning");
				return false;
			}
		}
	});
}

// 加载交互规则的查询和添加下拉框
function loadRule0Combobox() {
	// 如果场景要素名存在显示相应的下拉框
	var subject = public_thisGraphObj;
	// 获取线的节点数据
	var subjectData = subject.part.data;
	// 获取线的from对应的节点key
	var key = subjectData.from;
	// 找到相应的节点并获取节点值
	var nodedata = publicDiagram.model.findNodeDataForKey(key);
	// 获取节点内容
	var nodetext = nodedata.text;
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
			var info = data.rows;
			for ( var i = 0; i < info.length; i++) {
				robotMap[info[i]["id"]] = info[i]["text"];
			}
		}
	});

	for ( var i = 1; i < MAX_SCENARIO_COUNT; i++) {
		$("#selrule0name" + i).html("");
		$("#selrule0" + i).hide();
		$("#addrule0name" + i).html("");
		$("#addrule0" + i).hide();
	}
	$.ajax({
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
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				if (name == 'robotid') {
					robot = i;
				}
				if (name.indexOf("_知识名称") != -1) {
					name = name.split("_")[1];
				}
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				if (nodetext == name) {
					$("#selrule0name" + weight).html(name + ":");
					$("#selrule0" + weight).show();
				}
				$("#selrule0value" + weight).combobox({
					valueField : 'id',
					textField : 'text',
					editable : true,
					data : elementvalue
				});
				$("#selrule0value" + weight).combobox("setText", subjectData.text);
			}
		}
	});
}

//修改交互规则操作，将值放入编辑区
function editRule0() {
	// 如果场景要素名存在显示相应的下拉框
	var subject = public_thisGraphObj;
	// 获取对应的节点key
	var key = subject.part.data.key;
	// 找到相应的节点并获取节点值
	var nodedata = publicDiagram.model.findNodeDataForKey(key);
	// 获取节点内容
	var nodetext = nodedata.text;
	if (nodetext == '' || nodetext == null) {
		$.messager.alert('提示', "当前回复内容为空，请重新选择!", "warning");
		return;
	}
	var	ruleresponsetemplate = nodedata.ruleresponsetemplate;
	var	ruleresponse = nodedata.response;
	var responsetype = nodedata.responsetype;
	if (ioa.indexOf("IVR") > -1 && responsetype == '0') {
		responsetype = '-1';
	}
	$("#responsetype").combobox("setValue", responsetype);
	if (responsetype == "2") {
		$("#triggerAction_btn").hide();
		$("#hitquestion_btn").hide();
		$("#interactiveoptions_btn").show();
		$(".serviceinfo_tr").css("display","none");
	} else {
		if (ioa == '在线教育->尚德机构->多渠道应用') {
			$("#triggerAction_btn").show();
		}
		$("#hitquestion_btn").show();
		$("#interactiveoptions_btn").hide();
		$(".serviceinfo_tr").css("display","");
	}
	$("#span_addrule0weight").hide();
	$("#input_addrule0weight").hide();
		
	var answer = "";
	if (responsetype == "2"){
		answer = ruleresponse;
	} else {
		answer = ruleresponsetemplate;
	}
		
	if(answer == null){
		answer = "";
	}
	//给富文本框赋值，不追加
	UM.getEditor('myEditor').setContent(answer, false);
		
	if (responsetype == "0") {
		var content = "";
		var serviceinfo = "";
		var knoname = "";
		var serviceinfotemplate = "";
		var attrname = "";
		var attrvalue = "";
		if (ruleresponsetemplate.indexOf(";") != -1) {
			var arry = ruleresponsetemplate.split(";");
			if (arry[0].indexOf("信息表") != -1) {
				//给富文本框赋值，不追加
				UM.getEditor('myEditor').setContent(answer.substring(answer.indexOf(";") + 1, answer.length ), false);
				if (arry.length > 1) {
					content = arry[0] + ";"; 
					serviceinfotemplate = arry[0];
				    var tmp = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
				    serviceinfo = tmp.split(",")[0];
				    knoname = tmp.split(",")[2];
				    serviceinfo = serviceinfo.substring(1, serviceinfo.length - 1);
				    knoname = knoname.substring(1, knoname.length - 1);
				    attrname = knoname.split("|")[0];
				    attrvalue = knoname.split("|")[1];
				}
				var serviceinfoData = $("#serviceinfo").combobox("getData");
				var serviceid = "";
				for (var i = 0; i < serviceinfoData.length; i++) {
					if (serviceinfoData[i].text === serviceinfo) {
						serviceid = serviceinfoData[i].id;
					}
				}
				$("#serviceinfo").combobox("select", serviceid);
				$("#attrname").combobox("setValue", attrname);
				$("#attrvalue").combobox("setValue", attrvalue);
				$("#serviceinfotemplate").textbox("setValue", content);
			}
		}
	}
		
	if (responsetype == "2") {
		UM.getEditor('myEditor').setDisabled('fullscreen');
	} else {
		UM.getEditor('myEditor').setEnabled();
	}
}

// 添加回复类型onselect 事件
function responseTypeOnSelect() {
	$("#responsetype").combobox({
		editable : false,
		valueField : 'id',
		textField : 'text',
		data : [ {
			"id" : "0",
			"text" : "文本型"
		}, {
			"id" : "2",
			"text" : "菜单询问"
		}, {
			"id" : "-1",
			"text" : "手写规则"
		} ],
		onSelect : function(n) {
			var selectOption = $("#responsetype").combobox('getValue');
			if (selectOption == "2") {
				$("#interactiveoptions_btn").show();
				$("#templatecolumn_btn").hide();
				$("#triggerAction_btn").hide();
				$("#hitquestion_btn").hide();
				$(".serviceinfo_tr").css("display", "none");
				UM.getEditor('myEditor').setDisabled('fullscreen');
			} else {
				$("#interactiveoptions_btn").hide();
				$("#templatecolumn_btn").show();
				if (ioa == '在线教育->尚德机构->多渠道应用') {
					$("#triggerAction_btn").show();
				}
				$("#hitquestion_btn").show();
				$(".serviceinfo_tr").css("display", "");
				UM.getEditor('myEditor').setEnabled();
			}
		}
	});
}

// 清空缺失补全规则表单
function clearRule0Form() {
	$('#rule0form').form('clear');
	if (ioa.indexOf("IVR") > -1) {
		$("#responsetype").combobox('setValue', '-1');
	} else {
		$("#responsetype").combobox("setValue", "0");
	}
	$("#interactiveoptions_btn").hide();
	$("#templatecolumn_btn").show();
	if (ioa == '在线教育->尚德机构->多渠道应用') {
		$("#triggerAction_btn").show();
	} else {
		$("#triggerAction_btn").hide();
	}
	$("#hitquestion_btn").show();
	$("#addrule0weight").combobox("setValue", "");
	$("#serviceinfo").combobox("clear");
	UM.getEditor('myEditor').setContent("", false);
	UM.getEditor('myEditor').setEnabled();
	$("#span_addrule0weight").hide();
	$("#input_addrule0weight").hide();
	$('#adduserquestion').textbox('setValue', '');
	$('#addcurrentnode').textbox('setValue', '');
}

// 根据业务信息列下拉框
function createServiceInfoCombobox2() {
	$('#serviceinfo').combobox({
		url : '../interactiveScene.action?type=createserviceinfocombobox&scenariosid=' + publicscenariosid + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px',
		onSelect : function(rec) {
			var _serviceid = rec.id;
			if (_serviceid == "" || _serviceid == null) {
				$.messager.alert('提示', "请选择信息表名称!", "warning");
				return;
			}
			// selectSemanticsKeyword(_serviceid);

			// 加载属性名称下拉数据
			createAttrCombobox(_serviceid);

			// 选择时，重置信息表模板
			var serviceinfo = rec.text;
			var content = '查询("' + serviceinfo + '","","");';
			$("#serviceinfotemplate").textbox('setValue', content);
			$("#serviceinfotemplate").data('serviceinfo', {
				'serviceid' : _serviceid,
				'service' : serviceinfo
			}); // 绑定当前信息表到信息表模板对象上

			$('#attrvalue').combobox('setValue', '');
		},
		onChange : function(newVal, oldVal) {
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

// 加载模板路径下拉数据
function createAttrCombobox(serviceid) {
	var _url = '../interactiveScene.action?type=createattrnamecombobox&serviceid=' + serviceid + '&a=' + Math.random();
	$('#attrname').combobox({
		url : _url,
		valueField : 'id',
		textField : 'text',
		onSelect : function(rec) {
			if (rec.text != '') {
				// 选择时，清空信息表模板
				clearServiceinfotemplate();
				$('#attrvalue').textbox('setValue', '');
				createAttrLabelCombobox(serviceid, rec.text, rec.id);

				serviceinfo = $("#serviceinfo").combobox('getText');
				// 查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
				var content = '查询("' + serviceinfo + '","","' + rec.text + '|' + '<@' + rec.text + '>' + '");';
				$("#serviceinfotemplate").textbox('setValue', content);
				$("#serviceinfotemplate").data('serviceinfo', {
					'serviceid' : serviceid,
					'service' : serviceinfo
				}); // 绑定当前信息表到信息表模板对象上
			}
		}
	});
}

function clearServiceinfotemplate() {
	$("#serviceinfotemplate").textbox('setValue', '').data('serviceinfo', null);
}

// 加载模板内容数据
function createAttrLabelCombobox(serviceid, attrname, colnum) {
	var city = "all";
	// 加载属性标签内容下拉数据
	var _url = '../interactiveScene.action?type=createattrvaluescombobox&serviceid=' + serviceid
			+ '&column=' + colnum + '&city=' + city + '&a=' + Math.random();
	$('#attrvalue').combobox({
		url : _url,
		valueField : 'id',
		textField : 'text',
		groupField : 'service',
		onSelect : function(rec) {
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
			// 查询("信息表名称","","属性名称|属性值");提示用户("答案文本")
			var content = '查询("' + serviceinfo + '","","' + attrname + '|' + attrvalue + '");';
			if (flag != 0) {
				$("#serviceinfotemplate").textbox('setValue', content);
			} else {
				if ($("#attrvalue").combobox('getValue') == "") {
					$("#serviceinfotemplate").textbox('setValue', content);
				}
				flag = 1;
			}
			$("#serviceinfotemplate").data('serviceinfo', {
				'serviceid' : serviceid,
				'service' : serviceinfo
			}); // 绑定当前信息表到信息表模板对象上
		}
	});
}

// 打开触发动作window
function saveTriggerAction() {
	createtriggerActionNameCombobox();
	$('#add_triggerAction').window('open');
	$("#triggerActionName").combobox("setValue", "");
	$("#triggerActionValue01").textbox("setValue", "");
	$("#triggerActionValue02").textbox("setValue", "");
}

// 构造触发动作下拉
function createtriggerActionNameCombobox() {
	$('#triggerActionName').combobox({
		url : '../interactiveScene.action?type=createtriggeractionnamecombobox&scenariosid=' + publicscenariosid + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 保存交互选项
function saveInteractiveOoptions() {
	var arr = [];
	var w = 0;
	var subject = public_thisGraphObj;
	// 获取对应的节点key
	var key = subject.part.data.key;
	var modeldata = publicDiagram.model;
	var nodeDataArray = modeldata.nodeDataArray;
	var linkDataArray = modeldata.linkDataArray;
	var fromkey = '';
	for (var i = 0; i < linkDataArray.length; i++) {
		var to = linkDataArray[i].to;
		if (to == key) {
			var cond = '';
			var linktext = linkDataArray[i].text;
			if (linktext == '<用户未选或未告知,系统提示输入>') {
				cond = '交互';
			} else if (linktext == '<用户告知或已选择，系统可获知>') {
				cond = '已选';
			} else if (linktext == '<用户未选或未告知>'){
				cond = '缺失';
			} else {
				cond = linktext;
			}
			if (cond == "交互") {
				arr.push(cond);
				fromkey = linkDataArray[i].from;
				var nodedata = publicDiagram.model.findNodeDataForKey(fromkey);
				$.ajax({
					url : '../saveConfiguration.action',
					type : 'post',
					dataType : 'json',
					async : false,
					data : {
						type : 'getscenarioselementname',
						scenariosid : publicscenariosid,
						name : nodedata.text
					},
					success : function(data) {
						// 如果下拉列表中包含当前节点名继续否则抛出提示框返回
						if (data.length == 0) {
							w = 0;
						}
						var row = data[0];
						if (row) {
							w = row.weight;
						} else {
							w = 0;
						}
					}
				});
			}
		}
	}
	
	add_clearMenuItems();
	
	if (arr.length == 1) {
	    $("#add_interactivetemplate_div").hide();
	 	$.ajax({
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'getmenuitemsinfo',
				weight : w,
				scenariosid : publicscenariosid
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				var wordclassid = data.wordclassid;
				publicwordclassid = wordclassid;
				var name = data.name;
				if (wordclassid == "" || wordclassid == null) {
					$.messager.alert('提示', "当前规则交互列未配置对应词类!", "warning"); 
				    return false;
				} else {
					$("#add_interactionname_div").show();
					$("#add_correspondingwordclass_div").hide();
					$("#add_correspondingwordclass").val(wordclassid);
					$('#add_menuItems_edit').window('open');
					var itemmode = data.itemmode; 
					if (itemmode == "勾选+自定义") {
						$("#add_interactivetemplate_div").show();	
						$("#add_interactiveoptions_div").show();
					} else if (itemmode == "自定义") {
						$("#add_interactivetemplate_div").show();	
						$("#add_interactiveoptions_div").hide();
					}
					
					var knoElementName;
					if (publicscenariosname.endWith("场景")) {
						knoElementName = publicscenariosname + "_知识名称";
					} else {
						knoElementName = publicscenariosname + "场景_知识名称";
					}
					if (name == knoElementName) {
						$('#add_interactionname').textbox('setValue','知识名称');	
						$("#add_interactiveoptions").combobox('clear');
						$('#img_add').show();
						createInteractiveOptionsCombobox2(wordclassid);
						imgClick();
					} else {
						$('#img_add').hide();
						$('#add_interactionname').textbox('setValue',data.name);	
						createInteractiveOptionsCombobox2(wordclassid);
					}
					
					$('#add_interactivetemplate').val(data.interpat);
					$('#add_customvalue').val('多条值以|分隔开');
					$('#add_customvalue').css('color', '#ACA899');
					// 找到相应的节点并获取节点值
					var nodedata = publicDiagram.model.findNodeDataForKey(key);
					// 获取节点内容
					var nodetext = nodedata.text;
					if (nodetext == '' || nodetext == null) {
						$.messager.alert('提示', "当前回复内容为空，请重新选择!", "warning");
						return;
					}
					var	ruleresponsetemplate = nodedata.ruleresponsetemplate;
					var	ruleresponse = nodedata.response;
					var responsetype = nodedata.responsetype;
					var interactiveoptionsstr=  "";
				    if (responsetype == "2") {
				    	if (ruleresponsetemplate != "" && ruleresponsetemplate != null) {
				    		if (ruleresponse.indexOf("###;") > -1) {
				    			interactiveoptionsstr = ruleresponsetemplate.split('###;')[1].split('","')[1].split('")')[0];
				    	    	$('#add_interactivetemplateend').val(ruleresponsetemplate.split('###;')[1].split('","')[0].split('<@选项文本>')[1]);
				    	    	$('#add_interactivetemplate').val(ruleresponse.split('###;')[1].split('[')[0]);
				    	    } else {
				    	    	interactiveoptionsstr = ruleresponsetemplate.split('","')[1].split('")')[0];
				    	    	$('#add_interactivetemplateend').val(ruleresponsetemplate.split('","')[0].split('<@选项文本>')[1]);
				    	    	$('#add_interactivetemplate').val(ruleresponse.split('[')[0]);
				    	    }
				    	    $('#add_interactiveoptions').combobox('setValues',interactiveoptionsstr.split('||'));
				    	}
				    }
				} 
			}
		});
	} else {
		$("#add_correspondingwordclass").val("");
		$("#add_interactiveoptions_div").hide();
		$("#add_correspondingwordclass_div").hide();
		$("#add_interactionname_div").hide();
		$("#add_interactivetemplate_div").show();
		$('#add_customvalue').val('多条值以|分隔开');
		$('#add_customvalue').css('color', '#ACA899');
		$('#add_menuItems_edit').window('open');
	}
}

//根据交互选项下拉框
function createInteractiveOptionsCombobox2(wordclassid) {
	$("#add_interactiveoptions").combobox({    
	    url:'../interactiveScene.action?type=interactiveoptions&wordclassid=' + wordclassid + '&a=' + Math.random(),    
	    valueField:'id',    
	    textField:'text',
	    panelHeight:'150px',
	    multiple : true, // 支持多选
		separator : '|' ,// 多选的时候用“|”分隔
		editable:true 
	});  
}

//添加插入知识称交互项图片点击事件
function imgClick(){
	$("#img_add").click(function() {
		$("#add_knoname").window('open'); 
		getSearchCityTree("kw_search_city","");
		loadKnoName();
	});
}

//获得地市信息comboxtree
function getSearchCityTree(id, city) {
	var id ="#" + id;
	$(id).combotree({
		url:'../getCityTreeByLoginInfo.action',
		editable:false, 
		multiple:false,
		queryParams:{
			local : city
		}
	}); 
}

//加载知识名称
function loadKnoName(){ 
	var docName   = $(".kw-wrap [name=keyword]").val();
	var serviceid = $('#kw_type').combobox("getValue");
	var city = $('#kw_search_city').combotree("getValue");
	
	$("#kw-knowology-info").datagrid({
		url :  '../getKWDatas.action',
		width : 1000,
		height : 310,
		pagination : true,
		rownumbers : true,
		queryParams : {
			city : city == "全国" ? "" : city,
			serviceid :  serviceid,
			docName : docName ? $.trim(docName).replace(/ +/,"%") : ""
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

function add_clearMenuItems() {
	$('#add_menuItems_editform').form('clear');
	$('#add_interactionname').textbox('setValue', '');
	$('#add_interactiveoptions').combobox('setValue', "");
	$('#add_interactivetemplate').val('');
	$('#add_interactivetemplateend').val('');
	$("#add_customvalue").val('');
}

// 打开命中问题window
function saveHitquestion() {
	// 加载省份下拉列表
	createProvinceCombobox();
	$('#hit_city').combobox('clear');
	provinceChange("全国");
	$('#hitquestion').val('');
	$("#hitquestionresult").val('');
	$('#add_hitquestion').window('open');
}

// 构造业务信息列下拉框
function createProvinceCombobox() {
	$('#hit_province').combobox({
		url : '../interactiveScene.action?type=createprovincecombobox&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px',
		editable : false,
		onSelect : function() {
			var provincecode = $("#hit_province").combobox('getValue');
			provinceChange(provincecode);
		},
		onLoadSuccess : function() {
			var datas = $('#hit_province').combobox('getData');
			if (datas.length > 0) {
				$('#hit_province').combobox('setValue', '全国');
			}
		}
	});
}

// 构造地市下拉列表
function provinceChange(provincecode) {
	$('#hit_city').combobox({
		url : '../interactiveScene.action?type=createcitycombobox&province=' + provincecode + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px',
		editable : false,
		onLoadSuccess : function() {
			var datas = $('#hit_city').combobox('getData');
			if (datas.length > 0) {
				$('#hit_city').combobox('setValue', datas[0].id);
			}
		}
	});
}

// 打开场景元素列添加区
function openElementWin() {
	$("#add_elementcolumn").window('open');
	createElementColumnCombobox();
}

// 构造场景元素列下拉框
function createElementColumnCombobox() {
	$('#elementcolumn').combobox({
		url : '../interactiveScene.action?type=createlementcolumncombobox&scenariosid=' + publicscenariosid + '&a=' + Math.random(),
		valueField : 'id',
		textField : 'text',
		panelHeight : '150px'
	});
}

// 插入场景元素列
function add_elementColumn() {
	var elementcolumn = $("#elementcolumn").combobox('getText');
	if (elementcolumn == "" || elementcolumn == null) {
		return;
	}
	elementcolumn = "<@" + elementcolumn + ">";
	var content = $("#hitquestion").val() + elementcolumn;
	$("#hitquestion").val(content);
	$("#add_elementcolumn").window('close');
}

// 测试问题
function testHitQuestion() {
	$("#hitquestionresult").val("");
	var question = $("#hitquestion").val();
	if (question == "" || question == null || question == "<@列名>**列名") {
		return;
	}
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'testhitquestion',
			question : question,
			province : $("#hit_province").combobox('getText'),
			city : $('#hit_city').combobox('getText')
		},
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (data.success) {
				$("#hitquestionresult").val(data.msg);
			} else {
				$("#hitquestionresult").val("理解失败");
			}
		}
	});
}

// 生成命中问题格式回复文本
function add_hitquestion() {
	var hitquestion = $('#hitquestion').val();
	var hitquestionresult = $('#hitquestionresult').val();
	if (hitquestion == '<@列名>**列名' || hitquestion == "") {
		return;
	} else {
		// if(hitquestionresult==""||hitquestionresult=="理解失败"){
		// $.messager.alert('提示', "当前命中问题理解失败，请重新输入或联系语义维护人员！", "warning");
		// return ;
		// }

		var response = '命中问题("' + hitquestion + '")';
		UM.getEditor('myEditor').setContent(response, false);
		$('#add_hitquestion').window('close');
		$('#hitquestion').val();
	}
}

// 命中问题输入框Onfocus事件
function hitquestionOnfocus(id) {
	var hitquestion = $('#' + id).val();
	if (hitquestion == "<@列名>**列名") {
		$('#' + id).val('');
		$('#' + id).css('color', '#000000');
	}
}

//场景元素列取消操作
function clearElementColumn(){
	$("#add_elementcolumn").window('close');
}

// 命中问题取消操作
function clearHitquestion() {
	$('#hitquestion').val();
	$('#add_hitquestion').window('close');
}

//插入触发动作
function add_triggerAction() {
	var triggerAction = $("#triggerActionName").combobox('getText');
	var triggerActionValue01 = $("#triggerActionValue01").textbox('getText');
	var triggerActionValue02 = $("#triggerActionValue02").textbox('getText');
	if (triggerAction == "" || triggerAction == null) {
		return; 	
	}
	var content = UM.getEditor('myEditor').getContent();
	var response = $.trim(content.replace("<p>","").replace("</p>",""));
	response = response + triggerAction + "(\"" + triggerActionValue01 + "\",\"" + triggerActionValue02 + "\");";
	UM.getEditor('myEditor').setContent(response, false);
	$('#add_triggerAction').window('close');
	$("#triggerActionName").combobox("setValue", "");
}

//清空触发动作编辑框数据
function clearTriggerAction(){
	$('#add_triggerAction').window('close');
	$("#triggerActionName").combobox("setValue", "");
}

function add_saveMenuItems() {

	var interactiveoptions = $('#add_interactiveoptions').combobox('getText');

	var interpat = $("#add_interactivetemplate").val();
	var interpatend = $("#add_interactivetemplateend").val();

	var customvalue = $("#add_customvalue").val();
	if (customvalue == "多条值以|分隔开") {
		customvalue = "";
	}
	if (customvalue == "" && interactiveoptions == "") {
		$.messager.alert('提示', "请设置交互项选项值!", "warning");
		return false;
	}

	var wordclassid = $("#add_correspondingwordclass").val();

	if (wordclassid == "" || wordclassid == null) {
		var answer = interpat;
		interactiveoptions = interactiveoptions + "|" + customvalue;
		var options = interactiveoptions.split("|");
		var k = 0;
		var items = "";
		var template = "";
		var opt = "";
		for ( var i = 0; i < options.length; i++) {
			if (options[i] == "") {
				continue;
			}
			k++;
			items = items + "[" + k + "]" + options[i] + ".<br/>";
			template = template + "[" + k + "]" + options[i] + ".";
			opt = opt + options[i] + "||";
		}
		// template = template.substring(0,template.lastIndexOf("."));
		template = answer + template;
		answer = answer + "<br/>" + items + interpatend;

		opt = opt.substring(0, opt.lastIndexOf("||"));

		ruleresponsetemplate = '菜单询问("' + interpat + '<@选项文本>' + interpatend
				+ '","' + opt + '")';
		if (answer == null) {
			answer = "";
		}
		// if (ruleresponse != undefined && ruleresponse.indexOf('###;')>-1){
		// answer = ruleresponse.split('###;')[0] + '###;' + answer;
		// ruleresponsetemplate = ruleresponse.split('###;')[0] + '###;' +
		// ruleresponsetemplate;
		// }
		// ruleresponse = answer;
		// 给富文本框赋值，不追加
		UM.getEditor('myEditor').setContent(answer, false);
		$('#add_menuItems_edit').window('close');

	} else {
		$.ajax({
			url : '../interactiveScene.action',
			type : "post",
			data : {
				type : 'insertmenuitems',
				wordclassid : wordclassid,
				customvalue : customvalue,
				resourcetype : 'scenariosrules',
				operationtype : 'A',
				resourceid : publicscenariosid
			},
			async : false,
			dataType : "json",
			success : function(data, textStatus, jqXHR) {
				if (data.success) {
					var answer = interpat;
					interactiveoptions = interactiveoptions + "|" + customvalue;
					var options = interactiveoptions.split("|");
					var k = 0;
					var items = "";
					var template = "";
					var opt = "";
					for ( var i = 0; i < options.length; i++) {
						if (options[i] == "") {
							continue;
						}
						k++;
						items = items + "[" + k + "]" + options[i] + ".<br/>";
						template = template + "[" + k + "]"
								+ options[i] + ".";
						opt = opt + options[i] + "||";
					}
					template = answer + template;
					answer = answer + "<br/>" + items + interpatend;

					opt = opt.substring(0, opt.lastIndexOf("||"));

					ruleresponsetemplate = '菜单询问("' + interpat + '<@选项文本>' + interpatend + '","' + opt + '")';
					if (answer == null) {
						answer = "";
					}
					if (ruleresponse != undefined && ruleresponse.indexOf('###;') > -1) {
						answer = ruleresponse.split('###;')[0] + '###;' + answer;
						ruleresponsetemplate = ruleresponse.split('###;')[0] + '###;' + ruleresponsetemplate;
					}
					ruleresponse = answer;
					// 给富文本框赋值，不追加
					UM.getEditor('myEditor').setContent(answer, false);
					$('#add_menuItems_edit').window('close');
				} else {
					$.messager.alert('提示', data.msg, "warning");

				}
			}
		});
	}
}

// 实现endWith()方法
String.prototype.endWith = function(str) {
	var reg = new RegExp(str + "$");
	return reg.test(this);
}
// 实现replaceAll()方法
String.prototype.replaceAll = function(s1, s2) {     
    return this.replace(new RegExp(s1, "gm"), s2);     
} 

// 添加交互要素
function addInteractiveElement() {
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'getweight',
			scenariosid : publicscenariosid,
			weight : ""
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$('#interactiveElement_wei').combobox({
				valueField : 'id',
				textField : 'text',
				required : true,
				width : 200,
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
	$("#interactiveElement_com").combobox({
		url : '../interactiveScene.action?type=getInteractiveElement',
		valueField : 'id',
		textField : 'text',
		width : 200,
		missingMessage : '请选择交互要素！'
	});
	$("#interactiveElement_add").window('open');
}

// 保存交互要素
function saveInteractiveElement() {
	var wordclassid = $("#interactiveElement_com").combobox('getValue');
	var name = $("#interactiveElement_com").combobox('getText');
	var weight = $("#interactiveElement_wei").combobox('getValue');
	if (undefined == wordclassid || '' == wordclassid) {
		$.messager.alert('提示', "该交互要素不存在！", "warning");
		return;
	}
	var request = {
		type : 'saveinteractiveelement',
		scenariosid : publicscenariosid,
		scenariosName : publicscenariosname,
		wordclassid : wordclassid,
		name : name,
		weight : weight
	};
	$.ajax({
		url : '../interactiveScene.action',
		type : "post",
		data : request,
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$("#interactiveElement_add").window('close');
			$.messager.alert('提示', data.msg, "info");
		}
	});
}

function HTMLDecode(text) {
	var temp = document.createElement("div");
	temp.innerHTML = text;
	var output = temp.innerText || temp.textContent;
	temp = null;
	return output;
}

// 初始化连接线上面下拉窗口的值
function getLineComboboxData(){
	// 请求combox下拉框数据
	$.ajax({
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
			for ( var i = 0; i < info.length; i++) {
				var name = info[i]["name"];
				if (name == 'robotid') {
					robot = i;
				}
				if (name.indexOf("_知识名称") != -1) {
					name = name.split("_")[1];
				}
				var weight = info[i]["weight"];
				var elementvalue = info[i]["elementvalue"];
				$("#selrule0name" + weight).html(name + ":");
				$("#selrule0value" + weight).combobox({
					valueField : 'id',
					textField : 'text',
					editable : true,
					data : elementvalue
				});
				$("#selrule0value" + weight).combobox("setText", "");
			}
		}
	});
}

// 清空场景业务摘要对应关系
function clearRelation() {
	$("#service").combotree("clear");
	$("#kbdata").combobox('setValue', "");
}

//加载场景关系
function searchRelation() {
	loadSceneRelation();
}

// 打开业务摘要对应关系
function openScenarios() {
	clearRelation();
	$('#scenarios_window').window('open');
	loadSceneRelation();
	//加载业务树	  
    createServiceTree('service','kbdata');
}

//构造树形图
function createServiceTree(fid, id) {
	var fdocid = "#"+ fid;
	$(fdocid).combotree({
		url : '../interactiveScene.action?type=createservicetree&a=' + Math.random(),
		editable : true,
		onBeforeExpand : function(node, param) { 
			$(fdocid).combotree('tree').tree("options").url = "../interactiveScene.action?type=createservicetree&serviceid=" + node.id + '&a=' + Math.random();
		}, 
		onClick : function(rec) {
			createAbsCombobox(fid, id);
		}
	});
}

//根据业务构造摘要下拉框
function createAbsCombobox(fid, id) {
	var fdocid = "#" + fid;
	var docid = "#" + id
	// 获取树形结构选中的业务
	var serviceid = $(fdocid).combotree("getValue"); 
	$(docid).combobox({    
	    url : '../interactiveScene.action?type=createabstractcombobox&serviceid=' + serviceid,    
	    valueField : 'id',    
	    textField : 'text',
	    panelHeight : '150px'
	});  
}

// 保存场景业务对应关系
function saveRelation() {
	var absid  = $('#kbdata').combobox('getValue');
	var abs = $('#kbdata').combobox('getText');
	var sid = $('#service').combotree("getValue");
	var ser = $('#service').combotree("getText");
	if (sid == "") {
		$.messager.alert('提示', "请选择业务!", "warning");
		return false;
	}
	
	$.ajax({ 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'addrelation',
			scenariosid : publicscenariosid, 
			name : publicscenariosname,
			kbdataid : absid,
			abs : abs,
			serviceid : sid,
			service : ser,
			resourcetype : 'querymanage',
			operationtype : 'A',
			resourceid : sid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
			if (data.success == true) {
				$("#servicekbdatadatagrid").datagrid('load');	
			}
		}
	});
}

// 加载业务摘要对应关系
function loadSceneRelation() { 
	$("#servicekbdatadatagrid").datagrid({
		title : '业务摘要对应关系显示区',
		url : '../interactiveScene.action',
		width : 900,
		height : 395,
		toolbar : "#servicekbdatadatagrid_tb",
		pagination : true,
		rownumbers : true,
		queryParams : {
			type : 'selectservicekbdatada',
			kbdataid : $('#kbdata').combobox('getValue'),
			serviceid : $('#service').combotree("getValue"),
			scenariosid : publicscenariosid 
		},
		pageSize : 10,
		striped : true,
		singleSelect : true,
		columns : [ [
			{
				field : 'name',
				title : '场景名称',
				width : 180
			},
			{
				field : 'scenerelationid',
				title : '场景关系ID',
				width : 180,
				hidden : true
			},
			{
				field : 'service',
				title : '业务',
				width : 180
			},
			{
				field : 'serviceid',
				title : '业务ID',
				width : 50,
				hidden : true
			},
			{
				field : 'abstract',
				title : '摘要',
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
				title : '所属省份',
				width : 120,
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
				field : 'abstractid',
				title : '摘要ID',
				width : 200,
				hidden : true
			},
			{
				field : 'userquery',
				title : '用户问题',
				width : 350,
				hidden : true
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
					return '<a class="icon-delete btn_a" title="删除" onclick="deleteSceneRelation(event,' + id + ',\'' + abs + '\',\'' + service + '\')"></a>';
				}
			}
		] ]
	});
	$("#servicekbdatadatagrid").datagrid('getPager').pagination({
		showPageList : false
	});
}

//删除场景关系
function deleteSceneRelation(event, id, abs, service) {
	$.messager.confirm('提示', '确定删除该对应关系吗?', function(r) {
		if (r) {
			$.ajax({
				url : '../interactiveScene.action',
				type : "post",
				data : {
					type : 'deletescenerelation',
					scenerelationid : id,
					abs : abs,
					service : service,
					name : publicscenariosname,
					resourcetype : 'scenariosrules',
					operationtype : 'D',
					resourceid : publicscenariosid
				},
				async : false,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {
					$.messager.alert('提示', data.msg, "info");
					if (data.success == true) {
						$("#servicekbdatadatagrid").datagrid("reload");
					}
				}
			});
		}
	});
}

// 单个更新场景
function reloadScenarios() {
	$.ajax({ 
		url : '../interactiveScene.action',
		type : "post",
		data : {
			type : 'reloadScenarios',
			scenariosid : publicscenariosid
		},
		async : false,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			$.messager.alert('提示', data.msg, "info");
		},
		error : function(jqXHR, textStatus, errorThrown) {
			$.messager.alert('系统异常', "请求数据失败!", "error");
		}
	});
}