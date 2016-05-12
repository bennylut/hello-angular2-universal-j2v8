/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import java.io.File;
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

    public UniversalRenderingEnginesPool(int numEngines, File serverBundleFile) {
        this.engines = new UniversalRenderingEngine[numEngines];
        this.requestsQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < numEngines; i++) {
            this.engines[i] = new UniversalRenderingEngine(serverBundleFile, requestsQueue);
        }
    }

    public void start() {
        for (UniversalRenderingEngine e : engines) {
            new Thread(e).start();
        }
    }

    public void stop() throws InterruptedException {
        for (UniversalRenderingEngine e : engines) {
            e.stop();
        }
    }

    public CompletableFuture<String> submit(String url) {
        final UniversalRenderingRequest request = new UniversalRenderingRequest(url);
        requestsQueue.add(request);
        return request.getResult();
    }

}
