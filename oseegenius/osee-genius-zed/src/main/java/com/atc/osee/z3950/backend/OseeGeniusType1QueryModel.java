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
package com.atc.osee.z3950.backend;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jzkit.a2j.codec.util.OIDRegister;
import org.jzkit.a2j.codec.util.OIDRegisterEntry;
import org.jzkit.search.util.QueryModel.InvalidQueryException;
import org.jzkit.search.util.QueryModel.Internal.AttrValue;
import org.jzkit.search.util.QueryModel.Internal.ComplexNode;
import org.jzkit.search.util.QueryModel.Internal.InternalModelNamespaceNode;
import org.jzkit.search.util.QueryModel.Internal.InternalModelRootNode;
import org.jzkit.search.util.QueryModel.Internal.QueryNode;
import org.jzkit.z3950.QueryModel.Type1QueryModel;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.AttributesPlusTerm_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.Operand_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.RPNQuery_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.RPNStructure_type;
import org.jzkit.z3950.gen.v3.Z39_50_APDU_1995.rpnRpnOp_inline2_type;
import org.springframework.context.ApplicationContext;

public class OseeGeniusType1QueryModel extends Type1QueryModel 
{
	private RPNQuery_type query;
	private static Log log = LogFactory.getLog(OseeGeniusType1QueryModel.class);
	private final Map<String, String> defaultValues;
	
	public OseeGeniusType1QueryModel(final RPNQuery_type query, final Map<String, String> defaultValues) 
	{
		super(query);
		this.query = query;
		this.defaultValues = defaultValues;
	}

	public InternalModelRootNode toInternalQueryModel(ApplicationContext ctx) throws InvalidQueryException 
	{
		final OIDRegister reg = (OIDRegister) ctx.getBean("OIDRegister");
		final Properties attr_type_config = (Properties) ctx.getBean("RPNToInternalRules");
		return traverse(query, reg, attr_type_config);
	}

	private InternalModelRootNode traverse(final RPNQuery_type q, final OIDRegister reg, final Properties attr_type_config) 
	{
		if (q.attributeSet != null) {
			String attr_set = null;
			final OIDRegisterEntry re = reg.lookupByOID(q.attributeSet);
			if (null != re)
			{
				attr_set = re.getName();
			}
			
			return new InternalModelRootNode(
					new InternalModelNamespaceNode(
							attr_set, traverse(q.rpn, reg, attr_type_config, attr_set)));
		} else 
		{
			return new InternalModelRootNode(traverse(q.rpn, reg,
					attr_type_config, null));
		}
	}

	@SuppressWarnings("unchecked")
	private QueryNode traverse(final RPNStructure_type rpn, final OIDRegister reg, final Properties attr_type_config, final String attrset) 
	{
		switch (rpn.which) 
		{
			case RPNStructure_type.op_CID:
				// This node is an operand... Process it
				Operand_type ot = (Operand_type) (rpn.o);
				// What kind of operand (Let's hope it's AttrPlusTerm for now)
				switch (ot.which) 
				{
					case Operand_type.attrterm_CID:
						// Get hold of an easy handle to the Z3950 RPN node
						AttributesPlusTerm_type apt = (AttributesPlusTerm_type) (ot.o);
						
						// Injects default values for attributes
						QueryNode node = convertAPT(apt, reg, attr_type_config, attrset);

						Map<String, AttrValue> attributes = node.getAttrs();
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "AccessPoint");
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "Relation");
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "Position");
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "Structure");
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "Truncation");
						checkAndEventuallyInjectDefaultValue(attributes, attrset, "Completeness");
						return node;
					case Operand_type.resultset_CID:
						break;
					case Operand_type.resultattr_CID:
						break;
				}
			break;
		case RPNStructure_type.rpnrpnop_CID:
			ComplexNode complexNode = new ComplexNode(traverse(
					((rpnRpnOp_inline2_type) (rpn.o)).rpn1, reg,
					attr_type_config, attrset), traverse(
					((rpnRpnOp_inline2_type) (rpn.o)).rpn2, reg,
					attr_type_config, attrset),
					((rpnRpnOp_inline2_type) (rpn.o)).op.which + 1);
			return complexNode;
		}
		return null;
	}
	
	private void checkAndEventuallyInjectDefaultValue(Map<String, AttrValue> attributes, String attrset,  String attributeName)
	{
		AttrValue value = attributes.get(attributeName);
		if (value == null)
		{
			final String defaultValue = defaultValues.get(attributeName);
			if (defaultValue == null || defaultValue.trim().length() == 0)
			{
				log.debug("Unable to find a default value for attribute \"" + attributeName + "\"");
				return;
			}
			
			log.debug("Attribute kind \"" + attributeName + "\" not valorized. Replacing it with a default value of " + defaultValue);
			value = new AttrValue(attrset,defaultValue);
			attributes.put(attributeName, value);
		}
	}
}
