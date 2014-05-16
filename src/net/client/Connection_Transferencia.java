/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import net.start.Main;

/**
 * Classe para a transfência de arquivos.
 * 
 * @author Henrique
 */
class Connection_Transferencia extends Thread {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    private String caminhoDoDiretorio;
    
    private Main main;

    /**
     * Construtora da classe.
     * 
     * @param aClientSocket Socket vindo de TCP_Server_Transferencia.
     * @param caminho Caminho raíz do diretório que está atuando como o Tracker.
     */
    public Connection_Transferencia(Main main, Socket aClientSocket, String caminho) {
        try {
            this.main = main;
            clientSocket = aClientSocket;
            this.caminhoDoDiretorio = caminho;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    /**
     * Recebe o nome do arquivo para transferir, adicionando o diretório "controle" se for requisições de chave pública ou assinatura do quemTem.txt.
     * Envia o arquivo desejado, já sabendo que ele existe.
     */
    public void run() {
        try {
            String nomeDoArquivo = in.readUTF();  // read a line of data from the stream
            System.out.println("Connection_Transferencia recebeu: " + nomeDoArquivo);
            if ((nomeDoArquivo.equals("public_key")||nomeDoArquivo.equals("assinatura")) && main.isAmITracker()) {
                caminhoDoDiretorio = caminhoDoDiretorio+File.separator+"controle";
            }
            
            for (File file : new File(caminhoDoDiretorio).listFiles()) {
                if (file.getName().equals(nomeDoArquivo)) {
                    out.writeUTF(nomeDoArquivo);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buf = new byte[4096];
                    while (true) {
                        int len = fis.read(buf);
                        if (len == -1) {
                            break;
                        }
                        out.write(buf, 0, len);
                    }
                }
            }
            in.close();
            out.close();
            clientSocket.close();
            
        } catch (EOFException e) {
            System.out.println("ConnectionTransferencia - EOF:" + e.getMessage());
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
