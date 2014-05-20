/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.tracker;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import net.v2.start.Main;

/**
 * Classe para recebimento de listas.
 * 
 * @author Henrique
 */
public class ReceiveList extends Thread{
    private Main main;
    private Socket socket;
    private DataInputStream input;

    /**
     * Construtora da classe.
     * 
     * @param main Classe principal do processo.
     * @param socket Socket do Tracker.
     * @param input Fluxo de dados do Tracker. 
     */    
    public ReceiveList(Main main, Socket socket, DataInputStream input) {
        this.main = main;
        this.socket = socket;
        this.input = input;
        
        start();
    }

    /**
     * Método para receber dos outros processos quais arquivos que possuem.
     */    
    @Override
    public void run() {
        try {
            System.out.println("Receiving list from: " + input.readUTF());
            
            while (!this.isInterrupted()) {
                String identificacaoArquivo = input.readUTF();
                
                System.out.println("Received: " + identificacaoArquivo);
                
                FileOutputStream fos = new FileOutputStream(main.getTrackerFolder() + File.separator + "lista.txt", true);
                fos.write((identificacaoArquivo + "\n").getBytes());
            }
            input.close();

        } catch (EOFException e) {
            this.interrupt();

        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {/*close failed*/

            }
        }
    }
    
}
