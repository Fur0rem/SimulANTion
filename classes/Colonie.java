import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;

/*
 * class Colonie :
 * La case representant le nid des fourmies 
 */
public class Colonie extends RessAffichable{

    private static final Color couleur = new Color(250,230,90,255); //couleur du nid
    
    //constructeur
    public Colonie() {
        super("Colonie",0);
    }

    //dessine la case colonie
    public void dessiner(Graphics g, int y, int x){

        //dessine le care de la colonie
        g.setColor(couleur);
        g.fillRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
    
        //affiche sur la case le nombre de nourriture dans la colonie
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(super.getQuantite()), x*RessAffichable.div+11, y*RessAffichable.div+RessAffichable.div/2+4);

    }
 
}