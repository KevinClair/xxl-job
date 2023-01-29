package com.xxl.job.admin.controller.interceptor;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.common.constant.Constants;
import com.xxl.job.common.utils.I18nUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor {

	private final LoginService loginService;

	private final XxlJobAdminConfig adminConfig;

	public PermissionInterceptor(LoginService loginService, XxlJobAdminConfig adminConfig) {
		this.loginService = loginService;
		this.adminConfig = adminConfig;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (!(handler instanceof HandlerMethod)) {
			return true;    // proceed with the next interceptor
		}

		// if need login
		boolean needLogin = true;
		boolean needAdminuser = false;
		HandlerMethod method = (HandlerMethod)handler;
		PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
		if (permission!=null) {
			needLogin = permission.limit();
			needAdminuser = permission.adminuser();
		}

		if (needLogin) {
			XxlJobUser loginUser = loginService.ifLogin(request, response);
			if (loginUser == null) {
				response.setStatus(302);
				response.setHeader("location", request.getContextPath() + "/toLogin");
				return false;
			}
			if (needAdminuser && loginUser.getRole() != 1) {
				throw new RuntimeException(I18nUtil.getString("system_permission_limit"));
			}
			request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);
		}

		// 对JobApiController下的接口统一进行token的校验
		if (request.getRequestURI().startsWith(Constants.NEED_CHECK_TOKEN_URI)) {
			if (StringUtils.hasText(adminConfig.getAccessToken()) && !adminConfig.getAccessToken().equals(request.getHeader(Constants.XXL_JOB_ACCESS_TOKEN))) {
				throw new XxlJobException("The access token is wrong.");
			}
		}

		return true;    // proceed with the next interceptor
	}
	
}
