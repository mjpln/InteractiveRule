package com.knowology.km.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.sql.Result;
import javax.xml.namespace.QName;

import com.knowology.km.AnswerFindClient.AnswerFinderServiceDelegate;
import com.knowology.km.AnswerFindClient.AnswerFinderWebService;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.NLPAppWS.AnalyzeEnterService;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.NLPCallerWS.NLPWebService;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.webServiceClient.MsgQueueDelegate;
import com.knowology.km.webServiceClient.MsgQueueService;

public class getServiceClient {
	public static MsgQueueDelegate Client() {
		MsgQueueDelegate client = null;
		try {
			ResourceBundle resourcesTable = ResourceBundle
					.getBundle("MsgQueue");
			String ip = resourcesTable.getString("DeliveryQueueIP");
			URL url = new URL(ip);
			QName qname = new QName("http://service.DQ.knowology.com/",
					"MsgQueueService");
			MsgQueueService service = new MsgQueueService(url, qname);
			client = service.getMsgQueuePort();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return client;
	}

	public static AnswerFinderServiceDelegate AnswerClient() {
		AnswerFinderServiceDelegate client = null;
		try {
			ResourceBundle resourcesTable = ResourceBundle
					.getBundle("MsgQueue");
			String ip = resourcesTable.getString("AnswerFindServiceURL");
			URL url = new URL(ip);
			QName qname = new QName(
					"http://Services.AnswerFinderWebService.knowology.com/",
					"AnswerFinderWebService");
			AnswerFinderWebService service = new AnswerFinderWebService(url,
					qname);
			client = service.getAnswerFinderService();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return client;
	}

	/**
	 * 高级分析的接口客户端
	 * 
	 * @return 客户端
	 */
	public static NLPCaller4WSDelegate NLPCaller4WSClient() {
		NLPCaller4WSDelegate client = null;
		Result rs = UserOperResource.getConfigValue("高级分析服务地址配置", "本地服务");
        String ip =null;
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 获取配置表的ip
				ip = rs.getRows()[0].get("name").toString();
			} else {
				// 获取MsgQueue的配置文件对象
				ResourceBundle resourcesTable = ResourceBundle
						.getBundle("MsgQueue");
				// 获取配置文件的ip
				ip = resourcesTable.getString("NLPCaller4WSURL");
			}
			//ip = "http://222.186.101.212:8282/NLPWebService/NLPCallerWS?wsdl";
//			ip = "http://221.230.19.75:9191/NLPWebService/NLPCallerWS?wsdl";
//			ip = getConfigValue.jianxidizhi;
			URL url = null; 
			try {
				url = new URL(ip);
			} catch (MalformedURLException e) {
				 return client;
			}
			QName qname = new QName(
					"http://Services.NLPWebService.knowology.com/",
					"NLPWebService");
			NLPWebService service = new NLPWebService(url, qname);
			client = service.getNLPCallerWS();
		
		    return client;
	}

	/**
	 * 高级分析的接口客户端
	 * 
	 * @param ip参数IP地址
	 * @return 客户端
	 */
	public static NLPCaller4WSDelegate NLPCaller4WSClient(String ip) {
		NLPCaller4WSDelegate client = null;
		try {
			URL url = new URL(ip);
			QName qname = new QName(
					"http://Services.NLPWebService.knowology.com/",
					"NLPWebService");
			NLPWebService service = new NLPWebService(url, qname);
			client = service.getNLPCallerWS();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return client;
	}

	/**
	 * 简要分析接口的客户端
	 * 
	 * @return 客户端
	 */
	public static AnalyzeEnterDelegate NLPAppWSClient() {
		AnalyzeEnterDelegate client = null;
		Result rs = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
		try {
			// 定义ip变量
			String ip = "";
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 获取配置表的ip
				ip = rs.getRows()[0].get("name").toString();
			} else {
				// 获取MsgQueue的配置文件对象
				ResourceBundle resourcesTable = ResourceBundle
						.getBundle("MsgQueue");
				// 获取配置文件的ip
				ip = resourcesTable.getString("NLPAppWSURL");
			}
			//ip = "http://222.186.101.212:8282/NLPAppWS/AnalyzeEnterPort?wsdl"; 
			URL url = new URL(ip);
			QName qname = new QName("http://knowology.com/",
					"AnalyzeEnterService");
			AnalyzeEnterService service = new AnalyzeEnterService(url, qname);
			client = service.getAnalyzeEnterPort();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return client;
	}

	/**
	 * 简要分析接口的客户端
	 * 
	 * @param ip参数ip地址
	 * @return 客户端
	 */
	public static AnalyzeEnterDelegate NLPAppWSClient(String ip) {
		AnalyzeEnterDelegate client = null;
		try {
			URL url = new URL(ip);
			QName qname = new QName("http://knowology.com/",
					"AnalyzeEnterService");
			AnalyzeEnterService service = new AnalyzeEnterService(url, qname);
			client = service.getAnalyzeEnterPort();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return client;
	}
}
