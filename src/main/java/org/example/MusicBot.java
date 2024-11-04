package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class MusicBot {
    public final TrackScheduler trackScheduler;
    public final AudioPlayer audioplayer;

    private final AudioPlayerSendHandler sendhendler;


    public MusicBot(AudioPlayerManager playerManager) {
        this.audioplayer = playerManager.createPlayer();
        this.trackScheduler = new TrackScheduler(audioplayer);
        this.audioplayer.addListener(this.trackScheduler);
        this.sendhendler = new AudioPlayerSendHandler(this.audioplayer);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return this.sendhendler;
    }

}