import exception.*;
import task.Deadline;
import task.Event;
import task.Task;
import task.ToDo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Blob {
    // Divider to separate message instances by the chat-bot
    private static final String MESSAGE_DIVIDER = "=".repeat(100);
    // Header to signify start of a message by the chat-bot
    private static final String MESSAGE_HEADER = "\u001B[33m" + "Blob says: " + "\u001B[0m";

    // File path for text file which stores the data of all tasks
    private static final Path SAVED_TASKS_FILE_PATH = Paths.get("data","tasks.txt");

    private final ArrayList<Task> taskList = new ArrayList<>();

    /**
     * Prints a greeting message.
     */
    private void greet() {
        speak("Hello... me Blob...", "How can Blob help...?");
    }

    /**
     * Prints a parting message.
     */
    private void sayGoodbye() {
        this.speak("Thanks for talking to Blob...", "Blob see you soon...");
    }

    /**
     * Enumerates the list of added tasks.
     */
    private void listTasks() {
        StringBuilder tasksStringBuilder = new StringBuilder();
        tasksStringBuilder.append("\n");
        for (int i = 0; i < taskList.size(); i++) {
            tasksStringBuilder.append(String.format("\t\t%d. %s \n", i + 1, taskList.get(i).toString()));
        }
        speak(String.format("Blob remembers %d task(s)...", taskList.size()),
                taskList.size() == 0 ? "Give Blob task to remember...?" : tasksStringBuilder.toString());
    }

    /**
     * Adds a ToDo task to the list of tasks.
     */
    private void addTodo(String description) {
        ToDo task = new ToDo(description);
        taskList.add(task);
        speak("Blob will remember task...", String.format("\n\t\t%s \n", task),
                String.format("Blob now remembers %d task(s)...", taskList.size()));
    }

    /**
     * Adds a Deadline task to the list of tasks
     */
    private void addDeadline(String details) throws InvalidDeadlineException, InvalidDateFormatException {
        String[] deconstructedDetails = details.split("\\s+(/by)\\s+", 2);
        if (deconstructedDetails.length < 2) {
            throw new InvalidDeadlineException();
        }
        Deadline task = new Deadline(deconstructedDetails[0], deconstructedDetails[1]);
        taskList.add(task);
        speak("Blob will remember task...", String.format("\n\t\t%s \n", task),
                String.format("Blob now remembers %d task(s)...", taskList.size()));
    }

    /**
     * Adds an Event task to the list of tasks
     */
    private void addEvent(String details) throws InvalidEventException, InvalidDateFormatException {
        String[] deconstructedDetails = details.split("\\s+(/at)\\s+", 2);
        if (deconstructedDetails.length < 2) {
            throw new InvalidEventException();
        }
        Event task = new Event(deconstructedDetails[0], deconstructedDetails[1]);
        taskList.add(task);
        speak("Blob will remember task...", String.format("\n\t\t%s \n", task),
        String.format("Blob now remembers %d task(s)...", taskList.size()));
    }

    /**
     * Marks task at the index to be done.
     *
     * @param index The index of the task to be marked done
     */
    private void markTaskAtIndexDone(int index) throws InvalidTaskIndexException {
        try {
            Task task = taskList.get(index - 1);
            task.markAsDone();
            speak("Blob congratulates on task well done...", String.format("\n\t\t%s \n", task));
        } catch (IndexOutOfBoundsException exception) {
            throw new InvalidTaskIndexException();
        }
    }

    /**
     * Marks task at the index to be undone.
     *
     * @param index The index of the task to be marked undone
     */
    private void markTaskAtIndexUndone(int index) throws InvalidTaskIndexException {
        try {
            Task task = taskList.get(index - 1);
            task.markAsUndone();
            speak("Blob will mark as undone...", String.format("\n\t\t%s \n", task));
        } catch (IndexOutOfBoundsException exception) {
            throw new InvalidTaskIndexException();
        }
    }

    /**
     * Delete task at the index.
     *
     * @param index The index of the task to be deleted
     * @throws InvalidTaskIndexException
     */
    private void deleteTaskAtIndex(int index) throws InvalidTaskIndexException {
        try {
            Task task = taskList.get(index - 1);
            taskList.remove(index - 1);
            speak("Ok... Blob forget task...", String.format("\n\t\t%s \n", task));
        } catch (IndexOutOfBoundsException exception) {
            throw new InvalidTaskIndexException();
        }
    }

    /**
     * Prints a sequence of strings, each in an indented newline encapsulated in a message instance.
     *
     * @param content The sequence of strings to be printed
     */
    private void speak(String ...content) {
        System.out.println("\n" + MESSAGE_DIVIDER);
        System.out.println(MESSAGE_HEADER);
        for (int i = 0; i < content.length; i++) {
            System.out.println("\t" + content[i]);
        }
        System.out.println(MESSAGE_DIVIDER + "\n");
    }

    /**
     * Loads task based on data in text file
     */
    private void loadTasks() {
        try {
            Files.createDirectories(SAVED_TASKS_FILE_PATH.getParent());
            File taskFile = SAVED_TASKS_FILE_PATH.toFile();
            if (!taskFile.createNewFile()) {
                Scanner sc = new Scanner(new FileReader(taskFile));
                while (sc.hasNextLine()) {
                    String[] deconstructedDetails = sc.nextLine().trim().split("\\s+\\|\\s+");
                    String taskType = deconstructedDetails[0];
                    String done = deconstructedDetails[1];
                    String description = deconstructedDetails[2];
                    Task task = null;

                    if (Objects.equals(taskType, "T")) {
                        task = new ToDo(description);
                    } else if (Objects.equals(taskType, "D")) {
                        task = new Deadline(description, deconstructedDetails[3]);
                    } else if (Objects.equals(taskType, "E")) {
                        task = new Event(description, deconstructedDetails[3]);
                    }

                    if (Objects.equals(done, "1")) {
                        task.markAsDone();
                    }

                    taskList.add(task);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR!: " + e);
            System.exit(1);
        }
    }

    /**
     * Saves task to text file based on task list
     */
    private void saveTasks() {
        try {
            FileWriter taskFileWriter = new FileWriter(SAVED_TASKS_FILE_PATH.toFile());
            for (int i = 0; i < taskList.size(); i++) {
                taskFileWriter.write(taskList.get(i).toFileString());
                taskFileWriter.write(System.lineSeparator());
            }
            taskFileWriter.close();
        } catch(IOException e) {

        }
    }

    /**
     * Start the interaction with an instance of Blob
     */
    public void start() {
        this.loadTasks();
        this.greet();
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.print(">> ");
                String input = sc.nextLine().trim();
                String[] deconstructedInput = input.split("\\s+", 2);
                String command = deconstructedInput[0];
                switch (command) {
                case "bye":
                    end();
                case "list":
                    listTasks();
                    break;
                case "mark":
                    try {
                        int index = Integer.parseInt(deconstructedInput[1]);
                        markTaskAtIndexDone(index);
                    } catch (NumberFormatException exception) {
                        throw new InvalidTaskIndexException();
                    }
                    break;
                case "unmark":
                    try {
                        int index = Integer.parseInt(deconstructedInput[1]);
                        markTaskAtIndexUndone(index);
                    } catch (NumberFormatException exception) {
                        throw new InvalidTaskIndexException();
                    }
                    break;
                case "todo":
                    if (deconstructedInput.length < 2) {
                        throw new MissingTaskDescriptionException();
                    }
                    addTodo(deconstructedInput[1]);
                    break;
                case "deadline":
                    if (deconstructedInput.length < 2) {
                        throw new MissingTaskDescriptionException();
                    }
                    addDeadline(deconstructedInput[1]);
                    break;
                case "event":
                    if (deconstructedInput.length < 2) {
                        throw new MissingTaskDescriptionException();
                    }
                    addEvent(deconstructedInput[1]);
                    break;
                case "delete":
                    try {
                        int index = Integer.parseInt(deconstructedInput[1]);
                        deleteTaskAtIndex(index);
                    } catch (NumberFormatException exception) {
                        throw new InvalidTaskIndexException();
                    }
                    break;
                default:
                    throw new UnknownCommandException();
                }
            } catch (BlobException exception) {
                speak(exception.getBlobMessages());
            }
        }
    }

    public void end() {
        saveTasks();
        sayGoodbye();
        System.exit(0);
    }
}
