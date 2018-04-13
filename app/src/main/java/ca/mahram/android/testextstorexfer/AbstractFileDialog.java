package ca.mahram.android.testextstorexfer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.io.File;

public abstract class AbstractFileDialog extends DialogFragment {
    private static final String ARG_FILE = "file";
    protected File file;

    protected static Bundle getBaseArgsBundle(final File file) {
        final Bundle args = new Bundle();
        args.putSerializable(ARG_FILE, file);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgs();
    }

    protected void parseArgs() {
        final Bundle args = getArguments();

        if (null == args) throw new NullPointerException("args");

        file = (File) args.getSerializable(ARG_FILE);

        if (null == file) throw new NullPointerException("file");
    }
}
