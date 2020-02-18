package com.symverse.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(value="requestParameterInterceptor")
public class RequestParameterInterceptor  extends HandlerInterceptorAdapter  {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		 boolean accessAllow = true;
	     String requestUri = request.getRequestURI().replaceAll("//", "/");
	     log.debug("");
	     log.debug("");
	     log.debug(" [[ ↓↓ Request Argument Checking... ↓↓ ]] ");
	     log.debug("-----------★ RequestHeader parameter Information start ★----------------");
	     log.debug("RequestUri : ["+requestUri+"]");
	     Enumeration headerEnum = request.getHeaderNames();
	     while(headerEnum.hasMoreElements()) {
	    	 String headerName = (String) headerEnum.nextElement();
	    	 String headerValue = request.getHeader(headerName);
	    	 log.debug("[headerName] : "+headerName+""+" [headerValue] : "+headerValue+"");
	     }
	     log.debug("------------★ RequestHeader parameter Information End ★----------------");
	     log.debug("");
	     
	     
	     
	     Enumeration params = request.getParameterNames();
	     log.debug("------------★ RequestParamName Information start ★----------------");
	     while (params.hasMoreElements()){
	         String name = (String)params.nextElement();
	         log.debug("[RequestParamName] : "+name+""+" [RequestParamValue] : "+request.getParameter(name)+"");
	     }
	     log.debug("------------★ RequestParamName Information End   ★----------------");
	     log.debug(" [[ ↑↑ Request Argument Checking...↑↑ ]] ");
	     log.debug("");
	     log.debug("");


	     
	     return accessAllow;
	}
}
