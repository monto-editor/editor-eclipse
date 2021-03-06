package monto.eclipse.launching.debug;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import monto.eclipse.Activator;
import monto.service.command.CommandMessage;
import monto.service.command.Commands;
import monto.service.gson.GsonMonto;
import monto.service.launching.debug.StepRequest;
import monto.service.launching.debug.StepRequest.StepRange;
import monto.service.launching.debug.Thread;

public class MontoThread extends MontoDebugElement implements IThread {

  private final Thread thread;
  private MontoStackFrame[] stackFrames;
  private MontoLineBreakpoint[] suspendingBreakpoints;

  public MontoThread(MontoDebugTarget debugTarget, Thread thread) {
    super(debugTarget);
    this.thread = thread;
  }

  void _setStackFrames(MontoStackFrame[] stackFrames) {
    this.stackFrames = stackFrames;
  }

  void _setSuspendingBreakpoints(MontoLineBreakpoint[] suspendingBreakpoints) {
    this.suspendingBreakpoints = suspendingBreakpoints;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((thread == null) ? 0 : thread.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MontoThread other = (MontoThread) obj;
    if (thread == null) {
      if (other.thread != null)
        return false;
    } else if (!thread.equals(other.thread))
      return false;
    return true;
  }



  /* MODEL METHODS */

  @Override
  public IStackFrame[] getStackFrames() throws DebugException {
    return stackFrames;
  }

  @Override
  public boolean hasStackFrames() throws DebugException {
    return stackFrames.length > 0;
  }

  @Override
  public int getPriority() throws DebugException {
    return 0;
  }

  @Override
  public IStackFrame getTopStackFrame() throws DebugException {
    if (stackFrames.length > 0) {
      return stackFrames[0];
    }
    return null;
  }

  @Override
  public String getName() throws DebugException {
    return String.format("MontoThread %d [%s]", thread.getId(), thread.getName());
  }

  @Override
  public IBreakpoint[] getBreakpoints() {
    return suspendingBreakpoints;
  }



  /* DEBUG TARGET DELEGATE METHODS */

  @Override
  public boolean canResume() {
    return getDebugTarget().canResume();
  }

  @Override
  public boolean canSuspend() {
    return getDebugTarget().canSuspend();
  }

  @Override
  public boolean isSuspended() {
    return getDebugTarget().isSuspended();
  }

  @Override
  public void resume() throws DebugException {
    getDebugTarget().resume();
  }

  @Override
  public void suspend() throws DebugException {
    getDebugTarget().suspend();
  }

  @Override
  public boolean canTerminate() {
    return getDebugTarget().canTerminate();
  }

  @Override
  public boolean isTerminated() {
    return getDebugTarget().isTerminated();
  }

  @Override
  public void terminate() throws DebugException {
    getDebugTarget().terminate();
  }

  @Override
  public boolean canStepInto() {
    return getDebugTarget().isSuspended();
  }

  @Override
  public boolean canStepOver() {
    return getDebugTarget().isSuspended();
  }

  @Override
  public boolean canStepReturn() {
    return getDebugTarget().isSuspended();
  }

  @Override
  public boolean isStepping() {
    return getDebugTarget().isSuspended() || getDebugTarget().isTerminated();
  }



  /* STEPPING METHODS */

  @Override
  public void stepInto() throws DebugException {
    System.out.println("MontoThread.stepInto()");
    Activator.sendCommandMessage(new CommandMessage(getDebugTarget().getSessionId(), 0,
        Commands.DEBUG_STEP, getDebugTarget().getLanguage(),
        GsonMonto.toJsonTree(new StepRequest(thread, StepRange.INTO))));

    fireResumeEvent(DebugEvent.STEP_INTO);
  }

  @Override
  public void stepOver() throws DebugException {
    System.out.println("MontoThread.stepOver()");
    Activator.sendCommandMessage(new CommandMessage(getDebugTarget().getSessionId(), 0,
        Commands.DEBUG_STEP, getDebugTarget().getLanguage(),
        GsonMonto.toJsonTree(new StepRequest(thread, StepRange.OVER))));

    fireResumeEvent(DebugEvent.STEP_OVER);
  }

  @Override
  public void stepReturn() throws DebugException {
    System.out.println("MontoThread.stepReturn()");
    Activator.sendCommandMessage(new CommandMessage(getDebugTarget().getSessionId(), 0,
        Commands.DEBUG_STEP, getDebugTarget().getLanguage(),
        GsonMonto.toJsonTree(new StepRequest(thread, StepRange.OUT))));

    fireResumeEvent(DebugEvent.STEP_RETURN);
  }

}
