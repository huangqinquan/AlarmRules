package com.meiya.alarm.concurrent;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Administrator on 2017/12/20.
 */
public class SemaphoreExecutor {

    private static Logger LOG = Logger.getLogger(SemaphoreExecutor.class);

    public static boolean monitorSemaphorePermits = true;

    public static long semaphorePermitsPeriod = 12000L;		// 12s执行一次监控线程

    //	private static Map<String, Semaphore> semaphoreMaps = new ConcurrentHashMap<String, Semaphore>();
    public static Map<String, SemaphoreExecutor> semaphoreMaps = new ConcurrentHashMap<String, SemaphoreExecutor>();

    protected static ScheduledExecutorService scheduled = null;

    protected ExecutorService executor;

    protected Semaphore semaphore;

    protected String threadName;

    protected int size;

    static {
        if (monitorSemaphorePermits) {
            /**
             * 监控可用的线程池数量,可用于判断该线程池是否有瓶颈
             */
            scheduled = Executors.newScheduledThreadPool(1);
            startMonitorExecutor();
        }
    }

    public SemaphoreExecutor(int size, String threadName) {
        this.threadName = threadName;
        this.size = size;
        executor = Executors.newFixedThreadPool(size);
        semaphore = new Semaphore(size);
        semaphoreMaps.put(threadName, this);
    }

    //weic 为了继承的时候用
    protected SemaphoreExecutor() {
    }

    // 改构造方法将不再提供
	/*public SemaphoreExecutor(ExecutorService executor, Semaphore semaphore) {
		this.executor = executor;
		this.semaphore = semaphore;
		this.threadName = "SemaphoreExecutor-" + new Random().nextInt(Integer.MAX_VALUE);
	}*/

    public void execute(final Runnable command) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    command.run();
                } finally {
                    semaphore.release();
                }
            }
        });
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    public String getThreadName() {
        return threadName;
    }

    public void shutdown() {
        executor.shutdown();
        semaphoreMaps.remove(threadName);
    }

    private static void startMonitorExecutor() {
        scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, SemaphoreExecutor>> iter = semaphoreMaps.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, SemaphoreExecutor> entry = iter.next();
                    LOG.info("{" + entry.getKey() + "} Semaphore available permits: {" + entry.getValue().getSemaphore().availablePermits() + "}" );
                }
            }
        }, 0, semaphorePermitsPeriod, TimeUnit.MILLISECONDS);
    }

    public static synchronized void exit() {
        stopMonitorExecutor();
        Iterator<Map.Entry<String, SemaphoreExecutor>>  iter = semaphoreMaps.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, SemaphoreExecutor> entry = iter.next();
            SemaphoreExecutor semaphoreExecutor = entry.getValue();
            semaphoreExecutor.getExecutor().shutdown();
        }
        while (true) {
            boolean isSafeExit = true;
            for (Map.Entry<String, SemaphoreExecutor> entry : semaphoreMaps.entrySet()) {
                SemaphoreExecutor semaphoreExecutor = entry.getValue();
                int availablePermits = semaphoreExecutor.getSemaphore().availablePermits();
                if (availablePermits < semaphoreExecutor.getSize()) {
                    isSafeExit = false;
                    LOG.warn("{" + semaphoreExecutor.getThreadName() + "} is exiting, available permits:{" + availablePermits + "}");
                }
            }
            if (isSafeExit) {
                LOG.info("all semaphore executor already exit...");
                break;
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     *  可以停止日志的定时打印功能
     */
    public static synchronized void stopMonitorExecutor() {
        if (monitorSemaphorePermits) {
            scheduled.shutdown();
        }
        monitorSemaphorePermits = false;
    }
}
