$(function(){  //页面加载时执行
	isLogin();//判断是否登录
//	MsgRoll();//消息滚动
	enterSubmit();
});
function logout() {
	//登出
	$.ajax({
		type : "POST",
		url : "logout.action",
		dataType: "json",
		success : function(data, textStatus, jqXHR){
			top.location="login/login.jsp";
			//document.location="login/login.jsp";
		},
		error : function(jqXHR, textStatus, errorThrown) {
			top.location.reload();
		}
	});
}
//验证登录
function isLogin(){
	$.ajax({
		type : "POST",
		url : "common/getUserId.action",
		async: false,
		dataType: "json",
		success : function(data, textStatus, jqXHR){
		  	var user=data["userid"];
               if(user==' '){
                  document.location="login/login.jsp";
                }else{
                  $("#operator").text(user);
                 }
		},
		error : function(jqXHR, textStatus, errorThrown) {
			document.location="login/login.jsp";
		}
	});
}
function autoHeight(iframe){
	if(iframe.Document) {//ie自有属性
		iframe.style.height = iframe.Document.documentElement.scrollHeight;
	} else if(iframe.contentDocument) {//ie,firefox,chrome,opera,safari
		iframe.height = iframe.contentDocument.body.offsetHeight + 16;
	}
}
 var index = 0;
function createTab()
{	
	index++;
	var operator=$("#operator").text();
	var content= '<iframe name="iframe'+index+'" id="iframe'+index+'"  src="demo.jsp?operator='+operator+'" frameborder="0" width=100% onload="autoHeight(this)"></iframe>';
	$("#div_user_tabs").tabs('add',{  
			id:index,  
			title: '客户',    
			content:content,  
			closable: true 
		});
}
function MsgRoll(){
	jQuery(".txtMarquee-left").slide({mainCell:".bd ul",autoPlay:true,effect:"leftMarquee",vis:2,interTime:50});
}
function enterSubmit() {
//回车确认
$(function () {
  document.onkeypress = function (e) {
      var ev = document.all ? window.event : e;
      if (ev.keyCode == 13) {
        var pp = $('#div_user_tabs').tabs('getSelected'); 
		var id=$(pp).attr('id'); 
		var str="iframe"+id;
		$(window.frames[str].document).find("#save").click(); 
         return false;
      }
  }
});
}