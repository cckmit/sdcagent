package com.pfizer.sdc.filewriter;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class FileWriter {
    private final String mFileToWrite;
    private final String mSDCPath;
    private String mFileName;

    public FileWriter(String pFile, String pSDCPath) {
        this.mFileToWrite = pFile;
        this.mSDCPath = pSDCPath;
    }

    public void writeFile() {
        int noOfRetry = 3;
        do{
            if(canFileBeRead())
                noOfRetry = 0;
            else {
                noOfRetry--;
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();;
                    //TODO
                }
            }
        } while(noOfRetry > 0);

        byte[] bytesToWrite = readLocalFile();

        boolean success = writeToSDC(bytesToWrite);

        System.out.println("Written to SDC "+success);
    }

    private boolean writeToSDC(byte[] bytesToWrite) {
        Configuration conf = new Configuration();
        try {
            FileSystem fs = FileSystem.get(conf);
            Path writePath = new Path(this.mSDCPath+"/"+this.mFileName);
            FSDataOutputStream ostr = fs.create(writePath,
                    true, // overwrite
                    512 // buffersize
            );
            ostr.write(bytesToWrite);
            ostr.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] readLocalFile() {
        try {
            File file = new File(this.mFileToWrite);
            this.mFileName = file.getName();
            return IOUtils.toByteArray(new FileInputStream(file));
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
            System.out.println("Cannot read file to byte[]");
        }
        return null;
    }

    private boolean canFileBeRead() {
        File file = new File(this.mFileToWrite);
        RandomAccessFile stream = null;
        try {
            stream = new RandomAccessFile(file, "rw");
            return true;
        } catch (Exception e) {
            //cannot open as someone else is still writing
            //TODO
            System.out.println("Cannot open as someone else is still writing");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static void main(String args[]) {
        (new FileWriter(args[0], args[1])).writeFile();
    }
}
