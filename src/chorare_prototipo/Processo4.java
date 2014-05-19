/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chorare_prototipo;

import java.io.File;

/**
 * Classe para iniciar o Processo 8040.
 * 
 * @author Henrique
 */

public class Processo4 {

    /**
     * Envio (a cada 10 segundos) e recebimento de mensagens multicast do Processo denominado 8040.
     * 
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String args[]) throws InterruptedException {
        String caminhoRaiz = "C:"+File.separator+"arquivos_chorare"+File.separator;
        //String caminhoRaiz = "/home/todos/alunos/ct/a1156462/"+"arquivos_chorare"+File.separator;
        Janela janela = new Janela();
        janela.setVisible(true);
        Thread thread1 = new Thread(new EnvioMulticast(10000, 8040));
        Thread thread2 = new Thread(new RecebeMulticast(8040, caminhoRaiz, janela, 4));
        thread1.start();
        thread2.start();
                
    }
    
}
