package actions.queries;

import actor.Actor;
import common.Constants;
import common.EntityWithSortingCriteria;
import entertainment.Movie;
import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;
import utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public final class ActorsQuery {
    private ActorsQuery() { }

    /**
     * Process an actors query
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
            case Constants.AVERAGE -> executeAverage(database, actionInput, writer);
            case Constants.AWARDS -> executeAwards(database, actionInput, writer);
            case Constants.FILTER_DESCRIPTIONS -> executeFilterDescription(database, actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    /**
     * Executes an Actors Average query
     *
     * @param actionInput action input data
     * @param writer      output writer
     * @return result
     */
    private static JSONObject executeAverage(final Database database,
                                             final ActionInputData actionInput,
                                             final Writer writer) {
        HashMap<String, Double> ratingForUser = new HashMap<>();
        HashMap<String, Integer> noRatingsForUser = new HashMap<>();

        ArrayList<EntityWithSortingCriteria> actorsWithRating = new ArrayList<>();

        for (Movie movie : database.getMovies().values()) {
            for (String actorName : movie.getCast()) {
                if (movie.getRating() != 0) {
                    if (ratingForUser.containsKey(actorName)) {
                        ratingForUser.put(actorName,
                                ratingForUser.get(actorName) + movie.getRating());
                        noRatingsForUser.put(actorName, noRatingsForUser.get(actorName) + 1);
                    } else {
                        ratingForUser.put(actorName, movie.getRating());
                        noRatingsForUser.put(actorName, 1);
                    }
                }
            }
        }

        for (Serial serial : database.getSerials().values()) {
            for (String actorName : serial.getCast()) {
                if (serial.getRating() != 0) {
                    if (ratingForUser.containsKey(actorName)) {
                        ratingForUser.put(actorName,
                                ratingForUser.get(actorName) + serial.getRating());
                        noRatingsForUser.put(actorName, noRatingsForUser.get(actorName) + 1);
                    } else {
                        ratingForUser.put(actorName, serial.getRating());
                        noRatingsForUser.put(actorName, 1);
                    }
                }
            }
        }

        for (String name : ratingForUser.keySet()) {
            actorsWithRating.add(new EntityWithSortingCriteria(name,
                    ratingForUser.get(name) / (double) noRatingsForUser.get(name)));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(actorsWithRating);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(actorsWithRating, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + actorsWithRating
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * Executes an Actors Awards query
     *
     * @param database    database
     * @param actionInput action input data
     * @param writer      output writer
     * @return result
     */
    private static JSONObject executeAwards(final Database database,
                                            final ActionInputData actionInput,
                                            final Writer writer) {
        List<String> awardsList = actionInput.getFilters().get(Constants.AWARDS_FILTER_POSITION);

        List<EntityWithSortingCriteria> actorsResult = new ArrayList<>();

        for (Actor actor : database.getActors().values()) {
            boolean hasAwards = true;
            for (String award : awardsList) {
                if (!actor.getAwards().containsKey(Utils.stringToAwards(award))) {
                    hasAwards = false;
                    break;
                }
            }

            if (hasAwards) {
                actorsResult.add(new EntityWithSortingCriteria(actor.getName(),
                        (double) (actor.getAwardsCount())));
            }
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(actorsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(actorsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + actorsResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * Executes an Actors Filter Description Query
     *
     * @param database    database
     * @param actionInput action input data
     * @param writer      output writer
     * @return result
     */
    private static JSONObject executeFilterDescription(final Database database,
                                                       final ActionInputData actionInput,
                                                       final Writer writer) {
        List<String> keywords = actionInput.getFilters().get(Constants.WORDS_FILTER_POSITION);

        List<EntityWithSortingCriteria> actorsResult = new ArrayList<>();

        for (Actor actor : database.getActors().values()) {
            boolean containsKeywords = true;

            for (String keyword : keywords) {
                if (!Arrays.asList(actor
                        .getCareerDescription()
                        .toLowerCase()
                        .split("\\W+"))
                        .contains(keyword)) {
                    containsKeywords = false;
                    break;
                }
            }

            if (containsKeywords) {
                actorsResult.add(new EntityWithSortingCriteria(actor.getName(), (double) 0));
            }
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(actorsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(actorsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + actorsResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }
}
