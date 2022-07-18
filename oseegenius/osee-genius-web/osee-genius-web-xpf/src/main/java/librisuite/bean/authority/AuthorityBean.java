/*
 * 
 * AuthorityBean.java
 */
package librisuite.bean.authority;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import librisuite.bean.LibrisuiteBean;
import librisuite.bean.searching.BrowseBean;
import librisuite.business.authority.AuthorityNote;
import librisuite.business.authority.DAOAuthority;
import librisuite.business.common.DataAccessException;
import librisuite.business.descriptor.Descriptor;

/**
 * Manages searching (auth)
 * 

 */
public class AuthorityBean extends LibrisuiteBean {

	private Descriptor descriptor;
	private Map<String, List<AuthorityNote>> itemNotes;
	private DAOAuthority daoAut = new DAOAuthority();

	public static AuthorityBean getInstance(HttpServletRequest request) {
		AuthorityBean bean = (AuthorityBean) getSessionAttribute(request,
			  AuthorityBean.class);
		if (bean == null) {
			bean = new AuthorityBean();
			bean.setSessionAttribute(request, AuthorityBean.class);
		}
		return bean;
	}
	
	public Map<String, List<AuthorityNote>> getItemNotes() {
		return itemNotes;
	}

	public void setItemNotes(Map<String, List<AuthorityNote>> itemNotes) {
		this.itemNotes = itemNotes;
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(Descriptor descriptor) {
		this.descriptor = descriptor;
	}


	public Map<String, List<AuthorityNote>> getAuthorityNotesList(
			String indexName, int headingNbr, int cataloguingView) throws DataAccessException {
		Map<String, List<AuthorityNote>> authorityNotesList = null;
		authorityNotesList = daoAut
				.getAuthorityNotesList(indexName, headingNbr,cataloguingView);
		return authorityNotesList;
	}
	
	

}
