import os
import requests
import json
from dotenv import load_dotenv
load_dotenv()

class LLMClient:

    def call(self, prompt: str) -> str:
        response = self.make_http_request_to_llm(prompt)
        print(response)
        return response
    
    def make_http_request_to_llm(self, prompt):
        print("Making HTTP request to LLM...")
        print(f"Prompt: {prompt}")

        # Get configuration from environment variables
        api_key = os.environ.get("LLM_API_KEY")
        api_url = os.environ.get("LLM_API_URL")
        model_name = os.environ.get("LLM_MODEL_NAME")

        # Validate required environment variables
        if not api_key or not api_url or not model_name:
            raise ValueError("Missing required environment variables: OPENAI_API_KEY, OPENAI_API_URL, or OPENAI_MODEL_NAME")

        # Prepare headers with authentication
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}"
        }

        # Prepare request payload
        payload = {
            "model": model_name,
            "messages": [
                {
                    "role": "user",
                    "content": prompt
                }
            ]
        }

        # Make the API request
        try:
            response = requests.post(api_url, headers=headers, data=json.dumps(payload))
            response.raise_for_status()  # Raise exception for HTTP errors
            print(f"Response status code: {response.status_code}")
            # Parse and return the response
            response_json = response.json()
            return_response = response_json["choices"][0]["message"]["content"]
            # print(return_response)
            return return_response;
        except requests.exceptions.RequestException as e:
            raise Exception(f"API request failed: {str(e)}")
        

class MovieNightAgent:
    def __init__(self, llm_client: LLMClient):
        self.llm_client = llm_client

    def run(self):
        # 1. Input a mood from the user in the CLI
        mood = input("What's your mood today? ")
        
        # 2. Read movies.txt file and store in a list
        movies = []
        try:
            with open("movies.txt", "r") as file:
                movies = [line.strip() for line in file if line.strip()]
        except FileNotFoundError:
            print("Error: movies.txt file not found.")
            return
        
        # 3. Create a prompt asking for movie recommendation
        prompt = f"""
        Based on my current mood: {mood}
        
        Please recommend ONE movie from the following list that would best match this mood:
        {', '.join(movies)}
        
        If none of these movies fit my current mood, please explicitly say 'Nothing fits the mood'.
        Explain briefly why your recommendation fits my mood.
        """
        
        # 4. Pass the prompt to LLM and get response
        suggestion = self.llm_client.call(prompt)
        
        # 5. Print the response
        print("\nMovie Recommendation:")
        print(suggestion)


if __name__ == "__main__":
    # Initialize LLM client
    llm_client = LLMClient()
    
    # Create and run the Movie Night Agent
    agent = MovieNightAgent(llm_client)
    agent.run()
        