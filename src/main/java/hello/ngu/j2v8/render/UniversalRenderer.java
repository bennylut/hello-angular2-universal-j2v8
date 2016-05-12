/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bennyl
 */
public class UniversalRenderer implements Closeable {

    private UniversalRendringEngine handler;
    private Thread liveReloader = null;
    private File serverBundlePath;

    public UniversalRenderer(File serverBundlePath) {
        this.serverBundlePath = serverBundlePath;
    }

    public CompletableFuture<String> render(String url) {
        UniversalRendringRequest request = new UniversalRendringRequest(url);
        handler.addRendringRequest(request);
        return request.getResult();
    }

    public void start() {
        if (handler != null) {
            throw new IllegalStateException("already started");
        }

        handler = new UniversalRendringEngine(serverBundlePath);
        new Thread(handler).start();
    }

    @Override
    public void close() throws IOException {
        try {
            handler.stop();
            handler = null;
        } catch (InterruptedException ex) {
            throw new IOException("interrupted while stopping render engine", ex);
        }
    }

    public void startLiveReload() throws IOException {
        if (liveReloader != null) {
            throw new IllegalStateException("live reload already started");
        }

        Path bundle = serverBundlePath.toPath();

        liveReloader = new Thread(() -> {
            try {
                System.out.println("starting live reload");
                FileTime time = Files.readAttributes(bundle, BasicFileAttributes.class).creationTime();

                for (;;) {
                    Thread.sleep(1000);

                    if (bundle.toFile().exists()) {

                        FileTime currentTime = Files.readAttributes(bundle, BasicFileAttributes.class).creationTime();
                        if (currentTime.compareTo(time) != 0) {
                            System.out.println("reloading");
                            reload();
                            System.out.println("reload done");
                            time = currentTime;
                        }
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(UniversalRenderer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(UniversalRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        liveReloader.start();
    }

    public void reload() throws IOException {
        this.close();
        this.start();
    }

}
