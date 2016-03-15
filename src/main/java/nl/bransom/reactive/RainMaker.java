package nl.bransom.reactive;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Random;

public class RainMaker extends AbstractVerticle implements RainConstants {

  private static final Logger LOG = LoggerFactory.getLogger(RainMaker.class);

  @Override
  public void start() {
    vertx.eventBus()
        .<JsonObject>consumer(RAIN_MAKER_ADDRESS)
        .toObservable()
        .map(Message::body)
        .map(jsonObject -> jsonObject.getDouble(INTENSITY_KEY))
        .map(this::intensityToIntervalMillis)
        .switchMap(this::createRainDrops)
        .subscribe(
            rainDrop -> vertx.eventBus().publish(RAIN_DROP_ADDRESS, rainDrop.toJson()),
            throwable -> LOG.error("Error making rain.", throwable));
  }

  private long intensityToIntervalMillis(final double intensity) {
    final double effectiveIntensity = Math.min(Math.max(0.0, intensity), 1.0);
    LOG.debug("intensity: {}", effectiveIntensity);
    return Math.round(Math.pow(Math.E, Math.log(MAX_INTERVAL_MILLIS) * (1.0 - effectiveIntensity)));
  }

  private Observable<? extends RainDrop> createRainDrops(final long intervalMillis) {
    LOG.debug("intervalMillis: {}", intervalMillis);
    if (intervalMillis < MAX_INTERVAL_MILLIS) {
      return Observable.<RainDrop>create(subscriber -> createDelayedRainDrop(intervalMillis, subscriber));
    } else {
      return Observable.empty();
    }
  }

  private void createDelayedRainDrop(final long intervalMillis, final Subscriber<? super RainDrop> subscriber) {
    final long delayMillis = sampleDelayMillis(intervalMillis);
    vertx.setTimer(delayMillis, timerId -> {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new RainDrop());
        createDelayedRainDrop(intervalMillis, subscriber);
      }
    });
  }

  private long sampleDelayMillis(final long intervalMillis) {
    final Random random = new Random();
    return Math.max(1, Math.round(2.0 * random.nextDouble() * intervalMillis));
  }
}
