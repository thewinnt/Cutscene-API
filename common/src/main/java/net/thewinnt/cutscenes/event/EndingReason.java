package net.thewinnt.cutscenes.event;

public enum EndingReason {
    /** A cutscene has finished naturally */
    FINISH,

    /** A cutscene has been interrupted by another cutscene or the player logging out */
    INTERRUPT,

    /** A cutscene has been stopped by command */
    COMMAND,

    /** A cutscene ended with an error */
    ERROR
}
