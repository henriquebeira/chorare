/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.v2.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.v2.start.Main;

/**
 * Classe para validação de assinaturas do quemTem.txt.
 *
 * @author Henrique
 */
public class ValidateSignature {

    private Main main;

    /**
     * Construtora da classe.
     *
     * @param main Objeto Main do processo em execução.
     */
    public ValidateSignature(Main main) {
        this.main = main;
    }

    /**
     * Importação da chave pública enviada pelo Tracker. Importação da
     * assinatura enviada pelo Tracker. Criação do objeto Assinatura com a chave
     * pública. Importação do arquivo quemTem.txt e verificando se a assinatura,
     * em conjunto com a chave pública, é do Tracker que assinou.
     *
     * @return Retorna "true" se a verificação comprovar a veracidade do arquivo
     * quemTem.txt.
     */
    public boolean validate(String search) {
        FileInputStream keyfis = null;
        FileInputStream sigfis = null;
        BufferedInputStream bufin = null;

        try {
            /* import encoded public key */
            keyfis = new FileInputStream(main.getConfigFolder() + File.separator + "public-key");
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);

            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            sigfis = new FileInputStream(main.getConfigFolder() + File.separator + search + "-sign");
            byte[] sigToVerify = new byte[sigfis.available()];
            sigfis.read(sigToVerify);

            /* create a Signature object and initialize it with the public key */
            Signature sig = Signature.getInstance("DSA");
            sig.initVerify(pubKey);

            /* Update and verify the data */
            FileInputStream datafis = new FileInputStream(main.getConfigFolder() + File.separator + search + "-quemTem.txt");
            bufin = new BufferedInputStream(datafis);

            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                sig.update(buffer, 0, len);
            };

            return sig.verify(sigToVerify);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (keyfis != null) {
                    keyfis.close();
                }
            } catch (IOException ex) {
            }
            try {
                if (sigfis != null) {
                    sigfis.close();
                }
            } catch (IOException ex) {
            }
            try {
                if (bufin != null) {
                    bufin.close();
                }
            } catch (IOException ex) {
            }
        }

        return false;
    }
}
