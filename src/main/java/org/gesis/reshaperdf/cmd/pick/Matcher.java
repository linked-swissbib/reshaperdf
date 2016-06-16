/*
 * Copyright (C) 2016 GESIS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see 
 * http://www.gnu.org/licenses/ .
 */
package org.gesis.reshaperdf.cmd.pick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.gesis.reshaperdf.utils.ResourceComparator;
import org.gesis.reshaperdf.utils.StatemensComparatorUtils;
import org.gesis.reshaperdf.utils.URIComparator;
import org.gesis.reshaperdf.utils.ValueComparator;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author bensmafx
 */
public class Matcher {

    private ArrayList<Resource> sList = null;
    private ArrayList<URI> pList = null;
    private ArrayList<Value> oList = null;
    private int sListIdx = 0;
    
    private URIComparator uriComparator = null;
    private ValueComparator valueComparator = null;

    public Matcher(ArrayList<Resource> sList, ArrayList<URI> pList,
            ArrayList<Value> oList) {
        this.sList = sList;
        this.pList = pList;
        this.oList = oList;
        
        Collections.sort(this.sList, new ResourceComparator());  
        //remove duplicates 
        for (int i = sList.size() - 1; i > 0; i--) {
            if (sList.get(i).equals(sList.get(i - 1))) {
                System.out.println("Removing duplicate " + sList.get(i));
                sList.remove(i);
            }
        }  
        
        Collections.sort(this.pList, new URIComparator());
        //remove duplicates
        for (int i = pList.size() - 1; i > 0; i--) {
            if (pList.get(i).equals(pList.get(i - 1))) {
                System.out.println("Removing duplicate " + pList.get(i));
                pList.remove(i);
            }
        }
        
        Collections.sort(this.pList, new ValueComparator());
        //remove duplicates
        for (int i = oList.size() - 1; i > 0; i--) {
            if (oList.get(i).equals(oList.get(i - 1))) {
                System.out.println("Removing duplicate " + oList.get(i));
                oList.remove(i);
            }
        }
        
        
        sListIdx = 0;
        
        uriComparator=new URIComparator();
        valueComparator=new ValueComparator();
    }

    public void reset() {
        sListIdx = 0;
    }

    public void incIndex(){
        sListIdx++;
    }
    
    public int getIndex(){
        return sListIdx;
    }
    
    public int compareTo(Statement stmt) {

        int result0=Integer.MIN_VALUE;
        int result1=Integer.MIN_VALUE;
        int result2=Integer.MIN_VALUE;
        
        //compare subject
        if (sList.isEmpty()) {
            result0=0;
        }
        else if(sListIdx < sList.size()){
            result0=StatemensComparatorUtils.compare2Resources(stmt.getSubject(), sList.get(sListIdx));
        }
        if(result0 != 0){
            return result0;
        }
        
        //compare predicate
        if(pList.isEmpty()){
            result1=0;
        }
        else{
            int idx = Collections.binarySearch(pList, stmt.getPredicate(), uriComparator);
            if(idx>=0){
                result1=0;
            }
            else{
                return -1;
            }
        }
        
        //compare object
        if(oList.isEmpty()){
            return 0;
        }
        else{
            int idx = Collections.binarySearch(oList, stmt.getObject(), valueComparator);
            if(idx>=0){
                return 0;
            }
            else{
                return -1;
            }
        }
        
    }



}
