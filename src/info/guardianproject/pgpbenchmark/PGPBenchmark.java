
package info.guardianproject.pgpbenchmark;

import android.app.Activity;
import android.os.AsyncTask;
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

import info.guardianproject.gpg.GPGCli;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.concurrent.TimeUnit;

public class PGPBenchmark extends Activity {
    private final static String TAG = "PGPBenchmark";
    private final static int TEST_SIZE_MB = 100;
    TextView mJavaText;
    TextView mNativeText;
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
        mJavaButton= (Button) findViewById(R.id.javaButton);
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

        if( !prepare()) {
            Log.d(TAG, "javaEncrypt: prepare failed");
            return;
        }

        new JavaEncryptTask().execute();
    }
    private void nativeEncrypt() {
        Log.d(TAG, "nativeEncrypt: NYI");
    }

    private boolean prepare() {
        int megabytes = TEST_SIZE_MB;
        int size = megabytes * 1024* 1024;
        mTestFile = new File ( Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_" + megabytes + "M.dat");
        if( mTestFile.length() != size) {
            Log.e(TAG, "Testfile error: " + mTestFile.getAbsolutePath() + " size=" + size);
            return false;
        }

        mJavaOutFile = new File ( Environment.getExternalStorageDirectory() + "/test_" + megabytes + "M.dat.java.gpg");
        mNativeOutFile = new File ( Environment.getExternalStorageDirectory() + "/test_" + megabytes + "M.dat.native.gpg");

        mRecipientKeyFile = new File( Environment.getExternalStorageDirectory() + "/pgpbenchmark-recipient.pub.asc" );
        if( mRecipientKeyFile.length() == 0 ) {
            Log.e(TAG, "Public key " + mRecipientKeyFile.getAbsolutePath() + " does not exist");
            return false;
        }
        mSenderKeyFile = new File( Environment.getExternalStorageDirectory() + "/pgpbenchmark-sender.asc" );
        if( mSenderKeyFile.length() == 0 ) {
            Log.e(TAG, "Private key " + mSenderKeyFile.getAbsolutePath() + " does not exist");
            return false;
        }
        return true;
    }

    private void appendLog(TextView view, String msg) {
        if( msg.length() == 0 )
            return;

        String status = view.getText().toString();
        status += msg +"\n";
        view.setText(status);
    }

    class Progress {

        Progress(String message, int current, int total) {
            this.current = current;
            this.total = total;
            this.message = message;
        }
        public int current;
        public int total;
        public String message;
    }

    class JavaEncryptTask extends AsyncTask<Void, Progress, Void> implements ProgressDialogUpdater {

        long startTime;
        long endTime;
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
            PGPBenchmark.this.setProgressBarIndeterminateVisibility(true);
            PGPBenchmark.this.setProgressBarIndeterminate(true);
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileInputStream inStream = new FileInputStream(mTestFile);
                FileOutputStream outStream = new FileOutputStream(mJavaOutFile);
                InputData inputData = new InputData(inStream, mTestFile.length());


                FileInputStream pubKeyIn = new FileInputStream( mRecipientKeyFile );
                FileInputStream senderKeyIn = new FileInputStream( mSenderKeyFile );

                PGPPublicKey recipient = BouncyCastleHelper.importPublicKeyForEncryption(pubKeyIn);
                publishProgress(new Progress("Encrypting+signing a " + TEST_SIZE_MB + " megabyte file", 0, 100));
                publishProgress(new Progress("Extracting signature key", 0, 100));
                PGPSecretKeyRing senderKeyRing = BouncyCastleHelper.importSecretKeyRing(senderKeyIn);
                PGPSecretKey signer = BouncyCastleHelper.importSecretKeyForSigning(senderKeyRing);

                String signingUserId = BouncyCastleHelper.getMainUserId(BouncyCastleHelper.getMasterKey(senderKeyRing));
                char[] passphrase = "test".toCharArray();

                startTime = System.nanoTime();
                BouncyCastleHelper.encryptAndSign(this, inputData, outStream, recipient, signingUserId, signer, passphrase);
                endTime = System.nanoTime();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SignatureException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PGPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

            mProgressBar.setVisibility(View.INVISIBLE);
            PGPBenchmark.this.setProgressBarIndeterminateVisibility(false);
            final long elapsed = (endTime-startTime);
            final long seconds = TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
            appendLog(mJavaText, "Complete. " + seconds + " s elapsed");
            Log.d(TAG, "JavaEncryptTask: done after " + seconds + " seconds");
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            mProgressBar.setMax(values[0].total);
            mProgressBar.setProgress(values[0].current);
            appendLog(mJavaText, values[0].message);
            Log.d(TAG, "JavaEncryptTask: " +values[0].current + "/" + values[0].total +" "+ values[0].message);
        }

        @Override
        public void setProgress(String message, int current, int total) {
            Progress p = new Progress(message, current, total);
            publishProgress(p);
        }
        @Override
        public void setProgress(int current, int total) {
            Progress p = new Progress("", current, total);
            publishProgress(p);
        }

        @Override
        public void setProgress(int resourceId, int current, int total) {
            // we dont use this one
        }

    }

    class NativeEncryptTask extends AsyncTask<Void, Progress, Void> implements ProgressDialogUpdater {
        long startTime;
        long endTime;
        @Override
        protected void onPreExecute() {
            enableButtons(false);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
            PGPBenchmark.this.setProgressBarIndeterminateVisibility(true);
            PGPBenchmark.this.setProgressBarIndeterminate(true);
        }
        @Override
        protected Void doInBackground(Void... params) {

            GPGCli.getInstance().importKey(mRecipientKeyFile.getAbsolutePath());
            GPGCli.getInstance().importKey(mSenderKeyFile.getAbsolutePath());

            startTime = System.nanoTime();
            GPGCli.getInstance().encryptAndSign("randy@example.com", "sandra@example.com", mTestFile, mNativeOutFile);
            endTime = System.nanoTime();

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

            mProgressBar.setVisibility(View.INVISIBLE);
            PGPBenchmark.this.setProgressBarIndeterminateVisibility(false);
            final long elapsed = (endTime-startTime);
            final long seconds = TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
            appendLog(mNativeText, "Complete. " + seconds + " s elapsed");
            Log.d(TAG, "NativeEncryptTask: done after " + seconds + " seconds");
            enableButtons(false);
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            mProgressBar.setMax(values[0].total);
            mProgressBar.setProgress(values[0].current);
            appendLog(mNativeText, values[0].message);
            Log.d(TAG, "NativeEncryptTask: " +values[0].current + "/" + values[0].total +" "+ values[0].message);
        }

        @Override
        public void setProgress(String message, int current, int total) {
            Progress p = new Progress(message, current, total);
            publishProgress(p);
        }
        @Override
        public void setProgress(int current, int total) {
            Progress p = new Progress("", current, total);
            publishProgress(p);
        }

        @Override
        public void setProgress(int resourceId, int current, int total) {
            // we dont use this one
        }
    }


}