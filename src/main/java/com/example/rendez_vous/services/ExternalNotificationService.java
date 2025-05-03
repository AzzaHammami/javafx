package com.example.rendez_vous.services;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Arrays;

public class ExternalNotificationService {
    private static final String BASE_URL = "http://localhost:8080/api/notifications";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Notification> getNotificationsForUser(int userId) {
        String url = BASE_URL + "?userId=" + userId;
        ResponseEntity<Notification[]> response = restTemplate.getForEntity(url, Notification[].class);
        return Arrays.asList(response.getBody());
    }

    public Notification sendNotification(Notification notification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Notification> request = new HttpEntity<>(notification, headers);
        return restTemplate.postForObject(BASE_URL, request, Notification.class);
    }

    public Notification markAsRead(long id) {
        String url = BASE_URL + "/" + id + "/read";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Notification> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, request, Notification.class);
        return response.getBody();
    }

    public static class Notification {
        public long id;
        public int userId;
        public String title;
        public String message;
        public long timestamp;
        public boolean read;
    }
}
