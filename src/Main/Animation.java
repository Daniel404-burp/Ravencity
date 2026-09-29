package Main;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Animation {

    // Animation frames
    private ArrayList<BufferedImage> frames;

    // Duration of each frame
    private ArrayList<Long> durations;

    // Current frame
    private int currentFrame;

    // Time passed
    private long animTime;

    // Total animation duration
    private long totalDuration;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Animation() {

        frames = new ArrayList<BufferedImage>();

        durations = new ArrayList<Long>();

        currentFrame = 0;

        animTime = 0;

        totalDuration = 0;
    }


    // =========================
    // ADD FRAME
    // =========================

    public synchronized void addFrame(
            BufferedImage image,
            long duration) {

        totalDuration += duration;

        frames.add(image);

        durations.add(duration);
    }


    // =========================
    // START
    // =========================

    public synchronized void start() {

        animTime = 0;

        currentFrame = 0;
    }


    // =========================
    // STOP
    // =========================

    public synchronized void stop() {

        animTime = 0;

        currentFrame = 0;
    }


    // =========================
    // UPDATE
    // =========================

    public synchronized void update(long elapsedTime) {

        if (frames.size() <= 1) {

            return;
        }

        animTime += elapsedTime;

        if (animTime >= totalDuration) {

            animTime %= totalDuration;

            currentFrame = 0;
        }

        while (
            animTime > getFrameEndTime(currentFrame)
        ) {

            currentFrame++;

            if (currentFrame >= frames.size()) {

                currentFrame = 0;
            }
        }
    }


    // =========================
    // GET CURRENT IMAGE
    // =========================

    public synchronized BufferedImage getImage() {

        if (frames.size() == 0) {

            return null;
        }

        return frames.get(currentFrame);
    }


    // =========================
    // GET CURRENT FRAME
    // =========================

    public synchronized int getCurrentFrame() {

        return currentFrame;
    }


    // =========================
    // GET FRAME COUNT
    // =========================

    public synchronized int getFrameCount() {

        return frames.size();
    }


    // =========================
    // FRAME END TIME
    // =========================

    private long getFrameEndTime(int frame) {

        long time = 0;

        for (int i = 0; i <= frame; i++) {

            time += durations.get(i);
        }

        return time;
    }
}