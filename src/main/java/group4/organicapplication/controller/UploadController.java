package group4.organicapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Controller
public class UploadController {

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        if (!file.getContentType().equals("audio/mpeg")) {
            throw new RuntimeException("File is not an MP3");
        }

        // Sanitize filename
        String filename = file.getOriginalFilename().replace(" ", "_");

        // Save file
        FileOutputStream out = new FileOutputStream("/uploads/" + filename);
        out.write(file.getBytes());
        out.close();

        // Return success message
        return "Upload successful";
    }
}
