package info.guardianproject.pgpbenchmark;

import android.util.Log;

import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPLiteralDataGenerator;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;


/**
 * This is a collection of random functions taken from around the web to make this benchmark app work
 * Don't use this blindly. It's probably broken.
 *
 * sources:
 * OpenPGPKeyChain - https://github.com/dschuermann/openpgp-keychain/
 * this blog - https://subversivebytes.wordpress.com/2012/12/07/pgp-cryptography-with-the-legion-of-the-bouncy-castle-part-2/
 * this forum - http://www.coderanch.com/t/536248/java/java/PGP-encrypting-asc-public-key
 *
 */
public class BouncyCastleHelper {
    public static final String BOUNCY_CASTLE_PROVIDER_NAME = "SC";
    public static final String TAG = "BouncyCastleHelper";

    static {
        // register spongy castle provider
        Security.addProvider(new BouncyCastleProvider());
    }
    PGPPublicKey key = null;

    public static PGPPublicKey importPublicKeyForEncryption(InputStream in) throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
        PGPPublicKey key = null;
        Iterator rIt = pgpPub.getKeyRings();
        while (key == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            while (key == null && kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();
                if (k.isEncryptionKey()) {
                    key = k;
                }
            }
        }
        if (key == null) {
            throw new IllegalArgumentException("Can't find encryption key in key ring.");
        }
        return key;
    }

    public static PGPSecretKeyRing importSecretKeyRing(InputStream input) throws IOException, PGPException
    {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input));
        Iterator<PGPSecretKeyRing> iter = pgpSec.getKeyRings();
        PGPSecretKey secKey = null;

        while (iter.hasNext()) {
            PGPSecretKeyRing keyRing = iter.next();
            return keyRing;
        }
        return null;
    }

    public static PGPSecretKey importSecretKeyForSigning(PGPSecretKeyRing keyRing) throws PGPException
    {
        PGPSecretKey secKey = null;
        Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();
        while (keyIter.hasNext() && secKey == null) {
            PGPSecretKey key = keyIter.next();
            if (key.isSigningKey()) {
                secKey = key;
                break;
            }
        }

        if(secKey != null) {
            return secKey;
        }
        else {
            throw new IllegalArgumentException("Can't find signing key in key ring.");
        }
    }

    /**
     * <p>Return the first suitable key for encryption in the key ring
     * collection. For this case we only expect there to be one key
     * available for signing.</p>
     *
     * @param input - the input stream of the key PGP Key Ring
     * @return the first suitable PGP Secret Key found for signing
     * @throws IOException
     * @throws PGPException
     */
    public static PGPSecretKey importSecretKeyForSigning(InputStream input) throws IOException, PGPException
    {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input));
        Iterator<PGPSecretKeyRing> iter = pgpSec.getKeyRings();
        PGPSecretKey secKey = null;

        while (iter.hasNext() && secKey == null) {
            PGPSecretKeyRing keyRing = iter.next();
            Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();

            while (keyIter.hasNext()) {
                PGPSecretKey key = keyIter.next();
                if (key.isSigningKey()) {
                    secKey = key;
                    break;
                }
            }
        }

        if(secKey != null) {
            return secKey;
        }
        else {
            throw new IllegalArgumentException("Can't find signing key in key ring.");
        }
    }

    /**
     * Encrypt and Sign data
     *
     * @param context
     * @param progress
     * @param data
     * @param outStream
     * @param useAsciiArmor
     * @param compression
     * @param encryptionKeyIds
     * @param symmetricEncryptionAlgorithm
     * @param encryptionPassphrase
     * @param signatureKeyId
     * @param signatureHashAlgorithm
     * @param signatureForceV3
     * @param signaturePassphrase
     * @throws IOException
     * @throws PgpGeneralException
     * @throws PGPException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    public static void encryptAndSign(ProgressDialogUpdater progress, InputData data, OutputStream outStream, PGPPublicKey recipient, String signingUserId, PGPSecretKey signingKey, char[] passphrase) throws IOException, PGPException,
            NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
        OutputStream out = null;

        PBESecretKeyDecryptor keyDecryptor = new JcePBESecretKeyDecryptorBuilder().setProvider(
                BOUNCY_CASTLE_PROVIDER_NAME).build(passphrase);
        PGPPrivateKey signaturePrivateKey = signingKey.extractPrivateKey(keyDecryptor);

        OutputStream encryptOut = null;
        out = outStream;

        if( recipient == null ) {
            Log.e(TAG, "recipient key null");
            return;
        }

        if( signaturePrivateKey == null ) {
            Log.e(TAG, "signaturePrivateKey key null");
            return;
        }

        if( signingKey == null ) {
            Log.e(TAG, "signingKey key null");
            return;
        }

        updateProgress(progress, "Preparing streams", 5, 100);
        // encrypt and compress input file content
        JcePGPDataEncryptorBuilder encryptorBuilder = new JcePGPDataEncryptorBuilder(
                PGPEncryptedData.AES_256).setProvider(BOUNCY_CASTLE_PROVIDER_NAME)
                .setWithIntegrityPacket(true);

        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(encryptorBuilder);


        JcePublicKeyKeyEncryptionMethodGenerator pubKeyEncryptionGenerator = new JcePublicKeyKeyEncryptionMethodGenerator(
                recipient);
        cPk.addMethod(pubKeyEncryptionGenerator);

        encryptOut = cPk.open(out, new byte[1 << 16]);

        PGPSignatureGenerator signatureGenerator = null;

        updateProgress(progress, "preparing signature", 10, 100);
        // content signer based on signing key algorithm and choosen hash algorithm
        JcaPGPContentSignerBuilder contentSignerBuilder = new JcaPGPContentSignerBuilder(
                signingKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256)
                .setProvider(BOUNCY_CASTLE_PROVIDER_NAME);

        signatureGenerator = new PGPSignatureGenerator(contentSignerBuilder);
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, signaturePrivateKey);

        String userId = signingUserId;
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
        spGen.setSignerUserID(false, userId);
        signatureGenerator.setHashedSubpackets(spGen.generate());

        BCPGOutputStream bcpgOut = null;
        bcpgOut = new BCPGOutputStream(encryptOut);
        signatureGenerator.generateOnePassVersion(false).encode(bcpgOut);

        PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();
        // file name not needed, so empty string
        OutputStream pOut = literalGen.open(bcpgOut, PGPLiteralData.BINARY, "", new Date(),
                new byte[1 << 16]);
        updateProgress(progress, "Encrypting...", 20, 100);
        long done = 0;
        int n = 0;
        byte[] buffer = new byte[1 << 16];
        InputStream in = data.getInputStream();
        while ((n = in.read(buffer)) > 0) {
            pOut.write(buffer, 0, n);
            signatureGenerator.update(buffer, 0, n);
            done += n;
            if (data.getSize() != 0) {
                updateProgress(progress, "", (int) (20 + (95 - 20) * done / data.getSize()), 100);
            }
        }

        literalGen.close();

        updateProgress(progress, "Signing...", 95, 100);
        signatureGenerator.generate().encode(pOut);
        encryptOut.close();
        updateProgress(progress, "Done!", 100, 100);
    }

    public static String getMainUserId(PGPSecretKey key) {
        for (String userId : new IterableIterator<String>(key.getUserIDs())) {
            return userId;
        }
        return null;
    }
    public static PGPSecretKey getMasterKey(PGPSecretKeyRing keyRing) {
        if (keyRing == null) {
            return null;
        }
        for (PGPSecretKey key : new IterableIterator<PGPSecretKey>(keyRing.getSecretKeys())) {
            if (key.isMasterKey()) {
                return key;
            }
        }

        return null;
    }

    public static void updateProgress(ProgressDialogUpdater progress, String message, int current,
            int total) {
        if (progress != null) {
            progress.setProgress(message, current, total);
        }
    }

    public static void updateProgress(ProgressDialogUpdater progress, int current, int total) {
        if (progress != null) {
            progress.setProgress(current, total);
        }
    }


}
