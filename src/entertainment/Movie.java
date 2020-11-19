package entertainment;

import fileio.MovieInputData;
import utils.Utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Movie extends Video {
  private double rating;
  private int duration;

  public Movie(
      final String title,
      final int releaseYear,
      final ArrayList<Genre> genres,
      final ArrayList<String> cast,
      final int duration) {
    super(title, releaseYear, genres, cast);

    this.duration = duration;
    rating = 0;
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
}
