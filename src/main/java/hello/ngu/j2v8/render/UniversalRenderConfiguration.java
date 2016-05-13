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
    private String indexTemplate;
    private int numEngines;

    public UniversalRenderConfiguration(File universalServerBundlePath, String indexTemplate, int numEngines) {
        this.universalServerBundlePath = universalServerBundlePath;
        this.indexTemplate = indexTemplate;
        this.numEngines = numEngines;
    }

    public String getIndexTemplate() {
        return indexTemplate;
    }

    public File getUniversalServerBundlePath() {
        return universalServerBundlePath;
    }

    public int getNumEngines() {
        return numEngines;
    }

}
