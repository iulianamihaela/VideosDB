package main;

import actor.Actor;
import common.Constants;
import common.EntityWithSortingCriteria;
import common.EntityWithTwoSortingCriterias;
import entertainment.Genre;
import entertainment.Movie;
import entertainment.Season;
import entertainment.Serial;
import fileio.Input;
import fileio.ActionInputData;
import fileio.UserInputData;
import fileio.SerialInputData;
import fileio.MovieInputData;
import fileio.ActorInputData;
import fileio.Writer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import user.User;
import utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class VideosDB {
    private final Database database;

    public VideosDB() {
         database = new Database();
    }

    /**
     * Solves the movies database
     *
     * @param input      input file
     * @param fileWriter output file
     */
    public JSONArray run(final Input input, final Writer fileWriter) {
        readDB(input);

        JSONArray result = new JSONArray();

        for (ActionInputData actionInput : input.getCommands()) {
            JSONObject commandResult = executeAction(actionInput, fileWriter);
            if (commandResult != null && !commandResult.isEmpty()) {
                result.add(commandResult);
            }
        }

        return result;
    }

    /**
     * Reads video database based on input
     *
     * @param input Data input from JSON
     */
    private void readDB(final Input input) {
        readMovies(input.getMovies());
        readSerials(input.getSerials());
        readUsers(input.getUsers());
        readActors(input.getActors());
    }

    /**
     * Reads movies from list
     *
     * @param movieInputDataList list of input data for movies
     */
    private void readMovies(final List<MovieInputData> movieInputDataList) {
        for (MovieInputData movieInput : movieInputDataList) {
            database.getVideosOrder().add(movieInput.getTitle());
            database.getMovies().put(movieInput.getTitle(), new Movie(movieInput));
        }
    }

    /**
     * Reads getSerials() from list
     *
     * @param serialInputDataList list of input data for getSerials()
     */
    private void readSerials(final List<SerialInputData> serialInputDataList) {
        for (SerialInputData serialInput : serialInputDataList) {
            database.getVideosOrder().add(serialInput.getTitle());
            database.getSerials().put(serialInput.getTitle(), new Serial(serialInput));
        }
    }

    /**
     * Reads actors from list
     *
     * @param actorInputDataList list of input data for actors
     */
    private void readActors(final List<ActorInputData> actorInputDataList) {
        for (ActorInputData actorInput : actorInputDataList) {
            database.getActors().put(actorInput.getName(), new Actor(actorInput));
        }
    }

    /**
     * Reads users from list
     *
     * @param userInputDataList list of input data for users
     */
    private void readUsers(final List<UserInputData> userInputDataList) {
        for (UserInputData userInput : userInputDataList) {
            database.getUsers().put(userInput.getUsername(), new User(userInput));

            HashMap<String, Integer> history = new HashMap<>(userInput.getHistory());

            for (Map.Entry<String, Integer> pair : history.entrySet()) {
                if (database.getMovies().containsKey(pair.getKey())) {
                    database.getMovies()
                            .get(pair.getKey())
                            .addViewsForUser(userInput.getUsername(), pair.getValue());
                }
                if (database.getSerials().containsKey(pair.getKey())) {
                    database.getSerials()
                            .get(pair.getKey())
                            .addViewsForUser(userInput.getUsername(), pair.getValue());
                }
            }
        }
    }

    private JSONObject executeAction(final ActionInputData actionInput, final Writer writer) {
        return switch (actionInput.getActionType()) {
            case Constants.COMMAND -> executeCommand(actionInput, writer);
            case Constants.QUERY -> executeQuery(actionInput, writer);
            case Constants.RECOMMENDATION -> executeRecommendation(actionInput, writer);
            default -> new JSONObject();
        };
    }

    private JSONObject executeCommand(final ActionInputData actionInput, final Writer writer) {
        final String type = actionInput.getType();

        if (type == null) {
            return new JSONObject();
        }

        return switch (type) {
            case Constants.VIEW_COMMAND -> executeViewCommand(actionInput, writer);
            case Constants.FAVORITE -> executeFavoriteCommand(actionInput, writer);
            case Constants.RATING_COMMAND -> executeRatingCommand(actionInput, writer);
            default -> new JSONObject();
        };
    }


    /**
     * Executes a view command
     *
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private JSONObject executeViewCommand(final ActionInputData actionInput, final Writer output) {
        int actionId = actionInput.getActionId();
        String title = actionInput.getTitle();
        String user = actionInput.getUsername();

        boolean titleExists = database.getMovies().containsKey(title)
                || database.getSerials().containsKey(title);
        int views = 0;

        if (titleExists) {
            if (database.getMovies().containsKey(title)) {
                database.getMovies().get(title).addViewer(user);
                views = database.getMovies().get(title).getUsersViews(user);
            } else if (database.getSerials().containsKey(title)) {
                database.getSerials().get(title).addViewer(user);
                views = database.getSerials().get(title).getUsersViews(user);
            }
        }

        try {
            if (titleExists) {
                return output.writeFile(
                        actionId,
                        "message",
                        "success -> "
                                + title
                                + " was viewed with total views of "
                                + views);
            } else {
                return output.writeFile(actionId, "message", "error -> " + title
                        + " is not seen");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * Executes a favorite command
     *
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private JSONObject executeFavoriteCommand(final ActionInputData actionInput,
                                              final Writer output) {
        int actionId = actionInput.getActionId();
        String title = actionInput.getTitle();
        String username = actionInput.getUsername();

        boolean titleExists = database.getMovies().containsKey(title)
                || database.getSerials().containsKey(title);

        if (!titleExists) {
            return new JSONObject();
        }

        boolean hasBeenFavorited = false;
        boolean wasAlreadyFavorited = database.getUsers().get(username).hasFavoriteMovie(title);
        boolean hasBeenViewedByUser =
            (database.getMovies().containsKey(title)
                    && database.getMovies().get(title).hasBeenViewedByUser(username))
            || (database.getSerials().containsKey(title)
                    && database.getSerials().get(title).hasBeenViewedByUser(username));

        if (titleExists && (hasBeenViewedByUser || wasAlreadyFavorited)) {
            if (!wasAlreadyFavorited) {
                database.getUsers().get(username).addFavorite(title);
            }
            hasBeenFavorited = true;
        }

        try {
            if (hasBeenFavorited) {
                if (wasAlreadyFavorited) {
                    return output.writeFile(
                            actionId,
                            "message",
                            "error -> "
                                    + title
                                    + " is already in favourite list");
                } else {
                    return output.writeFile(
                            actionId,
                            "message",
                            "success -> "
                                    + title
                                    + " was added as favourite");
                }
            } else {
                return output.writeFile(actionId, "message", "error -> " + title
                        + " is not seen");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * Executes a rating command
     *
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private JSONObject executeRatingCommand(final ActionInputData actionInput,
                                            final Writer output) {
        int actionId = actionInput.getActionId();
        String user = actionInput.getUsername();
        String title = actionInput.getTitle();

        if (database.getMovies().containsKey(title)) {
            if (!database.getMovies().get(title).existsRatingFromUser(user)) {
                if (!database.getMovies().get(title).hasBeenViewedByUser(user)) {
                    try {
                        return output.writeFile(actionId,
                                "message",
                                "error -> "
                                        + title
                                        + " is not seen");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                database.getMovies().get(title).addRatingForUser(user, actionInput.getGrade());
                try {
                    return output.writeFile(actionId,
                            "message",
                            "success -> "
                                    + title
                                    + " was rated with "
                                    + String.format("%.1f", actionInput.getGrade())
                                    + " by "
                                    + user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    return output.writeFile(actionId,
                            "message",
                            "error -> "
                                    + title
                                    + " has been already rated");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int seasonNumber = actionInput.getSeasonNumber() - 1;
        if (database.getSerials().containsKey(title)) {
            if (!database.getSerials().get(title).hasBeenViewedByUser(user)) {
                try {
                    return output.writeFile(actionId,
                            "message",
                            "error -> "
                                    + title
                                    + " is not seen");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!database.getSerials().get(title).getSeason(seasonNumber).isRatedByUser(user)) {
                database.getSerials().get(title).getSeason(seasonNumber).addRatingByUser(user,
                        actionInput.getGrade());
                try {
                    return output.writeFile(actionId,
                            "message",
                            "success -> "
                                    + title
                                    + " was rated with "
                                    + String.format("%.1f", actionInput.getGrade())
                                    + " by "
                                    + user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    return output.writeFile(actionId,
                            "message",
                            "error -> "
                                    + title
                                    + " has been already rated");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new JSONObject();
    }

    /**
     * Process a query command
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    private JSONObject executeQuery(final ActionInputData actionInput,
                                    final Writer writer) {
        return switch (actionInput.getObjectType()) {
            case Constants.ACTORS -> executeActorsCriteria(actionInput, writer);
            case Constants.MOVIES -> executeMoviesCriteria(actionInput, writer);
            case Constants.SHOWS -> executeShowsCriteria(actionInput, writer);
            case Constants.USERS -> executeUsersCriteria(actionInput, writer);
            default -> new JSONObject();
        };
    }

    /**
     * Process a criteria actor execution
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    private JSONObject executeActorsCriteria(final ActionInputData actionInput,
                                             final Writer writer) {
        return switch (actionInput.getCriteria()) {
            case Constants.AVERAGE -> executeActorsAverageQuery(actionInput, writer);
            case Constants.AWARDS -> executeActorsAwardsCriteria(actionInput, writer);
            case Constants.FILTER_DESCRIPTIONS -> executeActorsFilterDescription(actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    /**
     * Process a criteria movies execution
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    private JSONObject executeMoviesCriteria(final ActionInputData actionInput,
                                             final Writer writer) {
        return switch (actionInput.getCriteria()) {
            case Constants.RATINGS_CRITERIA -> executeMoviesRatingCriteria(actionInput,
                    writer);
            case Constants.FAVORITE -> executeMoviesFavoriteCriteria(actionInput,
                    writer);
            case Constants.LONGEST -> executeMoviesLongestCriteria(actionInput,
                    writer);
            case Constants.MOST_VIEWED -> executeMoviesMostViewedCriteria(actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    /**
     * Process a criteria shows execution
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    private JSONObject executeShowsCriteria(final ActionInputData actionInput,
                                            final Writer writer) {
        return switch (actionInput.getCriteria()) {
            case Constants.RATINGS_CRITERIA -> executeShowsRatingCriteria(actionInput,
                    writer);
            case Constants.FAVORITE -> executeShowsFavoriteCriteria(actionInput,
                    writer);
            case Constants.LONGEST -> executeShowsLongestCriteria(actionInput,
                    writer);
            case Constants.MOST_VIEWED -> executeShowsMostViewedCriteria(actionInput,
                    writer);
            default -> new JSONObject();
        };
    }

    /**
     * Process a criteria users execution
     *
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    private JSONObject executeUsersCriteria(final ActionInputData actionInput,
                                            final Writer writer) {
        return switch (actionInput.getCriteria()) {
            case Constants.NUM_RATINGS -> executeNumRatingsUsersQuery(actionInput, writer);
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
    public JSONObject executeActorsAverageQuery(final ActionInputData actionInput,
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
     * @param actionInput action input data
     * @param writer      output writer
     * @return result
     */
    public JSONObject executeActorsAwardsCriteria(final ActionInputData actionInput,
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
     * @param actionInput action input data
     * @param writer      output writer
     * @return result
     */
    public JSONObject executeActorsFilterDescription(final ActionInputData actionInput,
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

    private JSONObject executeMoviesRatingCriteria(final ActionInputData actionInput,
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

    private JSONObject executeShowsRatingCriteria(final ActionInputData actionInput,
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

    private JSONObject executeMoviesFavoriteCriteria(final ActionInputData actionInput,
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

    private JSONObject executeShowsFavoriteCriteria(final ActionInputData actionInput,
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

    private JSONObject executeMoviesLongestCriteria(final ActionInputData actionInput,
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

    private JSONObject executeShowsLongestCriteria(final ActionInputData actionInput,
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

    private JSONObject executeMoviesMostViewedCriteria(final ActionInputData actionInput,
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

    private JSONObject executeShowsMostViewedCriteria(final ActionInputData actionInput,
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

    private JSONObject executeNumRatingsUsersQuery(final ActionInputData actionInput,
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

    private JSONObject executeRecommendation(final ActionInputData actionInput,
                                             final Writer writer) {
        final String type = actionInput.getType();

        if (type == null) {
            return new JSONObject();
        }

        return switch (type) {
            case Constants.STANDARD -> executeStandardRecommendation(actionInput, writer);
            case Constants.BEST_UNSEEN -> executeBestUnseenRecommendation(actionInput, writer);
            case Constants.POPULAR -> executePopularRecommendation(actionInput, writer);
            case Constants.FAVORITE -> executeFavoriteRecommendation(actionInput, writer);
            case Constants.SEARCH -> executeSearchRecommendation(actionInput, writer);
            default -> new JSONObject();
        };
    }

    private JSONObject executeStandardRecommendation(final ActionInputData actionInput,
                                                     final Writer writer) {
        String result = "";

        for (String title : database.getVideosOrder()) {
            if (database.getMovies().containsKey(title)
                    && !database.getMovies().get(title)
                            .hasBeenViewedByUser(actionInput.getUsername())) {
                result = title;
                break;
            } else if (database.getSerials().containsKey(title)
                    && !database.getSerials().get(title)
                            .hasBeenViewedByUser(actionInput.getUsername())) {
                result = title;
                break;
            }
        }

        if (result.isEmpty()) {
            for (Serial serial : database.getSerials().values()) {
                if (!serial.hasBeenViewedByUser(actionInput.getUsername())) {
                    result = serial.getTitle();
                }
            }
        }

        if (!result.isEmpty()) {
            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "StandardRecommendation result: "
                                + result
                );
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
        }

        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    "StandardRecommendation cannot be applied!");
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }

    private JSONObject executeBestUnseenRecommendation(final ActionInputData actionInput,
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
            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "BestRatedUnseenRecommendation cannot be applied!");
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
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

    private JSONObject executePopularRecommendation(final ActionInputData actionInput,
                                                    final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return recommendationFailure(actionInput, writer, "PopularRecommendation");
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

    private JSONObject executeFavoriteRecommendation(final ActionInputData actionInput,
                                                     final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return recommendationFailure(actionInput, writer, "FavoriteRecommendation");
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
            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "FavoriteRecommendation cannot be applied!");
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
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

    private JSONObject executeSearchRecommendation(final ActionInputData actionInput,
                                                   final Writer writer) {
        if (!database.getUsers().get(actionInput.getUsername()).isPremium()) {
            return recommendationFailure(actionInput, writer, "SearchRecommendation");
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
            try {
                return writer.writeFile(actionInput.getActionId(),
                        "message",
                        "SearchRecommendation cannot be applied!");
            } catch (IOException e) {
                e.printStackTrace();

                return new JSONObject();
            }
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

    private JSONObject recommendationFailure(final ActionInputData actionInput,
                                             final Writer writer, final String recommendation) {
        try {
            return writer.writeFile(actionInput.getActionId(),
                    "message",
                    recommendation + " cannot be applied!");
        } catch (IOException e) {
            e.printStackTrace();

            return new JSONObject();
        }
    }
}
