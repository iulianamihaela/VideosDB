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
import java.util.*;

public class PopularRecommendation {
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "PopularRecommendation");
        }

        HashMap<Genre, Integer> genresOccurrences = new HashMap<>();
        List<EntityWithSortingCriteria> genresPopularity = new ArrayList<>();

        for (Movie movie : database.getMovies().values()) {
            for (Genre genre : movie.getGenres()) {
                if (genresOccurrences.containsKey(genre)) {
                    genresOccurrences.put(genre, genresOccurrences.get(genre) + 1);
                } else {
                    genresOccurrences.put(genre, 1);
                }
            }
        }

        for (Serial serial : database.getSerials().values()) {
            for (Genre genre : serial.getGenres()) {
                if (genresOccurrences.containsKey(genre)) {
                    genresOccurrences.put(genre, genresOccurrences.get(genre) + 1);
                } else {
                    genresOccurrences.put(genre, 1);
                }
            }
        }

        for (Map.Entry<Genre, Integer> pair : genresOccurrences.entrySet()) {
            genresPopularity.add(new EntityWithSortingCriteria(pair.getKey().toString(),
                    (double) pair.getValue()));
        }

        Collections.sort(genresPopularity, Collections.reverseOrder());

        for (EntityWithSortingCriteria sorter : genresPopularity) {
            Genre genre = Utils.stringToGenre(sorter.toString());

            for (String title : database.getVideosOrder()) {
                if (database.getMovies().containsKey(title)
                        && database.getMovies().get(title).getGenres().contains(genre)
                        && !database.getMovies().get(title)
                        .hasBeenViewedByUser(actionInput.getUsername())) {
                    try {
                        return writer.writeFile(actionInput.getActionId(),
                                "message",
                                "PopularRecommendation result: " + title);
                    } catch (IOException e) {
                        e.printStackTrace();

                        return new JSONObject();
                    }
                } else if (database.getSerials().containsKey(title)
                        && database.getSerials().get(title).getGenres().contains(genre)
                        && !database.getSerials().get(title)
                        .hasBeenViewedByUser(actionInput.getUsername())) {
                    try {
                        return writer.writeFile(actionInput.getActionId(),
                                "message",
                                "PopularRecommendation result: " + title);
                    } catch (IOException e) {
                        e.printStackTrace();

                        return new JSONObject();
                    }
                }
            }

            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "PopularRecommendation cannot be applied!");
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
        }

        return new JSONObject();
    }
}
