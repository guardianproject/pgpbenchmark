package info.guardianproject.pgpbenchmark;

import android.os.Environment;
import android.util.Log;

import info.guardianproject.pgpbenchmark.bc.JavaEncryptTask;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BouncyCastleTest extends TaskTest {

    private final static String TAG = "BouncyCastleTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if(!prepare("java"))
            throw new Exception("test preparation failed!");
    }

    @Override
    protected void tearDown() throws Exception {
        if(mOutFile.exists()) {
            mOutFile.delete();
        }
    }

    public void testEncryptAndSign() {
        final CountDownLatch signal = new CountDownLatch(1);

        final BenchmarkInput input = new BenchmarkInput();
        input.mTestSizeMegs = 100;
        input.mTestFile = new File ( Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_" + TEST_SIZE_MB + "M.dat");
        input.mOutFile = mOutFile;
        input.mRecipientKeyFile = mRecipientKeyFile;
        input.mSenderKeyFile = mSenderKeyFile;

        try {
            runTestOnUiThread(new ProgressRunnable() {

                @Override
                public void run() {
                    JavaEncryptTask task = new JavaEncryptTask(this);
                    task.execute(input);
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
            if( !signal.await(timeout, TimeUnit.MINUTES) ) {
                String msg = "Test timedout after" + timeout + " minutes";
                fail(msg);
                Log.d(TAG, msg);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            fail("exception caught");
        }
    }

}
