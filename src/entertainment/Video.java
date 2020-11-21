package entertainment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Video {
  private String title;
  private int releaseYear;
  private ArrayList<Genre> genres;
  private ArrayList<String> cast;
  private HashMap<String, Integer> views;

  public Video(
      final String title,
      final int releaseYear,
      final ArrayList<Genre> genres,
      final ArrayList<String> cast) {
    this.title = title;
    this.releaseYear = releaseYear;
    this.genres = new ArrayList<>(genres);
    this.cast = new ArrayList<>(cast);

    views = new HashMap<>();
  }

  /**
   * Add a view from user
   * @param username user that has watched the video
   */
  public void addViewer(final String username) {
    if (views.containsKey(username)) {
      views.put(username, views.get(username) + 1);
    } else {
      views.put(username, 1);
    }
  }

  /**
   * Add a number of views for user
   * @param username user that has watched the video
   * @param count number of views
   */
  public void addViewsForUser(final String username, final Integer count) {
    if (views.containsKey(username)) {
      views.put(username, views.get(username) + count);
    } else {
      views.put(username, count);
    }
  }

  /**
   * Gets the views count
   * @return number of views
   */
  public int getViewsCount() {
    int totalViews = 0;

    for (Map.Entry<String, Integer> pair : views.entrySet()) {
        totalViews += pair.getValue();
    }

    return totalViews;
  }

  /**
   * Return if the video has been viewed by the given user
   * @param username given user
   * @return has been viewed by user
   */
  public boolean hasBeenViewedByUser(final String username) {
    return views.containsKey(username)
            && views.get(username) >= 1;
  }
}
