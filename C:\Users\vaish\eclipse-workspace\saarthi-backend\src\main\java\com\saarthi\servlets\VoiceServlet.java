package com.saarthi.servlets;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.VertexAiOptions;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

public class VoiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --- 1. UPDATE YOUR PROJECT ID ---
    private static final String PROJECT_ID = "saarthi-voice-app"; 

    // --- 2. UPDATE THE PATH TO YOUR KEY FILE ---
    private static final String JSON_KEY_PATH = "E:\\saarthi-voice-app-e3fa49c7defa.json";

    private static final String LOCATION = "us-central1";
    private static final String GEMINI_MODEL_NAME = "gemini-pro";

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject();
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            
            JSONObject jsonRequest = new JSONObject(sb.toString());
            String userInput = jsonRequest.getString("message");
            
            String reply = getGeminiReply(userInput);
            jsonResponse.put("reply", reply);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().print(jsonResponse.toString());
    }

    private String getGeminiReply(String userInput) throws IOException {
        String systemPrompt = "You are Saarthi, a friendly and helpful voice assistant for visually impaired users. Be clear, concise, and direct in your answers.";

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(JSON_KEY_PATH));
            
            VertexAiOptions options = VertexAiOptions.newBuilder()
                .setProjectId(PROJECT_ID)
                .setLocation(LOCATION)
                .setCredentials(credentials)
                .build();

            try (VertexAI vertexAi = new VertexAI(options)) {
                GenerativeModel model = new GenerativeModel(GEMINI_MODEL_NAME, vertexAi);
                GenerateContentResponse response = model.generateContent(systemPrompt + "\nUser: " + userInput);
                return ResponseHandler.getText(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "I'm sorry, I'm having trouble connecting to my brain right now.";
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
