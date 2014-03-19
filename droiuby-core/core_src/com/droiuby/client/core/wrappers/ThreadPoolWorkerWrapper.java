package com.droiuby.client.core.wrappers;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class ThreadPoolWorkerWrapper {
	
	ArrayList <Runnable> tasks = new ArrayList<Runnable>();
	
	public void addTask(Runnable task) {
		tasks.add(task);
	}
	
	public void start() {
		int worker_count = Runtime
				.getRuntime().availableProcessors() + 1;
		ExecutorService thread_pool = Executors.newFixedThreadPool(worker_count);
		Log.d(this.getClass().toString(), "Creating thread pool with " + worker_count);
		for(Runnable runnable : tasks) {
			thread_pool.execute(runnable);
		}
		thread_pool.shutdown();
		try {
			Log.d(this.getClass().toString(),
					"Waiting for download workers to finish.....");
			thread_pool.awaitTermination(240, TimeUnit.SECONDS);
			Log.d(this.getClass().toString(), "Download workers .... done.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
