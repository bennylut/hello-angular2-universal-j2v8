/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import com.google.gson.Gson;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author bennyl
 */
public class UniversalRenderingEnginesPool {

    private UniversalRenderingEngine[] engines;
    private BlockingQueue<UniversalRenderingRequest> requestsQueue;

    public UniversalRenderingEnginesPool(UniversalRenderConfiguration conf) {
        this.engines = new UniversalRenderingEngine[conf.numEngines()];
        this.requestsQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < conf.numEngines(); i++) {
            this.engines[i] = new UniversalRenderingEngine(conf, requestsQueue);
        }
    }

    public void start() {
        for (UniversalRenderingEngine e : engines) {
            new Thread(e).start();
        }
    }

    public void stop() throws InterruptedException {
        for (UniversalRenderingEngine e : engines) {
            requestsQueue.add(UniversalRenderingEngine.KILL_SIGNAL);
        }

        for (UniversalRenderingEngine e : engines) {
            e.join();
        }

    }

    public CompletableFuture<String> submit(Object key) {
        final UniversalRenderingRequest request = new UniversalRenderingRequest(key);
        requestsQueue.add(request);
        return request.getResult();
    }

}
