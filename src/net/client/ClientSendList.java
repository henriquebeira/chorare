/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Classe para envio dos arquivos que possui para o Tracker.
 * Também é feito a construção dos diretórios necessários para o Processo, caso não existam.
 * 
 * @author Henriques
 */
class ClientSendList extends Thread{
    
    private final int portaVencedora, numeroDesteProcesso;
    private final String caminhoDoDiretorio;

    /**
     * Construtora da classe.
     * 
     * @param caminhoDaPasta Caminho raíz de todos os processos.
     * @param portaVencedora Porta do Processo que atua como Tracker.
     * @param estaPorta Identificação do Processo.
     */
    public ClientSendList(String caminhoDaPasta, int portaVencedora, int estaPorta) {
        this.caminhoDoDiretorio = caminhoDaPasta;
        this.portaVencedora = portaVencedora;
        this.numeroDesteProcesso = estaPorta;
    }

    /**
     * Conexão com o Processo Tracker, e envio dos nomes dos arquivos que este presente Processo possui.
     * 
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            int portaTracker = portaVencedora;
            socket = new Socket("localhost", portaTracker);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            if (!new File(caminhoDoDiretorio+ numeroDesteProcesso).exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio+ numeroDesteProcesso)).mkdirs();   // Cria o diretório   
            }
            
            if (!new File(caminhoDoDiretorio+ numeroDesteProcesso + File.separator + "controle").exists()) { // Verifica se o diretório existe.   
                (new File(caminhoDoDiretorio+ numeroDesteProcesso + File.separator + "controle")).mkdir();   // Cria o diretório   
            }

            for (File file : new File(caminhoDoDiretorio+ numeroDesteProcesso).listFiles()) {
                if(!file.getName().equals("controle")) // Ignora o diretório denominado "controle"
                {
                    out.writeUTF(numeroDesteProcesso + ";" + file.getName() + ";");
                    System.out.println(numeroDesteProcesso + " respondeu que tem os arquivos: " + file.getName());
                }
            }
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
