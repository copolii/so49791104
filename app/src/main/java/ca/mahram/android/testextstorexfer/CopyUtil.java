package ca.mahram.android.testextstorexfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class CopyUtil {
    private static final byte[] buffer = new byte[4096];

    public static void copy(final File src, final File dst, final ProgressCallback callback) throws IOException {
        try (final InputStream fis = new FileInputStream(src)) {
            copy(fis, dst, callback);
        }
    }

    public static void copy(final InputStream in, final File dst, final ProgressCallback callback) throws IOException {
        try (final OutputStream fos = new FileOutputStream(dst)) {
            copy(in, fos, callback);
        }
    }

    public static void copy(final InputStream in, final OutputStream out, final ProgressCallback callback) throws IOException {
        long total = in.available();
        long copied = 0;

        callback.progress(copied, total);

        int read;

        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
            copied += read;
            callback.progress(copied, total);
        }
    }

    public interface ProgressCallback {
        void progress(final long written, final long total);
    }
}
