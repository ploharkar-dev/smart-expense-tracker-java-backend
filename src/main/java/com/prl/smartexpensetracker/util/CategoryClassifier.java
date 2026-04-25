package com.prl.smartexpensetracker.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CategoryClassifier {

    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // Food & Dining
        CATEGORY_KEYWORDS.put("pizza", "Food & Dining");
        CATEGORY_KEYWORDS.put("restaurant", "Food & Dining");
        CATEGORY_KEYWORDS.put("cafe", "Food & Dining");
        CATEGORY_KEYWORDS.put("coffee", "Food & Dining");
        CATEGORY_KEYWORDS.put("groceries", "Food & Dining");
        CATEGORY_KEYWORDS.put("supermarket", "Food & Dining");
        CATEGORY_KEYWORDS.put("burger", "Food & Dining");
        CATEGORY_KEYWORDS.put("diner", "Food & Dining");
        CATEGORY_KEYWORDS.put("food", "Food & Dining");
        CATEGORY_KEYWORDS.put("lunch", "Food & Dining");
        CATEGORY_KEYWORDS.put("dinner", "Food & Dining");

        // Travel
        CATEGORY_KEYWORDS.put("uber", "Travel");
        CATEGORY_KEYWORDS.put("taxi", "Travel");
        CATEGORY_KEYWORDS.put("bus", "Travel");
        CATEGORY_KEYWORDS.put("flight", "Travel");
        CATEGORY_KEYWORDS.put("hotel", "Travel");
        CATEGORY_KEYWORDS.put("gas", "Travel");
        CATEGORY_KEYWORDS.put("parking", "Travel");
        CATEGORY_KEYWORDS.put("train", "Travel");
        CATEGORY_KEYWORDS.put("airport", "Travel");
        CATEGORY_KEYWORDS.put("fuel", "Travel");

        // Utilities
        CATEGORY_KEYWORDS.put("electricity", "Utilities");
        CATEGORY_KEYWORDS.put("water", "Utilities");
        CATEGORY_KEYWORDS.put("internet", "Utilities");
        CATEGORY_KEYWORDS.put("phone", "Utilities");
        CATEGORY_KEYWORDS.put("utility", "Utilities");
        CATEGORY_KEYWORDS.put("power", "Utilities");
        CATEGORY_KEYWORDS.put("wifi", "Utilities");

        // Entertainment
        CATEGORY_KEYWORDS.put("movie", "Entertainment");
        CATEGORY_KEYWORDS.put("cinema", "Entertainment");
        CATEGORY_KEYWORDS.put("spotify", "Entertainment");
        CATEGORY_KEYWORDS.put("netflix", "Entertainment");
        CATEGORY_KEYWORDS.put("game", "Entertainment");
        CATEGORY_KEYWORDS.put("concert", "Entertainment");
        CATEGORY_KEYWORDS.put("ticket", "Entertainment");
        CATEGORY_KEYWORDS.put("shows", "Entertainment");

        // Health & Fitness
        CATEGORY_KEYWORDS.put("pharmacy", "Health & Fitness");
        CATEGORY_KEYWORDS.put("doctor", "Health & Fitness");
        CATEGORY_KEYWORDS.put("hospital", "Health & Fitness");
        CATEGORY_KEYWORDS.put("gym", "Health & Fitness");
        CATEGORY_KEYWORDS.put("medical", "Health & Fitness");
        CATEGORY_KEYWORDS.put("medicine", "Health & Fitness");
        CATEGORY_KEYWORDS.put("fitness", "Health & Fitness");

        // Shopping
        CATEGORY_KEYWORDS.put("amazon", "Shopping");
        CATEGORY_KEYWORDS.put("mall", "Shopping");
        CATEGORY_KEYWORDS.put("shop", "Shopping");
        CATEGORY_KEYWORDS.put("store", "Shopping");
        CATEGORY_KEYWORDS.put("retail", "Shopping");
        CATEGORY_KEYWORDS.put("clothing", "Shopping");
        CATEGORY_KEYWORDS.put("fashion", "Shopping");

        // Education
        CATEGORY_KEYWORDS.put("school", "Education");
        CATEGORY_KEYWORDS.put("university", "Education");
        CATEGORY_KEYWORDS.put("tuition", "Education");
        CATEGORY_KEYWORDS.put("course", "Education");
        CATEGORY_KEYWORDS.put("book", "Education");
        CATEGORY_KEYWORDS.put("education", "Education");
    }

    /**
     * Auto-categorize a transaction based on the description text.
     * Uses keyword matching to classify expenses.
     *
     * @param description Transaction description
     * @return Category name or "Other" if no match found
     */
    public String classifyCategory(String description) {
        if (description == null || description.isEmpty()) {
            return "Other";
        }

        String lowerCaseDesc = description.toLowerCase();

        // Check for keyword matches
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (lowerCaseDesc.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Other";
    }
}
