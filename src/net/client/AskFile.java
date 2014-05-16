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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * 
 * @author Henrique
 */
class AskFile extends Thread{
    
    private final String arquivodesejado, caminhoDoDiretorio;
    private InetAddress address;
    private int port;

    /**
     * Construtora da classe. 
     * 
     * @param caminhoDoDiretorio Caminho raiz de todos os processos.
     * @param port Porta do Processo que tem o arquivo solicitado.
     * @param arquivodesejado Nome do arquivo a ser transferido.
     */
    AskFile(String caminhoDoDiretorio, InetAddress address, int port, String arquivodesejado) {
        this.caminhoDoDiretorio = caminhoDoDiretorio;
        this.arquivodesejado = arquivodesejado;
        this.address = address;
        this.port = port;
    }

    /**
     * Conexão com o Processo que tem o arquivo solicitado.
     * Recebimento do arquivo, no diretório adequado para cada tipo de arquivo.
     * 
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            // Conectar com o Processo que tem o arquivo solicitado
            socket = new Socket(address, port); // Abre conexão com o peer desejado
            DataInputStream in = new DataInputStream(socket.getInputStream()); // Pega o stream de entrada
            DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // Pega o stream de saída
            out.writeUTF(arquivodesejado);      	// Faz requisição do arquivo...
            
            String nomeDoArquivo = in.readUTF();	    //... e recebe a resposta com os dados
            System.out.println("AskFile recebeu: " + nomeDoArquivo);
            //Se NÃO recebeu --1, baixe o arquivo
            if (!nomeDoArquivo.equals("--1")) {
                FileOutputStream fos;
                if (nomeDoArquivo.equals("public_key")||nomeDoArquivo.equals("assinatura")) { // se for um dos arquivos de controle / requisição para o tracker
                    fos = new FileOutputStream(new File(caminhoDoDiretorio + File.separator + "controle" + File.separator + arquivodesejado));
                } else { // se for uma requisição para um arquivo comum de outro peer
                    fos = new FileOutputStream(new File(caminhoDoDiretorio + File.separator + arquivodesejado));
                }
                byte[] buf = new byte[4096];
                int i = 1;
                while (true) {
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buf, 0, len);
                }
            }
            in.close();
            out.close();
            socket.close();
            
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
    }
}
