package controller;

import model.Parameters;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Andrew on 07/20/17.
 */
public class FTPSaver implements ISaver{

    private String FTPaddress;
    private String User;
    private String Pass;

    FTPClient client = new FTPClient();

    public FTPSaver(String FTPaddress, String User, String Pass) throws IOException {
        this.FTPaddress = FTPaddress;
        this.User = User;
        this.Pass = Pass;

//            client.connect("nl.cifr.us");
//            client.user("admin_gender2c");
//            client.pass("aBMTQlb4e4");
        client.connect(FTPaddress);
        client.user(User);
        client.pass(Pass);

    }

    @Override
    public boolean save(BufferedImage bufferedImage, String fileName) throws IOException {

        try {

            int reply = client.getReplyCode();
            if ((reply != 230)&&(reply != 226))
                throw new IOException("Error with FTP code: " + reply);

            if (!dirExists("/frames")) {
                boolean created = client.makeDirectory("/frames");
                if (created){
                    System.out.println("dirCreated");
                }
            }

            client.changeWorkingDirectory("/");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());

            client.setFileType(client.BINARY_FILE_TYPE);
            client.enterLocalPassiveMode();
            //
            // Store file to server
            //
            client.storeFile(fileName, is);
            System.out.println(client.getReplyCode());
            //client.logout();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            try {
//                client.disconnect();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        return false;
    }

    @Override
    public void close() {
        try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean dirExists(String dirPath) throws IOException {
        client.changeWorkingDirectory(dirPath);
        int returnCode = client.getReplyCode();
        if (returnCode == 550) {
            return false;
        }
        return true;
    }
}
