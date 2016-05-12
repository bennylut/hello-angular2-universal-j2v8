/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bennyl
 */
public class SimplePerformanceTest {

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>() {
            @Override
            protected byte[] initialValue() {
                return new byte[1 << 20]; //1m
            }
        };

        long time = System.currentTimeMillis();
        System.out.println("Start");
        ExecutorService exec = Executors.newFixedThreadPool(8);
        AtomicInteger idx = new AtomicInteger(0);
        for (int i = 0; i < 8; i++) {
            exec.submit(() -> {
                try {
                    URL url = new URL("http://localhost:3000/");
                    for (int j = 0; j < 1000; j++) {
                        InputStream stream = url.openStream();
                        stream.read(buffer.get());
                        stream.close();
                        final int cidx = idx.incrementAndGet();
                        if (cidx % 1000 == 0) {
                            System.out.println("time to handle " + cidx + " requests: " + (System.currentTimeMillis() - time));
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SimplePerformanceTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.DAYS);
        System.out.println("took: " + (System.currentTimeMillis() - time));

    }
}
