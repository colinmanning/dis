/**
 * 
 */
package com.setantamedia.fulcrum.common;

import java.util.Locale;

public class SearchDescriptor {

   public static enum QueryTypes { New, Narrow, Broaden, Page }
   public static enum PreviewFormats { Jpg, Png }
   
   protected String viewName = null;
   protected SortRule sortRule = null;
   protected String filter = null;
   protected String namedQuery = null;
   protected int offset = -1;
   protected int count = -1;
   protected QueryTypes queryType = null;
   protected Locale locale = Locale.getDefault();
   protected PreviewFormats previewFormat = PreviewFormats.Jpg;

   public SearchDescriptor() {

   }

   public String getViewName() {
      return viewName;
   }

   public void setViewName(String viewName) {
      this.viewName = viewName;
   }

   public SortRule getSortRule() {
      return sortRule;
   }

   public void setSortRule(SortRule sortRule) {
      this.sortRule = sortRule;
   }

   public String getFilter() {
      return filter;
   }

   public void setFilter(String filter) {
      this.filter = filter;
   }

   public int getOffset() {
      return offset;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public int getCount() {
      return count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public Locale getLocale() {
      return locale;
   }

   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   
   public QueryTypes getQueryType() {
      return queryType;
   }

   
   public void setQueryType(QueryTypes queryType) {
      this.queryType = queryType;
   }

   
   public PreviewFormats getPreviewFormat() {
      return previewFormat;
   }

   
   public void setPreviewFormat(PreviewFormats previewFormat) {
      this.previewFormat = previewFormat;
   }

public String getNamedQuery() {
	return namedQuery;
}

public void setNamedQuery(String namedQuery) {
	this.namedQuery = namedQuery;
}
   
   
}
