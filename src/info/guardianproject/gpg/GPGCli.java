package info.guardianproject.gpg;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GPGCli implements GPGBinding {
    private final static String TAG = "GPGCli";

    private static GPGCli instance;

    private final String GPG_PATH = "gpg2";

    public static GPGCli getInstance() {
        if(instance == null) {
            instance = new GPGCli();
        }
        return instance;
    }

    private GPGCli() {
        Log.i("GPGCli", "GPGCli initialized");
    }

    @Override
    public GPGKey getPublicKey(String keyId) {
        String rawList = Exec(GPG_PATH, "--with-colons", "--with-fingerprint", "--list-keys", keyId);
        Log.i("GPGCli", "Got public key: " + keyId);

        Scanner scanner = new Scanner(rawList);
        GPGKey key = parseKey(scanner, "pub:.*");
        scanner.close();

        return key;
    }

    @Override
    public GPGKey getSecretKey(String keyId) {
        String rawList = Exec(GPG_PATH, "--with-colons", "--with-fingerprint", "--list-secret-keys", keyId);
        Log.i("GPGCli", "Got secret key: " + keyId);

        Scanner scanner = new Scanner(rawList);
        GPGKey key = parseKey(scanner, "sec:.*");
        scanner.close();

        return key;
    }

    @Override
    public ArrayList<GPGKey> getPublicKeys() {
        String rawList = Exec(GPG_PATH, "--with-colons", "--with-fingerprint", "--list-keys");
        Log.i("GPGCli", "Got public keys: " + rawList);

        ArrayList<GPGKey> keys = new ArrayList<GPGKey>();
        Scanner scanner = new Scanner(rawList);
        GPGKey key;
        while((key = parseKey(scanner, "pub:.*")) != null) {
            keys.add(key);
        }
        scanner.close();

        return keys;
    }

    @Override
    public ArrayList<GPGKey> getSecretKeys() {
        String rawList = Exec(GPG_PATH, "--with-colons", "--with-fingerprint", "--list-secret-keys");
        Log.i("GPGCli", "Got secret keys: " + rawList);

        ArrayList<GPGKey> keys = new ArrayList<GPGKey>();
        Scanner scanner = new Scanner(rawList);
        GPGKey key;
        while((key = parseKey(scanner, "sec:.*")) != null) {
            keys.add(key);
        }
        scanner.close();

        return keys;
    }

    @Override
    public ArrayList<GPGKeyPair> getKeyPairs() {
        ArrayList<GPGKeyPair> keyPairs = new ArrayList<GPGKeyPair>();

        ArrayList<GPGKey> secretKeys = this.getPublicKeys();
        for(GPGKey secretKey : secretKeys) {
            GPGKey publicKey = this.getPublicKey(secretKey.getKeyId());
            GPGKeyPair keyPair = new GPGKeyPair(publicKey, secretKey);
            keyPairs.add(keyPair);
        }

        return keyPairs;
    }

    private GPGKey parseKey(Scanner scanner, String keyDelimiter) {
        Pattern keyRegex = Pattern.compile(keyDelimiter);

        if(scanner.hasNextLine() && !scanner.hasNext(keyRegex)) {
            scanner.nextLine();
        }

        if(!scanner.hasNextLine()) {
            return null;
        }

        String line = scanner.nextLine();
        GPGRecord parentKey = GPGRecord.FromColonListingFactory(line);
        GPGKey key = new GPGKey(parentKey);
        if( key != null ) {
            while(scanner.hasNextLine() && !scanner.hasNext(keyRegex)) {
                String record_line = scanner.nextLine();
                GPGRecord subRecord = GPGRecord.FromColonListingFactory(record_line);
                if( subRecord.getType() != null ) {
                    switch(subRecord.getType()) {
                        case UserId:
                            key.addUserId(subRecord);
                            break;
                        case Fingerprint:
                            //Fingerprint records use the userId field as the fingerprint
                            key.setFingerprint(subRecord.getUserId());
                            break;
                        default:
                            key.addSubKey(subRecord);
                            break;
                    }
                } else {
                    Log.d(TAG, "got null record: " +record_line);
                }
            }
        } else {
            Log.d(TAG, "Null key: " + line);
        }

        return key;
    }

    @Override
    public void signKey(String fingerprint, TrustLevel trustLevel) {
        int gpgTrustLevel = 1;
        switch(trustLevel) {
            case New:
                gpgTrustLevel = 1;
                break;
            case None:
                gpgTrustLevel = 2;
            case Marginal:
                gpgTrustLevel = 3;
                break;
            case Full:
                gpgTrustLevel = 4;
                break;
            case Ultimate:
                gpgTrustLevel = 5;
                break;
        }

        String trustDBRecord = fingerprint + ":" + gpgTrustLevel + ":";
        try {
            String tempPath = "/sdcard/Keymaster/tempTrustDb";
            PrintWriter printWriter = new PrintWriter(tempPath);
            printWriter.print(trustDBRecord);
            printWriter.close();

            Exec(GPG_PATH, "--import-ownertrust", tempPath);
            new File(tempPath).delete();
        } catch(Exception e) {
        }

    }

    @Override
    public void exportPublicKeyring(String destination) {
        String output = Exec(GPG_PATH, "--yes", "--output", destination, "--export");

        Log.i("GPGCli", "Public Keyring exported");
    }

    @Override
    public void exportSecretKeyring(String destination) {
        String output = Exec(GPG_PATH, "--yes", "--output", destination, "--export-secret-keys");

        Log.i("GPGCli", "Secret Keyring exported");
    }

    @Override
    public void exportKey(String destination, String keyId) {
        String outputPath = new File(destination, keyId + ".gpg").getAbsolutePath();
        Exec(GPG_PATH, "--yes", "--output", outputPath, "--export-secret-keys", keyId);

        Log.i("GPGCli", keyId + " exported to " + outputPath);
    }

    @Override
    public void importKey(String source) {
        String out = Exec(true, GPG_PATH, "--yes", "--allow-secret-key-import", "--import", source);
        Log.d(TAG, out);

        Log.i("GPGCli", source + " imported");
    }

    @Override
    public void pushToKeyServer(String server, String keyId) {
        Exec(GPG_PATH, "--yes", "--key-server", server, "--send-key", keyId);

        Log.i("GPGCli", keyId + " pushed to " + server);
    }

    @Override
    public String exportAsciiArmoredKey(String keyId) {
        String output = Exec(GPG_PATH, "--armor", "--export", keyId);
        Log.i("GPGCli", keyId + " exported");

        return output;
    }

    @Override
    public void importAsciiArmoredKey(String armoredKey) {
        try {
            String tempPath = "/sdcard/Keymaster/tempArmoredKey.asc";
            PrintWriter printWriter = new PrintWriter(tempPath);
            printWriter.print(armoredKey);
            printWriter.close();

            Exec(GPG_PATH, "--yes", "--import", tempPath);
            new File(tempPath).delete();
        } catch(Exception e) {
        }
    }
    @Override
    public void encryptAndSign(String recipientId, String signerId, File inputFile, File outputFile) {
        String output = Exec(true, GPG_PATH,
                "--trust-model", "always",
                "--batch",
                "--sign",
                "--encrypt",
                "--output", outputFile.getAbsolutePath(),
                "--local-user", signerId,
                "--recipient", recipientId,
                inputFile.getAbsolutePath());
        Log.d(TAG, "encryptAndSign: done " + output);
    }

    private String Exec(String... command) {
        return Exec(false, command);
    }

    private String Exec(boolean withStderr, String... command) {
        String rawOutput = "";
        try {

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(withStderr);
            Map<String, String> environment = pb.environment();
            environment.put("PATH", environment.get("PATH") + ":/data/data/info.guardianproject.gpg/app_opt/aliases");
            environment.put("LD_LIBRARY_PATH", environment.get("LD_LIBRARY_PATH") + ":/data/data/info.guardianproject.gpg/app_opt/lib:/data/data/info.guardianproject.gpg/lib");
            Log.d(TAG, TextUtils.join(" ", pb.command()));
            Process p = pb.start();
            p.waitFor();
            rawOutput = getProcessOutput(p);

        } catch(IOException e) {
            Log.e("GPGCli", e.getMessage());
        } catch (InterruptedException e) {
            Log.e("GPGCli", e.getMessage());
        }
        return rawOutput;
    }

    private String getProcessOutput(Process p) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            sb.append(line + "\n");
        }
        input.close();

        return sb.toString();
    }
}
