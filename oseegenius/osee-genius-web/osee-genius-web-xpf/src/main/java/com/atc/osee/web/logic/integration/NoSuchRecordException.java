package com.atc.osee.web.logic.integration;

/**
 * Each DAO method that has a "single" domain object as result type, 
 * must raise this exception in case no data is found.
 * 
 * NOTE: this is an "integration" specific exception so it mustn't be exposed to clients.
 * The business component (probably an EJB) that is catching this exception has to 
 * rethrow its own business specific exception to clients.
 * 
 * <pre>
 * ex.
 * 
 * public Library getLibraryById(int id) throws SQLException, NoSuchRecordException
 * {
 * 		...
 * 		if (resultset.next())
 * 		{
 * 			return new Library(...);
 * 		}
 * 		throw new NoSuchRecordException();
 * }
 * 
 * ...on EJB side
 * 
 * 
 * public Something doSomething() throws NoSuchLibraryException
 * {
 * 		LibraryDAO dao = ...
 * 
 * 		try 
 * 		{
 * 			Library library = dao.getLibraryId(...);
 * 		} catch(NoSuchRecordException exception)
 * 		{
 * 			throw new NoSuchLibraryException();
 * 		}
 * }
 * 
 * </pre>
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 *
 */
public class NoSuchRecordException extends Exception
{
	private static final long serialVersionUID = 4133931057354279237L;
}
