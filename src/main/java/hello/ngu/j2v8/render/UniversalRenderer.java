/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import java.io.Closeable;
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
    private LoadingCache<Object, CompletableFuture<String>> renderCache = null;
    private final UniversalRenderConfiguration conf;
    private final Gson gson;

    public UniversalRenderer(UniversalRenderConfiguration conf, Gson gson) {
        this.conf = conf;
        this.gson = gson;
    }

    public void useCache() {
        this.renderCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<Object, CompletableFuture<String>>() {
                    public CompletableFuture<String> load(Object key) throws Exception {
                        return renderers.submit(key);
                    }
                });
    }

    public CompletableFuture<String> render(Object key) {
        if (renderCache != null) {
            try {
                return renderCache.get(key);
            } catch (ExecutionException ex) {
                //this should never happen
                throw new RuntimeException(ex);
            }
        }
        return renderers.submit(key);
    }

    public void start() {
        if (renderers != null) {
            throw new IllegalStateException("already started");
        }

        renderers = new UniversalRenderingEnginesPool(conf, gson);
        renderers.start();
    }

    @Override
    public void close() throws IOException {
        try {
            renderers.stop();
            renderers = null;
            if (this.renderCache != null) {
                //reset the cache
                this.useCache();
            }

        } catch (InterruptedException ex) {
            throw new IOException("interrupted while stopping render engine", ex);
        }
    }

    public void startLiveReload() throws IOException {
        if (liveReloader != null) {
            throw new IllegalStateException("live reload already started");
        }

        Path bundle = conf.getUniversalServerBundlePath().toPath();

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
