package com.setantamedia.fulcrum.webdav;

import com.setantamedia.fulcrum.CoreServer;
import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class DisResourceFactory implements ResourceFactory {

   private final static Logger logger = Logger.getLogger(DisResourceFactory.class);
   private CoreServer disServer = null;

   @Override
   public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException {
      Resource result = null;
      Path path = Path.path(url);
      result = find(path);
      return result;
   }

   private Resource find(Path path) {
      Resource result = null;
      String[] bits = path.getParts();

      String categoryRoot = null;
      if (bits.length > 3) {
         categoryRoot = bits[3];
         for (int i = 4; i < bits.length; i++) {
            categoryRoot += ":" + bits[i];
         }
      }
      String baseUrl = bits[0] + "/" + bits[1];
      String connectionName = bits[2];
      result = new DisFolderResource(baseUrl, disServer.getDam(), connectionName, null, categoryRoot);
      return result;
   }

   public CoreServer getDisServer() {
      return disServer;
   }

   public void setDisServer(CoreServer disServer) {
      this.disServer = disServer;
   }
}
