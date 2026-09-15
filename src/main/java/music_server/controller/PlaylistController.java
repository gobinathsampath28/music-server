package music_server.controller;

import music_server.model.Playlist;
import music_server.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final SongService songService;

    public PlaylistController(
            SongService songService) {

        this.songService =
                songService;
    }

    // =========================
    // GET ALL PLAYLISTS
    // =========================

    @GetMapping
    public List<Playlist> getPlaylists() {

        return songService.getPlaylists();
    }

    // =========================
    // CREATE PLAYLIST
    // =========================

    @PostMapping
    public Playlist createPlaylist(
            @RequestParam String name) {

        return songService.createPlaylist(name);
    }

    // =========================
    // GET ONE PLAYLIST
    // =========================

    @GetMapping("/{id}")
    public Playlist getPlaylist(
            @PathVariable Long id) {

        return songService.getPlaylist(id);
    }

    // =========================
    // ADD SONG
    // =========================

    @PostMapping("/{playlistId}/songs/{songId}")
    public Playlist addSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {

        return songService.addSongToPlaylist(
                playlistId,
                songId
        );
    }

    // =========================
    // REMOVE SONG
    // =========================

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public Playlist removeSong(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {

        return songService.removeSongFromPlaylist(
                playlistId,
                songId
        );
    }

    // =========================
    // DELETE PLAYLIST
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlaylist(
            @PathVariable Long id) {

        songService.deletePlaylist(id);

        return ResponseEntity.ok(
                "Playlist deleted"
        );
    }
}