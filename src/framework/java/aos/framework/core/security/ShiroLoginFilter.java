package aos.framework.core.security;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShiroLoginFilter extends FormAuthenticationFilter {
    /**
     * 如果isAccessAllowed返回false 则执行onAccessDenied
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        boolean isAllowed = false;
        //登录请求直接放行
        if (request instanceof HttpServletRequest) {
            if(((HttpServletRequest) request).getParameter("router")!=null){
                String path = ((HttpServletRequest) request).getParameter("router").toUpperCase();
                if (path.indexOf("LOGIN")>-1) {
                    return true;
                }
            }
        }
        isAllowed = super.isAccessAllowed(request, response, mappedValue);
        if (isAllowed) {
            //登录状态，作一些日志记录
        }
        return isAllowed;
    }

    /**
     * 未登录时的处理
     *
     * @param request
     * @param response
     * @return true-继续往下执行，false-该filter过滤器已经处理，不继续执行其他过滤器
     * @throws Exception
     */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        final HttpServletRequest request2 = (HttpServletRequest) request;
        final HttpServletResponse response2 = (HttpServletResponse) response;

        //ajax访问接口返回数据结构
        if (false) {// ajax接口

        } else {
            //其他情况
            //shiro处理
            super.onAccessDenied(request, response);

            //其他处理方式

            // 页面，直接跳转登录页面
            //redirect("login.html", request2, response2);

            //web.xml处理
            //response2.setStatus(401);// 客户试图未经授权访问受密码保护的页面。
        }
        return false;
    }
}
