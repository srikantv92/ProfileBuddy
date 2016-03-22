package edu.uta.se.profilebuddy.exception;

/**
 * Custom exception which extends {@link Exception}
 *
 */
public class NoDataException extends Exception
{

	private static final long serialVersionUID = 1L;

	public NoDataException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public NoDataException(String detailMessage)
	{
		super(detailMessage);
	}
}
