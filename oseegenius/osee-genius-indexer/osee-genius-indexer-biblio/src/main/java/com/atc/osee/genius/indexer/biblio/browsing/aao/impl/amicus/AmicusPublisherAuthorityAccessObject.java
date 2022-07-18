package com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus;

/**
 * Authority Access Object for AMICUS Title headings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusPublisherAuthorityAccessObject extends AmicusAuthorityAccessObject 
{
	@Override
	protected String authoritiesRefSql() 
	{
		// #BZ 3005: REF_TYPE_CDE <> 1 where condition has been removed in order to match also "seeInstead" relations.
		return "SELECT SRC_PUBL_HDG_NBR,SRC.PUBL_HDG_SRT_FRM_NME, REF_TYP_CDE, TARGET.PUBL_HDG_STRNG_TXT_NME FROM PUBL_HDG SRC, PUBL_HDG TARGET, PUBL_REF WHERE SRC_PUBL_HDG_NBR=SRC.PUBL_HDG_NBR AND TRGT_PUBL_HDG_NBR=TARGET.PUBL_HDG_NBR ORDER BY SRC_PUBL_HDG_NBR ASC";
	}

	@Override
	protected String getHeadingsSql() {
		return "SELECT PUBL_HDG_SRT_FRM_NME FROM PUBL_HDG";
	}
}
