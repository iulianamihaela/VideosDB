package entertainment;

import java.util.ArrayList;

public class Movie extends Video {
    private double rating;
    private int duration;

    public Movie(String title, int releaseYear) {
        super(title, releaseYear);
    }

    public Movie(String title, int releaseYear, ArrayList<Genre> genres) {
        super(title, releaseYear, genres);
    }
}
