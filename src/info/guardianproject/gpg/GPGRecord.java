package info.guardianproject.gpg;

import android.util.Log;

public class GPGRecord {

    public enum Type {
        Public,
        X509,
        X509WithSecret,
        Secret,
        Sub,
        SecretSub,
        UserId,
        UserAttribute,
        Signature,
        RevocationSignature,
        Fingerprint,
        PublicKeyData,
        KeyGrip,
        RevocationKey,
        TrustDatabaseInformation,
        SignatureSubpacket
    }

    public enum Algorithm {
        RSA,
        ElgamalEncrypt,
        DSA,
        ElgamalSignEncrypt
    }

    public enum Capabilities {
        Encrypt,
        Sign,
        Certify,
        Authentication
    }

    private Type type;
    private TrustLevel validity;
    private Algorithm algorithm;
    private String length;
    private String keyId;
    private String creationDate;
    private String expirationDate;
    private String localId;
    private TrustLevel ownerTrust;
    private String userId;

    public GPGRecord() {
    }

    //A colon-listing is the output of "gpg --with-colons --list-keys"
    public static GPGRecord FromColonListingFactory(String colonListing) {
        GPGRecord key = new GPGRecord();

        String[] fields = colonListing.split(":");
        if(fields.length < 10) {
            return key;
        }

        String type = fields[0];
        if(type.equals("pub")) {
            key.type = Type.Public;
        } else if(type.equals("crt")) {
            key.type = Type.X509;
        } else if(type.equals("crs")) {
            key.type = Type.X509WithSecret;
        } else if(type.equals("sub")) {
            key.type = Type.Sub;
        } else if(type.equals("sec")) {
            key.type = Type.Secret;
        } else if(type.equals("ssb")) {
            key.type = Type.SecretSub;
        } else if(type.equals("uid")) {
            key.type = Type.UserId;
        } else if(type.equals("uat")) {
            key.type = Type.UserAttribute;
        } else if(type.equals("sig")) {
            key.type = Type.Signature;
        } else if(type.equals("rev")) {
            key.type = Type.RevocationSignature;
        } else if(type.equals("fpr")) {
            key.type = Type.Fingerprint;
        } else if(type.equals("pkd")) {
            key.type = Type.PublicKeyData;
        } else if(type.equals("grp")) {
            key.type = Type.KeyGrip;
        } else if(type.equals("rvk")) {
            key.type = Type.RevocationKey;
        } else if(type.equals("tru")) {
            key.type = Type.TrustDatabaseInformation;
        } else if(type.equals("spk")) {
            key.type = Type.SignatureSubpacket;
        } else {
            Log.e("GPGRecord", "UKNOWN TYPE:" + type);
        }

        if(fields[1].length() > 0) {
            key.validity = ParseTrustLevel(fields[1].charAt(0));
        }

        key.length = fields[2];

        try {
            int algorithm = Integer.parseInt(fields[3]);
            switch(algorithm) {
                case 1:
                    key.algorithm = Algorithm.RSA;
                    break;
                case 16:
                    key.algorithm = Algorithm.ElgamalEncrypt;
                    break;
                case 17:
                    key.algorithm = Algorithm.DSA;
                    break;
                case 20:
                    key.algorithm = Algorithm.ElgamalSignEncrypt;
                    break;
            }
        } catch(NumberFormatException e) {
        }

        key.keyId          = fields[4];
        key.creationDate   = fields[5];
        key.expirationDate = fields[6];
        key.localId        = fields[7];
        if(fields[8].length() > 0) {
            key.ownerTrust     = ParseTrustLevel(fields[8].charAt(0));
        }
        key.userId         = fields[9];

        return key;
    }

    public Type getType() {
        return type;
    }

    public TrustLevel getValidity() {
        return validity;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getLength() {
        return length;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getLocalId() {
        return localId;
    }

    public TrustLevel getOwnerTrust() {
        return ownerTrust;
    }

    public String getUserId() {
        return userId;
    }

    public String getPersonalName() {
        return this.getUserId().split("<")[0].trim();
    }

    public String getEmail() {
        String[] userIdParts = this.getUserId().split("<");
        if(userIdParts.length < 2) {
            return null;
        }

        return userIdParts[1].substring(0, userIdParts[1].length() - 1);
    }

    private static TrustLevel ParseTrustLevel(char trust) {
        switch(trust) {
            case 'o':
                return TrustLevel.New;
            case 'i':
                return TrustLevel.Invalid;
            case 'd':
                return TrustLevel.Disabled;
            case 'r':
                return TrustLevel.Revoked;
            case 'e':
                return TrustLevel.Expired;
            case 'q':
                return TrustLevel.Undefined;
            case 'm':
                return TrustLevel.Marginal;
            case 'f':
                return TrustLevel.Full;
            case 'u':
                return TrustLevel.Ultimate;
            case '-':
            default:
                return TrustLevel.Unknown;
        }
    }
}
