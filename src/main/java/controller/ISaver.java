package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Andrew on 07/20/17.
 */
public interface ISaver {
    boolean save(BufferedImage bufferedImage, String fileName) throws IOException;
    void close();
}
