package actions.recommendations;

import common.EntityWithSortingCriteria;
import entertainment.Genre;
import entertainment.Movie;
import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchRecommendation {
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "SearchRecommendation");
        }

        List<EntityWithSortingCriteria> resultList = new ArrayList<>();

        Genre genre = Utils.stringToGenre(actionInput.getGenre());
        String user = actionInput.getUsername();

        for (Movie movie : database.getMovies().values()) {
            if (!movie.hasBeenViewedByUser(user) && movie.getGenres().contains(genre)) {
                resultList.add(new EntityWithSortingCriteria(
                        movie.getTitle(),
                        movie.getRating()
                ));
            }
        }

        for (Serial serial : database.getSerials().values()) {
            if (!serial.hasBeenViewedByUser(user) && serial.getGenres().contains(genre)) {
                resultList.add(new EntityWithSortingCriteria(
                        serial.getTitle(),
                        serial.getRating()
                ));
            }
        }

        Collections.sort(resultList);

        if (resultList.size() <= 0) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "SearchRecommendation");
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "SearchRecommendation result: " + resultList);
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
