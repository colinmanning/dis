/**
 * 
 */
package com.setantamedia.fulcrum.common;

public class SortRule {

	public final static int SORT_ASCENDING = 0;
	public final static String SORT_ASCENDING_NAME = "ascending";

	public final static int SORT_DESCENDING = 1;
	public final static String SORT_DESCENDING_NAME = "descending";

	private int direction = SORT_ASCENDING;
	private String fieldName = "";
	private String fieldGuid = "";

	public SortRule() {

	}

	public boolean isAscending() {
		return (direction == SORT_ASCENDING);
	}

	private boolean validDirection(int value) {
		return (value == SORT_ASCENDING || value == SORT_DESCENDING) ? true : false;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		if (validDirection(direction)) {
			this.direction = direction;
		}
	}

	public void setDirection(String direction) {
		if (SORT_ASCENDING_NAME.equals(direction)) {
			this.direction = SORT_ASCENDING;
		} else if (SORT_DESCENDING_NAME.equals(direction)) {
			this.direction = SORT_DESCENDING;
		}
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldGuid() {
		return fieldGuid;
	}

	public void setFieldGuid(String fieldGuid) {
		this.fieldGuid = fieldGuid;
	}

}
