package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

/**
 * Authority Access Object for AMICUS Title headings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusTitleAuthorityAccessObject extends AmicusAuthorityAccessObject 
{
	@Override
	protected String authoritiesRefSql() 
	{
		// #BZ 3005: REF_TYPE_CDE <> 1 where condition has been removed in order to match also "seeInstead" relations.
		return "SELECT SRC_TTL_HDG_NBR,SRC.TTL_HDG_SRT_FORM, REF_TYP_CDE, TARGET.TTL_HDG_STRNG_TXT FROM TTL_HDG SRC, TTL_HDG TARGET, TTL_REF WHERE SRC_TTL_HDG_NBR=SRC.TTL_HDG_NBR  AND TRGT_TTL_HDG_NBR=TARGET.TTL_HDG_NBR ORDER BY SRC_TTL_HDG_NBR ASC";
	}
	
	@Override
	protected String getHeadingsSql() {
		return "SELECT TTL_HDG_SRT_FORM FROM TTL_HDG";
	}	
}
