/*
 * (c) LibriCore
 * 
 * Created on Oct 28, 2005
 * 
 * Tag.java
 */
package librisuite.business.cataloguing.common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import librisuite.business.cataloguing.bibliographic.MarcCorrelationException;
import librisuite.business.cataloguing.bibliographic.VariableField;
import librisuite.business.codetable.DAOCodeTable;
import librisuite.business.common.CorrelationValues;
import librisuite.business.common.DataAccessException;
import librisuite.business.common.LibrisuiteUtils;
import librisuite.business.common.PersistenceState;
import librisuite.business.exception.ValidationException;
import librisuite.hibernate.CorrelationKey;
import librisuite.hibernate.T_SINGLE;
import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.Subfield;

/**
 * @author paulm
 * @version $Revision: 1.6 $, $Date: 2006/11/23 15:01:47 $
 * @since 1.0
 */
public abstract class Tag implements Serializable, Cloneable, TagInterface {

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public Tag() {
		/*
		 * This default implementation can be overridden either in individual tag
		 * constructors or when the tag is added to a CatalogItem
		 */
		//setTagImpl(TagImplFactory.getDefaultImplementation());
	}

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public Tag(int itemNumber) {
		this();
		setItemNumber(itemNumber);
	}

	private TagImpl tagImpl;

	private static Log logger = LogFactory.getLog(Tag.class);

	protected static DAOCodeTable daoCodeTable = new DAOCodeTable();

	protected PersistenceState persistenceState;

	private int itemNumber = -1;

	public String getHeadingType() {
		return tagImpl.getHeadingType(this);
	}

	//MAURA
	/*public Catalog getCatalog() {
		return tagImpl.getCatalog();
	}*/

	/**
		 * indicates whether the proposed change in correlation values would result in a
		 * new persistent key for this tag.  Values of -1 are ignored.
		 *
		 */
	final public boolean correlationChangeAffectsKey(
		short value1,
		short value2,
		short value3) {
		return correlationChangeAffectsKey(
			new CorrelationValues(value1, value2, value3));
	}

	public boolean correlationChangeAffectsKey(CorrelationValues v) {
		return false;
	}

	/**
		 * @return true if tag is a publisher (260)
		 */
	public boolean isPublisher() {
		return false;
	}

	/** return true if tag is a note */
	public boolean isNote() {
		return false;
	}

	/** return true if tag is a relationship */
	public boolean isRelationship() {
		return false;
	}

	/**
		 * @return the name of the permission that is required if this tag is
		 * allowed to be edited -- to be overridden in concrete classes where needed
		 */
	public String getRequiredEditPermission() {
		return "basicCataloguing";
	}

	/**
		 * @param i - the correlation value wanted (1, 2, or 3)
		 * @return the appropriate correlation value for determining MARC coding (-1 if no
		 * value is available or known)
		 */
	public short getCorrelation(int i) {
		return getCorrelationValues().getValue(i);
	}

	/**
		 * sets the given correlation value for this tag
		 * 
		 * @param i - the index to be set (1, 2, or 3)
		 * @param s - the new value
		 * @since 1.0
		 */
	final public void setCorrelation(int i, short s) {
		setCorrelationValues(getCorrelationValues().change(i, s));
	}

	/**
	* @return the MARC tag and indicators for this tag
	*/
	public CorrelationKey getMarcEncoding()
		throws DataAccessException, MarcCorrelationException {
		return tagImpl.getMarcEncoding(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/*
	 * Note that this version of equals is a default implementation equating any two
	 * tags of the same class and itemNumber.  Subclasses should override where
	 * required
	 */
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(this.getClass())))
			return false;
		Tag other = (Tag) obj;
		return (other.getItemNumber() == this.getItemNumber());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getItemNumber();
	}

	/**
		 * After a change in correlation value 1, the available choices for values 2 and
		 * 3 are recalculated and the values are reset (to the first available valid choice)
		 * @param s the new value1
		 */
	public void updateFirstCorrelation(short s) throws DataAccessException {
		setCorrelation(1, s);
		List l = getSecondCorrelationList(s);
		if (l != null) {
			updateSecondCorrelation(((T_SINGLE) l.get(0)).getCode());
		}
	}

	/**
		 * After a change in correlation value 2, the available choices for values 3
		 * are recalculated and the value is reset (to the first available valid choice)
		 * @param s the new value 2
		 */
	public void updateSecondCorrelation(short s) throws DataAccessException {
		setCorrelation(2, s);
		List l = getThirdCorrelationList(getCorrelation(1), getCorrelation(2));
		if (l != null) {
			setCorrelation(3, ((T_SINGLE) l.get(0)).getCode());
		}
	}

	public void generateNewKey() throws DataAccessException {
		// TODO Auto-generated method stub

	}

	public DAOCodeTable getDaoCodeTable() {
		return daoCodeTable;
	}

	/**
		 * 
		 * @since 1.0
		 */
	public PersistenceState getPersistenceState() {
		return persistenceState;
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void setPersistenceState(PersistenceState object) {
		persistenceState = object;
	}

	/**
		 * 
		 * @since 1.0
		 */
	public int getUpdateStatus() {
		if (persistenceState == null) {
			return -1;
		} else {
			return persistenceState.getUpdateStatus();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean isChanged() {
		if (persistenceState == null) {
			return false;
		} else {
			return persistenceState.isChanged();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean isDeleted() {
		if (persistenceState == null) {
			return false;
		} else {
			return persistenceState.isDeleted();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean isNew() {
		if (persistenceState == null) {
			return false;
		} else {
			return persistenceState.isNew();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean isRemoved() {
		if (persistenceState == null) {
			return false;
		} else {
			return persistenceState.isRemoved();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void markChanged() {
		if (persistenceState != null) {
			persistenceState.markChanged();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void markDeleted() {
		if (persistenceState != null) {
			persistenceState.markDeleted();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void markNew() {
		if (persistenceState != null) {
			persistenceState.markNew();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void markUnchanged() {
		if (persistenceState != null) {
			persistenceState.markUnchanged();
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean onDelete(Session arg0) throws CallbackException {
		if (persistenceState != null) {
			return persistenceState.onDelete(arg0);
		} else {
			return true;
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void onLoad(Session arg0, Serializable arg1) {
		if (persistenceState != null) {
			persistenceState.onLoad(arg0, arg1);
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean onSave(Session arg0) throws CallbackException {
		if (persistenceState != null) {
			return persistenceState.onSave(arg0);
		} else {
			return true;
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public boolean onUpdate(Session arg0) throws CallbackException {
		if (persistenceState != null) {
			return persistenceState.onUpdate(arg0);
		} else {
			return true;
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void setUpdateStatus(int i) {
		if (persistenceState != null) {
			persistenceState.setUpdateStatus(i);
		}
	}

	/**
		 * 
		 * @since 1.0
		 */
	public void evict() throws DataAccessException {
		if (persistenceState != null) {
			persistenceState.evict(this);
		}
	}

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
	public Document toXmlDocument() {
		DocumentBuilderFactory documentBuilderFactory =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		Document xmlDocument = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			xmlDocument = documentBuilder.newDocument();
			xmlDocument.appendChild(toXmlElement(xmlDocument));
		} catch (ParserConfigurationException parserConfigurationException) {
			logger.error("", parserConfigurationException);
			//throw new XmlParserConfigurationException(parserConfigurationException);
		}
		return xmlDocument;
	}

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
	public Element toXmlElement(Document xmlDocument) {
		CorrelationKey marcEncoding = null;
		try {
			marcEncoding = getMarcEncoding();
		} catch (Exception exception) {
			throw new RuntimeException("Invalid tag found in Tag.toXmlElement");
		}

		String marcTag = marcEncoding.getMarcTag();
		String marcFirstIndicator =
			new String("" + marcEncoding.getMarcFirstIndicator());
		String marcSecondIndicator =
			new String("" + marcEncoding.getMarcSecondIndicator());

		Element field = null;
		if (isFixedField()) {
			field = xmlDocument.createElement("controlfield");
		} else {
			field = xmlDocument.createElement("datafield");
		}
		field.setAttribute("tag", marcTag);
		if (!isFixedField()) {
			field.setAttribute("ind1", marcFirstIndicator);
			field.setAttribute("ind2", marcSecondIndicator);
			for (Iterator subfieldIterator =
				((VariableField) this)
					.getStringText()
					.getSubfieldList()
					.iterator();
				subfieldIterator.hasNext();
				) {
				Subfield subfield = (Subfield) subfieldIterator.next();
				field.appendChild(subfield.toXmlElement(xmlDocument));
			}
		}
		return field;
	}

	/**
	 * This method is used to generated the model xml.
	 * 
	 * @since 1.0
	 */
	public Element generateModelXmlElement(Document xmlDocument) {
		Element field = null;
		if (xmlDocument != null) {
			field = xmlDocument.createElement("field");
			try {
				field.setAttribute("tag", this.getMarcEncoding().getMarcTag());
				field.setAttribute(
					"firstIndicator",
					"" + this.getMarcEncoding().getMarcFirstIndicator());
				field.setAttribute(
					"secondIndicator",
					"" + this.getMarcEncoding().getMarcSecondIndicator());
				field.setAttribute("categoryCode", "" + this.getCategory());
				field.setAttribute(
					"firstCorrelationValue",
					"" + this.getCorrelation(1));
				field.setAttribute(
					"secondCorrelationValue",
					"" + this.getCorrelation(2));
				field.setAttribute(
					"thirdCorrelationValue",
					"" + this.getCorrelation(3));
			} catch (MarcCorrelationException marcCorrelationException) {
			} catch (DataAccessException dataAccessException) {
			}
			field.appendChild(generateModelXmlElementContent(xmlDocument));
		}
		return field;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return LibrisuiteUtils.deepCopy(this);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public TagImpl getTagImpl() {
		return tagImpl;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setTagImpl(TagImpl impl) {
		tagImpl = impl;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public int getItemNumber() {
		return itemNumber;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setItemNumber(int itemNumber) {
		this.itemNumber = itemNumber;
	}

	/**
	 * 
	 * @since 1.0
	 */
	/*MAURA
	public Validation getValidation()
		throws MarcCorrelationException, DataAccessException {
		return tagImpl.getValidation(this);
	}*/

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getCategory()
	 */
	abstract public short getCategory();

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#isHasSubfieldW()
	 */
	public boolean isHasSubfieldW() {
		return false; //default implementation
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getDisplayCategory()
	 */
	public short getDisplayCategory() {
		return getCategory();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#getDisplaysHeadingType()
	 */
	public boolean getDisplaysHeadingType() {
		return false;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public HibernateUtil getDAO() {
		return persistenceState.getDAO();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.common.TagInterface#validate()
	 */
	public void validate(int index) throws ValidationException {
		// default implementation does nothing
	}
	
	/**
	 * Called where a 
	 * series of changes result in returning the new key back
	 * to a pre-existing key
	 */
	public void reinstateDeletedTag() {
		markUnchanged();
		markChanged();
	}

}
