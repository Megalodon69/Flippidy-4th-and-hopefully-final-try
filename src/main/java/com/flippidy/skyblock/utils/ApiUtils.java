package com.flippidy.skyblock.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dejvokep.boostedyaml.utils.conversion.PairConverter;
import io.github.reflxction.simplehttpClient.SimpleHttpClient;
import io.github.reflxction.simplehttpClient.SimpleHttpResponse;

public class ApiUtils {
    private static final String HYPIXEL_BAZAAR_API_URL = "https://api.hypixel.net/skyblock/bazaar";
    private static final String COFLNET_API_BASE_URL = "https://sky.coflnet.com/api";

    /**
     * Ruft aktuelle Bazaar-Daten vom Hypixel API ab
     * @return JsonObject mit den Bazaar-Daten oder null, wenn ein Fehler auftritt
     */
    public static JsonObject fetchHypixelBazaarData() {
        try {
            SimpleHttpClient client = new SimpleHttpClient();
            SimpleHttpResponse response = client.get(HYPIXEL_BAZAAR_API_URL);
            
            if (response.getStatusCode() != 200) {
                MessageUtils.sendDebugMessage("API-Fehler: Statuscode " + response.getStatusCode());
                return null;
            }
            
            return new JsonParser().parse(response.getResponseBody()).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Ruft historische Preisdaten von der CoflNet API ab
     * @return JsonObject mit den historischen Daten oder null, wenn ein Fehler auftritt
     */
    public static JsonObject fetchCoflnetHistoricalData() {
        try {
            // In einer echten Implementierung würde hier ein spezifischer Endpunkt für historische Daten verwendet werden
            String url = COFLNET_API_BASE_URL + "/items/prices/history/monthly";
            
            SimpleHttpClient client = new SimpleHttpClient();
            SimpleHttpResponse response = client.get(url);
            
            if (response.getStatusCode() != 200) {
                MessageUtils.sendDebugMessage("CoflNet API-Fehler: Statuscode " + response.getStatusCode());
                return null;
            }
            
            return new JsonParser().parse(response.getResponseBody()).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Holt den aktuellen Münzstand (Purse) des Spielers von Hypixel API
     * @return Die Anzahl der Münzen in der Geldbörse des Spielers oder 0 im Fehlerfall
     */
    public static double fetchPlayerPurse() {
        // In einer echten Implementierung würde hier ein API-Aufruf erfolgen
        // Da dies nur ein Proof of Concept ist, verwenden wir einen statischen Wert
        // oder würden den Wert aus dem Spieler-HUD auslesen
        return 1000000.0; // 1 Million Münzen als Beispiel
    }
}