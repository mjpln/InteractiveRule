package com.knowology.km.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.knowology.Bean.User;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.km.util.GetSession;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * �û���Ϣ������Ȩ��������
 * 
 */
public class ValidLoginInterceptor implements Interceptor {

	public String intercept(ActionInvocation arg0) throws Exception {
		// ��ȡrequest������Ϣ
		HttpServletRequest req = ServletActionContext.getRequest();
		// ��õ�ǰ����url
		String url = req.getServletPath();
		// �����������
		String type = req.getHeader("X-Requested-With");
		// ���session
		Object sre = GetSession.getSessionByKey("accessUser");
		// ��Ȩ���
		boolean authFlag = false;
		String userid = null;
		PrintWriter printWriter = null;
		if ("XMLHttpRequest".equalsIgnoreCase(type)) {// ajax����
			if (sre == null || "".equals(sre)) {// session����,����ǰ̨�Զ����ַ���js����
				printWriter = ServletActionContext.getResponse().getWriter();
				printWriter.print("ajaxSessionTimeOut");
				printWriter.flush();
				printWriter.close();
				return null;
			} else {// sessionδ���ڣ��жϲ���Ȩ��
				User user = (User) sre;
				userid = user.getUserID();
				String resourceType = req.getParameter("resourcetype");// ��Դ����
				String operationType = req.getParameter("operationtype");// ��Դ��������
				String resourceid = req.getParameter("resourceid");// ҵ����ԴID
				String robotid = req.getParameter("robotid");// ʵ�������ID
				String wordpatid = req.getParameter("wordpatid");// ��ģID
				if (resourceType != null && !"".equals(resourceType)
						&& operationType != null && !"".equals(operationType)
						&& resourceid != null && !"".equals(resourceid)) {
					operationType = operationType.toUpperCase();
					if (robotid != null && !"".equals(robotid)) {// ����ʵ�������IDȨ���ж�
						if (CommonLibPermissionDAO.isHaveOperationPermission(
								userid, resourceType, resourceid,
								operationType, robotid)) {
							authFlag = true;// ��Ȩ��
						} else {
							authFlag = false;// ��Ȩ��
						}
					} else {// Ȩ���ж�
						if (CommonLibPermissionDAO
								.isHaveOperationPermission(userid,
										resourceType, resourceid, operationType,wordpatid)) {
							authFlag = true;// ��Ȩ��
						} else {
							authFlag = false;// ��Ȩ��
						}
					}
				} else {
					return arg0.invoke();
				}
				if (authFlag) {// Ȩ������
					return arg0.invoke();
				} else {
					printWriter = ServletActionContext.getResponse()
							.getWriter();
					printWriter.print("noLimit");
					printWriter.flush();
					printWriter.close();
					return null;
				}
			}
		} else {// ��ͨhttp���� ֱ�ӷ���error
			if (sre == null || "".equals(sre)) {
				return "error";
			} else {
				return arg0.invoke();
			}
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}

}
