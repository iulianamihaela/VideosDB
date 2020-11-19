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
}
