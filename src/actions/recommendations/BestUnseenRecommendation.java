package actions.recommendations;

import common.EntityWithTwoSortingCriterias;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BestUnseenRecommendation {
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        List<EntityWithTwoSortingCriterias> results = new ArrayList<>();

        for (String name : database.getVideosOrder()) {
            if (database.getMovies().containsKey(name)
                    && !database.getMovies().get(name)
                    .hasBeenViewedByUser(actionInput.getUsername())) {
                results.add(new EntityWithTwoSortingCriterias(name,
                        database.getMovies().get(name).getRating(),
                        database.getVideosOrder().size() - (double) results.size()));
            }

            if (database.getSerials().containsKey(name)
                    && !database.getSerials().get(name)
                    .hasBeenViewedByUser(actionInput.getUsername())) {
                results.add(new EntityWithTwoSortingCriterias(name,
                        database.getSerials().get(name).getRating(),
                        database.getVideosOrder().size() - (double) results.size()));
            }
        }

        Collections.sort(results, Collections.reverseOrder());

        if (results.size() <= 0) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "BestRatedUnseenRecommendation");
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "BestRatedUnseenRecommendation result: " + results.get(0));
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
