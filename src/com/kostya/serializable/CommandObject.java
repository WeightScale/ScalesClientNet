package com.kostya.serializable;


import android.content.Context;

import java.io.Serializable;

/**
 * Created by Kostya on 10.07.2016.
 */
public class CommandObject implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private Commands commandName;
    String value = "";
    public CommandObject(Commands name, String value){
        this.commandName = name;
        this.value = value;
    }

    public CommandObject(Commands name){
        this.commandName = name;
    }

    public void execute(Context context){
        if (value.isEmpty())
            commandName.fetch(context);
        else
            commandName.setup(context, value);
    }

    public Commands getCommandName() {
        return commandName;
    }

    public void appendValue(String v){
        value = v;
    }

    /**
     * Always treat de-serialization as a full-blown constructor, by
     * validating the final state of the de-serialized object.
     */
    /*private void readObject( ObjectInputStream aInputStream ) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();

        //make defensive copy of the mutable Date field
        //fDateOpened = new Date(fDateOpened.getTime());

        //ensure that object state has not been corrupted or tampered with maliciously
        //validateState();
    }*/

    /**
     * This is the default implementation of writeObject.
     * Customise if necessary.
     */
    /*private void writeObject( ObjectOutputStream aOutputStream ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }*/
}