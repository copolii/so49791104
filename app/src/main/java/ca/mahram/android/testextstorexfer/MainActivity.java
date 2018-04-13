package ca.mahram.android.testextstorexfer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;
import com.airg.android.permission.PermissionHandlerClient;
import com.airg.android.permission.PermissionsHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final TaggedLogger LOG = Logger.tag("MAIN");
    private static final int REQUEST_STORAGE_PERM = 100;

    private PermissionsHandler permissionsHandler;

    @BindView(android.R.id.list)
    RecyclerView list;
    @BindView(R.id.emptyView)
    View emptyView;

    private FilesAdapter filesAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        filesAdapter = new FilesAdapter();
        list.setAdapter(filesAdapter);
        emptyView.setVisibility(filesAdapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

        permissionsHandler = PermissionsHandler.with(this, permissionHandlerClient);
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionsHandler.check(REQUEST_STORAGE_PERM, READ_EXTERNAL_STORAGE);
    }

    private void onItemClick(final View view) {
        final int position = list.getChildLayoutPosition(view);

        if (position < 0) return;

        final File file = filesAdapter.getFile(position);
        LOG.d("Confirming %s", file.getAbsolutePath());
        ClickConfirmDialog.confirm(file, getSupportFragmentManager());
    }

    private final PermissionHandlerClient permissionHandlerClient = new PermissionHandlerClient() {
        @Override
        public void onPermissionsGranted(int requestCode, Set<String> granted) {
            final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            filesAdapter.setFiles(downloads.listFiles());
        }

        @Override
        public void onPermissionDeclined(int requestCode, Set<String> denied) {
            finish();
        }

        @Override
        public void onPermissionRationaleDialogDimissed(int requestCode) {

        }

        @Override
        public AlertDialog showPermissionRationaleDialog(int requestCode, @NonNull Collection<String> permissions, @NonNull DialogInterface.OnClickListener listener) {
            return new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.permission_rationale_title)
                    .setMessage(R.string.permission_rationale_message)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener)
                    .show();
        }
    };

    public void selectionConfirmed(final File file) {
        LOG.d("Confirmed %s", file.getAbsolutePath());
        FileCopierDialog.copy(file, getSupportFragmentManager());
    }

    static class FileItem extends RecyclerView.ViewHolder {

        @BindView(R.id.fileName)
        TextView fileName;

        @BindView(R.id.fileSize)
        TextView fileSize;

        FileItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static int compareFiles(final File left, final File right) {
        if (left == right) return 0;
        if (left == null) return -1;
        if (right == null) return 1;

        final long diff = left.lastModified() - right.lastModified();
        if (diff > 0) return 1;
        if (diff < 0) return -1;
        return 0;
    }

    private class FilesAdapter extends RecyclerView.Adapter<FileItem> {
        private final List<File> files = new ArrayList<>();
        private final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        void setFiles(final File[] allFiles) {
            if (!files.isEmpty()) files.clear();

            Collections.addAll(files, allFiles);
            Collections.sort(files, MainActivity::compareFiles);
            notifyDataSetChanged();

            emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
        }

        @NonNull
        @Override
        public FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FileItem(inflater.inflate(R.layout.item_file, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FileItem holder, int position) {
            final File file = getFile(position);

            holder.fileName.setText(file.getName());
            holder.fileSize.setText(getFileSize(file));
            holder.itemView.setOnClickListener(MainActivity.this::onItemClick);
        }

        private String getFileSize(final File file) {
            return Formatter.formatFileSize(MainActivity.this, file.length());
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        File getFile(final int position) {
            return files.get(position);
        }
    }
}
