
package info.guardianproject.pgpbenchmark;

import android.content.Context;
import android.os.AsyncTask;

public abstract class PGPAsyncTask extends AsyncTask<BenchmarkInput, Progress, Void> implements
        ProgressDialogUpdater {
    protected ProgressDialogUpdater updater;
    protected Context context;
    protected long startTime;
    protected long endTime;

    public PGPAsyncTask() {

    }

    public PGPAsyncTask(ProgressDialogUpdater updater, Context c) {
        this.updater = updater;
        this.context = c;
    }

    public void setUpdater(ProgressDialogUpdater updater) {
        this.updater = updater;
    }

    public void setContext(Context c) {
        this.context = c;
    }
}
