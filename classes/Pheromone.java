import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * class pheromone :
 * indicateur de direction pour les agents
 * utilisation : quantite correspond à la quantite de pheromone anti-influence (FINI,BOUCLE) et non au total de vers_colo et vers_nour
 */
public class Pheromone extends RessAffichable{

    public static final int TAUX_NOUR_MAX=500; //quantite de pheromone maximum pour le chemin vers la nouriture
    public static final int TAUX_COLO_MAX=2500; //quantite de pheromone maximum pour le chemin vers la colonie

    public static enum types_diff_phers{
        BOUCLE,
        FINI,
        NONE,
    } //type de pheromones 

    private types_diff_phers type_pher = types_diff_phers.NONE; //a l'initialisation

    private int[] vers_colo = null; //tableau de direction pour la direction de la colonie [x precedant,y precedant,taux]
    private int[] vers_nour = null; //tableau de direction pour la direction de la nourriture [x precedant,y precedant,taux]

    /**
    * Constructeur de Pheromone
    * @param delta_x Direction X vers laquelle le pheromone pointe (-1,0,1)
    * @param delta_y Direction Y vers laquelle le pheromone pointe (-1,0,1)
    * @param mode Si la fourmi cherche, elle depose des pheromones de colonie, sinon de nourriture
    */
    public Pheromone(int delta_x, int delta_y, Fourmi.objectif mode){
        super("Pheromone",0);
        //si la fourmi qui pose les pheromones est rapporteuse
        if (mode == Fourmi.objectif.RAPPORTE)
            this.vers_nour = new int[] {delta_x,delta_y,TAUX_NOUR_MAX};
        //si elle cherche de la nourriture
        else
            this.vers_colo = new int[] {delta_x,delta_y,TAUX_COLO_MAX};
        //si la fourmie a recolter la derniere nourriture d'une case
        if (mode == Fourmi.objectif.DERNIERE){
            this.type_pher = types_diff_phers.FINI;
            super.setQuantite(510);
        }
    }

    /**
    * Constructeur de Pheromone de type FINI
    * @param wasFoodLast Si la nourriture recoltee etait la derniere, mettre plus de phermones
    */
    public Pheromone(boolean wasFoodLast){
        super("Pheromone",1);
        this.type_pher = types_diff_phers.FINI;
        if (wasFoodLast){
            this.setQuantite(500);
        }
        else{
            this.setQuantite(200);
        }
    }

    @Override
    public String toString(){

        String info_colo;
        if (vers_colo!=null)
            info_colo = String.format(" Vers colonie [ Direction : %d,%d | Taux : %d ]",
                                            this.vers_colo[0],this.vers_colo[1],this.vers_colo[2]);
        else
            info_colo = " Aucun pheromone vers colonie";
        
        String info_nour;
        if (vers_nour!=null)
            info_nour = String.format(" Vers nourriture [ Direction : %d,%d | Taux : %d ]",
                                            this.vers_nour[0],this.vers_nour[1],this.vers_nour[2]);
        else
            info_nour = " Aucun pheromone vers nourriture";

        return super.toString() + info_colo + info_nour;
    }

    //modifie la quantite de pheromone si le parametre present est plus grand que la quantite deja present
    private void setQuantiteIfLower(int qte){
        super.setQuantite(Math.max(qte,super.getQuantite()));
    }

    public types_diff_phers getTypePher(){
        return this.type_pher;
    }

    public boolean isPherNONE(){
        return this.type_pher==types_diff_phers.NONE;
    }

    public boolean isPherBOUCLE(){
        return this.type_pher==types_diff_phers.BOUCLE;
    }
    
    public boolean isNourNULL(){
        return this.vers_nour==null;
    }

    public boolean isColoNULL(){
        return this.vers_colo==null;
    }

    //test si il existe des pheromone du meme type que le mode d'opperation de la fourmi
    public boolean isSameNotNULL(Fourmi.objectif mode){
        if (mode==Fourmi.objectif.CHERCHE)
            return this.vers_colo!=null;
        return this.vers_nour!=null;
    }

    //detruit le pheromone de meme type au mode d'opperation de la fourmi
    public void videPherSame(Fourmi.objectif mode){
        if (mode==Fourmi.objectif.CHERCHE)
            this.vers_colo=null;      
        else
            this.vers_nour=null;
    }

    //rend la quantite des pheromone correspondant au mode d'opperation de la fourmi
    public int getPherSameQte(Fourmi.objectif mode){
        if (mode==Fourmi.objectif.CHERCHE){
            return this.vers_colo[2];
        }
        return this.vers_nour[2];
    }

    //rend la direction des pheromone correspondant au mode d'opperation de la fourmi
    public int[] getPherSameSuivant(Fourmi.objectif mode){
        if (mode==Fourmi.objectif.CHERCHE){
            if (this.vers_colo!=null)
                return new int[] {this.vers_colo[0],this.vers_colo[1]};
        }
        else{
            if (this.vers_nour!=null)
                return new int[] {this.vers_nour[0],this.vers_nour[1]};
        }
        return null;
    }

    //rend l'angle de la direction des pheromone correspondant au mode d'opperation de la fourmi
    public double getPherSameAngle(Fourmi.objectif mode){
        int[] suivant = getPherSameSuivant(mode);
        return Math.atan2(suivant[1] , suivant[0]);
    }

    /**
    * Mets ses caracteristiques a jour, fonction appelee une fois par tick
    */
    public void updatePheromone(){

        //test pour nullifier les pheromone disparus
        if (this.type_pher == types_diff_phers.FINI && this.vers_nour!=null){
            this.vers_nour[2]-=5;
            if (this.vers_nour[2]<=0){
                this.vers_nour=null;
            }
        }

        //decrementation des pheromone
        int qte_fini = super.getQuantite();
        if (qte_fini<=3){
            this.type_pher = types_diff_phers.NONE;
            super.setQuantite(0);
        }
        else
            super.setQuantite(qte_fini - 2);
    
        if (this.vers_colo!=null){
            this.vers_colo[2]-=1;
            if (this.vers_colo[2]<=0){
                this.vers_colo=null;
            }
        }
        if (this.vers_nour!=null){
            this.vers_nour[2]-=2;
            if (this.vers_nour[2]<=0){
                this.vers_nour=null;
                if (this.type_pher == types_diff_phers.FINI)
                this.type_pher = types_diff_phers.NONE;
            }
        }

    }

    /**
    * Ajoute des pheromones a celui la deja present, se met a jour avec les nouvelles donnees de facon a etre le plus utile pour les fourmis possible.
    * @param delta_x Direction X vers laquelle le depot pointe (-1,0,1)
    * @param delta_y Direction Y vers laquelle le depot pointe (-1,0,1)
    * @param mode Si la fourmi cherche, elle depose des pheromones de colonie, sinon de nourriture
    */
    public void addPheromone(int delta_x, int delta_y, Fourmi.objectif mode){

        //si elle est a recuperer le dernier morceau de nourriture, alors ne rien faire
        if (mode == Fourmi.objectif.DERNIERE){
            return;
        }

        if (mode == Fourmi.objectif.RAPPORTE){

            if (this.type_pher == types_diff_phers.NONE){

                if (this.vers_nour==null || this.vers_nour[2]<20)
                    this.vers_nour = new int[] {delta_x,delta_y,TAUX_NOUR_MAX};

                else{
                    int pro_scal = delta_x*this.vers_nour[0] + delta_y*this.vers_nour[1] + 1;
                    this.vers_nour[2]+=pro_scal*7;
                    this.vers_nour[2] = Math.min(this.vers_nour[2],TAUX_NOUR_MAX);
                    this.vers_nour[2] = Math.max(this.vers_nour[2],0);
                }
            }
        }
        //si la fourmi cherche de la nourriture
        else{
            if (this.vers_colo==null)
                this.vers_colo = new int[] {delta_x,delta_y,TAUX_COLO_MAX};
            
            else{
                int pro_scal = delta_x*this.vers_colo[0] + delta_y*this.vers_colo[1] + 1;
                this.vers_colo[2]+=pro_scal*7 + 3;
                this.vers_colo[2] = Math.min(this.vers_colo[2],TAUX_COLO_MAX);
                this.vers_colo[2] = Math.max(this.vers_colo[2],0);
            }
        }
    }

    //Change un pheromone NONE en FINI
    public void addFini(int qte){
        if (this.type_pher!=types_diff_phers.BOUCLE){
            this.type_pher=types_diff_phers.FINI;
            this.setQuantiteIfLower(qte);
        }
    }

    /**
    * Si la case fait partie d'une boucle, appelee par une fourmi, alors elle s'annule en fonction de l'influence de la fourmi.
    * @param isFCherche Si la fourmi est en train de chercher.
    */
    public void setBoucle(boolean isFCherche){
        super.setQuantite(120);
        this.type_pher=types_diff_phers.BOUCLE;
        if (isFCherche)
            this.vers_nour=null;
        else
            this.vers_colo=null;
    }

    //affiche le pheromone
    private static final BasicStroke stroke = new BasicStroke(5f); //eppaisseur des lignes du carre
    private static final Color couleur_fini = new Color(255,255,255,255);
    private static final Color couleur_boucle = new Color(0,0,0,255);
    private static final Color couleur_colo = new Color(Fourmi.couleur_cherche.getRed(),Fourmi.couleur_cherche.getGreen(),Fourmi.couleur_cherche.getBlue(),127);
    private static final Color couleur_nour = new Color(Fourmi.couleur_rapporte.getRed(),Fourmi.couleur_rapporte.getGreen(),Fourmi.couleur_rapporte.getBlue(),127);

    public void dessiner(Graphics g, int y, int x){
        
        ((Graphics2D)g).setStroke(Pheromone.stroke);

        //affiche si la pheromone est FINI
        if (this.type_pher==types_diff_phers.FINI){
            g.setColor(Pheromone.couleur_fini);
            g.drawRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
            g.setColor(new Color(255,255,255,this.getQuantite()/2));
            g.fillRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
        }

        //affiche si la pheromone est BOUCLE
        else if (this.type_pher==types_diff_phers.BOUCLE){
            g.setColor(Pheromone.couleur_boucle);
            g.drawRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
            g.setColor(new Color(0,0,0,this.getQuantite()));
            g.fillRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
        }

        //affiche les infos du pheromone vers la colonie
        if (this.vers_colo!=null){
            g.setColor(Pheromone.couleur_colo);
            g.drawRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
            g.setColor(new Color(0,0,255,this.vers_colo[2]*255/TAUX_COLO_MAX));
            g.drawLine(RessAffichable.div*x + RessAffichable.div/2,
                        RessAffichable.div*y + RessAffichable.div/2,
                        RessAffichable.div*x + RessAffichable.div/2 + this.vers_colo[1]*20,
                        RessAffichable.div*y + RessAffichable.div/2 + this.vers_colo[0]*20);
        }

        //affiche les infos du pheromone vers la nourriture
        if (this.vers_nour!=null){
            g.setColor(Pheromone.couleur_nour);
            g.drawRect(RessAffichable.div*x,RessAffichable.div*y,RessAffichable.div,RessAffichable.div);
            g.setColor(new Color(255,0,0,this.vers_nour[2]*255/TAUX_NOUR_MAX));
            g.drawLine(RessAffichable.div*x + RessAffichable.div/2,
                        RessAffichable.div*y + RessAffichable.div/2,
                        RessAffichable.div*x + RessAffichable.div/2 + this.vers_nour[1]*15,
                        RessAffichable.div*y + RessAffichable.div/2 + this.vers_nour[0]*15);
        }
    }

}