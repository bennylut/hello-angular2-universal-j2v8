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

    private final Object key;
    private final CompletableFuture<String> result;

    public UniversalRenderingRequest(Object url) {
        this.key = url;
        this.result = new CompletableFuture<>();
    }

    public CompletableFuture<String> getResult() {
        return result;
    }

    public Object getKey() {
        return key;
    }

}
