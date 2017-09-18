package com.ticketing.service.services;

import org.junit.Before;
import org.junit.Test;

import com.ticketing.service.model.SeatHold;
import com.ticketing.service.model.Status;
import com.ticketing.service.model.Venue;

import junit.framework.TestCase;

public class TicketServiceImplTest extends TestCase  {

	TicketServiceImpl ticketService;
	
	private static final Long onHoldTimeOut = 2000L;
	private static final Long threadSleepTime = 3000L;
	
	@Before
	public void setUp() {
		ticketService = new TicketServiceImpl(4, 4, onHoldTimeOut);
	}

	@Test
	public void testNumSeatsAvailable(){
		assertEquals(16, ticketService.numSeatsAvailable());
		
		//Make sure the first seat of first row is on hold
		assertStatus(0,0,Status.AVAILABLE);
	}
	
	@Test
	public void testFindAndHoldSeatsOverbooking(){
		SeatHold seatHold = ticketService.findAndHoldSeats(20, "customerEmail");
		assertTrue(seatHold.isError());
		assertEquals(TicketServiceImpl.TICKETS_ERROR_TOO_MANY_REQUESTED.replace("{0}", "20"), seatHold.getErrorMessage());
		
	}
	
	@Test
	public void testFindAndHoldSeatsNoConsecutive(){
		SeatHold seatHold = ticketService.findAndHoldSeats(5, "customerEmail");
		assertTrue(seatHold.isError());
		assertEquals(TicketServiceImpl.TICKET_ERROR_CONSECUTIVE_NOT_AVAILABLE.replace("{0}", "5"), seatHold.getErrorMessage());
		
	}
	
	@Test
	public void testFindAndHoldSeats(){
		SeatHold seatHold = ticketService.findAndHoldSeats(3, "customerEmail");
		assertFalse(seatHold.isError());
		
		//Make sure the first 3 seats of first row are on hold
		assertStatus(0,0,Status.ON_HOLD);
		assertStatus(0,1,Status.ON_HOLD);
		assertStatus(0,2,Status.ON_HOLD);
	}
	
	
	@Test
	public void testReserveSeats(){
		SeatHold seatHold = ticketService.findAndHoldSeats(3, "customerEmail");
		assertFalse(seatHold.isError());
		
		String message = ticketService.reserveSeats(seatHold.getSeatHoldId(), "customerEmail");
		
		//Make sure the first 3 seats of first row are reserved
		assertStatus(0,0,Status.RESERVED);
		assertStatus(0,1,Status.RESERVED);
		assertStatus(0,2,Status.RESERVED);
		assertEquals(TicketServiceImpl.TICKET_MESSAGE_SUCCESS, message);
	}
	
	@Test
	public void testReserveSeatsEmailMismatch(){
		SeatHold seatHold = ticketService.findAndHoldSeats(3, "customerEmail");
		assertFalse(seatHold.isError());
		
		String message = ticketService.reserveSeats(seatHold.getSeatHoldId(), "someOtherEmailId");
		
		//Make sure the seats are on hold
		assertStatus(0,0,Status.ON_HOLD);
		assertStatus(0,1,Status.ON_HOLD);
		assertStatus(0,2,Status.ON_HOLD);
		assertEquals(TicketServiceImpl.TICKET_MESSAGE_EMAIL_MISMATCH, message);
	}
	
	@Test
	public void testFindSeatHoldAndReserveReserveSeatsConcurrent(){
		
		Runnable task1 = () -> { 
			SeatHold seatHold = ticketService.findAndHoldSeats(2, "A2"); 
			ticketService.reserveSeats(seatHold.getSeatHoldId(), "A2"); };
			
        Runnable task2 = () -> { SeatHold seatHold = ticketService.findAndHoldSeats(2, "B2"); 
		ticketService.reserveSeats(seatHold.getSeatHoldId(), "B2"); };
        
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
		
		
		//Make sure the first 3 seats of first row are reserved
		assertStatus(0,0,Status.RESERVED);
		assertStatus(0,1,Status.RESERVED);
		assertStatus(0,2,Status.RESERVED);
		assertStatus(0,3,Status.RESERVED);
	}
	
	@Test
	public void testFindAndHoldSeatsNotReservedWithinTimeOut(){
		SeatHold seatHold = ticketService.findAndHoldSeats(3, "customerEmail");
		assertFalse(seatHold.isError());
		
		//Make sure the first 3 seats of first row are on hold
		assertStatus(0,0,Status.ON_HOLD);
		assertStatus(0,1,Status.ON_HOLD);
		assertStatus(0,2,Status.ON_HOLD);
		assertEquals(13, ticketService.numSeatsAvailable());
		
		try        
        {
            Thread.sleep(threadSleepTime);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
		String message = ticketService.reserveSeats(seatHold.getSeatHoldId(), "customerEmail");
		
		//Make sure the first 3 seats of first row are available again
		assertStatus(0,0,Status.AVAILABLE);
		assertStatus(0,1,Status.AVAILABLE);
		assertStatus(0,2,Status.AVAILABLE);
		assertEquals(16, ticketService.numSeatsAvailable());
		assertEquals(TicketServiceImpl.TICKET_MESSAGE_UNSUCCESSFUL, message);
	}
	
	@Test
	public void testFindAndHoldSeatsAvailableSeatsReservedAgain(){
		ticketService.findAndHoldSeats(3, "A1");
		
		SeatHold seatHold2 = ticketService.findAndHoldSeats(1, "B1");
		ticketService.reserveSeats(seatHold2.getSeatHoldId(), "B1");
		
		
		//Make sure the first 3 seats of first row are on hold
		assertStatus(0,0,Status.ON_HOLD);
		assertStatus(0,1,Status.ON_HOLD);
		assertStatus(0,2,Status.ON_HOLD);
		assertEquals(12, ticketService.numSeatsAvailable());
		
		//Make sure the the 4th seat of first row is reserved
		assertStatus(0,3,Status.RESERVED);
		
		try        
        {
            Thread.sleep(threadSleepTime);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
		
		//Make sure the first 3 seats of first row are available again
		assertStatus(0,0,Status.AVAILABLE);
		assertStatus(0,1,Status.AVAILABLE);
		assertStatus(0,2,Status.AVAILABLE);
		assertEquals(15, ticketService.numSeatsAvailable());
		
		// 3 tickets are booked after the previous set of on hold tickets became available
		SeatHold seatHold3 = ticketService.findAndHoldSeats(3, "c@example.com");
		ticketService.reserveSeats(seatHold3.getSeatHoldId(), "c@example.com");
		
		
		// The first 3 tickets from row 1 are booked 
		assertStatus(0,0,Status.RESERVED);
		assertStatus(0,1,Status.RESERVED);
		assertStatus(0,2,Status.RESERVED);
		assertEquals(12, ticketService.numSeatsAvailable());
	}
	
	@Test
	public void testFindAndHoldSeatsAvailableSeatsNotReserved(){
		ticketService.findAndHoldSeats(3, "A2");
		
		SeatHold seatHold2 = ticketService.findAndHoldSeats(1, "B2");
		ticketService.reserveSeats(seatHold2.getSeatHoldId(), "B2");
		
		
		//Make sure the first 3 seats of first row are on hold
		assertStatus(0,0,Status.ON_HOLD);
		assertStatus(0,1,Status.ON_HOLD);
		assertStatus(0,2,Status.ON_HOLD);
		assertEquals(12, ticketService.numSeatsAvailable());
		
		//Make sure the the 4th seat of first row is reserved
		assertStatus(0,3,Status.RESERVED);
		
		try        
        {
            Thread.sleep(threadSleepTime);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
		
		//Make sure the first 3 seats of first row are available again
		assertStatus(0,0,Status.AVAILABLE);
		assertStatus(0,1,Status.AVAILABLE);
		assertStatus(0,2,Status.AVAILABLE);
		assertEquals(15, ticketService.numSeatsAvailable());
		
		// 4 tickets are booked after the previous set of on hold tickets became available
		SeatHold seatHold3 = ticketService.findAndHoldSeats(4, "C1");
		ticketService.reserveSeats(seatHold3.getSeatHoldId(), "C1");
		
		
		// The first 3 available seats from row 1 cannot be reserved as the 4th seat is taken
		// 4 Seats from next available row will be reserved
		
		assertStatus(0,0,Status.AVAILABLE);
		assertStatus(0,1,Status.AVAILABLE);
		assertStatus(0,2,Status.AVAILABLE);
		
		assertStatus(1,0,Status.RESERVED);
		assertStatus(1,1,Status.RESERVED);
		assertStatus(1,2,Status.RESERVED);
		assertStatus(1,3,Status.RESERVED);
		assertEquals(11, ticketService.numSeatsAvailable());
	}
	
	
	
    private void assertStatus(int rowId, int seatId, Status status)
    {
    	assertEquals(status, Venue.venueTickets.get(rowId).getSeats().get(seatId).getStatus());
    }
	
}
