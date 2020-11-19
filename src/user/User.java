package user;

import entertainment.Video;

import java.util.ArrayList;

public class User {
    private UserType userType;

    private ArrayList<Video> favoriteVideos;
    private ArrayList<Video> viewedVideos;

    public User(UserType userType) {
        this.userType = userType;

        favoriteVideos = new ArrayList<>();
        viewedVideos = new ArrayList<>();
    }
}
