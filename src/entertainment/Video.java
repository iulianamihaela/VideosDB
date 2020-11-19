package entertainment;

import java.util.ArrayList;

public abstract class Video {
  private String title;
  private int releaseYear;
  private ArrayList<Genre> genres;
  private ArrayList<String> cast;

  public Video(
      final String title,
      final int releaseYear,
      final ArrayList<Genre> genres,
      final ArrayList<String> cast) {
    this.title = title;
    this.releaseYear = releaseYear;
    this.genres = new ArrayList<>(genres);
    this.cast = new ArrayList<>(cast);
  }

  /** Video's title */
  public String getTitle() {
    return title;
  }

  /** The year the video was release */
  public int getReleaseYear() {
    return releaseYear;
  }

  /** Video's genres list */
  public ArrayList<Genre> getGenres() {
    return genres;
  }
}
