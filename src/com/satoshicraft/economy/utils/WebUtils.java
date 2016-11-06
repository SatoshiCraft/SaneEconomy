package com.satoshicraft.economy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.satoshicraft.economy.SatoshiEconomy;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public class WebUtils {
    public static String getContents(String url) {
        try {
            String out = "";
            URL uri = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(uri.openConnection().getInputStream()));
            String line;

            while ((line = br.readLine()) != null) {
                out += line + "\n";
            }

            return out;
        } catch (IOException e) {
            SatoshiEconomy.logger().warning("Failed to get contents of URL " + url);
            e.printStackTrace();
            throw new RuntimeException("Failed to get URL contents!");
        }
    }
}
