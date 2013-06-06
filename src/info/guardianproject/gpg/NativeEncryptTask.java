
package info.guardianproject.gpg;

import android.util.Log;

import info.guardianproject.pgpbenchmark.BenchmarkInput;
import info.guardianproject.pgpbenchmark.PGPAsyncTask;
import info.guardianproject.pgpbenchmark.Progress;
import info.guardianproject.pgpbenchmark.ProgressDialogUpdater;

import java.util.concurrent.TimeUnit;

public class NativeEncryptTask extends PGPAsyncTask {
    private final static String TAG = "NativeEncryptTask";

    public NativeEncryptTask(ProgressDialogUpdater updater) {
        super(updater);
    }

    public NativeEncryptTask() {
    }

    @Override
    protected void onPreExecute() {
        updater.onPre(null);
    }

    @Override
    protected Void doInBackground(BenchmarkInput... params) {
        BenchmarkInput data = params[0];

        String passphrase = "123";
        GPGCli.getInstance().importKey(data.mRecipientKeyFile.getAbsolutePath(), passphrase);
        GPGCli.getInstance().importKey(data.mSenderKeyFile.getAbsolutePath(),passphrase);
        for (GPGKey key : GPGCli.getInstance().getPublicKeys()) {
            Log.d(TAG, " pubkey " + key.getUserIds().get(0).getEmail());
        }
        for (GPGKey key : GPGCli.getInstance().getSecretKeys()) {
            Log.d(TAG, " seckey " + key.getUserIds().get(0).getEmail());
        }

        startTime = System.nanoTime();
        GPGCli.getInstance().encryptAndSign("randy@example.com", "sandra@example.com", passphrase,
                data.mTestFile, data.mOutFile);
        endTime = System.nanoTime();

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
        Log.d(TAG, values[0].current + "/" + values[0].total + " " + values[0].message);
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
