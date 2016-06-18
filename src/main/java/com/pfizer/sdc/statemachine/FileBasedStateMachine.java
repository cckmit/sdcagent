package com.pfizer.sdc.statemachine;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by LALAPM on 6/16/2016.
 */
class FileBasedStateMachine implements StateMachine {

    private static final char DELIMITER = ',';
    private final String mFile;
    private final Lock lock = new ReentrantLock();

    FileBasedStateMachine() {
        //TODO read config files to get the state machine file location
        this.mFile = "C:\\tools\\test\\statemachine.csv";
    }

    @Override
    public boolean update(FileStateData pFileStateData) {
        Boolean myLock = false;
        try {
            myLock = lock.tryLock();
            if(!myLock)
                return false;

            System.out.println("GOT LOCK WRITING "+pFileStateData.getFileName()+" "+pFileStateData.getStatus().name());
            FileWriter fileWriter = new FileWriter(this.mFile,true);

            CSVWriter csvWriter = new CSVWriter(fileWriter, DELIMITER, CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.NO_ESCAPE_CHARACTER, "\n");

            String[] nextLine = new String[4];
            nextLine[0] = pFileStateData.getFileName();
            nextLine[1] = pFileStateData.getFilePath();
            nextLine[2] = pFileStateData.getStatus().toString();
            nextLine[3] = pFileStateData.getDate().toString();

            csvWriter.writeNext(nextLine);
            csvWriter.close();
            return true;
        } catch (IOException e) {
            //TODO Log exception
            e.printStackTrace();
            return false;
        } finally {
            if(myLock)
                lock.unlock();
        }
    }

    @Override
    public boolean shutdown() {
        return true;
    }
}
