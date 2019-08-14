/*
 * 执行sqlid对应的sql语句，返回结果由process进行处理
 */
function select(ids,sqlids,paras,level,callback,sync){
	var request={
			sqlid:sqlids[level],
			paras:paras[level]
		};
		var data={
			request:JSON.stringify(request)
		};		
		 $.ajax({
             type: "POST",
             url: "/KM/common/index",
             sync: sync, 
             data: data,
             success: function(data, textStatus, jqXHR) {
			 	callback(ids,sqlids,paras,level,data);
		 }
     });
}

function filldll(ids,sqlids,paras,level,data){	
	var columns =data.rows;
	//alert(columns);
	var id="#"+ids[level];
	$(id).empty();
	$(id).append("<option value='请选择'>请选择</option>");
	for(var i=0;i<columns.length;i++){
		$(id).append("<option value='"+columns[i]["NAME"]+"'>"+columns[i]["NAME"]+"</option>");
	}
	if(level==ids.length-1)return;
	level++;
	$(id).change(function(){
		(function(_ids,_sqlids,_paras,_level,_data){
			_paras[_level]=[];
			for(var j=0;j<_level;j++){
				_paras[_level].push($("#"+ids[j]).val());
			}
			select(_ids,_sqlids,_paras,_level,filldll,true);
		})(ids,sqlids,paras,level,data);
	});
	$(id).change();
}



