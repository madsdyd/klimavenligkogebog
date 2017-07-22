package dydensborg.dk;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String recipeOutputFileName = "recipes.tex";
        String dataOutputFileName = "data.tex";
        String configFileName = "config.json";

        // add options
        options.addOption("h", "help", false, "print this message");
        options.addOption("r", "recipe", true, "recipe output file [" + recipeOutputFileName + "]");
        options.addOption("d", "data", true, "data output file [" + dataOutputFileName + "]");
        options.addOption("c", "config", true, "configuration file [" + configFileName + "]");
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

        if (line.hasOption("recipe")) {
            recipeOutputFileName = line.getOptionValue("recipe");
        }
        if (line.hasOption("data")) {
            dataOutputFileName = line.getOptionValue("data");
        }
        if (line.hasOption("config")) {
            configFileName = line.getOptionValue("config");
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
        // Parse the configuration
        Config config = Config.fromJsonFile(Paths.get(configFileName));
        System.out.println("Config: " + config.toString());

        // Create a Rater object and rate stuff.
        Rater rater = new Rater(config.ratings);


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
            r.calculateCo2(ingredientMap, rater);

        }

        // Debug
        System.out.println("Dumping all read recipe data");
        recipes.forEach(System.out::println);



        ///////////////////////////////////////////////////
        // Generate output that builds the recipes
        StringBuilder recipeOutput = new StringBuilder();

        // Sort recipes by sortorder, name, then group them
        recipes = recipes
                .stream()
                .sorted(Comparator.comparing(Recipe::getMealOrder)
                        .thenComparing(Recipe::getName))
                .collect(Collectors.toList());


        String lastSection = "";
        for(Recipe r : recipes) {
            if (!lastSection.equals(r.getMealType())) {
                recipeOutput.append("\\rusection{" + r.getMealType() + "}" + System.lineSeparator());
                lastSection = r.getMealType();
            }
            recipeOutput.append(r.toTex(ingredientMap));
        }

        // System.out.print(output.toString());
        // Write this output to the recipe Output file.
        FileUtils.writeStringToFile(new File(recipeOutputFileName), recipeOutput.toString(), Charset.forName("UTF-8"), false);
        System.out.println( "Recipe output written to " + recipeOutputFileName );

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        // Write some data to the data output file.
        StringBuilder dataOutput = new StringBuilder();

        // Write the number of persons to a variable
        dataOutput.append("\\def \\ruNumPersons {").append(numPersons).append("}").append(System.lineSeparator());

        // Write the list of ingredients with coo
        List<Ingredient> listIngredients = ingredients.stream().filter(i -> i.isList()).sorted(Comparator.comparing(Ingredient::getId)).collect(Collectors.toList());
        dataOutput.append("\\newcommand{\\rucooingredients}{").append(System.lineSeparator());
        for(Ingredient i: listIngredients) {
            if (i.getUnit().isEmpty()) {
                i.setUnit("stk");
            }

            BigDecimal bd = new BigDecimal(i.getCo2());
            bd = bd.round(new MathContext(3));
            String co2String = new DecimalFormat("###.##").format(bd.doubleValue());

            dataOutput.append("  \\rucooingredient{")
                    .append(i.getId()).append("}{")
                    .append(i.getUnit()).append("}{")
                    .append(co2String).append("}").append(System.lineSeparator());
        }
        dataOutput.append("}").append(System.lineSeparator());

        // Write this output to the data Output file.
        FileUtils.writeStringToFile(new File(dataOutputFileName), dataOutput.toString(), Charset.forName("UTF-8"), false);


        // For temporary purposes, dump list of all recipes, sorted on CO2, then name
        List<Recipe> co2Recipes = recipes.stream().sorted(Comparator.comparing(Recipe::getCo2).thenComparing(Recipe::getName)).collect(Collectors.toList());
        System.out.println("Dumping list of " + co2Recipes.size() + " recipes, sorted on CO2 pr. person");
        for (Recipe r: co2Recipes) {
            System.out.println(String.format("%.3f: %s : %s", r.getCo2()/r.getPersons(), r.getCo2Rating(), r.getName()));
        }
    }
}
