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
@DiscriminatorValue("3")
public class AttrAndRuleDBO extends BooleanRuleNodeDBO
{
	@Override
	public boolean isValid(String default_namespace, AttrPlusTermNode aptn, QueryVerifyResult qvr)
	{
		boolean result = true;
		for (Iterator<RuleNodeDBO> iterator = child_rules.iterator(); ((iterator.hasNext()) && (result));)
		{
			RuleNodeDBO next = (RuleNodeDBO) iterator.next();
			result = result && next.isValid(default_namespace, aptn, qvr);
		}
		return result;
	}

	@Override
	@Transient
	public String getDesc()
	{
		return "Query term must match all of";
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
		return "Match All";
	}
}