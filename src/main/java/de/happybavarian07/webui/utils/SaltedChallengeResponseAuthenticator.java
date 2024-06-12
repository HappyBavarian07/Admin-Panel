package de.happybavarian07.webui.utils;/*
 * @Author HappyBavarian07
 * @Date 13.05.2024 | 16:55
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class SaltedChallengeResponseAuthenticator {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Set<Challenge> usedChallenges = new HashSet<>();

    public String generateChallenge() {
        byte[] randomBytes = new byte[24];
        while (true) {
            secureRandom.nextBytes(randomBytes);
            String challenge = Base64.getEncoder().encodeToString(randomBytes);
            if (!isChallengeUsed(challenge)) {
                return challenge;
            }
        }
    }

    public boolean verifyResponse(String storedPassword, String clientResponse, String challenge) {
        //System.out.println("Stored Password: " + storedPassword);
        //System.out.println("Challenge: " + challenge);
        String serverResponse = hashPasswordWithChallenge(storedPassword, challenge);
        //System.out.println("Server Response: " + serverResponse);
        //System.out.println("Client Response: " + clientResponse);
        return serverResponse.equals(clientResponse);
    }

    private String hashPasswordWithChallenge(String hashedPassword, String challenge) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(challenge.getBytes());
            byte[] bytes = md.digest(hashedPassword.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }

    public Challenge storeChallenge(String challenge, long timestamp, User user) {
        Challenge c = new Challenge(challenge, timestamp, user);
        if(isChallengeUsed(challenge, timestamp, user)) {
            return null;
        }
        if(challengeExpired(challenge)) {
            throw new IllegalArgumentException("Challenge expired before storing it!");
        }
        usedChallenges.add(c);
        return c;
    }

    public boolean isChallengeUsed(String challenge) {
        return usedChallenges.stream().anyMatch(c -> c.getChallenge().equals(challenge));
    }

    public boolean isChallengeUsed(String challenge, User user) {
        return usedChallenges.stream().anyMatch(c -> c.getChallenge().equals(challenge) && c.getUser().equals(user));
    }

    public boolean isChallengeUsed(String challenge, long timestamp, User user) {
        return usedChallenges.stream().anyMatch(c -> c.getChallenge().equals(challenge) && c.getTimestamp() == timestamp && c.getUser().equals(user));
    }

    public void removeChallenge(String challenge) {
        usedChallenges.removeIf(c -> c.getChallenge().equals(challenge));
    }

    public boolean challengeExpired(String challenge) {
        return usedChallenges.stream().anyMatch(c -> c.getChallenge().equals(challenge) && System.currentTimeMillis() - c.getTimestamp() > 300000);
    }

    public boolean challengeExpired(long timestamp) {
        return System.currentTimeMillis() - timestamp > 300000;
    }

    public void removeChallenge(String challenge, long timestamp, User user) {
        usedChallenges.removeIf(c -> c.getChallenge().equals(challenge) && c.getTimestamp() == timestamp && c.getUser().equals(user));
    }

    public void clearChallenges() {
        usedChallenges.clear();
    }

    public Challenge getChallenge(String challenge) {
        return usedChallenges.stream().filter(c -> c.getChallenge().equals(challenge)).findFirst().orElse(null);
    }

    public Challenge getUserChallenge(User user) {
        return usedChallenges.stream().filter(c -> c.getUser().equals(user)).findFirst().orElse(null);
    }

    public static class Challenge {
        private final String challenge;
        private final long timestamp;
        private final User user;

        public Challenge(String challenge, long timestamp, User user) {
            this.challenge = challenge;
            this.timestamp = timestamp;
            this.user = user;
        }

        public String getChallenge() {
            return challenge;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public User getUser() {
            return user;
        }
    }
}
