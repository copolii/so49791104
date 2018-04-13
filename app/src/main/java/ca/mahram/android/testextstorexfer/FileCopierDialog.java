package ca.mahram.android.testextstorexfer;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class FileCopierDialog extends AbstractFileDialog {

    private final TaggedLogger LOG = Logger.tag("COPY");

    private static final int PROGRESS_MAX = 1000;

    private DialogBody body;
    private CopyTask copyTask = null;

    static FileCopierDialog copy(@NonNull final File file, final FragmentManager fragmentManager) {
        final FileCopierDialog dialog = new FileCopierDialog();
        dialog.setArguments(getBaseArgsBundle(file));
        dialog.show(fragmentManager, "copy");
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        copyTask = new CopyTask();
        copyTask.execute(file);
        LOG.d("Copy started");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);

        final Context context = getContext();
        if (context == null) throw new NullPointerException("context");

        body = new DialogBody(View.inflate(context, R.layout.dialog_copy, null));
        body.prompt.setText(getString(R.string.copying_x, file.getName()));
        body.progress.setMax(PROGRESS_MAX);
        LOG.d("Dialog up");
        return new AlertDialog.Builder(context)
                .setView(body.view)
                .create();
    }

    private synchronized void onCopyFinished() {
        LOG.d("Copy finished");
        copyTask = null;
        dismiss();
    }

    @Override
    public synchronized void onStop() {
        if (copyTask != null) {
            copyTask.cancel(true);
        }

        super.onStop();
    }

    private void updateProgress(final long copied, final long total) {
        if (body == null) return;

        final int progress = (int) (((double) copied / (double) total) * PROGRESS_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            body.progress.setProgress(progress, true);
        } else {
            body.progress.setProgress(progress);
        }

        final Context context = getContext();

        if (context == null) return;

        body.Status.setText(getString(R.string.ratio,
                Formatter.formatFileSize(context, copied),
                Formatter.formatFileSize(context, total)));
    }

    static class DialogBody {
        final View view;
        @BindView(R.id.prompt)
        TextView prompt;
        @BindView(R.id.status)
        TextView Status;
        @BindView(R.id.progress)
        ProgressBar progress;

        DialogBody(final View itemView) {
            view = itemView;
            ButterKnife.bind(this, view);
        }
    }

    private class CopyTask extends AsyncTask<File, Long, File> implements CopyUtil.ProgressCallback {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            onCopyFinished();
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            updateProgress(values[0], values[1]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            LOG.d("Cancelled.");
        }

        @Override
        protected File doInBackground(File... files) {
            final File src = files[0];

            final Context context = getContext();

            if (context == null) return null;

            final File dst = new File(context.getFilesDir(), "copy_" + src.getName());

            LOG.d("Writing to %s", dst.getAbsolutePath());

            try {
                CopyUtil.copy(src, dst, this);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return dst;
        }

        @Override
        public void progress(long written, long total) {
            publishProgress(written, total);
        }
    }
}
