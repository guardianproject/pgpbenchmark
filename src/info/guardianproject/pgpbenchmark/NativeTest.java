
package info.guardianproject.pgpbenchmark;

import android.os.Environment;

import java.io.File;

public class NativeTest extends TaskTest {
    private final static String TAG = "NativeTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!prepare("native"))
            throw new Exception("test preparation failed!");
    }

    @Override
    protected void tearDown() throws Exception {
        if (mOutFile.exists()) {
            mOutFile.delete();
        }
    }

    public void testEncryptAndSign() {
        final BenchmarkInput input = new BenchmarkInput();
        input.mTestSizeMegs = 100;
        input.mTestFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/test_" + TEST_SIZE_MB + "M.dat");
        input.mOutFile = mOutFile;
        input.mRecipientKeyFile = mRecipientKeyFile;
        input.mSenderKeyFile = mSenderKeyFile;

        final int res = taskTest("info.guardianproject.gpg.NativeEncryptTask", input);
        if (res < 0) {
            fail();
        }
    }

}
