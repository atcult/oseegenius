package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

/**
 * Authority Access Object for AMICUS Name headings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusNameAuthorityAccessObject extends AmicusAuthorityAccessObject 
{
	@Override
	protected String authoritiesRefSql() 
	{
		// #BZ 3005: REF_TYPE_CDE <> 1 where condition has been removed in order to match also "seeInstead" relations.
		return "SELECT SRC_NME_HDG_NBR,SRC.NME_HDG_SRT_FORM, REF_TYP_CDE, TARGET.NME_HDG_STRNG_TXT FROM NME_HDG SRC, NME_HDG TARGET, NME_REF WHERE SRC_NME_HDG_NBR=SRC.NME_HDG_NBR AND TRGT_NME_HDG_NBR=TARGET.NME_HDG_NBR ORDER BY SRC_NME_HDG_NBR ASC";
	}

	@Override
	protected String getHeadingsSql() {
		return "SELECT NME_HDG_SRT_FORM FROM NME_HDG";
	}
}