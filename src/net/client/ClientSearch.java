/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.client;

/**
 * Classe para a requisição de busca de arquivos para o Tracker. Requisição da
 * chave pública, disponibilizada pelo Tracker. Requisição do arquivo desejado
 * ao Tracker. Recebimento de quemTem.txt, assim como da sua assinatura.
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
//    private final int portaServidor, portaCliente;
//    private String caminhoDoDiretorio;
    private VerificadorAssinatura verificadorAssinatura;

    private String requestedFile;

    /**
     * Construtora da classe. Preparação do verificador de assinaturas feitas
     * pelo Tracker.
     *
     * @param caminhoDoDiretorio Caminho raíz dos processos.
     * @param portaServidor Porta do Tracker.
     * @param portaCliente Porta do peer.
     */
    public ClientSearch(Main main, String file) {
        this.main = main;
//        this.caminhoDoDiretorio = caminhoDoDiretorio;
//        this.portaServidor = portaServidor;
        this.portaCliente = main.getClient().getAddressPort();
        this.requestedFile = file;
        verificadorAssinatura = new VerificadorAssinatura(main.getDefaultDiretory().getPath() + File.separator + main.getNickName());

    }

    /**
     * Conexão com a porta de recebimento de buscas do Tracker, que é final 2,
     * e.g. 8012 (quando o peer é 8010). Recebimento da chave pública do
     * Tracker. Preparação da entrada que receberá o arquivo desejado para ser
     * transferido. Recebimento do arquivo quemTem.txt. Recebimento da
     * assinatura do arquivo quemTem.txt. Se a assinatura foi realmente feita
     * pelo Tracker, efetivar a transferência do arquivo requisitado com o
     * Processo indicado pelo quemTem.txt.
     *
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
//            int serverPort = portaServidor + 2;
//            System.out.print("Porta Servidor Busca: " + serverPort + "\n");

            // Recebimento da chave pública do Tracker
//            if (!main.isAmITracker()) {
////                Thread thread3 = new Thread(new TCP_Server_Transferencia(caminhoDoDiretorio, portaServidor));
////                thread3.start();
////
////                Thread.sleep(3000);
//                Thread thread4 = new ClientSearch(caminhoDoDiretorio, portaServidor, portaCliente, "public_key");
//                thread4.start();
//            }
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
            System.out.println("TCP_Client_Busca recebeu quem tem: " + quemTem);
            //Se NÃO recebeu --1, baixe o arquivo
            if (!quemTem.equals("--1")) {
                // Recebe o arquivo quemTem.txt, caso o requisitante não seja o próprio Processo que está atuando como Tracker
//                if (portaCliente != portaServidor) {
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

                out.writeUTF("assinatura");

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
//                }

                // Recebimento da assinatura do arquivo quemTem.txt, caso o requisitante não seja o próprio Processo que está atuando como Tracker
//                Thread.sleep(5000);
//                if (portaCliente != portaServidor) {
//                    System.out.println("Buscar assinatura...");
//                    Thread thread3 = new Thread(new TCP_Server_Transferencia(caminhoDoDiretorio, portaServidor));
//                    thread3.start();
//
//                    Thread.sleep(3000);
//                    Thread thread4 = new Thread(new TCP_Client_Transferencia(caminhoDoDiretorio, portaServidor, portaCliente, "assinatura"));
//                    thread4.start();
//                }
                // Se a assinatura foi realmente feita pelo Tracker, efetivar a transferência do arquivo requisitado.
//                Thread.sleep(5000);
                if (verificadorAssinatura.verificar() == true) {
                    main.getClient().searchResult(new String[0][0]);
//                    System.out.println("Assinatura válida!!! ");
//                    // Abrir e recuperar o Processo que tem o arquivo desejado
//                    Scanner sc = new Scanner(new File(main.getDefaultDiretory().getPath() + File.separator + main.getNickName() + File.separator + "controle" + File.separator + "quemTem.txt"));
//                    String numeroProcesso = sc.nextLine();
//                    System.out.println("TCP_Client_Busca conecta com o Processo: " + numeroProcesso);
//
//                    // Recebe o diretório/processo que tem o arquivo solicitado, e prepara transferência
//                    Thread thread3 = new Thread(new TCP_Server_Transferencia(caminhoDoDiretorio, Integer.parseInt(numeroProcesso)));
//                    thread3.start();
//
//                    Thread.sleep(3000);
//                    Thread thread4 = new Thread(new TCP_Client_Transferencia(caminhoDoDiretorio, Integer.parseInt(numeroProcesso), portaCliente, arquivodesejado));
//                    thread4.start();
                } else {
                    System.out.println("Assinatura inválida! ");
                }

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
