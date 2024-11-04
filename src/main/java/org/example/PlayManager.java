package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class PlayManager {
    private static PlayManager instance;

    private final Map<Long, MusicBot> musicmanager;
    private final AudioPlayerManager audioplayerManager;

    public PlayManager() {
        this.musicmanager = new HashMap<>();
        this.audioplayerManager = new DefaultAudioPlayerManager();
        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true);

        this.audioplayerManager.registerSourceManager(yt);
        AudioSourceManagers.registerRemoteSources(this.audioplayerManager);
    }

    public MusicBot getMusicManager(Guild guild) {
        return this.musicmanager.computeIfAbsent(guild.getIdLong(), guildId -> {
            final MusicBot musicBot = new MusicBot(this.audioplayerManager);
            guild.getAudioManager().setSendingHandler(musicBot.getSendHandler());
            return musicBot;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackUrl) {

        final MusicBot musicBot = this.getMusicManager(textChannel.getGuild());

        this.audioplayerManager.loadItemOrdered(musicBot, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicBot.trackScheduler.queue(audioTrack);
                textChannel.sendMessage("加入隊列!").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    musicBot.trackScheduler.queue(track);
                }
                textChannel.sendMessage("播放清單已加入播放隊列！").queue();
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("未找到匹配的音樂，請檢查URL是否正確。").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                textChannel.sendMessage("音樂加載失敗：" + e.getMessage()).queue();
                e.printStackTrace();
            }
        });
    }

    public static PlayManager getInstance() {
        if (instance == null) {
            instance = new PlayManager();
        }
        return instance;
    }
}
