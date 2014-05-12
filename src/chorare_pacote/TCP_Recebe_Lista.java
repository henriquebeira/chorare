/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_pacote;

/**
 * Classe que o Tracker utiliza para abrir uma conexão e receber os nomes de arquivos que os outros peers possuem.
 * Montagem do arquivo lista.txt
 * 
 * @author Henrique
 */
import java.net.*;
import java.io.*;

public class TCP_Recebe_Lista implements Runnable{
    
    private final int portaVencedoraDaEleicao;
    private final String caminhoDoDiretorio;

    /**
     * Construtora da classe. 
     * 
     * @param caminhoDaPasta Caminho raíz dos diretórios dos processos.
     * @param porta Porta do Processo vencedor da eleição para Tracker.
     */
    TCP_Recebe_Lista(String caminhoDaPasta, int porta) {
        this.caminhoDoDiretorio = caminhoDaPasta;
        this.portaVencedoraDaEleicao = porta;
    }

    /**
     * Verifica a existência dos diretórios base do Processo. 
     * Criação, ou reinício, do arquivo lista.txt.
     * Inicialização da conexão para recebimento de mensagens dos outros peers.
     * 
     */
    @Override
    public void run() {
        try {
            int serverPort = portaVencedoraDaEleicao; // the server port
            String caminhoCompletoDoDiretorio = caminhoDoDiretorio+portaVencedoraDaEleicao;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            
            if (!new File(caminhoCompletoDoDiretorio).exists()) { // Verifica se o diretório existe.   
                (new File(caminhoCompletoDoDiretorio)).mkdir();   // Cria o diretório   
            }

            if (!new File(caminhoCompletoDoDiretorio + File.separator + "controle").exists()) { // Verifica se o diretório existe.   
                (new File(caminhoCompletoDoDiretorio + File.separator + "controle")).mkdir();   // Cria o diretório   
                
            }
            FileOutputStream fos = new FileOutputStream(caminhoCompletoDoDiretorio + File.separator + "controle" + File.separator + "lista.txt");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection_Lista c = new Connection_Lista(portaVencedoraDaEleicao, clientSocket, caminhoCompletoDoDiretorio, fos);
            }
        } catch (IOException e) {
            System.out.println("TCP_Recebe_Lista - Listen socket:" + e.getMessage());
        }
    }
    
}

