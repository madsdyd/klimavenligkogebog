package dydensborg.dk;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        // create Options object
        Options options = new Options();

        int budgetYear = 2017;
        String outputFileName = "recepies.tex";

        // add options
        options.addOption("h", "help", false, "print this message");
        options.addOption("o", "output", true, "output file [" + outputFileName + "]");

        // generate Help Formatter
        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            formatter.printHelp( "cookbook [OPTIONS] ingredients.json recepies.json", "Where options can be:", options, null );
            System.exit(1);
        }

        if (line.hasOption("help")) {
            formatter.printHelp( "cookbook [OPTIONS] ingredients.json recepies.json", "Where options can be:", options, null );
            System.exit(0);
        }

        // Test we have two free options, or complain
        if ( line.getArgList().size() != 2 ) {
            System.err.println("You need to supply two arguments: The list of ingredients, and the list of recepies.");
            formatter.printHelp( "cookbook [OPTIONS] ingredients.json recepies.json", "Where options can be:", options, null );
            System.exit(1);
        }

        String ingredientsFile = line.getArgList().get(0);
        String recipesFile = line.getArgList().get(1);





        //////////////////////////////////////////////////
        // Parse the data

        System.out.println("Parsing ingredients from " + ingredientsFile);
        List<Ingredient> ingredients = null;
        try {
            ingredients = Ingredient.fromJsonFile(Paths.get(ingredientsFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Debug
        ingredients.forEach(System.out::println);

        System.out.println("Parsing recipes from " + recipesFile);
        List<Recipe> recipes = null;
        try {
            recipes = Recipe.fromJsonFile(Paths.get(recipesFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Debug
        recipes.forEach(System.out::println);



        ///////////////////////////////////////////////////
        // Generate output.


        System.out.println( "Everything passed" );

    }
}
