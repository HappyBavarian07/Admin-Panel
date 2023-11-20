package de.happybavarian07.adminpanel.syncing.utils;

import au.com.xandar.crypto.AsymmetricCipher;
import au.com.xandar.crypto.CryptoException;
import au.com.xandar.crypto.CryptoPacket;
import au.com.xandar.crypto.RSAKeyPair;
import au.com.xandar.crypto.RSAKeyPairGenerator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtils {
    private static final String ALGORITHM = "RSA";
    private final AsymmetricCipher cipher = new AsymmetricCipher();
    private final RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
    private RSAKeyPair keyPair;

    public EncryptionUtils(String publicKeyBase64, String privateKeyBase64) {
        if (publicKeyBase64 == null && privateKeyBase64 == null) {
            this.generateKeyPair();
        }

        this.keyPair = new RSAKeyPair(privateKeyBase64, publicKeyBase64);
    }

    public EncryptionUtils(RSAKeyPair keyPair) {
        if (keyPair == null) {
            this.generateKeyPair();
        }

        this.keyPair = keyPair;
    }

    public EncryptionUtils(boolean generateKeyPair) {
        if (generateKeyPair) {
            this.generateKeyPair();
        }

    }

    protected void generateKeyPair() {
        this.keyPair = this.rsaKeyPairGenerator.generate();
    }

    public CryptoPacket encrypt(String message) {
        if (this.keyPair.getBase64PrivateKey() == null && this.keyPair.getBase64PrivateKey().isEmpty()) {
            return null;
        } else {
            try {
                return this.cipher.encrypt(message.getBytes(StandardCharsets.UTF_8), this.keyPair.getBase64PrivateKey());
            } catch (CryptoException var3) {
                throw new RuntimeException(var3);
            }
        }
    }

    public String decrypt(String encryptedMessage, String encryptedSymmetricKey, String symmetricCipherInitializationVector) {
        if (this.keyPair.getBase64PublicKey() != null && !this.keyPair.getBase64PublicKey().isEmpty()) {
            try {
                CryptoPacket cryptoPacket = new CryptoPacket(Base64.getDecoder().decode(encryptedMessage), Base64.getDecoder().decode(encryptedSymmetricKey), Base64.getDecoder().decode(symmetricCipherInitializationVector));
                byte[] decryptedBytes = this.cipher.decrypt(cryptoPacket, this.keyPair.getBase64PublicKey());
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } catch (CryptoException var6) {
                throw new RuntimeException(var6);
            }
        } else {
            return encryptedMessage;
        }
    }

    public RSAKeyPair getKeyPair() {
        return this.keyPair;
    }

    public String getPublicKeyAsBase64() {
        return this.keyPair.getBase64PublicKey();
    }

    public String getPrivateKeyAsBase64() {
        return this.keyPair.getBase64PrivateKey();
    }
}
    