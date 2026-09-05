package it.unifi.ast.parktree.transaction;

import java.util.function.Function;

@FunctionalInterface
public interface TransactionCode<T> extends Function<ParkTreeRepositories, T> {

}
