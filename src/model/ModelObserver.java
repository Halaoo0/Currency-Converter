package model;

/** Observer interface: anything that wants to be told when the model changes. */
public interface ModelObserver {
    /** Called by the model after every change of its state. */
    void modelChanged();
}
