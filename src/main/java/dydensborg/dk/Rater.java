package dydensborg.dk;

import java.util.List;

/**
 * Created by madsdyd on 22-07-17.
 */
public class Rater {
    List<Config.Rating> ratings;


    public Rater(List<Config.Rating> ratings) {
        this.ratings = ratings;
    }

    public String rate(double co2) {
        String res = ratings.get(0).getRating();
        for(Config.Rating rating : ratings) {
            if (co2 > rating.getLimit()) {
                res = rating.getRating();
            } else {
                return res;
            }
        }
        return res;
    }

}
