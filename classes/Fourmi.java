import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;

/*
 * class Fourmi :
 * Les agents de la simulation
 */
public class Fourmi implements Affichage{

    private double x; //position x
    private double y; //position y
    private double angle; //angle de la fourmi (radians)
    private AntiBoucle visites = new AntiBoucle(); //Systeme pour eviter les boucles

    private int qte_nourriture=0; //quantite de nourriture portee
    private final int qte_max; //quantite max de nourriture portable


    private static int total_pris=0; //total de nourriture transportee
    private final static double VITESSE_FOURMI = 0.084; //vitesse de la fourmi (par a port au terrain)
    private final static double ROTATION_FOURMI = 0.4; //vitesse de rotation

    public static enum objectif {
        CHERCHE, //Cherche de la nourriture
        RAPPORTE, //Rapporte de la nourriture
        DERNIERE, //Rapporte la derniere nourriture d'un amat
    } //mode d'opperation de la fourmi

    private objectif mode = objectif.CHERCHE;

    /**
    * Constructeur de Fourmi
    * @param coors tableau de 2 ints representant la case sur laquelle faire apparaitre la fourmi
    * @param taille_terrain_x nombre de lignes du terrain, pour ne pas deborder
    * @param taille_terrain_y nombre de colonnes du terrain, pour ne pas deborder
    */
    public Fourmi(int coors[], int taille_terrain_y, int taille_terrain_x){
        this.x = (double)coors[0]+0.5 + (Math.random()-0.5)*0.5;
        this.y = (double)coors[1]+0.5 + (Math.random()-0.5)*0.5;
        this.angle = Math.random()*Math.PI*2;
        this.qte_max = (int)(Math.random()*5)+2;
    }

    /**
    * Constructeur de copie
    * @param other la fourmi a copier
    * Mode et qte_nourriture ne sont pas copies car sinon cela dupliquerait de la nourriture.
    */
    public Fourmi(Fourmi other){
        this.x = other.x; this.y = other.y; this.angle = other.angle;
        this.qte_max = other.qte_max;
        this.visites = new AntiBoucle(other.visites);
    }

    public int getCaseX(){
        return (int)Math.floor(this.x);
    }

    public int getCaseY(){
        return (int)Math.floor(this.y);
    }

    public void seDeplacer(double new_x, double new_y, double new_angle){
        this.x = new_x;
        this.y = new_y;
        this.angle = new_angle;
    }

    public boolean isModeCHERCHE(){
        return this.mode==Fourmi.objectif.CHERCHE;
    }

    public static int getTotalPris(){
        return Fourmi.total_pris;
    }

    @Override
    public String toString(){
        return String.format("X:%.3f ; Y:%.3f ; Angle : %.3f, Cases visitees sous influence : %s", this.x, this.y, this.angle, this.visites.toString());
    }

    //reset le total_pris
    public static void reset(){
        Fourmi.total_pris=0;
    }

    private int keepInBetween(int nb, int min, int max){
        return Math.max( Math.min(nb,max), min);
    }

    //renvoie si les coordonnees ne sortent pas du terrain
    private boolean isInBounds(int y, int x, int tx, int ty){
        return (
                ((x < ty) &&
                (x >= 0)) &&
                ((y < tx) &&
                (y >= 0))
                );
    }

    //renvoie la distance entre la fourmi et une case
    public double distance(int x, int y){
        return Math.sqrt(
            Math.pow((this.x - x),2) +
            Math.pow((this.y - y),2)
        );
    }

    //renvoie si la direction est valide
    private boolean isGoodDirection(double old_x, double old_y, double x, double y, Terrain tr){

        int ix = (int)Math.floor(x);
        int iy = (int)Math.floor(y);

        if (isInBounds(ix,iy,tr.nbLignes,tr.nbColonnes)) {
            if ((tr.getCase(ix,iy) instanceof Obstacle))
                return false;
            else //Dans le cas ou la fourmi se deplace en diagonale, verifie s'il n'y a pas des murs avant.
                return !((tr.getCase(ix,this.getCaseY()) instanceof Obstacle) && (tr.getCase(this.getCaseX(),iy) instanceof Obstacle));
        }
        return false;
    }

    /**
    * Depose des pheromones sur le terrain, modifiant ou creant des pheromones s'il y en a deja ou non
    * @param tr le terrain sur lequel deposer les pheromones
    * @param delta_x Direction x de la precedante case de la fourmi (-1,0,1)
    * @param delta_y Direction y de la precedante case de la fourmi (-1,0,1)
    */ 
    public void setPheromone(Terrain tr, int delta_x, int delta_y){
        
        //Ne pas ajouter des pheromones inutiles
        if (delta_x==0 && delta_y==0) return;
        

        int cx = this.getCaseX();
        int cy = this.getCaseY();

        //ajouter des anti pheromones si la fourmi est la derniere
        if (this.mode == objectif.DERNIERE){
            for (int i=-2; i<=2; i++){
                for (int j=-2; j<=2; j++){
                    Ressource r1 = tr.getCase(cx+i, cy+j);
                    if (r1 instanceof Pheromone){
                        ((Pheromone)r1).addFini(keepInBetween(540 - (int)Math.pow(distance(cx+i,cy+j),2.5)*120, 25, 300));
                    }
                }
            }
            return;
        }

        //S'il n'y a aucun pheromone a cet endroit
        Ressource r = tr.getCase(cx,cy);
        if (r==null)
            tr.setCase(cx, cy, new Pheromone(delta_x,delta_y, this.mode));

        
        else if (r instanceof Pheromone){
            Pheromone r_cast = ((Pheromone)r);
            
            //empecher que 2 cases se pointent l'une à l'autre
            if (r_cast.isSameNotNULL(this.mode)){

                int[] suivant = r_cast.getPherSameSuivant(this.mode);
                Ressource r_suivant = tr.getCase(cx+suivant[0],cy+suivant[1]);

                if (r_suivant instanceof Pheromone){

                    Pheromone r_suivant_cast = (Pheromone)r_suivant;

                    if (r_suivant_cast.isSameNotNULL(this.mode)){

                        int[] suivant_suivant = r_suivant_cast.getPherSameSuivant(this.mode);
                        if ((suivant_suivant[0] == -suivant[0]) && (suivant_suivant[1] == -suivant[1])){
                            
                            //La pheromone la plus forte prend le dessus
                            if (r_suivant_cast.getPherSameQte(this.mode)>r_cast.getPherSameQte(this.mode))
                                r_cast.videPherSame(this.mode);
                            else
                                r_suivant_cast.videPherSame(this.mode);
                            
                        }
                    }
                }
            }

            r_cast.addPheromone(delta_x,delta_y,this.mode);
        }

    }

    /**
    * Renvoie si la fourmi est influencee par des pheromones
    * @param phm Les pheromones sur lequels la fourmi se trouve
    */
    public boolean isInfluencee(Pheromone phm){

        if (this.mode==objectif.CHERCHE){
            if (phm.isPherNONE())
                return !phm.isNourNULL();
            return false;
        }
        else{
            if (!phm.isPherBOUCLE())
                return !phm.isColoNULL();
            return false; 
        }
    }

    /**
    * Renvoie si la fourmi est influencee par des pheromones
    * @param tr Le terrain sur lequel la fourmi est.
    */
    public boolean isInfluencee(Terrain tr){
        Ressource r = tr.getCase(this.getCaseX(),this.getCaseY());
        if (!(r instanceof Pheromone))
            return false;
            
        return isInfluencee((Pheromone)r);
    }


    //renvoie l'inverse du mode de la fourmi
    private Fourmi.objectif inv_mode(){
        if (this.mode==Fourmi.objectif.CHERCHE)
            return Fourmi.objectif.RAPPORTE;
        return Fourmi.objectif.CHERCHE;
    }

    /**
    * Fait bouger la fourmi
    * La fait bouger dans une direction precise si elle est influencee
    * Sinon on scan a droite & a gauche pour trouver une direction valide
    * @param terrain Terrain sur lequel se deplace la fourmi
    */
    public int[] bouger(Terrain terrain){

        this.angle += (Math.random()*ROTATION_FOURMI) - ROTATION_FOURMI/2; // changement de l'angle

        double new_angle_left = this.angle;
        double new_angle_right = this.angle;

        double new_x_left = this.x + Math.cos(new_angle_left)*VITESSE_FOURMI;
        double new_y_left = this.y + Math.sin(new_angle_left)*VITESSE_FOURMI;
        double new_x_right = new_x_left;
        double new_y_right = new_y_left;

        double final_x;
        double final_y;

        //S'il y a une boucle
        int old_x = this.getCaseX();
        int old_y = this.getCaseY();  
        
        if (this.visites.enBoucle()){
            for (int x=-2; x<=2; x++){
                for (int y=-2; y<=2; y++){
                    if (distance(old_x+x,old_y+y)<3){
                        Ressource r = terrain.getCase(old_x+x,old_y+y);
                        if (r instanceof Pheromone)
                            ((Pheromone)r).setBoucle(this.mode==Fourmi.objectif.CHERCHE);   
                    }   
                }
            }
        }

        //L'influencer s'il y a une influence
        Ressource r = terrain.getCase(this.getCaseX(),this.getCaseY());
        if (r instanceof Pheromone){
            Pheromone phm = (Pheromone)r;
            if ((phm.isSameNotNULL(this.inv_mode())) && (this.isInfluencee(phm))){ 
                if ((int)Math.random()*25<phm.getPherSameQte(this.inv_mode())){ //Desobeisance si pheromone trop faible
                    new_angle_left = phm.getPherSameAngle(this.inv_mode()) - (Math.random() - 0.5);
                    new_angle_right = new_angle_left;
                }
            }
        }  


        //Scan pour une direction valide si aucune influence
        for (int _i=0; _i<4; _i++){


            if (isGoodDirection(this.x,this.y,new_x_left,new_y_left,terrain)){
                
                this.seDeplacer(new_x_left,new_y_left,new_angle_left);
                if (!(this.getCaseX()==old_x && this.getCaseY()==old_y)){
                    if (this.isInfluencee(terrain))
                        this.visites.addCase(this.getCaseX(),this.getCaseY());            
                }
                return new int[]  {old_x - this.getCaseX(),old_y - this.getCaseY()};
            }

            else if (isGoodDirection(this.x,this.y, new_x_right,new_y_right,terrain)){

                this.seDeplacer(new_x_right,new_y_right,new_angle_right);

                if (!(this.getCaseX()==old_x && this.getCaseY()==old_y)){
                    if (this.isInfluencee(terrain))
                        this.visites.addCase(this.getCaseX(),this.getCaseY());
                }
                return new int[] {old_x - this.getCaseX(),old_y - this.getCaseY()};
            }

            new_angle_left += ROTATION_FOURMI;
            new_angle_right -= ROTATION_FOURMI;
            new_x_left = this.x + Math.cos(new_angle_left)*VITESSE_FOURMI;
            new_y_left = this.y + Math.sin(new_angle_left)*VITESSE_FOURMI;
            new_x_right = this.x + Math.cos(new_angle_right)*VITESSE_FOURMI;
            new_y_right = this.y + Math.sin(new_angle_right)*VITESSE_FOURMI;
        }

        // Cas ou il n'y a pas de direction valide : par default la faire tourner vers la gauche
        this.angle = new_angle_left;
        return new int[]  {old_x - this.getCaseX(),old_y - this.getCaseY()};

    }


    /**
    * Fonction pour prendre de la nourriture si possible
    * @param tr le terrain sur lequel prendre de la nourriture
    */
    public void rapporter(Terrain tr){

        int cx = this.getCaseX();
        int cy = this.getCaseY();
        Ressource r = tr.getCase(cx,cy);

        //S'il n'y a pas de nourriture à rapporter (par manque ou par objectif)
        if (!((r instanceof Nourriture) && (this.mode == objectif.CHERCHE) && (this.qte_max > this.qte_nourriture)))
            return;
                    
        int qte_prise = Math.min(r.getQuantite(),this.qte_max) - this.qte_nourriture;

        //Si elle ne rapporte rien
        if (qte_prise<=0)
            return;

        r.setQuantite(r.getQuantite() - qte_prise);
        this.qte_nourriture+=qte_prise;
        Fourmi.total_pris+=qte_prise;
        this.mode = objectif.RAPPORTE;
        this.angle -= Math.PI;
        this.visites.clean();
                
        //Si elle a fini la ressource
        if (r.getQuantite()<=0){

            this.mode = objectif.DERNIERE;
            //verifie si il y a de la nourriture aux alentours            
            for (int x=-1; x<=1; x++){
                for (int y=-1; y<=1; y++){
                    Ressource voi = tr.getCase(this.getCaseX()+x,this.getCaseY()+y);
                    if (voi instanceof Nourriture){
                        if (voi.getQuantite()>0)
                            this.mode = objectif.RAPPORTE;
                    }
                }
            }
            tr.videCase(cx,cy);
            tr.setCase(cx,cy,new Pheromone(this.mode == objectif.DERNIERE)); //Si derniere nourriture alors on met plus d'anti feromones
        }

    }

    /**
    * Fonction pour deposer de la nourriture si possible
    * @param tr le terrain sur lequel prendre de la nourriture
    */
    public void deposerNourriture(Terrain tr){
        int cx = this.getCaseX();
        int cy = this.getCaseY();
        Ressource r = tr.getCase(cx,cy);
        if (r instanceof Colonie){
            if (this.mode!=objectif.CHERCHE){
                r.setQuantite(r.getQuantite()+this.qte_nourriture);
                this.qte_nourriture=0;
                this.mode = objectif.CHERCHE;
                this.angle = Math.random()*Math.PI;
                this.visites.clean();
            }
        }
    }

    //coloration
    public static final Color couleur_fourmi = new Color(50,0,0,255);
    public static final Color couleur_derniere = new Color(0,255,0,255);
    public static final Color couleur_cherche = new Color(0,0,255,255);
    public static final Color couleur_rapporte = new Color(255,0,0,255);

    //dessine la fourmi sur la fenetre
    public void dessiner(Graphics g, int y, int x){
        
        switch (this.mode){
            case CHERCHE:
                g.setColor(Fourmi.couleur_cherche);
                break;
            case RAPPORTE:
                g.setColor(Fourmi.couleur_rapporte);
                break;
            default: //DERNIERE
                g.setColor(Fourmi.couleur_derniere);
                break;
        }
        g.fillOval((int)(this.y*RessAffichable.div)-3,(int)(this.x*RessAffichable.div)-3,9,9);

        g.setColor(Fourmi.couleur_fourmi);
        g.fillOval((int)(this.y*RessAffichable.div)-2,(int)(this.x*RessAffichable.div)-2,7,7);

    }

}