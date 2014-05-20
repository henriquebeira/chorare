/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.tracker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import net.v2.start.Main;

/**
 * Classe para gerar o par de chaves, e assinar o quemTem.txt
 * 
 * @author Henrique
 */
public class SignatureGenerator {
    private Main main;

    /**
     * Construtora da classe.
     * 
     * Geração do par de chaves privada e pública.
     * 1. gera um numero aleatório seguro.
     * 2. indica que as chaves são de 1024 bits.
     * 3. Gera as chaves pública e privada.
     * 4. Salva a chave privada, e a pública, em um arquivo cada.
     */
    public SignatureGenerator(Main main) {
        this.main = main;
        
        try {
            /* Generate a key pair */
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = new SecureRandom(); 

            keyGen.initialize(1024, random); 

            KeyPair pair = keyGen.generateKeyPair(); 
            PrivateKey priv = pair.getPrivate(); 
            PublicKey pub = pair.getPublic(); 

            byte[] privateKey = priv.getEncoded();
            FileOutputStream privateKeyFos = new FileOutputStream(main.getTrackerFolder() + File.separator + "private-key");
            privateKeyFos.write(privateKey);
            privateKeyFos.close();

            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream(main.getTrackerFolder() + File.separator + "public-key");
            keyfos.write(key);
            keyfos.close();

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }
    
    /**
     * Assinatura do arquivo quemTem.txt, para assegurar que este arquivo é verdadeiro.
     * 1. Recuperação da chave privada.
     * 2. Criação do objeto Assinatura, usando a chave privada.
     * 3. O objeto lê o arquivo quemTem.txt, e gera uma "assinatura" para ele.
     * 4. Salva a assinatura no arquivo "assinatura".
     * 
     */
    public void assinar(String signTo) {
        try {
            
            FileInputStream keyfis = new FileInputStream(main.getTrackerFolder() + File.separator + "private-key");
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();

            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);

            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

            Signature rsa = Signature.getInstance("DSA");
            rsa.initSign(privKey);

            FileInputStream fis = new FileInputStream(main.getTrackerFolder() + File.separator + signTo + "-quemTem.txt");
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                rsa.update(buffer, 0, len);
            };
            bufin.close();

            byte[] realSig = rsa.sign();

            FileOutputStream sigfos = new FileOutputStream(main.getTrackerFolder() + File.separator + signTo + "-sign");
            sigfos.write(realSig);
            sigfos.close();

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }
}
