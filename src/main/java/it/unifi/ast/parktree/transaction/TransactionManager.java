package it.unifi.ast.parktree.transaction;

public interface TransactionManager {

	<T> T doInTransaction(TransactionCode<T> code);

}
