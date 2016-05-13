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
import java.io.IOException;
import java.nio.file.Files;
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

    public static final UniversalRenderingRequest KILL_SIGNAL = new UniversalRenderingRequest(null);

    private BlockingQueue<UniversalRenderingRequest> requestsQueue;
    private ConcurrentMap<Integer, UniversalRenderingRequest> runningRequests = new ConcurrentHashMap<>();

    private NodeJS node;
    private V8 v8;

    private V8Object api;
    private V8Object renderedCallback;

    private Thread executingThread;

    private File serverBundleFile;
    private String index;

    private AtomicInteger nextRequestId = new AtomicInteger(0);

    public UniversalRenderingEngine(File serverBundleFile, String index) {
        this(serverBundleFile, index, new LinkedBlockingQueue<>());

    }

    public UniversalRenderingEngine(File serverBundleFile, String index, BlockingQueue<UniversalRenderingRequest> requestsQueue) {
        this.serverBundleFile = serverBundleFile;
        this.requestsQueue = requestsQueue;
        this.index = index;
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
                    array.push(requestId);
                    array.push(renderedCallback);
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

    public void join() throws InterruptedException {
        executingThread.join();
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
            V8Array clientHtml = new V8Array(v8);
            clientHtml.push(index);
            api.executeVoidFunction("setClientHtml", clientHtml);
            clientHtml.release();
        }, "registerJavaEngine");

        node.require(serverBundleFile.getAbsoluteFile()).release();

        v8.registerJavaMethod((received, params) -> {
            V8Object exception = params.getObject(0);
            String result = params.getString(1);
            int requestId = params.getInteger(2);

            UniversalRenderingRequest pending = runningRequests.remove(requestId);
            if (pending == null) {
                throw new IllegalStateException("unregistered request completed");
            }

            if (exception == null) {
                pending.getResult().complete(result);
            } else {
                pending.getResult().completeExceptionally(new UniversalRenderException(exception.toString()));
            }

        }, "renderCallback");

        renderedCallback = v8.getObject("renderCallback");

    }

}
