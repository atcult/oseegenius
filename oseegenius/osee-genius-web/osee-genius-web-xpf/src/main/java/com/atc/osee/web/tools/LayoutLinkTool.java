package com.atc.osee.web.tools;

import org.apache.velocity.tools.view.LinkTool;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
  
/**
 * This is meant to demonstrate how to extend the LinkTool to
 * avoid the manual use of "magic" or common query parameters.
 * So instead of doing $link.param('layout', 'Table.vm') in your
 * template, you would just do $link.layout('Table').
 *
 * @author Nathan Bubna
 */
public class LayoutLinkTool extends LinkTool
{
	private static final String TEMPLATE_SUFFIX = ".vm";
	
	/**
	 * Returns the layout associated with the given identifier.
	 * 
	 * @param identifier the layout identifier.
	 * @return the layout associated with the given identifier.
	 */
	public LayoutLinkTool layout(final Object identifier)
	{
		return (identifier != null) ? layout(identifier.toString()) : null;
    }

	/**
	 * Returns the layout associated with the given name.
	 * 
	 * @param layoutName the layout name.
	 * @return the layout associated with the given name.
	 */
    public LayoutLinkTool layout(final String layoutName)
    {
        return (layoutName != null && !layoutName.endsWith(TEMPLATE_SUFFIX))
        		? (LayoutLinkTool)param(VelocityLayoutServlet.KEY_LAYOUT, layoutName + TEMPLATE_SUFFIX)
        		: (LayoutLinkTool)param(VelocityLayoutServlet.KEY_LAYOUT, layoutName);		
	}
}