/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe 
 * 
 * @author a1155997
 */
public class TrackerListReceiver extends Thread {

    private final String caminhoDoDiretorio;
    private boolean hasToDie = false;
    private ServerSocket listenSocket;

    /**
     * Construtora da classe.
     *
     * @param caminhoDaPasta Caminho raíz dos diretórios dos processos.
     */
    TrackerListReceiver(String caminhoDaPasta) {
        this.caminhoDoDiretorio = caminhoDaPasta;
        try {
            listenSocket = new ServerSocket(0, 0, InetAddress.getLocalHost());
        } catch (IOException ex) {
        }
    }

    /**
     * Verifica a existência dos diretórios base do Processo. 
     * Criação, ou reinício, do arquivo lista.txt.
     * Inicialização da conexão para recebimento de mensagens dos outros peers.
     */
    @Override
    public void run() {
        System.out.println("Tracker Recebe Lista Inicializado");
        try {

            listenSocket.setSoTimeout(900);

            if (!new File(caminhoDoDiretorio + File.separator).exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio + File.separator)).mkdir();   // Cria o diretório   
            }

            if (!new File(caminhoDoDiretorio + File.separator + "controle").exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio + File.separator + "controle")).mkdir();   // Cria o diretório    
            }

            if (!new File(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track").exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track")).mkdir();   // Cria o diretório    
            }
            
            System.out.println(new File(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track").getAbsolutePath());
            System.out.println(new File(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track").getPath());
//            FileOutputStream fos = new FileOutputStream(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track" + File.separator + "lista.txt");
            Socket clientSocket;
            while (true) {
                if (hasToDie) {
                    return;
                }
                try {
                    clientSocket = listenSocket.accept();
                    System.out.println("TrackListReceiver - Recebeu uma lista.");
                    Connection_Lista c = new Connection_Lista(clientSocket, caminhoDoDiretorio);
                } catch (IOException io) {
                }
            }
        } catch (IOException e) {
            System.out.println("TrackerListReceiver - Listen socket:" + e.getMessage());
        }

        System.out.println("Tracker Recebe Lista Finalizado");
    }

    /**
     * Método para finalizar a construção do lista.txt.
     * @param hasToDie True caso deve parar de construir a lista.txt.
     */
    public void setHasToDie(boolean hasToDie) {
        this.hasToDie = hasToDie;
    }

    public ServerSocket getListenSocket() {
        return listenSocket;
    }
}
