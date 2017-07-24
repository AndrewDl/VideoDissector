package controller;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by Andrew on 07/21/17.
 */
public interface DissectorEventListener {
    void onDissectionFinished(List<BufferedImage> frames);
}
