package task;

/**
 * The Event class encapsulates a type of task to be done.
 * A Deadline task is a task that starts at a specific time and ends at a specific time.
 */
public class Event extends Task{
    protected String at;

    public Event(String description, String at) {
        super(description, TaskType.EVENT);
        this.at = at;
    }

    /**
     * Returns a string representation of the Event task.
     *
     * @return A string representation of the task.
     */
    @Override
    public String toString() {
        return String.format("%s (at: %s)", super.toString(), this.at);
    }

    /**
     * Returns the formatted Event task details to be stored in text file.
     *
     * @return Formatted Event task details to be stored in text file
     */
    @Override
    public String toFileString() {
        return super.toFileString() + " | " + this.at ;
    }
}
