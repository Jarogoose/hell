package com.jarogoose.hell.performance.execute;

import com.jarogoose.hell.performance.persist.data.ConfigurationKey.Position;
import com.jarogoose.hell.performance.persist.data.ConfigurationKey.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MeasurementFactory {

  private final Random random = new Random();
  private final List<Integer> tokens;
  private final Type type;
  private final Position position;
  private final int size;
  private final int randomization;

  private MeasurementFactory(Type type, Position position, int size, int randomization) {
    this.type = type;
    this.position = position;
    this.size = size;
    this.randomization = randomization;

    switch (this.type) {
      case ARRAY_LIST:
        this.tokens = new ArrayList<>();
        return;
      case LINKED_LIST:
        this.tokens = new LinkedList<>();
        return;
    }

    final String errMessage = String.format("Type %s is not supported.", this.type);
    throw new InvalidInitializationException(errMessage);
  }

  public static Config config() {
    return new Config();
  }

  public long generation() {
    long time = Stopwatch.nanos(() -> generate(size, randomization));
    log.debug("Generated {} nodes of {} in time - {} ms", size, type, time);
    return time;
  }

  public long sorting() {
    long time = Stopwatch.nanos(() -> {
      Collections.sort(tokens);
      return tokens;
    });
    log.debug("Sort of {} in time - {}} ms", type, time);
    return time;
  }

  public long inserting() {
    final int index = index();
    final int value = random.nextInt();
    long time = Stopwatch.nanos(() -> {
      tokens.add(index, value);
      return tokens;
    });
    log.debug("Add to {} in time - {} nanos ({} ms)",
        type, time, Stopwatch.nanosToMillis(time));
    return time;
  }

  public long deleting() {
    final int index = index();
    long time = Stopwatch.nanos(() -> tokens.remove(index));
    log.debug("Remove from {} in time - {} nanos ({} ms)",
        type, time, Stopwatch.nanosToMillis(time));
    return time;
  }

  public long retrieving() {
    final int index = index();
    long time = Stopwatch.nanos(() -> tokens.get(index));
    log.debug("Get from {} in time - {} nanos ({} ms)",
        type, time, Stopwatch.nanosToMillis(time));
    return time;
  }

  private Collection<Integer> generate(int size, int randomization) {
    for (int i = 0; i < size; i++) {
      tokens.add(random.nextInt(randomization));
    }
    return tokens;
  }

  private int index() {
    switch (position) {
      case BEGINNING:
        return 0;
      case MIDDLE:
        return tokens.size() / 2;
      case END:
        return tokens.size() - 1;
    }
    final String errMessage = String.format("Position %s is not supported.", this.position);
    throw new InvalidInitializationException(errMessage);
  }

  public Position at() {
    return position;
  }

  @Override
  public String toString() {
    final String tab = "  - ";
    return "Factory configurations:" + System.lineSeparator()
        + tab + type + System.lineSeparator()
        + tab + position + System.lineSeparator()
        + tab + size + "s" + System.lineSeparator()
        + tab + randomization + "r" + System.lineSeparator();
  }

  public static class Config {
    private Type type;
    private Position position;

    public Config of(Type type) {
      this.type = type;
      return this;
    }

    public Config at(Position position) {
      this.position = position;
      return this;
    }

    public MeasurementFactory with(int size, int randomization) {
      return new MeasurementFactory(type, position, size, randomization);
    }
  }

  private static class InvalidInitializationException extends RuntimeException {

    public InvalidInitializationException(String message) {
      super(message);
    }
  }
}
