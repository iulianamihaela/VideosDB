package entertainment;

import fileio.MovieInputData;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Movie extends Video {
  private double rating;
  private int duration;

  private HashMap<String, Double> ratingsFromUsers;

  public Movie(
      final String title,
      final int releaseYear,
      final ArrayList<Genre> genres,
      final ArrayList<String> cast,
      final int duration) {
    super(title, releaseYear, genres, cast);

    this.duration = duration;

    rating = 0;
    ratingsFromUsers = new HashMap<>();
  }

  public Movie(final MovieInputData movieInput) {
    this(
        movieInput.getTitle(),
        movieInput.getYear(),
        new ArrayList<Genre>(
            movieInput
                // Obtinem lista de genuri (ca string)
                .getGenres()
                // Pentru fiecare gen facem conversia la enum
                .stream()
                .map(g -> Utils.stringToGenre(g))
                // Convertim in lista
                .collect(Collectors.toList())),
        movieInput.getCast(),
        movieInput.getDuration());
  }

  /**
   * Adds rating given by user
   * @param user user that rates
   * @param givenRating rating given by user
   */
  public void addRatingForUser(final String user, final Double givenRating) {
    ratingsFromUsers.put(user, givenRating);
  }

  /**
   * Returns if a user has rated the movie
   * @param user user that needs to be checked
   * @return if the user has rated the movie
   */
  public boolean existsRatingFromUser(final String user) {
    return ratingsFromUsers.containsKey(user);
  }

  /**
   * Gets the movie's rating
   * @return rating of movie
   */
  @Override
  public Double getRating() {
    if (ratingsFromUsers.size() == 0) {
      return (double) 0;
    }

    return ratingsFromUsers.values().stream().mapToDouble(Double::doubleValue).sum()
            / ratingsFromUsers.values().size();
  }

  /**
   * Gets the movie's duration
   * @return movie's duration
   */
  public int getDuration() {
    return duration;
  }
}
