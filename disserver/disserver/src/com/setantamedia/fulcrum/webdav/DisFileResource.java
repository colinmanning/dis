package com.setantamedia.fulcrum.webdav;

import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.ws.types.Record;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.property.PropertySource.PropertyAccessibility;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.resource.GetableResource;
import io.milton.resource.MultiNamespaceCustomPropertyResource;
import io.milton.resource.PropFindableResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class DisFileResource implements GetableResource, PropFindableResource, MultiNamespaceCustomPropertyResource {

   private final static Logger logger = Logger.getLogger(DisFileResource.class);
   public final static String NS_DIS_WEBDAV = "com.setantamedia.fulcrum.webdav";
   protected Dam dam = null;
   protected String connectionName = null;
   protected String baseUrl = null;
   protected Long contentLength = -1L;
   protected String contentType = null;
   protected Record record = null;
   protected DisFolderResource folder = null;
   protected Map<String, Object> customProperties = new HashMap<>();

   public DisFileResource(DisFolderResource folder, String baseUrl, Dam dam, String connectionName, Record record) {
      Long v = record.getLongField("Asset Data Size (Long)");
      this.folder = folder;
      this.dam = dam;
      this.connectionName = connectionName;
      this.baseUrl = baseUrl;
      this.record = record;
      setProperty(new QName(NS_DIS_WEBDAV, "Comments"), record.getStringField("Caption"));
   }

   public String getNameSpaceURI() {
      return NS_DIS_WEBDAV;
   }

   @Override
   public void sendContent(OutputStream out, Range range, Map params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
   }

   @Override
   public Long getMaxAgeSeconds(Auth auth) {
      return null;
   }

   @Override
   public String getContentType(String string) {
      return contentType;
   }

   @Override
   public Long getContentLength() {
      return record.getLongField("Asset Data Size (Long)");
   }

   @Override
   public String getUniqueId() {
      return connectionName + "-" + record.getId();
   }

   @Override
   public String getName() {
      return record.getFileName();
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
      return record.getDateTimeField("Asset Modification Date");
   }

   @Override
   public Date getCreateDate() {
      return record.getDateTimeField("Asset Creation Date");
   }

   @Override
   public String checkRedirect(Request rqst) throws NotAuthorizedException, BadRequestException {
      return null;
   }

   @Override
   public final Object getProperty(QName name) {
      Object result = PropertyMetaData.UNKNOWN;
      if (name.getNamespaceURI().equals(NS_DIS_WEBDAV)) {
         result = customProperties.get(name.getLocalPart());
      }
      return result;
   }

   @Override
   public final void setProperty(QName name, Object value) {
      if (name.getNamespaceURI().equals(NS_DIS_WEBDAV)) {
         customProperties.put(name.getLocalPart(), (String) value);
      }
   }

   @Override
   public final PropertyMetaData getPropertyMetaData(QName name) {
      PropertyMetaData result = PropertyMetaData.UNKNOWN;
      if (name.getNamespaceURI().equals(NS_DIS_WEBDAV)) {
         result = new PropertyMetaData(PropertyAccessibility.WRITABLE, String.class);
      }
      return result;
   }

   @Override
   public final List<QName> getAllPropertyNames() {
      List<QName> result = new ArrayList<>();
      for (String key : customProperties.keySet()) {
         result.add(new QName(NS_DIS_WEBDAV, key));
      }
      return result;
   }
}
