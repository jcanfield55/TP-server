/**
 * 
 */
package com.apprika.otp.dataobject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nirmal
 * 
 */
public class Parameter implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962760209521636374L;
	public static final int LESS = 0;
	public static final int GREATER = 1;
	public static final int EQUAL = 2;

	/**
	 * Pass Object[] in parameter
	 */
	public static final int IN = 3;
	public static final int NOT_EQUAL = 4;
	public static final int LIMIT = 5;

	/**
	 * No parameter <b>name</b> required
	 */
	public static final int ORDER_DESK = 6;

	/**
	 * No parameter <b>name</b> required
	 */
	public static final int ORDER_ASC = 7;
	public static final int PAGE_SIZE = 8;
	public static final int PAGINATION = 9;
	public static final int PROJECTION = 10;

	private String name;
	private Object value;
	private int comparator;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public int getComparator() {
		return comparator;
	}

	public void setComparator(int comparator) {
		this.comparator = comparator;
	}

	public Parameter(String name, Object value, int comparator) {
		super();
		this.name = name;
		this.value = value;
		this.comparator = comparator;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Parameter(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}

	public Parameter() {
		super();
	}

	/**
	 * Gets the single param list.
	 *
	 * @param key the key
	 * @param obj the obj
	 * @param comparator the comparator
	 * @return the single param list
	 */
	@SuppressWarnings("rawtypes")
	public static List getSingleParamList(String key, Object obj, int comparator) {
		List<Parameter> lst = new ArrayList<Parameter>();
		lst.add(new Parameter(key, obj, comparator));
		return lst;
	}

	@Override
	public String toString() {
		return "Parameter [name=" + name + ", value=" + value + ", comparator="
				+ comparator + "]";
	}


}
