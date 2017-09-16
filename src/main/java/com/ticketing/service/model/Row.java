package com.ticketing.service.model;

import java.util.ArrayList;
import java.util.List;

public class Row {
	
	int rowId;
	
	List<Seat> seats;
	
	int availableSeatCount;

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	public int getAvailableSeatCount() {
		return availableSeatCount;
	}

	public void setAvailableSeatCount(int availableSeatCount) {
		this.availableSeatCount = availableSeatCount;
	}

	
}
