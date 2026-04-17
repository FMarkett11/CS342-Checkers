import java.io.Serializable;

public class User implements Serializable {
    String username;
    String password;
    int wins;
    int losses;
    int draws;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }
}