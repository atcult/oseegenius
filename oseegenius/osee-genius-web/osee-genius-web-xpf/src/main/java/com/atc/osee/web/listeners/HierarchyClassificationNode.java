/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.listeners;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A hierarchy classification composite node.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class HierarchyClassificationNode implements Serializable
{
	private static final long serialVersionUID = 4261310177318217951L;

	final static HierarchyNodeComparator COMPARATOR = new HierarchyNodeComparator();
	
	private final String messageKey;
	private List<HierarchyClassificationNode> children = new ArrayList<HierarchyClassificationNode>();
	private HierarchyClassificationNode parent;
	
	/**
	 * Builds a new node with the given (name) key.
	 * Note that it is supposed to be not a literal but instead a key that 
	 * will be used for i18n.
	 * 
	 * @param key the node name (the key).
	 */
	public HierarchyClassificationNode(final String key)
	{
		this.messageKey = key;
	}
	
	/**
	 * Adds a new child to this node.
	 * 
	 * @param child the new child.
	 */
	public void addChild(final HierarchyClassificationNode child)
	{
		if (!children.contains(child))
		{
			children.add(child);
			child.parent = this;
			Collections.sort(children, COMPARATOR);
		}
	}
	
	/**
	 * Returns the (name) key of this node.
	 * 
	 * @return the (name) key of this node.
	 */
	public String getMessageKey()
	{
		return messageKey;
	}
	
	@Override
	public String toString()
	{
		return messageKey;
	}

	/**
	 * Returns the children of this node.
	 * 
	 * @return the children of this node.
	 */
	public List<HierarchyClassificationNode> getChildren()
	{
		return children;
	}

	/**
	 * Returns the parent of this node.
	 * 
	 * @return the parent of this node.
	 */
	public HierarchyClassificationNode getParent()
	{
		return parent;
	}
	
	/**
	 * Returns true if this node doesn't have children.
	 * 
	 * @return true if this node doesn't have children.
	 */
	public boolean isLeaf()
	{
		return children == null || children.isEmpty();
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		return obj instanceof HierarchyClassificationNode && messageKey.equals(((HierarchyClassificationNode)obj).messageKey);
	}
	
	@Override
	public int hashCode() 
	{
		return messageKey.hashCode();
	}
}