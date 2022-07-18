/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Affero General Public License version 3 as published by the Free 
 * Software Foundation with the addition of the following permission added to Section 
 * 15 as permitted in Section 7(a). 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. 
 * You should have received a copy of the GNU Affero General Public License along with this program;
 */
package org.jzkit.search.util.Profile;

import javax.persistence.DiscriminatorValue;

@javax.persistence.Entity
@DiscriminatorValue("1")
public abstract class BooleanRuleNodeDBO extends RuleNodeDBO
{
	public BooleanRuleNodeDBO()
	{
		// Nothing to be done here...
	}

	public BooleanRuleNodeDBO(RuleNodeDBO[] children)
	{
		for (int i = 0; i < children.length; i++)
		{
			child_rules.add(children[i]);
			children[i].setParent(this);
		}
	}

	public void add(RuleNodeDBO rule)
	{
		child_rules.add(rule);
		rule.setParent(this);
	}
}
