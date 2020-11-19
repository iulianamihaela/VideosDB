package entertainment;

import java.util.ArrayList;

public abstract class Video {
    private String title;
    private int releaseYear;
    private ArrayList<Genre> genres;

    public Video(String title, int releaseYear) {
        this.title = title;
        this.releaseYear = releaseYear;
    }

    public Video(String title, int releaseYear, ArrayList<Genre> genres) {
        this(title, releaseYear);
        this.genres = new ArrayList<>(genres);
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }
}
