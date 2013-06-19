/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.fulcrum.webdav;

import com.setantamedia.fulcrum.common.Dam;
import com.setantamedia.fulcrum.ws.types.Record;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.GetableResource;
import io.milton.resource.ReplaceableResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Colin Manning
 */
public class DisBinaryResource extends DisFileResource implements GetableResource, ReplaceableResource {

   private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DisBinaryResource.class);
   private byte[] bytes;
   private String contentType;

   public DisBinaryResource(DisFolderResource folder, String baseUrl, Dam dam, String connectionName, Record record, byte[] bytes, String contentType) {
      super(folder, baseUrl, dam, connectionName, record);
      this.bytes = bytes;
   }

   protected Object clone(DisFolderResource newFolder, String destName) {
      return new DisBinaryResource(newFolder, baseUrl, dam, connectionName, record, bytes, contentType);
   }

   @Override
   public void sendContent(OutputStream out, Range range, Map params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
      out.write(bytes);
   }

   @Override
   public Long getContentLength() {
      return (long) bytes.length;
   }

   @Override
   public String getContentType(String accept) {
      return contentType;
   }

   @Override
   public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
      try {
         ByteArrayOutputStream bos = DisFolderResource.readStream(in);
         this.bytes = bos.toByteArray();
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public Long getMaxAgeSeconds(Auth auth) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String getUniqueId() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String getName() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object authenticate(String string, String string1) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean authorise(Request rqst, Method method, Auth auth) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String getRealm() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Date getModifiedDate() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String checkRedirect(Request rqst) throws NotAuthorizedException, BadRequestException {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
