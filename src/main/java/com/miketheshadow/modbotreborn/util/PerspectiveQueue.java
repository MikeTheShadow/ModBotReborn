package com.miketheshadow.modbotreborn.util;

import com.miketheshadow.modbotreborn.ModBotReborn;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PerspectiveQueue {

    public static final double TOXICICITY_BAR = 0.85;
    public static final MongoCollection<Document> collection = init();

    public static MongoCollection<Document> init() {
        if(collection == null) {
            MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            MongoDatabase database = client.getDatabase(ModBotReborn.DB_NAME);
            return database.getCollection("toxicComments");
        }
        return collection;
    }

    public static double awaitQueue(String sentiment,String author) {
        try {
            Thread.sleep(1000);
        }catch (Exception e) {
        }
            try {
                double x = queue(sentiment);
                if(x > TOXICICITY_BAR) {

                    Document document = new Document();
                    document.append("author",author);
                    document.append("text",sentiment);

                    if(collection.find(new BasicDBObject("author",author)).first() == null) {
                        collection.insertOne(document);
                    }
                }
                return x;

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return -1;
    }


    public static double queue(String sentiment) throws Exception {
        String key = ModBotReborn.configuration.getString("api_key");
        URL url = new URL("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + key);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        byte[] out = ("{ \"comment\": { \"text\": \"" + sentiment.replaceAll("[\"]","") + "\" }, \"languages\": [\"en\"], \"requestedAttributes\": { \"SEVERE_TOXICITY\": {}, \"SEXUALLY_EXPLICIT\": {}} }").getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        OutputStream os = http.getOutputStream();
        Throwable throwable = null;

        try {
            os.write(out);
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            if (os != null) {
                if (throwable != null) {
                    try {
                        os.close();
                    } catch (Throwable var20) {
                        throwable.addSuppressed(var20);
                    }
                } else {
                    os.close();
                }
            }
        }

        InputStream stream = http.getInputStream();
        String output = convertStreamToString(stream);
        JSONObject obj = new JSONObject(output);
        double id_attack = obj.getJSONObject("attributeScores").getJSONObject("SEVERE_TOXICITY").getJSONObject("summaryScore").getDouble("value");
        double da_attack = obj.getJSONObject("attributeScores").getJSONObject("SEXUALLY_EXPLICIT").getJSONObject("summaryScore").getDouble("value");
        List<Double> dList = new ArrayList<>();
        dList.add(id_attack);
        dList.add(da_attack);
        return Collections.max(dList);
    }
    private static String convertStreamToString(InputStream is) {
        Scanner s = (new Scanner(is)).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
