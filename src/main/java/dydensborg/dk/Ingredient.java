package dydensborg.dk;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by madsdyd on 11-07-17.
 */
public class Ingredient {

    String id;
    String unit;
    double co2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getCo2() {
        return co2;
    }

    public void setCo2(double co2) {
        this.co2 = co2;
    }

    /**
     * Create a single Posting object from a JsonObject.
     * <p>
     * I would assume you could do stuff like this with bindings, but I do not know how. And do not really care.
     *
     * @param jsonObject The object to parse from.
     * @return A new posting object, representing the stuff in the jsonObject
     */
    public static Ingredient fromJsonObject(JsonObject jsonObject) {
        Ingredient i = new Ingredient();

        i.id = jsonObject.getString("id");
        if ( i.id == null || i.id.equals("")) {
            System.err.println("While parsing ingredient, missing id. Please check file for errors");
        }

        i.unit = jsonObject.getString("unit");
        if ( i.unit == null || i.unit.equals("")) {
            System.err.println("While parsing ingredient " + i.id + ", missing unit. Please check file for errors");
        }

        if ( jsonObject.getJsonNumber("co2") == null) {
            System.err.println("While parsing ingredient " + i.id + ", missing co2. Please check file for errors");
        }
        i.co2 = jsonObject.getJsonNumber("co2").doubleValue();

        System.out.println("Parsed ingredient " + i.id);

        return i;
    }

    /**
     * Creates a list of Postings from a JSON file with multiple posting in.
     *
     * @param path The path to a JSON poting file from Spiir
     * @return A list of postings corresponding to the file.
     * @throws IOException If path can not be found
     */
    public static List<Ingredient> fromJsonFile(Path path) throws IOException {
        List<Ingredient> ingredients = new ArrayList<>();

        try (InputStream is = Files.newInputStream(path);
             JsonReader rdr = Json.createReader(is)) {

            JsonArray jpostings = rdr.readArray();

            for (JsonObject result : jpostings.getValuesAs(JsonObject.class)) {
                ingredients.add(fromJsonObject(result));
            }
        }

        return ingredients;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id='" + id + '\'' +
                ", unit='" + unit + '\'' +
                ", co2=" + co2 +
                '}';
    }
}
