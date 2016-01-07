/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autonomouspathplanner.GUI;

/**
 * A class representing exceptions caused when something happens and the program doesn't know what class this is
 * @author Alex
 */
public class UnknownClassException extends Exception {

    /**
     * Creates a new instance of <code>UnknownClassException</code> without
     * detail message.
     */
    public UnknownClassException() {
    }

    /**
     * Constructs an instance of <code>UnknownClassException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownClassException(String msg) {
        super(msg);
    }
}
