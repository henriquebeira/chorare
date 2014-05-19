/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import net.v2.start.Main;

/**
 *
 * @author User
 */
public class Client extends Thread {

    private Main main;
    private ServerSocket sSocket;

    public Client(Main main) {
        try {
            this.main = main;

            sSocket = new ServerSocket(0, 0, InetAddress.getLocalHost());

            sSocket.setSoTimeout(3000);
            
            start();

        } catch (UnknownHostException ex) {
            System.err.println("Client - Host: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Cleint - IO: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        while(!interrupted()){
            try {
                System.out.println("Waiting request");
                Socket sReceive = sSocket.accept();
                
                new TransferFile(main, sReceive);
                
            } catch(SocketTimeoutException tEx){
            }
            catch (IOException ex) {
                System.err.println("Client - IO: " + ex.getMessage());
            }
        }
    }

    public void sendFileList() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(300);
                } catch (InterruptedException ex) {
                }

                try {
                    File folder = main.getFilesFolder();
                    Socket socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());

                    DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());

                    sOut.writeUTF(Main.LIST_UTF);
                    sOut.writeUTF(main.getNickName());

                    System.out.println("Sending my List!");

                    String data = main.getNickName() + ":" + sSocket.getInetAddress().getHostAddress() + ":" + sSocket.getLocalPort() + ":";

                    for (File f : folder.listFiles()) {
                        if (!f.getName().equals("controle")) {

                            sOut.writeUTF(data.concat(f.getName()).concat(":"));

                        }
                    }

                } catch (IOException ex) {
                    System.err.println("Client - SendFile : " + ex.getMessage());
                }
            }
        }.start();
    }

    public void search(String search) {
        new Search(main, search);
    }

    public void requestFileFromPeer(String peerNick, String peerIP, Integer peerPort, String file) {
        new RequestFile(main, peerNick, peerIP, peerPort, file);
    }
}



class RequestFile extends Thread{
    private Main main;
    private InetAddress ip;
    private Integer port;
    private String file;
    private String nick;

    public RequestFile(Main main, String peerNick, String ip, Integer port, String file) {
        try {
            this.main = main;
            this.ip = InetAddress.getByName(ip);
            this.port = port;
            this.file = file;
            this.nick = peerNick;
            
            start();
        } catch (UnknownHostException ex) {
        }
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, port);
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            out.writeUTF(file);
            
            String fileRec = in.readUTF();
            
            if(!fileRec.equals("--1")){
                FileOutputStream fOut = new FileOutputStream(main.getFilesFolder() + File.separator + fileRec);
                
                byte[] buff = new byte[4096];
                
                while(true){
                    int len = in.read(buff);
                    
                    if(len == -1){
                        break;
                    }
                    
                    fOut.write(buff, 0, len);
                }
                
                main.getGui().warnCompletedDownload(file, nick, new File(main.getFilesFolder() + File.separator + file));
            }else{
                
            }
            
        } catch (IOException ex) {
            System.err.println("Client#RequestFile - IO: " + ex.getMessage());
        }
    }
}

class Search extends Thread {

    private Main main;
    private String search;
    private ValidateSignature validate;

    public Search(Main main, String search) {
        this.main = main;
        this.search = search;

        validate = new ValidateSignature(main);

        start();
    }

    @Override
    public void run() {
        try {
            File configFolder = main.getConfigFolder();

            Socket socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());

            DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());
            DataInputStream sIn = new DataInputStream(socket.getInputStream());

            sOut.writeUTF(Main.SEARCH_UTF);

            boolean hasPublicKey = false;

            for (File f : configFolder.listFiles()) {
                if (f.getName().equals("public-key")) {
                    hasPublicKey = true;
                }
            }

            if (!hasPublicKey) {
                sOut.writeUTF("Y_PK");

                String incoming = sIn.readUTF();

                System.out.println("File: " + incoming);

                FileOutputStream fOut = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

                byte[] buf = new byte[4096];
                int i = 1;
                long auxR = 0;
                long totalRead = sIn.readLong();
                
                System.out.println("File SIze: " + totalRead);

                while (true) {
                    int len = sIn.read(buf);
                    if (len == -1) {
                        break;
                    }
                    
                    fOut.write(buf, 0, len);

                    auxR += len;
                    
                    if(auxR >= totalRead){
                        break;
                    }
                }

                fOut.close();

            } else {
                sOut.writeUTF("N_PK");
            }

            sOut.writeUTF(search);

            String incoming = sIn.readUTF();

            System.out.println("File: " + incoming);

            if (incoming.equals("--1")) {
                System.out.println("No search could be done.");
                main.getGui().receiveSearchResponse(new Object[0][0], search);
                return;
            } else {
                System.out.println("Search done.");
            }

            FileOutputStream fOutSearch = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

            byte[] buf = new byte[4096];
            int i = 1;
            long auxR = 0;
            long totalRead = sIn.readLong();

            while (true) {
                int len = sIn.read(buf);
                    if (len == -1) {
                        break;
                    }
                    fOutSearch.write(buf, 0, len);

                    auxR += len;
                    
                    if(auxR >= totalRead){
                        break;
                    }
            }

            fOutSearch.close();

            incoming = sIn.readUTF();

            System.out.println("File: " + incoming);

            fOutSearch = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

            buf = new byte[4096];
            i = 1;

            auxR = 0;
            totalRead = sIn.readLong();

            while (true) {
                int len = sIn.read(buf);
                    if (len == -1) {
                        break;
                    }
                    fOutSearch.write(buf, 0, len);

                    auxR += len;
                    
                    if(auxR >= totalRead){
                        break;
                    }
            }

            fOutSearch.close();

            if (validate.validate(search)) {
                System.out.println("Tracker validated!");

                Scanner sc = new Scanner(new File(configFolder.getPath() + File.separator + search + "-quemTem.txt"));

                ArrayList<AuxData> list = new ArrayList<>();

                while (sc.hasNext()) {
                    String[] buff = sc.nextLine().split(":");

                    list.add(new AuxData().setNick(buff[0]).setFileName(buff[1]).setAddress(buff[2]).setPort(Integer.parseInt(buff[3])));
                }

                Object[][] data = new Object[list.size()][4];

                int k = 0;
                for (AuxData a : list) {
                    data[k][0] = a.getFileName();
                    data[k][1] = a.getNick();
                    data[k][2] = a.getAddress();
                    data[k][3] = a.getPort();

                    k++;
                }

                main.getGui().receiveSearchResponse(data, search);
            }


            new File(configFolder.getPath() + File.separator + search + "-sign").delete();
            new File(configFolder.getPath() + File.separator + search + "-quemTem.txt").delete();

        } catch (IOException ex) {
            System.err.println("Client#Search - IO: " + ex.getMessage());
        }
    }
}

class AuxData {

    private String address;
    private Integer port;
    private String nick;
    private String fileName;

    public String getAddress() {
        return address;
    }

    public AuxData setAddress(String address) {
        this.address = address;

        return this;
    }

    public Integer getPort() {
        return port;
    }

    public AuxData setPort(Integer port) {
        this.port = port;

        return this;
    }

    public String getNick() {
        return nick;
    }

    public AuxData setNick(String nick) {
        this.nick = nick;

        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public AuxData setFileName(String fileName) {
        this.fileName = fileName;

        return this;
    }
}