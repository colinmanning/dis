package com.setantamedia.fulcrum.common;

import java.util.HashMap;
import java.util.Map;

public class Query {

	private String name = null;
	private String text = null;
	private SortRule sortRule = null;

	public Query() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public SortRule getSortRule() {
		return sortRule;
	}

	public void setSortRule(SortRule sortRule) {
		this.sortRule = sortRule;
	}

	/**
	 * Build the actual query based on a set of parameters
	 * @param params the parameters to use in the query
	 * @return
	 */
	public String buildInstance(HashMap<String, String> params) {
		String result = text;
		for (Map.Entry<String, String> entry: params.entrySet()) {
			String paramKey = "\\$\\{"+entry.getKey()+"\\}";
			result = result.replaceAll(paramKey, entry.getValue());
		}
		return result;
	}
        
        @Override
        public String toString() {
            return text;
        }
}
