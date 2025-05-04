package com.flippidy.skyblock.handlers;

import com.flippidy.skyblock.FlippidyMod;
import com.flippidy.skyblock.models.UserProfile;
import com.flippidy.skyblock.utils.MessageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileManager {
    private final File profilesDir;
    private Map<String, UserProfile> profileCache;
    private UserProfile activeProfile;
    
    public ProfileManager(File configDir) {
        this.profilesDir = new File(configDir, "profiles");
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        
        this.profileCache = new HashMap<>();
        loadProfiles();
        
        // Lade oder erstelle Standardprofil
        if (profileCache.isEmpty()) {
            UserProfile defaultProfile = new UserProfile("Standard");
            saveProfile(defaultProfile);
            setActiveProfile(defaultProfile);
        } else if (activeProfile == null) {
            setActiveProfile(profileCache.values().iterator().next());
        }
    }
    
    private void loadProfiles() {
        File[] profileFiles = profilesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profileFiles == null) return;
        
        for (File profileFile : profileFiles) {
            String fileName = profileFile.getName();
            String profileName = fileName.substring(0, fileName.length() - 5); // Entferne .json
            
            UserProfile profile = UserProfile.load(profileFile);
            if (profile != null) {
                profileCache.put(profileName.toLowerCase(), profile);
            }
        }
    }
    
    public void saveProfile(UserProfile profile) {
        File profileFile = new File(profilesDir, profile.getName().toLowerCase() + ".json");
        profile.save(profileFile);
        profileCache.put(profile.getName().toLowerCase(), profile);
    }
    
    public UserProfile createProfile(String name) {
        if (profileCache.containsKey(name.toLowerCase())) {
            MessageUtils.sendMessage("§cEin Profil mit diesem Namen existiert bereits!");
            return null;
        }
        
        UserProfile newProfile = new UserProfile(name);
        saveProfile(newProfile);
        return newProfile;
    }
    
    public UserProfile getProfile(String name) {
        return profileCache.get(name.toLowerCase());
    }
    
    public boolean deleteProfile(String name) {
        if (name.equalsIgnoreCase("standard")) {
            MessageUtils.sendMessage("§cDas Standardprofil kann nicht gelöscht werden!");
            return false;
        }
        
        if (!profileCache.containsKey(name.toLowerCase())) {
            return false;
        }
        
        File profileFile = new File(profilesDir, name.toLowerCase() + ".json");
        boolean deleted = profileFile.delete();
        
        if (deleted) {
            if (activeProfile != null && activeProfile.getName().equalsIgnoreCase(name)) {
                // Wenn wir das aktive Profil gelöscht haben, verwende das erste verfügbare
                setActiveProfile(getProfiles().get(0));
            }
            
            profileCache.remove(name.toLowerCase());
        }
        
        return deleted;
    }
    
    public List<UserProfile> getProfiles() {
        return new ArrayList<>(profileCache.values());
    }
    
    public UserProfile getActiveProfile() {
        return activeProfile;
    }
    
    public void setActiveProfile(UserProfile profile) {
        this.activeProfile = profile;
        
        // Speichere die aktive Profilauswahl in einer separaten Datei
        try {
            File activeProfileFile = new File(profilesDir, "active_profile.txt");
            java.nio.file.Files.writeString(activeProfileFile.toPath(), profile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean setActiveProfileByName(String name) {
        UserProfile profile = getProfile(name);
        if (profile != null) {
            setActiveProfile(profile);
            return true;
        }
        return false;
    }
}