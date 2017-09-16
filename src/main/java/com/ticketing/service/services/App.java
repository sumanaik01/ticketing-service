package com.ticketing.service.services;

import com.ticketing.service.model.SeatHold;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String rows = args[0];
        String cols = args[1];
        String onHoldTimeOutSeconds = args[2];
        
        TicketServiceImpl impl = null;
        if(rows == null || cols == null || onHoldTimeOutSeconds == null){
        	impl = new TicketServiceImpl();
        }else{
        	impl = new TicketServiceImpl(Integer.valueOf(rows), Integer.valueOf(cols), Long.valueOf(onHoldTimeOutSeconds));
        }
        
        System.out.println("Just after initialization, the venue seat matrix looks like this. A-Available,O-On hold,R-Reserved");
        impl.printVenueStatus();
        System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
        
        System.out.println("*****************************************************");
        System.out.println("Hold 17 seats, more than total number of seats in the venue");
        SeatHold sh1 = impl.findAndHoldSeats(17, "B");
        System.out.println("Error Message: " + sh1.getErrorMessage());
        
        
        System.out.println("*****************************************************");
        System.out.println("Hold 6 seats, more than total number of seats in a row");
        SeatHold sh2 = impl.findAndHoldSeats(6, "C");
        System.out.println("Error Message: " + sh2.getErrorMessage());
        
        
        System.out.println("*****************************************************");
        System.out.println("Hold and reserve 2 seats");
        SeatHold sh = impl.findAndHoldSeats(2, "A");
        impl.reserveSeats(sh.getSeatHoldId(), "A");
        impl.printVenueStatus();
        System.out.println("Total Seats Available: " + impl.numSeatsAvailable());
        
        System.out.println("*****************************************************");
        System.out.println("Hold 2 seats but do not reserve");
        SeatHold sh3 = impl.findAndHoldSeats(2, "D");
        System.out.println("Before timeout");
        impl.printVenueStatus();
        
        try        
        {
            Thread.sleep(6000);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        System.out.println("After timeout");
        impl.printVenueStatus();
        
        
        scenario6(impl);
        
        scenario7(impl);
        
        concurrencyScenario(impl);
        
        }

	private static void scenario6(TicketServiceImpl impl) {
		System.out.println("*****************************************************");
        System.out.println("Hold 1 Seat, do not reserve");
        SeatHold sh4 = impl.findAndHoldSeats(1, "E");
        System.out.println("Hold another Seat and reserve");
        SeatHold sh5 = impl.findAndHoldSeats(1, "F");
        impl.reserveSeats(sh5.getSeatHoldId(), "F");
        
        System.out.println("Before timeout");
        impl.printVenueStatus();
        
        try        
        {
            Thread.sleep(6000);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        System.out.println("After timeout");
        impl.printVenueStatus();
        System.out.println("Hold 2 Seats and reserve");
        SeatHold sh6 = impl.findAndHoldSeats(2, "G");
        impl.reserveSeats(sh6.getSeatHoldId(), "G");
        
        System.out.println("2 Seats get blocked in the next row");
        impl.printVenueStatus();
	}
	
	private static void scenario7(TicketServiceImpl impl) {
		System.out.println("*****************************************************");
        System.out.println("Hold 1 Seat, do not reserve");
        SeatHold sh4 = impl.findAndHoldSeats(1, "E");
        System.out.println("Hold another Seat and reserve");
        SeatHold sh5 = impl.findAndHoldSeats(1, "F");
        impl.reserveSeats(sh5.getSeatHoldId(), "F");
        
        System.out.println("Before timeout");
        impl.printVenueStatus();
        
        try        
        {
            Thread.sleep(6000);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        System.out.println("After timeout");
        impl.printVenueStatus();
        System.out.println("Hold 1 Seats and reserve");
        SeatHold sh6 = impl.findAndHoldSeats(1, "G");
        impl.reserveSeats(sh6.getSeatHoldId(), "G");
        
        System.out.println("The previously held and relesaed seat gets booked");
        impl.printVenueStatus();
	}
	
	private static void concurrencyScenario(TicketServiceImpl impl) {
		System.out.println("*****************************************************");
		System.out.println("Concurrency: 2 threads hold 2 and 3 seats each simulataneous");
		Runnable task1 = () -> { SeatHold sh = impl.findAndHoldSeats(2, "Concurrent"); };
        Runnable task2 = () -> { SeatHold sh56 = impl.findAndHoldSeats(3, "Concurrent11"); };
        
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
        System.out.println("Before timeout");
        impl.printVenueStatus();
        System.out.println("Two different set of seats are put on hold by each thread.");
        
        try        
        {
            Thread.sleep(6000);
        } 
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
        System.out.println("After timeout");
        impl.printVenueStatus();
        System.out.println("The appropriate seats are released back to the pool by both threads.");
	}
}
