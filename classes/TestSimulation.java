import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.io.IOException;
import java.io.File;
/*
 * class TestSimulation :
 * le main du projet
 */
public class TestSimulation {
    public static void main(String[] args) throws InterruptedException{

		int nb_iters;
		int nb_fourmis;
		//test des arguments
		switch (args.length){
			case 0 : 
				nb_iters = 500;
				nb_fourmis = 50;
				break;
			
			case 1:
				nb_fourmis = Integer.valueOf(args[0]);
				nb_iters = Integer.MAX_VALUE;
				break;

			default:
				nb_iters = Integer.valueOf(args[0]);
				nb_fourmis = Integer.valueOf(args[1]);
				break;
		}
		
		//Lancement des simulations
		try { 

			Simulation sim1 = new Simulation("fichiers/sim1.txt",nb_iters,nb_fourmis);
			sim1.simuler();
			Thread.sleep(1000);   

			Simulation sim2 = new Simulation("fichiers/sim2.txt");
			sim2.simuler();
			Thread.sleep(1000); 

			Simulation sim3 = new Simulation("fichiers/sim3.txt", 500, 50);
			sim3.simuler();
			Thread.sleep(1000); 

			Simulation sim4 = new Simulation("fichiers/sim4.txt", 2000, 10000);
			sim4.simuler();
			Thread.sleep(1000); 
		}

		catch (FileNotFoundException err){ //fichier manquant
			int index = err.getMessage().indexOf(".txt");
			String missing_file_name = err.getMessage().substring(0, index)+".txt";
        	try { 
				new File(missing_file_name).createNewFile();
				System.out.println(String.format("Le fichier %s manquait, il a donc ete cree.",missing_file_name));
			}
			catch (IOException _err){
				System.out.println(String.format("Le fichier %s manque et n'a pas pu etre cree",missing_file_name));
				_err.printStackTrace();
			}
		}

		catch (NoSuchElementException err){
			System.out.println("Le fichier passe en parametre est vide");
			err.printStackTrace();
		}

		catch (InvalidFileContentException err){ //format incorrect
			if (err.getErrType() == InvalidFileContentException.type.DIMENSIONS){
				String[] errlogs = err.getMessage().split(";");
        		System.out.println(String.format("%s eu : %s ; attendu : %s", errlogs[0], errlogs[1], errlogs[2]));
			}
			else if (err.getErrType() == InvalidFileContentException.type.NOMBRE_COLONIES){
				System.out.println(String.format("Nombre de colonies différent de 1 : %s",err.getMessage()));
			}
			else{
				System.out.println(String.format("Charactère invalide présent dans le fichier : %s",err.getMessage()));
			}
			err.printStackTrace();
    	}
    }
}