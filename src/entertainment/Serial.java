package entertainment;

import fileio.SerialInputData;
import utils.Utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Serial extends Video {
  private ArrayList<Season> seasons;

  public Serial(
      final String title,
      final int releaseYear,
      final ArrayList<Genre> genres,
      final ArrayList<String> cast,
      final ArrayList<Season> seasons) {
    super(title, releaseYear, genres, cast);

    this.seasons = new ArrayList<>(seasons);
  }

  public Serial(final SerialInputData serialInput) {
    this(
        serialInput.getTitle(),
        serialInput.getYear(),
        new ArrayList<Genre>(
            serialInput
                // Obtinem lista de genuri (ca string)
                .getGenres()
                // Pentru fiecare gen facem conversia la enum
                .stream()
                .map(g -> Utils.stringToGenre(g))
                // Convertim in lista
                .collect(Collectors.toList())),
        serialInput.getCast(),
        serialInput.getSeasons());
  }

  /**
   * Returns the season with the given number
   * @param seasonNumber season's number
   * @return season
   */
  public Season getSeason(final int seasonNumber) {
    return seasons.get(seasonNumber);
  }

  /**
   * Retrieves the serial's rating
   * @return serial's rating
   */
  @Override
  public Double getRating() {
    double rating = 0;

    if (seasons == null || seasons.size() == 0) {
      return (double) 0;
    }

    for (Season season : seasons) {
      rating += season.getRating();
    }

    return rating / seasons.size();
  }
}
