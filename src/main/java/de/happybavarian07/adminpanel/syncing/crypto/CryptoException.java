package de.happybavarian07.adminpanel.syncing.crypto;

/**
 * Indicates there was a problem during encryption or decryption.
 */
public final class CryptoException extends Exception {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
