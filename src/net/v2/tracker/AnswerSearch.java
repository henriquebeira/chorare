/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.tracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import net.v2.start.Main;

/**
 *
 * @author User
 */
public class AnswerSearch extends Thread {

    private Main main;
    private Socket socket;
    private DataInputStream input;
    private SignatureGenerator sign;

    public AnswerSearch(Main main, Socket socket, DataInputStream input) {
        this.main = main;
        this.socket = socket;
        this.input = input;

        sign = new SignatureGenerator(main);

        start();
    }

    @Override
    public void run() {
        try {
            String needPublicKey = input.readUTF();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            FileInputStream fis;
            byte[] buf;

            if (needPublicKey.equals("Y_PK")) {
                System.out.println("He needs PK");

                out.writeUTF("public-key");
                fis = new FileInputStream(main.getTrackerFolder() + File.separator + "public-key");
                buf = new byte[4096];
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        System.out.println("Breaking!");
                        out.write(buf, 0, -1);
                        break;
                    }
                    System.out.println("writing ...");
                    out.write(buf, 0, len);
                }

                fis.close();

            }


            String searchingFor = input.readUTF();
            
            System.out.println("He is searching for: " + searchingFor);

            File list = new File(main.getTrackerFolder() + File.separator + "lista.txt");

            if (list.exists()) {
                Scanner sc = new Scanner(list);

                FileOutputStream fOutTemp = new FileOutputStream(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt");
                DataOutputStream auxTemp = new DataOutputStream(fOutTemp);

                while (sc.hasNext()) {
                    String[] buff = sc.nextLine().split(":");

                    if (buff[3].contains(searchingFor)) {
                        auxTemp.writeUTF(buff[0] + ":" + buff[3] + ":" + buff[1] + ":" + buff[2] + ":");
                    }
                }

                sc.close();
                auxTemp.close();
                fOutTemp.close();

                out.writeUTF(searchingFor + "-quemTem.txt");
                
                System.out.println("Sending list.");

                out.writeUTF(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt");
                fis = new FileInputStream(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt");
                buf = new byte[4096];
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
                fis.close();

                sign.assinar(searchingFor);

                out.writeUTF(searchingFor + "-sign");
                
                System.out.println("Sending signature");

                out.writeUTF(main.getTrackerFolder() + File.separator + searchingFor + "-sign");
                fis = new FileInputStream(main.getTrackerFolder() + File.separator + searchingFor + "-sign");
                buf = new byte[4096];
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }

                fis.close();

                new File(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt").delete();
                new File(main.getTrackerFolder() + File.separator + searchingFor + "-sign").delete();

            } else {
                out.writeUTF("--1");
            }

            out.close();

        } catch (IOException ex) {
            System.err.println("AnswerSearch - IO: " + ex.getMessage());
        }
    }
}
