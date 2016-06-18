package com.pfizer.sdc.statemachine;

/**
 * Created by LALAPM on 6/16/2016.
 */
public interface StateMachine {
    boolean update(FileStateData pFileStateData);

    boolean shutdown();
}
