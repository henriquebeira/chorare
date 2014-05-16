/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chorare_pacote;

/**
 * Classe utilizada para guardar os votos da eleição para Tracker.
 * 
 * @author Henrique
 */

public class Voto {
    private int voto;
    private String nick;

    /**
     * Construtora da classe.
     * 
     * @param porta Porta/Identificação do Processo.
     * @param voto Número que representa o voto do Processo.
     */
    public Voto(String porta, int voto) {
        this.nick = porta;
        this.voto = voto;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String porta) {
        this.nick = porta;
    }

    public int getVoto() {
        return voto;
    }

    public void setVoto(int voto) {
        this.voto = voto;
    }
    
    
}
