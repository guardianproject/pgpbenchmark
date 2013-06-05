package info.guardianproject.pgpbenchmark.bc;

import info.guardianproject.pgpbenchmark.utils.PositionAwareInputStream;

import java.io.InputStream;


public class InputData {
    private PositionAwareInputStream mInputStream;
    private long mSize;

    public InputData(InputStream inputStream, long size) {
        mInputStream = new PositionAwareInputStream(inputStream);
        mSize = size;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public long getSize() {
        return mSize;
    }

    public long getStreamPosition() {
        return mInputStream.position();
    }
}