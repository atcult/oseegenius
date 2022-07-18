/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package com.atc.osee.z3950.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Abstract definition of a SOLR service.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class SolrService implements ApplicationContextAware
{
	protected String serviceUrl;
	protected String code;
	
	protected ApplicationContext applicationContext;
	
	@SuppressWarnings("unchecked")
	protected Map archetypes = new HashMap();
	
	public void setServiceUrl(String serviceUrl) 
	{
		this.serviceUrl = serviceUrl;
	}
	
	public String getServiceUrl() 
	{
		return serviceUrl;
	}
	
	public String getCode() 
	{
		return code;
	}
	
	public void setCode(String code) 
	{
		this.code = code;
	}
	
	public void close() 
	{
		// Nothing to be done here...
	}
	
	@SuppressWarnings({ "unchecked" })
	public void setRecordArchetypes(Map archetypes) 
	{
		this.archetypes = archetypes;
	}

	@SuppressWarnings("unchecked")
	public Map getRecordArchetypes() 
	{
		return archetypes;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) 
	{
		this.applicationContext = applicationContext;
	}
}