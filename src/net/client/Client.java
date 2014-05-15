/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import net.start.Main;

/**
 *
 * @author a1155997
 */
public class Client extends Thread{
    private Main main;
    
//    private final int portaServidor;
    private final String caminhoDoDiretorio;
    
    private ServerSocket listenSocket; 

    /**
     * Construtora da classe.
     * 
     * 
     * @param main Referência para a classe principal que instânciou este processo.
     */
    public Client(Main main) {
        this.main = main;
        this.caminhoDoDiretorio = main.getDefaultDiretory().getPath() + File.separator + main.getNickName();
//        this.portaServidor = porta;
        
    }

    /**
     * Porta Servidora + 4 é a porta utilizada para a transferência de arquivos, e.g. 8014 (caso o peer seja 8010).
     */
    @Override
    public void run() {
        try {
            listenSocket = new ServerSocket();
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection_Transferencia c = new Connection_Transferencia(main, clientSocket, caminhoDoDiretorio);
            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
    
    public void enviaLista(){
        ClientSendList sendList = new ClientSendList(main, listenSocket.getInetAddress(), listenSocket.getLocalPort());
        sendList.start();
    }
    
    public void realizarBusca(String buscar){
        ClientSearch search = new ClientSearch(main, buscar);
        search.start();
    }
    
    public void searchResult(String[][] data){
        main.getGui().searchDone(data);
    }
    
    public void requererArquivoPeer(String nomeArquivo, InetAddress[] addresses){
        
    }

    int getAddressPort() {
        return listenSocket.getLocalPort();
    }
}
