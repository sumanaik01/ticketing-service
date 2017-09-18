package com.ticketing.service.util;

import com.ticketing.service.services.TicketServiceImpl;

public class App {
	public static void main(String[] args) {

		//Collect the input from command line args
		String rowsStr = args[0];
		String colsStr = args[1];
		String holdTimeOutStr = args[2];

		int rows = Integer.parseInt(rowsStr);
		int cols = Integer.parseInt(colsStr);
		long holdTimeOut = Long.parseLong(holdTimeOutStr);

		//Initialize the service using the command line parameters
		//This is the main service that implements the ticket service functionality
		TicketServiceImpl impl = new TicketServiceImpl(rows, cols, holdTimeOut);

		PrintResult.initialState(impl);

		PrintResult.scenarioOverBooking(impl, rows, cols);

		PrintResult.scenarioConsecutiveSeatsUnavailable(impl, cols);

		PrintResult.scenarioHoldAndReserve(impl, cols);

		PrintResult.scenarioHoldButNotReserve(impl, cols, holdTimeOut);

		PrintResult.scenarioBookNextRow(impl,holdTimeOut);

		PrintResult.scenarioBookBestAvailable(impl, holdTimeOut);

		PrintResult.concurrencyScenario(impl, holdTimeOut);
		
		System.exit(0);
	}
}
