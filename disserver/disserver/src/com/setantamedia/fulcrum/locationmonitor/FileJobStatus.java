package com.setantamedia.fulcrum.locationmonitor;

public class FileJobStatus {
	public static enum StatusValues {
		notstarted, inprogress, completed, failed
	}

	private Exception exception = null;
	
	private StatusValues status = StatusValues.notstarted;
	private String failReason = null;
	
	public FileJobStatus() {
		
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public boolean isRunning() {
		return (status == StatusValues.inprogress);
	}

	public void setStatus(StatusValues status) {
		this.status = status;
	}

	public boolean isCompleted() {
		return (status == StatusValues.completed);
	}

	public boolean isFailed() {
		return (status == StatusValues.failed);
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

}
