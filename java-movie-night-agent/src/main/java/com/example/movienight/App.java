package com.example.movienight;

public class App {
    public static void main(String[] args) {
        LLMClient client = new LLMClient();
        MovieNightAgent agent = new MovieNightAgent(client);
        agent.run();
    }
}