package controller;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.*;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import model.Parameters;
import model.XMLwriterReader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private Label labelInfo;
    @FXML
    private TextField textFieldPath;
    @FXML
    private TextField textFieldObject;
    @FXML
    private TextField textFieldLocation;
    @FXML
    private TextField textFieldFrameRate;

    @FXML
    private TextField textFieldFTP;
    @FXML
    private TextField textFieldUser;
    @FXML
    private PasswordField textFieldPass;

    @FXML
    private ProgressBar progressBarProcessed;
    @FXML
    private Button buttonStart;
    @FXML
    private CheckBox checkBoxFTP;

    private Stage stage = Main.getPrimaryStage();

    //private String objectName;
    //private String location;
    //private File file = null;
    private Parameters parameters = Parameters.getInstance();
    private List<File> filesToConvert = null;

    private Random r = new Random();

    int totalFiles = 0;
    int processedFiles = 0;

    public void initialize(URL location, ResourceBundle resources) {


        FTPClient client = new FTPClient();


/*
        if (!dirExists("/frames")) {
            boolean created = client.makeDirectory("/frames");
            if (created){
                System.out.println("dirCreated");
            }
        }
*/


        File folder = new File("frames/");
        if (!folder.exists()) folder.mkdirs();

        //Delete frames that are older than 2 days
        File[] frames = folder.listFiles();
        long currentTime = System.currentTimeMillis();
        for (File f : frames){
            Path p = Paths.get(f.getAbsoluteFile().toURI());
            BasicFileAttributes attributes = null;

            try {
                attributes = Files.readAttributes(p,BasicFileAttributes.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            long fileTime = attributes.lastModifiedTime().toMillis();

            if ( currentTime - fileTime > 86400000) f.delete();
        }

        //if there is something in parameters - put it on the view
        if (parameters.getFilePath()!=null) textFieldPath.setText( parameters.getFilePath() );
        if (parameters.getObject()!=null) textFieldObject.setText( parameters.getObject() );
        if (parameters.getLocation()!=null) textFieldLocation.setText( parameters.getLocation() );
        textFieldFrameRate.setText( String.valueOf( parameters.getFrameFrequency() ) );

        if (parameters.getFTPaddress()!=null) textFieldFTP.setText( parameters.getFTPaddress() );
        if (parameters.getUser()!=null) textFieldUser.setText( parameters.getUser() );
        if (parameters.getPass()!=null) textFieldPass.setText( parameters.getPass() );

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                parameters.save();
            }
        });

    }

    public void onStart(ActionEvent actionEvent) {



        buttonStart.setDisable(true);

        if (processedFiles == totalFiles) {
            processedFiles = 0;
            progressBarProcessed.setProgress((float)processedFiles/totalFiles);
        }

        parameters.setFilePath(textFieldPath.getText());
        parameters.setObject(textFieldObject.getText());
        parameters.setLocation(textFieldLocation.getText());

        if(textFieldFrameRate.getText() != null)
            parameters.setFrameFrequency(Integer.valueOf(textFieldFrameRate.getText()));

        parameters.setFTPaddress(textFieldFTP.getText());
        parameters.setUser(textFieldUser.getText());
        parameters.setPass(textFieldPass.getText());




        if (textFieldPath.getText().equals("") ||
                textFieldObject.getText().equals("") ||
                textFieldLocation.getText().equals("") ||
                textFieldFrameRate.getText().equals("") ||
                textFieldFTP.getText().equals("") ||
                textFieldUser.getText().equals("")||
                textFieldPass.getText().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"All fields should be filled!");
            alert.setHeaderText("Some fields are empty!");
            alert.show();
            buttonStart.setDisable(false);
            return;
        }


        //check ability to connect to FTP
        FTPClient client = new FTPClient();

        try {
            client.connect(parameters.getFTPaddress());
            client.user(parameters.getUser());
            client.pass(parameters.getPass());

            if (client.getReplyCode() != 230){
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Please, check user or password");
                alert.setHeaderText("Problem with FTP connection");
                alert.show();

                client.disconnect();
                buttonStart.setDisable(false);
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File file = new File(parameters.getFilePath());

        filesToConvert = findFile(file);
        totalFiles = filesToConvert.size();

        labelInfo.setText( processedFiles + " of " + totalFiles);

        Dissector dissector = new Dissector(parameters);
        dissector.addListener(new DissectorEventListener() {
            @Override
            public void onDissectionFinished(List<BufferedImage> frames) {

                //save frames
                ISaver saver = new FileSaver();
                //aBMTQlb4e4
                ISaver ftpSaver = null;
                try {
                    ftpSaver = new FTPSaver(parameters.getFTPaddress(),parameters.getUser(),textFieldPass.getText());
                } catch (IOException e) {
                    e.printStackTrace();

                    saver.close();
                    ftpSaver.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"Please, check FTP address");
                    alert.setHeaderText("Problem with FTP connection");
                    alert.show();

                    buttonStart.setDisable(false);

                    return;
                }
                for(BufferedImage frame : frames) {
                    try {
                        Path p = Paths.get(file.getAbsoluteFile().toURI());
                        BasicFileAttributes attributes = null;

                        attributes = Files.readAttributes(p,BasicFileAttributes.class);

                        Date fileDate = new Date(attributes.lastModifiedTime().toMillis());
                        DateFormat fileDateFormat = new SimpleDateFormat("yy-MM-dd_hh-mm-ss");

                        String timeStamp = fileDateFormat.format(fileDate);

                        String ouptutFileName = "frames/" +
                                parameters.getObject() + "_" +
                                parameters.getLocation() + "_" +
                                timeStamp + "_" +
                                System.currentTimeMillis() + "_" +
                                r.nextInt(1000) + ".jpg";

                        saver.save(frame,ouptutFileName);

                        if (checkBoxFTP.isSelected())
                            ftpSaver.save(frame,ouptutFileName);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                processedFiles++;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        labelInfo.setText( processedFiles + " of " + totalFiles);
                        progressBarProcessed.setProgress((float)processedFiles/totalFiles);
                    }
                });

                if (processedFiles < totalFiles) {
                    dissector.dissect(filesToConvert.get(processedFiles));
                    buttonStart.setDisable(false);
                }
                if (processedFiles == totalFiles){
                    ftpSaver.close();
                    saver.close();
                    buttonStart.setDisable(false);
                }

            }
        });
        dissector.dissect(filesToConvert.get(processedFiles));

    }

    private List<File> findFile (File file){
        List<File> result = new ArrayList<>();

        if (file.isFile()) {
            if (validate(file.getName())) result.add(file);
            //return result;
        } else {
            File[] files = file.listFiles();

            if (files != null)
                for (File f : files){
                    result.addAll(findFile(f));
                }
        }

        return result;
    }

    private boolean validate(String filename){
        return filename.matches(".*[.]" + parameters.getFileFormat());
    }

    public void onChooseFile(ActionEvent actionEvent) {

        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Open Resource File");

        String lastDir = parameters.getFilePath();

        //path validation
        if ( lastDir!=null && !lastDir.equals("") && new File(lastDir).exists() )
            folderChooser.setInitialDirectory(new File(parameters.getFilePath()));
        else
            folderChooser.setInitialDirectory(new File("/"));

        File file = folderChooser.showDialog(stage);

        if (file != null) {
            parameters.setFilePath(file.getPath());
            textFieldPath.setText(file.getPath());
        }
    }


}
