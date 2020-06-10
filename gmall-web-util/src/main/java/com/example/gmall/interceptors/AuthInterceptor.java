package com.example.gmall.interceptors;

import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.constant.SysConstant;
import com.example.gmall.util.CookieUtil;
import com.example.gmall.util.HttpclientUtil;
import com.example.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截代码
        HandlerMethod hm = (HandlerMethod) handler;
        // 获取@LoginRequired
        LoginRequired typeAnnotation = hm.getMethod().getDeclaringClass().getAnnotation(LoginRequired.class); // 获取类上的自定义注解
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

        // 新token覆盖老token ///////////////////////////////
        String token = "";
        String cookie_token = CookieUtil.getCookieValue(request, "access_token", true);
        String return_token = request.getParameter("access_token");
        if (StringUtils.isNotBlank(cookie_token)) {
            token = cookie_token;
        }
        if (StringUtils.isNotBlank(return_token)) {
            token = return_token;

        }
        //////////////////////////////////////////////////
        // 验证
        String verifyResult = "";
        if (StringUtils.isNotBlank(token)) {
            verifyResult = HttpclientUtil.doGet("http://passport.mall.com:8085/verify?token=" + token);
        }

        // 判断请求访问的代码的注解是否是需要拦截的
        // 不必拦截则放行
        if (typeAnnotation == null) {
            if (methodAnnotation == null) {
                return true;
            }
            // 需要拦截
            boolean value = methodAnnotation.value();
            // 必须登录
            if (value) {
                StringBuffer originUrl = request.getRequestURL();
                // 必须登录
                // token为空，则重定向到认证中心，进行登录
                if (StringUtils.isBlank(token)) {
                    response.sendRedirect("http://passport.mall.com:8085/index?originUrl=" + originUrl);
                    return false;
                }
                // token不为空，则去认证中心校验
                // 校验失败,则重定向到认证中心，进行登录
                if (verifyResult == null) {
                    response.sendRedirect("http://passport.mall.com:8085/index?originUrl=" + originUrl);
                    return false;
                }

                //校验成功， 将token存入此请求域名下的cookie中
                CookieUtil.setCookie(request, response, "access_token", token, 60 * 60 * 72, true);
                Map<String, Object> userMap = JwtUtil.decode(token, SysConstant.ENCRYKEY, SysConstant.SALT);
                Object memberId = userMap.get("id");
                request.setAttribute("memberId", memberId);
                // 放行
                return true;

            }
            // 我的思路
            // 如购物车业务中，若cookie中的用户与登录用户为同一用户，则该用户登录后需要合并购物车
            // 因此，此处获取最新的token并将其中的用户信息传给购物车业务
            // @TODO 若token不空，则获取其中的用户信息，此处使用模拟数据,合并购物车使用
            if ("success".equals(verifyResult)) {
                Map<String, Object> userMap = JwtUtil.decode(token, SysConstant.ENCRYKEY, SysConstant.SALT);
                Object memberId = userMap.get("id");
                request.setAttribute("memberId", memberId);
                request.setAttribute("memberId", memberId);
            }
            return true;
        }
        StringBuffer originUrl = request.getRequestURL();
        // 必须登录
        // token为空，则重定向到认证中心，进行登录
        if (StringUtils.isBlank(token)) {
            response.sendRedirect("http://passport.mall.com:8085/index?originUrl=" + originUrl);
            return false;
        }
        // token不为空，则去认证中心校验
        // 校验失败,则重定向到认证中心，进行登录
        if (verifyResult == null) {
            response.sendRedirect("http://passport.mall.com:8085/index?originUrl=" + originUrl);
            return false;
        }

        //校验成功， 将token存入此请求域名下的cookie中
        CookieUtil.setCookie(request, response, "access_token", token, 60 * 60 * 72, true);
        Map<String, Object> userMap = JwtUtil.decode(token, SysConstant.ENCRYKEY, SysConstant.SALT);
        Object memberId = userMap.get("id");
        request.setAttribute("memberId", memberId);
        // 放行
        return true;

    }
}
