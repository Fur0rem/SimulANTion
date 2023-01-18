import java.util.*;

/**
 * classe AntiBoucle :
 * Système pour eviter les boucles
 * Toute case avec coordonnees negatives n'est pas une vraie case
 */
public class AntiBoucle{

    private static int NB_BACKTRACES = 60; //nombres de case sauvegarder
    private int[][] visites = new int[AntiBoucle.NB_BACKTRACES][2]; // tableau de sauvegarde de chemin

    //constructeur
    public AntiBoucle(){
        for (int i=0; i<AntiBoucle.NB_BACKTRACES; i++){
            this.visites[i][0]=-i-1;
            this.visites[i][1]=-i-1;
        }
    }

    //Constructeur de copie
    public AntiBoucle(AntiBoucle other){
        for (int i=0; i<AntiBoucle.NB_BACKTRACES; i++){
            this.visites[i][0]=other.visites[i][0];
            this.visites[i][1]=other.visites[i][1];
        }
    }

    //vide visites
    public void clean(){
        for (int i=0; i<AntiBoucle.NB_BACKTRACES; i++){
            this.visites[i][0]=-i-1;
            this.visites[i][1]=-i-1;
        }
    }

    //ajout de case dans la liste 
    public void addCase(int x, int y){
        for (int i=0; i<AntiBoucle.NB_BACKTRACES-1; i++){
            this.visites[i][0]=this.visites[i+1][0];
            this.visites[i][1]=this.visites[i+1][1];
        }
        this.visites[AntiBoucle.NB_BACKTRACES-1][0]=x;
        this.visites[AntiBoucle.NB_BACKTRACES-1][1]=y;
        
    }

    //rend si une boucle est detectee
    public boolean enBoucle(){
        
        int i, j, occurences;
        for (i=0; i<AntiBoucle.NB_BACKTRACES; i++){
            occurences=0;
            //test du nombres d'occurence d'une case dans la liste
            for (j=0; j<AntiBoucle.NB_BACKTRACES; j++){
                if (j!=i){
                    if (this.visites[i][0]==this.visites[j][0] && this.visites[i][1]==this.visites[j][1]){
                        occurences++;
                    }
                }
            }
            //indique si une boucle est presente
            if (occurences>10){
                this.clean();
                return true;
            }

        }
        return false;
    }

    private String caseToString(int[] case_vis){
        if (case_vis[0]<0)
            return "";
        return String.format("| %d %d |",case_vis[0],case_vis[1]);
    }

    //Methode toString() qui ne renvoie que le String des cases visitées (x>=0,y>=0)
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for (int[] v : this.visites){
            str.append(caseToString(v));
        }
        return str.toString();
    }

}