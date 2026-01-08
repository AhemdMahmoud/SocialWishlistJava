package com.iwish.client;

import javafx.scene.image.Image;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCacheManager {

    private static ImageCacheManager instance;
    private final File cacheDir;
    private final Map<String, Image> memoryCache;
    private final ExecutorService executor;

    private ImageCacheManager() {
        // Create cache directory in user home
        String home = System.getProperty("user.home");
        cacheDir = new File(home, ".iwish/image_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        memoryCache = new HashMap<>();
        executor = Executors.newFixedThreadPool(4); // 4 threads for image loading
    }

    public static synchronized ImageCacheManager getInstance() {
        if (instance == null) {
            instance = new ImageCacheManager();
        }
        return instance;
    }

    public Image getImage(String url, double requestedWidth) {
        if (url == null || url.isEmpty())
            return null;

        // 1. Check Memory Cache
        if (memoryCache.containsKey(url)) {
            return memoryCache.get(url);
        }

        // 2. Check Disk Cache
        String fileName = getHash(url) + ".png"; 
        File cachedFile = new File(cacheDir, fileName);

        if (cachedFile.exists()) {
            try {
                String fileUri = cachedFile.toURI().toString();
                // Load from disk: background=true
                Image img = new Image(fileUri, requestedWidth, 0, true, true); 
                memoryCache.put(url, img);
                return img;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Not in cache, download it
        downloadAndCache(url);

        // Return standard background loading image for now (so UI shows something/placeholder)
        Image img = new Image(url, requestedWidth, 0, true, true);
        memoryCache.put(url, img);
        return img;
    }

    private void downloadAndCache(String urlStr) {
        executor.submit(() -> {
            try {
                if (urlStr.startsWith("file:"))
                    return; // Don't cache local files

                String fileName = getHash(urlStr) + ".png";
                File cachedFile = new File(cacheDir, fileName);
                if (cachedFile.exists())
                    return;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                try (InputStream in = conn.getInputStream();
                        FileOutputStream out = new FileOutputStream(cachedFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

            } catch (Exception e) {
                // System.err.println("Failed to cache image: " + urlStr);
            }
        });
    }

    private String getHash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(s.hashCode());
        }
    }
}
