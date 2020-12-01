package main;

import actions.Action;
import actor.Actor;
import entertainment.Movie;
import entertainment.Serial;
import fileio.Input;
import fileio.Writer;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import fileio.ActorInputData;
import fileio.ActionInputData;
import org.json.simple.JSONArray;
import user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            result.add(Action.execute(database, actionInput, fileWriter));
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
}
