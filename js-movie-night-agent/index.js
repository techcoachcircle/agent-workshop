import dotenv from 'dotenv';
import fs from 'fs';
import readline from 'readline';
import axios from 'axios';

dotenv.config();

class LLMClient {
    async call(prompt) {
        const response = await this.makeHttpRequestToLLM(prompt);
        console.log(response);
        return response;
    }

    async makeHttpRequestToLLM(prompt) {
        console.log("Making HTTP request to LLM...");
        console.log(`Prompt: ${prompt}`);

        const apiKey = process.env.LLM_API_KEY;
        const apiUrl = process.env.LLM_API_URL;
        const modelName = process.env.LLM_MODEL_NAME;

        if (!apiKey || !apiUrl || !modelName) {
            throw new Error("Missing required environment variables: LLM_API_KEY, LLM_API_URL, or LLM_MODEL_NAME");
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${apiKey}`
        };

        const payload = {
            model: modelName,
            messages: [
                {
                    role: 'user',
                    content: prompt
                }
            ]
        };

        try {
            const response = await axios.post(apiUrl, payload, { headers });
            console.log(`Response status code: ${response.status}`);
            return response.data.choices[0].message.content;
        } catch (error) {
            throw new Error(`API request failed: ${error.message}`);
        }
    }
}

class MovieNightAgent {
    constructor(llmClient) {
        this.llmClient = llmClient;
    }

    async run() {
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });

        rl.question("What's your mood today? ", async (mood) => {
            let movies = [];
            try {
                const data = fs.readFileSync('movies.txt', 'utf-8');
                movies = data.split('\n').map(line => line.trim()).filter(Boolean);
            } catch {
                console.log("Error: movies.txt file not found.");
                rl.close();
                return;
            }

            const prompt = `
Based on my current mood: ${mood}

Please recommend ONE movie from the following list that would best match this mood:
${movies.join(', ')}

If none of these movies fit my current mood, please explicitly say 'Nothing fits the mood'.
Explain briefly why your recommendation fits my mood.
`;

            try {
                const suggestion = await this.llmClient.call(prompt);
                console.log("\nMovie Recommendation:");
                console.log(suggestion);
            } catch (error) {
                console.error("Error during LLM call:", error.message);
            } finally {
                rl.close();
            }
        });
    }
}

const client = new LLMClient();
const agent = new MovieNightAgent(client);
agent.run();