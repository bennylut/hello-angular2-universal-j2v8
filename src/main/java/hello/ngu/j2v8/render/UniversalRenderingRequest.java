/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author bennyl
 */
public class UniversalRenderingRequest {

    private final String url;
    private final CompletableFuture<String> result;

    public UniversalRenderingRequest(String url) {
        this.url = url;
        this.result = new CompletableFuture<>();
    }

    public CompletableFuture<String> getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

}
