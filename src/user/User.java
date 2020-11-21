package user;

import fileio.UserInputData;
import utils.Utils;

import java.util.ArrayList;

public class User {
  private String username;
  private UserType userType;

  private ArrayList<String> favoriteVideos;
  private ArrayList<String> viewedVideos;

  public User(
      final String username,
      final UserType userType,
      final ArrayList<String> favoriteVideos) {
    this.username = username;
    this.userType = userType;

    this.favoriteVideos = new ArrayList<String>(favoriteVideos);

    viewedVideos = new ArrayList<>();
  }

  public User(final UserInputData userInput) {
    this(
        userInput.getUsername(),
        Utils.stringToUserType(userInput.getSubscriptionType()),
        userInput.getFavoriteMovies());
  }

  /**
   * Gets the user's name
   * @return username of user
   */
  public String getUsername() {
    return username;
  }

  /**
   * Adds a movie as favorite if it is not already
   * @param movieTitle movie title
   */
  public void addFavorite(final String movieTitle) {
    if (!favoriteVideos.contains(movieTitle)) {
      favoriteVideos.add(movieTitle);
    }
  }

  /**
   * Get if user has the given movie as favorite
   * @param movieTitle given movie
   * @return is favorite
   */
  public boolean hasFavoriteMovie(final String movieTitle) {
    return favoriteVideos.contains(movieTitle);
  }
}
