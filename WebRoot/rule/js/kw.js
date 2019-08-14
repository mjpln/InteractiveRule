//获得地市信息comboxtree
function getSearchCityTree(id,city){
//	var city = "南京市,合肥市,江苏省,北京市";
	var id ="#"+id;
	$(id).combotree({
		url:  '../getCityTreeByLoginInfo.action',
		editable:false, 
		multiple:false,
		queryParams:{
			local : city
		}
	}); 
}
getSearchCityTree("kw_search_city","");

//知识类别，下拉框
$('#kw_type').combobox({    
    url:'../getKwTypes.action',    
    valueField: 'serviceid',    
    textField: 'service'  ,
    onLoadSuccess: function () { 
    	var datas = $('#kw_type').combobox('getData');
        if (datas.length > 0) {
            $('#kw_type').combobox('select', datas[0].serviceid);

           //页面初始化，搜索数据
           loadSceneRelation();
        } 
	}
});  

//加载业务摘要对应关系
function loadSceneRelation(){ 
	var docName   = $(".kw-wrap [name=keyword]").val(),
		serviceid = $('#kw_type').combobox("getValue"),
		city      = $('#kw_search_city').combotree("getValue");
	
	$("#kw-knowology-info")
			.datagrid(
					{
						url :  '../getKWDatas.action',
						width : 1000,
						height : 395,
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
									width : 290
								},
								{
									field : 'service',
									title : '知识类别',
									width : 260
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
	loadSceneRelation();
});

//保存数据
$(document).on("click", ".save", function(){
	var $kShowRows = $(".kw-wrap .kshow .datagrid-row");
	var $checkboxes = $kShowRows.find("input"),
		$docNames   = $kShowRows.find("[field=docName] div"),
		$services   = $kShowRows.find("[field=service] div"),
		$cities  	= $kShowRows.find("[field=city] div");
	
	//要抛出的数据
	var toSaveDatas = [],
		checkboxLen = $checkboxes.length;
	
	for (var i = 0; i < checkboxLen; i++) {
		
		if ($($checkboxes[i]).is(":checked")) {
			var obj = {
				docName: $($docNames[i]).text(),
				service: $($services[i]).text(),
				city   : $($cities[i]).text()
			}
			
			toSaveDatas.push(obj);
		}
	}
	
	alert(toSaveDatas);
});


