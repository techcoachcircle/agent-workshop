using dotenv.net;
using Microsoft.Extensions.Configuration;
using System;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

class LLMClient
{
    private readonly string _apiKey;
    private readonly string _apiUrl;
    private readonly string _modelName;
    private readonly HttpClient _httpClient;

    public LLMClient(IConfiguration config)
    {
        _apiKey = config["LLM_API_KEY"];
        _apiUrl = config["LLM_API_URL"];
        _modelName = config["LLM_MODEL_NAME"];
        _httpClient = new HttpClient();

        if (string.IsNullOrEmpty(_apiKey) || string.IsNullOrEmpty(_apiUrl) || string.IsNullOrEmpty(_modelName))
        {
            throw new Exception("Missing required environment variables: LLM_API_KEY, LLM_API_URL, or LLM_MODEL_NAME");
        }
    }

    public async Task<string> CallAsync(string prompt)
    {
        Console.WriteLine("Making HTTP request to LLM...");
        Console.WriteLine($"Prompt: {prompt}");

        var requestBody = new
        {
            model = _modelName,
            messages = new[]
            {
                new { role = "user", content = prompt }
            }
        };

        var request = new HttpRequestMessage(HttpMethod.Post, _apiUrl);
        request.Headers.Add("Authorization", $"Bearer {_apiKey}");
        request.Content = new StringContent(JsonSerializer.Serialize(requestBody), Encoding.UTF8, "application/json");

        var response = await _httpClient.SendAsync(request);
        response.EnsureSuccessStatusCode();

        var responseJson = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        return responseJson.RootElement.GetProperty("choices")[0]
                                       .GetProperty("message")
                                       .GetProperty("content")
                                       .GetString();
    }
}

class MovieNightAgent
{
    private readonly LLMClient _llmClient;

    public MovieNightAgent(LLMClient llmClient)
    {
        _llmClient = llmClient;
    }

    public async Task RunAsync()
    {
        Console.Write("What's your mood today? ");
        string mood = Console.ReadLine();

        string[] movies;
        try
        {
            movies = File.ReadAllLines("movies.txt");
        }
        catch (FileNotFoundException)
        {
            Console.WriteLine("Error: movies.txt file not found.");
            return;
        }

        string prompt = $@"
Based on my current mood: {mood}

Please recommend ONE movie from the following list that would best match this mood:
{string.Join(", ", movies)}

If none of these movies fit my current mood, please explicitly say 'Nothing fits the mood'.
Explain briefly why your recommendation fits my mood.
";

        var suggestion = await _llmClient.CallAsync(prompt);
        Console.WriteLine("\nMovie Recommendation:");
        Console.WriteLine(suggestion);
    }
}

class Program
{
    static async Task Main(string[] args)
    {
        DotEnv.Load();

        var config = new ConfigurationBuilder()
            .AddEnvironmentVariables()
            .Build();

        var llmClient = new LLMClient(config);
        var agent = new MovieNightAgent(llmClient);

        await agent.RunAsync();
    }
}