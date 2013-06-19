package com.setantamedia.fulcrum.webdav;

import com.setantamedia.fulcrum.DamManagerNotImplementedException;
import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.common.SearchDescriptor;
import com.setantamedia.fulcrum.common.Utilities;
import com.setantamedia.fulcrum.ws.types.Category;
import com.setantamedia.fulcrum.ws.types.QueryResult;
import com.setantamedia.fulcrum.ws.types.Record;
import io.milton.common.StreamUtils;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class DisFolderResource implements GetableResource, PropFindableResource, PutableResource, MakeCollectionableResource {

   private final static Logger logger = Logger.getLogger(DisFolderResource.class);
   private Dam dam = null;
   private String connectionName = null;
   private String id = null;
   private String name = null;
   private String baseName = null;
   private String nameUrl = null;
   private String baseUrl = null;
   private final static String ROOT = "$Categories";

   public DisFolderResource(String baseUrl, Dam dam, String connectionName, String id, String name) {
      this.dam = dam;
      this.connectionName = connectionName;
      this.id = id;
      this.name = name;
      this.baseUrl = baseUrl;
      String[] bits = name.split(":");
      if (this.name != null) {
         nameUrl = "/" + baseUrl + "/" + connectionName + "/" + bits[0];
         if (bits.length > 1) {
            for (int i = 1; i < bits.length; i++) {
               nameUrl += "/" + bits[i];
            }
         }
         baseName = bits[bits.length - 1];
      } else {
         this.name = ROOT;
         baseName = "";
         nameUrl = "";
      }
   }

   @Override
   public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
      DisFolderResource result = null;
      return result;
   }

   @Override
   public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
      Resource result = null;
      for (Resource resource : getChildren()) {
         if (resource.getName().equals(childName)) {
            result = resource;
            break;
         }
      }
      return result;
   }

   @Override
   public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
      List<Resource> resources = new ArrayList<>();
      try {
         String categoryPath = ROOT + ":" + name;

         // get categories
         Category categoryRoot = dam.manager.findCategories(dam.connections.get(connectionName), categoryPath);
         if (id == null) {
            // First time
            id = String.valueOf(categoryRoot.getId());
         }

         // get assets
         SearchDescriptor sd = new SearchDescriptor();
         sd.setViewName("webdav");
         QueryResult categoryAssets = dam.manager.categorySearch(dam.connections.get(connectionName), id, sd, false, Utilities.getLocale());

         if (categoryRoot != null) {
            for (Category category : categoryRoot.getSubCategories()) {
               DisFolderResource resource = new DisFolderResource(baseUrl, dam, connectionName, String.valueOf(category.getId()), name + ":" + category.getName());
               resources.add(resource);
            }
            Record[] assets = categoryAssets.getRecords();
            for (Record asset : assets) {
               DisFileResource resource = new DisFileResource(this, baseUrl, dam, connectionName, asset);
               resources.add(resource);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return resources;
   }

   public static ByteArrayOutputStream readStream(final InputStream in) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      StreamUtils.readTo(in, bos);
      return bos;
   }

   @Override
   public String getUniqueId() {
      return connectionName + "-" + id;
   }

   @Override
   public String getName() {
      return baseName;
   }

   @Override
   public Object authenticate(String string, String string1) {
      return "anonymous";
   }

   @Override
   public boolean authorise(Request rqst, Method method, Auth auth) {
      return true;
   }

   @Override
   public String getRealm() {
      return null;
   }

   @Override
   public Date getModifiedDate() {
      return Calendar.getInstance().getTime();
   }

   @Override
   public String checkRedirect(Request rqst) throws NotAuthorizedException, BadRequestException {
      return null;
   }

   @Override
   public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
      DisFolderResource result = new DisFolderResource(baseUrl, dam, connectionName, null, newName);
      return result;
   }

   @Override
   public Date getCreateDate() {
      return Calendar.getInstance().getTime();
   }

   @Override
   public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
      PrintWriter pw = new PrintWriter(out);
      pw.print("<html><body>");
      pw.print("<h2>" + getNameUrl() + "</h2>");
      doBody(pw);
      pw.print("</body></html>");
      pw.flush();
   }

   protected void doBody(PrintWriter pw) {
      pw.print("<ul>");
      try {
         for (Resource resource : getChildren()) {
            if (resource instanceof DisFolderResource) {
               String href = ((DisFolderResource) resource).getNameUrl();
               href = href + "/";
               pw.print("<li><a href='" + href + "'>" + ((DisFolderResource) resource).getBaseName() + "</a></li>");
            } else if (resource instanceof DisFileResource) {
               pw.print("<li>" + ((DisFileResource) resource).getName() + "</li>");
            }
         }
      } catch (NotAuthorizedException | BadRequestException e) {
         e.printStackTrace();
      }
      pw.print("</ul>");
   }

   @Override
   public Long getMaxAgeSeconds(Auth auth) {
      return null;
   }

   @Override
   public String getContentType(String string) {
      return null;
   }

   @Override
   public Long getContentLength() {
      return null;
   }

   public String getNameUrl() {
      return nameUrl;
   }

   public String getBaseName() {
      return baseName;
   }
}
