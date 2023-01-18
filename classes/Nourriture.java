import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;


/*
 * class Nourriture :
 * case nourriture
 */
public class Nourriture extends RessAffichable{

    //couleur
    private static final Color couleur = new Color(255,100,180,255);

    //constructeur
    public Nourriture(int quantite){
        super("Nourriture",quantite);
    }

    //dessine la case nourriture
    public void dessiner(Graphics g, int y, int x){
        
        g.setColor(Nourriture.couleur);
        g.drawRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
        g.setColor(new Color(255,100,180,this.getQuantite()));
        g.fillRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);

    }

}

