/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_pacote;

import java.io.File;

/**
 * Classe para iniciar o Processo 8020.
 * 
 * @author Henrique
 */

public class Processo2 {

    /**
     * Envio (a cada 10 segundos) e recebimento de mensagens multicast do Processo denominado 8020.
     * 
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String args[]) throws InterruptedException {
        String caminhoRaiz = "C:"+File.separator+"arquivos_chorare"+File.separator;
        //String caminhoDaPasta = "/home/todos/alunos/ct/a1155997/"+"arquivos_chorare"+File.separator;
        Thread thread1 = new Thread(new EnvioMulticast(10000, 8020));
        Thread thread2 = new Thread(new RecebeMulticast(8020, caminhoRaiz));
        thread1.start();
        thread2.start();
                
    }
    
}