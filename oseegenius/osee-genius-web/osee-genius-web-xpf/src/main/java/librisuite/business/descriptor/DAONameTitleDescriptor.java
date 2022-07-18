/*
 * (c) LibriCore
 * 
 * Created on Dec 13, 2005
 * 
 * DAONameTitleDescriptor.java
 */
package librisuite.business.descriptor;

import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.SortFormException;
import librisuite.business.common.View;
import librisuite.hibernate.NME_HDG;
import librisuite.hibernate.NME_TTL_HDG;
import librisuite.hibernate.REF;
import librisuite.hibernate.TTL_HDG;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

/**
 * @author paulm
 * @version $Revision: 1.3 $, $Date: 2006/01/11 13:36:23 $
 * @since 1.0
 */
public class DAONameTitleDescriptor extends DAODescriptor {
	private static final DAONameDescriptor daoName = new DAONameDescriptor();
	private static final DAOTitleDescriptor daoTitle = new DAOTitleDescriptor();

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#getPersistentClass()
	 */
	public Class getPersistentClass() {
		return NME_TTL_HDG.class;
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#supportsAuthorities()
	 */
	public boolean supportsAuthorities() {
		return true;
	}

	protected void loadHeadings(List l, int cataloguingView)
		throws DataAccessException {
		for (int i = 0; i < l.size(); i++) {
			NME_TTL_HDG aHdg = (NME_TTL_HDG) l.get(i);
			loadHeadings(aHdg, cataloguingView);
		}
	}

	protected void loadHeadings(NME_TTL_HDG d, int cataloguingView)
		throws DataAccessException {
		d.setNameHeading(
			(NME_HDG) daoName.load(d.getNameHeadingNumber(), cataloguingView));
		d.setTitleHeading(
			(TTL_HDG) daoTitle.load(
				d.getTitleHeadingNumber(),
				cataloguingView));
	}

	/* (non-Javadoc)
	 * @see librisuite.business.descriptor.DAODescriptor#load(int, int)
	 */
	public Descriptor load(int headingNumber, int cataloguingView)
		throws DataAccessException {
		NME_TTL_HDG d =
			(NME_TTL_HDG) super.load(headingNumber, cataloguingView);
		loadHeadings(d, cataloguingView);
		return d;
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
					+ " where ref.nameTitleHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'MH' "
					+ " and substr(ref.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
		result = result + ((Integer) l.get(0)).intValue();

		l =
			find(
				"select count(*) from TTL_NME_TTL_REF as ref "
					+ " where ref.nameTitleHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'MH' "
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
					+ " where ref.nameTitleHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'MH' "
					+ " and substr(ref.userViewString, ?, 1) = '1'",
				new Object[] {
					new Integer(source.getKey().getHeadingNumber()),
					new Integer(cataloguingView)},
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER }));

		l.addAll(
			find(
				"from TTL_NME_TTL_REF as ref "
					+ " where ref.nameTitleHeadingNumber = ? "
					+ " and ref.sourceHeadingType = 'MH' "
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

			REF result = null;
		if (source.getClass() == target.getClass()) {
			return super.loadReference(
				source,
				target,
				referenceType,
				cataloguingView);
		}
		else if (target.getClass() == NME_HDG.class){
			List l =
				find(
					"from NME_NME_TTL_REF as ref "
						+ " where ref.nameTitleHeadingNumber = ? AND "
						+ " ref.nameHeadingNumber = ? AND "
						+ " ref.sourceHeadingType = 'MH' AND "
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
		else if (target.getClass() == TTL_HDG.class){
			List l =
				find(
					"from TTL_NME_TTL_REF as ref "
						+ " where ref.nameTitleHeadingNumber = ? AND "
						+ " ref.titleHeadingNumber = ? AND "
						+ " ref.sourceHeadingType = 'MH' AND "
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
		else {
			throw new RuntimeException("Unsupported cross-reference type");
		}
	}

	/**
	 * MIKE: get matching for both name and title
	 */
//	public Descriptor getMatchingHeading(Descriptor d) throws DataAccessException, SortFormException {
//		NME_TTL_HDG ntd = (NME_TTL_HDG) d;
//		Descriptor nameDescriptor = ntd.getNameHeading();
//		Descriptor nameHdg = ((DAODescriptor)nameDescriptor.getDAO()).getMatchingHeading(nameDescriptor);
//		if(nameHdg==null) return null;
//		Descriptor titleDescriptor = ntd.getTitleHeading();
//		Descriptor titleHdg = ((DAODescriptor)titleDescriptor.getDAO()).getMatchingHeading(titleDescriptor);
//		if(titleHdg==null) return null;
//		return d;
//	}	
	
	/**
	 * MIKE: get matching for both name and title
	 * If the heading is found, the NME_HDG and the TTL_HDG are 
	 * the same of the descriptor passed as parameter, the the result is
	 * populated with this headings without accessing to the DB
	 */
	public Descriptor getMatchingHeading(Descriptor d) throws DataAccessException, SortFormException {
		NME_TTL_HDG ntd = (NME_TTL_HDG) d;
		List l = loadHeadings(ntd.getNameHeading(), ntd.getTitleHeading(), ntd.getKey().getUserViewString());
		if(l!=null && l.size()>0){
			NME_TTL_HDG hdg = (NME_TTL_HDG)l.get(0);
			hdg.setNameHeading(ntd.getNameHeading());
			hdg.setTitleHeading(ntd.getTitleHeading());
			return hdg;
		}
		return null;
	}

	public boolean isMatchingAnotherHeading(Descriptor d) {
		try {
			NME_TTL_HDG ntd = (NME_TTL_HDG) d;
			// MIKE: verificare che il load non crei un conflitto di chiavi Hibernate (Existent object ecc....)
			List l = countHeadings(ntd, ntd.getNameHeading(), ntd.getTitleHeading(), ntd.getKey().getUserViewString());
			return (l!=null && l.size()>0);
		} catch (DataAccessException e) {
			return false;
		}
	}

	public List getHeadingsByBlankSortform(String operator, String direction,
			String term, String filter, int cataloguingView, int count, int collectionCode)
			throws DataAccessException, InstantiationException, IllegalAccessException {
		// MIKE: standard feauture for Name/Title blank sortform because they haven't sortforms
		return super.getHeadingsBySortform(operator, direction, term, filter,
				cataloguingView, count, collectionCode);
	}

	public List loadHeadings(NME_HDG nameHdg, TTL_HDG titleHdg, String cataloguingViewString) throws DataAccessException {
		int view = View.toIntView(cataloguingViewString);
		
		Session s = currentSession();
	
		List l = null;
		try {
			Query q =
				s.createQuery(
					"select distinct hdg from "
						+ "NME_TTL_HDG as hdg, "
						+ " where hdg.nameHeadingNumber = :nameKey "
						+ " and hdg.titleHeadingNumber = :titleKey "
						+ "  and "
						+ " SUBSTR(hdg.key.userViewString, :view, 1) = '1' ");
			q.setInteger("nameKey", nameHdg.getKey().getHeadingNumber());
			q.setInteger("titleKey", titleHdg.getKey().getHeadingNumber());
			q.setInteger("view", view);
	
			l = q.list();
			l = isolateViewForList(l, view);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return l;
	}
	public List countHeadings(NME_TTL_HDG d, NME_HDG nameHdg, TTL_HDG titleHdg, String cataloguingViewString) throws DataAccessException {
		int view = View.toIntView(cataloguingViewString);
		
		Session s = currentSession();
	
		List l = null;
		try {
			Query q =
				s.createQuery(
					"select distinct hdg from "
						+ "NME_TTL_HDG as hdg, "
						+ " where hdg.nameHeadingNumber = :nameKey "
						+ " and hdg.titleHeadingNumber = :titleKey "
						+ " and c.key.headingNumber <> :currHdgNbr "
						+ " and SUBSTR(hdg.key.userViewString, :view, 1) = '1' ");
			q.setInteger("nameKey", nameHdg.getKey().getHeadingNumber());
			q.setInteger("titleKey", titleHdg.getKey().getHeadingNumber());
			q.setInteger("currHdgNbr", d.getKey().getHeadingNumber());
			q.setInteger("view", view);
	
			l = q.list();
			l = isolateViewForList(l, view);
		} catch (HibernateException e) {
			logAndWrap(e);
		}
		return l;
	}

}
