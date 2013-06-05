
package info.guardianproject.pgpbenchmark;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import java.io.File;

public abstract class TaskTest extends ActivityInstrumentationTestCase2<PGPBenchmark> {

    public TaskTest() {
        super("info.guardianproject.pgpbenchmark", PGPBenchmark.class);
    }


    protected static String TAG = "AsyncTaskTest";
    protected final static int TEST_SIZE_MB = 100;
    protected File mTestFile;
    protected File mOutFile;

    protected File mRecipientKeyFile;
    protected File mSenderKeyFile;

    protected boolean prepare(String type) {
        int megabytes = TEST_SIZE_MB;
        int size = megabytes * 1024 * 1024;
        mTestFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_"
                + megabytes + "M.dat");
        if (mTestFile.length() != size) {
            Log.e(TAG, "Testfile error: " + mTestFile.getAbsolutePath() + " size=" + size);
            return false;
        }

        mOutFile = new File(Environment.getExternalStorageDirectory() + "/test_" + megabytes
                + "M.dat."+type+".gpg");

        if (mOutFile.exists()) {
            mOutFile.delete();
        }

        mRecipientKeyFile = new File(Environment.getExternalStorageDirectory()
                + "/pgpbenchmark-recipient.pub.asc");
        if (mRecipientKeyFile.length() == 0) {
            Log.e(TAG, "Public key " + mRecipientKeyFile.getAbsolutePath() + " does not exist");
            return false;
        }
        mSenderKeyFile = new File(Environment.getExternalStorageDirectory()
                + "/pgpbenchmark-sender.asc");
        if (mSenderKeyFile.length() == 0) {
            Log.e(TAG, "Private key " + mSenderKeyFile.getAbsolutePath() + " does not exist");
            return false;
        }
        return true;
    }

}
