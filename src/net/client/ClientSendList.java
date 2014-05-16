/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import net.start.Main;

/**
 * Classe para envio dos arquivos que o Processo possui para o Tracker.
 * Também é feito a construção dos diretórios necessários para o Processo, caso não existam.
 * 
 * @author Henriques
 */
class ClientSendList extends Thread{
    
    private Main main;
    private SocketAddress receiveAddress;
    private int receivePort;

    /**
     * Construtora da classe.
     * 
     * @param main Main do Processo.
     * @param receiveAddress Endereço de transferência de arquivos do Processo.
     * @param receivePort Porta de transferência de arquivos do Processo.
     */
    public ClientSendList(Main main, SocketAddress receiveAddress, int receivePort) {
        this.main = main;
        this.receiveAddress = receiveAddress;
        this.receivePort = receivePort;
    }

    /**
     * Conexão com o Processo Tracker, e envio dos nomes dos arquivos que este presente Processo possui.
     * Verifica se os diretórios necessários pelo Processo já existem.
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            if (!new File(main.getDefaultDiretory().getPath()).exists()) { // Verifica se o diretório existe.   
                (new File(main.getDefaultDiretory().getPath())).mkdir();   // Cria o diretório   
            }
            
            if (!new File(main.getDefaultDiretory().getPath() + File.separator + "controle").exists()) { // Verifica se o diretório existe.   
                (new File(main.getDefaultDiretory().getPath() + File.separator + "controle")).mkdir();   // Cria o diretório   
            }

            for (File file : new File(main.getDefaultDiretory().getPath()).listFiles()) {
                if(!file.getName().equals("controle")) // Ignora o diretório denominado "controle"
                {
                    out.writeUTF(receiveAddress.getHostAddress() + ";" + receivePort + ";" + file.getName() + ";");
                }
            }
            
            System.out.println("Lista enviada.");
            
            out.close();
            socket.close();
            
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF TCP_Envia_Lista:" + e.getMessage());
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
