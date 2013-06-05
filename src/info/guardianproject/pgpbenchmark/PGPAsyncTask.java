
package info.guardianproject.pgpbenchmark;

import android.os.AsyncTask;

public abstract class PGPAsyncTask extends AsyncTask<BenchmarkInput, Progress, Void> implements
        ProgressDialogUpdater {
    protected ProgressDialogUpdater updater;
    protected long startTime;
    protected long endTime;

    public PGPAsyncTask() {

    }

    public PGPAsyncTask(ProgressDialogUpdater updater) {
        this.updater = updater;
    }

    public void setUpdater(ProgressDialogUpdater updater) {
        this.updater = updater;
    }
}
