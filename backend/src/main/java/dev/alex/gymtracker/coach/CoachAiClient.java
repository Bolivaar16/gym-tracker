package dev.alex.gymtracker.coach;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import dev.alex.gymtracker.config.AppProperties;
import org.springframework.stereotype.Component;

@Component
public class CoachAiClient {

    static final String MODEL = "claude-haiku-4-5-20251001";
    static final long MAX_TOKENS = 512;

    private final AnthropicClient client;

    public CoachAiClient(AppProperties props) {
        String key = props.anthropicApiKey();
        this.client = (key == null || key.isBlank())
                ? null
                : AnthropicOkHttpClient.builder().apiKey(key).build();
    }

    public boolean isConfigured() {
        return client != null;
    }

    public String ask(String systemPrompt, String userMessage) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(MAX_TOKENS)
                .system(systemPrompt)
                .addUserMessage(userMessage)
                .build();
        Message message = client.messages().create(params);
        // ContentBlock.text() returns Optional<TextBlock>; TextBlock.text() returns String
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(tb -> tb.text())
                .reduce("", String::concat);
    }
}
