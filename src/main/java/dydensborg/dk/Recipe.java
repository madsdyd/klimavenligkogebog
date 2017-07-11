package dydensborg.dk;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }


    String name;
    String desc;
    String recipe;
    List<Content> contents = new ArrayList<>();
    double co2; // Calculated total.
    int persons;
    String time;



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

        r.recipe = jsonObject.getString("recipe");
        if ( r.recipe == null || r.recipe.equals("")) {
            System.err.println("While parsing recipe for " + r.name + ", missing recipe. Please check file for errors");
        }

        r.time = jsonObject.getString("time");
        if ( r.time == null || r.time.equals("")) {
            r.time = "Ukendt";
        }

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
}
