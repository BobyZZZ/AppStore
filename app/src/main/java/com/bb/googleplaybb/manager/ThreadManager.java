package com.bb.googleplaybb.manager;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Boby on 2018/7/18.
 */

public class ThreadManager {
    private static ThreadPool mThreadPool;

    public static ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadManager.class) {
                if (mThreadPool == null) {
                    int cpuCount = Runtime.getRuntime().availableProcessors();
                    System.out.println("cpuCount : " + cpuCount);
                    mThreadPool = new ThreadPool(2 * cpuCount + 1, 2 * cpuCount + 1, 60L);
                }
            }
        }
        return mThreadPool;
    }

    public static class ThreadPool {
        private int corePoolSize;
        private int maxPoolSize;
        private long keepAliveTime;

        private ThreadPoolExecutor executor;

        private ThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;//主线程数
            this.maxPoolSize = maxPoolSize;//最大线程数
            this.keepAliveTime = keepAliveTime;//休息时间
        }

        public void execute(Runnable r) {
            if (executor == null) {
                executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
            }
            executor.execute(r);
            Log.e("ThreadManager", "execute: " + r);
        }

        public void cancel(Runnable r) {
            if (executor != null) {
                BlockingQueue<Runnable> queue = executor.getQueue();
                if (queue.contains(r)) {
                    queue.remove(r);
                    Log.e("ThreadManager", "cancel: " + r);
                }
            }
        }
    }

}
