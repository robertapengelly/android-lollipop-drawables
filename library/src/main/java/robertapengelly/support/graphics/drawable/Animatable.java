package robertapengelly.support.graphics.drawable;

/** Interface that drawables supporting animations should implement. */
public interface Animatable {

    /**
     * Indicates whether the animation is running.
     *
     * @return True if the animation is running, false otherwise.
     */
    boolean isRunning();
    
    /** Starts the drawable's animation. */
    void start();
    
    /** Stops the drawable's animation. */
    void stop();

}