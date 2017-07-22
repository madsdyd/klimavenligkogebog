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
import java.util.List;

/**
 * Created by madsdyd on 22-07-17.
 */
public class Config {

    public static class Rating {
        double limit;
        String rating;

        public double getLimit() {
            return limit;
        }

        public String getRating() {
            return rating;
        }

        public static Rating fromJsonObject(JsonObject jsonObject) {
            Rating r = new Rating();
            if (!jsonObject.containsKey("limit")) {
                System.err.println("Missing key limit from rating in config.");
            }
            r.limit = jsonObject.getJsonNumber("limit").doubleValue();
            if (!jsonObject.containsKey("rating")) {
                System.err.println("Missing key rating from rating in config.");
            }
            r.rating = jsonObject.getString("rating");
            System.err.println("Read: " + r.toString());
            return r;
        }

        @Override
        public String toString() {
            return "Rating{" +
                    "limit=" + limit +
                    ", rating='" + rating + '\'' +
                    '}';
        }
    }


    List<Rating> ratings = new ArrayList<>();

    public static Config fromJsonObject(JsonObject jsonObject) {
        Config c = new Config();

        if (!jsonObject.containsKey("ratings")) {
            throw new RuntimeException("While parsing config, missing section 'ratings'");
        }
        JsonArray jratings = jsonObject.getJsonArray("ratings");

        for (JsonObject rating : jratings.getValuesAs(JsonObject.class)) {
            c.ratings.add(Rating.fromJsonObject(rating));
        }

        return c;
    }


    public static Config fromJsonFile(Path path) throws IOException {
        Config res = null;
        try (InputStream is = Files.newInputStream(path);
             JsonReader rdr = Json.createReader(is)) {
            JsonObject jconfig = rdr.readObject();
            res = fromJsonObject(jconfig);
        }
        return res;
    }

    @Override
    public String toString() {
        return "Config{" +
                "ratings=" + ratings +
                '}';
    }
}
