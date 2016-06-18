package com.pfizer.sdc.filewriter;

import com.pfizer.sdc.statemachine.FileStateData;
import com.pfizer.sdc.statemachine.StateMachineFactory;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class FileWriterThread implements Callable<Boolean> {

    private final String mFile;
    private final String mSDCPath;
    private final String mFileName;

    public FileWriterThread(String pFile, String pSDCPath) {
        this.mFile = pFile;
        this.mSDCPath = pSDCPath;
        File file = new File(pFile);
        this.mFileName = file.getName();
    }

    public Boolean call() throws Exception {
        updateStateMachine(FileStateData.Status.START_SEND);//update that starting to read

        sendFileToSDC(); //sending raw file

        updateStateMachine(FileStateData.Status.RAW_SENT); //Status that raw file is sent

        generateMetadata();

        //sendFileToSDC(null); //sending metadata to SDC

        //updateStateMachine(FileStateData.Status.METADATA_SENT); //Sttaus that the metadata is sent

        return Boolean.TRUE;
    }

    private void generateMetadata() {

    }

    private void sendFileToSDC() {
        FileWriter fw = new FileWriter(this.mFile,this.mSDCPath);
        fw.writeFile();
    }

    private void updateStateMachine(FileStateData.Status pStatus) {
        System.out.println("UPDATING STATE MACHINE");
        FileStateData fileStateData = new FileStateData(this.mFile, this.mFileName,pStatus,new Date());
        boolean success = StateMachineFactory.getInstance().update(fileStateData);
        if(!success) {
            //TODO This is a serious condition. We might have to do a retry here if we continue using file based state machine data
            System.err.println("Another thread is writin to the file so cannot write");
        }
    }
}
