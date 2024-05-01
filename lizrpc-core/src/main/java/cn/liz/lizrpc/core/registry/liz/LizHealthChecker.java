package cn.liz.lizrpc.core.registry.liz;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LizHealthChecker {

    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;

    public void start() {
        log.info(" ====== [LizHealthChecker] : start ...");
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
        log.info(" ====== [LizHealthChecker] : stop...");
        gracefulShutdownExecutor(consumerExecutor);
        gracefulShutdownExecutor(providerExecutor);
        log.info(" ====== [LizHealthChecker] : stopped !");
    }

    public void providerChecker(Callback callback) {
//        providerExecutor.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    callback.call();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        , 5, 5, TimeUnit.SECONDS);
        providerExecutor.scheduleAtFixedRate(
                () -> {
                    try {
                        callback.call();
                    } catch (Exception e) {
                        log.warn("[LizHealthChecker] providerChecker exception , e : ", e);
                    }
                }
                , 5, 5, TimeUnit.SECONDS);
    }

    public void consumerChecker(Callback callback) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.warn("[LizHealthChecker] consumerChecker exception , e : ", e);
            }

        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    private void gracefulShutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void call() throws Exception;
    }

}
