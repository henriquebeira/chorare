package net.tracker;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import chorare_pacote.*;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Classe para gerar o par de chaves privada e pública, e também para a assinatura do arquivo quemTem.txt, 
 * @author Henrique
 */
class GeradorAssinatura {

    private final String caminhoPublicKey; 
    private final String caminhoAssinatura;
    private final String caminhoDoArquivoQuemTem;
    private final String caminhoPrivateKey;

    /**
     * Construtora da classe.
     * 
     * @param caminhoDiretorio Caminho raíz do diretório do Tracker.
     */
    public GeradorAssinatura(String caminhoDiretorio) {
        this.caminhoPublicKey = caminhoDiretorio + File.separator + "controle" + File.separator + "public_key";
        this.caminhoAssinatura = caminhoDiretorio + File.separator + "controle" + File.separator+"assinatura";
        this.caminhoDoArquivoQuemTem = caminhoDiretorio + File.separator + "controle" + File.separator+"quemTem.txt";
        this.caminhoPrivateKey = caminhoDiretorio + File.separator + "controle" + File.separator+"private_key";
    }

    /**
     * Geração do par de chaves privada e pública.
     * 1. gera um numero aleatório seguro.
     * 2. indica que as chaves são de 1024 bits.
     * 3. Gera as chaves pública e privada.
     * 4. Salva a chave privada, e a pública, em um arquivo cada.
     */
    public void gerar() {
        try {
            /* Generate a key pair */
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = new SecureRandom(); 

            keyGen.initialize(1024, random); 

            KeyPair pair = keyGen.generateKeyPair(); 
            PrivateKey priv = pair.getPrivate(); 
            PublicKey pub = pair.getPublic(); 

            byte[] privateKey = priv.getEncoded();
            FileOutputStream privateKeyFos = new FileOutputStream(caminhoPrivateKey);
            privateKeyFos.write(privateKey);
            privateKeyFos.close();

            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream(caminhoPublicKey);
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
    public void assinar() {
        try {
            
            FileInputStream keyfis = new FileInputStream(caminhoPrivateKey);
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();

            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);

            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

            Signature rsa = Signature.getInstance("DSA");
            rsa.initSign(privKey);

            FileInputStream fis = new FileInputStream(caminhoDoArquivoQuemTem);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                rsa.update(buffer, 0, len);
            };
            bufin.close();

            byte[] realSig = rsa.sign();

            FileOutputStream sigfos = new FileOutputStream(caminhoAssinatura);
            sigfos.write(realSig);
            sigfos.close();
            
            System.out.println("quemTem.txt assinado...");

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }
}
