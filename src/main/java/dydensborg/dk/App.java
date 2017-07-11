package dydensborg.dk;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {

        // create Options object
        Options options = new Options();

        int numPersons = 4;
        String outputFileName = "recepies.tex";

        // add options
        options.addOption("h", "help", false, "print this message");
        options.addOption("o", "output", true, "output file [" + outputFileName + "]");
        options.addOption("p", "persons", true, "how many persons to create for [" + numPersons + "]");

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

        if (line.hasOption("persons")) {
            numPersons = Integer.parseInt(line.getOptionValue("persons"));
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

        // Create a hash for quicker lookup with many ingredients.
        Map<String, Ingredient> ingredientMap = new HashMap<>();
        ingredients.forEach(i -> ingredientMap.put(i.getId(), i));

        // Debug
        System.out.println("Dumping all read ingredient data");
        ingredients.forEach(System.out::println);

        System.out.println("Parsing recipes from " + recipesFile);
        List<Recipe> recipes = null;
        try {
            recipes = Recipe.fromJsonFile(Paths.get(recipesFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        for (Recipe r : recipes) {
            r.adjustToPersons(numPersons);
            r.calculateCo2(ingredientMap);

        }

        // Debug
        System.out.println("Dumping all read recipe data");
        recipes.forEach(System.out::println);



        ///////////////////////////////////////////////////
        // Generate output.
        StringBuilder output = new StringBuilder();
        for(Recipe r : recipes) {
            output.append(r.toTex(ingredientMap));
        }

        // System.out.print(output.toString());

        FileUtils.writeStringToFile(new File(outputFileName), output.toString(), Charset.forName("UTF-8"), false);

        System.out.println( "Output written to " + outputFileName );

    }
}
