/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

import com.google.gson.Gson;
import static hello.ngu.j2v8.Configuration.*;
import hello.ngu.j2v8.render.UniversalRenderer;
import java.io.IOException;
import static spark.Spark.*;

/**
 *
 * @author bennyl
 */
public class Server {

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        UniversalRenderer renderer = new UniversalRenderer(createUniversalRenderConfiguration(gson));

        renderer.start();
        renderer.startLiveReload();

        port(WEB_PORT);
        staticFiles.externalLocation(WEB_PUBLIC_PATH);

        get("/app/*", (req, res) -> {
            return renderer.render(new ExampleRequestInfo(req.pathInfo())).get(); //TODO: switch to async once spark will add support
        });

        get("/data.json", (req, res) -> {
            return new FakeData("This fake data came from the java server.");
        }, gson::toJson);

        redirect.get("/", "/app/");

    }

    private static final class FakeData {

        String data;

        FakeData(String data) {
            this.data = data;
        }

    }

    /**
     * this object will get cached so it must implement the {@link #hashCode()}
     * and {@link #equals(java.lang.Object)} methods.
     */
    public static final class ExampleRequestInfo {

        public String url;

        ExampleRequestInfo(String url) {
            this.url = url;
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (!(obj instanceof ExampleRequestInfo))) {
                return false;
            }
            return url.equals(((ExampleRequestInfo) obj).url);
        }

    }
}
