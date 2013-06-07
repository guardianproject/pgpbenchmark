
package info.guardianproject.pgpbenchmark;

import android.app.Activity;
import android.os.Environment;
import android.test.SingleLaunchActivityTestCase;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class TaskTest extends SingleLaunchActivityTestCase<PGPBenchmarkActivity> {

    private static final String REPORT_PATH = "/sdcard/pgpbenchmark-report.txt";
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

    private void reportHeader() throws FileNotFoundException {
        File reportF = new File(REPORT_PATH);
        if( !reportF.exists() ) {
            PrintWriter pw = new PrintWriter( new FileOutputStream(REPORT_PATH));
            pw.println("Date,Type,Test,Size (MB),Elapsed (s)");
            pw.close();
        }
    }

    private String className(String fullyQualifiedClassName) {
        return fullyQualifiedClassName.substring(fullyQualifiedClassName.lastIndexOf(".")+1);
    }
    private void writeReport(String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        String line = timestamp +"," + msg;

        try {
            reportHeader();
            PrintWriter pw = new PrintWriter( new FileOutputStream(REPORT_PATH, true));
            pw.println(line);
            pw.close();
            Log.d(TAG, line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "writing report failed, report was:" + line);
        }
    }
    private void writeReportSuccess(String fullyQualifiedClassName, long elapsed, int size) {
        final String format = "%s,%s,%s,%s";
        final String line = String.format(format, className(fullyQualifiedClassName), "encrypt+sign", size, elapsed);
        writeReport(line);
    }
    private void writeReportFailure(String fullyQualifiedClassName, String msg) {
        final String format = "%s,%s,%s:,%s";
        final String line = String.format(format, className(fullyQualifiedClassName), "encrypt+sign", "failed", msg);
        writeReport(line);
    }

    protected int taskTest(final String fullyQualifiedClassName, final BenchmarkInput input) {
        final CountDownLatch signal = new CountDownLatch(1);
        final Activity context = getActivity();
        try {

            runTestOnUiThread(new ProgressRunnable() {

                @Override
                public void run() {
                    try {
                        PGPAsyncTask task = (PGPAsyncTask) Class.forName(fullyQualifiedClassName).newInstance();
                        task.setUpdater(this);
                        task.setContext(context);
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
                    writeReportSuccess(fullyQualifiedClassName, progress.elapsed, TEST_SIZE_MB);
                    signal.countDown();
                }
            });

            final int timeout = 5;
            if (!signal.await(timeout, TimeUnit.MINUTES)) {
                String msg = "Test timedout after" + timeout + " minutes";
                writeReportFailure(fullyQualifiedClassName, msg);
                Log.d(TAG, msg);
                return -1;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            writeReportFailure(fullyQualifiedClassName, e.getMessage());
            return -2;
        } finally {
            context.finish();
        }
        return 0;
    }

}
