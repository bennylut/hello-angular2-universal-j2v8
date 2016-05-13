/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

import com.google.gson.Gson;
import static hello.ngu.j2v8.Configuration.*;
import hello.ngu.j2v8.render.UniversalRenderer;
import static hello.ngu.j2v8.util.IOUtil.consumeTextResource;
import java.io.File;
import java.io.IOException;
import static spark.Spark.*;

/**
 *
 * @author bennyl
 */
public class Server {

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        String index = consumeTextResource(INDEX_HTML_RESOURCE_PATH);
        UniversalRenderer renderer = new UniversalRenderer(
                new File(NG2_SERVER_BUNDLE_PATH),
                index,
                NUM_RENDERING_ENGINES);

        renderer.start();
        renderer.startLiveReload();
        renderer.useUrlCache();

        port(WEB_PORT);
        staticFiles.externalLocation(WEB_PUBLIC_PATH);

        get("/app/*", (req, res) -> {
            return renderer.render(req.pathInfo()).get(); //TODO: switch to async once spark will add support
        });

        get("/data.json", (req, res) -> {
            return new FakeData("This fake data came from the java server.");
        }, gson::toJson);

        redirect.get("/", "/app/");
    }

    private static final class FakeData {

        String data;

        public FakeData(String data) {
            this.data = data;
        }

    }
}
