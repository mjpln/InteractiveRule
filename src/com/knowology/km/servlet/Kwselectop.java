/*
*	create by lxf
*/
package com.knowology.km.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.dal.Database;
import javax.servlet.jsp.jstl.sql.Result;

public class Kwselectop extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public Kwselectop() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		StringBuilder sql = new StringBuilder();
		sql.append("select serviceid,service from SERVICEATTRNAME2COLNUM where name='docName'");
		Result rs = Database.executeQuery(sql.toString());
		
		JSONArray types = new JSONArray();
		
	
		if (rs != null && rs.getRowCount() > 0) {
			
			int rowCount = rs.getRowCount();
			// 循环遍历数据源
			for (int i = 0; i < rowCount; i++) {
				
				// 定义json对象
				JSONObject obj = new JSONObject();
				SortedMap row = rs.getRows()[i];
				// 生成attrid对象
				obj.put("service", row.get("service").toString().replace("信息表", ""));
				obj.put("serviceid", row.get("serviceid").toString());
				// 将生成的对象放入jsonArr数组中
				types.add(obj);
			}
		}
		
		request.setAttribute("types", types);
		response.setContentType("text/html;charset=utf-8;pageEncoding=utf-8;");
	    request.getRequestDispatcher("/rule/kwselectpage.jsp").forward(request,response); 
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the POST method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
