package com.fuar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS configuration is now handled in SecurityConfig
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get project root directory
        String projectRoot = System.getProperty("user.dir");
        System.out.println("Project root directory: " + projectRoot);
        
        // Define multiple possible upload directories
        String projectUploadDir = projectRoot + "/uploads";
        String tempUploadDir = System.getProperty("java.io.tmpdir") + "/uploads";
        String homeUploadDir = System.getProperty("user.home") + "/uploads";
        String relativeUploadDir = "uploads";
        
        // Create project uploads directory
        createDirectoryWithPermissions(projectUploadDir, "Project upload directory");
        createDirectoryWithPermissions(projectUploadDir + "/profiles", "Project profiles directory");
        
        // Create other directories if they don't exist
        createDirectoryWithPermissions(tempUploadDir, "Temp upload directory");
        createDirectoryWithPermissions(tempUploadDir + "/profiles", "Temp profiles directory");
        
        createDirectoryWithPermissions(homeUploadDir, "Home upload directory");
        createDirectoryWithPermissions(homeUploadDir + "/profiles", "Home profiles directory");
        
        createDirectoryWithPermissions(relativeUploadDir, "Relative upload directory");
        createDirectoryWithPermissions(relativeUploadDir + "/profiles", "Relative profiles directory");
        
        // Add resource handlers for all possible upload locations with explicit paths
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "file:" + projectUploadDir + "/",
                    "file:" + tempUploadDir + "/",
                    "file:" + homeUploadDir + "/",
                    "file:" + relativeUploadDir + "/"
                )
                .setCachePeriod(3600) // Cache period in seconds
                .resourceChain(true);
        
        // Explicitly add a handler for the profile images
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(
                    "file:" + projectUploadDir + "/profiles/",
                    "file:" + tempUploadDir + "/profiles/",
                    "file:" + homeUploadDir + "/profiles/",
                    "file:" + relativeUploadDir + "/profiles/"
                )
                .setCachePeriod(0) // Disable caching for profile images to ensure fresh content
                .resourceChain(true);
        
        System.out.println("Resource handlers configured for the following paths:");
        System.out.println("- " + projectUploadDir + "/");
        System.out.println("- " + tempUploadDir + "/");
        System.out.println("- " + homeUploadDir + "/");
        System.out.println("- " + relativeUploadDir + "/");
    }
    
    /**
     * Creates a directory with appropriate permissions
     * @param directoryPath The path to create
     * @param directoryDescription A description for logging
     */
    private void createDirectoryWithPermissions(String directoryPath, String directoryDescription) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            try {
                boolean created = directory.mkdirs();
                if (created) {
                    System.out.println(directoryDescription + " created: " + directory.getAbsolutePath());
                    
                    // Set permissions to 755 (rwxr-xr-x) to ensure read access
                    try {
                        Set<PosixFilePermission> permissions = new HashSet<>();
                        permissions.add(PosixFilePermission.OWNER_READ);
                        permissions.add(PosixFilePermission.OWNER_WRITE);
                        permissions.add(PosixFilePermission.OWNER_EXECUTE);
                        permissions.add(PosixFilePermission.GROUP_READ);
                        permissions.add(PosixFilePermission.GROUP_EXECUTE);
                        permissions.add(PosixFilePermission.OTHERS_READ);
                        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                        
                        Files.setPosixFilePermissions(Paths.get(directory.getAbsolutePath()), permissions);
                        System.out.println("Permissions set for " + directoryDescription);
                    } catch (Exception e) {
                        System.err.println("Failed to set permissions for " + directoryDescription + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Failed to create " + directoryDescription + ": " + directory.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error creating " + directoryDescription + ": " + e.getMessage());
            }
        } else {
            System.out.println(directoryDescription + " already exists: " + directory.getAbsolutePath());
            // Verify directory is readable and writable
            if (!directory.canRead()) {
                System.err.println("WARNING: " + directoryDescription + " is not readable!");
            }
            if (!directory.canWrite()) {
                System.err.println("WARNING: " + directoryDescription + " is not writable!");
            }
        }
    }
}
