package com.pfizer.sdc.statemachine;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class FileStateData implements Serializable {
    private String filePath;
    private String fileName;
    private Status status;
    private Date date;

    public FileStateData(){
    }

    public FileStateData(String pFilePath, String pFileName, Status pStatus, Date pDate) {
        this.filePath = pFilePath;
        this.fileName = pFileName;
        status = pStatus;
        this.date = pDate;
    }

    public enum Status {
        START_SEND, RAW_SENT, METADATA_SENT, ACK_RECEIVED, DELETED
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
