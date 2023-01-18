import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;

/*
 * class Obstacle :
 * case obstacle
 */
public class Obstacle extends RessAffichable{

    //couleur
    private static final Color couleur = new Color(50,50,50,255);

    //constructeur
    public Obstacle() {
        super("Obstacle",0);
    }

    //dessine le bloc
    public void dessiner(Graphics g, int y, int x){
        
        g.setColor(couleur);
        g.fillRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);

    }

}