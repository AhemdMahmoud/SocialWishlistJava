package com.iwish.client;

/**
 * Launcher class to work around JavaFX module issues.
 * This class does not extend Application and simply delegates to ClientMain.
 */
public class Launcher {
    public static void main(String[] args) {
        System.out.println("!!! VERSION CHECK: TILEPANE FIX APPLIED !!!");
        ClientMain.main(args);
    }
}
