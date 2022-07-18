/*
 * (c) LibriCore
 * 
 * Created on Mar 30, 2006
 * 
 * UTF8Filter.java
 */
package com.libricore.librisuite.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Ensures that the incoming request URL is treated as UTF8
 * @author paulm
 * @version $Revision: 1.2 $, $Date: 2006/04/05 14:30:56 $
 * @since 1.0
 */
public class UTF8Filter implements Filter {

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain)
		throws IOException, ServletException {
		if (((HttpServletRequest) request).getMethod().equals("GET")) {
			HttpServletRequestWrapper myWrapper =
				new HttpServletRequestWrapper((HttpServletRequest) request) {
				public String[] getParameterValues(String name) {
					List result = new ArrayList();
					String[] values = super.getParameterValues(name);
					if (values == null) {
						return null;
					}
					for (int i = 0; i < values.length; i++) {
						try {
							result.add(
								new String(
									values[i].getBytes("ISO-8859-1"),
									"UTF8"));
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException("Latin 1 not supported");
						}
					}
					return (String[]) result.toArray(new String[0]);
				}
			};
			chain.doFilter(myWrapper, response);
		} else {
			if (request.getCharacterEncoding() == null) {
				request.setCharacterEncoding("UTF8");
			}
			chain.doFilter(request, response);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

}
