/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

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

    public static UniversalRenderConfiguration createUniversalRenderConfiguration() throws IOException {
        return new UniversalRenderConfiguration(
                new File(UNIVERSAL_SERVER_BUNDLE_PATH), 
                IOUtil.consumeTextResource(INDEX_HTML_RESOURCE_PATH),
                NUM_RENDERING_ENGINES);
    }
}
