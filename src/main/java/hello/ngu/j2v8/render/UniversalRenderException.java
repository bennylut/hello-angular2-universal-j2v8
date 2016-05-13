/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.render;

/**
 *
 * @author bennyl
 */
public class UniversalRenderException extends RuntimeException {

    public UniversalRenderException() {
    }

    public UniversalRenderException(String message) {
        super(message);
    }

    public UniversalRenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniversalRenderException(Throwable cause) {
        super(cause);
    }

}
