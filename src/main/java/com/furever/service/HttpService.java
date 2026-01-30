package com.furever.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpService {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private static final List<String> USER_AGENTS = List.of(
            // Chrome (Windows 10)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",

            // Chrome (macOS)
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",

            // Firefox (Windows)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",

            // Safari (macOS)
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15",

            // Edge (Windows)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0"
    );


    public HttpService() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    private String getRandomUserAgent() {
        int index = ThreadLocalRandom.current().nextInt(USER_AGENTS.size());
        return USER_AGENTS.get(index);
    }

    public <T> T get(String url, Class<T> responseType) {
        try {
            HttpResponse<String> response = send(url);
            return mapper.readValue(response.body(), responseType);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось скачать: " + url, e);
        }
    }

    public <T> T get(String url, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response = send(url);
            return mapper.readValue(response.body(), responseType);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось скачать: " + url, e);
        }
    }

    private HttpResponse<String> send(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", getRandomUserAgent())
                .header("Referer", "https://leonbets.com/")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка сервера: " + response.statusCode());
        }
        return response;
    }
}
