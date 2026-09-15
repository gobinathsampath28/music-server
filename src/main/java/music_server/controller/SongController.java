package music_server.controller;

import music_server.model.Song;
import music_server.service.SongService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public List<Song> getSongs() {
        return songService.getSongs();
    }

    @GetMapping("/search")
    public List<Song> searchSongs(
            @RequestParam String q) {

        return songService.searchSongs(q);
    }

    @GetMapping("/favorites")
    public List<Song> getFavoriteSongs() {

        return songService.getFavoriteSongs();
    }

    @PutMapping("/{id}/favorite")
    public Song toggleFavorite(
            @PathVariable Long id) {

        return songService.toggleFavorite(id);
    }

    @PostMapping("/upload")
    public Song uploadSong(
            @RequestParam("file") MultipartFile file)
            throws Exception {

        return songService.uploadSong(file);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncSongs() {

        songService.synchronizeLibrary();

        return ResponseEntity.ok(
                "Music library synchronized"
        );
    }

    @GetMapping("/{fileName}/stream")
    public ResponseEntity<Void> streamSong(
            @PathVariable String fileName) {

        String songUrl =
                songService.getSongUrl(fileName);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(
                        HttpHeaders.LOCATION,
                        songUrl
                )
                .build();
    }
}