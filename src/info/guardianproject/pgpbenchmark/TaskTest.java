
package info.guardianproject.pgpbenchmark;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class TaskTest extends ActivityInstrumentationTestCase2<PGPBenchmarkActivity> {

    public TaskTest() {
        super("info.guardianproject.pgpbenchmark", PGPBenchmarkActivity.class);
    }

    protected static String TAG = "TaskTest";
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
                + "M.dat." + type + ".gpg");

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

    protected int taskTest(final String className, final BenchmarkInput input) {
        final CountDownLatch signal = new CountDownLatch(1);
        try {

            runTestOnUiThread(new ProgressRunnable() {

                @Override
                public void run() {
                    try {
                        PGPAsyncTask task = (PGPAsyncTask) Class.forName(className).newInstance();
                        task.setUpdater(this);
                        task.execute(input);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void setProgress(String message, int current, int total) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void setProgress(int resourceId, int current, int total) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void setProgress(int current, int total) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onPre(Progress progress) {
                    Log.d(TAG, "test starting");

                }

                @Override
                public void onUpdate(Progress progress) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onComplete(Progress progress) {
                    Log.d(TAG, "test complete: " + progress.elapsed);
                    signal.countDown();

                }
            });

            final int timeout = 5;
            if (!signal.await(timeout, TimeUnit.MINUTES)) {
                String msg = "Test timedout after" + timeout + " minutes";
                Log.d(TAG, msg);
                return -1;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return -2;
        }
        return 0;
    }

}
