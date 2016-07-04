package org.goldenroute.cq.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {
    public static boolean tryParse(String text, AtomicReference<Integer> value) {
        try {
            value.set(Integer.parseInt(text));
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static List<String> getRemovableStorageDirectoryPaths() {
        String line;
        List<String> paths = new ArrayList<>();
        String pattern = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";

        try {
            Process process = new ProcessBuilder()
                    .command("mount")
                    .redirectErrorStream(true)
                    .start();
            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase(Locale.US).contains("asec")) {
                    continue;
                }
                if (line.matches(pattern)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                paths.add(part);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }
}
