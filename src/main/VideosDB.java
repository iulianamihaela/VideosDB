package main;

import actor.Actor;
import common.ActorWithSortingCriteria;
import common.Constants;
import entertainment.Movie;
import entertainment.Serial;
import fileio.Input;
import fileio.UserInputData;
import fileio.SerialInputData;
import fileio.MovieInputData;
import fileio.ActionInputData;
import fileio.ActorInputData;
import fileio.Writer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import user.User;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class VideosDB {
  private final HashMap<String, Movie> movies;
  private final HashMap<String, Serial> serials;
  private final HashMap<String, Actor> actors;
  private final HashMap<String, User> users;

  public VideosDB() {
    movies = new HashMap<>();
    serials = new HashMap<>();
    users = new HashMap<>();
    actors = new HashMap<>();
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
   * Reads actors from list
   * @param actorInputDataList list of input data for actors
   */
  private void readActors(final List<ActorInputData> actorInputDataList) {
    for (ActorInputData actorInput : actorInputDataList) {
      actors.put(actorInput.getName(), new Actor(actorInput));
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
    return switch (actionInput.getActionType()) {
      case Constants.COMMAND -> executeCommand(actionInput, writer);
      case Constants.QUERY -> executeQuery(actionInput, writer);
      case Constants.RECOMMENDATION -> new JSONObject();
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

  /**
   * Process a query command
   * @param actionInput action input
   * @param writer output writer
   * @return result
   */
  private JSONObject executeQuery(final ActionInputData actionInput, final Writer writer) {
    return switch (actionInput.getObjectType()) {
      case Constants.ACTORS -> executeActorsCriteria(actionInput, writer);
      default -> new JSONObject();
    };
  }

  /**
   * Process a criteria actor execution
   * @param actionInput action input
   * @param writer output writer
   * @return result
   */
  private JSONObject executeActorsCriteria(final ActionInputData actionInput, final Writer writer) {
    return switch (actionInput.getCriteria()) {
      case Constants.AVERAGE -> executeActorsAverageQuery(actionInput, writer);
      case Constants.AWARDS -> executeActorsAwardsCriteria(actionInput, writer);
      case Constants.FILTER_DESCRIPTIONS -> executeActorsFilterDescription(actionInput, writer);
      default -> new JSONObject();
    };
  }

  /**
   * Executes an Actors Average query
   * @param actionInput action input data
   * @param writer output writer
   * @return result
   */
  public JSONObject executeActorsAverageQuery(final ActionInputData actionInput,
                                              final Writer writer) {
    HashMap<String, Double> ratingForUser = new HashMap<>();
    HashMap<String, Integer> noRatingsForUser = new HashMap<>();

    ArrayList<ActorWithSortingCriteria> actorsWithRating = new ArrayList<>();

    for (Movie movie : movies.values()) {
      for (String actorName : movie.getCast()) {
        if (movie.getRating() != 0) {
          if (ratingForUser.containsKey(actorName)) {
            ratingForUser.put(actorName, ratingForUser.get(actorName) + movie.getRating());
            noRatingsForUser.put(actorName, noRatingsForUser.get(actorName) + 1);
          } else {
            ratingForUser.put(actorName, movie.getRating());
            noRatingsForUser.put(actorName, 1);
          }
        }
      }
    }

    for (Serial serial : serials.values()) {
      for (String actorName : serial.getCast()) {
        if (serial.getRating() != 0) {
          if (ratingForUser.containsKey(actorName)) {
            ratingForUser.put(actorName, ratingForUser.get(actorName) + serial.getRating());
            noRatingsForUser.put(actorName, noRatingsForUser.get(actorName) + 1);
          } else {
            ratingForUser.put(actorName, serial.getRating());
            noRatingsForUser.put(actorName, 1);
          }
        }
      }
    }

    for (String name : ratingForUser.keySet()) {
      actorsWithRating.add(new ActorWithSortingCriteria(name,
              ratingForUser.get(name) / noRatingsForUser.get(name)));
    }

    Collections.sort(actorsWithRating);

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
   * @param actionInput action input data
   * @param writer output writer
   * @return result
   */
  public JSONObject executeActorsAwardsCriteria(final ActionInputData actionInput,
                                                final Writer writer) {
    List<String> awardsList = actionInput.getFilters().get(Constants.AWARDS_FILTER_POSITION);

    List<ActorWithSortingCriteria> actorsResult = new ArrayList<>();

    for (Actor actor : actors.values()) {
      boolean hasAwards = true;

      for (String award : awardsList) {
        if (!actor.getAwards().containsKey(award)) {
          hasAwards = false;
          break;
        }
      }

      if (hasAwards) {
        actorsResult.add(new ActorWithSortingCriteria(actor.getName(),
                (double) (actor.getAwardsCount())));
      }
    }

    if (actionInput.getSortType() == Constants.ASC_SORTING) {
      Collections.sort(actorsResult);
    }
    if (actionInput.getSortType() == Constants.DESC_SORTING) {
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
   * @param actionInput action input data
   * @param writer output writer
   * @return result
   */
  public JSONObject executeActorsFilterDescription(final ActionInputData actionInput,
                                                   final Writer writer) {
    List<String> keywords = actionInput.getFilters().get(Constants.WORDS_FILTER_POSITION);

    List<ActorWithSortingCriteria> actorsResult = new ArrayList<>();

    for (Actor actor : actors.values()) {
      boolean containsKeywords = true;

      for (String keyword : keywords) {
        if (!actor.getCareerDescription().contains(keyword)) {
          containsKeywords = false;
          break;
        }
      }

      if (containsKeywords) {
        actorsResult.add(new ActorWithSortingCriteria(actor.getName(), (double) 0));
      }
    }

    if (actionInput.getSortType() == Constants.ASC_SORTING) {
      Collections.sort(actorsResult);
    }
    if (actionInput.getSortType() == Constants.DESC_SORTING) {
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
