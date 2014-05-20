/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import net.v2.start.Main;

/**
 * Classe para transferir arquivos requisitados por unicast.
 * 
 * @author Henrique
 */
public class TransferFile extends Thread {

    private Main main;
    private Socket socket;

    /**
     * Construtora da classe.
     * 
     * @param main Classe principal do processo.
     * @param socket Socket para transferência de arquivos.
     */
    public TransferFile(Main main, Socket socket) {
        this.main = main;
        this.socket = socket;
        
        start();
    }

    /**
     * Método para realizar a tranferência de arquivos.
     */
    @Override
    public void run() {
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String fileRequested = in.readUTF();

            File f = new File(main.getFilesFolder() + File.separator + fileRequested);

            if (f.exists()) {
                out.writeUTF(f.getName());

                FileInputStream fIn = new FileInputStream(f);

                byte[] buff = new byte[4096];
                while (true) {
                    int len = fIn.read(buff);
                    if (len == -1) {
                        break;
                    }

                    out.write(buff, 0, len);
                }

                fIn.close();

            } else {
                out.writeUTF("--1");
            }

        } catch (IOException ex) {
            System.err.println("TransferFile - IO: " + ex.getMessage());
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
            
            if(out != null){
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
