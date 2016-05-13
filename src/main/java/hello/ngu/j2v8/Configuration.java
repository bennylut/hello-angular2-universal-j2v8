/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

import com.google.gson.Gson;
import hello.ngu.j2v8.render.UniversalRenderConfiguration;
import hello.ngu.j2v8.util.IOUtil;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author bennyl
 */
public class Configuration {

    public static final String UNIVERSAL_SERVER_BUNDLE_PATH = "ng2-target/server/bundle.js";
    public static final String WEB_PUBLIC_PATH = "ng2-target/client";
    public static final int WEB_PORT = 3000;
    public static final int NUM_RENDERING_ENGINES = 4;
    public static final String INDEX_HTML_RESOURCE_PATH = "/client/index.html";
    public static final int CACHE_SIZE = 1000;

    public static UniversalRenderConfiguration createUniversalRenderConfiguration(Gson gson) throws IOException {
        return new UniversalRenderConfiguration()
                .useCache(CACHE_SIZE)
                .jsonRenderer(gson::toJson)
                .numEngines(NUM_RENDERING_ENGINES)
                .template(IOUtil.consumeTextResource(INDEX_HTML_RESOURCE_PATH))
                .universalServerBundlePath(UNIVERSAL_SERVER_BUNDLE_PATH);

    }
}
