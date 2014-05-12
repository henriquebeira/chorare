/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.tracker;

import chorare_pacote.GeradorAssinatura;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe que o Tracker usa para iniciar a conexão que receberá as requisições de busca dos peers.
 * 
 * @author Henriques
 */
public class Tracker extends Thread{
    private final GeradorAssinatura gerarParChaves;
    
    /**
     * Construtora da classe. Geração do par de chaves privada e pública.
     * 
     * @param caminhoDaPasta
     * @param porta 
     */
    Tracker() {
        gerarParChaves = new GeradorAssinatura(caminhoDaPasta+File.separator+numeroPortaPasta);
    }
    
    /**
     * Porta final 2 do Tracker é utilizada para receber requisições de buscas no tracker, e.g. 8012 (quando peer vencedor for 8010).
     * 
     */
    @Override
    public void run() {
        try {
            int serverPort = numeroPortaPasta+2; 
            ServerSocket listenSocket = new ServerSocket(serverPort);
            gerarParChaves.gerar();
            while (true) {
                Socket clientSocket = listenSocket.accept();
                SearchRequest c = new SearchRequest(clientSocket, caminhoDaPasta, Integer.toString(numeroPortaPasta));
            }
        } catch (IOException e) {
            System.out.println("Listen socket TCP_Server_Busca:" + e.getMessage());
        }
    }
}