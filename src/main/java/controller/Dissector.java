package controller;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import model.Parameters;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Created by Andrew on 07/20/17.
 */
public class Dissector {
    private List<DissectorEventListener> listeners = new ArrayList<>();

    private Parameters parameters;

    public Dissector(Parameters parameters){
        this.parameters = parameters;
    }

    public void addListener(DissectorEventListener listener){
        listeners.add(listener);
    }

    private void fireEvent(List<BufferedImage> frames){
        for (DissectorEventListener listener : listeners)
            listener.onDissectionFinished(frames);
    }

    public void dissect(File file){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<BufferedImage> result = convertToJPGs(file);
                fireEvent(result);
            }

        });

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.setDaemon(true);
        t.start();
    }

    private List<BufferedImage> convertToJPGs(File file){
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
        BufferedImage frame = null;

        try {
            //start grabber
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            //if something went wrong - indicate that connection was lost or can't be opened
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<BufferedImage> frames = new ArrayList<>();

        for (int i = 0; ; i++) {
            try {
                frame = grabber.grab().getBufferedImage();


                if (i%parameters.getFrameFrequency() == 0) {
                    BufferedImage bi = new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);

                    for (int x = 0; x < frame.getWidth(); x++) {
                        for (int y = 0; y < frame.getHeight(); y++) {
                            bi.setRGB(x,y,frame.getRGB(x,y));
                        }
                    }

                    frames.add(bi);
                }

            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                try {
                    grabber.stop();
                } catch (FrameGrabber.Exception e1) {
                    e1.printStackTrace();
                } finally {
                    System.out.println("TakeNext");
                    break;
                }
            }
        }

        return frames;

    }

}
