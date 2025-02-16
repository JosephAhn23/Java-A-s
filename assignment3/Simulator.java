/**
 * @author Mehrdad Sabetzadeh, University of Ottawa
 *
 */
public class Simulator {

	/**
	 * Length of car plate numbers
	 */
	public static final int PLATE_NUM_LENGTH = 3;

	/**
	 * Number of seconds in one hour
	 */
	public static final int NUM_SECONDS_IN_1H = 3600;

	/**
	 * Maximum duration a car can be parked in the lot
	 */
	public static final int MAX_PARKING_DURATION = 8 * NUM_SECONDS_IN_1H;

	/**
	 * Total duration of the simulation in (simulated) seconds
	 */
	public static final int SIMULATION_DURATION = 24 * NUM_SECONDS_IN_1H;

	/**
	 * The probability distribution for a car leaving the lot based on the duration
	 * that the car has been parked in the lot
	 */
	public static final TriangularDistribution departurePDF = new TriangularDistribution(0, MAX_PARKING_DURATION / 2,
			MAX_PARKING_DURATION);

	/**
	 * The probability that a car would arrive at any given (simulated) second
	 */
	private Rational probabilityOfArrivalPerSec;

	/**
	 * The simulation clock. Initially the clock should be set to zero; the clock
	 * should then be incremented by one unit after each (simulated) second
	 */
	private int clock;

	/**
	 * Total number of steps (simulated seconds) that the simulation should run for.
	 * This value is fixed at the start of the simulation. The simulation loop
	 * should be executed for as long as clock < steps. When clock == steps, the
	 * simulation is finished.
	 */
	private int steps;

	/**
	 * Instance of the parking lot being simulated.
	 */
	private ParkingLot lot;

	/**
	 * Queue for the cars wanting to enter the parking lot
	 */
	private Queue<Spot> incomingQueue;

	/**
	 * Queue for the cars wanting to leave the parking lot
	 */
	private Queue<Spot> outgoingQueue;

	/**
	 * @param lot   is the parking lot to be simulated
	 * @param steps is the total number of steps for simulation
	 */
	public Simulator(ParkingLot lot, int perHourArrivalRate, int steps) {
	
		this.lot = lot;
        this.steps = steps;
        this.probabilityOfArrivalPerSec = new Rational(perHourArrivalRate, NUM_SECONDS_IN_1H);
        this.incomingQueue = new LinkedQueue<>();
        this.outgoingQueue = new LinkedQueue<>();
        this.clock = 0;
	}


	/**
	 * Simulate the parking lot for the number of steps specified by the steps
	 * instance variable
	 * NOTE: Make sure your implementation of simulate() uses peek() from the Queue interface.
	 */
	public void simulate() {
		while (clock < steps) {
			boolean addNewCar = RandomGenerator.eventOccurred(probabilityOfArrivalPerSec);
			if (addNewCar) {
				incomingQueue.enqueue(new Spot(RandomGenerator.generateRandomCar(PLATE_NUM_LENGTH), clock));
			}


			for (int i = 0; i < lot.getOccupancy(); i++) {
				Spot spot = lot.getSpotAt(i);
				int duration = clock - spot.getTimestamp();
				boolean willLeave = false;
		
				if (duration > 8 * NUM_SECONDS_IN_1H) {
					willLeave = true;
				} else {
					willLeave = RandomGenerator.eventOccurred(departurePDF.pdf(duration));
				}
		
				if (willLeave) {
					lot.remove(i);
					spot.setTimestamp(clock);
					outgoingQueue.enqueue(spot);
					i--;
				}
			}
			
			if (!incomingQueue.isEmpty()) {
				Spot incomingToProcess = incomingQueue.peek();
				try {
					lot.park(incomingToProcess.getCar(), clock);
					
					incomingQueue.dequeue();
				} catch (IllegalStateException e) {
	
				}
			}
			clock++;
		}
	}
	

	public int getIncomingQueueSize() {
        return incomingQueue.size();
    }

}