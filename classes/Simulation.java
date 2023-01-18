import java.awt.Graphics;
import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter; 
import java.util.Scanner;
import java.io.IOException;

import java.time.*;

/**
 * class simulation :
 * base de toute la simulation
 */
public class Simulation extends JPanel{

    //parametres de la fenetre
    private final int TAILLE_ECRAN_X;
    private final int TAILLE_ECRAN_Y;
    private static final int OFFSET = 0;
    private static final int T_WAIT = 40;
    private static int TAILLE_DIV=50;


    private final int nb_iters; //nombre d'iterations
    private int colo_coors[] = {0,0}; //coordonees de la colonie
    private final int nb_fourmis; //nombre de fourmis
    private int total = 0;

    private Terrain terrain; //le terrain
    private Fourmi[] array_fourmis; //l'array de fourmi
    private ArrayList<int[]> evolution_quantites; //Pour les logs
    private final String nom_fichier;

    /**
    * Constructeur de Simulation
    * @param filename Nom du fichier depuis lequel lire
    * @param nb_iters Nombre d'iterations auquel faire un arret de la simulation si elle n'est pas terminee.
    * @param nb_fourmis Nombre de fourmis dans la simulation
    * @exception FileNotFoundException Si le fichier indique ne correspond pas
    * @exception InvalidFileContentExecption Si le fichier a un mauvais formattage (Nombre de lignes ou colonnes incorrectes, 0 ou 2+ colonnies, mauvais charactere)
    */
    public Simulation(String filename, int nb_iters, int nb_fourmis) throws FileNotFoundException, InvalidFileContentException{

        this.nom_fichier = filename;
        //recuperation des arguments du main
        this.nb_iters = nb_iters;
        this.nb_fourmis = nb_fourmis;
        this.array_fourmis = new Fourmi[nb_fourmis];
        this.evolution_quantites = new ArrayList< int[]>(nb_iters+1);
        int[] dimensions = {0,0};

        //lecture du fichier terrain
        File fileTerrain = new File(filename);
        Scanner readerTerrain = new Scanner(fileTerrain);
        for (int h=0; h<2; h++) {
            String data = readerTerrain.nextLine();
            dimensions[h] = Integer.valueOf(data.substring(2));
        }
        this.terrain = new Terrain(dimensions[1],dimensions[0]);
        RessAffichable.div=TAILLE_DIV;
        TAILLE_ECRAN_X=(TAILLE_DIV)*this.terrain.nbColonnes;
        TAILLE_ECRAN_Y=(TAILLE_DIV)*this.terrain.nbLignes+38;

        int ligne = 0;
        int nb_colonies = 0;

        String data;

        //On remplit le terrain des ressources lues
        while (readerTerrain.hasNextLine() && ligne<this.terrain.nbLignes) {

            data = readerTerrain.nextLine();
            String[] arr = data.split(",");

            if (arr.length!=this.terrain.nbColonnes)
                throw new InvalidFileContentException("Colonnes",arr.length,this.terrain.nbColonnes); //exception

            for (int colonne=0; colonne<arr.length; colonne++){
                    
                switch (arr[colonne]) {
                    case "C" :
                        this.terrain.setCase(ligne, colonne, new Colonie());
                        this.colo_coors[0]=ligne;
                        this.colo_coors[1]=colonne;
                        nb_colonies++;
                        break;
                    case "O" :
                        this.terrain.setCase(ligne,colonne, new Obstacle());
                        break;
                    case "N" :
                        this.terrain.setCase(ligne,colonne, new Nourriture(250));
                        this.total+=250;
                        break;
                    case " ":
                        break;
                    default :
                        throw new InvalidFileContentException(arr[colonne]); //exception
                }
            }

            ligne++;
        }

        //Si le contenu du fichier est invalide
        if (ligne!=this.terrain.nbLignes)
            throw new InvalidFileContentException("Ligne",ligne,this.terrain.nbLignes);

        if (nb_colonies!=1)
            throw new InvalidFileContentException(nb_colonies);


        readerTerrain.close();

        for (int n=0; n<this.nb_fourmis;n++){
            this.array_fourmis[n] = new Fourmi(this.colo_coors,this.terrain.nbLignes,this.terrain.nbColonnes);
        }

    }

    /**
    * Constructeur de Simulation, mais avec un nombre par default d'iterations (32767) et de fourmis (500)
    * @param filename Nom du fichier depuis lequel lire
    */
    public Simulation(String filename) throws FileNotFoundException, InvalidFileContentException {
        this(filename,Short.MAX_VALUE,500);
    }
 
    /**
    * Simule les fourmis
    */
    public void simulerFourmis(){
        for (Fourmi f : this.array_fourmis){
            int[] delta_dir = f.bouger(this.terrain);
            f.rapporter(this.terrain);
            f.deposerNourriture(this.terrain);
            f.setPheromone(this.terrain, delta_dir[0], delta_dir[1]);
        }
    }

    /**
    * Met a jour les pheromones
    */
    public void updatePheromones(){
        Ressource r;
        for (int x=0; x<this.terrain.nbLignes; x++){
            for (int y=0; y<this.terrain.nbColonnes; y++){
                r=this.terrain.getCase(x,y);

                if (r instanceof Pheromone){
                    Pheromone r_cast = (Pheromone)r;
                    r_cast.updatePheromone();
                    if (r_cast.getQuantite()<=0 && r_cast.isColoNULL() && r_cast.isNourNULL()){
                        this.terrain.videCase(x,y);
                    }
                }
            }
        }
    }

    private static final BasicStroke default_stroke = new BasicStroke(); //eppaisseur du cadrillage

    //dessine le plateau de jeu
    private void dessinerRessources(Graphics g){

        for (int x=0; x<this.terrain.nbLignes; x++){
            for (int y=0; y<this.terrain.nbColonnes; y++){
                Ressource r = this.terrain.getCase(x,y);
                if (r!=null){
                    ((Affichage)r).dessiner(g, x, y);
                }
            }
        }

    }

    //dessine le cadrillage
    private void dessinerQuadrillage(Graphics g){

        for (int x=0; x<=this.terrain.nbColonnes; x++){
            g.setColor(new Color(0,0,0,100));
            g.drawRect(TAILLE_DIV*x,0,2,TAILLE_ECRAN_Y);
            g.fillRect(TAILLE_DIV*x-2,0,4,TAILLE_ECRAN_Y);
        }

        for (int y=0; y<=this.terrain.nbLignes; y++){
            g.setColor(new Color(0,0,0,50));
            g.drawRect(0,TAILLE_DIV*y,TAILLE_ECRAN_X,2);
            g.fillRect(0,TAILLE_DIV*y-2,TAILLE_ECRAN_X,4);
        }

    }

    //dessine toutes les fourmis
    private void dessinerFourmis(Graphics g){

        for (Fourmi f : array_fourmis){
            f.dessiner(g,0,0);
        }

    }

    //dessine la quantite de nourriture dans la colonie
    private void dessinerTotal(int x, int y, Graphics g){

        g.setColor(new Color(0,0,0,255));
        g.drawString(String.valueOf(this.terrain.getCase(colo_coors[0],colo_coors[1]).getQuantite()), x*TAILLE_DIV+10, y*TAILLE_DIV+TAILLE_DIV/2+5);
    
    }

    //dessine tout
    private final Image background_img = new ImageIcon("fichiers/background.png").getImage();

    @Override
    public void paint(Graphics g){
        g.drawImage(background_img,0,0, this);
        dessinerRessources(g);
        ((Graphics2D)g).setStroke(Simulation.default_stroke);
        dessinerQuadrillage(g);
        dessinerFourmis(g);
	}

    /**
    * Log les données de la simulation
    * @param nb_iters_pris le nombre d'itérations que la simulation a pris
    */
    public void log(int nb_iters_pris){

        boolean arret_par_iter = (nb_iters_pris>=this.nb_iters);

        int index = this.nom_fichier.indexOf(".txt");
        String nom_file_log = this.nom_fichier.substring(0, index) + ".log";

        Ressource colo = this.terrain.getCase(colo_coors[0],colo_coors[1]);

        try {

            File logFile = new File(nom_file_log);
            FileWriter myWriter = new FileWriter(logFile);

            //Cause d'arret
            myWriter.write("Cause d'arret : ");

            if (arret_par_iter){
                myWriter.write("Nombre d'iterations max atteintes : " + nb_iters_pris + "\n");
                myWriter.write(String.format("Total (Recolte %d || Rapporte %d) / %d\n",Fourmi.getTotalPris(),colo.getQuantite(),this.total));
            } else {
                myWriter.write("Toute la nourriture fut recoltee\n");
                myWriter.write(String.format("Iterations prises : %d\n",nb_iters_pris));
            }

            myWriter.write("<Fourmis>\n");
            for (Fourmi f : this.array_fourmis){
                myWriter.write(f.toString()+"\n");
            }
            myWriter.write("</Fourmis>\n");

            myWriter.write("<Ressources>\n");
            Ressource r;
            for (int x=0; x<this.terrain.nbLignes; x++){
                for (int y=0; y<this.terrain.nbColonnes; y++){
                    r=this.terrain.getCase(x,y);

                    if (r!=null){
                        myWriter.write(r.toString()+"\n");
                    }
                }
            }
            myWriter.write("</Ressources>\n");

            myWriter.write("<Evolution>\n");
            for (int[] qtes : this.evolution_quantites){
                myWriter.write(String.format("%d|%d\n", qtes[0], qtes[1]));
            }
            myWriter.write("</Evolution>\n");


            myWriter.close();
            System.out.println("Logs crees avec succes :)");
        }
    
        catch (IOException e) {
            System.out.println("Erreur lors de la creation des logs :(");
            e.printStackTrace();
        }
    }


    /**
    * Ceci est la boucle de simulation
    */
    public void simuler() throws InterruptedException{

        //initialisation de la fenetre     
        JFrame frame = new JFrame("SimulANTion");
        frame.getContentPane().add(this);
        frame.setSize(TAILLE_ECRAN_X,TAILLE_ECRAN_Y);
        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Instant time_before;
        Instant time_after;
        long time_spent;

        int i;
        Ressource colo = this.terrain.getCase(colo_coors[0],colo_coors[1]);

        //boucle de simulation
        for (i=0; (i<=this.nb_iters && colo.getQuantite()<this.total); i++){
            time_before = Instant.now();
            
            this.evolution_quantites.add(new int[] {colo.getQuantite(), Fourmi.getTotalPris()});
            this.simulerFourmis();
            this.updatePheromones();
            frame.repaint();
            frame.setTitle(String.format("SimulANTion |||| Iteration %d |||| %d/%d/%d",i,colo.getQuantite(),Fourmi.getTotalPris(),this.total));

            //attente entre les iteration pour la lisibilite
            time_after = Instant.now();
            time_spent = Duration.between(time_before, time_after).toMillis();
            if (time_spent < T_WAIT)
                Thread.sleep(T_WAIT - time_spent);
        }

        //affichage final
        frame.repaint();
        frame.setTitle(String.format("Fini! SimulANTion |||| Iteration %d |||| %d/%d/%d",i,colo.getQuantite(),Fourmi.getTotalPris(),this.total));
        this.log(i);
        Fourmi.reset();
    }
}