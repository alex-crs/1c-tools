package handlers;

import org.apache.log4j.Logger;

import java.io.File;

public class FileLengthCalculator {
    private static final Logger LOGGER = Logger.getLogger(FileLengthCalculator.class);

    public static long getOccupiedSpace(String path) {
        return calcFileLength(path);
    }

    public static String getOccupiedSpaceByUser(String path){
        return spaceToString(calcFileLength(path));
    }

    //определение размера папки с кэшем
    public static long calcFileLength(String sourcePath) {
        long totalSize = 0;
        File file;
        File sourceDirectory = new File(sourcePath);
        if (!sourceDirectory.isFile()) {
            String[] sourceDirectoryFilesList = sourceDirectory.list();
            if (sourceDirectoryFilesList != null && sourceDirectoryFilesList.length != 0) {
                for (String element : sourceDirectoryFilesList) {
                    String path = sourcePath + element;
                    file = new File(path);
                    if (file.isFile()) {
                        totalSize += file.length();
                    }
                    if (file.isDirectory()) {
                        totalSize += calcFileLength(path + File.separator);
                    }
                }
            }
        } else {
            totalSize += sourceDirectory.length();
        }
        return totalSize;
    }

    //форматирование выводимого размера папки с кэшем
    public static String spaceToString(float digit) {
        if (digit < 1000) {
            return String.format("%.0f byte", digit);
        } else if (digit < 1000000) {
            return String.format("%.0f kb", digit / 1000);
        } else if (digit < 1000000000) {
            return String.format("%.2f mb", digit / 1000000);
        } else if (digit < 1000000000000L) {
            return String.format("%.2f Gb", digit / 1000000000L);
        }
        return digit + "bytes";
    }
}
