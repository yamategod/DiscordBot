package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiscordBot extends ListenerAdapter {
    private final AudioPlayer player;

    public DiscordBot() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        this.player = playerManager.createPlayer();
        String token = "MTI5ODU1Njg0MjE1ODU4NzkwNA.GdF9ZW.T0Cqe28gFGxtingmXcbgkeSz5Nq_aolwiNl_H4";
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.addEventListeners(this);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        jdaBuilder.build();
    }

    public static void main(String[] args) {
        new DiscordBot();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String message = event.getMessage().getContentRaw();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (message.startsWith("!")) {
            String[] command = message.split(" ");
            if (command.length > 1) {
                connectToVoiceChannel(event.getGuild());
                System.out.println(command[1]);
                PlayManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), command[1]);
            } else {
                System.out.println("No arguments found in the message");
            }
        }
        if(message.startsWith("&")){
            String[] command = message.split(" ");
            if (command.length > 1) {
                System.out.println(command[1]);
                String response = getChatGPTResponse(command[1]);
                event.getChannel().sendMessage(response).queue();

            } else {
                System.out.println("No arguments found in the message");
            }
        }
    }

    private String getChatGPTResponse(String message) {
        try {
            // 配置OpenAI API請求
            String apiKey = "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";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{ \"model\": \"gpt-3.5-turbo\", \"prompt\": \"" + message + "\", \"max_tokens\": 100 }"))
                    .build();

            // 取得OpenAI API的回應
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // 檢查回應內容
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                String reply = root.get("choices").get(0).get("text").asText();
                return reply.trim();
            } else {
                System.err.println("Response format is unexpected: " + responseBody);
                return "抱歉，我無法處理您的請求。";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我無法處理您的請求。";
        }
    }


    public void connectToVoiceChannel(Guild guild) {
        if (guild.getVoiceChannels().isEmpty()) {
            System.out.println("没有可用的语音频道");
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(guild.getVoiceChannels().get(0));  // 加入第一个语音频道
        audioManager.setSendingHandler(new AudioPlayerSendHandler(player)); // 设置音频发送处理器
    }
}