package music_server.service;

import music_server.model.Playlist;
import music_server.model.Song;
import music_server.repository.PlaylistRepository;
import music_server.repository.SongRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final SupabaseStorageService supabaseStorageService;

    public SongService(
            SongRepository songRepository,
            PlaylistRepository playlistRepository,
            SupabaseStorageService supabaseStorageService) {

        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    public List<Song> getSongs() {
        return songRepository.findAll();
    }

    public List<Song> searchSongs(String query) {

        if (query == null || query.isBlank()) {
            return getSongs();
        }

        return songRepository
                .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCase(
                        query,
                        query,
                        query
                );
    }

    public List<Song> getFavoriteSongs() {
        return songRepository.findByFavoriteTrue();
    }

    public Song toggleFavorite(Long songId) {

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new RuntimeException("Song not found: " + songId));

        song.setFavorite(!song.isFavorite());

        return songRepository.save(song);
    }

    public Song uploadSong(MultipartFile file)
            throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("MP3 file is empty");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        fileName = extractFileName(fileName);

        if (!fileName.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException(
                    "Only MP3 files are supported"
            );
        }

        /*
         * Upload the actual MP3 to Supabase.
         */
        supabaseStorageService.uploadFile(file);

        /*
         * Read metadata from the uploaded MultipartFile.
         */
        String title = removeExtension(fileName);

        String artist = "Unknown Artist";
        String album = "Unknown Album";
        long duration = 0;

        java.nio.file.Path tempFile = null;

        try {

            tempFile = java.nio.file.Files.createTempFile(
                    "music-app-",
                    ".mp3"
            );

            file.transferTo(tempFile);

            try {

                org.jaudiotagger.audio.AudioFile audioFile =
                        org.jaudiotagger.audio.AudioFileIO.read(
                                tempFile.toFile()
                        );

                duration = Math.round(
                        audioFile.getAudioHeader().getTrackLength()
                );

                org.jaudiotagger.tag.Tag tag =
                        audioFile.getTag();

                if (tag != null) {

                    String tagTitle =
                            tag.getFirst(
                                    org.jaudiotagger.tag.FieldKey.TITLE
                            );

                    String tagArtist =
                            tag.getFirst(
                                    org.jaudiotagger.tag.FieldKey.ARTIST
                            );

                    String tagAlbum =
                            tag.getFirst(
                                    org.jaudiotagger.tag.FieldKey.ALBUM
                            );

                    if (tagTitle != null && !tagTitle.isBlank()) {
                        title = tagTitle;
                    }

                    if (tagArtist != null && !tagArtist.isBlank()) {
                        artist = tagArtist;
                    }

                    if (tagAlbum != null && !tagAlbum.isBlank()) {
                        album = tagAlbum;
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "Unable to read MP3 metadata: "
                                + fileName
                );
            }

        } finally {

            if (tempFile != null) {

                try {
                    java.nio.file.Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }

        /*
         * If the song already exists, update its metadata.
         */
        Song song = songRepository
                .findByFileName(fileName)
                .orElseGet(Song::new);

        song.setTitle(title);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setFileName(fileName);
        song.setDuration(duration);

        return songRepository.save(song);
    }

    public String getSongUrl(String fileName) {

        return supabaseStorageService.getPublicUrl(fileName);
    }

    /*
     * Kept so your existing API doesn't break.
     * Cloud storage is now the source of truth.
     */
    public void synchronizeLibrary() {
        // Nothing to synchronize from the local PC anymore.
    }

    public Resource getSongResource(String fileName)
            throws MalformedURLException {

        throw new UnsupportedOperationException(
                "Songs are streamed directly from Supabase Storage."
        );
    }

    public List<Playlist> getPlaylists() {
        return playlistRepository.findAll();
    }

    public Playlist createPlaylist(String name) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Playlist name cannot be empty"
            );
        }

        return playlistRepository.save(
                new Playlist(name.trim())
        );
    }

    public Playlist getPlaylist(Long playlistId) {

        return playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Playlist not found: " + playlistId
                        )
                );
    }

    public Playlist addSongToPlaylist(
            Long playlistId,
            Long songId) {

        Playlist playlist = getPlaylist(playlistId);

        Song song = songRepository.findById(songId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Song not found: " + songId
                        )
                );

        if (!playlist.getSongs().contains(song)) {
            playlist.getSongs().add(song);
        }

        return playlistRepository.save(playlist);
    }

    public Playlist removeSongFromPlaylist(
            Long playlistId,
            Long songId) {

        Playlist playlist = getPlaylist(playlistId);

        playlist.getSongs()
                .removeIf(song ->
                        song.getId().equals(songId)
                );

        return playlistRepository.save(playlist);
    }

    public void deletePlaylist(Long playlistId) {

        Playlist playlist = getPlaylist(playlistId);

        playlistRepository.delete(playlist);
    }

    private String extractFileName(String originalFileName) {

        String fileName =
                originalFileName.replace("\\", "/");

        int slashIndex =
                fileName.lastIndexOf("/");

        if (slashIndex >= 0) {
            fileName =
                    fileName.substring(slashIndex + 1);
        }

        if (fileName.contains("..")) {
            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }

        return fileName;
    }

    private String removeExtension(String fileName) {

        int dotIndex =
                fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }
}