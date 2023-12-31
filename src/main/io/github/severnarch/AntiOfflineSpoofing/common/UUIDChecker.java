package io.github.severnarch.AntiOfflineSpoofing.common;

import javax.sound.midi.SysexMessage;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UUIDChecker {
    private static final String usernameAPI = "https://api.mojang.com/users/profiles/minecraft/";

    private static final ArrayList<String> usernamesWithPremiumUUIDs = new ArrayList<>();
    private static final ArrayList<String> usernamesWithoutPremiumUUIDs = new ArrayList<>();
    private static final HashMap<String, UUID> usernamesAndUUIDS = new HashMap<>();

    // Main function for testing purposes
    public static void main(String[] args) {
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing in progress...");

        // Test isOfflineUUID
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing UUIDChecker.isOfflineUUID(\"Severnarch\", UUID.fromString(\"ca05036b-1665-4407-b176-c33573693209\")): "+
                            isOfflineUUID("Severnarch", UUID.fromString("ca05036b-1665-4407-b176-c33573693209")));
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing UUIDChecker.isOfflineUUID(\"RandomCrackedAccount\", UUID.fromString(\"3b889845-ad24-3dcf-8510-cb2e75c245fa\")): "+
                            isOfflineUUID("RandomCrackedAccount", UUID.fromString("3b889845-ad24-3dcf-8510-cb2e75c245fa")));

        // Test usernameHasPremiumUUID
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing UUIDChecker.usernameHasPremiumUUID(\"Severnarch\"): "+
                            usernameHasPremiumUUID("Severnarch"));
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing UUIDChecker.usernameHasPremiumUUID(\"RandomCrackedAccount\"): "+
                            usernameHasPremiumUUID("RandomCrackedAccount"));

        // Test getPremiumUUID
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing UUIDChecker.getPremiumUUID(\"Severnarch\"): "+
                            getPremiumUUID("Severnarch"));

        // Print caches
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Cached usernames with Premium: "+
                            usernamesWithPremiumUUIDs);
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Cached usernames without Premium: "+
                            usernamesWithoutPremiumUUIDs);
        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Cached usernames and UUIDs: "+
                            usernamesAndUUIDS);

        System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.main] Testing complete.");
    }

    public static boolean isOfflineUUID(String username, UUID uuid) {
        return Objects.equals(uuid, UUID.nameUUIDFromBytes(("OfflinePlayer:"+username).getBytes(StandardCharsets.UTF_8)));
    }
    public static boolean usernameHasPremiumUUID(String username) {
        for (String entry : usernamesWithPremiumUUIDs) {if (Objects.equals(entry, username)) {return true;}}
        for (String entry : usernamesWithoutPremiumUUIDs) {if (Objects.equals(entry, username)) {return false;}}
        try {
            URL url = new URL(usernameAPI + username);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            if (huc.getResponseCode() == 200) {
                usernamesWithPremiumUUIDs.add(username);
                return true;
            } else {
                usernamesWithoutPremiumUUIDs.add(username);
                return false;
            }
        } catch (MalformedURLException mue) {
            System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.usernameHasPremiumUUID] Something went wrong, and the Username API was attempted to be reached with a malformed URL.");
            return false;
        } catch (IOException ioe) {
            System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.usernameHasPremiumUUID] Something went wrong, and a HTTP connection was unable to be established.");
            return false;
        }
    }

    public static UUID getPremiumUUID(String username) {
        if (usernamesAndUUIDS.containsKey(username)) {
            return usernamesAndUUIDS.get(username);
        }
        if (usernameHasPremiumUUID(username)) {
            try {
                URL url = new URL(UUIDChecker.usernameAPI + username);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                BufferedReader bfr = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                StringBuilder rsp = new StringBuilder();
                for (String line; (line = bfr.readLine()) != null;) {
                    if (!rsp.isEmpty()) {
                         rsp.append("\n");
                     }
                     rsp.append(line);
                }
                // Unformatted UUID string.
                String ufs = rsp.toString().split("\"id\"")[1].split(",")[0].split("\"")[1];
                // Format UUID string into a UUID
                String ids = "-";
                String uis = ufs.substring(0,8)+ids+ufs.substring(8,12)+ids+ufs.substring(12,16)+ids+ufs.substring(16,20)+ids+ufs.substring(20,32);
                UUID uid = UUID.fromString(uis);
                usernamesAndUUIDS.put(username, uid);
                return uid;
            } catch (MalformedURLException mue) {
                System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.getPremiumUUID] Something went wrong, and the Username API was attempted to be reached with a malformed URL.");
                return null;
            } catch (IOException ioe) {
                System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.getPremiumUUID] Something went wrong, and a HTTP connection was unable to be established.");
                return null;
            }
        } else {
            System.out.println("[AntiOfflineSpoofing::common::UUIDChecker.getPremiumUUID] Username "+username+" is not a valid premium account!");
            return null;
        }
    }
}
