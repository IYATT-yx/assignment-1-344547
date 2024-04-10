package modules;

/**
 * Implementing the Lamport logical clock algorithm
 */
public class LamportClock
{
    private int current_time; // The logical clock value of the current process.

    /**
     * initialization
     */
    public LamportClock()
    {
        this.current_time = 0;
    }

    /**
     * The first rule in the Lamport logical clock algorithm.
     * @return Returns a result of current_time + 1 before each local event occurs.
     */
    public synchronized int get_and_add_clock()
    {
        return ++this.current_time;
    }

    /**
     * The second rule in the Lamport logical clock algorithm.
     * When receiving a message, update the current_time to the larger value of new_time and current_time plus one.
     * @param new_time The logical clock value of the sender included in the received message.
     */
    public synchronized void compare_and_set(int new_time)
    {
        this.current_time = new_time > this.current_time ? new_time + 1 : this.current_time + 1;
    }

    /**
     * Used to return the logical clock value of the current process.
     * @return The logical clock value of the current process.
     */
    public synchronized int get_with_out_set()
    {
        return this.current_time;
    }
}
