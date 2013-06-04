package info.guardianproject.gpg;

import java.io.File;
import java.util.List;

public interface GPGBinding {

    GPGKey getPublicKey(String keyId);
    GPGKey getSecretKey(String keyId);

    List<GPGKey> getPublicKeys();
    List<GPGKey> getSecretKeys();

    List<GPGKeyPair> getKeyPairs();

    void exportPublicKeyring(String destination);
    void exportSecretKeyring(String destination);

    void exportKey(String destination, String keyId);
    void importKey(String source);

    void signKey(String fingerprint, TrustLevel trustLevel);

    void pushToKeyServer(String server, String keyId);

    String exportAsciiArmoredKey(String keyId);
    void importAsciiArmoredKey(String armoredKey);

    void encryptAndSign(String recipientId, String signerId, File inputFile, File outputFile);
}