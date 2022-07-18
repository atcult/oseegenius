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
package org.jzkit.search.util.Profile;

import org.jzkit.search.util.QueryModel.Internal.*;
import java.util.regex.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.persistence.*;

@javax.persistence.Entity
@DiscriminatorValue("5")
public class RegexpDBO extends RuleNodeDBO 
{ 
  public static Log LOGGER = LogFactory.getLog(RegexpDBO.class);
  
  private Pattern pattern;
  private String pattern_string;
  private String type;
  private String namespace;

  public RegexpDBO() {}
  
  public RegexpDBO(String type, String namespace, String pattern) 
  {
    this.type = type;
    this.namespace = namespace;
    this.setPattern(pattern);
  }

  public boolean isValid(final String namespace, final AttrPlusTermNode aptn, final QueryVerifyResult qvr) 
  {
    boolean result = false;
    Object attribute = aptn.getAttr(type);
    
    if (attribute == null)
    {
        qvr.setFailingAttr(type);
        qvr.setIsValid(false);
        LOGGER.debug("No mapping for attribute type "+type+" from aptn "+aptn);    	
        return false;
    }
    
	if (attribute instanceof AttrValue) 
	{
		AttrValue attributeValue = (AttrValue) attribute;
        String value = attributeValue.getWithDefaultNamespace(namespace);
        result = pattern.matcher(value).matches();
        if ( !result ) 
        {
          qvr.setFailingAttr(type);
          qvr.setIsValid(false);
        }
        return result;
	} else 
	{
        LOGGER.debug("checking simple string");
        return pattern.matcher(attribute.toString()).matches();
	}
  }

  @Column(name="MATCH_ATTR_TYPE", length=32)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Column(name="REGEXP_NAMESPACE", length=32)
  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Column(name="REGEXP_PATTERN", length=256)
  public String getPattern() {
    return pattern_string;
  }

  public void setPattern(String pattern) 
  {
	  pattern_string = pattern;

	  try 
	  {
		  this.pattern = Pattern.compile(pattern);
	  } catch ( IllegalArgumentException iae ) 
	  {
		  LOGGER.warn("Problem compiling exception "+iae);
      		throw new RuntimeException(iae);
	  }
  }

  	@Transient
  	public String getDesc() 
  	{
	  return pattern_string;
  	}

  	@Transient
  	public String getNodeType() 
  	{
	  return "Regular Expression Rule";
  	}

  	public String toString() 
  	{
	  return "Regular Expression for "+type+" : "+pattern_string;
    }
}
