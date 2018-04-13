package ca.mahram.android.testextstorexfer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.io.File;

public final class ClickConfirmDialog extends AbstractFileDialog {

    static ClickConfirmDialog confirm (@NonNull final File file, final FragmentManager fragmentManager) {
        final ClickConfirmDialog dialog = new ClickConfirmDialog();
        dialog.setArguments(getBaseArgsBundle(file));
        dialog.show(fragmentManager, "confirm");
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        if (null == context) throw new NullPointerException("context");

        setCancelable(true);

        final String message = getString(R.string.confirm_x_copy, file.getName());

        return new AlertDialog.Builder(context)
                .setTitle(R.string.confirm_selection)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, this::onClick)
                .setNegativeButton(android.R.string.cancel, this::onClick)
                .create();
    }

    private void onClick(final DialogInterface dialogInterface, final int which) {
        if (which != DialogInterface.BUTTON_POSITIVE) return;

        final MainActivity activity = (MainActivity) getActivity();

        if (activity == null) return;

        activity.selectionConfirmed (file);
    }
}
