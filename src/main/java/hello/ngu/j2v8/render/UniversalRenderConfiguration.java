/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import java.io.File;

/**
 *
 * @author bennyl
 */
public class UniversalRenderConfiguration {

    private File universalServerBundlePath;
    private String template;
    private int numEngines = Runtime.getRuntime().availableProcessors() + 1;
    private JsonRenderer jsonRenderer;
    private int cacheSize = -1; //means that the cache is disabled

    public File universalServerBundlePath() {
        return this.universalServerBundlePath;
    }

    public UniversalRenderConfiguration universalServerBundlePath(File path) {
        this.universalServerBundlePath = path;
        return this;
    }

    public UniversalRenderConfiguration universalServerBundlePath(String path) {
        return universalServerBundlePath(new File(path));
    }

    public String template() {
        return this.template;
    }

    public UniversalRenderConfiguration template(String template) {
        this.template = template;
        return this;
    }

    public int numEngines() {
        return this.numEngines;
    }

    public UniversalRenderConfiguration numEngines(int numEngines) {
        if (numEngines <= 0) {
            throw new IllegalArgumentException("numEngines <= 0 : " + numEngines);
        }
        this.numEngines = numEngines;
        return this;
    }

    public JsonRenderer jsonRenderer() {
        return this.jsonRenderer;
    }

    public UniversalRenderConfiguration jsonRenderer(JsonRenderer renderer) {
        this.jsonRenderer = renderer;
        return this;
    }

    public UniversalRenderConfiguration useCache(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("cache size <= 0 : " + size);
        }
        this.cacheSize = size;
        return this;
    }

    public boolean usingCache() {
        return this.cacheSize > 0;
    }

    public UniversalRenderConfiguration disableCache() {
        this.cacheSize = -1;
        return this;
    }

    public int cacheSize() {
        return cacheSize;
    }

}
