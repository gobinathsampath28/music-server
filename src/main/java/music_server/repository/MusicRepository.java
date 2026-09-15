package music_server.repository;

import music_server.model.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Repository
public class MusicRepository {

    private final Path musicFolder =
            Path.of("D:/PRACTICE PROJECTS/MusicApp/songsFolder")
                    .toAbsolutePath()
                    .normalize();

    public List<Song> findAllSongs() {

        try (Stream<Path> files = Files.list(musicFolder)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(this::isMp3)
                    .sorted()
                    .map(this::createSong)
                    .toList();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to read music folder", e);
        }
    }

    private boolean isMp3(Path file) {

        return file.toString()
                .toLowerCase()
                .endsWith(".mp3");
    }

    private Song createSong(Path file) {

        String fileName =
                file.getFileName().toString();

        String title =
                removeExtension(fileName);

        String artist =
                "Unknown Artist";

        String album =
                "Unknown Album";

        long duration = 0;

        try {

            AudioFile audioFile =
                    AudioFileIO.read(file.toFile());

            duration =
                    Math.round(
                            audioFile
                                    .getAudioHeader()
                                    .getTrackLength()
                    );

            Tag tag =
                    audioFile.getTag();

            if (tag != null) {

                String tagTitle =
                        tag.getFirst(FieldKey.TITLE);

                String tagArtist =
                        tag.getFirst(FieldKey.ARTIST);

                String tagAlbum =
                        tag.getFirst(FieldKey.ALBUM);

                if (tagTitle != null &&
                        !tagTitle.isBlank()) {

                    title = tagTitle;
                }

                if (tagArtist != null &&
                        !tagArtist.isBlank()) {

                    artist = tagArtist;
                }

                if (tagAlbum != null &&
                        !tagAlbum.isBlank()) {

                    album = tagAlbum;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to read metadata: "
                            + fileName);
        }

        return new Song(
                title,
                artist,
                album,
                fileName,
                duration
        );
    }

    private String removeExtension(
            String fileName) {

        int dotIndex =
                fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            return fileName;
        }

        return fileName.substring(
                0,
                dotIndex
        );
    }

    public Path findSongFile(
            String fileName) {

        Path file =
                musicFolder
                        .resolve(fileName)
                        .normalize();

        if (!file.startsWith(musicFolder)) {

            throw new IllegalArgumentException(
                    "Invalid file name"
            );
        }

        if (!Files.exists(file) ||
                !Files.isRegularFile(file)) {

            throw new RuntimeException(
                    "Song not found: "
                            + fileName
            );
        }

        return file;
    }
}