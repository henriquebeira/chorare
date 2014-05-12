/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_pacote;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe para receber as mensagens dos peers sobre quais arquivos possuem.
 * 
 * @author Henrique
 */
class Connection_Lista extends Thread {

    private DataInputStream in;
    private Socket clientSocket;
    private String caminhoCompletoDoDiretorio;
    private FileOutputStream fos;

    /**
     * Construtora da classe.
     * 
     * @param porta Porta do Tracker.
     * @param aClientSocket Socker vindo de TCP_Recebe_Lista.
     * @param caminho Caminho raíz, já com a identificação do Tracker.
     * @param fos Fluxo para a construção do arquivo lista.txt.
     */
    public Connection_Lista(int porta, Socket aClientSocket, String caminho, FileOutputStream fos) {
        try {
            this.fos = fos;
            clientSocket = aClientSocket;
            this.caminhoCompletoDoDiretorio = caminho;
            in = new DataInputStream(clientSocket.getInputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    /**
     * Recebimento das mensagens contendo o Processo/Pasta e o arquivo que ele contém, salvando-os em lista.txt.
     * 
     */
    public void run() {
        try {
            while (!this.isInterrupted()) {
                String identificacaoArquivo = in.readUTF();
                System.out.println("Connection_Lista recebeu: " + identificacaoArquivo);
                fos = new FileOutputStream(caminhoCompletoDoDiretorio + File.separator + "controle" + File.separator + "lista.txt", true);
                fos.write((identificacaoArquivo + "\n").getBytes());
            }
            in.close();
            clientSocket.close();

        } catch (EOFException e) {
            this.interrupt();

        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {/*close failed*/

            }
        }

    }
}
