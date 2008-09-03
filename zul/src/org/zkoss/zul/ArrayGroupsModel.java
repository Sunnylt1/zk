/*ArrayGroupsModel.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 3, 2008 9:50:12 AM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zul.event.GroupsDataEvent;

/**
 * A array implementation of {@link GroupsModel}.
 * This implementation supports regroup array to groups depends on {@link Comparator} and {@link GroupComparator}.
 * 
 * @author Dennis.Chen
 * @since 3.5.0
 * @see GroupsModel
 */
public class ArrayGroupsModel extends AbstractGroupsModel implements GroupsModelExt{
	
	/**
	 * member field to store native array data
	 */
	protected Object[] _nativedata;
	
	/**
	 * member field to store Comparator for initial grouping.
	 */
	protected Comparator _comparator;
	
	/**
	 * member field to store group data
	 */
	protected Object[][] _data;
	
	/**
	 * member field to store group head data
	 */
	protected Object[] _heads;
	
	/**
	 * member field to store group foot data
	 */
	protected Object[] _foots;
	
	
	/**
	 * Constructor
	 * @param data an array data to be grouping.
	 * @param cmpr a comparator implementation help group the data. you could implements {@link GroupComparator} to do more grouping control.<br/>
	 * At 1st phase, it calls {@link Comparator#compare(Object, Object)} or {@link GroupComparator#compareGroup(Object, Object)} to sort the data.<br/>
	 * At 2nd phase, it calls {@link Comparator#compare(Object, Object)} or {@link GroupComparator#compareGroup(Object, Object)} to decide which data belong to which group. 
	 * In this phase it also invoke {@link #createGroupHead(Object[], int)} and {@link #createGroupFoot(Object[], int)} to create head of foot Object of each group.<br/>
	 * At 3rd phase, it calls {@link Comparator#compare(Object, Object)} to sort data in each group.<br/>
	 */
	public ArrayGroupsModel(Object[] data,Comparator cmpr){
		this(data,cmpr,0);
	}
	/**
	 * 
	 * @param data an array data to be grouping.
	 * @param cmpr a comparator implementation help group the data. you could implements {@link GroupComparator} to do more grouping control.<br/>
	 * At 1st phase, it calls {@link Comparator#compare(Object, Object)} or {@link GroupComparator#compareGroup(Object, Object)} to sort the data.<br/>
	 * At 2nd phase, it calls {@link Comparator#compare(Object, Object)} or {@link GroupComparator#compareGroup(Object, Object)} to decide which data belong to which group. 
	 * In this phase it also invoke {@link #createGroupHead(Object[], int)} and {@link #createGroupFoot(Object[], int)} to create head of foot Object of each group.<br/>
	 * At 3rd phase, it calls {@link Comparator#compare(Object, Object)} to sort data in each group.<br/>
	 * @param col column index associate with cmpr.
	 */
	public ArrayGroupsModel(Object[] data,Comparator cmpr, int col){
		if (data == null || cmpr == null)
			throw new IllegalArgumentException("null parameter");
		_nativedata = Arrays.copyOf(data, data.length);
		_comparator = cmpr;
		group(_comparator,true,col);
	}

	public Object getChild(int groupIndex, int index) {
		return _data[groupIndex][index];
	}


	public int getChildCount(int groupIndex) {
		return _data[groupIndex].length;
	}


	public Object getGroup(int groupIndex) {
		return  _heads==null?_data[groupIndex]:_heads[groupIndex];
	}


	public int getGroupCount() {
		return _data.length;
	}

	public Object getGroupfoot(int groupIndex) {
		return _foots == null ? null:_foots[groupIndex];
	}

	public boolean hasGroupfoot(int groupIndex) {
		return _foots == null ? false:_foots[groupIndex]!=null;
	}

	public void sort(Comparator cmpr, boolean ascending, int col) {
		sortGroup(cmpr,ascending,col);

		fireEvent(GroupsDataEvent.GROUPS_CHANGED,-1,-1,-1);
	}

	public void group(final Comparator cmpr, boolean ascending, int col) {
		Comparator cmprx = null;
		if(cmpr instanceof GroupComparator){
			cmprx = new Comparator(){
				public int compare(Object o1, Object o2) {
					return ((GroupComparator)cmpr).compareGroup(o1, o2);
				}
			};
		}else{
			cmprx = cmpr;
		}
		
		sortNativeData(cmprx,ascending,col);//use comparator from constructor to sort native data
		organizeGroup(cmprx,col);
		sortGroup(cmpr,ascending,col);//sort by original comparator
		
		fireEvent(GroupsDataEvent.GROUPS_CHANGED,-1,-1,-1);
	}

	/**
	 * sort data in each group, the group order will not change. invoke this method doesn't fire event.
	 */
	protected void sortGroup(Comparator cmpr,boolean ascending, int col) {
		for(int i=0;i<_data.length;i++){
			sortGroupData(_heads[i],_data[i],cmpr,ascending,col);
		}
	}
	
	/**
	 * sort data of a group. invoke this method doesn't fire event.
	 */
	protected void sortGroupData(Object group,Object[] groupdata,Comparator cmpr,boolean ascending, int col){
		Arrays.sort(groupdata,cmpr);
	}

	/**
	 * organize the group, the _nativedata must sorted already.
	 */
	protected void organizeGroup(Comparator cmpr, int col) {
		List group = new ArrayList();
		List gdata = null;
		Object last = null;
		Object curr = null;
		
		//regroup native
		for(int i=0;i<_nativedata.length;i++){
			curr = _nativedata[i];
			boolean hitn = false;
			boolean hita = false;
			if(last==null || cmpr.compare(last,curr)!=0){
				hitn = true;
				gdata = new ArrayList();
				group.add(gdata);
			}
			gdata.add(curr);
			last = _nativedata[i];
		}
		
		//prepare data,head & foot
		List[] gd = new List[group.size()];
		group.toArray(gd);
		
		_data = new Object[gd.length][];
		_foots = new Object[_data.length];
		_heads = new Object[_data.length];
		
		for(int i=0;i<gd.length;i++){
			gdata = (List)gd[i];
			_data[i] = new Object[gdata.size()];
			gdata.toArray(_data[i]);
			_heads[i] = createGroupHead(_data[i],i,col);
			_foots[i] = createGroupFoot(_data[i],i,col);
		}
	}


	/**
	 * create group head Object, default implementation return first element of groupdata.
	 * you can override this method to return your Object.
	 * @param groupdata data the already in a group.
	 * @param index group index
	 * @param col column to group
	 */
	protected Object createGroupHead(Object[] groupdata,int index,int col) {
		return groupdata[0];
	}

	/**
	 * create group foot Object, default implementation return null, which means no foot .
	 * you can override this method to return your Object.
	 * @param groupdata data the already in a group.
	 * @param index group index
	 * @param col column to group
	 */
	protected Object createGroupFoot(Object[] groupdata,int index,int col) {
		return null;
	}

	/**
	 * sort the native data.
	 */
	protected void sortNativeData(Comparator cmpr, boolean ascending, int colIndex) {
		Arrays.sort(_nativedata,cmpr);
	}



	
	

}
