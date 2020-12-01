package main;

import actor.Actor;
import entertainment.Movie;
import entertainment.Serial;
import user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {
    private HashMap<String, Movie> movies;
    private HashMap<String, Serial> serials;
    private HashMap<String, Actor> actors;
    private HashMap<String, User> users;

    private List<String> videosOrder;

    public Database() {
        movies = new HashMap<>();
        serials = new HashMap<>();
        actors = new HashMap<>();
        users = new HashMap<>();

        videosOrder = new ArrayList<>();
    }

    /**
     * Get movies from database
     * @return HashMap<MovieTitle, Movie>
     */
    public HashMap<String, Movie> getMovies() {
        return movies;
    }

    /**
     * Get serials from database
     * @return HashMap<SerialTitle, Serial>
     */
    public HashMap<String, Serial> getSerials() {
        return serials;
    }

    /**
     * Get actors from database
     * @return HashMap<ActorName, Actor>
     */
    public HashMap<String, Actor> getActors() {
        return actors;
    }

    /**
     * Get users from database
     * @return HashMap<UserName, User>
     */
    public HashMap<String, User> getUsers() {
        return users;
    }

    /**
     * Get videos order from database
     * @return List<VideoTitle>
     */
    public List<String> getVideosOrder() {
        return videosOrder;
    }
}
