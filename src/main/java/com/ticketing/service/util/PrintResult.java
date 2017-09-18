package com.ticketing.service.util;

import com.ticketing.service.model.SeatHold;
import com.ticketing.service.services.TicketServiceImpl;

/**
 * This is an utility class to print the results of different scenarios being
 * performed by my test
 * 
 * System out statements are used instead of logs for a better display of
 * results
 * 
 * @author Suma
 *
 */
public class PrintResult {

	protected static void initialState(TicketServiceImpl impl) {
		System.out.println("*****************************************************");
		System.out.println("Seat States: A-Available, O-On hold, R-Reserved");
		System.out.println("*****************************************************");
		System.out.println("Just after initialization, the Venue Seat Matrix looks like this.");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioOverBooking(TicketServiceImpl impl, int rows, int cols) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: Hold more than the total number of seats available in the venue for user1");
		
		SeatHold sh1 = impl.findAndHoldSeats((rows * cols + 1), "user1@example.com");
		
		System.out.println("Error Message: " + sh1.getErrorMessage());
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioConsecutiveSeatsUnavailable(TicketServiceImpl impl, int rows) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: Hold more than the total number of seats available in a row for user2");
		
		SeatHold sh2 = impl.findAndHoldSeats(rows + 1, "user2@example.com");
		
		System.out.println("Error Message: " + sh2.getErrorMessage());
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioHoldAndReserve(TicketServiceImpl impl, int cols) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: Hold and Reserve " + (cols - 2) + " seats for user3");

		SeatHold sh = impl.findAndHoldSeats(cols - 2, "user3@example.com");
		String message = impl.reserveSeats(sh.getSeatHoldId(), "user3@example.com");
		System.out.println("Message to User: " + message);
		impl.printVenueStatus();

		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioHoldButNotReserve(TicketServiceImpl impl, int cols, long holdTimeOut) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: Hold " + (cols - 2) + " seats for user4 but do not Reserve. The held seats will go back "
				+ "to the available pool if not reserved within the timeout");

		SeatHold sh = impl.findAndHoldSeats(cols - 2, "user4@example.com");

		System.out.println("State of seats before timeout");
		impl.printVenueStatus();

		try {
			Thread.sleep(holdTimeOut+1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		String message = impl.reserveSeats(sh.getSeatHoldId(), "user4@example.com");
		System.out.println("Message to User: " + message);

		System.out.println("State of seats after timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());

		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioBookNextRow(TicketServiceImpl impl, long holdTimeOut) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: The seat look up always starts from better seats but if enough consecutive "
				+ "seats are not available in the front rows, seats in next row are booked.");
		System.out.println("Hold 1 seat for user5 but do not Reserve");
		
		impl.findAndHoldSeats(1, "user5@example.com");

		System.out.println("Hold and Reserve 1 seat for user6");
		SeatHold sh5 = impl.findAndHoldSeats(1, "user6@example.com");
		impl.reserveSeats(sh5.getSeatHoldId(), "user6@example.com");

		System.out.println("State of seats before timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());

		try {
			Thread.sleep(holdTimeOut + 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("State of seats after timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("Hold and reserve 2 seats for user7");
		SeatHold sh6 = impl.findAndHoldSeats(2, "user7@example.com");
		impl.reserveSeats(sh6.getSeatHoldId(), "user7@example.com");

		System.out.println("2 Seats get blocked for user7 in the next row");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void scenarioBookBestAvailable(TicketServiceImpl impl, long holdTimeOut) {
		System.out.println("*****************************************************");
		System.out.println("Use Case: The seat look up always starts from better seats and any "
				+ "available spots in the middle of other reserved seats get filled up");
		System.out.println("Hold 1 Seat for user8 but do not Reserve");

		impl.findAndHoldSeats(1, "user8@example.com");

		System.out.println("Hold and Reserve 1 seat for user9");

		SeatHold sh5 = impl.findAndHoldSeats(1, "user9@example.com");
		impl.reserveSeats(sh5.getSeatHoldId(), "user9@example.com");

		System.out.println("State of seats before timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());

		try {
			Thread.sleep(holdTimeOut + 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("State of seats after timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("Hold and reserve 1 seat for user10");

		SeatHold sh6 = impl.findAndHoldSeats(1, "user10@example.com");
		impl.reserveSeats(sh6.getSeatHoldId(), "user10@example.com");

		System.out.println("The previously held and released seat for user9 gets booked for user10");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

	protected static void concurrencyScenario(TicketServiceImpl impl, long holdTimeOut) {
		System.out.println("*****************************************************");
		System.out.println("Concurrency: 2 threads hold 2 and 3 seats each simulataneous");

		Runnable task1 = () -> {
			impl.findAndHoldSeats(2, "user11@example.com");
		};
		Runnable task2 = () -> {
			impl.findAndHoldSeats(3, "user12@example.com");
		};
		// start the thread
		Thread t1 = new Thread(task1);
		Thread t2 = new Thread(task2);

		t1.start();
		t2.start();

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("State of seats before timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("Two different set of seats are put on hold for user 11 and user 12 by each thread.");

		try {
			Thread.sleep(holdTimeOut + 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		System.out.println("State of seats after timeout");
		impl.printVenueStatus();
		System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
		System.out.println("The appropriate seats are released back to the pool by both threads.");
		
		System.out.println("*****************************************************");
		System.out.println("\n");
	}

}
