package info.guardianproject.gpg;


public class GPGKeyPair {

    private GPGKey publicKey;
    private GPGKey secretKey;

    public GPGKeyPair(GPGKey publicKey, GPGKey secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }

    public GPGKey getPublicKey() {
        return this.publicKey;
    }

    public GPGKey getSecretKey() {
        return this.secretKey;
    }
}
