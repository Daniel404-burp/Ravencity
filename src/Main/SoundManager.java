package Main;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    public static void play(String fileName){

        try{

            URL url = SoundManager.class.getResource(
                    "/assets/sounds/" + fileName);

            if(url == null){
                System.out.println("Missing sound: " + fileName);
                return;
            }

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(url);

            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            clip.addLineListener(event -> {
                if(event.getType() == LineEvent.Type.STOP){
                    clip.close();
                    try{
                        audio.close();
                    }catch(Exception ignored){}
                }
            });

            clip.start();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
