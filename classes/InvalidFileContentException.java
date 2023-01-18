/*
 * exception InvalidFileContentException :
 *  erreur dans la lecture du fichier terrain.txt
 */
public class InvalidFileContentException extends Exception{

    public static enum type{
        DIMENSIONS,
        NOMBRE_COLONIES,
        CHARACTERE
    } //type de l'exeption

    public final InvalidFileContentException.type err; 

    //constructeur erreur : dimensions incorrectes
    public InvalidFileContentException(String type, int got, int expected){
        super(String.format("%s;%d;%d", type, got, expected));
        this.err=InvalidFileContentException.type.DIMENSIONS;
    }

    //constructeur erreur : nombre de colonie différent de 1
    public InvalidFileContentException(int nb_colonies){
        super(String.valueOf(nb_colonies));
        this.err=InvalidFileContentException.type.NOMBRE_COLONIES;
    }

    //constructeur erreur : caractere invalide
    public InvalidFileContentException(String char_case){
        super(char_case);
        this.err=InvalidFileContentException.type.CHARACTERE;
    }

    //getter err pour connaitre le type d'erreur
    public InvalidFileContentException.type getErrType(){
        return this.err;
    }

}