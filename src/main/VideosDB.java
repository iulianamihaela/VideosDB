package main;

import entertainment.Movie;
import entertainment.Serial;
import fileio.Input;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import user.User;

import java.util.ArrayList;
import java.util.List;

public class VideosDB {
  private ArrayList<Movie> movies;
  private ArrayList<Serial> serials;
  private ArrayList<User> users;

  public VideosDB() {
    movies = new ArrayList<>();
    serials = new ArrayList<>();
    users = new ArrayList<>();
  }

  /**
   * Reads video database based on input
   *
   * @param input Data input from JSON
   */
  public void readDB(final Input input) {
    readMovies(input.getMovies());
    readSerials(input.getSerials());
    readUsers(input.getUsers());
  }

  /**
   * Reads movies from list
   *
   * @param movieInputDataList list of input data for movies
   */
  public void readMovies(final List<MovieInputData> movieInputDataList) {
    for (MovieInputData movieInput : movieInputDataList) {
      movies.add(new Movie(movieInput));
    }
  }

  /**
   * Reads serials from list
   *
   * @param serialInputDataList list of input data for serials
   */
  public void readSerials(final List<SerialInputData> serialInputDataList) {
    for (SerialInputData serialInput : serialInputDataList) {
      serials.add(new Serial(serialInput));
    }
  }

  /**
   * Reads users from list
   *
   * @param userInputDataList list of input data for users
   */
  public void readUsers(final List<UserInputData> userInputDataList) {
    for (UserInputData userInput : userInputDataList) {
      users.add(new User(userInput));
    }
  }
}
