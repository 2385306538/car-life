package com.order.config;

import com.zaxxer.hikari.util.UtilityElf;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        /**
         * 工作顺序：
         * 一个线程池，core 7 ,max20,queue:50  100并发进来是怎么分配的
         *      1. 7个core会立即执行，50个会进入队列,在开13个进行执行
         *      2. 剩下30个，可以使用拒绝策略抛弃
         *      3. 如果不想抛弃还要执行：callerRunsPolicy执行
         *      
         * LinkedBlockingDeque<>() 默认是Integer的最大值
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,
                10,
                200,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
    public static class Thread01 extends Thread {
        @Override
        public void run(){
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果: " + i);
        }
    }
}


