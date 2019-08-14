//全局ajax控制，用于session超时 提示  
//jquery
$(document).ajaxComplete(function(event, xhr, options) {
	var resText = xhr.responseText;
	if (resText == 'ajaxSessionTimeOut') {
		sessionTimeOut();
		return false;
	} else if (resText == 'noLimit') {
		noLimit();
		return false;
	} else if (resText == 'noPageLimit') {
		noPageLimit();
		return false;
	}

});

//登录信息超时
function sessionTimeOut() {
	$.messager.alert('系统提示', '用户登录会话已过期，请重新登录！');
}
//无权限  
function noLimit() {
	$.messager.alert('操作提示', '无相应操作权限，请联系系统管理员！');
}
//无页面权限
function noPageLimit() {
	$.messager.alert('操作提示', '无相应页面操作权限，请联系系统管理员！');
}
