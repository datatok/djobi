package io.datatok.djobi.utils.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

public class CompressionUtils {

    static public byte[] deflate(final String inStr) throws IOException {
        final byte[] input = inStr.getBytes(StandardCharsets.UTF_8);
        final byte[] buffer = new byte[512];
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();

        while(!deflater.finished()) {
            int compressedDataLength = deflater.deflate(buffer);

            bos.write(buffer, 0, compressedDataLength);
        }

        deflater.end();

        bos.close();

        return bos.toByteArray();
    }

}
