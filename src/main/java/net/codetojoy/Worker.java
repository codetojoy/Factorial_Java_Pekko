package net.codetojoy;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.PostStop;
import org.apache.pekko.actor.typed.javadsl.*;
import org.apache.pekko.actor.typed.ActorRef;

public class Worker extends AbstractBehavior<Worker.Command> {
    public static Behavior<Worker.Command> create() {
        return Behaviors.setup(Worker::new);
    }

    private Worker(ActorContext<Worker.Command> context) {
        super(context);
    }

    public interface Command {}

    public static final class ProcessRangeCommand implements Command {
        final Range range;
        final ActorRef<Calculator.Command> calculator;
        final ActorRef<Reporter.Event> reporter;

        public ProcessRangeCommand(Range range, ActorRef<Calculator.Command> calculator, ActorRef<Reporter.Event> reporter) {
            this.range = range;
            this.calculator = calculator;
            this.reporter = reporter;
        }
    }

    @Override
    public Receive<Worker.Command> createReceive() {
        return newReceiveBuilder()
                   .onMessage(ProcessRangeCommand.class, this::onProcessRangeCommand)
                   .onSignal(PostStop.class, signal -> onPostStop())
                   .build();
    }

    private Behavior<Worker.Command> onProcessRangeCommand(ProcessRangeCommand processRangeCommand) {

        var range = processRangeCommand.range;

        for (int a = range.low; a <= range.high; a++) {
            for (int b = range.low; b <= range.high; b++) {
                for (int c = range.low; c <= range.high; c++) {
                    var calcCommand = new Calculator.CalcCommand(a, b, c, processRangeCommand.reporter);
                    processRangeCommand.calculator.tell(calcCommand);
                }
            }
        }

        getContext().getLog().info("TRACER worker ({},{}) DONE", range.low, range.high);

        return this;
    }

    private Behavior<Command> onPostStop() {
        getContext().getLog().info("TRACER worker STOPPED");
        return Behaviors.stopped();
    }
}
