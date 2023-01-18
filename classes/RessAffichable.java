import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;


/*
 * abstract class RessAffichable :
 * class abstract d'une case Ressource affichable
 */
public abstract class RessAffichable extends Ressource implements Affichage{

    public static int div; //Taille d'une case en pixels

    //constructeur
    public RessAffichable(String type, int quantite){
        super(type,quantite);
    }

    public abstract void dessiner(Graphics g, int x, int y);

}