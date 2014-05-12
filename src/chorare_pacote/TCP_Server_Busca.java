/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_pacote;

/**
 * Classe que o Tracker usa para iniciar a conexão que receberá as requisições de busca dos peers.
 * 
 * @author Henrique
 */
import java.net.*;
import java.io.*;

public class TCP_Server_Busca implements Runnable{
    
    private final int numeroPortaPasta;
    private final String caminhoDaPasta;
    private final GeradorAssinatura gerarParChaves;

    /**
     * Construtora da classe. Geração do par de chaves privada e pública.
     * 
     * @param caminhoDaPasta
     * @param porta 
     */
    TCP_Server_Busca(String caminhoDaPasta, int porta) {
        this.caminhoDaPasta = caminhoDaPasta;
        this.numeroPortaPasta = porta;
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
                Connection_Busca c = new Connection_Busca(clientSocket, caminhoDaPasta, Integer.toString(numeroPortaPasta));
            }
        } catch (IOException e) {
            System.out.println("Listen socket TCP_Server_Busca:" + e.getMessage());
        }
    }
}

