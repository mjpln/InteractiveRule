function start() {  //页面加载时执行
		selectQueryItem();//获取用户咨询
		  selectFastReply(); //获取快捷回复区数据
		  getCharts();//获取图线的功能
		  showQueryHistory();//给咨询历史按钮添加click事件
		  enterSubmit1();
}
var time=setInterval(selectQueryItem,20000);//每隔20秒执行一次，这个事件只执行一次 只要执行成功就清除
var queryItem;
function selectQueryItem()//取消息
{ 
if(queryItem==null){
	$.ajax({
		type : "POST",
		url : "common/getMsg.action",
		dataType: "json",
		beforeSend: function(){
			queryItem="";
			},
		success : function(data, textStatus, jqXHR){
			queryItem = data;
			fillQueryItem();
		},
		error : function(jqXHR, textStatus, errorThrown) {	
			queryItem==null;
		}
	});	
}	
}
//根据ID取信息并填充
function getById()
{	
	if(queryItem==null){
	var id=$("#telephone").text();
	var data={i_param:id};
	$.ajax({
		type : "POST",
		url : "common/getMsgById.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			queryItem = data;
			if(queryItem!=null){
				clearInterval(time1);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {	
			queryItem==null;
		}
	});	
}
	if($("#source").text()=="")
	{
		if(queryItem!=null){
			fillQueryItem();
		}
	}
}
function fillQueryItem()//填充控件，用户的信息和咨询
{
	if(queryItem!=null)
	{
		var str=JSON.stringify(queryItem);
		$("#nlpresult").val(str);
		$("#telephone").text(queryItem["queryObject"]["userID"]);
		//$("#sendtime").text(queryItem["endTime"]);
		$("#source").text(queryItem["queryObject"]["channel"]);
		//$("#query").text(queryItem["query"]);
		//$("#show").append("<span style='color:blue;'>"+queryItem["queryObject"]["userID"]+"&nbsp;"+queryItem["endTime"]+"</span><br/>");
		//$("#show").append("<span>"+queryItem["query"]+"</span><br/>");
		$("#show").append("<div class='alert alert-success'><em>"+queryItem["queryObject"]["userID"]+"</em>"+queryItem["endTime"]+"<p>"+queryItem["query"]+"</p></div>");
		fillService();
		var date=new Date().pattern("yyyy-MM-dd HH:mm:ss");
		$("#starttime").val(date);
		$("#answer").val(queryItem["kNLPResults"][0]["answer"]);	
		if(queryItem["kNLPResults"].length>=2)
		{
			$("#Top_2").text(queryItem["kNLPResults"][1]["answer"])
		}
		if(queryItem["kNLPResults"].length>=3)
		{
			$("#Top_3").text(queryItem["kNLPResults"][2]["answer"])
		}
		if(queryItem["kNLPResults"].length>=4)
		{
			$("#Top_4").text(queryItem["kNLPResults"][3]["answer"])
		}
	}
	//滑动聊天框的滚动条
	var e=document.getElementById("show");
	e.scrollTop=e.scrollHeight; //控制scrollTop的值
	if(queryItem!=null){
		if(typeof(time)!="undefined"){
			clearInterval(time);
		}
	}
}
//获取service信息并填充
function fillService()
{
	var param=queryItem["kNLPResults"][0]["service"];
	var data={i_param:param};
	$.ajax({
		type : "POST",
		url : "common/getService.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			var services= data["rows"];
			$("#select_business").append('<option >--请选择--</option>');
			for(var i=0;i<services.length;i++)
			{
				$("#select_business").append('<option value="' +services[i]["SERVICE"]+ '">'+services[i]["SERVICE"]+'</option>');	
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
}
//根据service获取topic信息并填充
function fillTopic()
{	//var t=$("#select_theme").find("option");
	if($("#select_theme").find("option").length==0){
		var param=$("#select_business").val();
		if(param!="--请选择--")
		{
			var data={i_param:param};
			$.ajax({
				type : "POST",
				url : "common/getTopic.action",
				data:data,
				dataType: "json",
				success : function(data, textStatus, jqXHR){
					var services= data["rows"];
					$("#select_theme").append('<option >--请选择--</option>');
					for(var i=0;i<services.length;i++)
					{
						$("#select_theme").append('<option value="' +services[i]["TOPIC"]+ '">'+services[i]["TOPIC"]+'</option>');	
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {	
				}
				});	
			}
		
	}
	
}
//根据service和topic获取Abstract并填充
function fillAbstract()
{
	if($("#select_remark").find("option").length==0)
	{
		var param={service:$("#select_business").val(),topic:$("#select_theme").val()};
		if($("#select_theme").val()!="--请选择--")
		{
			var data={i_param:JSON.stringify(param)};
			$.ajax({
				type : "POST",
				url : "common/getAbstract.action",
				data:data,
				dataType: "json",
				success : function(data, textStatus, jqXHR){
					var services= data["rows"];
					$("#select_remark").append('<option >--请选择--</option>');
					for(var i=0;i<services.length;i++)
					{
						$("#select_remark").append('<option value="' +services[i]["ABSTRACT"]+ '">'+services[i]["ABSTRACT"]+'</option>');	
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {	
				}
			});	
		}
		
	}
}
//当业务改变时清除后面的topic和abstract内容并填充topic
function clearSelect()
{
	$("#select_theme").empty();
	$("#select_remark").empty();
	fillTopic();
}
//当topic改变时清除abstract内容 并填充abstract
function clearAbstract()
{
	$("#select_remark").empty();
	fillAbstract();
}
function save()//将处理后的消息写入队列
{ 
if($("#answer").val()=="")
{
	alert("回复信息不能为空");
}else
{
	if(queryItem==null)
	{
		$("#operationType").val("主动发起提问");
		$("#starttime").val(new Date().pattern("yyyy-MM-dd HH:mm:ss"));
		}
	var operator=$("#operator").val();//临时定义的变量 因为目前得不到操作员的id
	var date=new Date().pattern("yyyy-MM-dd HH:mm:ss");
	var param=$($.parseJSON($("#nlpresult").val()));
	param.attr("answer",$("#answer").val());
	param.attr("operator",operator);
	param.attr("startProcessTime",$("#starttime").val());
	param.attr("endProcessTime",date);
	param.attr("operationType",$("#operationType").val());
	param.attr("fastReply",$("#fastReply").val());
	var str=JSON.stringify(param);
	var param1={id:$("#telephone").text(),msg:str.substring(5,str.length-12)};
	var data={i_param:JSON.stringify(param1)};
	$.ajax({
		type : "POST",
		url : "common/sendMsg.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			//$("#show").append("<span style='color:blue;'>"+"我"+"&nbsp;"+new Date().pattern("yyyy-MM-dd HH:mm:ss")+"</span><br/>");
			$("#show").append("<div class='alert'><em>我</em>"+new Date().pattern("yyyy-MM-dd HH:mm:ss")+"<p>"+$("#answer").val()+"</p></div>");
		//$("#show").append("<span>"+$("#answer").val()+"</span><br/>");
		var e=document.getElementById("show");
		e.scrollTop=e.scrollHeight; //控制scrollTop的值
			//$("#telephone").text("");
			$("#source").text("");
			$("#starttime").val("");
			$("#answer").val("");
			$("#select_business").empty();
			queryItem=null;
			//每发送一次就刷新图表
			getCharts();
			var time1=setInterval(getById,1000);
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
}
	
}
var fastReply;
//快捷回复区取数据
function selectFastReply()
{	var userid=$("#operator").val();
	var data={i_param:userid};
	$.ajax({
		type : "POST",
		url : "common/selectFastReply.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
		fastReply=data["rows"];
		fillFastReply();
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
}
//快捷回复区显示
function fillFastReply(){
	for(var i=0;i<fastReply.length;i++){
		if(fastReply[i]["type"]=="1"){
			$("#tab01 .guild table").append("<tr id='"+fastReply[i]["id"]+"'><td id='span_1_"+fastReply[i]["id"]+"'>"+fastReply[i]["q"]+"</td><td id='span_2_"+fastReply[i]["id"]+"'>"+fastReply[i]["a"]+"</td> <td><button class='close' onclick='setFastReply("+fastReply[i]["id"]+")'>使用</button></td></tr>");
		}else if(fastReply[i]["type"]=="2"){
			$("#tab02 .guild table").append("<tr id='"+fastReply[i]["id"]+"'><td id='span_1_"+fastReply[i]["id"]+"'>"+fastReply[i]["q"]+"</td><td id='span_2_"+fastReply[i]["id"]+"'>"+fastReply[i]["a"]+"</td> <td><button class='close' onclick='setFastReply("+fastReply[i]["id"]+")'>使用</button></td></tr>");
		}else{
			$("#tab03 .guild table").append("<tr id='"+fastReply[i]["id"]+"'><td id='span_1_"+fastReply[i]["id"]+"'>"+fastReply[i]["q"]+"</td><td id='span_2_"+fastReply[i]["id"]+"'>"+fastReply[i]["a"]+"</td> <td><button class='close' onclick='setFastReply("+fastReply[i]["id"]+")'>使用</button></td></tr>");
		}
	}
	  editTable();
}
//点击快捷回复区的答案  改变话务回复里面的答案
function setFastReply(id){
	var str="#span_1_"+id;
	var str1="#span_2_"+id;
	$("#answer").val($(str1).text());
	$("#operationType").val("使用快捷答案");
	$("#fastReply").val($(str).text()+"@_@"+$(str1).text());//给fastReply赋值
	//改变QA的点击量
	var data={i_param:id};
	$.ajax({
		type : "POST",
		url : "common/resetPageview.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
}
//点击摘要改变答案
function changeAnswer(id)
{
	var str="#"+id;
	$("#answer").val($(str).val());
	$("#operationType").val("选择"+id+"答案");	
}
//点击修改改变快捷回复区里面的问题和答案
function changeFastReply(id1)
{
	var qid="#span_1_"+id1;
	var aid="#span_2_"+id1;
	var q=$(qid).text();
	var a=$(aid).text();
	var qa1=q+"@_@"+a;
	var temp={id:id1,qa:qa1};
	var data={i_param:JSON.stringify(temp)};
	$.ajax({
		type : "POST",
		url : "common/resetQA.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
}
//重置答案功能
function resetVal()
{
	$("#answer").val("");
	$("#operationType").val("重置后发送");
}
//送黑名单
function sendBlackList()
{
	$("#operationType").val("送黑名单");
}


/** * 对Date的扩展，将 Date 转化为指定格式的String * 月(M)、日(d)、12小时(h)、24小时(H)、分(m)、秒(s)、周(E)、季度(q)
    可以用 1-2 个占位符 * 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) * eg: * (new
    Date()).pattern("yyyy-MM-dd hh:mm:ss.S")==> 2006-07-02 08:09:04.423      
 * (new Date()).pattern("yyyy-MM-dd E HH:mm:ss") ==> 2009-03-10 二 20:09:04      
 * (new Date()).pattern("yyyy-MM-dd EE hh:mm:ss") ==> 2009-03-10 周二 08:09:04      
 * (new Date()).pattern("yyyy-MM-dd EEE hh:mm:ss") ==> 2009-03-10 星期二 08:09:04      
 * (new Date()).pattern("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18      
 */        
Date.prototype.pattern=function(fmt) {         
    var o = {         
    "M+" : this.getMonth()+1, //月份         
    "d+" : this.getDate(), //日         
    "h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12, //小时         
    "H+" : this.getHours(), //小时         
    "m+" : this.getMinutes(), //分         
    "s+" : this.getSeconds(), //秒         
    "q+" : Math.floor((this.getMonth()+3)/3), //季度         
    "S" : this.getMilliseconds() //毫秒         
    };         
    var week = {         
    "0" : "/u65e5",         
    "1" : "/u4e00",         
    "2" : "/u4e8c",         
    "3" : "/u4e09",         
    "4" : "/u56db",         
    "5" : "/u4e94",         
    "6" : "/u516d"        
    };         
    if(/(y+)/.test(fmt)){         
        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));         
    }         
    if(/(E+)/.test(fmt)){         
        fmt=fmt.replace(RegExp.$1, ((RegExp.$1.length>1) ? (RegExp.$1.length>2 ? "/u661f/u671f" : "/u5468") : "")+week[this.getDay()+""]);         
    }         
    for(var k in o){         
        if(new RegExp("("+ k +")").test(fmt)){         
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));         
        }         
    }         
    return fmt;         
}       
//获取曲线图的xy轴的信息
function getCharts() {
	var param=$("#operator").val();//应为坐席的帐号或是id
	var data={i_param:param};
	$.ajax({
        type: "post",
        url: "common/chart.action",
		data:data,
        dataType: "json",
        beforeSend: function () {
         $("#chart").hide();
        },
        success: function (data, textStatus, jqXHR) {
            $("#chart").show();
			var json=$.parseJSON(data);
            var xAxisValue =json["xAxisValue"];
            var yAxisValue =json["yAxisValue"];
                getColumnChartsData(xAxisValue, yAxisValue);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
        },
        complete: function (XMLHttpRequest, textStatus) {
            this;
        }
    });
}

var chart;
//创建曲线图并显示

function getColumnChartsData(xAxisValue, yAxisValue) {
	chart = new Highcharts.Chart({ //ͼ����ֱ���
        chart: {
            renderTo: 'chart',//定义要绑定到的div的ID
            width: 400,
           defaultSeriesType: 'spline',
            zoomType: 'xy'
        },
        title: {
            text: '当日处理咨询量',
            style: {
                margin: '10px 0 0 0' // center it
            }
        },
        subtitle: {
            text: '个',
            style: {
                margin: '0 0 0 0' // center it
            }
        },
        xAxis: {
            categories: xAxisValue,
            labels: {
                rotation: -45,
                align: 'right',
                style: {
                    font: 'normal 13px Verdana, sans-serif'
                }
            }
        },
        yAxis: { // Primary yAxis
			min:0,
            title: {
                text: '咨询数处理量',
                style: {
                    color: '#4572A7'
                }
				 
            },
            opposite: true
        },
		plotOptions:{ //设置数据点 
            line:{ 
                dataLabels:{ 
                    enabled:true  //在数据点上显示对应的数据值 
                }, 
                enableMouseTracking: false //取消鼠标滑向触发提示框 
            } 
        }, 
//        tooltip: {
//           enabled: false,
//            formatter: function() {
//                return '<b>'+ this.series.name +'</b><br>'+this.x +': '+ this.y +'°C';
//            }
//        },
        //legend: {
         //   layout: 'horizontal',
         //   style: {
         //       left: '100px',
         //       bottom: 'auto',
         //       right: 'auto',
         //       top: '10px'
         //   },
         //   backgroundColor: '#FFFFFF'
       // },
        series: yAxisValue
    });
}
var str = "<input type='text' class='input' name='editTable' id='editTable' />";//编辑的输入框
//将快速回复区的table、变成可编辑的
function editTable()
{	
	$("#tab01 .guild table td").dblclick(function(event){
		editTableVal(this);
	});
	$("#tab02 .guild table td").dblclick(function(event){
		editTableVal(this);
	});
	$("#tab03 .guild table td").dblclick(function(event){
		editTableVal(this);
	});

}
function editTableVal(obj){

$("body").append(str);

var $input = $("#editTable");

//1.确定input输入框的位置

var event = $(obj).offset();

$input.css({"top":(event.top+1),"left":(event.left+1),"position":"absolute"});

//2.将table的值取出来复制到input框中

$input.focus(function(){

$input.val($(obj).text());

});

//3.显示input框

$input.show();

//4.获取焦点

$input.focus();

$input.blur(function(){

//5.修改后保存数据库

//可以通过ajax向存储空间输入值

//6.将input框中的值填写到table中

$(obj).text($(this).val());
var str=$(obj).attr("id");
str=str.substring(7,str.length);
changeFastReply(str);
$(this).remove();
});
}
var history = "<div  name='queryHistory' id='queryHistory' ></div>";
function showQueryHistory()
{
	$("#showQueryHistory").click(function(event){
		queryHistory(this);
	});
}
function queryHistory(obj)
{
	$("body").append(history);
	var $div = $("#queryHistory");

//1.确定div框的位置

var event = $(obj).offset();

$div.css({"top":(event.top-210),"left":"30px","position":"absolute","width":"64%","height":"200px","overflow":"auto","background-color":"white"});

//2.获取历史咨询数据
	var userid=$("#telephone").text();
	var data={i_param:userid};
	$.ajax({
		type : "POST",
		url : "common/selectQueryHistory.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			var historys=data["rows"];
			$div.append("<span style='color:red;'>双击此区域关闭历史咨询</span><br/>")
			for(var i=0;i<historys.length;i++)
			{
				//$div.append("<span style='color:blue;'>"+userid+"&nbsp;"+historys[i]["STARTTIME"]+"</span><br/>");
				//$div.append("<span>"+historys[i]["QUERY"]+"</span><br/>");
				$div.append("<div class='alert alert-success'><em>"+userid+"</em>"+historys[i]["STARTTIME"]+"<p>"+historys[i]["QUERY"]+"</p></div>");
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	

//3.显示div框
$div.show();
//4.双击div关闭
$div.dblclick(function(event){
		$(this).remove();
	});
}
//根据摘要调用方法获得答案
function getAnswer()
{
	var abs1=$("#select_remark").val();//获取摘要内容
	if(abs1!="--请选择--")
	{
		var data={i_param:abs1};
	$.ajax({
		type : "POST",
		url : "common/getAnswer.action",
		data:data,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			var result=data["answer"];
			$("#textarea").val(result);
		},
		error : function(jqXHR, textStatus, errorThrown) {	
		}
	});	
	}
	
}
//使用调用webservice获得的答案
function useAnswer()
{
	$("#answer").val($("#textarea").val());
}
//显示type的选择框
var selectType="<select id='select_type'onChange='saveAsFastReply()'><option >--请选择快速回复区标签页--</option><option value='1'>标签1</option><option value='2'>标签2</option><option value='3'>标签3</option></select>";
function showSelectType(obj){
	$("body").append(selectType);
	var $select = $("#select_type");

	//1.确定input输入框的位置

	var event = $(obj).offset();

	$select.css({"top":(event.top),"left":(event.left),"position":"absolute","z-index":"1"});
	$select.show();
}
//存为快捷回复
function saveAsFastReply()
{
	var type=$("#select_type").val();
	if(type!="--请选择快速回复区标签页--"){
		var operator=$("#operator").val();
		var query=queryItem["query"];
		var answer=$("#textarea").val();
		var param={userid:operator,type:type,qa:query+"@_@"+answer};
		var data={i_param:JSON.stringify(param)};
		$.ajax({
			type : "POST",
			url : "common/saveFastReply.action",
			data:data,
			dataType: "json",
			success : function(data, textStatus, jqXHR){
				$("#tab01 .guild table").empty();
				$("#tab02 .guild table").empty();
				$("#tab03 .guild table").empty();
				selectFastReply();
				$("#select_type").hide();
				$("#select_type").remove();
			},
			error : function(jqXHR, textStatus, errorThrown) {	
				$("#select_type").hide();
				$("#select_type").remove();
			}
		});	
	}	
}
function enterSubmit1() {
    //回车确认
    $(function () {
        document.onkeypress = function (e) {
            var ev = document.all ? window.event : e;
            if (ev.keyCode == 13) {
                save();
                return false;
            }
        }
    });
}
//为响应主页面回车事件
function enter()
{
	 var question = $("#textarea1").val();
       if (question == null || question == "") {
               alert("输入不能为空");
                    //清除用户的问题
                $("#textarea1").val("");
                   //获取焦点
                $("#textarea1").focus();
                   return false;
             }
          save();
     return false;
}