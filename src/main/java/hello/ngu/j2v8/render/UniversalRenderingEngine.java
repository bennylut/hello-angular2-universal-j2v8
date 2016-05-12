/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bennyl
 */
public class UniversalRenderingEngine implements Runnable {

    private static final UniversalRenderingRequest KILL_SIGNAL = new UniversalRenderingRequest(null);

    private BlockingQueue<UniversalRenderingRequest> requestsQueue;
    private ConcurrentMap<Integer, UniversalRenderingRequest> runningRequests = new ConcurrentHashMap<>();

    private NodeJS node;
    private V8 v8;
    private V8Object api;

    private V8Object renderedCallback;
    private Thread executingThread;

    private File serverBundleFile;

    private AtomicInteger nextRequestId = new AtomicInteger(0);

    public UniversalRenderingEngine(File serverBundleFile) {
        this(serverBundleFile, new LinkedBlockingQueue<>());
    }

    public UniversalRenderingEngine(File serverBundleFile, BlockingQueue<UniversalRenderingRequest> requestsQueue) {
        this.serverBundleFile = serverBundleFile;
        this.requestsQueue = requestsQueue;
    }

    @Override
    public void run() {
        initNode();

        try {
            executingThread = Thread.currentThread();
            while (!executingThread.isInterrupted()) {
                UniversalRenderingRequest req = null;
                if (node.handleMessage()) {
                    req = requestsQueue.poll();
                } else {
                    req = requestsQueue.take();
                }

                if (req == KILL_SIGNAL) {
                    return;
                }

                if (req != null) {
                    int requestId = nextRequestId.incrementAndGet();
                    runningRequests.put(requestId, req);
                    V8Array array = new V8Array(v8);
                    array.push(req.getUrl());
                    array.push(renderedCallback);
                    array.push(requestId);
                    api.executeVoidFunction("render", array);

                    array.release();

                }

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UniversalRenderingEngine.class.getName()).log(Level.WARNING, null, ex);
        } finally {
            closeNode();
        }
    }

    public void addRendringRequest(UniversalRenderingRequest request) {
        this.requestsQueue.add(request);
    }

    public void stop() throws InterruptedException {
        requestsQueue.add(KILL_SIGNAL);
        executingThread.join();
    }

    private void closeNode() {
        api.release();
        renderedCallback.release();
        node.release();
    }

    private void initNode() {

        node = NodeJS.createNodeJS();
        v8 = node.getRuntime();

        v8.registerJavaMethod((V8Object receiver, V8Array parameters) -> {
            api = parameters.getObject(0);
        }, "registerJavaAPI");

        node.require(serverBundleFile.getAbsoluteFile()).release();

        v8.registerJavaMethod((received, params) -> {
            String result = params.getString(0);
            int requestId = params.getInteger(1);

            UniversalRenderingRequest pending = runningRequests.remove(requestId);
            if (pending == null) {
                throw new IllegalStateException("unregistered request completed");
            }

            pending.getResult().complete(result);
        }, "renderCallback");

        renderedCallback = v8.getObject("renderCallback");

    }

}
