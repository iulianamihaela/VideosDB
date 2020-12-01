package actions.queries;

import common.Constants;
import common.EntityWithSortingCriteria;
import entertainment.Genre;
import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;
import user.User;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ShowsQuery {
    private ShowsQuery() { }

    /**
     * Process shows query
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
            case Constants.RATINGS_CRITERIA -> executeRating(database,
                    actionInput,
                    writer);
            case Constants.FAVORITE -> executeFavorite(database,
                    actionInput,
                    writer);
            case Constants.LONGEST -> executeLongest(database,
                    actionInput,
                    writer);
            case Constants.MOST_VIEWED -> executeMostViewed(database,
                    actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    private static JSONObject executeRating(final Database database,
                                            final ActionInputData actionInput,
                                            final Writer writer) {
        List<EntityWithSortingCriteria> showsResult = new ArrayList<>();

        boolean hasYearFilter = true;
        boolean hasGenreFilter = true;

        int releaseYear = 0;
        Genre genre = null;

        try {
            releaseYear = Integer.parseInt(
                    actionInput
                            .getFilters()
                            .get(Constants.YEAR_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasYearFilter = false;
        }

        try {
            genre = Utils.stringToGenre(
                    actionInput
                            .getFilters()
                            .get(Constants.GENRE_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasGenreFilter = false;
        }

        for (Serial serial : database.getSerials().values()) {
            if (hasYearFilter && serial.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !serial.getGenres().contains(genre)) {
                continue;
            }
            if (serial.getRating() <= 0) {
                continue;
            }

            showsResult.add(new EntityWithSortingCriteria(serial.getTitle(), serial.getRating()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(showsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(showsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + showsResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }

    private static JSONObject executeFavorite(final Database database,
                                              final ActionInputData actionInput,
                                              final Writer writer) {
        List<EntityWithSortingCriteria> showsResult = new ArrayList<>();

        boolean hasYearFilter = true;
        boolean hasGenreFilter = true;

        int releaseYear = 0;
        Genre genre = null;

        try {
            releaseYear = Integer.parseInt(
                    actionInput
                            .getFilters()
                            .get(Constants.YEAR_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasYearFilter = false;
        }

        try {
            genre = Utils.stringToGenre(
                    actionInput
                            .getFilters()
                            .get(Constants.GENRE_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasGenreFilter = false;
        }

        for (Serial serial : database.getSerials().values()) {
            if (hasYearFilter && serial.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !serial.getGenres().contains(genre)) {
                continue;
            }

            int occurences = 0;

            for (User user : database.getUsers().values()) {
                if (user.hasFavoriteMovie(serial.getTitle())) {
                    occurences++;
                }
            }

            if (occurences > 0) {
                showsResult.add(new EntityWithSortingCriteria(serial.getTitle(),
                        (double) occurences));
            }
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(showsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(showsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + showsResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }

    private static JSONObject executeLongest(final Database database,
                                             final ActionInputData actionInput,
                                             final Writer writer) {
        List<EntityWithSortingCriteria> showsResult = new ArrayList<>();

        boolean hasYearFilter = true;
        boolean hasGenreFilter = true;

        int releaseYear = 0;
        Genre genre = null;

        try {
            releaseYear = Integer.parseInt(
                    actionInput
                            .getFilters()
                            .get(Constants.YEAR_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasYearFilter = false;
        }

        try {
            genre = Utils.stringToGenre(
                    actionInput
                            .getFilters()
                            .get(Constants.GENRE_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasGenreFilter = false;
        }

        for (Serial serial : database.getSerials().values()) {
            if (hasYearFilter && serial.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !serial.getGenres().contains(genre)) {
                continue;
            }

            showsResult.add(new EntityWithSortingCriteria(serial.getTitle(),
                    (double) serial.getDuration()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(showsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(showsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + showsResult
                            .stream()
                            .limit(actionInput.getNumber())
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }

    private static JSONObject executeMostViewed(final Database database,
                                                final ActionInputData actionInput,
                                                final Writer writer) {
        List<EntityWithSortingCriteria> showsResult = new ArrayList<>();

        boolean hasYearFilter = true;
        boolean hasGenreFilter = true;

        int releaseYear = 0;
        Genre genre = null;

        try {
            releaseYear = Integer.parseInt(
                    actionInput
                            .getFilters()
                            .get(Constants.YEAR_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasYearFilter = false;
        }

        try {
            genre = Utils.stringToGenre(
                    actionInput
                            .getFilters()
                            .get(Constants.GENRE_FILTER_POSITION)
                            .get(0)
            );
        } catch (Exception e) {
            hasGenreFilter = false;
        }

        for (Serial serial : database.getSerials().values()) {
            if (hasYearFilter && serial.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !serial.getGenres().contains(genre)) {
                continue;
            }
            if (serial.getViewsCount() <= 0) {
                continue;
            }

            showsResult.add(new EntityWithSortingCriteria(serial.getTitle(),
                    (double) serial.getViewsCount()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(showsResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(showsResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + showsResult
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
