package audio.omgsoundboard.android;

import android.content.SharedPreferences;

/**
 * Created by kkelly on 11/1/15.
 */
public class FavStore {
    private static FavStore instance;
    private SharedPreferences prefs;

    public static void init(SharedPreferences prefs) {
        instance = new FavStore(prefs);
    }

    private FavStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static FavStore getInstance() {
        return instance;
    }

    public void setSoundFavorited(String soundName, boolean shouldFavorite) {
        prefs.edit().putBoolean(soundName, shouldFavorite).apply();
    }

    public boolean isSoundFavorited(String soundName) {
        return prefs.getBoolean(soundName, false);
    }

    public void setShowFavorites(boolean showFavorites) {
        prefs.edit().putBoolean("switch", showFavorites).apply();
    }

    public boolean getShowFavorites() {
        return prefs.getBoolean("switch", false);
    }
}
