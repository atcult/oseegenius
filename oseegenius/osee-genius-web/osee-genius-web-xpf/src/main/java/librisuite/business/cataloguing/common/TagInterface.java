/*
 * (c) LibriCore
 * 
 * Created on Nov 3, 2005
 * 
 * TagInterface.java
 */
package librisuite.business.cataloguing.common;

import java.io.Serializable;
import java.util.List;

import librisuite.business.cataloguing.bibliographic.MarcCorrelationException;
import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.PersistenceState;
import librisuite.business.exception.ValidationException;
import librisuite.hibernate.CorrelationKey;
import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Session;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author paulm
 * @version $Revision: 1.5 $, $Date: 2006/11/23 15:01:47 $
 * @since 1.0
 */
public interface TagInterface {
	public abstract Object clone();

	/**
	 * indicates whether the proposed change in correlation values would result in a
	 * new persistent key for this tag.  Values of -1 are ignored.
	 *
	 */
	public abstract boolean correlationChangeAffectsKey(CorrelationValues v);
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object obj);
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void evict() throws DataAccessException;
	/**
	 * This method is used to generated the model xml.
	 * 
	 * @since 1.0
	 */
	public abstract Element generateModelXmlElement(Document xmlDocument);
	/**
	 * This method is used to generated the tags content in the model xml.
	 * 
	 * @since 1.0
	 */
	public abstract Element generateModelXmlElementContent(Document xmlDocument);
	public abstract void generateNewKey() throws DataAccessException;
	/**
	 * @return the tag category used in determining MARC coding (correlation)
	 */
	public abstract short getCategory();
	/**
	 * @param i - the correlation value wanted (1, 2, or 3)
	 * @return the appropriate correlation value for determining MARC coding (-1 if no
	 * value is available or known)
	 */
	public abstract short getCorrelation(int i);
	public abstract CorrelationValues getCorrelationValues();
	public abstract DAOCodeTable getDaoCodeTable();
	/**
	 * override when displayed tag category differs from the correlation category
	 * 
	 * @since 1.0
	 */

	public abstract short getDisplayCategory();

	/**
	 * true if the display of a headingType option list is appropriate while editing
	 * 
	 * @since 1.0
	 */
	public abstract boolean getDisplaysHeadingType();
	/**
	 * @return the first correlation list for this tag
	 * entry
	 */
	public abstract List getFirstCorrelationList() throws DataAccessException;
	/**
	 * provides abstract access to item identifier (bib or aut)
	 * 
	 * @since 1.0
	 */
	public abstract int getItemNumber();

	/**
	 * @return the MARC tag and indicators for this tag
	 *
	 */
	public abstract CorrelationKey getMarcEncoding()
		throws DataAccessException, MarcCorrelationException;
	/**
	 * 
	 * @since 1.0
	 */
	public abstract PersistenceState getPersistenceState();
	/**
	 * @return the name of the permission that is required if this tag is
	 * allowed to be edited -- to be overridden in concrete classes where needed
	 */
	public abstract String getRequiredEditPermission();
	/**
	 * Gets appropriate values for selection of the second correlation list.  Values
	 * are filtered based on the given value from the first correlation list.  Only
	 * valid tag combinations are permitted to be chosen
	 * @param value1 the first correlation value
	 * @return the second correlation list for this tag
	 * entry
	 */
	public abstract List getSecondCorrelationList(short value1)
		throws DataAccessException;
	/**
	 * Gets appropriate values for selection of the second correlation list.  Values
	 * are filtered based on the given value from the first correlation list.  Only
	 * valid tag combinations are permitted to be chosen
	 * @param value1 the first correlation value
	 * @param value2 the second correlation value
	 * @return the second correlation list for this tag
	 * entry
	 */
	public abstract List getThirdCorrelationList(short value1, short value2)
		throws DataAccessException;
	/**
	 * 
	 * @since 1.0
	 */
	public abstract int getUpdateStatus();
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public abstract int hashCode();
	/**
	 * @return true if tag may be deleted
	 */
	public abstract boolean isAbleToBeDeleted();
	/**
	 * @return true if tag can be chosen from a browse list
	 */
	public abstract boolean isBrowsable();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean isChanged();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean isDeleted();
	/**
	 * @return true if tag is an editable header (category 1) field
	 */
	public abstract boolean isEditableHeader();
	/**
	 * @return true if tag is a fixed (control) field
	 */
	public abstract boolean isFixedField();

	/**
	 * true if the tag contains a coded subfield w (Authority reference tags)
	 * 
	 * @since 1.0
	 */
	public abstract boolean isHasSubfieldW();
	/**
	 * @return true if tag is a header (category 1) field
	 */
	public abstract boolean isHeaderField();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean isNew();
	/** return true if tag is a note */
	public abstract boolean isNote();
	/**
	 * @return true if tag is a publisher (260)
	 */
	public abstract boolean isPublisher();
	/** return true if tag is a relationship */
	public abstract boolean isRelationship();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean isRemoved();
	/**
	 * @return true if tag can be directly edited on the worksheet
	 */
	public abstract boolean isWorksheetEditable();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void markChanged();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void markDeleted();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void markNew();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void markUnchanged();
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean onDelete(Session arg0) throws CallbackException;
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void onLoad(Session arg0, Serializable arg1);
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean onSave(Session arg0) throws CallbackException;
	/**
	 * 
	 * @since 1.0
	 */
	public abstract boolean onUpdate(Session arg0) throws CallbackException;
	/**
	 * This method is used to generate the tags content from a model
	 * 
	 * @since 1.0
	 */
	public abstract void parseModelXmlElementContent(Element xmlElement);
	public abstract void setCorrelationValues(CorrelationValues v);
	/**
	 * provides abstract access to item identifier (bib or aut)
	 * 
	 * @since 1.0
	 */
	public abstract void setItemNumber(int itemNumber);
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void setPersistenceState(PersistenceState object);
	/**
	 * 
	 * @since 1.0
	 */
	public abstract void setUpdateStatus(int i);
	/**
	 * This method creates a XML Document as follows
	 * <datafield tag="100" ind1="1" ind2="@">
	 *  <subfield code="a">content</subfield>
	 *  <subfield code="b">content</subfield>
	 * </datafield>
	 * or for a control field
	 * <controlfield tag="001">000000005581</controlfield>
	 * 
	 * @return a Document
	 */
	public abstract Document toXmlDocument();
	/**
	 * This method creates a XML Element as follows
	 * <datafield tag="100" ind1="1" ind2="@">
	 *  <subfield code="a">content</subfield>
	 *  <subfield code="b">content</subfield>
	 * </datafield>
	 * or for a control field
	 * <controlfield tag="001">000000005581</controlfield>
	 * 
	 * @return an Element
	 */
	public abstract Element toXmlElement(Document xmlDocument);
	/**
	 * After a change in correlation value 1, the available choices for values 2 and
	 * 3 are recalculated and the values are reset (to the first available valid choice)
	 * @param s the new value1
	 */
	public abstract void updateFirstCorrelation(short s)
		throws DataAccessException;
	/**
	 * After a change in correlation value 2, the available choices for values 3
	 * are recalculated and the value is reset (to the first available valid choice)
	 * @param s the new value 2
	 */
	public abstract void updateSecondCorrelation(short s)
		throws DataAccessException;

	public abstract void validate(int index) throws ValidationException;

	/**
	 * Called where a 
	 * series of changes result in returning the new key back
	 * to a pre-existing key
	 */
	public abstract void reinstateDeletedTag();
}