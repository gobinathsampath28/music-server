package music_server.service;

import music_server.model.Playlist;
import music_server.model.Song;
import music_server.repository.MusicRepository;
import music_server.repository.PlaylistRepository;
import music_server.repository.SongRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Service
public class SongService {

    private final MusicRepository musicRepository;
    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;

    public SongService(
            MusicRepository musicRepository,
            SongRepository songRepository,
            PlaylistRepository playlistRepository) {

        this.musicRepository = musicRepository;
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
    }

    // =========================
    // SONGS
    // =========================

    public List<Song> getSongs() {

        synchronizeLibrary();

        return songRepository.findAll();
    }

    public List<Song> searchSongs(String query) {

        if (query == null || query.isBlank()) {
            return getSongs();
        }

        synchronizeLibrary();

        return songRepository
                .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCase(
                        query,
                        query,
                        query
                );
    }

    public List<Song> getFavoriteSongs() {

        synchronizeLibrary();

        return songRepository.findByFavoriteTrue();
    }

    public Song toggleFavorite(Long songId) {

        Song song = songRepository
                .findById(songId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Song not found: " + songId
                        )
                );

        song.setFavorite(!song.isFavorite());

        return songRepository.save(song);
    }

    // =========================
    // LIBRARY SYNC
    // =========================

    public void synchronizeLibrary() {

        List<Song> folderSongs =
                musicRepository.findAllSongs();

        for (Song folderSong : folderSongs) {

            songRepository
                    .findByFileName(
                            folderSong.getFileName()
                    )
                    .ifPresentOrElse(

                            existingSong -> {

                                existingSong.setTitle(
                                        folderSong.getTitle()
                                );

                                existingSong.setArtist(
                                        folderSong.getArtist()
                                );

                                existingSong.setAlbum(
                                        folderSong.getAlbum()
                                );

                                existingSong.setDuration(
                                        folderSong.getDuration()
                                );

                                songRepository.save(
                                        existingSong
                                );
                            },

                            () -> {

                                songRepository.save(
                                        folderSong
                                );
                            }
                    );
        }

        // Remove songs from DB
        // if their files no longer exist.
        List<Song> databaseSongs =
                songRepository.findAll();

        for (Song databaseSong : databaseSongs) {

            boolean exists =
                    folderSongs.stream()
                            .anyMatch(folderSong ->
                                    folderSong
                                            .getFileName()
                                            .equals(
                                                    databaseSong
                                                            .getFileName()
                                            )
                            );

            if (!exists) {

                songRepository.delete(
                        databaseSong
                );
            }
        }
    }

    // =========================
    // STREAM
    // =========================

    public Resource getSongResource(
            String fileName)
            throws MalformedURLException {

        Path file =
                musicRepository
                        .findSongFile(fileName);

        return new UrlResource(
                file.toUri()
        );
    }

    // =========================
    // PLAYLISTS
    // =========================

    public List<Playlist> getPlaylists() {

        return playlistRepository.findAll();
    }

    public Playlist createPlaylist(
            String name) {

        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Playlist name cannot be empty"
            );
        }

        Playlist playlist =
                new Playlist(name.trim());

        return playlistRepository.save(
                playlist
        );
    }

    public Playlist getPlaylist(Long playlistId) {

        return playlistRepository
                .findById(playlistId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Playlist not found: "
                                        + playlistId
                        )
                );
    }

    public Playlist addSongToPlaylist(
            Long playlistId,
            Long songId) {

        Playlist playlist =
                getPlaylist(playlistId);

        Song song =
                songRepository
                        .findById(songId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Song not found: "
                                                + songId
                                )
                        );

        if (!playlist.getSongs().contains(song)) {

            playlist.getSongs().add(song);
        }

        return playlistRepository.save(
                playlist
        );
    }

    public Playlist removeSongFromPlaylist(
            Long playlistId,
            Long songId) {

        Playlist playlist =
                getPlaylist(playlistId);

        playlist.getSongs()
                .removeIf(song ->
                        song.getId()
                                .equals(songId)
                );

        return playlistRepository.save(
                playlist
        );
    }

    public void deletePlaylist(
            Long playlistId) {

        Playlist playlist =
                getPlaylist(playlistId);

        playlistRepository.delete(
                playlist
        );
    }
}