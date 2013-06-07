
package info.guardianproject.pgpbenchmark.bc;

import android.content.Context;
import android.util.Log;

import info.guardianproject.pgpbenchmark.BenchmarkInput;
import info.guardianproject.pgpbenchmark.PGPAsyncTask;
import info.guardianproject.pgpbenchmark.Progress;
import info.guardianproject.pgpbenchmark.ProgressDialogUpdater;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.concurrent.TimeUnit;

public class JavaEncryptTask extends PGPAsyncTask {
    private final static String TAG = "JavaEncryptTask";

    public JavaEncryptTask() {
    }

    public JavaEncryptTask(ProgressDialogUpdater updater, Context c) {
        super(updater, c);
    }

    @Override
    protected void onPreExecute() {
        updater.onPre(null);
    }

    @Override
    protected Void doInBackground(BenchmarkInput... params) {
        BenchmarkInput data = params[0];
        try {
            FileInputStream inStream = new FileInputStream(data.mTestFile);
            FileOutputStream outStream = new FileOutputStream(data.mOutFile);
            InputData inputData = new InputData(inStream, data.mTestFile.length());

            FileInputStream pubKeyIn = new FileInputStream(data.mRecipientKeyFile);
            FileInputStream senderKeyIn = new FileInputStream(data.mSenderKeyFile);

            PGPPublicKey recipient = BouncyCastleHelper.importPublicKeyForEncryption(pubKeyIn);
            publishProgress(new Progress("Encrypting+signing a " + data.mTestSizeMegs
                    + " megabyte file", 0, 100));
            publishProgress(new Progress("Extracting signature key", 0, 100));
            PGPSecretKeyRing senderKeyRing = BouncyCastleHelper.importSecretKeyRing(senderKeyIn);
            PGPSecretKey signer = BouncyCastleHelper.importSecretKeyForSigning(senderKeyRing);

            String signingUserId = BouncyCastleHelper.getMainUserId(BouncyCastleHelper
                    .getMasterKey(senderKeyRing));
            char[] passphrase = "123".toCharArray();

            startTime = System.nanoTime();
            BouncyCastleHelper.encryptAndSign(this, inputData, outStream, recipient, signingUserId,
                    signer, passphrase);
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
        Progress p = new Progress("", 100, 100);
        final long elapsed = (endTime - startTime);
        final long seconds = TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
        Log.d(TAG, "done after " + seconds + " seconds");
        p.elapsed = seconds;
        updater.onComplete(p);
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        updater.onUpdate(values[0]);

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

    @Override
    public void onPre(Progress progress) {
        // we dont use this one
    }

    @Override
    public void onUpdate(Progress progress) {
        // we dont use this one
    }

    @Override
    public void onComplete(Progress progress) {
        // we dont use this one
    }

}
