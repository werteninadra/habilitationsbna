package com.bna.habilitationbna.service;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class IpfsUploader {
    private static final OkHttpClient client = new OkHttpClient();

    // URL API Pinata
    private static final String PINATA_API_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    // Remplace par ta clé API Pinata (API key) et ton secret
    private static final String PINATA_API_KEY = "0e031a63a0472a27e188";
    private static final String PINATA_SECRET_API_KEY = "7d126d89cd3c01d646542d8e61a5b197b4a6132577ab316a769dce8db7a06be3";

    public static String uploadFile(File file) throws IOException {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(PINATA_API_URL)
                .addHeader("pinata_api_key", PINATA_API_KEY)
                .addHeader("pinata_secret_api_key", PINATA_SECRET_API_KEY)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur lors de l'upload vers Pinata : " + response);
            }

            // Le body est en JSON, exemple : {"IpfsHash":"Qm...","PinSize":123,"Timestamp":"2023-xx-xx"}
            String responseBody = response.body().string();

            // Tu peux extraire le hash IPFS du JSON (par ex. avec Jackson ou Gson)
            // Pour simplifier, on fait un parse simple ici :
            String ipfsHash = parseIpfsHashFromResponse(responseBody);

            return ipfsHash;
        }
    }

    private static String parseIpfsHashFromResponse(String json) {
        // Extrait la valeur "IpfsHash" du JSON brut (simple extraction, remplacer par une vraie lib JSON)
        String key = "\"IpfsHash\":\"";
        int start = json.indexOf(key);
        if (start < 0) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }
}
