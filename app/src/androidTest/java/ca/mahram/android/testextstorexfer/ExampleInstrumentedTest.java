package ca.mahram.android.testextstorexfer;

import android.Manifest;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.airg.android.logging.Logger;
import com.airg.android.logging.TaggedLogger;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final TaggedLogger LOG = Logger.tag("TEST");
    private static final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private static final String[] assets = new String[]{
            "test1.jpg",
            "test2.png",
            "test3.jpg"
    };

    @BeforeClass
    public static void prepareAssets() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            grantPermissions ();

        final AssetManager am = InstrumentationRegistry.getContext().getAssets();
        LOG.d("Preparing test assets");

        try {
            for (final String asset : assets) {
                CopyUtil.copy(am.open(asset),
                        new File(downloads, asset),
                        (c, t) -> LOG.d("%d/%d", c, t));
            }
        } catch (IOException e) {
            LOG.e(e, "Failed to prepare test assets");
            Assert.fail("Prep failed");
        }
    }

    private static void grantPermissions() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + InstrumentationRegistry.getContext().getPackageName() + " " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + InstrumentationRegistry.getTargetContext().getPackageName() + " " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Test
    public void countFiles() {
        final Set<String> lookup = new HashSet<>();
        Collections.addAll(lookup, assets);

        Assert.assertEquals(assets.length, downloads.listFiles((dir, name) ->lookup.contains(name)).length);
    }
}
