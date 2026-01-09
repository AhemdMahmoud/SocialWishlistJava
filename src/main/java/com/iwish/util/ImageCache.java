package com.iwish.util;

import javafx.scene.image.Image;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Image cache utility to speed up image loading
 * Caches loaded images to avoid reloading them
 */
public class ImageCache {
    private static final ImageCache instance = new ImageCache();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();
    private final Map<String, String> urlMapping = new ConcurrentHashMap<>(); // Maps original URL to normalized URL

    private ImageCache() {
    }

    public static ImageCache getInstance() {
        return instance;
    }

    /**
     * Get or load image from cache
     * @param imageUrl Original image URL
     * @return Cached or newly loaded Image
     */
    public Image getImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // Normalize URL
        String normalizedUrl = normalizeUrl(imageUrl);
        
        // Check cache first
        Image cached = cache.get(normalizedUrl);
        if (cached != null && !cached.isError()) {
            return cached;
        }

        // Load new image
        try {
            Image img = new Image(normalizedUrl, true); // Background loading
            
            // Wait a bit for image to start loading, then cache it
            // We cache even if not fully loaded to avoid duplicate requests
            cache.put(normalizedUrl, img);
            urlMapping.put(imageUrl, normalizedUrl);
            
            return img;
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageUrl + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Normalize URL to handle different formats
     */
    private String normalizeUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }

        // HTTP/HTTPS URLs - use as is
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        // File URLs - use as is
        if (imageUrl.startsWith("file:")) {
            return imageUrl;
        }

        // Windows file paths (C:, D:, G:, etc.)
        if (imageUrl.length() >= 2 && imageUrl.charAt(1) == ':') {
            return "file:///" + imageUrl.replace("\\", "/");
        }

        // Try as file path
        File imgFile = new File(imageUrl);
        if (imgFile.exists()) {
            return imgFile.toURI().toString();
        }

        // Return as is if nothing matches
        return imageUrl;
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        cache.clear();
        urlMapping.clear();
    }

    /**
     * Remove specific image from cache
     */
    public void removeFromCache(String imageUrl) {
        String normalizedUrl = urlMapping.get(imageUrl);
        if (normalizedUrl != null) {
            cache.remove(normalizedUrl);
            urlMapping.remove(imageUrl);
        }
    }

    /**
     * Get cache size
     */
    public int getCacheSize() {
        return cache.size();
    }
}

