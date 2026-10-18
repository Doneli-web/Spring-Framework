package framework.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapFile {
    private String fileName;
    private String extension;
    private byte[] bytes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }


    public void saveFile(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "Le dossier de destination est obligatoire."
            );
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Aucun fichier valide n'a été fourni."
            );
        }
        if (bytes == null) {
            throw new IllegalArgumentException(
                    "Le contenu du fichier est absent."
            );
        }

        Path dossier = Path.of(path);
        Files.createDirectories(dossier);

        Path destination = dossier.resolve(fileName);

        Files.write(destination, bytes);
    }

}
