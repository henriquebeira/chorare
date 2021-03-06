/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_prototipo;

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
 * @author Henrique
 */
class Connection_Busca extends Thread {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    private String caminhoDaPasta, numeroPortaPasta;
    private boolean achou = false;
    private GeradorAssinatura geradorAssinatura;
    private Janela janela;

    /**
     * Construtora da classe. Instância da classe GeradorAssinatura, para assinar o
     * arquivo quemTem.txt com a identificação de quem tem o arquivo buscado.
     * 
     * @param aClientSocket socket vindo de TCP_Server_Busca.
     * @param caminho caminho raíz do diretório.
     * @param diretorio identificação do diretório que é o Tracker.
     */
    public Connection_Busca(Socket aClientSocket, String caminho, String diretorio, Janela jan) {
        try {
            clientSocket = aClientSocket;
            this.caminhoDaPasta = caminho;
            this.numeroPortaPasta = diretorio;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            geradorAssinatura = new GeradorAssinatura(caminhoDaPasta + numeroPortaPasta);
            janela = jan;
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
        }
    }

    /**
     * Espera uma requisição, com a identificação do requerente, e o arquivo buscado.
     * Abre a lista e busca quem tem o arquivo pesquisado.
     * Verifica se o requerente já possui o arquivo, e escreve no quemTem.txt a identificação de quem tem o arquivo buscado.
     * Realiza a assinatura do quemTem.txt e envia este arquivo ao requerente.
     * 
     */
    public void run() {
        try {
            // Espera por dados...
            String requerente = in.readUTF();
            String nomeArquivo = in.readUTF();
            janela.setjLog(requerente + " busca o arquivo: " + nomeArquivo);
            BufferedReader br = new BufferedReader(new FileReader(caminhoDaPasta + numeroPortaPasta + File.separator + "controle"+File.separator+"lista.txt"));

            while (br.ready()) {
                String linha = br.readLine();
                String[] parts = linha.split(";");
                if (nomeArquivo.equals(parts[1])) {
                    janela.setjLog("Arquivo encontrado! ");
                    
                    if (!parts[0].equals(requerente)) {
                        janela.setjLog("Quem tem o arquivo: " + parts[0]);
                        out.writeUTF(parts[0]);
                        FileOutputStream fos = new FileOutputStream(caminhoDaPasta + numeroPortaPasta + File.separator + "controle"+File.separator+"quemTem.txt");
                        fos.write((parts[0]).getBytes());
                        geradorAssinatura.assinar();

                        for (File file : new File(caminhoDaPasta + numeroPortaPasta + File.separator + "controle").listFiles()) {
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
                        
                    }else{
                        janela.setjLog("O arquivo já pertence ao requerente.");
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
            System.out.println("EOF:" + e.getMessage());
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
