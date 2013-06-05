
package info.guardianproject.pgpbenchmark;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import info.guardianproject.gpg.NativeEncryptTask;
import info.guardianproject.pgpbenchmark.bc.JavaEncryptTask;

import java.io.File;

public class PGPBenchmarkActivity extends Activity implements ProgressDialogUpdater {
    private final static String TAG = "PGPBenchmark";
    private final static int TEST_SIZE_MB = 100;
    TextView mJavaText;
    TextView mNativeText;
    TextView mActiveText;
    Button mJavaButton;
    Button mNativeButton;
    ProgressBar mProgressBar;

    File mTestFile;
    File mJavaOutFile;
    File mNativeOutFile;

    File mRecipientKeyFile;
    File mSenderKeyFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_pgpbenchmark);

        mJavaText = (TextView) findViewById(R.id.javaView);
        mNativeText = (TextView) findViewById(R.id.nativeView);
        mJavaButton = (Button) findViewById(R.id.javaButton);
        mNativeButton = (Button) findViewById(R.id.nativeButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mJavaButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                javaEncrypt();
            }
        });

        mNativeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                nativeEncrypt();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pgpbenchmark, menu);
        return true;
    }

    private void javaEncrypt() {

        if (!prepare()) {
            Log.d(TAG, "javaEncrypt: prepare failed");
            return;
        }

        mActiveText = mJavaText;
        BenchmarkInput input = new BenchmarkInput();
        input.mTestFile = mTestFile;
        input.mOutFile = mJavaOutFile;
        input.mRecipientKeyFile = mRecipientKeyFile;
        input.mSenderKeyFile = mSenderKeyFile;
        input.mTestSizeMegs = TEST_SIZE_MB;

        new JavaEncryptTask(this).execute(input);
    }

    private void nativeEncrypt() {
        if (!prepare()) {
            Log.d(TAG, "nativeEncrypt: prepare failed");
            return;
        }

        mActiveText = mNativeText;
        BenchmarkInput input = new BenchmarkInput();
        input.mTestFile = mTestFile;
        input.mOutFile = mNativeOutFile;
        input.mRecipientKeyFile = mRecipientKeyFile;
        input.mSenderKeyFile = mSenderKeyFile;
        input.mTestSizeMegs = TEST_SIZE_MB;
        new NativeEncryptTask(this).execute(input);
    }

    private void enableButtons(boolean enabled) {
        mJavaButton.setEnabled(enabled);
        mNativeButton.setEnabled(enabled);
    }

    private boolean prepare() {
        int megabytes = TEST_SIZE_MB;
        int size = megabytes * 1024 * 1024;
        mTestFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_"
                + megabytes + "M.dat");
        if (mTestFile.length() != size) {
            Log.e(TAG, "Testfile error: " + mTestFile.getAbsolutePath() + " size=" + size);
            return false;
        }

        mJavaOutFile = new File(Environment.getExternalStorageDirectory() + "/test_" + megabytes
                + "M.dat.java.gpg");
        mNativeOutFile = new File(Environment.getExternalStorageDirectory() + "/test_" + megabytes
                + "M.dat.native.gpg");

        if (mNativeOutFile.exists()) {
            mNativeOutFile.delete();
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

    private void appendLog(TextView view, String msg) {
        if (msg.length() == 0)
            return;

        String status = view.getText().toString();
        status += msg + "\n";
        view.setText(status);
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
        enableButtons(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);
        PGPBenchmarkActivity.this.setProgressBarIndeterminateVisibility(true);
        PGPBenchmarkActivity.this.setProgressBarIndeterminate(true);
    }

    @Override
    public void onUpdate(Progress progress) {
        mProgressBar.setMax(progress.total);
        mProgressBar.setProgress(progress.current);
        appendLog(mActiveText, progress.message);
    }

    @Override
    public void onComplete(Progress progress) {
        mProgressBar.setVisibility(View.INVISIBLE);
        PGPBenchmarkActivity.this.setProgressBarIndeterminateVisibility(false);

        appendLog(mActiveText, "Complete. " + progress.elapsed + " s elapsed");
        enableButtons(true);
    }

}
