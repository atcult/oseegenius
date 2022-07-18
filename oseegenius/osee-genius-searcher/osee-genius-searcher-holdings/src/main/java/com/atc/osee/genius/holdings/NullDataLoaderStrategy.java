/*
     Copyright (c) 2010 @Cult s.r.l. All rights reserved.
   
    @Cult s.r.l. makes no representations or warranties about the
    suitability of the software, either express or implied, including
    but not limited to the implied warranties of merchantability, fitness
    for a particular purpose, or non-infringement.
   
    @Cult s.r.l.not be liable for any damage suffered by
    licensee as a result of using, modifying or distributing this software
    or its derivates.
   
    This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.genius.holdings;

import javax.sql.DataSource;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

/**
 * An OFF state for data loader strategy.
 * This will be used in case the concrete strategy implementor raises 
 * an error during initialisation.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class NullDataLoaderStrategy implements DataLoaderStrategy 
{
	private static final NamedList<NamedList<Object>> EMPTY_HOLDING_DATA = new SimpleOrderedMap<NamedList<Object>>();

	@Override
	public NamedList<NamedList<Object>> getHoldingData(final String uri) throws UnableToLoadHoldingsDataException 
	{
		return EMPTY_HOLDING_DATA;
	}

	@Override
	public void setDataSource(final DataSource datasource) 
	{
		// Nothing to be done here...
	}
}
