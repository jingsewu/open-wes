package org.openwes.common.utils.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Slf4j
public class CompressUtils {

    private CompressUtils() {

    }

    public static byte[] compress(String value) {

        if (value == null) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (DeflaterOutputStream deflaterOutputStream =
                 new DeflaterOutputStream(outputStream, new Deflater(Deflater.DEFAULT_COMPRESSION))) {
            byte[] input = value.getBytes(StandardCharsets.UTF_8);

            deflaterOutputStream.write(input);
            deflaterOutputStream.finish();

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Unable to compress: {} , error:", value, e);
            return new byte[0];
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("stream close error :", e);
            }
        }
    }

    public static String decompress(byte[] compressedData) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Unable to decompress :", e);
            return null;
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                log.error("stream close error :", e);
            }
        }
    }
}
