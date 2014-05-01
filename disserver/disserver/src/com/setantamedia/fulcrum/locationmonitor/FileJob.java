package com.setantamedia.fulcrum.locationmonitor;

import com.setantamedia.fulcrum.ws.types.Record;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.apache.log4j.Logger;

public class FileJob implements Runnable {
	private static Logger logger = Logger.getLogger(FileJob.class);

	public enum Operations {
		create, delete, modify, rename
	}

	public enum Priorities {
		low, medium, high
	}

	public final static int DEFAULT_STABLE_TEST_TIMEOUT = 100; // 100 milliseconds
	public final static int DEFAULT_MAX_RETRIES = 3;

	private FileJobStatus status = null;

	private Priorities priority = Priorities.medium;

	private int maxRetries = DEFAULT_MAX_RETRIES;

	private int retries = 0;

	private boolean stable = false;

	private Path file = null;
	private long fileSize = 0L;
	private Date startTime = null;
	private Date endTime = null;
	private int stableTestTimout = DEFAULT_STABLE_TEST_TIMEOUT;
	private Integer id = null;
	private Record record = null;

	private IFileProcessorWorker worker = null;
	private Operations operation = null;

	public FileJob() {

	}

   @Override
	public void run() {
		if (status == null) {
			status = new FileJobStatus();
		}
		if (!isStable()) return;
		try {
			switch (operation) {
			case create:
				status.setException(null);
				status.setStatus(worker.doFileCreated(file));
				break;
			case delete:
				status.setException(null);
				status.setStatus(worker.doFileDeleted(file));
				break;
			case modify:
				status.setException(null);
				status.setStatus(worker.doFileModified(file));
				break;
			case rename:
				break;
			default:
				break;
			}
		} catch (Exception e) {
			status.setException(e);
		} finally {
			runDone();
		}
	}

	private void runDone() {
		retries++;
		if (retries >= maxRetries) {
			status.setStatus(FileJobStatus.StatusValues.failed);
			status.setFailReason("Max retries of " + maxRetries +" reached");
			logger.error("Job failed - "+status.getFailReason());
		}
	}

	public FileJobStatus getStatus() {
		return status;
	}

	public Priorities getPriority() {
		return priority;
	}

	public void setPriority(Priorities priority) {
		this.priority = priority;
	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public int getRetries() {
		return retries;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public boolean isStable() {
		stable = false;
		if (file == null) {
			return stable;
		}
		try {
			long l = Files.size(file);
			if (fileSize > 0L && l == fileSize) {
				stable = true;
			} else {
				// wait a bit and see if still growing
				Thread.sleep(stableTestTimout);
				long l1 = Files.size(file);
				if (l1 == l) {
					// ok - not growing, so let us assume it is stable
					stable = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stable;
	}

	public IFileProcessorWorker getWorker() {
		return worker;
	}

	public void setWorker(IFileProcessorWorker worker) {
		this.worker = worker;
	}

	public Operations getOperation() {
		return operation;
	}

	public void setOperation(Operations operation) {
		this.operation = operation;
	}

   @Override
	public String toString() {
		return file.toString() + " (Operation: "+operation+" - Priority: "+priority+")";
	}

	public int getStableTestTimout() {
		return stableTestTimout;
	}

	public void setStableTestTimout(int stableTestTimout) {
		this.stableTestTimout = stableTestTimout;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}
}
