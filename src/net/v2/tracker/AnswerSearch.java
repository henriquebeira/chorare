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
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import net.v2.start.Main;

/**
 * Classe para responder buscas.
 * 
 * @author Henrique
 */
public class AnswerSearch extends Thread {

    private Main main;
    private Socket socket;
    private DataInputStream input;
    private SignatureGenerator sign;

    /**
     * Construtora da classe.
     * Criação do gerador de assinatura.
     * 
     * @param main Classe principal do processo.
     * @param socket Socket do Tracker.
     * @param input Fluxo de dados do Tracker. 
     */    
    public AnswerSearch(Main main, Socket socket, DataInputStream input) {
        this.main = main;
        this.socket = socket;
        this.input = input;

        sign = new SignatureGenerator(main);

        start();
    }

    /**
     * Método para receber uma busca.
     * Envio da chave pública, caso necessário.
     * Carrega o quemTem.txt com quais processos possuem o arquivo desejado, e envia ao cliente.
     * Gera assinatura da lista, e envia ao cliente.
     * 
     */    
    @Override
    public void run() {
        try {
            String IP = input.readUTF();
            int port = input.readInt();
            
            Socket reconnect;
            
            String needPublicKey = input.readUTF();
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            FileInputStream fis;
            byte[] buf;

            if (needPublicKey.equals("Y_PK")) {
                System.out.println("He needs PK");
                
                reconnect = new Socket(InetAddress.getByName(IP), port);
                DataOutputStream outRec = new DataOutputStream(reconnect.getOutputStream());

                out.writeUTF("public-key");
                fis = new FileInputStream(main.getTrackerFolder() + File.separator + "public-key");
                buf = new byte[4096];
                
//                out.writeLong(new File(main.getTrackerFolder() + File.separator + "public-key").length());
                
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        System.out.println("Breaking!");
                        break;
                    }
                    System.out.println("writing ...");
                    outRec.write(buf, 0, len);
                }
                
                outRec.close();
                reconnect.close();
                
//                out.flush();

                fis.close();

            }

            //Recebe a busca...
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
                        auxTemp.writeUTF(buff[0] + ":" + buff[3] + ":" + buff[1] + ":" + buff[2] + ":\n");
                    }
                }

                sc.close();
                auxTemp.close();
                fOutTemp.close();
                
                reconnect = new Socket(InetAddress.getByName(IP), port);
                DataOutputStream outRec = new DataOutputStream(reconnect.getOutputStream());

                out.writeUTF(searchingFor + "-quemTem.txt");
                
//                out.writeLong(new File(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt").length());
                System.out.println("QuemTem Size: " + new File(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt").length());
                
                System.out.println("Sending list.");

                fis = new FileInputStream(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt");
                buf = new byte[4096];
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        System.out.println("Breaking");
                        break;
                    }
                    System.out.println("Writing");
                    outRec.write(buf, 0, len);
                }
                
                outRec.close();
                reconnect.close();
                
//                out.flush();
                
                fis.close();

                //Assinar o quemTem.txt
                sign.assinar(searchingFor);
                
                reconnect = new Socket(InetAddress.getByName(IP), port);
                outRec = new DataOutputStream(reconnect.getOutputStream());

                out.writeUTF(searchingFor + "-sign");
                
//                out.writeLong(new File(main.getTrackerFolder() + File.separator + searchingFor + "-sign").length());
                
                System.out.println("Sending signature");
                
                System.out.println("Sign size: " + new File(main.getTrackerFolder() + File.separator + searchingFor + "-sign").length());
                
                fis = new FileInputStream(main.getTrackerFolder() + File.separator + searchingFor + "-sign");
                buf = new byte[4096];
                while (true) {
                    int len = fis.read(buf);
                    if (len == -1) {
                        break;
                    }
                    outRec.write(buf, 0, len);
                }
                
                outRec.close();
                reconnect.close();
                
//                out.flush();

                fis.close();

//                new File(main.getTrackerFolder() + File.separator + searchingFor + "-quemTem.txt").delete();
//                new File(main.getTrackerFolder() + File.separator + searchingFor + "-sign").delete();

            } else {
                out.writeUTF("--1");
            }

//            out.close();

        } catch (IOException ex) {
            System.err.println("AnswerSearch - IO: " + ex.getMessage());
        }
    }
}
