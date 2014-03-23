package com.parrot.freeflight.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class StreamUtils {
    /**
     * Performs copy of one stream into another.
     *
     * @param is - input stream
     * @param os - output stream
     * @throws java.io.IOException
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int count = 0;

        while ((count = is.read(buffer)) != -1) {
            os.write(buffer, 0, count);
        }

        os.flush();
    }
}
