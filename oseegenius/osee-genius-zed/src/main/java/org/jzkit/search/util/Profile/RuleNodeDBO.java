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

import org.jzkit.search.util.QueryModel.Internal.AttrPlusTermNode;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.*;

@Entity
@Table(name="JZ_RULE_NODE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="NODE_TYPE", discriminatorType=javax.persistence.DiscriminatorType.INTEGER)
public abstract class RuleNodeDBO 
{
  protected Long id;
  protected Set<RuleNodeDBO> child_rules = new HashSet<RuleNodeDBO>();
  protected RuleNodeDBO parent;

  @Id
  @Column(name="ID")
  @GeneratedValue(strategy=GenerationType.AUTO)
  public Long getId() 
  {
    return id;
  }

  public void setId(Long id) 
  {
    this.id = id;
  }

  @OneToMany(mappedBy="parent",cascade={CascadeType.ALL})
  public Set<RuleNodeDBO> getChildren() 
  {
    return child_rules;
  }

  public void setChildren(Set<RuleNodeDBO> child_rules) 
  {
    this.child_rules=child_rules;
  }

  @ManyToOne(cascade=CascadeType.ALL)
  @JoinColumn(name="PARENT_NODE")
  public RuleNodeDBO getParent() 
  {
    return parent;
  }

  public void setParent(RuleNodeDBO parent) 
  {
    this.parent = parent;
  }

  public abstract boolean isValid(String default_namespace, AttrPlusTermNode aptn, QueryVerifyResult qvr);

  @Transient
  public abstract String getDesc();

  @Transient
  public abstract String getNodeType();
}