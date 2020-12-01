package actions.queries;

import common.Constants;
import common.EntityWithSortingCriteria;
import entertainment.Movie;
import entertainment.Season;
import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UsersQuery {
    /**
     * Process users query
     *
     * @param database    database
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    public static JSONObject execute(final Database database,
                               final ActionInputData actionInput,
                               final Writer writer) {
        return switch (actionInput.getCriteria()) {
            case Constants.NUM_RATINGS -> executeNumRatings(database,
                    actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    private static JSONObject executeNumRatings(final Database database,
                                                final ActionInputData actionInput,
                                                final Writer writer) {
        HashMap<String, Integer> usersWithTotalRatings = new HashMap<>();
        List<EntityWithSortingCriteria> usersResult = new ArrayList<>();

        for (Movie movie : database.getMovies().values()) {
            for (String user : movie.getRatingsForUsers().keySet()) {
                if (usersWithTotalRatings.containsKey(user)) {
                    usersWithTotalRatings.put(user, usersWithTotalRatings.get(user) + 1);
                } else {
                    usersWithTotalRatings.put(user, 1);
                }
            }
        }

        for (Serial serial : database.getSerials().values()) {
            for (Season season : serial.getSeasons()) {
                for (String user : season.getRatingsForUsers().keySet()) {
                    if (usersWithTotalRatings.containsKey(user)) {
                        usersWithTotalRatings.put(user, usersWithTotalRatings.get(user) + 1);
                    } else {
                        usersWithTotalRatings.put(user, 1);
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> pair : usersWithTotalRatings.entrySet()) {
            usersResult.add(new EntityWithSortingCriteria(pair.getKey(), (double) pair.getValue()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(usersResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(usersResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + usersResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
