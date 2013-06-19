package com.setantamedia.fulcrum.locationmonitor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

public class FileProcessorThread extends Thread {

	public final static int DEFAULT_WAIT = 5000; // every 5 seconds

	private static Logger logger = Logger.getLogger(FileProcessorThread.class);

	public final static int DEFAULT_MAX_WORKER_THREADS = 4;

	private final ArrayList<FileJob> jobs = new ArrayList<>();
	private ExecutorService threadPool = null;


	public FileProcessorThread() {
		super();
		threadPool = Executors.newFixedThreadPool(DEFAULT_MAX_WORKER_THREADS);
	}

	@Override
	public void run() {
		while (true) {
			synchronized (jobs) {
				for (FileJob job:jobs) {
					threadPool.submit(job);
					//job.run();
					//job.runDone();
				}
				/*
				// remove appropriate jobs
				Vector<FileJob> tmpJobs = new Vector<FileJob>();
				for (FileJob job:jobs) {
					FileJobStatus status = job.getStatus();
					if (status.isCompleted() || status.isFailed() || job.getRetries() >= job.getMaxRetries()) {
						continue;
					} else {
						tmpJobs.add(job);
					}
				}
				jobs = tmpJobs;
				*/
			}
			jobs.clear();
			try {
				Thread.sleep(DEFAULT_WAIT);
			} catch (InterruptedException ie) {
				logger.info("Thread interrupted, exiting");
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start() {
		super.start();
	}

	public void addJob(FileJob job) {
		logger.info("New file job added to worker queue: "+job);
		jobs.add(job);
	}

}
