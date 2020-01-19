package aos.framework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aos.framework.core.asset.WebCxt;
import aos.framework.core.utils.AOSCons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aos.framework.core.asset.AOSBeanLoader;
import aos.framework.core.utils.AOSUtils;
import aos.system.common.model.UserModel;
import aos.system.modules.cache.CacheUserDataService;

/**
 * <b>后台管理系统动态资源请求拦截器</b>
 * <p>
 *    拦截http路径的请求，进行登录身份验证和登录有效期展期 (维持心跳)
 * 
 * @author xiongchun
 */
public class HttpBackendFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(HttpBackendFilter.class);

	/**
	 * 初始化
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	/**
	 * 过滤
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// 这里可以扩展自己的相应实现
		log.info(AOSCons.CONSOLE_FLAG3 + "ClientIP：{}，URI：{}", WebCxt.getClientIpAddr(httpRequest),
				httpRequest.getRequestURI());
		// 无实现，可自行扩展

		chain.doFilter(httpRequest, httpResponse);
	}

	/**
	 * 释放资源
	 */
	@Override
	public void destroy() {
	}

}
