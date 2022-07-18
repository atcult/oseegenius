/*
 * Created on 2013-06-01
 *
 */
package com.atc.osee.web.sbn;

public class Index {
    private String name;
    private String use;
    
    public Index(String name, String use) {
        setName(name);
        setUse(use);
    }
    
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Index) {
            return ((Index)arg0).name.equals(this.name);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "Index('" + getName() + "')";
    }

    public String getName() {
        return name;
    }
    private void setName(String name) {
        this.name = name;
    }
    public String getUse() {
        return use;
    }
    private void setUse(String use) {
        this.use = use;
    }
    
    
}
