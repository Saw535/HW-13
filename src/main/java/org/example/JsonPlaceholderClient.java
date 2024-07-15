package org.example;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPlaceholderClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";
    private static final Logger logger = Logger.getLogger(JsonPlaceholderClient.class.getName());

    public String createUser(String jsonInputString) {
        try {
            URI uri = URI.create(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating user", e);
            return null;
        }
    }

    public String updateUser(int userId, String jsonInputString) {
        try {
            URI uri = URI.create(BASE_URL + "/" + userId);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating user", e);
            return null;
        }
    }

    public String deleteUser(int userId) {
        try {
            URI uri = URI.create(BASE_URL + "/" + userId);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("DELETE");

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting user", e);
            return null;
        }
    }

    public String getAllUsers() {
        try {
            URI uri = URI.create(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching all users", e);
            return null;
        }
    }

    public String getUserById(int userId) {
        try {
            URI uri = URI.create(BASE_URL + "/" + userId);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching user by ID", e);
            return null;
        }
    }

    public String getUserByUsername(String username) {
        try {
            URI uri = URI.create(BASE_URL + "?username=" + username);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            return getResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching user by username", e);
            return null;
        }
    }

    public void saveCommentsOfLastPost(int userId) {
        try {
            URI uri = URI.create(BASE_URL + "/" + userId + "/posts");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String postsResponse = getResponse(conn);

            JSONArray posts = new JSONArray(postsResponse);
            int lastPostId = -1;
            for (int i = 0; i < posts.length(); i++) {
                int postId = posts.getJSONObject(i).getInt("id");
                if (postId > lastPostId) {
                    lastPostId = postId;
                }
            }

            if (lastPostId == -1) {
                logger.log(Level.SEVERE, "No posts found for user with ID " + userId);
                return;
            }

            uri = URI.create("https://jsonplaceholder.typicode.com/posts/" + lastPostId + "/comments");
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String commentsResponse = getResponse(conn);

            String filename = "user-" + userId + "-post-" + lastPostId + "-comments.json";
            try (FileWriter file = new FileWriter(filename)) {
                file.write(commentsResponse);
                logger.log(Level.INFO, "Comments saved to " + filename);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error writing to file " + filename, e);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching comments for the last post", e);
        }
    }

    public String getOpenTodosForUser(int userId) {
        try {
            URI uri = URI.create(BASE_URL + "/" + userId + "/todos");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String todosResponse = getResponse(conn);

            JSONArray todos = new JSONArray(todosResponse);
            JSONArray openTodos = new JSONArray();
            for (int i = 0; i < todos.length(); i++) {
                JSONObject todo = todos.getJSONObject(i);
                if (!todo.getBoolean("completed")) {
                    openTodos.put(todo);
                }
            }

            return openTodos.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching open todos for user with ID " + userId, e);
            return null;
        }
    }

    private String getResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        if (responseCode >= 200 && responseCode < 300) {
            return response.toString();
        } else {
            throw new RuntimeException("HTTP request failed with response code " + responseCode);
        }
    }

    public static void main(String[] args) {
        JsonPlaceholderClient client = new JsonPlaceholderClient();
        try {
            String newUser = "{\"name\": \"John Doe\", \"username\": \"john doe\", \"email\": \"john.doe@example.com\"}";
            System.out.println("Create User: " + client.createUser(newUser));

            String updatedUser = "{\"id\": 1, \"name\": \"Jane Doe\", \"username\": \"japanned\", \"email\": \"jane.doe@example.com\"}";
            System.out.println("Update User: " + client.updateUser(1, updatedUser));

            System.out.println("Delete User: " + client.deleteUser(1));

            System.out.println("All Users: " + client.getAllUsers());

            System.out.println("User by ID: " + client.getUserById(1));

            System.out.println("User by Username: " + client.getUserByUsername("Bret"));

            client.saveCommentsOfLastPost(1);

            System.out.println("Open Todos for User 1: " + client.getOpenTodosForUser(1));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in main method", e);
        }
    }
}
