package com.ticketing.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Venue {
	
	public static Map<Integer, SeatHold> seatHoldMap = Collections.synchronizedMap(new HashMap<>());

	public static List<Row> venueTickets = Collections.synchronizedList(new ArrayList<>());

}
