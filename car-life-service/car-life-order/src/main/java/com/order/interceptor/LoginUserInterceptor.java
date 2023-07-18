//package com.order.interceptor;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * 拦截器
// */
////@Component
//public class LoginUserInterceptor implements HandlerInterceptor {
//    public static ThreadLocal<String> loginUser = new ThreadLocal();
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("token");
//        if (token != null){
//            loginUser.set(token);
//            return true;
//        }
//        request.getSession().setAttribute("mes","请进行登录操作");
//        response.sendRedirect("/登录地址");
//        return false;
//    }
//}
