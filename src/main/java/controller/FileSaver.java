package controller;

import model.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Andrew on 07/20/17.
 */
public class FileSaver implements ISaver{


    public FileSaver(){
    }

    @Override
    public boolean save(BufferedImage frame, String fileName) throws IOException {
//        Path p = Paths.get(file.getAbsoluteFile().toURI());
//        BasicFileAttributes attributes = null;
//
//        attributes = Files.readAttributes(p,BasicFileAttributes.class);
//
//        Date fileDate = new Date(attributes.lastModifiedTime().toMillis());
//        DateFormat fileDateFormat = new SimpleDateFormat("yy-MM-dd_hh-mm-ss");
//
//        String timeStamp = fileDateFormat.format(fileDate);
//
//        File ouptutFile = new File("frames/" +
//                parameters.getObject() + "_" +
//                parameters.getLocation() + "_" +
//                timeStamp + "_" +
//                System.currentTimeMillis() + "_" +
//                r.nextInt(1000) + ".jpg");

        ImageIO.write(frame, "jpg", new File(fileName));
        System.out.println(fileName);
        return false;
    }

    @Override
    public void close() {

    }
}
