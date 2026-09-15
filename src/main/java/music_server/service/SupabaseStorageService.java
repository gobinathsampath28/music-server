package music_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SupabaseStorageService {

    @Value("${SUPABASE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_KEY}")
    private String serviceKey;

    @Value("${SUPABASE_BUCKET}")
    private String bucket;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadFile(MultipartFile file) throws IOException, InterruptedException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        String fileName = extractFileName(originalFileName);

        if (!fileName.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Only MP3 files are supported");
        }

        String encodedFileName = encodePathSegment(fileName);

        String uploadUrl =
                supabaseUrl
                        + "/storage/v1/object/"
                        + bucket
                        + "/"
                        + encodedFileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .header("Content-Type", getContentType(file))
                .header("x-upsert", "true")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Supabase upload failed. Status: "
                            + response.statusCode()
                            + " Response: "
                            + response.body()
            );
        }

        return getPublicUrl(fileName);
    }

    public String getPublicUrl(String fileName) {

        String encodedFileName = encodePathSegment(fileName);

        return supabaseUrl
                + "/storage/v1/object/public/"
                + bucket
                + "/"
                + encodedFileName;
    }

    private String extractFileName(String originalFileName) {

        String fileName = originalFileName
                .replace("\\", "/");

        int slashIndex = fileName.lastIndexOf("/");

        if (slashIndex >= 0) {
            fileName = fileName.substring(slashIndex + 1);
        }

        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        return fileName;
    }

    private String encodePathSegment(String value) {

        return URLEncoder
                .encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%2F", "%252F");
    }

    private String getContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            return "audio/mpeg";
        }

        return contentType;
    }
}