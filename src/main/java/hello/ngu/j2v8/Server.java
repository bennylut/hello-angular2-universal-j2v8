/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8;

import com.google.gson.Gson;
import static hello.ngu.j2v8.Configuration.*;
import hello.ngu.j2v8.render.UniversalRenderer;
import java.io.File;
import java.io.IOException;
import static spark.Spark.*;

/**
 *
 * @author bennyl
 */
public class Server {

    public static void main(String[] args) throws IOException {
        port(WEB_PORT);
        Gson gson = new Gson();
        UniversalRenderer renderer = new UniversalRenderer(new File(NG2_SERVER_BUNDLE_PATH), 4);
        renderer.start();
        renderer.startLiveReload();
        staticFiles.externalLocation(WEB_PUBLIC_PATH);

        get("/app/*", (req, res) -> {
            return renderer.render(req.pathInfo()).get();
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
