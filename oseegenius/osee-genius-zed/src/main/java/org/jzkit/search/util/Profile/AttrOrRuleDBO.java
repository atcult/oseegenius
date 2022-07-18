/*
 * This program is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the following 
 * permission added to Section 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package org.jzkit.search.util.Profile;

import org.jzkit.search.util.QueryModel.Internal.AttrPlusTermNode;
import java.util.*;
import javax.persistence.*;

@javax.persistence.Entity
@DiscriminatorValue("2")
public class AttrOrRuleDBO extends BooleanRuleNodeDBO
{
	@Override
	public boolean isValid(final String defaultNamespace, final AttrPlusTermNode aptn, final QueryVerifyResult qvr)
	{
		boolean result = false;
		String firstFailingAttribute = null;
		for (Iterator<RuleNodeDBO> iterator = child_rules.iterator(); ((iterator .hasNext()) && (!result));)
		{
			RuleNodeDBO next = (RuleNodeDBO) iterator.next();
			result = result || next.isValid(defaultNamespace, aptn, qvr);
			if (firstFailingAttribute == null)
			{
				firstFailingAttribute = qvr.getFailingAttr();
			}
		}

		// Since this was an OR, we might should use the first failing attr.
		// Perhaps we should use them all?
		// If one of the children matched, don't record any failing attr.
		if (!result)
		{
			qvr.setFailingAttr(firstFailingAttribute);
			// Log maybe : "The query is not conformant because the profile
			// demands that at least one of the following
			// are true: <List child nodes>.
		} else
		{
			qvr.setFailingAttr(null);
		}

		// Bacause this is an OR node, we only need one of the children to be
		// true.
		qvr.setIsValid(result);

		return result;
	}

	@Override
	@Transient
	public String getDesc()
	{
		return "Query term must match at least one of";
	}

	@Override
	@Transient
	public String getNodeType()
	{
		return "";
	}

	@Override
	public String toString()
	{
		return "Match at least one of";
	}
}