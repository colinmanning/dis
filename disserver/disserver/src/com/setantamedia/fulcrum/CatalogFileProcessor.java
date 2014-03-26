package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import java.nio.file.Path;

/**
 *
 * @author Colin Manning
 */
public class CatalogFileProcessor extends FileProcessor {

   @Override
	public void init() {
		super.init();
   }

   @Override
   public void directoryModified(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   @Override
   public void terminate() {

   }

}
