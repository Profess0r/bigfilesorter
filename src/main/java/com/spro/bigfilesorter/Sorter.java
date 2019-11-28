package com.spro.bigfilesorter;

public class Sorter {

    public static void main(String[] args) {

        String fileName = args[0];

        int maximumUsingSpaceMb = 100; // can be corrected
        if (args.length > 1) {
            maximumUsingSpaceMb = Integer.parseInt(args[1]);
        }

        FileSorter fileSorter = new FileSorter(maximumUsingSpaceMb);
        fileSorter.sortIntegerValuesFile(fileName);

    }
}
