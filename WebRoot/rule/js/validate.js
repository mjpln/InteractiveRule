// 校验URL
function checkUrl(url){
	var strRegex = '^((https|http|ftp|rtsp|mms)?://)?'
        +'(([0-9a-z_!~*().&=+$%-]+: )?[0-9a-z_!~*().&=+$%-]+@)?' //ftp的user@
        +'(([0-9]{1,3}.){3}[0-9]{1,3}|'// IP形式的URL- 199.194.52.184
        +'([0-9a-z_!~*()-]+.)*'// 域名- www.
        +'[a-z]{2,6})'//域名的扩展名
        +'(:[0-9]{1,4})?'// 端口- :80
        +'((/?)|(/[0-9a-z_!~*().;?:@&=+$,%#-]+)+/?)$';
    return new RegExp(strRegex).test(url);
}
// 校验是否为英文
function isEnglish(str){
	var strRegex = '^[a-zA-Z]+$';
    return new RegExp(strRegex).test(str);
}
// 校验按键值0-9以逗号分隔
function checkPressNumber(pressNumber) {
	var strRegex = '^(([0-9]{1})+,?)+$';
    return new RegExp(strRegex).test(pressNumber);
}

// 校验手机号码
function checkPhoneNumber(phoneNumber) {
	var strRegex = '/^1[34578]\d{9}$/';
    return new RegExp(strRegex).test(phoneNumber);
}

// 校验固话
function checkTelePhone(telePhone) {
	var strRegex = '/^((0\d{2,3})-?)(\d{7,8})(-(\d{3,}))?$/';
    return new RegExp(strRegex).test(telePhone);
}