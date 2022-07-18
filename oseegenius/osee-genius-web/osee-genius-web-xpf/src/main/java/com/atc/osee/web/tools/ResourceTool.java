package com.atc.osee.web.tools;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.LocaleUtils;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.SkipSetters;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.model.Visit;

/**
 * OseeGenius -W- implementation of Velocity Resource Tool.
 * This is the main entry point for i18n.
 * 
 * @author agazzarini
 * @since 1.0
 */
@DefaultKey("text")
@SkipSetters
@ValidScope({"session","request"})
public class ResourceTool extends SafeConfig implements Serializable 
{
	private static final long serialVersionUID = -458477533437751160L;

	public static final String BUNDLES_KEY = "bundles";

    private boolean useAdditionalMessageBundle;
    
    private Visit visit;
    
    @Override
    protected void configure(final ValueParser parser)
    {
    	HttpSession session = (HttpSession) parser.get("session");
    	if (session != null)
    	{
    		visit = (Visit) session.getAttribute(HttpAttribute.VISIT);
    	} 
		 
        try
        {
        	ResourceBundle.getBundle(IConstants.ADDITIONAL_MESSAGE_BUNDLE_NAME, Locale.getDefault());
        	useAdditionalMessageBundle = true;
        } catch (Exception exception)
        {
        	useAdditionalMessageBundle = false;
        }
    }

    /**
     * Accepts objects and uses their string value as the key.
     * If the key is null or no corresponding value will be found 
     * this method will return null.
     * 
     * @see Key#get(String)
     * @param objectKey the key as an object.
     * @return the Key associated with the given key.
     */
    public String get(final Object objectKey)
    {
        String key = (objectKey == null) ? null : String.valueOf(objectKey);
        return get(key);
    }

    /**
     * Returns the value associated with the given key.
     * 
     * @param key the key value.
     * @return the value associated with the given key.
     */
    public String get(final String key)
    {
    	String result = null;
    	if (useAdditionalMessageBundle)
    	{
        	try 
        	{
        		ResourceBundle additionalMessageBundle = ResourceBundle.getBundle(IConstants.ADDITIONAL_MESSAGE_BUNDLE_NAME,getLocale());        		
        		if (additionalMessageBundle.containsKey(key))
        		{
        			result = additionalMessageBundle.getString(key);    		
        		}	
        	} catch (Exception exception)
        	{
        		// Ignore
        	}
    	}
    	
    	if (result == null)
    	{
	    	try 
	    	{
	    		result = ResourceBundle.getBundle(IConstants.DEFAULT_MESSAGE_BUNDLE_NAME,getLocale()).getString(key);    		
	    	} catch (Exception exception)
	    	{
	    		result = "???" + key + "???";
	    	}
    	}
    	
    	return result;
    }

    public String bundleGet(final String bundle, final String key)
    {
    	try 
    	{
    		return ResourceBundle.getBundle(bundle, getLocale()).getString(key);    		
    	} catch (Exception exception)
    	{
    		return "???" + key + "???";
    	}
    }
    
    public Enumeration<String> getBundle (final String bundle) {
    	try {
    		return ResourceBundle.getBundle(bundle, getLocale()).getKeys();
    	} catch (Exception exception) {
    		return null;
    	}
    	
    }
    
    public String bundleGet(final String bundle, final String key, final String locale)
    {
    	try 
    	{
    		return ResourceBundle.getBundle(bundle, new Locale(locale)).getString(key);    		
    	} catch (Exception exception)
    	{
    		return "???" + key + "???";
    	}
    }
    
    public String bundleGetWithOverride(final String bundle, final String key)
    {
    	String result = null;
    	try 
    	{
    		result = ResourceBundle.getBundle("custom_" + bundle,getLocale()).getString(key);    		
    	} catch (Exception exception)
    	{
    	}
    	
    	if (result == null)
    	{
	    	try 
	    	{
	    		result = ResourceBundle.getBundle(bundle,getLocale()).getString(key);    		
	    	} catch (Exception exception)
	    	{
	    		result = "???" + key + "???";
	    	}
    	}
    	return result;
    }

    /**
     * Returns the locale associated with this resource tool.
     * Specifically, that will be the user preferred locale.
     * 
     * @return the locale associated with this resource tool.
     */
    public Locale getLocale()
    {
    	return visit.getPreferredLocale();
    }
    
    /**
     * Retrieves the {@link ResourceBundle} for the specified baseName
     * and locale, if such exists.  If the baseName or locale is null
     * or if the locale argument cannot be converted to a {@link Locale},
     * then this will return null.
     * 
     * @param baseName the base name of the requested resource.
     * @param localeObject the locale as a java.lang.Object
     */
    protected ResourceBundle getBundle(final String baseName, final Object localeObject)
    {
        Locale locale = (localeObject == null) ? getLocale() : toLocale(localeObject);
        return (baseName ==  null || locale == null)
        	? null
        	: ResourceBundle.getBundle(baseName, locale);
    }

    private Locale toLocale(final Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Locale)
        {
            return (Locale)obj;
        }
        String s = String.valueOf(obj);
        return ConversionUtils.toLocale(s);
    }
}
