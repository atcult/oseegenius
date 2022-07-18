/*
 * (c) LibriCore
 * 
 * Created on Jun 21, 2004
 * 
 * NameDescriptor.java
 */
package librisuite.business.descriptor;

import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.Persistence;
import librisuite.business.common.ReferentialIntegrityException;
import librisuite.business.common.View;
import librisuite.hibernate.NME_HDG;
import librisuite.hibernate.REF;
import librisuite.hibernate.TTL_HDG;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;

/**
 * Manages headings in the NME_HDG table
 * @author paulm
 */
public class DAONameDescriptor extends DAODescriptor {
	static protected Class persistentClass = NME_HDG.class;

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.business.Descriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return DAONameDescriptor.persistentClass;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#supportsAuthorities()
	 */
	public boolean supportsAuthorities() {
		return true;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#getXrefCount(librisuite.business.descriptor.Descriptor, int)
	 */
	public int getXrefCount(Descriptor source, int cataloguingView)
		throws DataAccessException {

		int result = super.getXrefCount(source, cataloguingView);
		List l =
			find(
				"select count(*) from NME_NME_TTL_REF as ref "
					+ " where ref.nameHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'NH' "
					+ " and substr(ref.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
		result = result + ((Integer) l.get(0)).intValue();
		return result;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#getCrossReferences(librisuite.business.descriptor.Descriptor, int)
	 */
	public List getCrossReferences(Descriptor source, int cataloguingView)
		throws DataAccessException {

		List l = super.getCrossReferences(source, cataloguingView);

		l.addAll(
			find(
				"from NME_NME_TTL_REF as ref "
					+ " where ref.nameHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'NH' "
					+ " and substr(ref.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER }));
		return l;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#loadReference(librisuite.business.descriptor.Descriptor, librisuite.business.descriptor.Descriptor, short, int)
	 */
	public REF loadReference(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView)
		throws DataAccessException {

		if (source.getClass() == target.getClass()) {
			return super.loadReference(
				source,
				target,
				referenceType,
				cataloguingView);
		} else {
			REF result = null;
			List l =
				find(
					"from NME_NME_TTL_REF as ref "
						+ " where ref.nameHeadingNumber = ? AND "
						+ " ref.nameTitleHeadingNumber = ? AND "
						+ " ref.sourceHeadingType = 'NH' AND "
						+ " substr(ref.userViewString, ?, 1) = '1' AND "
						+ " ref.type = ?",
					new Object[] {
						new Integer(source.getKey().getHeadingNumber()),
						new Integer(target.getKey().getHeadingNumber()),
						new Integer(cataloguingView),
						new Short(referenceType)},
					new Type[] {
						Hibernate.INTEGER,
						Hibernate.INTEGER,
						Hibernate.INTEGER,
						Hibernate.SHORT });
			if (l.size() == 1) {
				result = (REF) l.get(0);
				result = (REF) isolateView(result, cataloguingView);
			}
			return result;
		}
	}

	/* (non-Javadoc)
	 * @see com.libricore.librisuite.common.HibernateUtil#delete(librisuite.business.common.Persistence)
	 */
	public void delete(Persistence p)
		throws ReferentialIntegrityException, DataAccessException {
		/*
		 * first check for name/title usage
		 */
		NME_HDG n = (NME_HDG) p;
		List l =
			find(
				"select count(*) from NME_TTL_HDG as t where "
					+ " t.nameHeadingNumber = ? and "
					+ " substr(t.key.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(n.getKey().getHeadingNumber()),
					new Integer(View.toIntView(n.getUserViewString()))},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
		if (((Integer) l.get(0)).intValue() > 0) {
			throw new ReferentialIntegrityException("NME_TTL_HDG", "NME_HDG");
		}
		super.delete(p);
	}
	
	public boolean isMatchingAnotherHeading(Descriptor desc) {
		NME_HDG d = (NME_HDG) desc;
		try {
			List l = currentSession().find(
					"select count(*) from "
							+ getPersistentClass().getName()
							+ " as c "
							+ " where c.stringText= ? "
							+ " and c.indexingLanguage = ? "
							+ " and c.accessPointLanguage = ?" 
							+ " and c.typeCode =? "
							+ " and c.subTypeCode =? "
							+ " and c.key.userViewString = ?"
							+ " and c.key.headingNumber <> ?",
					new Object[] { 
							d.getStringText(),
							new Integer(d.getIndexingLanguage()),
							new Integer(d.getAccessPointLanguage()),
							new Integer(d.getTypeCode()),
							new Integer(d.getSubTypeCode()),
							d.getUserViewString(),
							new Integer(d.getKey().getHeadingNumber()) },
					new Type[] { Hibernate.STRING,
							Hibernate.INTEGER, 
							Hibernate.INTEGER,
							Hibernate.INTEGER, 
							Hibernate.INTEGER,
							Hibernate.STRING,
							Hibernate.INTEGER});
			return ((Integer) l.get(0)).intValue() > 0;
		} catch (Exception e) {
			return false;
		}
	}



}
