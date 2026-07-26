package gdd;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

    // Store current position and state
    Long currentFrame;
    Clip clip;
    String status;

    AudioInputStream audioInputStream;
    String filePath;

    // Constructor to initialize audio streams and clip
    public AudioPlayer(String filePath)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        
        this.filePath = filePath;
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // Create clip reference and open audio stream
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        // Loop continuous background music by default
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    // Method to play the audio
    public void play() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        status = "play";
    }

    // Method to pause the audio
    public void pause() {
        if ("paused".equals(status)) {
            return;
        }
        this.currentFrame = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    // Method to resume the audio
    public void resumeAudio() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        if ("play".equals(status)) {
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        this.play();
    }

    // Method to restart the audio from the beginning
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }

    // Method to stop and close the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        currentFrame = 0L;
        clip.stop();
        clip.close();
    }

    // Method to jump to a specific microsecond timestamp
    public void jump(long c) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            this.play();
        }
    }

    // Method to reset the audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(
                new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void playSoundEffect(String filePath) {
        new Thread(() -> {
            try {
                File audioFile = new File(filePath).getAbsoluteFile();
                if (!audioFile.exists()) {
                    System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
                    return;
                }
                AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
                Clip sfxClip = AudioSystem.getClip();
                sfxClip.open(ais);
                sfxClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}