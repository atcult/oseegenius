/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.libricore.librisuite.presentation;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wim
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface Presentation {
    public void show(
        HttpServlet httpServlet,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse);

    public void processHttpRequest(HttpServletRequest request);
}
