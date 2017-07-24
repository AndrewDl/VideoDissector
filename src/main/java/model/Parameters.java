package model;

import java.io.File;
import java.io.IOException;

/**
 * Created by Andrew on 07/19/17.
 */
public class Parameters {
    private static Parameters instance = null;

    private Parameters(){}

    private String filePath = null;
    private String object = null;
    private String location = null;
    private int frameFrequency = 1;
    private String FTPaddress = null;
    private String user = null;
    private String pass = null;
    private String fileFormat = "mp4";

    public static Parameters getInstance(){

        if (instance == null){

            File settingsFile = new File("config/config.cfg");
            if (settingsFile.exists()){
                XMLwriterReader<Parameters> reader = new XMLwriterReader(settingsFile.toString());
                try {
                    instance = reader.ReadFile(Parameters.class);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                instance = new Parameters();
            }

        }

        return instance;
    }

    public void save(){
        XMLwriterReader<Parameters> writer = new XMLwriterReader("config/config.cfg");
        try {
            writer.WriteFile(instance,Parameters.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        if (filePath!=null)
            this.filePath = filePath;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        if (object!=null)
            this.object = object;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location!=null)
            this.location = location;
    }

    public int getFrameFrequency() {
        return frameFrequency;
    }

    public void setFrameFrequency(int frameFrequency) {
        this.frameFrequency = frameFrequency;
    }

    public String getFTPaddress() {
        return FTPaddress;
    }

    public void setFTPaddress(String FTPaddress) {
        if (FTPaddress!=null)
            this.FTPaddress = FTPaddress;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        if (user!=null)
            this.user = user;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
