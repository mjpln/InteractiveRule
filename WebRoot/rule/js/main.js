var kbdataid = "";
var kbcontentid = "";
var kbanswerid = "";
var _abstract = "";
var publicscenariosid;
var combitionArr = [];
var rule0Arr = [];
var rule1Arr = [];
var insertorupdate_combition = 0;
var insertorupdate_rule0 = 0;
var insertorupdate_rule1 = 0;
var insertorupdate_rule2 = 0;
var userid; 
var ioa;
var citySelect = "";
$(function() {
    var urlparams = new UrlParams();// 所有url参数
    userid = decodeURI(urlparams.userid);
    ioa =  decodeURI(urlparams.ioa);
    
    // 初始化菜单区
    $('#menuarea').contextmenu(function(e){
        e.preventDefault();
    })
    
    setid(userid, ioa);
    
    loadCity();
    cityOnSelect("aa");
    
    loadCombobox();
    // //重新加载场景
    // reload();
    if (ioa == '在线教育->尚德机构->多渠道应用'){
        $('#AA').hide();
    }else{
        $('#BB').hide();
    }
});


function loadCity(){
    $("#aa").combotree({
        url:'../getCityTreeByLoginInfo.action',
        editable:false, 
        multiple:false,
        async : true,
        queryParams:{
            local : 'edit'
        },
        onLoadSuccess : function(node, data) {
//			alert(data[0].text);
            citySelect = data[0].id;
            $("#aa").combotree("setValue",citySelect);
            // 加载场景列表
            loadInteractiveSceneList(citySelect);
        }
    });
}

function loadCombobox(){
    $('#bb').combobox({
        url : '../interactiveScene.action?type=getStrategyType',
        valueField : 'id',
        textField : 'text'
    });
}

//添加city onselect事件
function cityOnSelect(id){ 
    var id ="#"+id;
    $(id).combotree({
        onSelect : function(node){
        var city = node.id;
        citySelect = city;
//		alert(citySelect);
        loadInteractiveSceneList(citySelect);
        }
    }); 
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
            //加载场景右击菜单
            getRightClickMenu();
//			alert(accessUser);
        }
//		,
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
//		}
    });

}

function search() {
    $('#menu_select').window('open');
    createScenariosCombobox();
}

function edit(){
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    $('#select_menu_name2').textbox('setValue', text);
    $('#menu_edit').window('open');
    $(this).tree('beginEdit', node.target);
}

function clearName() {
    $("#select_menu_name").combobox('setText', "");
}

function searchByName() {
//	var name = $("#select_menu_name").combobox('getText');
    var id = $("#select_menu_name").combobox('getValue');

    $("#tt")
            .tree(
                    {
//						url : '../interactiveScene.action?type=createinteractivescenetreebyname&name='
//								+ encodeURIComponent(name) + '&a=' + Math.random(),
                        url : '../interactiveScene.action?type=createinteractivescenetreebyname',
                        method : 'post',
                        animate : true,
                        cache : false,
                        queryParams : {
                        name:id
//						name:name
                        },
                        onContextMenu : function(e, node) {
                            e.preventDefault();
                            $(this).tree('select', node.target);
                            $('#mm').menu('show', {
                                left : e.pageX,
                                top : e.pageY
                            });
                        },
                        onLoadSuccess : function(node, data) {

                            $('#menu_select').window('close');
                            $("#select_menu_name").combobox('setText', "");
                            // $("#tt
                            // li:eq(0)").find("div").addClass("tree-node-selected");
                            // //设置第一个节点高亮
                            // var n = $("#tt").tree("getSelected");
                            // if(n!=null){
                            // expandAll();
                            // $("#tt").tree("select",n.target);
                            // //相当于默认点击了一下第一个节点，执行onSelect方法
                            // }

                        },

                        onBeforeExpand : function(node) {
                            $('#tt').tree('options').url = "../interactiveScene.action?type=createinteractivescenetree&scenariosid="
                                    + node.id + '&a=' + Math.random(); // 展开时发送请求去加载节点
                        },
                        onClick : function(node) {
                            var sname = node.text;
                            if (sname != "场景名称") {
                                var title = "【" + sname + "】";
                                opentab(title, node.id,sname)
                            }

                        }

                    });

}

// 根据角色归属加载不同的右击菜单
function getRightClickMenu() {
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'getcustomer',
            userid:userid,
            ioa:ioa
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            var url;
            if (data.customer == "全行业") {
                $("#s").show();
                $("#sm").show();
                $("#c").show();
                $("#cm").show();
                $("#r").show();
                $("#rm").show();
                $("#a").show();
                $("#am").show();
                $("#d").show();
                $("#dm").show();
                $("#p").show();
                $("#pm").show();
                $("#u").show();
                $("#um").show();
                $("#sw").show();
            } else {
                $("#a").show();
                $("#am").show();
                $("#c").show();
                $("#cm").show();
                $("#d").show();
                $("#dm").show();
                $("#u").hide();
                $("#um").hide();
                $("#p").hide();
                $("#pm").hide();
                // $("#sw").hide();
    }
}
//		,
//error : function(jqXHR, textStatus, errorThrown) {
//	$.messager.alert('系统异常', "请求数据失败!", "error");
//}
    });

}

// 构造场景下拉框
function createScenariosCombobox() {
    $("#select_menu_name")
            .combobox(
                    {
                        url : '../interactiveScene.action?type=createscenarioscombobox&a=' + Math
                                .random() + '&citySelect=' + encodeURI(citySelect),
                        valueField : 'id',
                        textField : 'text',
                        panelHeight : '150px',
                        multiple : false, // 支持多选
                        editable : true,
                        onSelect : function(node) {
                            searchByName();
                        }
                    });
}

////构造场景下拉框
//function createScenariosCombobox2() {
//	$("#select_menu_name2")
//			.combobox(
//					{
//						url : '../interactiveScene.action?type=createscenarioscombobox&a=' + Math
//								.random(),
//						valueField : 'id',
//						textField : 'text',
//						panelHeight : '150px',
//						multiple : false, // 支持多选
//						editable : true,
////						onSelect : function(node) {
////							searchByName();
////						}
//					});
//}

// 重新加载场景
function reload() {
    loadInteractiveSceneList();
}

function expandAll() {
    var node = $('#tt').tree('getSelected');
    if (node) {
        $('#tt').tree('expandAll', node.target);
    } else {
        $('#tt').tree('expandAll');
    }
}
// 加载场景列表
function loadInteractiveSceneList(citySelect) {
    // $('#tt').tree("reload");
    
    $("#tt")
            .tree(
                    {
                        url : '../interactiveScene.action?type=createinteractivescenetree&userid='+userid+'&ioa='+ioa+'&a=' + Math
                                .random()+'&citySelect='+encodeURI(citySelect),
                        method : 'get',
                        animate : true,
                        cache : false,
                        onContextMenu : function(e, node) {
                            e.preventDefault();
                            $(this).tree('select', node.target);
                            $('#mm').menu('show', {
                                left : e.pageX,
                                top : e.pageY
                            });
                        },
                        onLoadSuccess : function(node, data) {

                            // $("#tt
                            // li:eq(0)").find("div").addClass("tree-node-selected");
                            // //设置第一个节点高亮
                            // var n = $("#tt").tree("getSelected");
                            // if(n!=null){
                            // expandAll();
                            // $("#tt").tree("select",n.target);
                            // //相当于默认点击了一下第一个节点，执行onSelect方法
                            // }

                        },

                        onBeforeExpand : function(node) {
                            $('#tt').tree('options').url = "../interactiveScene.action?type=createinteractivescenetree&scenariosid="
                                    + node.id + '&a=' + Math.random()+'&citySelect='+encodeURI(citySelect); // 展开时发送请求去加载节点
                        },
                        onClick : function(node) {
                            // // $('#aa').accordion('select','业务摘要对应关系');
                        // //加载业务树
                        // createServiceTree();
                        // publicscenariosid =node.id;
                        // //加载场景业务摘要对应关系
                        // loadSceneRelation();
                        //        
                        //     
                        // //加载场景要素
                        // loadElementName();
                        // loadWeightCombobox();
                        // loadElementValue();
                        // //加载交互规
                        // loadRule0Combobox();
                        // loadRule0();
                        var sname = node.text;
                        if (sname != "场景名称") {
                            var title = "【" + sname + "】";
                            opentab(title, node.id,sname)
                        }

                    },

                    loadFilter : function(data, parent) {
                        for ( var i = 0; i < data.length; i++) {
                            // alert(data[i].text);
                        // if (data[i].text == "场景名称") {
                        // $('#tt').tree('expandAll');
                        // $('#tt').tree('options').url =
                        // "../interactiveScene.action?type=createinteractivescenetree&scenariosid="+data[i].id+'&a='+
                        // Math.random(); //展开时发送请求去加载节点
                        // data[i].state = "open";
                        // // expand(data[i]);
                        // expandAll();
                        // }
                    }
                    return data;
                }

                    });

}

function opentab(title, id,sname) {
    var bb = $('#bb').combotree('getValue');
    if (undefined != bb && '' != bb){
        title = title + "&" + bb; 
    }
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'getcustomer',
            userid:userid,
            ioa:ioa
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            var url;
            if (data.customer == "全行业") {
                url = "./scenarios.html?scenariosid=" + id+'&scenariosname='+sname+'&ioa='+ioa+'&strategy='+bb;
            } else {//非全行业，查询配置
                $.ajax( {
                    url : '../interactiveScene.action',
                    type : "post",
                    data : {
                        type : 'getUrl',
                        ioa:ioa
                    },
                    async : false,
                    dataType : "json",
                    success : function(data, textStatus, jqXHR) {
                        url = data.url + "?scenariosid=" + id+'&scenariosname='+sname+'&ioa='+ioa;
//						addTab(title, url);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        $.messager.alert('系统异常', "请求数据失败!", "error");
                    }
                });
            }
            addTab(title, url);
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });

}

// 打开场景对应选项卡
function addTab(title, url) {
    if ($('#tb').tabs('exists', title)) {
        $('#tb').tabs('select', title);
    }
    else {
    
//		$('#tb').tabs('close', 0);
    
        var content = '<iframe scrolling="auto" frameborder="0"  src="' + url + '" style="width:100%;height:520px;"></iframe>';
        $('#tb').tabs('add', {
            title : title,
            content : content,
            closable : true
        });

    }
}

// 加载场景关系
function searchRelation() {
    loadSceneRelation();
}

// 打开场景添加编辑框
function append(flag) {
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    $('#menu_super').textbox('setValue', text);
    $('#menu_add').find("a:first").removeAttr("onclick");
    if(flag == 'callout')
    {
        $('#menu_add').first().css("height","250px");
        $('#menu_add').find("div[robot=\"roboid\"]").show();
        $('#menu_add').find("a:first").unbind("click").bind("click",addmenuCallOut);
    }
    else if(flag == 'callin')
    {
        $('#menu_add').first().css("height","250px");
        $('#menu_add').find("div[robot=\"roboid\"]").hide();
        $('#menu_add').find("a:first").first().unbind("click").bind("click",addmenuCallIn);
    } 
    else 
    {
    	$('#menu_add').first().css("height","150px");
        $('#menu_add').find("div[robot=\"roboid\"]").hide();
        $('#menu_add').find("a:first").first().unbind("click").bind("click",addmenu);
    }
    
    $('#menu_add').window('open');
    //t.tree('beginEdit', node.target);
}

// 打开上传文档操作框
function uploadhtml() {
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    $('#menuname').textbox('setValue', text);
    $('#html_upload').window('open');

}

// 提交上传文件
function upload() {
    // 得到上传文件的全路径
    var fileName = $('#fileuploadtxt').filebox('getValue');
    // 进行基本校验
    if (fileName == "") {
        $.messager.alert('信息提示', '请选择上传文件!', 'info');
    } else {
        var t = $('#tt');
        var node = t.tree('getSelected');
        var id = node.id;
        // 对文件格式进行校验
        var d1 = /\.[^\.]+$/.exec(fileName.toLowerCase());
        if (d1 == ".doc") {
            $("#formUpload").form(
                    "submit",
                    {
                        url : "../file/upload?type=html&path=scenariosdoc/"
                                + id,
                        success : function(data) {
                            var info = $.parseJSON(data);
                            var state = info["state"];
                            if (state == "success") {
                                var name = info["names"][0];
                                saveDocname(id, name);

                            } else {
                                $.messager.alert('提示', info["message"]
                                        + " 请重新上传!", 'warning');
                            }
                            $('#fileuploadtxt').filebox('setValue', '');
                        }
                    });
        } else {
            $.messager.alert('信息提示', '请选择doc格式文件!', 'info');
            $('#fileuploadtxt').filebox('setValue', '');
        }
    }
}

// 保存文档名称
function saveDocname(id, name) {

    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'updatedocname',
            scenariosid : id,
            name : name
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            $.messager.alert('提示', data.msg, "info");
            if (data.success == true) {
                $('#html_upload').window('close');
            }

            // $.messager.alert('信息提示', data.msg, 'info');
//	},
//	error : function(jqXHR, textStatus, errorThrown) {
//		$.messager.alert('系统异常', "请求数据失败!", "error");
    }
    });

}

// 查看文档

function seehtml() {
    var t = $('#tt');
    var node = t.tree('getSelected');
    var id = node.id;
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'getdocpath',
            scenariosid : id
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {

            if (data.success == true) {

                var text = node.text + "_说明文档";
                var path = data.path;
                // alert(path);
        addTab("【" + text + "】", path);
    } else {
        // $.messager.alert('信息提示', "查看失败!", 'warning');
        $.messager.alert('提示', data.msg, "info");
    }

//},
//error : function(jqXHR, textStatus, errorThrown) {
//	$.messager.alert('系统异常', "请求数据失败!", "error");
}
    });

}


function validateName(msgname, name)
{
    //对场景名进行特殊字符验证
    if(name.indexOf('=')>-1 || name.indexOf('/')>-1 || name.indexOf('%')>-1
    || name.indexOf('>')>-1 || name.indexOf('<')>-1 || name.indexOf('*')>-1
    || name.indexOf('Contain')>-1 || name.indexOf('@')>-1 
    || name.indexOf('!')>-1 || name.indexOf('$')>-1 || name.indexOf('包含')>-1){
    $.messager.alert('系统提示', msgname + "存在非法字符串!", "info");
    return false;
   }
   return true;
}
function addmenuCallOut()
{
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    var name = replaceSpace($("#menu_name").val());
    var scenariosid = node.id;
    var robotname = replaceSpace($("#menu_robot_name").val());
    var robotid = replaceSpace($("#menu_robot_ID").val());
    
    if(!validateName("机器人名称",robotname) 
    		|| !validateName("机器人ID",robotid) 
    		|| !validateName("场景名称",name))
    {
        return;
    }    

    $('#menu_add').window('close');
    $('#waihu_msg').window('open');
    $('#waihu_msg_content').text("正在创建外呼场景...");

    var createScen = function(scname,scid) {
        return $.ajax({
            url : '../interactiveScene.action',
            type : "post",
            data : {
                type : 'addmenuCallOut',
                scenariosid : scid,
                name : scname,
                resourcetype : 'scenariosrules',
                operationtype : 'A',
                resourceid : scid,
                robotName: robotname,
                robotID: robotid
            },
            dataType : "json",
        });
    };
    createScen(name,scenariosid).done(function(data) {
        if (data.success != true) {
            $('#waihu_msg').window('close');
            $.messager.alert('系统提示', data.msg, "info");
            return;
        }
        var res1= data.childIds;
        var names = data.childNames;
        t.tree('append', {
            parent : (node ? node.target : null),
            data : [ {
                    id : data.id,
                    text : name,
                    leaf : false,
                    children:[
                    { 
                        id : res1[0],
                        text : names[0],
                        leaf : true,
                    },{ 
                        id : res1[0],
                        text : names[1],
                        leaf : true,
                    }]
                }]
            });
        clearmenu();
        t.load();
        $('#waihu_msg').window('close');
        $.messager.alert('提示', "外呼场景创建成功" , "info");
    });
}

function addmenuCallIn()
{
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    var name = replaceSpace($("#menu_name").val());
    var scenariosid = node.id;
    var robotname = replaceSpace($("#menu_robot_name").val());
    var robotid = replaceSpace($("#menu_robot_ID").val());
    
    if(!validateName("机器人名称",robotname) 
    		|| !validateName("机器人ID",robotid) 
    		|| !validateName("场景名称",name))
    {
        return;
    }    

    $('#menu_add').window('close');
    $('#waihu_msg').window('open');
    $('#waihu_msg_content').text("正在创建呼入场景...");

    var createScen = function(scname,scid) {
        return $.ajax({
            url : '../interactiveScene.action',
            type : "post",
            data : {
                type : 'addmenuCallOut',
                scenariosid : scid,
                name : "【" + scname + "】",
                resourcetype : 'scenariosrules',
                operationtype : 'A',
                resourceid : scid,
                robotName: robotname,
                robotID: robotid
            },
            dataType : "json",
        });
    };
    createScen(name,scenariosid).done(function(data) {
        if (data.success != true) {
            $('#waihu_msg').window('close');
            $.messager.alert('系统提示', data.msg, "info");
            return;
        }
        var res1= data.childIds;
        var names = data.childNames;
        t.tree('append', {
            parent : (node ? node.target : null),
            data : [ {
                    id : data.id,
                    text : name,
                    leaf : false,
                    children:[
                    { 
                        id : res1[0],
                        text : names[0],
                        leaf : true,
                    },{ 
                        id : res1[0],
                        text : names[1],
                        leaf : true,
                    }]
                }]
            });
        clearmenu();
        t.load();
        $('#waihu_msg').window('close');
        $.messager.alert('提示', "呼入场景创建成功" , "info");
    });
}

function addOtherRurles(publicscenariosid,publicscenariosname,qianti,response,ruletype )
{
    var data = {
            type : 'insertrule',
            scenariosid : publicscenariosid,
            scenariosName : publicscenariosname,
            conditions : "",
            //weight : weight,
            ruletype : ruletype,
            city:'全国',
            //copycity:copyCity,
            //flag:flag,
            ruleresponse : qianti + "==>" + response,
            ruleresponsetemplate:qianti + "==>" + response,
            resourcetype:'scenariosrules',
            operationtype:'A',
            resourceid:publicscenariosid
        }
    
     return $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : data,
        async : false,
        dataType : "json",
//		success : function(data, textStatus, jqXHR) {
//			$.messager.alert('提示', data.msg, "info");
//			if (data.success == true) {
//				$("#rule2datagrid").datagrid("reload");
//				$('#rule2_edit').panel('close');
//				clearRule2Form();
//			}
//		}
    });
}

// 添加场景名称节点
function addmenu() {
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    var name = replaceSpace($("#menu_name").val());
    var scenariosid = node.id;
    
    //对场景名进行特殊字符验证
    if(name.indexOf('=')>-1 || name.indexOf('/')>-1 || name.indexOf('%')>-1
            || name.indexOf('>')>-1 || name.indexOf('<')>-1 || name.indexOf('*')>-1
            || name.indexOf('Contain')>-1 || name.indexOf('@')>-1 
            || name.indexOf('!')>-1 || name.indexOf('$')>-1 || name.indexOf('包含')>-1){
        $.messager.alert('系统提示', "存在非法字符串!", "info");
        return;
    }
    
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'addmenu',
            scenariosid : scenariosid,
            name : name,
            resourcetype : 'scenariosrules',
            operationtype : 'A',
            resourceid : scenariosid
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            $.messager.alert('提示', data.msg, "info");
            if (data.success == true) {
                t.tree('append', {
                    parent : (node ? node.target : null),
                    data : [ {
                        id : data.id,
                        text : name,
                        leaf : true
                    } ]
                });
                clearmenu();
                $('#menu_add').window('close');
                t.load();
            }
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });
}

//修改场景名称节点
function editmenu() {
    var t = $('#tt');
    var node = t.tree('getSelected');
    var text = node.text;
    var name = replaceSpace($("#menu_newname").val());
    var oldName = replaceSpace($("#select_menu_name2").val());
    var scenariosid = node.id;
    
    //对场景名进行特殊字符验证
    if(name.indexOf('=')>-1 || name.indexOf('/')>-1 || name.indexOf('%')>-1
            || name.indexOf('>')>-1 || name.indexOf('<')>-1 || name.indexOf('*')>-1
            || name.indexOf('Contain')>-1 || name.indexOf('@')>-1 
            || name.indexOf('!')>-1 || name.indexOf('$')>-1 || name.indexOf('包含')>-1){
        $.messager.alert('系统提示', "存在非法字符串!", "info");
        return;
    }
    
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'editmenu',
            scenariosid : scenariosid,
            name : name,
            oldName : oldName,
            resourcetype : 'scenariosrules',
            operationtype : 'U',
            resourceid : scenariosid
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            $.messager.alert('提示', data.msg, "info");
            if (data.success == true) {
                t.tree('update', {
                        target : node.target,
                        text : name
                });
                clearmenu2();
                $('#menu_edit').window('close');
//				loadInteractiveSceneList();
                t.load();
            }
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });
}


// 清空菜单编辑框
function clearmenu() {
    $('#menu_name').textbox('setValue', '');
}


//清空菜单编辑框
function clearmenu2() {
    $('#menu_newname').textbox('setValue', '');
}

// 删除场景名称节点
function removeit() {
    var node = $('#tt').tree('getSelected');
    var text = node.text;
    var scenariosid = node.id;
    var leaf = node.leaf;
    if (text == "知识库" || !leaf || text == "电信集团场景") {
        $.messager.alert('提示', "仅允许删除子节点！", "info");
        return false;
    }

    $.messager.confirm('提示', '删除场景节点后场景下相关内容将一并删除,确定删除吗?', function(r) {
        if (r) {
            $.ajax( {
                url : '../interactiveScene.action',
                type : "post",
                data : {
                    type : 'deletemenu',
                    scenariosid : scenariosid,
                    name : text,
                    resourcetype : 'scenariosrules',
                    operationtype : 'D',
                    resourceid : scenariosid
                },
                async : false,
                dataType : "json",
                success : function(data, textStatus, jqXHR) {
                    $.messager.alert('提示', data.msg, "info");
                    if (data.success == true) {
                        $('#tt').tree('remove', node.target);
                    }
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
                }
            });
        }
    });

}

// 构造树形图
function createServiceTree() {
    $("#service")
            .combotree(
                    {
                        url : '../interactiveScene.action?type=createservicetree&a=' + Math
                                .random(),
                        editable : true,
                        onBeforeExpand : function(node, param) {
                            $('#service').combotree('tree').tree("options").url = "../interactiveScene.action?type=createservicetree&serviceid="
                                    + node.id + '&a=' + Math.random();
                        },
                        onClick : function(rec) {
                            createCombobox();
                        }
                    });
}

// 根据业务构造摘要下拉框
function createCombobox() {
    // 获取树形结构选中的业务
    var serviceid = $('#service').combotree("getValue");
    $('#kbdata')
            .combobox(
                    {
                        url : '../interactiveScene.action?type=createabstractcombobox&serviceid=' + serviceid,
                        valueField : 'id',
                        textField : 'text',
                        panelHeight : '150px'
                    // editable:false
                    });
}

// 保存保存场景业务对应关系
function saveRelation() {
    var absid = $('#kbdata').combobox('getValue');
    var abs = $('#kbdata').combobox('getText');
    var sid = $('#service').combotree("getValue");
    var ser = $('#service').combotree("getText");
    var uquery = replaceSpace($("#userquery").val());
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'addrelation',
            scenariosid : publicscenariosid,
            kbdataid : absid,
            abs : abs,
            serviceid : sid,
            service : ser,
            query : uquery
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

function collapse(node) {
    $('#tt').tree('collapse', node.target);
}
function expand(node) {
    $('#tt').tree('expand', node.target);
}

// 加载业务摘要对应关系

function loadSceneRelation() {
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
                            serviceid : $('#service').combotree("getValue"),
                            query : replaceSpace($("#userquery").val()),
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
                                    width : 200
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
                                    width : 350
                                },
                                {
                                    field : "delete",
                                    title : '删除',
                                    width : 35,
                                    align : 'center',
                                    formatter : function(value, row, index) {
                                        var id = row["scenerelationid"];
                                        return '<a class="icon-delete btn_a" title="删除" onclick="deleteSceneRelation(event,' + id + ')"></a>';
                                    }
                                } ] ],
                        onClickRow : function(rowIndex, rowData) {
                            $('#elementvaluedatagrid').datagrid('load', {
                                type : 'selectword',
                                wordclassid : rowData.wordclassid,
                                name : $.trim($("#selelementvalue").val())
                            });
                        }
                    });
    $("#servicekbdatadatagrid").datagrid('getPager').pagination( {
        showPageList : false
    });

}

// 查询问题要素
function searchElementName() {
    $('#elementnamedatagrid').datagrid('load', {
        type : 'selectelementname',
        kbdataid : kbdataid,
        kbcontentid : kbcontentid,
        name : $.trim($("#selelementname").val())
    });
}

// 加载问题要素列表
function loadElementName() {
    $("#elementnamedatagrid")
            .datagrid(
                    {
                        title : '场景要素显示区',
                        url : '../interactiveScene.action',
                        width : 550,
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
                                    width : 180
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
                                    width : 200
                                },
                                {
                                    field : 'infotalbepath',
                                    title : '对应信息表',
                                    width : 200
                                },
                                {
                                    field : 'interpat',
                                    title : '交互模板',
                                    width : 200
                                },
                                {
                                    field : 'city',
                                    title : '地市',
                                    width : 200
                                },
                                {
                                    field : 'itemmode',
                                    title : '填写方式',
                                    width : 200
                                },
                                {
                                    field : 'isshare',
                                    title : '是否共享',
                                    width : 200
                                },
                                {
                                    field : "delete",
                                    title : '删除',
                                    width : 35,
                                    align : 'center',
                                    formatter : function(value, row, index) {
                                        var id = row["id"];
                                        var weight = row["weight"];
                                        var name = row["name"];
                                        return '<a class="icon-delete btn_a" title="删除" onclick="deleteElementName(event,'
                                                + id
                                                + ','
                                                + weight
                                                + ',\''
                                                + name + '\')"></a>';
                                    }
                                } ] ],
                        onClickRow : function(rowIndex, rowData) {
                            $('#elementvaluedatagrid').datagrid('load', {
                                type : 'selectword',
                                wordclassid : rowData.wordclassid,
                                name : $.trim($("#selelementvalue").val())
                            });
                        }
                    });
    $("#elementnamedatagrid").datagrid('getPager').pagination( {
        showPageList : false,
        buttons : [ {
            text : "新增",
            iconCls : "icon-add",
            handler : function() {
                $('#elementedit_w').window('open');
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
        }, "-" ]
    });
}

// 加载问题要素页面的优先级下拉框
function loadWeightCombobox() {
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'getweight',
            scenariosid : publicscenariosid
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

// 提交问题要素表单
function submitElementNameForm() {
    $('#elementnameform').form('submit', {
        onSubmit : function() {
            var isValid = $(this).form('enableValidation').form('validate');
            if (isValid) {
                insertElementName();
            }
            return false;
        }
    });
}

// 清空新增问题要素表单
function clearElementNameForm() {
    $("#elementnametextbox").textbox('setValue', "");
    $("#wordclasstextbox").textbox('setValue', "");
    loadWeightCombobox();
}

// 新增问题要素
function insertElementName() {
    var name = $.trim($("#elementnametextbox").val());
    if (name == '') {
        $.messager.alert('提示', "问题要素名称不能为空字符串,请填写问题要素称!", "warning");
        return;
    }
    var wordclass = $.trim($("#wordclasstextbox").val());
    // if (wordclass == '') {
    // $.messager.alert('提示', "对应词类不能为空字符串,请填写对应词类!", "warning");
    // return;
    // }
    var weight = $("#weightcombobox").combobox('getText');
    $.ajax( {
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'insertelementname',
            name : name,
            weight : weight,
            wordclass : wordclass,
            scenariosid : publicscenariosid
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            $.messager.alert('提示', data.msg, "info");
            if (data.success == true) {
                $("#elementnamedatagrid").datagrid("reload");
                clearElementNameForm();
            }
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });
}

// 删除场景关系
function deleteSceneRelation(event, id) {
    alert("dd");
    // if (event.stopPropagation) {// Mozilla and Opera
    // event.stopPropagation();
    // } else if (window.event) {// IE
    // window.event.cancelBubble = true;
    // }
    $.messager.confirm('提示', '确定删除该对应关系吗?', function(r) {
        if (r) {
            $.ajax( {
                url : '../interactiveScene.action',
                type : "post",
                data : {
                    type : 'deletescenerelation',
                    scenerelationid : id
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

// 删除问题要素
function deleteElementName(event, id, weight, name) {
    if (event.stopPropagation) {// Mozilla and Opera
        event.stopPropagation();
    } else if (window.event) {// IE
        window.event.cancelBubble = true;
    }
    $.messager.confirm('提示', '确定删除该记录吗?', function(r) {
        if (r) {
            $.ajax( {
                url : '../queryelement.action',
                type : "post",
                data : {
                    type : 'deleteelementname',
                    kbdataid : kbdataid,
                    kbcontentid : kbcontentid,
                    weight : weight,
                    elementnameid : id,
                    name : name,
                    abs : _abstract
                },
                async : false,
                dataType : "json",
                success : function(data, textStatus, jqXHR) {
                    $.messager.alert('提示', data.msg, "info");
                    if (data.success == true) {
                        $("#elementnamedatagrid").datagrid("reload");
                        loadWeightCombobox();
                    }
//				},
//				error : function(jqXHR, textStatus, errorThrown) {
//					$.messager.alert('系统异常', "请求数据失败!", "error");
                }
            });
        }
    });
}

// 查询问题要素值
function searchElementValue() {
    var rows = $("#elementnamedatagrid").datagrid('getSelections');
    if (rows.length == 1) {
        $('#elementvaluedatagrid').datagrid('load', {
            type : 'selectword',
            wordclassid : rows[0].wordclassid,
            name : $.trim($("#selelementvalue").val())
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
                        title : '场景问题要素值显示区',
                        url : '../queryelement.action',
                        width : 350,
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
                                    width : 250
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

// 提交问题要素表单
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

// 清空新增属性值表单
function clearElementValueForm() {
    $("#elementvaluetextbox").textbox('setValue', "");
}

// 新增属性值
function insertElementValue() {
    var name = $.trim($("#elementvaluetextbox").val());
    if (name == '') {
        $.messager.alert('提示', "问题要素值不能为空字符串,请填写问题要素值!", "warning");
        return;
    }
    var row = $('#elementnamedatagrid').datagrid('getSelected');
    if (!row) {
        $.messager.alert('提示', "请选择问题要素列表中的任意一行!", "warning");
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
        $.messager.alert('提示', "请选择问题要素值对应的问题要素!", "warning");
        return;
    }
    var wordclass = row.wordclass;
    var weight = row.weight;
    $.messager.confirm('提示', '确定删除该记录吗?', function(r) {
        if (r) {
            $.ajax( {
                url : '../queryelement.action',
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
    for ( var i = 1; i < 11; i++) {
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
    for ( var i = 1; i < 11; i++) {
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
    for ( var i = 1; i < 11; i++) {
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

// 加载缺失补全规则的查询和添加下拉框
function loadRule0Combobox() {
    for ( var i = 1; i < 11; i++) {
        $("#selrule0name" + i).html("");
        $("#selrule0" + i).hide();
        $("#addrule0name" + i).html("");
        $("#addrule0" + i).hide();
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
            rule0Arr = [];
            rule0Arr.push( {
                field : 'ck',
                checkbox : true
            });
            for ( var i = 0; i < info.length; i++) {
                var name = info[i]["name"];
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
                $("#addrule0name" + weight).html(name + ":");
                $("#addrule0" + weight).show();
                $("#addrule0value" + weight).combobox( {
                    valueField : 'id',
                    textField : 'text',
                    editable : false,
                    data : elementvalue
                });
                rule0Arr.push( {
                    field : "condition" + weight,
                    title : name,
                    align : 'center'
                });
            }
            rule0Arr.push( {
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
            rule0Arr.push( {
                field : "weight",
                title : "规则优先级",
                align : 'center'
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
    $("#selrule0type").combobox( {
        valueField : 'id',
        textField : 'text',
        editable : false,
        panelHeight : 'auto',
        data : typedata,
        value : "0"
    });
    $("#selrule0type").combobox("disable");
    $("#addrule0type").combobox( {
        valueField : 'id',
        textField : 'text',
        editable : false,
        panelHeight : 'auto',
        data : typedata,
        value : "0"
    });
    $("#addrule0type").combobox("disable");
    clearRule0Form();
}

// 查询缺失补全规则的信息
function searchRule0() {
    var conditions = [];
    for ( var i = 1; i < 11; i++) {
        var con = $("#selrule0value" + i).combobox("getText");
        if (con == "(空)") {
            con = "";
        }
        conditions.push(con);
    }
    var weight = $("#selrule0weight").numberbox("getValue");
    $('#rule0datagrid').datagrid('load', {
        type : 'selectrule',
        kbdataid : kbdataid,
        kbcontentid : kbcontentid,
        conditions : conditions.join("@"),
        ruletype : 0,
        weight : weight
    });
}

// 加载缺失补全规则的列表
function loadRule0() {
    $("#rule0datagrid").datagrid( {
        url : '../queryelement.action',
        height : 335,
        pagination : true,
        rownumbers : true,
        queryParams : {
            type : 'selectrule',
            kbdataid : kbdataid,
            kbcontentid : kbcontentid,
            conditions : "",
            ruletype : 0,
            weight : ""
        },
        pageSize : 10,
        striped : true,
        nowrap : true,
        fitColumns : true,
        singleSelect : false,
        columns : [ rule0Arr ]
    });
    $("#rule0datagrid").datagrid('getPager').pagination( {
        showPageList : false,
        buttons : [ {
            text : "新增",
            iconCls : "icon-add",
            handler : function() {
                $('#w').window('open');
                clearRule0Form();
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
        }, "-" ]
    });
}

// 修改缺失补全规则操作，将值放入编辑区
function editRule0() {
    var rows = $("#rule0datagrid").datagrid("getSelections");
    if (rows.length == 1) {
        for ( var i = 1; i < rule0Arr.length - 3; i++) {
            var field = rule0Arr[i].field;
            var con = rows[0][field];
            var weight = field.replace("condition", "");
            $("#addrule0value" + weight).combobox("setValue", con);
        }

        $("#addrule0weight").numberbox("setValue", rows[0].weight);
        $("#addrule0response").val(rows[0].response);
        $("#rule0id").val(rows[0].id);
        insertorupdate_rule0 = 1;
    } else {
        $.messager.alert('提示', "请选择一行!", "warning");
        return;
    }
}

// 删除(批量)缺失补全规则
function deleteRule0() {
    var ruleid = [];
    var rows = $("#rule0datagrid").datagrid("getSelections");
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
                $("#rule0datagrid").datagrid("reload");
            }
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });
}

// 清空缺失补全规则表单
function clearRule0Form() {
    $('#rule0form').form('clear');
    $("#addrule0type").combobox("setValue", "0");
    $("#addrule0weight").numberbox("setValue", "");
    $("#addrule0response").focus();
    insertorupdate_rule0 = 0;
}

// 新增或修改缺失补全规则
function saveRule0() {
    var conditions = [];
    var count = 0;
    for ( var i = 1; i < 11; i++) {
        var cond = $("#addrule0value" + i).combobox("getText");
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
    var weight = $("#addrule0weight").numberbox("getValue");
    if (weight === null || weight === "") {
        $.messager.alert('提示', "请输入优先级!", "warning");
        return;
    }
    var response = $.trim($("#addrule0response").val());
    var data = {};
    if (insertorupdate_rule0 == 0) {
        data = {
            type : 'insertrule',
            kbdataid : kbdataid,
            kbcontentid : kbcontentid,
            conditions : conditions.join("@"),
            weight : weight,
            ruletype : 0,
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
            ruletype : 0,
            ruleresponse : response,
            ruleid : $("#rule0id").val()
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
                $("#rule0datagrid").datagrid("reload");
                clearRule0Form();
            }
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
        }
    });
}

// 更新业务规则
//function updateRuleNLP() {
//	$.ajax( {
//		url : '../queryelement.action',
//		type : "post",
//		data : {
//			type : "updaterulenlp"
//		},
//		async : false,
//		dataType : "json",
//		success : function(data, textStatus, jqXHR) {
//			$.messager.alert('提示', data.msg, "info");
//		},
//		error : function(jqXHR, textStatus, errorThrown) {
//			$.messager.alert('系统异常', "请求数据失败!", "error");
//		}
//	});
//}

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
    for ( var i = 1; i < 11; i++) {
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
            kbdataid : kbdataid,
            kbcontentid : kbcontentid,
            kbanswerid : kbanswerid
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
    for ( var i = 1; i < 11; i++) {
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
    for ( var i = 1; i < 11; i++) {
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
        // "id" : "0",
        // "text" : "缺失补全规则"
        // }, {
        // "id" : "1",
        // "text" : "问题要素冲突判断规则"
        // }, {
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
    // value : "2"
            });
    // $("#addrule2type").combobox("disable");
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
            ruletype : 2,
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
                } else if (value == '2') {
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

// 替换字符串中所有空格
function replaceSpace(str) {
    str = str.replace(new RegExp(' ', 'g'), '');
    return str;
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

function myKeyDown() {
    var k = window.event.keyCode;
    if (8 == k) {
        event.keyCode = 0;// 取消按键操作
    }
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

// 打开流程图
function opengojs(){
    var t = $('#tt');
    var node = t.tree('getSelected');
    var sname = node.text;
    var id  = node.id;
    if (sname != "场景名称") {
        var title = "【" + sname + "-流程图】";
        opengojstab(title, node.id, sname)
    }
    //t.tree('beginEdit', node.target);
}

function opengojstab(title, id,sname) {
    var bb = $('#bb').combotree('getValue');
    if (undefined != bb && '' != bb) {
        title = title + "&" + bb; 
    }
    $.ajax({
        url : '../interactiveScene.action',
        type : "post",
        data : {
            type : 'getcustomer',
            userid : userid,
            ioa : ioa
        },
        async : false,
        dataType : "json",
        success : function(data, textStatus, jqXHR) {
            var url;
            if (data.customer == "全行业") {
            	if(sname.indexOf('【') > -1 && sname.indexOf('】') > -1) {
            		url = "./scenariosCall.html?scenariosid=" + id + '&scenariosname=' + sname + '&ioa=' + ioa + '&strategy=' + bb + '&sceneType=callIn';
            	} else {
            		url = "./scenariosCall.html?scenariosid=" + id + '&scenariosname=' + sname + '&ioa=' + ioa + '&strategy=' + bb + '&sceneType=callOut';
            	}
            } else {//非全行业，查询配置
                $.ajax({
                    url : '../interactiveSceneCall.action',
                    type : "post",
                    data : {
                        type : 'getUrl',
                        ioa : ioa
                    },
                    async : false,
                    dataType : "json",
                    success : function(data, textStatus, jqXHR) {
                        url = data.url + "?scenariosid=" + id + '&scenariosname=' + sname + '&ioa=' + ioa;
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        $.messager.alert('系统异常', "请求数据失败!", "error");
                    }
                });
            }
            addTab(title, url);
        }
    });
}