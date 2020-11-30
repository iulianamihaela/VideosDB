package main;

import common.Constants;
import entertainment.Movie;
import entertainment.Serial;
import fileio.ActionInputData;
import fileio.Writer;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import fileio.Input;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import user.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideosDB {
  private final HashMap<String, Movie> movies;
  private final HashMap<String, Serial> serials;
  private final HashMap<String, User> users;

  public VideosDB() {
    movies = new HashMap<>();
    serials = new HashMap<>();
    users = new HashMap<>();
  }

  /**
   * Solves the movies database
   *
   * @param input input file
   * @param fileWriter output file
   */
  public JSONArray run(final Input input, final Writer fileWriter) {
    readDB(input);

    JSONArray result = new JSONArray();

    for (ActionInputData actionInput : input.getCommands()) {
      JSONObject commandResult = executeCommand(actionInput, fileWriter);
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
  }

  /**
   * Reads movies from list
   *
   * @param movieInputDataList list of input data for movies
   */
  private void readMovies(final List<MovieInputData> movieInputDataList) {
    for (MovieInputData movieInput : movieInputDataList) {
      movies.put(movieInput.getTitle(), new Movie(movieInput));
    }
  }

  /**
   * Reads serials from list
   *
   * @param serialInputDataList list of input data for serials
   */
  private void readSerials(final List<SerialInputData> serialInputDataList) {
    for (SerialInputData serialInput : serialInputDataList) {
      serials.put(serialInput.getTitle(), new Serial(serialInput));
    }
  }

  /**
   * Reads users from list
   *
   * @param userInputDataList list of input data for users
   */
  private void readUsers(final List<UserInputData> userInputDataList) {
    for (UserInputData userInput : userInputDataList) {
      users.put(userInput.getUsername(), new User(userInput));

      HashMap<String, Integer> history = new HashMap<>(userInput.getHistory());

      for (Map.Entry<String, Integer> pair : history.entrySet()) {
        if (movies.containsKey(pair.getKey())) {
          movies.get(pair.getKey()).addViewsForUser(userInput.getUsername(), pair.getValue());
        }
      }
    }
  }

  private JSONObject executeAction(final ActionInputData actionInput, final Writer writer) {
    switch (actionInput.getActionType()) {
      case Constants.COMMAND -> executeCommand(actionInput, writer);
      case Constants.QUERY -> new JSONObject();
      case Constants.RECOMMENDATION -> new JSONObject();
      default -> new JSONObject();
    }

    return new JSONObject();
  }

  private JSONObject executeCommand(final ActionInputData actionInput, final Writer writer) {
    final String type = actionInput.getType();

    if (type == null) {
      return new JSONObject();
    }

    return switch (type) {
      case Constants.VIEW_COMMAND -> executeViewCommand(actionInput, writer);
      case Constants.FAVORITE_COMMAND -> executeFavoriteCommand(actionInput, writer);
      case Constants.RATING_COMMAND -> executeRatingCommand(actionInput, writer);
      default -> new JSONObject();
    };
  }

  /**
   * Executes a view command
   *
   * @param actionInput action input data
   * @param output output writer
   * @return JsonObject
   */
  private JSONObject executeViewCommand(final ActionInputData actionInput, final Writer output) {
    int actionId = actionInput.getActionId();
    String movieTitle = actionInput.getTitle();

    boolean movieExists = movies.get(movieTitle) != null;

    if (movieExists) {
      movies.get(movieTitle).addViewer(actionInput.getUsername());
    }

    try {
      if (movieExists) {
        return output.writeFile(
            actionId,
            "message",
            "success -> "
                + movieTitle
                + " was viewed with total views of "
                + movies.get(movieTitle).getViewsCount());
      } else {
        // TO DO: Change text
        return output.writeFile(actionId, "message", "error -> " + movieTitle + " is not seen");
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
   * @param output output writer
   * @return JsonObject
   */
  private JSONObject executeFavoriteCommand(final ActionInputData actionInput,
                                            final Writer output) {
    int actionId = actionInput.getActionId();
    String movieTitle = actionInput.getTitle();
    String username = actionInput.getUsername();

    boolean movieExists = movies.get(movieTitle) != null;

    if (!movieExists) {
      return new JSONObject();
    }

    boolean hasBeenFavorited = false;
    boolean wasAlreadyFavorited = users.get(username).hasFavoriteMovie(movieTitle);
    boolean hasBeenViewedByUser = movies.get(movieTitle).hasBeenViewedByUser(username);

    if (movieExists && (hasBeenViewedByUser || wasAlreadyFavorited)) {
      if (!wasAlreadyFavorited) {
        users.get(username).addFavorite(movieTitle);
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
                          + movieTitle
                          + " is already in favourite list");
        } else {
          return output.writeFile(
                  actionId,
                  "message",
                  "success -> "
                          + movieTitle
                          + " was added as favourite");
        }
      } else {
        return output.writeFile(actionId, "message", "error -> " + movieTitle + " is not seen");
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
   * @param output output writer
   * @return JsonObject
   */
  private JSONObject executeRatingCommand(final ActionInputData actionInput,
                                          final Writer output) {
    int actionId = actionInput.getActionId();
    String user = actionInput.getUsername();
    String title = actionInput.getTitle();

    if (movies.containsKey(title)) {
      if (!movies.get(title).existsRatingFromUser(user)) {
        movies.get(title).addRatingForUser(user, actionInput.getGrade());
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
    if (serials.containsKey(title)) {
      if (!serials.get(title).getSeason(seasonNumber).isRatedByUser(user)) {
        serials.get(title).getSeason(seasonNumber).addRatingByUser(user, actionInput.getGrade());
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
      }
    }

    return new JSONObject();
  }
}
