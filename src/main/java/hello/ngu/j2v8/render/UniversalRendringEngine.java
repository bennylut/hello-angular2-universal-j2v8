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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bennyl
 */
public class UniversalRendringEngine implements Runnable {

    private static final UniversalRendringRequest KILL_SIGNAL = new UniversalRendringRequest(null);

    private BlockingQueue<UniversalRendringRequest> requestsQueue = new LinkedBlockingQueue<>();

    private NodeJS node;
    private V8 v8;
    private V8Object api;

    private UniversalRendringRequest current = null;
    private V8Object renderedCallback;
    private Thread executingThread;

    private File serverBundleFile;

    public UniversalRendringEngine(File serverBundleFile) {
        this.serverBundleFile = serverBundleFile;
    }

    @Override
    public void run() {
        initNode();

        try {
            executingThread = Thread.currentThread();
            while (!executingThread.isInterrupted()) {
                current = null;
                current = requestsQueue.take();
                if (current == KILL_SIGNAL) {
                    return;
                }

                V8Array array = new V8Array(v8);
                array.push(current.getUrl());
                array.push(renderedCallback);

                api.executeVoidFunction("render", array);
                driveNode();
                array.release();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(UniversalRendringEngine.class.getName()).log(Level.WARNING, null, ex);
        } finally {
            closeNode();
        }
    }

    public void addRendringRequest(UniversalRendringRequest request) {
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
            current.getResult().complete(params.getString(0));
        }, "renderCallback");

        renderedCallback = v8.getObject("renderCallback");

    }

    private void driveNode() {
        while (node.handleMessage()) {
            //keep looping..
        }
    }

}
