package com.spro.bigfilesorter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SorterTest {

    private Random random = new Random();
    private static final int NUMBER_OF_TEST_VALUES = 1000000;
    private static final int MAXIMUM_USING_SPACE_MB = 1;
    private static final String TEST_FILE_NAME = "testFile";
    private static final String SORTED_FILE_SUFFIX = "_sorted";


    @Before
    public void createTestFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(TEST_FILE_NAME))) {
            for (int i = 0; i < NUMBER_OF_TEST_VALUES; i++) {
                bufferedWriter.write(Integer.toString(random.nextInt()));
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't create test file.", e);
        }
    }


    @Test
    public void testSorting() {
        long startTime = System.currentTimeMillis();

        Sorter.main(new String[]{TEST_FILE_NAME, String.valueOf(MAXIMUM_USING_SPACE_MB)});

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        System.out.println("Duration im millis: " + durationMillis);

        File sourceFile = new File(TEST_FILE_NAME);
        File resultFile = new File(TEST_FILE_NAME + SORTED_FILE_SUFFIX);

        Assert.assertEquals(sourceFile.getTotalSpace(), resultFile.getTotalSpace());

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(TEST_FILE_NAME + SORTED_FILE_SUFFIX))) {
            int previousValue = Integer.valueOf(bufferedReader.readLine());

            for (int i = 0; i < NUMBER_OF_TEST_VALUES - 1; i++) {
                int currentValue = Integer.valueOf(bufferedReader.readLine());

                Assert.assertTrue(currentValue >= previousValue);
                previousValue = currentValue;
            }

        } catch (IOException e) {
            throw new RuntimeException("Test failed.", e);
        }
    }

    @After
    public void cleanup() {
        File file = new File(TEST_FILE_NAME);
        file.delete();

        file = new File(TEST_FILE_NAME + SORTED_FILE_SUFFIX);
        file.delete();
    }
}
