/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bennyl
 */
public class UniversalRenderer implements Closeable {

    private UniversalRenderingEnginesPool renderers;
    private Thread liveReloader = null;
    private File serverBundlePath;
    private final int numEngines;
    private final String index;
    private LoadingCache<String, CompletableFuture<String>> renderCache = null;

    public UniversalRenderer(File serverBundlePath, String index, int numEngines) {
        this.serverBundlePath = serverBundlePath;
        this.numEngines = numEngines;
        this.index = index;
    }

    public void useUrlCache() {
        this.renderCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<String, CompletableFuture<String>>() {
                    public CompletableFuture<String> load(String url) throws Exception {
                        return renderers.submit(url);
                    }
                });
    }

    public CompletableFuture<String> render(String url) {
        if (renderCache != null) {
            try {
                return renderCache.get(url);
            } catch (ExecutionException ex) {
                //this should never happen
                throw new RuntimeException(ex);
            }
        }
        return renderers.submit(url);
    }

    public void start() {
        if (renderers != null) {
            throw new IllegalStateException("already started");
        }

        renderers = new UniversalRenderingEnginesPool(numEngines, serverBundlePath, index);
        renderers.start();
    }

    @Override
    public void close() throws IOException {
        try {
            renderers.stop();
            renderers = null;
            if (this.renderCache != null) {
                //reset the cache
                this.useUrlCache();
            }

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
