package com.example.KinopoiskScraperV2;

import com.example.KinopoiskScraperV2.configuration.KinopoiskScraperV2ApplicationConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class KinopoiskScraperV2Application implements CommandLineRunner {

    private final KinopoiskScraperV2ApplicationConfig kinopoiskScraperV2ApplicationConfig;

    @Autowired
    public KinopoiskScraperV2Application(KinopoiskScraperV2ApplicationConfig kinopoiskScraperV2ApplicationConfig) {
        this.kinopoiskScraperV2ApplicationConfig = kinopoiskScraperV2ApplicationConfig;
    }

    public static void main(String[] args) {
        SpringApplication.run(KinopoiskScraperV2Application.class, args);
    }

    @Override
    public void run(String... args) {
        String apiUrl = "https://kinopoiskapiunofficial.tech/api/v2.2/films/326/reviews";

        String contentType = "application/json";

        HttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(apiUrl);

        httpGet.setHeader("X-API-KEY", kinopoiskScraperV2ApplicationConfig.getApiKey());
        httpGet.setHeader("Content-Type", contentType);

        try {
            HttpResponse response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();

            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Status Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            writeReviewsToCsv(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeReviewsToCsv(String responseBody) {
        JsonElement jsonElement = JsonParser.parseString(responseBody);
        if (jsonElement.isJsonObject()) {
            try (CSVWriter writer = new CSVWriter(new FileWriter("output.csv"))) {
                writeReviewDataToCsv(jsonElement.getAsJsonObject(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid JSON format");
        }
    }

    private void writeReviewDataToCsv(JsonObject jsonObject, CSVWriter writer) {
        if (jsonObject.has("items")) {
            JsonElement itemsElement = jsonObject.get("items");
            if (itemsElement.isJsonArray()) {
                for (JsonElement reviewElement : itemsElement.getAsJsonArray()) {
                    if (reviewElement.isJsonObject()) {
                        JsonObject reviewObject = reviewElement.getAsJsonObject();
                        String kinopoiskId = reviewObject.get("kinopoiskId").getAsString();
                        String type = reviewObject.get("type").getAsString();
                        String date = reviewObject.get("date").getAsString();
                        int positiveRating = reviewObject.get("positiveRating").getAsInt();
                        int negativeRating = reviewObject.get("negativeRating").getAsInt();
                        String author = reviewObject.get("author").getAsString();
                        String title = reviewObject.get("title").getAsString();
                        String description = reviewObject.get("description").getAsString();

                        kinopoiskId = kinopoiskId.replaceAll("\\s+", " ");
                        type = type.replaceAll("\\s+", " ");
                        date = date.replaceAll("\\s+", " ");
                        author = author.replaceAll("\\s+", " ");
                        title = title.replaceAll("\\s+", " ");

                        String oneLineDescription = description.replaceAll("\\s+", " ");
                        String textWithLineBreaks = insertLineBreaks(oneLineDescription, 90);

                        String[] rowData = {
                                "kinopoiskId:" + kinopoiskId,
                                "type:" + type,
                                "date:" + date,
                                "positiveRating:" + positiveRating,
                                "negativeRating:" + negativeRating,
                                "author:" + author,
                                "title:" + title,
                                "description:" + textWithLineBreaks + "\n"
                        };
                        for (String string : rowData) {
                            System.out.println(string);
                        }
                        writer.writeNext(rowData);
                    }
                }
            } else {
                System.out.println("Invalid 'items' format. Expected JSON array.");
            }
        } else {
            System.out.println("Key 'items' not found in JSON object.");
        }
    }
    private static String insertLineBreaks(String text, int charactersPerLine) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (char c : text.toCharArray()) {
            result.append(c);
            count++;
            if (count == charactersPerLine) {
                result.append("\n");
                count = 0;
            }
        }
        return result.toString();
    }
}
