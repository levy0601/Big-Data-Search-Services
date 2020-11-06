package com.example.demo.interceptor;

import com.example.demo.util.AuthHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.springframework.http.HttpHeaders.*;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //TODO: adding jwt token validation
        String token = request.getHeader(AUTHORIZATION);
        if(token != null){
            token = token.replaceFirst("Bearer ","");
        }
        AuthHelper.verifyGoogleToken(token);
        return true;
    }
}
