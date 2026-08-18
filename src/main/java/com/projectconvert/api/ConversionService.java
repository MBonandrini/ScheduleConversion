package com.projectconvert.api;

import org.mpxj.ProjectFile;
import org.mpxj.reader.UniversalProjectReader;
import org.mpxj.writer.FileFormat;
import org.mpxj.writer.UniversalProjectWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Service
public class ConversionService {

    public ConversionResult convert(MultipartFile upload, String target) throws Exception {
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("No project file was supplied.");
        }

        OutputFormat output = OutputFormat.fromRequest(target);
        String originalName = safeFilename(upload.getOriginalFilename());
        String sourceSuffix = suffix(originalName);

        Path input = Files.createTempFile("projectconvert-input-", sourceSuffix);
        Path outputFile = Files.createTempFile("projectconvert-output-", "." + output.extension());

        try {
            upload.transferTo(input);

            UniversalProjectReader reader = new UniversalProjectReader();
            List<ProjectFile> projects = reader.readAll(input.toFile());

            if (projects == null || projects.isEmpty()) {
                throw new IllegalArgumentException("MPXJ could not find a project in the uploaded file.");
            }

            UniversalProjectWriter writer = new UniversalProjectWriter(output.fileFormat());

            // XER can contain multiple projects, so preserve all projects when possible.
            // MSPDI represents a single Microsoft Project schedule; for multi-project input,
            // exporting all schedules into one MSPDI file is not supported by the format.
            if (output == OutputFormat.XER && projects.size() > 1) {
                writer.write(projects, outputFile.toString());
            } else {
                writer.write(projects.get(0), outputFile.toString());
            }

            byte[] bytes = Files.readAllBytes(outputFile);
            String baseName = stripExtension(originalName);
            String downloadName = baseName + "_converted." + output.extension();

            return new ConversionResult(bytes, downloadName, output.contentType(), projects.size());
        } finally {
            deleteQuietly(input);
            deleteQuietly(outputFile);
        }
    }

    private static String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "project";
        }
        String normalized = filename.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private static String suffix(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return ".tmp";
        }
        String suffix = filename.substring(dot).toLowerCase(Locale.ROOT);
        return suffix.length() <= 16 ? suffix : ".tmp";
    }

    private static void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public record ConversionResult(
            byte[] bytes,
            String filename,
            String contentType,
            int sourceProjectCount) {
    }

    private enum OutputFormat {
        XER(FileFormat.XER, "xer", "application/octet-stream"),
        MSPDI(FileFormat.MSPDI, "xml", "application/xml");

        private final FileFormat fileFormat;
        private final String extension;
        private final String contentType;

        OutputFormat(FileFormat fileFormat, String extension, String contentType) {
            this.fileFormat = fileFormat;
            this.extension = extension;
            this.contentType = contentType;
        }

        public FileFormat fileFormat() {
            return fileFormat;
        }

        public String extension() {
            return extension;
        }

        public String contentType() {
            return contentType;
        }

        static OutputFormat fromRequest(String value) {
            if (value == null) {
                throw new IllegalArgumentException("No output format was selected.");
            }

            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "xer" -> XER;
                case "xml", "mspdi" -> MSPDI;
                default -> throw new IllegalArgumentException(
                        "Unsupported output format. Choose XER or Microsoft Project XML (MSPDI)."
                );
            };
        }
    }
}
