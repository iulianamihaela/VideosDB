package user;

import fileio.UserInputData;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
  private String username;
  private UserType userType;

  private HashMap<String, Integer> history;
  private ArrayList<String> favoriteVideos;
  private ArrayList<String> viewedVideos;

  public User(
      final String username,
      final UserType userType,
      final ArrayList<String> favoriteVideos,
      final Map<String, Integer> history) {
    this.username = username;
    this.userType = userType;

    this.favoriteVideos = new ArrayList<String>(favoriteVideos);
    this.history = new HashMap<String, Integer>(history);

    viewedVideos = new ArrayList<>();
  }

  public User(final UserInputData userInput) {
    this(
        userInput.getUsername(),
        Utils.stringToUserType(userInput.getSubscriptionType()),
        userInput.getFavoriteMovies(),
        userInput.getHistory());
  }
}
