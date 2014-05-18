/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_prototipo;

/**
 * Classe que o Processo/Peer utiliza para ser a parte servidora da transferência de um arquivo que possui.
 * 
 * @author Henrique
 */
import java.net.*;
import java.io.*;

public class TCP_Server_Transferencia implements Runnable{
    
    private final int portaServidor;
    private final String caminhoDoDiretorio;

    /**
     * Construtora da classe.
     * 
     * @param caminhoDoDiretorio Caminho raíz dos diretórios de todos os processos.
     * @param porta Porta que será utilizada para realizar a transferência de um arquivo.
     */
    TCP_Server_Transferencia(String caminhoDoDiretorio, int porta) {
        this.caminhoDoDiretorio = caminhoDoDiretorio;
        this.portaServidor = porta;
    }

    /**
     * Porta Servidora + 4 é a porta utilizada para a transferência de arquivos, e.g. 8014 (caso o peer seja 8010).
     */
    @Override
    public void run() {
        try {
            int serverPort = portaServidor+4; 
            System.out.println("Porta para transferências: "+serverPort);
            String caminhoCompletoDiretorio = caminhoDoDiretorio+portaServidor;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection_Transferencia c = new Connection_Transferencia(clientSocket, caminhoCompletoDiretorio);
            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}

