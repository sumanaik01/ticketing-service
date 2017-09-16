package com.ticketing.service.services;

import static com.ticketing.service.model.Venue.seatHoldMap;
import static com.ticketing.service.model.Venue.venueTickets;

import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.ticketing.service.model.Seat;
import com.ticketing.service.model.SeatHold;
import com.ticketing.service.model.Status;

public class SeatHoldTimerTask extends TimerTask {
	

	private static final Logger log = Logger.getLogger(SeatHoldTimerTask.class.getName());
	private String emailId;

	public SeatHoldTimerTask(String emailId) {
		this.emailId = emailId;
	}

	@Override
	public void run() {
		synchronized (seatHoldMap) {
			if (seatHoldMap != null) {
				processOnHoldTimeOut(seatHoldMap);
			}
		}
	}

	/**
	 * Loop over the SeatHold map and match the email assigned to the seat with 
	 * the email assigned to this class.(this timer task is triggered every time we hold
	 * the seat for anyone, email id comparison ensures we are only resetting the appropriate
	 * seats assigned to the email)
	 * 
	 * If they match then, reset the seats back to available as the on hold timeout has been reached
	 * 
	 * @param seatHoldMap
	 */
	private void processOnHoldTimeOut(Map<Integer, SeatHold> seatHoldMap) {
		int rowId = 0;
		int counter = 0;
		int oldCount = 0;
		int holdMapKey = 0;
		String emailAssignedToSeat = null;

		for (Map.Entry<Integer, SeatHold> entry : seatHoldMap.entrySet()) {
			SeatHold seatHold = entry.getValue();
			holdMapKey = entry.getKey();

			rowId = seatHold.getRowId();
			for (Seat seat : seatHold.getSeats()) {
				if (seat.getStatus().equals(Status.ON_HOLD) && seat.getEmailId().equalsIgnoreCase(emailId)) {
					counter++;
					emailAssignedToSeat = seat.getEmailId();

					updateSeatOnHoldTimeOut(seatHold, seat);
				}
			}
		}

		// Doing this outside of for loop so it executes just once
		if (counter > 0) {
			handleTicketsOnHoldTimeOut(counter, emailAssignedToSeat, rowId, oldCount, holdMapKey);
		}
	}

	/**
	 * Update the available count on the row as these seats just became available 
	 * and remove this entry from the SeatHold map.
	 * 
	 * @param counter
	 * @param emailAssignedToSeat
	 * @param rowId
	 * @param oldCount
	 * @param holdMapKey
	 */
	private void handleTicketsOnHoldTimeOut(int counter, String emailAssignedToSeat, int rowId, int oldCount,
			int holdMapKey) {
		//I am just logging this message for now, but in real life, the user needs to be notified
		String errorMessage = "Can't hold " + counter + " seats any longer for " + emailAssignedToSeat;
		log.info(errorMessage);
		
		venueTickets.get(rowId).setAvailableSeatCount(oldCount + counter);
		
		seatHoldMap.remove(holdMapKey);
	}

	/**
	 * Update the status of the Seat back to available 
	 * and reset the associated email to null
	 * 
	 * @param seatHold
	 * @param seat
	 */
	private void updateSeatOnHoldTimeOut(SeatHold seatHold, Seat seat) {
		Seat seatToBeUpdated = venueTickets.get(seatHold.getRowId()).getSeats().get(seat.getSeatId());
		seatToBeUpdated.setStatus(Status.AVAILABLE);
		seatToBeUpdated.setEmailId(null);
	}
}
