package SampleJWT.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import SampleJWT.auth.entity.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String findMatchesWithAI(Item lostItem, List<Item> foundItems) {
        try {
            // Build the matching prompt
            String prompt = buildMatchingPrompt(lostItem, foundItems);

            // Create Gemini request
            Map<String, Object> requestBody = createGeminiRequest(prompt);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build URL with API key
            String url = apiUrl + "?key=" + apiKey;

            // Make API call
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // Parse and return response
            return parseGeminiResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error finding matches: " + e.getMessage();
        }
    }

    private String buildMatchingPrompt(Item lostItem, List<Item> foundItems) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an intelligent AI assistant for a campus Lost & Found matching system.\n");
        prompt.append("Your goal is to find SIMILAR items, not just exact matches.\n\n");

        prompt.append("IMPORTANT MATCHING RULES:\n");
        prompt.append("═══════════════════════════\n");
        prompt.append("1. Look for SIMILAR items, not exact matches\n");
        prompt.append("2. Items don't need to match perfectly - focus on:\n");
        prompt.append("   - Same general category (e.g., all backpacks, all phones)\n");
        prompt.append("   - Similar descriptions (color, size, brand)\n");
        prompt.append("   - Date proximity (within 1-2 weeks)\n");
        prompt.append("3. Give partial matches confidence scores:\n");
        prompt.append("   - 80-100%: Very likely the same item\n");
        prompt.append("   - 60-79%: Good similarity, worth checking\n");
        prompt.append("   - 40-59%: Possible match, some similarities\n");
        prompt.append("   - Below 40%: Not a good match\n");
        prompt.append("4. Be LENIENT - better to suggest a possibility than miss a match\n\n");

        prompt.append("LOST ITEM DETAILS:\n");
        prompt.append("═══════════════════\n");
        prompt.append("Title: ").append(lostItem.getTitle()).append("\n");
        prompt.append("Description: ").append(lostItem.getDescription()).append("\n");
        prompt.append("Category: ").append(lostItem.getCategory()).append("\n");
        prompt.append("Date Lost: ").append(lostItem.getEventDate()).append("\n\n");

        prompt.append("FOUND ITEMS IN DATABASE:\n");
        prompt.append("═══════════════════════════\n");

        if (foundItems.isEmpty()) {
            prompt.append("⚠️ No found items in the database yet.\n");
            prompt.append("Please inform the user to check back later or browse manually.\n");
            return prompt.toString();
        }

        for (int i = 0; i < foundItems.size(); i++) {
            Item found = foundItems.get(i);
            prompt.append("\n[Item ID: ").append(found.getId()).append("]\n");
            prompt.append("Title: ").append(found.getTitle()).append("\n");
            prompt.append("Description: ").append(found.getDescription()).append("\n");
            prompt.append("Category: ").append(found.getCategory()).append("\n");
            prompt.append("Date Found: ").append(found.getEventDate()).append("\n");
            prompt.append("---\n");
        }

        prompt.append("\n\nYOUR TASK:\n");
        prompt.append("═══════════\n");
        prompt.append("Analyze the lost item against ALL found items and find SIMILAR matches.\n");
        prompt.append("Consider items that are:\n");
        prompt.append("- Same type of object (e.g., both bags, both electronics)\n");
        prompt.append("- Similar colors or appearance\n");
        prompt.append("- Found around the same time\n\n");

        prompt.append("Return ALL items with confidence score above 40%.\n");
        prompt.append("If no items score above 40%, explain why and suggest what to look for.\n\n");

        prompt.append("FORMAT YOUR RESPONSE CLEARLY:\n");
        prompt.append("═══════════════════════════════\n");
        prompt.append("For EACH potential match:\n\n");
        prompt.append("🔍 Match #[number]: [Item ID: X]\n");
        prompt.append("📊 Confidence: X%\n");
        prompt.append("📝 Item: [Title from database]\n");
        prompt.append("✅ Similarities:\n");
        prompt.append("   - [List what matches]\n");
        prompt.append("⚠️ Differences:\n");
        prompt.append("   - [List what doesn't match]\n");
        prompt.append("💡 Recommendation: [Should they check this item? Why?]\n\n");

        prompt.append("If NO matches above 40%:\n");
        prompt.append("State: '❌ NO STRONG MATCHES FOUND'\n");
        prompt.append("Then explain:\n");
        prompt.append("- Why no good matches were found\n");
        prompt.append("- What type of items ARE in the database\n");
        prompt.append("- Suggestions for the user (check manually, report to office, etc.)\n");

        return prompt.toString();
    }


    private Map<String, Object> createGeminiRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();

        // Create the "parts" object
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(textPart);

        // Create the "contents" object
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);

        request.put("contents", contents);

        return request;
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    return text;
                }
            }

            return "No matches found in the database.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing AI response: " + e.getMessage();
        }
    }
}
