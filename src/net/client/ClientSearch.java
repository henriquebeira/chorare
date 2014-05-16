/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

/**
 * Classe para a requisição de busca de arquivos para o Tracker. 
 * Requisição da chave pública, disponibilizada pelo Tracker. 
 * Requisição do arquivo desejado ao Tracker. 
 * Recebimento de quemTem.txt, assim como da sua assinatura.
 * Efetivar a transferência do arquivo requisitado, por unicast.
 *
 * @author Henrique
 */
import java.net.*;
import java.io.*;
import net.start.Main;

class ClientSearch extends Thread {

    private Main main;
    private final int portaCliente;
    private VerificadorAssinatura verificadorAssinatura;
    private String requestedFile;

    /**
     * Construtora da classe. 
     * Preparação do verificador de assinaturas feitas pelo Tracker, com o caminho raíz do diretório.
     *
     * @param main Classe principal dos processos.
     * @param file Nome do arquivo requisitado.
     */
    public ClientSearch(Main main, String file) {
        this.main = main;
        this.portaCliente = main.getClient().getAddressPort();
        this.requestedFile = file;
        verificadorAssinatura = new VerificadorAssinatura(main.getDefaultDiretory().getPath() + File.separator + main.getNickName());
    }

    /**
     * Conexão com a porta de recebimento de buscas do Tracker.
     * Recebimento da chave pública do Tracker. 
     * Preparação da entrada que receberá o arquivo desejado para ser transferido. 
     * Recebimento do arquivo quemTem.txt. 
     * Recebimento da assinatura do arquivo quemTem.txt. 
     * Se a assinatura foi realmente feita pelo Tracker, efetivar a monstagem dos resultados na GUI.
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("public_key");

            FileOutputStream fos = new FileOutputStream(new File(main.getDefaultDiretory().getPath() + File.separator + main.getNickName() + File.separator + "controle" + File.separator + "public_key"));
            byte[] buf = new byte[4096];
            int i = 1;
            while (true) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
            }

            System.out.println("O arquivo requerido é: " + requestedFile);
            out.writeUTF(String.valueOf(portaCliente));
            out.writeUTF(requestedFile);   // Faz requisição do arquivo...

            String quemTem = in.readUTF();    //... e recebe qual processo tem o arquivo desejado
            System.out.println("ClientSearch recebeu quem tem: " + quemTem);
            //Se NÃO recebeu --1, baixe o arquivo
            if (!quemTem.equals("--1")) {
                // Recebe o arquivo quemTem.txt.
                fos = new FileOutputStream(new File(main.getDefaultDiretory().getPath() + File.separator + main.getNickName() + File.separator + "controle" + File.separator + "quemTem.txt"));
                buf = new byte[4096];
                i = 1;
                while (true) {
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buf, 0, len);
                }

                out.writeUTF("assinatura"); // Receberá a assinatura do quemTem.txt.

                fos = new FileOutputStream(new File(main.getDefaultDiretory().getPath() + File.separator + main.getNickName() + File.separator + "controle" + File.separator + "assinatura"));
                buf = new byte[4096];
                i = 1;
                while (true) {
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buf, 0, len);
                }

                in.close();
                out.close();
                socket.close();

                // Se a assinatura foi realmente feita pelo Tracker, montar os resultados na GUI.
                if (verificadorAssinatura.verificar() == true) {
                    main.getClient().searchResult(new String[0][0]);
                } else {
                    System.out.println("Assinatura inválida! ");
                }

                // Apaga o conteúdo do arquivo quemTem.txt.
                fos = new FileOutputStream(new File(main.getDefaultDiretory().getPath() + File.separator + main.getNickName() + File.separator + "controle" + File.separator + "quemTem.txt"));
                fos.write("".getBytes());
                fos.close();

            } else {
                System.out.println("Arquivo não existe ou já está no diretório");
            }
            in.close();
            out.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("Client Search - EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("TCP_Client_Busca readline:" + e.getMessage());
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
