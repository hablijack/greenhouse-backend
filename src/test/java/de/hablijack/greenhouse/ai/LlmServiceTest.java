package de.hablijack.greenhouse.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.llm.LlmClient;
import de.hablijack.greenhouse.ai.llm.LlmService;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionResponse;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionResponse.Choice;
import de.hablijack.greenhouse.ai.llm.dto.ChatMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  AiConfig config;

  @Mock
  LlmClient llmClient;

  ObjectMapper objectMapper = new ObjectMapper();

  private LlmService llmService;

  @BeforeEach
  void setUp() {
    llmService = new LlmService(llmClient, config, objectMapper);
  }

  @Test
  void testChatSuccess() {
    ChatCompletionResponse mockResponse = new ChatCompletionResponse();
    Choice choice = new Choice();
    choice.message = ChatMessage.assistant("This is a test response");
    mockResponse.choices = List.of(choice);

    when(llmClient.chat(any())).thenReturn(mockResponse);

    String result = llmService.chat("system prompt", "user prompt");
    assertEquals("This is a test response", result);
  }

  @Test
  void testChatWithRetry() {
    when(config.llm().maxRetries()).thenReturn(1);

    ChatCompletionResponse mockResponse = new ChatCompletionResponse();
    Choice choice = new Choice();
    choice.message = ChatMessage.assistant("Response after retry");
    mockResponse.choices = List.of(choice);

    when(llmClient.chat(any()))
        .thenThrow(new RuntimeException("Temporary error"))
        .thenReturn(mockResponse);

    String result = llmService.chat("system", "user");
    assertEquals("Response after retry", result);
    verify(llmClient, times(2)).chat(any());
  }
}
