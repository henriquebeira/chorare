/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe 
 * 
 * @author a1155997
 */
public class TrackerListReceiver extends Thread{
    
    private final String caminhoDoDiretorio;
    
    private boolean hasToDie = false;

    /**
     * Construtora da classe. 
     * 
     * @param caminhoDaPasta Caminho raíz dos diretórios dos processos.
     */
    TrackerListReceiver(String caminhoDaPasta) {
        this.caminhoDoDiretorio = caminhoDaPasta;
    }

    /**
     * Verifica a existência dos diretórios base do Processo. 
     * Criação, ou reinício, do arquivo lista.txt.
     * Inicialização da conexão para recebimento de mensagens dos outros peers.
     */
    @Override
    public void run() {
        try {
            ServerSocket listenSocket = new ServerSocket();
            
            listenSocket.setSoTimeout(500);
            
            if (!new File(caminhoDoDiretorio).exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio)).mkdir();   // Cria o diretório   
            }

            if (!new File(caminhoDoDiretorio + File.separator + "controle").exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio + File.separator + "controle")).mkdir();   // Cria o diretório   
                
            }
            FileOutputStream fos = new FileOutputStream(caminhoDoDiretorio + File.separator + "controle" + File.separator + "track" + File.separator + "lista.txt");
            Socket clientSocket;
            while (true) {
                if(hasToDie){
                    return;
                }
                clientSocket = listenSocket.accept();
                Connection_Lista c = new Connection_Lista(clientSocket, caminhoDoDiretorio, fos);
            }
        } catch (IOException e) {
            System.out.println("TrackerListReceiver - Listen socket:" + e.getMessage());
        }
    }

    /**
     * Método para finalizar a construção do lista.txt.
     * @param hasToDie True caso deve parar de construir a lista.txt.
     */
    public void setHasToDie(boolean hasToDie) {
        this.hasToDie = hasToDie;
    }
}
