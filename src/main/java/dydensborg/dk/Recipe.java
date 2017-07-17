package dydensborg.dk;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by madsdyd on 11-07-17.
 */
public class Recipe {

    static class Content {
        String id;
        double amount;

        public static Content fromJsonObject(JsonObject jsonObject) {
            Content c = new Content();

            c.id = jsonObject.getString("id");
            if ( c.id == null || c.id.equals("")) {
                System.err.println("While parsing recipe ingredient, missing id. Please check file for errors");
            }

            if ( jsonObject.getJsonNumber("amount") == null) {
                System.err.println("While parsing recipe ingredient " + c.id + ", missing amount. Please check file for errors");
            }
            c.amount = jsonObject.getJsonNumber("amount").doubleValue();

            return c;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "id='" + id + '\'' +
                    ", amount=" + amount +
                    '}';
        }

        // Adjust to a single person, based on the number of persons the recipe is for.
        public void adustToPersons(double persons) {
            amount = amount / persons;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }


        public String toTex(Map<String,Ingredient> ingredientMap) {
            StringBuilder res = new StringBuilder();

            // We present the information differently than it is stored
            double presentationAmount = getAmount();
            String presentationUnit = ingredientMap.get(getId()).getUnit();

            // Convert some units to make stuff prettier

            // Weird units
            // Teaspoon to something larger
            if ( presentationUnit.equals("tsk") && presentationAmount > 8) {
                presentationAmount = presentationAmount / 3;
                presentationUnit = "spsk";
            }

            // Spoons to dl
            if ( presentationUnit.equals("spsk") && presentationAmount > 10) {
                // 1 spsk is 0.15 dl
                presentationAmount = presentationAmount *0.15;
                presentationUnit = "dl";
            }


            // SI: Basic rule, if larger > 1000, go up in unit, if lower than 1, go down
            // Special rule for dl => l unit.
            if (presentationAmount > 10) {
                // If we know the unit, go "up" in unit
                if (presentationUnit.equals("dl")) {
                    presentationAmount = presentationAmount / 10;
                    presentationUnit = "l";
                }
            }

            if (presentationAmount > 1000) {
                // If we know the unit, go "up" in unit
                if (presentationUnit.equals("g")) {
                    presentationAmount = presentationAmount / 1000;
                    presentationUnit = "kg";
                }
                if (presentationUnit.equals("ml")) {
                    presentationAmount = presentationAmount / 1000;
                    presentationUnit = "l";
                }
            }


            if (presentationAmount < 1) {
                // If we know the unit, go "down" in unit
                if (presentationUnit.equals("kg")) {
                    presentationAmount = presentationAmount * 1000;
                    presentationUnit = "g";
                    presentationAmount = Math.round(presentationAmount);
                }
                if (presentationUnit.equals("l")) {
                    presentationAmount = presentationAmount * 1000;
                    presentationUnit = "ml";
                    presentationAmount = Math.round(presentationAmount);
                }
            }



            // Do not use decimals for these types.
            if (presentationUnit.equals("example")) {
                presentationAmount = Math.round(presentationAmount);
            }




            // format without trailing zeros, max two decimals.
            // String amountString = new DecimalFormat("###.##").format(presentationAmount);

            String amountString = "";

            // For some units, round or use interval.
            if ((presentationUnit.equals("tsk") || presentationUnit.equals("spsk"))
                    && presentationAmount > 1 ) {
                if ((presentationAmount - Math.floor(presentationAmount)) < 0.3) {
                    amountString = new DecimalFormat("###.##").format(Math.floor(presentationAmount));
                } else {
                    if ((Math.ceil(presentationAmount) - presentationAmount) < 0.3) {
                        amountString = new DecimalFormat("###.##").format(Math.ceil(presentationAmount));
                    } else {
                        amountString = new DecimalFormat("###.##").format(Math.floor(presentationAmount)) + "-"
                                + new DecimalFormat("###.##").format(Math.ceil(presentationAmount));
                    }
                }
            } else {
                // Round to two significant digits
                BigDecimal bd = new BigDecimal(presentationAmount);
                bd = bd.round(new MathContext(2));
                presentationAmount = bd.doubleValue();
                 amountString = new DecimalFormat("###.##").format(presentationAmount);
            }

            // Ignore amounts that are zero
            if ( getAmount() < 0.000001 ) {
                amountString = "";
                presentationUnit = "";
            }

            res
                    .append("    \\ruingredient{")
                    .append(getId()).append("}{")
                    .append(amountString).append("}{")
                    .append(presentationUnit)
                    .append("}" + System.lineSeparator());
            return res.toString();
        }

    }

    static public class MealTypeOrdering {
        static public int sortOrder(String mealType)  {
            switch ( mealType) {
                case "Aftensmad" : return 30;
                case "Morgenmad" : return 10;
                case "Frokost" : return 20;
                case "Snack" : return 40;
                default : return 50;
            }
        }
    }

    //  Name of the recipe
    String name;
    // Short description / introduction
    String desc;
    // The steps in the recipe
    String recipe;
    // Content
    List<Content> contents = new ArrayList<>();
    // Calculated co2 for the total number of persons
    double co2;
    // Number of persons recipe match - read from file, then adjusted to setting
    int persons;
    // Time needed to prepare the dish.
    String time;
    // type of meal, "Morgenmad", "Frokost", "Aftensmad", etc.
    String mealType;
    int mealOrder; // Calculated based on mealType....


    public String getName() {
        return name;
    }
    public int getMealOrder() {
        return mealOrder;
    }

    public String getMealType() {
        return mealType;
    }

    /**
     * Create a single Posting object from a JsonObject.
     * <p>
     * I would assume you could do stuff like this with bindings, but I do not know how. And do not really care.
     *
     * @param jsonObject The object to parse from.
     * @return A new posting object, representing the stuff in the jsonObject
     */
    public static Recipe fromJsonObject(JsonObject jsonObject) {
        Recipe r = new Recipe();

        r.name = jsonObject.getString("name");
        if ( r.name == null || r.name.equals("")) {
            System.err.println("While parsing recipe, missing name. Please check file for errors");
        }

        r.desc = jsonObject.getString("desc");
        if ( r.desc == null || r.desc.equals("")) {
            System.err.println("While parsing recipe for " + r.name + ", missing desc. Please check file for errors");
        }

        r.recipe = jsonObject.getString("recipe");
        if ( r.recipe == null || r.recipe.equals("")) {
            System.err.println("While parsing recipe for " + r.name + ", missing recipe. Please check file for errors");
        }

        r.time = jsonObject.getString("time");
        if ( r.time == null || r.time.equals("")) {
            r.time = "Ukendt";
        }

        // If meal type is not set, use Aftensmad.
        try {
            r.mealType = jsonObject.getString("meal");
        } catch (NullPointerException e) {
            r.mealType = "Aftensmad";
        }
        r.mealOrder = MealTypeOrdering.sortOrder(r.mealType);

        // Parse the array.
        JsonArray content = jsonObject.getJsonArray("content");
        if ( content == null || content.size() == 0 ) {
            System.err.println("While parsing recipe for " + r.name + ", missing content. Please check file for errors");
        }

        for (JsonObject c : content.getValuesAs(JsonObject.class)) {
            r.contents.add(Content.fromJsonObject(c));
        }

        if ( jsonObject.getJsonNumber("persons") == null) {
            System.err.println("While parsing recipe ingredient " + r.name + ", missing persons. Please check file for errors");
        }
        r.persons = jsonObject.getJsonNumber("persons").intValue();

        System.out.println("Parsed recipe " + r.name);

        return r;
    }

    /**
     * Creates a list of Postings from a JSON file with multiple posting in.
     *
     * @param path The path to a JSON poting file from Spiir
     * @return A list of postings corresponding to the file.
     * @throws IOException If path can not be found
     */
    public static List<Recipe> fromJsonFile(Path path) throws IOException {
        List<Recipe> recipes = new ArrayList<>();

        try (InputStream is = Files.newInputStream(path);
             JsonReader rdr = Json.createReader(is)) {

            JsonArray jpostings = rdr.readArray();

            for (JsonObject result : jpostings.getValuesAs(JsonObject.class)) {
                recipes.add(fromJsonObject(result));
            }
        }

        return recipes;
    }


    @Override
    public String toString() {
        return "Recipe{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", recipe='" + recipe + '\'' +
                ", contents=" + contents +
                ", co2=" + co2 +
                ", persons=" + persons +
                ", time='" + time + '\'' +
                '}';
    }

    // Only call this once. It adjust the content to the number of persons.
    public void adjustToPersons(int numPersons) {
        if (persons != numPersons) {
            contents.forEach(c -> {
                c.adustToPersons( ((double) persons) / numPersons);
            });
            persons = numPersons;
        }
    }

    public double calculateCo2(Map<String, Ingredient> ingredientsMap) {
        double res = 0.0;
        for(Content c : contents) {
            if ( null == ingredientsMap.get(c.getId())) {
                System.err.println("While calculating CO2 for '" + name + "': Unable to locate ingredient '" + c.getId() + "'");
            }
            res += c.getAmount() * ingredientsMap.get(c.getId()).getCo2();
        };
        co2 = res;
        return res;
    }

    /**
     * Generate a tex string.
     *
     * Environment variables
     *
     * #1 : Navn
     * #2 : Introtekst / desc
     * #3 : How many persons
     * #4 : CO2
     * #5 : Tid
     * #6 : Ingredientslist
     * #7 : Recipe
     *
     * @return A tex String.
     */
    public String toTex(Map<String, Ingredient> ingredientMap) {
        StringBuilder sb = new StringBuilder();

        // Ingredient list
        StringBuilder il = new StringBuilder();
        //il.append("  \\begin{ruingredients}" + System.lineSeparator());
        for (Content c: contents) {
            il.append(c.toTex(ingredientMap));
        }
        //il.append("  \\end{ruingredients}" + System.lineSeparator());

        // Co2 is currently for the entire course, lets do per person too
        String co2string = String.format("%.2f/%.2f", co2/persons, co2);

        // The steps needs to be calculated from \\ in the code.
        List<String> stepsList = Arrays.asList(recipe.split("\\\\"));
        StringBuilder steps = new StringBuilder();
        for(String step : stepsList) {
            steps.append("\\rustep{").append(step).append("}").append(System.lineSeparator());
        }


        sb
                .append("\\begin{rurecipe}" + System.lineSeparator())
                .append("  {" + name + "}" + System.lineSeparator())
                .append("  {" + desc.replaceAll(("\\\\"), ("\\\\\\\\" + System.lineSeparator())) + "}" + System.lineSeparator())
                .append("  {" + persons + "}" + System.lineSeparator())
                // #4 : C2
                .append("  {" + co2string + "}" + System.lineSeparator())
                .append("  {" + time + "}" + System.lineSeparator())
                .append("  {" + il.toString() + "}" + System.lineSeparator())
                .append("  {" + steps.toString() + "}" + System.lineSeparator())
                .append("\\end{rurecipe}" + System.lineSeparator() + System.lineSeparator());

        return sb.toString();
    }

}
