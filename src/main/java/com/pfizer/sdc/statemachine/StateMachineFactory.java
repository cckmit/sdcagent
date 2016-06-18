package com.pfizer.sdc.statemachine;

/**
 * Created by LALAPM on 6/16/2016.
 */
public class StateMachineFactory {
    private static StateMachine stateMachine;

    public enum StateMachineType {
        FILE, DB
    }

    public static StateMachine newInstance(StateMachineType type) {
        //NO DB implementation is supported so returning the file implementation itself
        //At a given time only one type of State Machine is supported
        if (stateMachine == null)
            stateMachine = new FileBasedStateMachine();
        return stateMachine;
    }

    public static StateMachine getInstance() {
        //Default implementation is file
        if (stateMachine == null)
            stateMachine = newInstance(StateMachineType.FILE);
        return stateMachine;
    }
}
