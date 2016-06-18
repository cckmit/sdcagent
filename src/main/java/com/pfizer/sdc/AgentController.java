package com.pfizer.sdc;

import com.pfizer.sdc.folderwatcher.FolderWatcherService;
import com.pfizer.sdc.statemachine.FileStateData;
import com.pfizer.sdc.statemachine.StateMachine;
import com.pfizer.sdc.statemachine.StateMachineFactory;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class AgentController {

    //TODO depending on the deep folder structure we might need to have multiple submit to folder watch thread pool
    //TODO following numbers will be plugged in from a configuration file
    //TODO do we want tpo use the cached thread pool? Some performance testing results can help us decide one way or the other
    static ExecutorService folderWatcherThreadPool = Executors.newFixedThreadPool(2);
    static ScheduledExecutorService fileWriterThreadPool = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) throws IOException, InterruptedException {

        //All the args will be externalized to a config file
        //arg[0] - the folder to start watching
        //arg[1] - the regex to match for the folder names
        //arg[2] - SDC volume where to create the files

        //Creating a desired instance of state machine to be used later
        StateMachineFactory.newInstance(StateMachineFactory.StateMachineType.FILE);

        //TODO handle exception gracefully
        Future folderWatchServiceFuture = folderWatcherThreadPool.submit(new FolderWatcherService(args[0],args[1],fileWriterThreadPool,args[2]));

        //TODO appropriately report out the status
        for(;;) {
            Thread.sleep(10000);
            System.out.println("Is folder watch alive "+!folderWatchServiceFuture.isDone());
        }
    }
}
