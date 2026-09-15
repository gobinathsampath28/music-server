package music_server.controller;

import music_server.model.Playlist;
import music_server.model.Song;
import music_server.service.SongService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(
            SongService songService) {

        this.songService =
                songService;
    }

    // =========================
    // ALL SONGS
    // =========================

    @GetMapping
    public List<Song> getSongs() {

        return songService.getSongs();
    }

    // =========================
    // SEARCH
    // =========================

    @GetMapping("/search")
    public List<Song> searchSongs(
            @RequestParam String q) {

        return songService.searchSongs(q);
    }

    // =========================
    // FAVORITES
    // =========================

    @GetMapping("/favorites")
    public List<Song> getFavoriteSongs() {

        return songService.getFavoriteSongs();
    }

    @PutMapping("/{id}/favorite")
    public Song toggleFavorite(
            @PathVariable Long id) {

        return songService.toggleFavorite(id);
    }

    // =========================
    // SYNC
    // =========================

    @PostMapping("/sync")
    public ResponseEntity<String> syncSongs() {

        songService.synchronizeLibrary();

        return ResponseEntity.ok(
                "Music library synchronized"
        );
    }

    // =========================
    // STREAM
    // =========================

    @GetMapping("/{fileName}/stream")
    public ResponseEntity<Resource> streamSong(
            @PathVariable String fileName)
            throws MalformedURLException {

        Resource resource =
                songService
                        .getSongResource(fileName);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "audio/mpeg"
                        )
                )
                .body(resource);
    }
}