package com.setantamedia.fulcrum.locationmonitor;

import com.setantamedia.fulcrum.config.FulcrumConfig;
import java.nio.file.Path;

public interface LocationListener {
	public void fileCreated(Path file);
	public void fileDeleted(Path file);
	public void fileModified(Path file);

	public void directoryCreated(Path directory);
	public void directoryDeleted(Path directory);
	public void directoryModified(Path directory);

   public void init();
   public void setFulcrumConfig(FulcrumConfig fulcrumConfig);
   public void terminate();
}