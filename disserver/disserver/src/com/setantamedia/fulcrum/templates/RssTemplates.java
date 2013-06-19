package com.setantamedia.fulcrum.templates;

public class RssTemplates {

   // Hard code for prototype - will read from file of course
   // {field_name} are place holders for field values
   public final static String ITEM_DESCRIPTION = "&lt;table border=&quot;0&quot; cellpadding=&quot;2&quot; cellspacing=&quot;7&quot; style=&quot;vertical-align:top;&quot;&gt;" + "&lt;tr&gt;"
            + "&lt;td width=&quot;80&quot; align=&quot;center&quot; valign=&quot;top&quot;&gt;" + "&lt;font style=&quot;font-size:85%;font-family:arial,sans-serif&quot;&gt;"
            + "&lt;a href=&quot;${preview_rssmedium}?maxSize=400&quot; target=&quot;_blank&quot;&gt;"
            + "&lt;img src=&quot;${preview_rsssmall}?size=80&quot; alt=&quot;Image ${ID}&quot; border=&quot;1&quot; width=&quot;80&quot; height=&quot;80&quot; /&gt;" + "&lt;br /&gt;"
            + "&lt;font size=&quot;-2&quot;&gt;${Record Name}&lt;/font&gt;" + "&lt;/a&gt;" + "&lt;/font&gt;" + "&lt;/td&gt;" + "&lt;td valign=&quot;top&quot; class=&quot;j&quot;&gt;"
            + "&lt;font style=&quot;font-size:85%;font-family:arial,sans-serif&quot;&gt;" + "&lt;br /&gt;" + "&lt;div style=&quot;padding-top:0.4em;&quot;&gt;"
            + "&lt;img alt=&quot;&quot; height=&quot;1&quot; width=&quot;1&quot; /&gt;" + "&lt;/div&gt;" + "&lt;div class=&quot;lh&quot;&gt;"
            + "&lt;a href=&quot;${baseUrl}/asset/${catalog}/${ID}/get/${Record Name}&quot;/&gt;" + "&lt;b&gt;Download&lt;/b&gt;" + "&lt;/a&gt;" + "&lt;br /&gt;"
            + "&lt;a href=&quot;${metadata_flexapp}&quot; target=&quot;_blank&quot;&gt;" + "&lt;b&gt;${Caption}&lt;/b&gt;" + "&lt;/a&gt;" + "&lt;br /&gt;" + "&lt;font size=&quot;-1&quot;&gt;"
            + "&lt;b&gt;" + "&lt;font color=&quot;#6f6f6f&quot;&gt;${Copyright Notice}&lt;/font&gt;" + "&lt;/b&gt;" + "&lt;/font&gt;" + "&lt;br /&gt;"
            + "&lt;font size=&quot;-1&quot;&gt;${Description}.&lt;/font&gt;" + "&lt;br /&gt;" + "&lt;/font&gt;" + "&lt;br /&gt;" + "&lt;/font&gt;" + "&lt;br /&gt;" + "&lt;/div&gt;" + "&lt;/td&gt;"
            + "&lt;/tr&gt;" + "&lt;/table&gt;";

   public final static String ITEM_HEADER = "<title>Asset ${ID} - ${Record Name}</title>" + "<link>${baseUrl}/${catalog}/${viewName}/${ID}</link>"
            + "<guid isPermaLink=\"false\">tag:images.acalian.com,2011:cluster=${catalog}-${ID}</guid>" + "<pubDate>${timeNow}</pubDate>";

   public final static String HEADER = "<generator>NFE/1.0</generator>" + "<title>Images from Colin</title>" + "<link>${baseUrl}/${catalog}/${viewName}/${queryName}?rss</link>"
            + "<language>en</language>" + "<webMaster>colin.manning@mac.com</webMaster>" + "<copyright>&amp;copy;2011 Colin Manning</copyright>" + "<pubDate>${timeNow}</pubDate>"
            + "<lastBuildDate>${Asset Modification Date}</lastBuildDate>" + "<description> Images</description>" + "<image>" + "<title>Images from Colin</title>"
            + "<url>http://acalian.com/images/anon-logo.png</url>" + "<link>${baseUrl}/metadata/${catalog}/${viewName}/${queryName}?rss</link>" + "</image>";

   public final static String convert(String source) {
      String result = source;
      return result;
   }
}
