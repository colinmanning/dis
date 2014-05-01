package com.setantamedia.fulcrum.locationmonitor;

import java.nio.file.Path;

public interface IFileProcessorWorker {

	public FileJobStatus.StatusValues doFileCreated(Path file);
	public FileJobStatus.StatusValues doFileDeleted(Path file);
	public FileJobStatus.StatusValues doFileModified(Path fileName);
	public FileJobStatus.StatusValues doDirectoryCreated(Path file);
	public FileJobStatus.StatusValues doDirectoryDeleted(Path file);
}
