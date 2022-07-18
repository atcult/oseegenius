/*
 * Created on Sep 8, 2004
 *
 */
package librisuite.hibernate;

import java.io.Serializable;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Session;

import com.libricore.librisuite.common.HibernateUtil;

import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Persistence;
import librisuite.business.common.PersistenceState;

/**
 * Class comment
 * @author janick
 */
public abstract class CodeTable implements Persistence {

	/**
	 * 
	 */
	public CodeTable() {
		super();
	}

	private boolean obsoleteIndicator;
	
	private String shortText;

	private String longText;

	private String language;
	
	private int sequence;	
	
	
	public abstract int getNextNumber() throws DataAccessException;

	/**
		 * Getter for longLanguageText
		 * 
		 * @return longLanguageText
		 */
	public String getLongText() {
		return longText;
	}

	/**
		 * Getter for obsoleteIndicator
		 * 
		 * @return obsoleteIndicator
		 */
	public boolean isObsoleteIndicator() {
		return obsoleteIndicator;
	}

	/**
		 * Getter for shortLanguageText
		 * 
		 * @return shortLanguageText
		 */
	public String getShortText() {
		return shortText;
	}

	/**
		 * Setter for longLanguage1Text
		 * 
		 * @param string longLanguage1Text
		 */
	public void setLongText(String string) {
		longText = string;
	}

	/**
		 * Setter for obsoleteIndicator
		 * 
		 * @param b obsoleteIndicator
		 */
	public void setObsoleteIndicator(boolean b) {
		obsoleteIndicator = b;
	}

	/**
		 * Setter for shortLanguage1Text
		 * 
		 * @param string shortLanguage1Text
		 */
	public void setShortText(String string) {
		shortText = string;
	}

	/**
		 * Getter for sequence
		 * 
		 * @return sequence
		 */
	public int getSequence() {
		return sequence;
	}

	/**
		 * Setter for sequence
		 * 
		 * @param s sequence
		 */
	public void setSequence(int s) {
		sequence = s;
	}

	abstract public String getCodeString();
	/**
	 * 
	 * 
	 * @return
	 * @exception
	 * @see
	 * @since 1.0
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * 
	 * 
	 * @param string
	 * @exception
	 * @see
	 * @since 1.0
	 */
	public void setLanguage(String string) {
		language = string;
	}

	//-----------------------------
	// Persistence and LifeCycle
	//-----------------------------
	private PersistenceState persistenceState = new PersistenceState();
	
	public void evict() throws DataAccessException {
		persistenceState.evict(this);
	}

	public void generateNewKey() throws DataAccessException {
		// MIKE: this value should be assigned when all the objects are available (eng, ita, etc...) 
		// sequence = ((DAOCodeTable)getDAO()).suggestNewKey((CodeTable)this);
		
	}

	public HibernateUtil getDAO() {
		return new DAOCodeTable();
	}

	public int getUpdateStatus() {
		return persistenceState.getUpdateStatus();
	}

	public boolean isChanged() {
		return persistenceState.isChanged();
	}

	public boolean isDeleted() {
		return persistenceState.isDeleted();
	}

	public boolean isNew() {
		return persistenceState.isNew();
	}

	public void markChanged() {
		persistenceState.markChanged();
	}

	public void markDeleted() {
		persistenceState.markDeleted();
	}

	public void markNew() {
		persistenceState.markNew();
	}

	public void markUnchanged() {
		persistenceState.markUnchanged();
	}

	public void setUpdateStatus(int i) {
		persistenceState.setUpdateStatus(i);
	}

	public boolean onDelete(Session arg0) throws CallbackException {
		return persistenceState.onDelete(arg0);
	}

	public void onLoad(Session arg0, Serializable arg1) {
		persistenceState.onLoad(arg0,arg1);
	}

	public boolean onSave(Session arg0) throws CallbackException {
		return persistenceState.onSave(arg0);
	}

	public boolean onUpdate(Session arg0) throws CallbackException {
		return persistenceState.onUpdate(arg0);
	} 
	//-------------------------------
	// END Persistence and LifeCycle
	//-------------------------------

	public abstract void setExternalCode(Object extCode);

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + sequence;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CodeTable other = (CodeTable) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (sequence != other.sequence)
			return false;
		return true;
	}
 

	
}
