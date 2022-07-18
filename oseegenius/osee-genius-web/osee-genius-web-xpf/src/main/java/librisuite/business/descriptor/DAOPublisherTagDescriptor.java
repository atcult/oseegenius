package librisuite.business.descriptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import librisuite.business.common.DataAccessException;
import librisuite.business.common.SortFormException;
import librisuite.business.common.View;
import librisuite.hibernate.PUBL_HDG;
import librisuite.hibernate.PUBL_TAG;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.type.Type;

import com.libricore.librisuite.common.TransactionalHibernateOperation;

public class DAOPublisherTagDescriptor extends DAODescriptor {

	//@Override
	public Class getPersistentClass() {
		return PublisherTagDescriptor.class;
	}



	//@Override
	public Descriptor load(int headingNumber, int cataloguingView,
			Class persistentClass) throws DataAccessException {
		PublisherTagDescriptor d = new PublisherTagDescriptor();
		d.setHeadingNumber(headingNumber);
		d.setUserViewString(View.makeSingleViewString(cataloguingView));
		List/*<PUBL_TAG>*/ multiView = find("from PUBL_TAG as t "
				+ " where t.publisherTagNumber = ? "
				+ " and substr(t.userViewString, ?, 1) = '1' "
				+ " order by t.sequenceNumber ", new Object[] {
				new Integer(headingNumber), new Integer(cataloguingView) },
				new Type[] { Hibernate.INTEGER, Hibernate.INTEGER });
		List/*<PUBL_TAG>*/ singleView = isolateViewForList(multiView,
				cataloguingView);
		Iterator ite = singleView.iterator();
		while(ite.hasNext()){
		PUBL_TAG t =(PUBL_TAG)ite.next();
			PUBL_HDG ph = null;
			if (t.getPublisherHeadingNumber() != null) {
				ph = (PUBL_HDG) t.getDescriptorDAO().load(
						(t.getPublisherHeadingNumber()).intValue(), cataloguingView);
			}
			t.setDescriptor(ph);
			d.getPublisherTagUnits().add(t);
		}
		return d;
	}
	
	

}
