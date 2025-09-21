package controller;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static Map<String, Clip> sounds = new HashMap<>();
    private static float globalVolume = 1.0f;
    private static long pausePosition = 0;

    public static void setGlobalVolume(float volume) {
        globalVolume = volume;
        for (Clip clip : sounds.values()) {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
        }
    }

    public static void loadSound(String name, String path) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
            AudioFormat baseFormat = audioInputStream.getFormat();
            AudioFormat decodeFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED ,
                    baseFormat.getSampleRate() ,16
                    , baseFormat.getChannels() ,baseFormat.getChannels()*2
                    , baseFormat.getSampleRate() ,false);
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat , audioInputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(dais);
            sounds.put(name, clip);
        } catch (Exception e) {
            System.err.println("error :" + e.getMessage());
        }
    }

    public static void playSound(String name) {
        Clip clip = sounds.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
    public static void pauseClip() {
        for (Clip clip : sounds.values()) {
            if (clip != null && clip.isRunning()) {
                pausePosition = clip.getMicrosecondPosition();
                clip.stop();
            }
        }
    }

    public static void resumeClip() {
        for (Clip clip : sounds.values()) {
            if (clip != null && !clip.isRunning()) {
                clip.setMicrosecondPosition(pausePosition);
                clip.start();
            }
        }
    }

    public static void stopClip() {
        for (Clip clip : sounds.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.flush();
                clip.setFramePosition(0);
                pausePosition = 0;
            }
        }
    }
}