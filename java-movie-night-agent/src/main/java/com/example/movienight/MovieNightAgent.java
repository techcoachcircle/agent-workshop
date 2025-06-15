package com.example.movienight;

import java.nio.file.*;
import java.util.*;
import java.io.*;

public class MovieNightAgent {

    private final LLMClient llmClient;

    public MovieNightAgent(LLMClient client) {
        this.llmClient = client;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("What's your mood today? ");
        String mood = scanner.nextLine();

        List<String> movies;
        try {
            movies = Files.readAllLines(Paths.get("movies.txt"));
        } catch (IOException e) {
            System.out.println("Error: movies.txt file not found.");
            return;
        }

        movies.removeIf(String::isBlank);

        String prompt = String.format(
            "Based on my current mood: %s\n\n" +
            "Please recommend ONE movie from the following list that would best match this mood:\n%s\n\n" +
            "If none of these movies fit my current mood, please explicitly say 'Nothing fits the mood'. " +
            "Explain briefly why your recommendation fits my mood.",
            mood, String.join(", ", movies));

        try {
            String suggestion = llmClient.call(prompt);
            System.out.println("\nMovie Recommendation:");
            System.out.println(suggestion);
        } catch (Exception e) {
            System.err.println("Failed to get movie suggestion: " + e.getMessage());
        }
    }
}