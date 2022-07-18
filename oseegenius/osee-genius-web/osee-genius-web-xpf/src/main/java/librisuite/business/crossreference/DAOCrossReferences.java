/*
 * (c) LibriCore
 * 
 * Created on Jun 21, 2004
 * 
 * CrossReferences.java
 */
package librisuite.business.crossreference;

import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.Persistence;
import librisuite.business.common.View;
import librisuite.business.descriptor.DAODescriptor;
import librisuite.business.descriptor.Descriptor;
import librisuite.hibernate.REF;
import librisuite.hibernate.ReferenceType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.libricore.librisuite.common.HibernateUtil;
import com.libricore.librisuite.common.TransactionalHibernateOperation;

/**
 * Abstract class representing the Cross References for a single heading
 * 
 * @author paulm
 */
public class DAOCrossReferences extends HibernateUtil {
	protected Log logger = LogFactory.getLog(DAOCrossReferences.class);


	public void save(Persistence p) throws DataAccessException {
		final REF ref = (REF)p;
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				// save this row
				s.save(ref);
				// calculate the reciprocal
				REF reciprocal = ref.createReciprocal();
				// save the reciprocal
				s.save(reciprocal);
			}
		}
		.execute();
	}

	public void delete(Persistence p) throws DataAccessException {
		final REF ref = (REF)p;
		final REF reciprocal =
			loadReciprocal(ref, View.toIntView(ref.getUserViewString()));
		new TransactionalHibernateOperation() {
			public void doInHibernateTransaction(Session s)
				throws HibernateException {
				s.delete(ref);
				s.delete(reciprocal);
			}
		}
		.execute();
	}

	public REF loadReciprocal(REF ref, int cataloguingView)
		throws DataAccessException {

		short reciprocalType =
			ReferenceType.getReciprocal(ref.getType());

		REF result = null;

		List l =
			find(
				"from "
					+ ref.getClass().getName()
					+ " as ref "
					+ " where ref.key.target = ? AND "
					+ " ref.key.source = ? AND "
					+ " substr(ref.key.userViewString, ?, 1) = '1' AND "
					+ " ref.key.type = ?",
				new Object[] {
					new Integer(ref.getSource()),
					new Integer(ref.getTarget()),
					new Integer(cataloguingView),
					new Short(reciprocalType)},
				new Type[] {
					Hibernate.INTEGER,
					Hibernate.INTEGER,
					Hibernate.INTEGER,
					Hibernate.SHORT });
		if (l.size() == 1) {
			result = (REF) l.get(0);
		}

		return result;
	}

	public REF load(
		Descriptor source,
		Descriptor target,
		short referenceType,
		int cataloguingView)
		throws DataAccessException {

		return ((DAODescriptor) source.getDAO()).loadReference(
			source,
			target,
			referenceType,
			cataloguingView);
	}
}
