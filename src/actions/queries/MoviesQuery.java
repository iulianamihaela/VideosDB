package actions.queries;

import common.Constants;
import common.EntityWithSortingCriteria;
import entertainment.Genre;
import entertainment.Movie;
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

public final class MoviesQuery {
    private MoviesQuery() { }

    /**
     * Process movies query
     *
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
        List<EntityWithSortingCriteria> moviesResult = new ArrayList<>();

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

        for (Movie movie : database.getMovies().values()) {
            if (hasYearFilter && movie.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !movie.getGenres().contains(genre)) {
                continue;
            }
            if (movie.getRating() <= 0) {
                continue;
            }

            moviesResult.add(new EntityWithSortingCriteria(movie.getTitle(), movie.getRating()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(moviesResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(moviesResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + moviesResult
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
        List<EntityWithSortingCriteria> moviesResult = new ArrayList<>();

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

        for (Movie movie : database.getMovies().values()) {
            if (hasYearFilter && movie.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !movie.getGenres().contains(genre)) {
                continue;
            }

            int occurences = 0;

            for (User user : database.getUsers().values()) {
                if (user.hasFavoriteMovie(movie.getTitle())) {
                    occurences++;
                }
            }

            if (occurences > 0) {
                moviesResult.add(new EntityWithSortingCriteria(movie.getTitle(),
                        (double) occurences));
            }
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(moviesResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(moviesResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + moviesResult
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
        List<EntityWithSortingCriteria> moviesResult = new ArrayList<>();

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

        for (Movie movie : database.getMovies().values()) {
            if (hasYearFilter && movie.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !movie.getGenres().contains(genre)) {
                continue;
            }

            moviesResult.add(new EntityWithSortingCriteria(movie.getTitle(),
                    (double) movie.getDuration()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(moviesResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(moviesResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + moviesResult
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
        List<EntityWithSortingCriteria> moviesResult = new ArrayList<>();

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

        for (Movie movie : database.getMovies().values()) {
            if (hasYearFilter && movie.getReleaseYear() != releaseYear) {
                continue;
            }
            if (hasGenreFilter && !movie.getGenres().contains(genre)) {
                continue;
            }
            if (movie.getViewsCount() <= 0) {
                continue;
            }

            moviesResult.add(new EntityWithSortingCriteria(movie.getTitle(),
                    (double) movie.getViewsCount()));
        }

        if (actionInput.getSortType().equals(Constants.ASC_SORTING)) {
            Collections.sort(moviesResult);
        }
        if (actionInput.getSortType().equals(Constants.DESC_SORTING)) {
            Collections.sort(moviesResult, Collections.reverseOrder());
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "Query result: "
                            + moviesResult
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
