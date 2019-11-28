package com.spro.bigfilesorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FileSorter {

    private static final Logger logger = LogManager.getLogger();

    private static final String CHUNK_FILE_NAME_BASE = "chunkFile";
    private static final String SORTED_FILE_SUFFIX = "_sorted";

    private int chunkArraySize;

    public FileSorter(int maximumUsingSpaceMb) {
        chunkArraySize = Math.min(maximumUsingSpaceMb * 1024 * 1024 / 12, 100 * 1024 * 1024 / 12); // set maximum using space (can be corrected)
    }

    void sortIntegerValuesFile(String fileName) {

        int chunkFilesCount = splitFileToSortedChunks(fileName);

        mergeChunkFiles(fileName, chunkFilesCount);

        deleteChunkFiles(chunkFilesCount);
    }


    private int splitFileToSortedChunks(String fileName) {
        logger.debug("Splitting file...");

        int[] chunk = new int[chunkArraySize];

        int counter = 0;
        int chunkFilesCount = 0;

        // read file line by line
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                chunk[counter] = Integer.valueOf(line);

                counter++;

                if (counter == chunkArraySize) {
                    Arrays.sort(chunk);
                    writeChunkArrayToFile(CHUNK_FILE_NAME_BASE + chunkFilesCount, chunk);

                    counter = 0;
                    chunkFilesCount++;
                }
            }

            // handle rest of file
            if (counter > 0) {
                chunk = Arrays.copyOf(chunk, counter);

                Arrays.sort(chunk);
                writeChunkArrayToFile(CHUNK_FILE_NAME_BASE + chunkFilesCount, chunk);
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't open file for sorting.", e);
        }

        logger.debug("Splitting finished.");

        return chunkFilesCount;
    }

    private void mergeChunkFiles(String fileName, int chunkFilesCount) {
        logger.debug("Merging chunk files...");

        // open chunk files
        List<FileIterator> readers = new ArrayList<>(chunkFilesCount);
        for (int i = 0; i <= chunkFilesCount; i++) {
            readers.add(new FileIterator(CHUNK_FILE_NAME_BASE + i));
        }

        // open output file
        try (BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(fileName + SORTED_FILE_SUFFIX))) {

            // merge
            while (!readers.isEmpty()) {
                int currentMinimumValue = Integer.MAX_VALUE;
                FileIterator replaceIterator = null;

                // get minimum value among top values
                for (FileIterator fileIterator : readers) {
                    if (fileIterator.peek() <= currentMinimumValue) {
                        currentMinimumValue = fileIterator.peek();
                        replaceIterator = fileIterator;
                    }
                }

                if (!replaceIterator.hasNext()) {
                    readers.remove(replaceIterator);
                    replaceIterator.close();
                }

                // write current minimum value to resulting file
                outputFileWriter.write(Integer.toString(currentMinimumValue));
                outputFileWriter.newLine();
            }

            outputFileWriter.flush();

            logger.debug("Merging finished.");
        } catch (IOException e) {
            throw new RuntimeException("Merging chunks to resulting file failed.", e);
        } finally {
            for (FileIterator fileIterator: readers) {
                fileIterator.close();
            }
        }
    }


    private void writeChunkArrayToFile(String filename, int[] array) {
        try (BufferedWriter chunkFileWriter = new BufferedWriter(new FileWriter(filename))) {
            for (int value : array) {
                chunkFileWriter.write(Integer.toString(value));
                chunkFileWriter.newLine();
            }
            chunkFileWriter.flush();
            logger.debug("Chunk file wrote: " + filename);
        } catch (IOException e) {
            throw new RuntimeException("Can't write chunk file.", e);
        }
    }

    private void deleteChunkFiles(int chunkFilesCount) {
        logger.debug("Deleting temporary files...");
        boolean deletionSuccessful = true;
        for (int i = 0; i <= chunkFilesCount; i++) {
            File file = new File(CHUNK_FILE_NAME_BASE + i);
            if (!file.delete()) {
                deletionSuccessful = false;
            }
        }

        if (deletionSuccessful) {
            logger.debug("Temporary files deleted.");
        } else {
            logger.error("Unable to delete some temporary files.");
        }
    }

}
