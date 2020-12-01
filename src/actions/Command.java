package actions;

import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import main.Database;
import org.json.simple.JSONObject;

import java.io.IOException;

public class Command {
    /**
     * Process a simple command
     * @param database    database
     * @param actionInput action input
     * @param writer      output writer
     * @return result
     */
    public static JSONObject execute(final Database database,
                                     final ActionInputData actionInput,
                                     final Writer writer) {
        return switch (actionInput.getType()) {
            case Constants.VIEW_COMMAND -> executeViewCommand(database, actionInput, writer);
            case Constants.FAVORITE -> executeFavoriteCommand(database, actionInput, writer);
            case Constants.RATING_COMMAND -> executeRatingCommand(database, actionInput, writer);
            default -> new JSONObject();
        };
    }

    /**
     * Executes a view command
     * @param database    database
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private static JSONObject executeViewCommand(final Database database,
                                                 final ActionInputData actionInput,
                                                 final Writer output) {
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
     * @param database    database
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private static JSONObject executeFavoriteCommand(final Database database,
                                              final ActionInputData actionInput,
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
     * @param database    database
     * @param actionInput action input data
     * @param output      output writer
     * @return JsonObject
     */
    private static JSONObject executeRatingCommand(final Database database,
                                            final ActionInputData actionInput,
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
}
