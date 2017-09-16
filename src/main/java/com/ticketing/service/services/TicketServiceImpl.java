package com.ticketing.service.services;

import static com.ticketing.service.model.Venue.seatHoldMap;
import static com.ticketing.service.model.Venue.venueTickets;

import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Logger;

import com.ticketing.service.model.Row;
import com.ticketing.service.model.Seat;
import com.ticketing.service.model.SeatHold;
import com.ticketing.service.model.Status;

public class TicketServiceImpl implements TicketService {

	private static final Logger log = Logger.getLogger(TicketServiceImpl.class.getName());
	private static int VENUE_HALL_COLUMNS = 4;

	private static int VENUE_HALL_ROWS = 4;

	private Long ON_HOLD_TIME_OUT_MILLISECS = 5000L;

	private static final int RANDOM = 98765;

	public TicketServiceImpl() {
		initializeVenueTickets(VENUE_HALL_ROWS, VENUE_HALL_COLUMNS);
	}

	public TicketServiceImpl(int rows, int cols, Long onHoldTimeOut) {
		initializeVenueTickets(rows, cols);
		VENUE_HALL_ROWS = rows;
		VENUE_HALL_COLUMNS = cols;
		ON_HOLD_TIME_OUT_MILLISECS = onHoldTimeOut;
	}

	@Override
	public synchronized int numSeatsAvailable() {
		int counter = 0;

		// loop over the rows and add up the available seats at each row
		for (Row row : venueTickets) {
			counter += row.getAvailableSeatCount();
		}
		return counter;
	}

	public synchronized void printVenueStatus() {

		System.out.println("--------");
		for (Row row : venueTickets) {
			for (Seat seat : row.getSeats()) {
				System.out.print(seat.getStatus().toString().substring(0, 1) + " ");
			}
			System.out.println();
		}
		System.out.println("--------");

	}

	@Override
	public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		SeatHold seatHold = new SeatHold();
		if (numSeats > numSeatsAvailable()) {
			seatHold.setError(Boolean.TRUE);
			seatHold.setErrorMessage("Sorry, we do not have " + numSeats + " tickets available.");

			return seatHold;
		}

		boolean isHeld = Boolean.FALSE;
		for (Row row : venueTickets) {
			// avoid looping through the row if the row is already at capacity
			if (row.getAvailableSeatCount() != 0) {
				isHeld = holdSeats(numSeats, customerEmail, seatHold, row);

				if (isHeld) {
					setNewRowCount(numSeats, row);
					break;
				}
			}
		}
		if (!isHeld) {
			seatHold.setError(Boolean.TRUE);
			seatHold.setErrorMessage("Sorry, " + numSeats + " consecutive seats are not available in the venue.");
		}

		//SeatHoldTimerTask executes after the timeout for hold has been reached
		Timer timer = new Timer();
		timer.schedule(new SeatHoldTimerTask(customerEmail), ON_HOLD_TIME_OUT_MILLISECS);

		return seatHold;
	}

	@Override
	public synchronized String reserveSeats(int seatHoldId, String customerEmail) {
		String message = "Sorry your seats could not be reserved.";
		SeatHold seatHold = seatHoldMap.get(seatHoldId);

		if (seatHold != null) {
			for (Seat seat : seatHold.getSeats()) {
				Seat reservedSeat = venueTickets.get(seatHold.getRowId()).getSeats().get(seat.getSeatId());
				updateSeatStatusAndEmail(customerEmail, reservedSeat, Status.RESERVED);

				message = "Hurray! Your seats have been reserved. Congratulations!";
			}

			//Remove this entry from the seatHoldMap as the seats have been reserved
			seatHoldMap.remove(seatHoldId);
		}

		return message;
	}

	/**
	 * Initialize the venueTickets collection which is a list of rows that
	 * contains rowId and a list of Seat objects
	 * 
	 * Seat has an ID and status among other things
	 * 
	 * Initialize the collection so that we have rows number of "rows" and
	 * "cols" number of seats in each row with status of each Seat as AVAILABLE
	 * and emailId as null
	 * 
	 * 
	 * @param rows
	 * @param cols
	 */
	private synchronized void initializeVenueTickets(int rows, int cols) {

		venueTickets = new ArrayList<>();

		for (int i = 0; i < rows; i++) {
			ArrayList<Seat> seats = new ArrayList<>();
			for (int j = 0; j < cols; j++) {
				Seat seat = makeNewAvailableSeat(j);
				seats.add(seat);
			}
			Row row = makeNewRow(cols, i, seats);
			venueTickets.add(row);
		}
	}

	/**
	 * This makes a new row
	 * 
	 * @param cols
	 * @param rowId
	 * @param seats
	 * @return
	 */
	private Row makeNewRow(int cols, int rowId, ArrayList<Seat> seats) {
		Row row = new Row();
		row.setRowId(rowId);
		row.setAvailableSeatCount(cols);
		row.setSeats(seats);
		return row;
	}

	/**
	 * This makes a new AVAILABLE Seat
	 * 
	 * @param seatId
	 * @return
	 */
	private Seat makeNewAvailableSeat(int seatId) {
		Seat seat = new Seat();
		seat.setSeatId(seatId);
		seat.setStatus(Status.AVAILABLE);
		return seat;
	}

	/**
	 * Update the row count after Seats in the row are marked ON_HOLD
	 * 
	 * @param numSeats
	 * @param row
	 */
	private void setNewRowCount(int numSeats, Row row) {
		int currentCount = row.getAvailableSeatCount();
		int newCount = currentCount - numSeats;
		row.setAvailableSeatCount(newCount);
	}

	/**
	 * If Seats can be marked ON_HOLD for this row, do so else continue to next
	 * row
	 * 
	 * @param numSeats
	 * @param customerEmail
	 * @param seatHold
	 * @param row
	 */
	private synchronized boolean holdSeats(int numSeats, String customerEmail, SeatHold seatHold, Row row) {
		boolean isHold = Boolean.FALSE;
		for (Seat seat : row.getSeats()) {
			// Find the first available seat and check if "numSeats" consecutive
			// seats are available in this row
			if (seat.getStatus().equals(Status.AVAILABLE) && seat.getSeatId() + numSeats <= VENUE_HALL_COLUMNS) {
				if (canSeatsBeMarkedOnHold(numSeats, row, seat.getSeatId())) {
					populateSeatHoldMap(numSeats, customerEmail, seatHold, row, seat);
					isHold = Boolean.TRUE;
					break;
				}
			}
		}

		return isHold;
	}

	/**
	 * If Seats can be marked on hold, update their Status and emailId and add
	 * them to the SeatHold object and add the SeatHold into the seatHoldMap
	 * 
	 * @param numSeats
	 * @param customerEmail
	 * @param seatHold
	 * @param row
	 * @param seat
	 */
	private synchronized void populateSeatHoldMap(int numSeats, String customerEmail, SeatHold seatHold, Row row,
			Seat seat) {

		seatHold.setTotalSeats(numSeats);
		seatHold.setRowId(row.getRowId());
		seatHold.setSeatHoldId(Integer.parseInt(createUniqueSeatHoldId(row, seat)));

		updateSeatHold(numSeats, row, seat, seatHold, customerEmail);

		seatHoldMap.put(seatHold.getSeatHoldId(), seatHold);
	}

	/**
	 * Making a unique seatHoldId For Eg for a seat hold that starts at 4th row
	 * 5th seat, the ID will be: 49876545
	 * 
	 * @param row
	 * @param seat
	 * @return seatHoldId
	 */
	private String createUniqueSeatHoldId(Row row, Seat seat) {
		return Integer.toString(row.getRowId()) + RANDOM + row.getRowId() + seat.getSeatId();
	}

	/**
	 * Mark the seats to ON_HOLD and add them to the SeatHold object
	 * 
	 * 
	 * @param totalSeats
	 * @param row
	 * @param seat
	 * @param seatHold
	 * @param emailId
	 */
	private synchronized void updateSeatHold(int totalSeats, Row row, Seat seat, SeatHold seatHold, String emailId) {
		ArrayList<Seat> seats = new ArrayList<>();

		// loop over all seats to be marked onHold
		for (int i = seat.getSeatId(); i < seat.getSeatId() + totalSeats; i++) {
			Seat seatToBeHeld = row.getSeats().get(i);
			updateSeatStatusAndEmail(emailId, seatToBeHeld, Status.ON_HOLD);

			seats.add(seatToBeHeld);
		}
		seatHold.setSeats(seats);
	}

	private void updateSeatStatusAndEmail(String emailId, Seat seatToBeHeld, Status status) {
		seatToBeHeld.setStatus(status);
		seatToBeHeld.setEmailId(emailId);
	}

	/**
	 * This method checks to see if or not "totalSeats" consecutive seats are
	 * available in this row to be held
	 * 
	 * @param totalSeats
	 * @param row
	 * @param seat
	 * @return
	 */
	private synchronized boolean canSeatsBeMarkedOnHold(int totalSeats, Row row, int seatId) {
		for (int i = seatId; i < seatId + totalSeats; i++) {
			if (!row.getSeats().get(i).getStatus().equals(Status.AVAILABLE)) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
}
