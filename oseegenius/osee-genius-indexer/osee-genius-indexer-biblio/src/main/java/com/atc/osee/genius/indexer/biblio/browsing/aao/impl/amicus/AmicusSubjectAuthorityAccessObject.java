package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

/**
 * Authority Access Object for AMICUS Subject headings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusSubjectAuthorityAccessObject extends AmicusAuthorityAccessObject 
{
	@Override
	protected String authoritiesRefSql() 
	{
		// #BZ 3005: REF_TYPE_CDE <> 1 where condition has been removed in order to match also "seeInstead" relations.
		return "SELECT SRC_SBJCT_HDG_NBR,SRC.SBJCT_HDG_SRT_FORM, REF_TYP_CDE, TARGET.SBJCT_HDG_STRNG_TXT FROM SBJCT_HDG SRC, SBJCT_HDG TARGET, SBJCT_REF WHERE SRC_SBJCT_HDG_NBR=SRC.SBJCT_HDG_NBR AND TRGT_SBJCT_HDG_NBR=TARGET.SBJCT_HDG_NBR ORDER BY SRC_SBJCT_HDG_NBR ASC";
	}
	
	@Override
	protected String getHeadingsSql() {
		return "SELECT SBJCT_HDG_SRT_FORM FROM SBJCT_HDG";
	}
}