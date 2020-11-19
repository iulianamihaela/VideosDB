package actor;

import fileio.ActorInputData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Actor {
  private String name;
  private String careerDescription;
  private HashMap<ActorsAwards, Integer> awards;
  private ArrayList<String> filmography;

  public Actor(
      final String name,
      final String careerDescription,
      final Map<ActorsAwards, Integer> awards,
      final ArrayList<String> filmography) {
    this.name = name;
    this.careerDescription = careerDescription;
    this.awards = new HashMap<>(awards);
    this.filmography = new ArrayList<>(filmography);
  }

  public Actor(final ActorInputData actorInput) {
    this(
        actorInput.getName(),
        actorInput.getCareerDescription(),
        actorInput.getAwards(),
        actorInput.getFilmography());
  }
}
