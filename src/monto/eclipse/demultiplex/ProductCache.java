package monto.eclipse.demultiplex;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import monto.eclipse.Activator;

/**
 * Awaits all products of a given services.
 * 
 * The class mediates between a thread that polls the connection and the UI thread that calls
 * {@link #getProduct() getProduct}.
 */
public class ProductCache<I, P> {
  protected Lock lock;
  protected Condition arrived;
  protected P product;
  protected Function<I, P> messageDeserializer;
  protected long timeout;
  protected Fetch state;
  protected String logProductTag;

  public ProductCache(String logProductTag, Function<I, P> messageDeserializer) {
    this.lock = new ReentrantLock();
    this.arrived = lock.newCondition();
    this.timeout = 100;
    this.product = null;
    this.messageDeserializer = messageDeserializer;
    this.state = Fetch.PENDING;
    this.logProductTag = logProductTag;
  }

  public void invalidateProduct() {
    withLock(() -> {
      this.product = null;
      this.state = Fetch.PENDING;
      arrived.signalAll();
    });
  }

  public void onProductMessage(I incoming) {
    P newProduct = messageDeserializer.apply(incoming);

    withLock(() -> {
      if ((state == Fetch.PENDING || state == Fetch.WAITING)) {
        this.product = newProduct;
        this.state = Fetch.ARRIVED;
        arrived.signalAll();
      }
    });
  }

  public Optional<P> getProduct() {
    if (state == Fetch.PENDING) {
      lock.lock();
      try {

        this.state = Fetch.WAITING;

        arrived.await(timeout, TimeUnit.MILLISECONDS);

        if (state != Fetch.PENDING && state != Fetch.ARRIVED)
          this.state = Fetch.LOST;

      } catch (InterruptedException e) {
        Activator.error("service got interupted: %s", e);
      } finally {
        lock.unlock();
      }
    }

    Activator.debug("%s: <%s> getProduct() -> %s", state, logProductTag, product);

    return Optional.ofNullable(product);
  }


  protected void withLock(Runnable runnable) {
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  protected enum Fetch {
    /**
     * Represents a state, where a new product is requested, that is not yet available and no one is
     * waiting on it.
     */
    PENDING,

    /**
     * Represents a state, where a new product is requested, that is not yet available and
     * getProduct got called.
     */
    WAITING,

    /**
     * Represents a state, where a new product has arrived, that is not invalidated yet.
     */
    ARRIVED,

    /**
     * Represents a state, where a new product has been requested, but the deadline was missed.
     */
    LOST;
  }
}
