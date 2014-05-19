/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_prototipo;

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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCP_Client_Busca implements Runnable {

    private final int portaServidor, portaCliente;
    private final String caminhoDoDiretorio;
    private final VerificadorAssinatura verificadorAssinatura;
    private final Janela janela;

    /**
     * Construtora da classe. Preparação do verificador de assinaturas feitas
     * pelo Tracker.
     *
     * @param caminhoDoDiretorio Caminho raíz dos processos.
     * @param portaServidor Porta do Tracker.
     * @param portaCliente Porta do peer.
     */
    TCP_Client_Busca(String caminhoDoDiretorio, int portaServidor, int portaCliente, Janela jan) {
        this.caminhoDoDiretorio = caminhoDoDiretorio;
        this.portaServidor = portaServidor;
        this.portaCliente = portaCliente;
        verificadorAssinatura = new VerificadorAssinatura(caminhoDoDiretorio + portaCliente);
        janela = jan;
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
            int serverPort = portaServidor + 2;
            janela.setjLog("Porta Servidor Busca: " + serverPort + "\n");

            //Todos os Processos preparam as suas portas para possíveis transferências unicast de arquivos
            Thread thread2 = new Thread(new TCP_Server_Transferencia(caminhoDoDiretorio, portaCliente));
            thread2.start();
            
            //Faz a requisição da chave pública do Tracker
            if (portaCliente != portaServidor) {
                Thread.sleep(3000);
                Thread thread4 = new Thread(new TCP_Client_Transferencia(caminhoDoDiretorio, portaServidor, portaCliente, "public_key", janela));
                thread4.start();
            }

            while (true) {
                socket = new Socket("localhost", serverPort);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                janela.setjLog("\n Digite o nome do arquivo desejado... ");
                janela.bloqueioBotaoBusca(true);
                while (janela.isClicou() == false) { //Enquanto o botão "Buscar arquivo" não for clicado...
                    Thread.sleep(1000);
                }
                String arquivoDesejado = janela.getjCampoBusca();
                janela.setjLog("O nome digitado foi: " + arquivoDesejado);
                
                out.writeUTF(String.valueOf(portaCliente)); // Envia quem fez a requisição
                out.writeUTF(arquivoDesejado);      	    // Envia o nome do arquivo requirido...

                String quemTem = in.readUTF();	    //... e recebe qual processo tem o arquivo desejado
                janela.setjLog("Quem tem o arquivo? " + quemTem);
                //Se NÃO recebeu --1, baixe o arquivo
                if (!quemTem.equals("--1")) {
                    // Recebe o arquivo quemTem.txt, caso o requisitante não seja o próprio Processo que está atuando como Tracker
                    if (portaCliente != portaServidor) {
                        FileOutputStream fos = new FileOutputStream(new File(caminhoDoDiretorio + portaCliente + File.separator + "controle" + File.separator + "quemTem.txt"));
                        byte[] buf = new byte[4096];
                        int i = 1;
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
                    }

                    // Recebimento da assinatura do arquivo quemTem.txt, caso o requisitante não seja o próprio Processo que está atuando como Tracker
                    Thread.sleep(5000);
                    if (portaCliente != portaServidor) {
                        janela.setjLog("Buscar assinatura...");
                        Thread.sleep(3000);
                        Thread thread4 = new Thread(new TCP_Client_Transferencia(caminhoDoDiretorio, portaServidor, portaCliente, "assinatura", janela));
                        thread4.start();
                    }

                    // Se a assinatura foi realmente feita pelo Tracker, efetivar a transferência do arquivo requisitado.
                    Thread.sleep(5000);
                    if (verificadorAssinatura.verificar() == true) {
                        janela.setjLog("Assinatura válida!!! ");
                        // Abrir e recuperar o Processo que tem o arquivo desejado
                        Scanner sc = new Scanner(new File(caminhoDoDiretorio + portaCliente + File.separator + "controle" + File.separator + "quemTem.txt"));
                        String numeroProcesso = sc.nextLine();
                        janela.setjLog("Conectar ao Processo: " + numeroProcesso);

                        // Recebe o diretório/processo que tem o arquivo solicitado, e requisita transferência
                        Thread.sleep(3000);
                        Thread thread4 = new Thread(new TCP_Client_Transferencia(caminhoDoDiretorio, Integer.parseInt(numeroProcesso), portaCliente, arquivoDesejado, janela));
                        thread4.start();
                        
                        // Atualiza a lista.txt do Tracker
                        socket = new Socket("localhost", portaServidor);
                        out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(portaCliente + ";" + arquivoDesejado + ";");
                        
                    } else {
                        janela.setjLog("Assinatura inválida! ");
                    }

                } else {
                    janela.setjLog("Arquivo não existe ou já está no diretório");
                }
                in.close();
                out.close();
                socket.close();
                janela.setClicou(false);
            }

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("TCP_Client_Busca readline:" + e.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(TCP_Client_Busca.class.getName()).log(Level.SEVERE, null, ex);
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
