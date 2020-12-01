package actions.recommendations;

import common.EntityWithTwoSortingCriterias;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;
import user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class FavoriteRecommendation {
    private FavoriteRecommendation() { }

    /**
     * Executes a favorite recommendation
     *
     * @param database    database
     * @param actionInput action input data
     * @param writer      output writer
     * @return recommendation result as JSONObject
     */
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "FavoriteRecommendation");
        }

        HashMap<String, Integer> favoriteVideosOccurrences = new HashMap<>();
        List<EntityWithTwoSortingCriterias> resultList = new ArrayList<>();

        String user = actionInput.getUsername();

        for (User username : database.getUsers().values()) {
            for (String title : username.getFavoriteVideos()) {
                if (favoriteVideosOccurrences.containsKey(title)) {
                    favoriteVideosOccurrences.put(title, favoriteVideosOccurrences.get(title) + 1);
                } else {
                    favoriteVideosOccurrences.put(title, 1);
                }
            }
        }

        for (String title : database.getVideosOrder()) {
            if (!favoriteVideosOccurrences.containsKey(title)) {
                continue;
            }

            if (database.getMovies().containsKey(title)) {
                if (!database.getMovies().get(title).hasBeenViewedByUser(user)) {
                    resultList.add(
                            new EntityWithTwoSortingCriterias(
                                    title,
                                    (double) favoriteVideosOccurrences.get(title),
                                    database.getVideosOrder().size() - (double) resultList.size()
                            )
                    );
                }
            } else if (database.getSerials().containsKey(title)) {
                if (!database.getSerials().get(title).hasBeenViewedByUser(user)) {
                    resultList.add(
                            new EntityWithTwoSortingCriterias(
                                    title,
                                    (double) favoriteVideosOccurrences.get(title),
                                    database.getVideosOrder().size() - (double) resultList.size()
                            )
                    );
                }
            }
        }

        Collections.sort(resultList, Collections.reverseOrder());

        if (resultList.size() <= 0) {
            return RecommendationUtils.recommendationFailure(actionInput,
                    writer,
                    "FavoriteRecommendation");
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "FavoriteRecommendation result: " + resultList.get(0));
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
