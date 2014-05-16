/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.tracker;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

/**
 * Classe responsável em atender aos pedidos de quem tem um determinado arquivo.
 *
 * @author Henriques
 */
public class SearchRequest extends Thread {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    private String caminhoDaPasta;
    private boolean achou = false;
    private GeradorAssinatura geradorAssinatura;

    /**
     * Construtora da classe. 
     * Instância da classe GeradorAssinatura, para assinar o arquivo quemTem.txt.
     *
     * @param aClientSocket socket vindo de TCP_Server_Busca.
     * @param caminho caminho raíz do diretório.
     */
    public SearchRequest(Socket aClientSocket, String caminho) {
        try {
            clientSocket = aClientSocket;
            this.caminhoDaPasta = caminho;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            geradorAssinatura = new GeradorAssinatura(caminhoDaPasta);
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    /**
     * Espera uma requisição para buscar um arquivo.
     * Abre a lista.txt e busca quem tem o arquivo pesquisado. 
     * Escreve no quemTem.txt a identificação de quem tem o arquivo buscado. 
     * Realiza a assinatura do quemTem.txt e envia este arquivo ao requerente.
     *
     */
    public void run() {
        try {
            String key = in.readUTF();

            if (key.equals("public_key")) {
                FileInputStream keyInput = new FileInputStream(caminhoDaPasta + File.separator + "controle" + File.separator + "track" + File.separator + "public_key");

                byte[] buf = new byte[4096];
                while (true) {
                    int len = keyInput.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
            }
            
            System.out.println("PublicKey está Ok");

            // Espera por dados...
            String requerente = in.readUTF();
            String nomeArquivo = in.readUTF();
            System.out.println("\n SearchRequest recebeu: " + nomeArquivo);
            BufferedReader br = new BufferedReader(new FileReader(caminhoDaPasta + File.separator + "controle" + File.separator + "track" + File.separator + "lista.txt"));

            while (br.ready()) {
                String linha = br.readLine();
                String[] parts = linha.split(";");
                if (nomeArquivo.equals(parts[1])) {
                    System.out.println("Achou o arquivo!! ");

                    if (!parts[0].equals(requerente)) {
                        System.out.println("\n SearchRequest respondeu quem tem o arquivo: " + parts[0]);
                        out.writeUTF(parts[0]);
                        FileOutputStream fos = new FileOutputStream(caminhoDaPasta + File.separator + "controle" + File.separator + "track" + File.separator + "quemTem.txt");
                        fos.write((parts[0]).getBytes());
                        geradorAssinatura.assinar();

                        for (File file : new File(caminhoDaPasta + File.separator + "controle" + File.separator + "track").listFiles()) {
                            if (file.getName().equals("quemTem.txt")) {
                                achou = true;
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
                        break;

                    } else {
                        System.out.println("O arquivo pertence ao requerente.");
                    }
                }
            }
            br.close();
            if (achou == false) {
                out.writeUTF("--1");
            }
            in.close();
            out.close();
            clientSocket.close();
            achou = false;

        } catch (EOFException e) {
            System.out.println("Search Request - EOF:" + e.getMessage());
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
