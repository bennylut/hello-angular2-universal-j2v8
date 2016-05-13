/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hello.ngu.j2v8.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author bennyl
 */
public class IOUtil {

    public static String consume(Reader r) throws IOException {
        char[] chunk = new char[(1 << 10) * 6];
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = r.read(chunk)) >= 0) {
            sb.append(chunk, 0, read);
        }

        return sb.toString();
    }

    public static String consumeTextResource(String absolutePath) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(IOUtil.class.getResourceAsStream(absolutePath))) {
            return consume(reader);
        }
    }
}
