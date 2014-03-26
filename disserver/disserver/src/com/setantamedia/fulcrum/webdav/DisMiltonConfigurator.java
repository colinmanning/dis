package com.setantamedia.fulcrum.webdav;

import com.setantamedia.fulcrum.CoreServer;
import com.setantamedia.fulcrum.ws.FulcrumServletContextListener;
import io.milton.http.HttpManager;
import io.milton.servlet.Config;
import io.milton.servlet.DefaultMiltonConfigurator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class DisMiltonConfigurator extends DefaultMiltonConfigurator {

   private final static Logger logger = Logger.getLogger(DisMiltonConfigurator.class);
   private DisResourceFactory resourceFactory = null;
   private CoreServer disServer = null;

   @Override
   protected void build() {
      super.build();
      resourceFactory = (DisResourceFactory) builder.getMainResourceFactory(); // get our resource factory from the builder
    }

   @Override
   public HttpManager configure(Config config) throws ServletException {
      HttpManager result = super.configure(config);
      ServletContext context =  config.getServletContext();
      disServer = (CoreServer) context.getAttribute(FulcrumServletContextListener.MAIN_SERVER);
      resourceFactory.setDisServer(disServer);
      logger.info("Gor DIS Main Server into Milton WebDAV");
      return result;
   }

   @Override
   public void shutdown() {
      super.shutdown();
   }
}
