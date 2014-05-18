/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_prototipo;

/**
 * Classe que faz o recebimento efetivo dos arquivos transferidos.
 * 
 * @author Henrique
 */
import java.net.*;
import java.io.*;

public class TCP_Client_Transferencia implements Runnable {

    private final int portaServidor, portaCliente;
    private final String arquivodesejado, caminhoDoDiretorio;
    private final Janela janela;

    /**
     * Construtora da classe. 
     * 
     * @param caminhoDoDiretorio Caminho raiz de todos os processos.
     * @param portaServidor Porta do peer que irá enviar o arquivo solicitado.
     * @param portaCliente Porta do peer que irá receber o arquivo solicitado.
     * @param arquivodesejado Nome do arquivo a ser transferido.
     */
    TCP_Client_Transferencia(String caminhoDoDiretorio, int portaServidor, int portaCliente, String arquivodesejado, Janela janela) {
        this.caminhoDoDiretorio = caminhoDoDiretorio;
        this.portaServidor = portaServidor;
        this.portaCliente = portaCliente;
        this.arquivodesejado = arquivodesejado;
        this.janela = janela;
    }

    /**
     * Conexão com o Processo que tem o arquivo solicitado, pela porta de final 4, e.g. 8014 (quando o peer é 8010).
     * Recebimento do arquivo, no diretório adequado para cada tipo de arquivo.
     * 
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            // Conectar com o Processo que tem o arquivo solicitado, pela porta de final 4, e.g. 8014 (quando o peer é 8010)
            int serverPort = portaServidor+4;
            socket = new Socket("localhost", serverPort);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(arquivodesejado);      	// Faz requisição do arquivo...
            
            String nomeDoArquivo = in.readUTF();	    //... e recebe a resposta com os dados
            //Se NÃO recebeu --1, baixe o arquivo
            if (!nomeDoArquivo.equals("--1")) {
                FileOutputStream fos;
                if (nomeDoArquivo.equals("public_key")||nomeDoArquivo.equals("assinatura")) {
                    fos = new FileOutputStream(new File(caminhoDoDiretorio+portaCliente+ File.separator+ "controle"+File.separator+arquivodesejado));
                } else {
                    fos = new FileOutputStream(new File(caminhoDoDiretorio+portaCliente+ File.separator+ arquivodesejado));
                    janela.setjAreaArquivos(arquivodesejado);
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
                janela.setjLog("Arquivo " + nomeDoArquivo + " transferido com sucesso!");
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

