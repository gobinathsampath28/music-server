package music_server.repository;

import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class MusicRepository {

    /*
     * Local music-folder access has been removed.
     *
     * MP3 files are now stored in Supabase Storage.
     */

    public Path findSongFile(String fileName) {
        throw new UnsupportedOperationException(
                "Local music files are no longer used. Songs are stored in Supabase Storage."
        );
    }
}