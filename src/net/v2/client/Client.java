/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.v2.start.Main;

/**
 * Classe de controle da parte cliente de um processo.
 *
 * @author Henrique
 */
public class Client extends Thread {

    private Main main;
    private ServerSocket sSocket;

    /**
     * Construtora da classe.
     *
     * @param main Classe principal de um processo.
     */
    public Client(Main main) {
        try {
            this.main = main;

            sSocket = new ServerSocket(0, 0, InetAddress.getLocalHost());

            sSocket.setSoTimeout(3000);

            start();

        } catch (UnknownHostException ex) {
            System.err.println("Client - Host: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Cleint - IO: " + ex.getMessage());
        }
    }

    /**
     * Método para tranferir arquivos solicitados.
     */
    @Override
    public void run() {
        while (!interrupted()) {
            try {
                System.out.println("Waiting request");
                Socket sReceive = sSocket.accept();

                new TransferFile(main, sReceive);

            } catch (SocketTimeoutException tEx) {
            } catch (IOException ex) {
                System.err.println("Client - IO: " + ex.getMessage());
            }
        }
    }

    /**
     * Método para avisar ao Tracker quais arquivos possui.
     */
    public void sendFileList() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(300);
                } catch (InterruptedException ex) {
                }

                try {
                    File folder = main.getFilesFolder();
                    Socket socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());

                    DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());

                    sOut.writeUTF(Main.LIST_UTF);
                    sOut.writeUTF(main.getNickName());

                    System.out.println("Sending my List!");

                    String data = main.getNickName() + ":" + sSocket.getInetAddress().getHostAddress() + ":" + sSocket.getLocalPort() + ":";

                    for (File f : folder.listFiles()) {
                        if (!f.getName().equals("controle")) {

                            sOut.writeUTF(data.concat(f.getName()).concat(":"));

                        }
                    }

                } catch (IOException ex) {
                    System.err.println("Client - SendFile : " + ex.getMessage());
                }
            }
        }.start();
    }

    /**
     * Método para realizar uma requisição de busca ao Tracker.
     *
     * @param search Nome de arquivo desejado.
     */
    public void search(String search) {
        new Search(main, search);
    }

    /**
     * Método para requisitar um arquivo de um peer.
     *
     * @param peerNick Nome do processo.
     * @param peerIP Número do IP do peer servidor.
     * @param peerPort Número da porta do peer servidor.
     * @param file Nome do arquivo a ser transferido.
     */
    public void requestFileFromPeer(String peerNick, String peerIP, Integer peerPort, String file) {
        new RequestFile(main, peerNick, peerIP, peerPort, file);
    }
}

/**
 * Classe usada para realizar o download de um arquivo.
 *
 * @author Henrique
 */
class RequestFile extends Thread {

    private Main main;
    private InetAddress ip;
    private Integer port;
    private String file;
    private String nick;

    /**
     * Construtora da classe.
     *
     * @param main Classe principal do processo.
     * @param peerNick Nome do processo.
     * @param ip Número do IP do peer servidor.
     * @param port Número da porta do peer servidor.
     * @param file Nome do arquivo a ser transferido.
     */
    public RequestFile(Main main, String peerNick, String ip, Integer port, String file) {
        try {
            this.main = main;
            this.ip = InetAddress.getByName(ip);
            this.port = port;
            this.file = file;
            this.nick = peerNick;

            start();
        } catch (UnknownHostException ex) {
        }
    }

    /**
     * Método para a conexão com o peer que irá transferir o arquivo.
     */
    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, port);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeUTF(file);

            String fileRec = in.readUTF();

            if (!fileRec.equals("--1")) {
                FileOutputStream fOut = new FileOutputStream(main.getFilesFolder() + File.separator + fileRec);

                byte[] buff = new byte[4096];

                while (true) {
                    int len = in.read(buff);

                    if (len == -1) {
                        break;
                    }

                    fOut.write(buff, 0, len);
                }

                main.getGui().warnCompletedDownload(file, nick, new File(main.getFilesFolder() + File.separator + file));
            } else {

            }

        } catch (IOException ex) {
            System.err.println("Client#RequestFile - IO: " + ex.getMessage());
        }
    }
}

/**
 * Classe para buscar um arquivo, e receber do Tracker a lista de quem tem.
 *
 * @author Henrique
 */
class Search extends Thread {

    private Main main;
    private String search;
    private ValidateSignature validate;

    /**
     * Construtora da classe. Inicialização do validador de assinaturas.
     *
     * @param main Classe principal de um processo.
     * @param search Nome do arquivo a ser requisitado.
     */
    public Search(Main main, String search) {
        this.main = main;
        this.search = search;

        validate = new ValidateSignature(main);

        start();
    }

    /**
     * Método para iniciar toda a transação. Realização do contato inicial com o
     * Tracker. Transferência da chave pública, caso ainda não a tenha
     * disponível localmente. Transferência do quemTem.txt Transferência da
     * assinatura. Validação da assinatura. Extração dos dados do quemTem.txt e
     * envio para a construção da tabela na GUI.
     */
    @Override
    public void run() {
        Socket socket = null;
        try {
            File configFolder = main.getConfigFolder();

            ServerSocket svS = new ServerSocket(0, 0, main.getThisAddress());

            socket = new Socket(main.getTrackerAddress(), main.getTrackerPort());

            DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());
            DataInputStream sIn = new DataInputStream(socket.getInputStream());

            sOut.writeUTF(Main.SEARCH_UTF);

            sOut.writeUTF(svS.getInetAddress().getHostAddress());
            sOut.writeInt(svS.getLocalPort());

            boolean hasPublicKey = false;

            for (File f : configFolder.listFiles()) {
                if (f.getName().equals("public-key")) {
                    hasPublicKey = true;
                }
            }

            if (!hasPublicKey) {
                sOut.writeUTF("Y_PK");

                Socket rec = svS.accept();
                DataInputStream inpRec = new DataInputStream(rec.getInputStream());

                String incoming = sIn.readUTF();

                System.out.println("File: " + incoming);

                FileOutputStream fOut = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

                byte[] buf = new byte[4096];
                int i = 1;
                long auxR = 0;
//                long totalRead = sIn.readLong();

//                System.out.println("File SIze: " + totalRead);

                while (true) {
                    int len = inpRec.read(buf);
                    if (len == -1) {
                        break;
                    }

                    fOut.write(buf, 0, len);
                }

                inpRec.close();

                fOut.close();

            } else {
                sOut.writeUTF("N_PK");
            }

            sOut.writeUTF(search);

            String incoming = sIn.readUTF();

            System.out.println("File: " + incoming);

            if (incoming.equals("--1")) {
                System.out.println("No search could be done.");
                main.getGui().receiveSearchResponse(new Object[0][0], search);
                return;
            } else {
                System.out.println("Search done.");
            }

            FileOutputStream fOutSearch = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

            byte[] buf = new byte[4096];
            int i = 1;
            long auxR = 0;
//            long totalRead = sIn.readLong();

//            System.out.println("List size: " + totalRead);

            Socket rec = svS.accept();
            DataInputStream inpRec = new DataInputStream(rec.getInputStream());

            while (true) {
                int len = inpRec.read(buf);
                if (len == -1) {
                    System.out.println("Break -1");
                    break;
                }
                System.out.println("Writing " + len);
                fOutSearch.write(buf, 0, len);
            }

            inpRec.close();

            fOutSearch.close();

            System.out.println("quem tem recebido.");

            incoming = sIn.readUTF();

            System.out.println("File: " + incoming);

            fOutSearch = new FileOutputStream(configFolder.getPath() + File.separator + incoming);

            rec = svS.accept();
            inpRec = new DataInputStream(rec.getInputStream());

            buf = new byte[4096];
            i = 1;

            auxR = 0;
//            totalRead = sIn.readLong();

            while (true) {
                int len = inpRec.read(buf);
                if (len == -1) {
                    break;
                }
                fOutSearch.write(buf, 0, len);
            }

            fOutSearch.close();

            if (validate.validate(search)) {
                System.out.println("Tracker validated!");

                Scanner sc = new Scanner(new File(configFolder.getPath() + File.separator + search + "-quemTem.txt"));

                ArrayList<AuxData> list = new ArrayList<>();

                while (sc.hasNext()) {
                    String[] buff = sc.nextLine().split(":");

                    list.add(new AuxData().setNick(buff[0]).setFileName(buff[1]).setAddress(buff[2]).setPort(Integer.parseInt(buff[3])));
                }

                Object[][] data = new Object[list.size()][4];

                int k = 0;
                for (AuxData a : list) {
                    data[k][0] = a.getFileName();
                    data[k][1] = a.getNick();
                    data[k][2] = a.getAddress();
                    data[k][3] = a.getPort();

                    k++;
                }

                main.getGui().receiveSearchResponse(data, search);
            } else {
                System.out.println("Invalid Tracker.");
            }

            new File(configFolder.getPath() + File.separator + search + "-sign").delete();
            new File(configFolder.getPath() + File.separator + search + "-quemTem.txt").delete();

        } catch (IOException ex) {
            System.err.println("Client#Search - IO: " + ex.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}

/**
 * Classe usada para a montagem do objeto que é utilizado para montar a tabela
 * na GUI.
 *
 * @author Henrique
 */
class AuxData {

    private String address;
    private Integer port;
    private String nick;
    private String fileName;

    public String getAddress() {
        return address;
    }

    public AuxData setAddress(String address) {
        this.address = address;

        return this;
    }

    public Integer getPort() {
        return port;
    }

    public AuxData setPort(Integer port) {
        this.port = port;

        return this;
    }

    public String getNick() {
        return nick;
    }

    public AuxData setNick(String nick) {
        this.nick = nick;

        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public AuxData setFileName(String fileName) {
        this.fileName = fileName;

        return this;
    }
}
