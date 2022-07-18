/*
 * Created on May 6, 2004
 * */
package librisuite.hibernate;

import java.io.Serializable;

import librisuite.business.common.CrossReferenceExistsException;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.Defaults;
import librisuite.business.common.PersistenceState;
import librisuite.business.common.PersistentObjectWithView;
import librisuite.business.common.View;
import librisuite.business.crossreference.DAOCrossReferences;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.Descriptor;
import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Session;

import com.libricore.librisuite.common.HibernateUtil;

/**
 * abstract class for all cross-reference tables (including NME_NME_TTL_REF
 * and TTL_NME_TTL_REF)
 * @author paulm
 * @version $Revision: 1.10 $, $Date: 2006/01/19 12:31:26 $
 * @since 1.0
 */
public abstract class REF
	extends PersistenceState
	implements Serializable, Cloneable, PersistentObjectWithView {
	private static final DAOCrossReferences theDAO = new DAOCrossReferences();
	private final PersistenceState persistenceState = new PersistenceState();
	private REF_KEY key = new REF_KEY();
	abstract public DAODescriptor getTargetDAO();
	private Character printConstant;
	private Character noteGeneration;
	private Character formerHeading;
	private Character authorityStructure;
	private Character earlierRules;
    private Character verificationLevel;
    
    public Character getPrintConstant() {
		return printConstant;
	}

	public void setPrintConstant(Character printConstant) {
		this.printConstant = printConstant;
	}

	public Character getNoteGeneration() {
		return noteGeneration;
	}

	public void setNoteGeneration(Character noteGeneration) {
		this.noteGeneration = noteGeneration;
	}

	public Character getFormerHeading() {
		return formerHeading;
	}

	public void setFormerHeading(Character formerHeading) {
		this.formerHeading = formerHeading;
	}

	public Character getAuthorityStructure() {
		return authorityStructure;
	}

	public void setAuthorityStructure(Character authorityStructure) {
		this.authorityStructure = authorityStructure;
	}

	public Character getEarlierRules() {
		return earlierRules;
	}

	public void setEarlierRules(Character earlierRules) {
		this.earlierRules = earlierRules;
	}

	public Character getVerificationLevel() {
		return verificationLevel;
	}

	public void setVerificationLevel(Character verificationLevel) {
		this.verificationLevel = verificationLevel;
	}



	public REF() {
		setDefault();
	}

	public static REF add(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView,boolean isAttribute)
		throws DataAccessException {

		// instantiate the appropriate REF type and populate key from arguments
		REF ref =
			REF.newInstance(source, target, referenceType, cataloguingView,isAttribute);

		DAOCrossReferences dao = (DAOCrossReferences) ref.getDAO();
		// verify that this xref doesn't already exist in the database
		if (dao.load(source, target, referenceType, cataloguingView) != null) {
			throw new CrossReferenceExistsException();
		}
		// add the reference
		dao.save(ref);

		return ref;
	}

	public void setDefault() {
		this.setAuthorityStructure('a');
		this.setEarlierRules('x');
		this.setFormerHeading('x');
		if (getKey() != null
			&& ReferenceType.isEquivalence(getKey().getType())) {
			this.setNoteGeneration('x');
		} else {
			this.setNoteGeneration('@');
		}
		this.setPrintConstant(Defaults.getChar("authority.reference.specialRelationship"));
		this.setVerificationLevel('1');
	}

	static public REF newInstance(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView, boolean isAttribute) {
		REF ref = null;
		try {
			/*if(isAttribute)
				ref = new THS_ATRIB();
			else*/
			ref =
				(REF) source.getReferenceClass(target.getClass()).newInstance();
			;
		} catch (Exception e) {
			throw new RuntimeException("error creating cross-reference object");
		}

		ref.init(source, target, referenceType, cataloguingView);
		return ref;
	}

	public void init(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView) {
		setKey(new REF_KEY());
		getKey().setSource(source.getKey().getHeadingNumber());
		getKey().setTarget(target.getKey().getHeadingNumber());
		getKey().setType(referenceType);
		getKey().setUserViewString(View.makeSingleViewString(cataloguingView));
		setDefault();
	}

	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Getter for key
	 * 
	 * @return key
	 */
	public REF_KEY getKey() {
		/* getKey is made private so that implementers are required to provide individual
		 * delegate methods.  This so that both NME_REF and NME_NME_TTL_REF can behave 
		 * polymorphically
		 */
		return key;
	}

	/**
	 * Setter for key
	 * 
	 * @param nme_ref_key key
	 */
	public void setKey(REF_KEY nme_ref_key) {
		key = nme_ref_key;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			REF_KEY newKey = (REF_KEY) getKey().clone();
			REF result = (REF) super.clone();
			result.setKey(newKey);
			return result;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistentObjectWithView#getUserViewString()
	 */
	public String getUserViewString() {
		return getKey().getUserViewString();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistentObjectWithView#setUserViewString(java.lang.String)
	 */
	public void setUserViewString(String s) {
		getKey().setUserViewString(s);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistentObject#generateNewKey()
	 */
	public void generateNewKey() throws DataAccessException {
		// do nothing
		return;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj.getClass().equals(this.getClass())) {
			return this.getKey().equals(((REF) obj).getKey());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getKey().hashCode();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#getUpdateStatus()
	 */
	public int getUpdateStatus() {
		return persistenceState.getUpdateStatus();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#isChanged()
	 */
	public boolean isChanged() {
		return persistenceState.isChanged();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#isDeleted()
	 */
	public boolean isDeleted() {
		return persistenceState.isDeleted();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#isNew()
	 */
	public boolean isNew() {
		return persistenceState.isNew();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#isRemoved()
	 */
	public boolean isRemoved() {
		return persistenceState.isRemoved();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#markChanged()
	 */
	public void markChanged() {
		persistenceState.markChanged();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#markDeleted()
	 */
	public void markDeleted() {
		persistenceState.markDeleted();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#markNew()
	 */
	public void markNew() {
		persistenceState.markNew();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#markUnchanged()
	 */
	public void markUnchanged() {
		persistenceState.markUnchanged();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#onDelete(net.sf.hibernate.Session)
	 */
	public boolean onDelete(Session s) throws CallbackException {
		return persistenceState.onDelete(s);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#onLoad(net.sf.hibernate.Session, java.io.Serializable)
	 */
	public void onLoad(Session s, Serializable id) {
		persistenceState.onLoad(s, id);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#onSave(net.sf.hibernate.Session)
	 */
	public boolean onSave(Session s) throws CallbackException {
		return persistenceState.onSave(s);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#onUpdate(net.sf.hibernate.Session)
	 */
	public boolean onUpdate(Session s) throws CallbackException {
		return persistenceState.onUpdate(s);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#setUpdateStatus(int)
	 */
	public void setUpdateStatus(int i) {
		persistenceState.setUpdateStatus(i);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.Persistence#evict()
	 */
	public void evict() throws DataAccessException {
		persistenceState.evict(this);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public int getSource() {
		return key.getSource();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public int getTarget() {
		return key.getTarget();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public Short getType() {
		return key.getType();
	}


	/**
	 * 
	 * @since 1.0
	 */
	public void setSource(int i) {
		key.setSource(i);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setTarget(int i) {
		key.setTarget(i);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setType(short s) {
		key.setType(s);
	}

	public REF createReciprocal() {
		REF result = (REF) this.clone();
		result.setSource(getTarget());
		result.setTarget(getSource());
		result.setType(ReferenceType.getReciprocal(result.getType()));
		return result;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.common.PersistenceState#getDAO()
	 */
	public HibernateUtil getDAO() {
		return theDAO;
	}

}
