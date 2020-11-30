package entertainment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Information about a season of a tv show
 *
 * <p>DO NOT MODIFY
 */
public final class Season {
  /** Number of current season */
  private final int currentSeason;
  /** Duration in minutes of a season */
  private int duration;
  /** List of ratings for each season */
  private List<Double> ratings;

  private HashMap<String, Double> ratingsByUser;

  public Season(final int currentSeason, final int duration) {
    this.currentSeason = currentSeason;
    this.duration = duration;
    this.ratings = new ArrayList<>();
    this.ratingsByUser = new HashMap<>();
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(final int duration) {
    this.duration = duration;
  }

  public List<Double> getRatings() {
    return ratings;
  }

  public void setRatings(final List<Double> ratings) {
    this.ratings = ratings;
  }

  /**
   * Adds a rating given by an user
   * @param user user that rated the season
   * @param rating rating value
   */
  public void addRatingByUser(final String user, final Double rating) {
    ratingsByUser.put(user, rating);
    ratings.add(rating);
  }

  /**
   * Retrieve the season's rating
   * @return season's rating
   */
  public Double getRating() {
    if (ratings.size() == 0) {
      return (double) 0;
    }

    return ratings.stream().mapToDouble(Double::doubleValue).sum() / ratings.size();
  }

  /**
   * Checks if the season has been rated by a given user
   * @param user user to check
   * @return if the season has been rated by the given user
   */
  public boolean isRatedByUser(final String user) {
    return ratingsByUser.containsKey(user);
  }

  @Override
  public String toString() {
    return "Episode{" + "currentSeason=" + currentSeason + ", duration=" + duration + '}';
  }
}
