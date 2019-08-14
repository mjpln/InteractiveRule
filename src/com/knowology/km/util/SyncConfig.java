/**  
 * @Project: SyncData 
 * @Title: DemoConfig.java
 * @Package demo
 * @author c_wolf your emai address
 * @date 2014-6-30 下午1:39:39
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.km.util;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.log.Log4jLogger;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;


/**
 * 内容摘要 ：
 * jfinal 的基础配置类
 * 类修改者	修改日期
 * 修改说明 
 * @ClassName DemoConfig
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-6-30 下午1:39:39
 * @version V1.0
 */

public class SyncConfig extends JFinalConfig {
	
	public static int showsql = 0;
	public static int cache = 0;
	public static int demode = 0;
	Logger log = Log4jLogger.getLogger(SyncConfig.class);
	
	@Override
	public void beforeJFinalStop(){
		log.info("系统正在关闭！");
		
	}
	
	@Override
	public void afterJFinalStart(){
		log.info("系统启动完毕！");
	}
	
	@Override
	public void configConstant(Constants me) {
		//先加载配置文件，源码显示，加载的是web-inf下的文件，如果放在src下的话，加上classes路径
		loadPropertyFile("classes/jdbc.properties");
		//上传文件配置
		try{
		showsql = getPropertyToInt("showsql");
		demode=getPropertyToInt("devmode");
		if(demode==1){
			//开发模式
			me.setDevMode(true);
		}
		cache=getPropertyToInt("cache");
		
		}catch(Exception e){
			System.out.println("系统默认不显示sql语句");
		}
	}

	
	@Override
	public void configRoute(Routes me) {
		//这个是配置的公共的路由（通用）
		
	}
	
	/**
	 * 初始化数据库连接，和查询类
	 */
	@Override
	public void configPlugin(Plugins me) {
		
		
		//获取到数据库连接属性，并将上传文件配置初始化
		String jdbcurl = getProperty("url");
		String username = getProperty("username");
		String password = getProperty("password");
		String driverName = getProperty("driverClassName");
		//记录日志类的配置
		//LogWriteThread.initLogTool(getProperty("className"));
		DruidPlugin dp = new DruidPlugin(jdbcurl,username,password,driverName);
		int initialSize = getPropertyToInt("initialSize");
		int minIdle = getPropertyToInt("minIdle");
		int maxActive = getPropertyToInt("maxActive");
		dp.set(initialSize, minIdle, maxActive);
		dp.setValidationQuery("select 1 from dual");
		
		me.add(dp);
		//ActiveRecord是操作数据库的核心类
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
		if(showsql==1){
			arp.setShowSql(true);
		}
		me.add(arp);
		//根据数据库类型， 配置Oracle方言或者mysql方言。（因为每个数据库还是有些差异的）
		if(jdbcurl.indexOf("oracle")>=0){
			arp.setDialect(new OracleDialect());	
		}else if(jdbcurl.indexOf("mysql")>=0){
			arp.setDialect(new MysqlDialect());
		}else if(jdbcurl.indexOf("postgresql")>=0){
			arp.setDialect(new PostgreSqlDialect());
		}
		// 配置属性名(字段名)大小写不敏感容器工厂
		arp.setContainerFactory(new CaseInsensitiveContainerFactory());
		//加载缓存插件
		//EhCachePlugin ec = new EhCachePlugin();
		//me.add(ec);
		//加载sql语句查询的插件,并判断是否需要些日志
		//添加定时任务插件
		//QuartzPlugin pl = new QuartzPlugin();
		//me.add(pl);
	}

	
	@Override
	public void configInterceptor(Interceptors me) {
		

	}

	
	@Override
	public void configHandler(Handlers me) {
	}

	
	
	
}
