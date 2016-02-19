package tests;

import cmsc433.p1.AuctionServer;
import cmsc433.p1.Bidder;
import cmsc433.p1.Item;
import cmsc433.p1.Seller;
import org.junit.*;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class ServerTestMulti3
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Before
	public void setUp() throws Exception
	{
		// Before each test, reset the Server, by re-initializing its instance. 
		Constructor<ServerPrinter> serverConstructor = ServerPrinter.class.getDeclaredConstructor((Class<ServerPrinter>[])null);
		serverConstructor.setAccessible(true);

		Field serverInstance = AuctionServer.class.getDeclaredField("instance");
		serverInstance.setAccessible(true);

		serverInstance.set(null, serverConstructor.newInstance((Object[])null));
	}

	@After
	public void tearDown() throws Exception
	{
	}

	private void testMultiThread(int testNumber, int nrSellers, int nrBuyers)
	{   
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);

		Logger.getInstance().setOutput(ps);

		Thread[] sellerThreads = new Thread[nrSellers];
		Thread[] buyerThreads = new Thread[nrBuyers];
		Seller[] sellers = new Seller[nrSellers];
		Bidder[] buyers = new Bidder[nrBuyers];

		for (int i=0; i<nrSellers; ++i)
		{
			AuctionServer serverPrinter = AuctionServer.getInstance();

			sellers[i] = new Seller(
					/* server = */          serverPrinter, 
					/* sellerName = */      "Seller"+i, 
					/* cycles = */          100, 
					/* maxSleepTimeMs = */  2, 
					/* randomSeed = */      i);
			sellerThreads[i] = new Thread(sellers[i]);
			sellerThreads[i].start();
		}




		for (int i=0; i<nrBuyers; ++i)
		{
			buyers[i] = new Bidder(
					/* server = */          AuctionServer.getInstance(),
					/* buyerName = */       "Buyer"+i, 
					/* initialCash = */     1000, 
					/* cycles = */          20, 
					/* maxSleepTimeMs = */  2, 
					/* randomSeed = */      i);
			buyerThreads[i] = new Thread(buyers[i]);
			buyerThreads[i].start();
		}




		for (int i=0; i<nrSellers; ++i)
		{
			try
			{
				sellerThreads[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		int moneySpent = 0;
		for (int i=0; i<nrBuyers; ++i)
		{
			try
			{
				buyerThreads[i].join();
				moneySpent += buyers[i].cashSpent();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}






		//        String output = os.toString();
		//        
		//        FileWriter writer;
		//        try
		//        {
		//            // Output the log of the test in a separate file, for ease of debugging.
		//            writer = new FileWriter("out" + testNumber + ".txt");
		//            writer.write(output);
		//            writer.close();
		//        }
		//        catch (IOException e)
		//        {
		//            e.printStackTrace();
		//        }

		assertFalse("You don't have any recording of how much was spent.", moneySpent==0);
		assertTrue("For test number " + testNumber + " the server revenue " + AuctionServer.getInstance().revenue() + " differs from the revenue reported by the buyers " + moneySpent + "!", moneySpent == AuctionServer.getInstance().revenue());
		for (int i=0; i<buyers.length; ++i)
		{
			assertTrue("For test number " + testNumber + " the server items capacity " + buyers[i].mostItemsAvailable() + " exceeds the limit of " + AuctionServer.serverCapacity + "!", buyers[i].mostItemsAvailable() <= AuctionServer.serverCapacity);
		}
	}

	@Test
	public void testMultiThread0() throws Exception
	{
		setUp();
		testMultiThread(0, 80, 80);
	}

	@Test
	public void testMultiThread1() throws Exception
	{
		setUp();
		testMultiThread(1, 80, 160);
	}

	@Test
	public void testMultiThread2() throws Exception
	{
		setUp();
		testMultiThread(2, 80, 240);
	}

	@Test
	public void testMultiThread3() throws Exception
	{
		setUp();
		testMultiThread(3, 80, 320);
	}

	@Test
	public void testMultiThread4() throws Exception
	{
		testMultiThread(4, 20, 200);

	}

	@Test
	public void testMultiThread5() throws Exception {
		setUp();
		AuctionServer server = AuctionServer.getInstance();
		Seller s = new Seller(server, "seller1", 5, 1, 1);
		Bidder[] buyers = new Bidder[2];
		Thread[] buyerThreads = new Thread[2];
		Thread sellerThread = new Thread(s);
		sellerThread.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i=0; i<2; ++i)
		{
			buyers[i] = new Bidder(
					/* server = */          AuctionServer.getInstance(),
					/* buyerName = */       "Buyer"+i, 
					/* initialCash = */     1000, 
					/* cycles = */          10, 
					/* maxSleepTimeMs = */  2, 
					/* randomSeed = */      i);
			buyerThreads[i] = new Thread(buyers[i]);
			buyerThreads[i].start();
		}

		try
		{
			sellerThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		int moneySpent = 0;
		for (int i=0; i<2; ++i)
		{
			try
			{
				buyerThreads[i].join();
				moneySpent += buyers[i].cashSpent();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		assertTrue("No items should be sold " + moneySpent, moneySpent==0);
		assertTrue("For test number " + 5 + " the server revenue " + AuctionServer.getInstance().revenue() + " differs from the revenue reported by the buyers " + moneySpent + "!", moneySpent == AuctionServer.getInstance().revenue());
		for (int i=0; i<buyers.length; ++i)
		{
			assertTrue("For test number " + 5 + " the server items capacity " + buyers[i].mostItemsAvailable() + " exceeds the limit of " + AuctionServer.serverCapacity + "!", buyers[i].mostItemsAvailable() <= AuctionServer.serverCapacity);
		}
		assertTrue("Total items sold ", AuctionServer.getInstance().soldItemsCount() == 0);


	}

	@Test
	public void testMultiThread6() throws Exception {
		setUp();
		AuctionServer server = AuctionServer.getInstance();
		Seller s = new Seller(server, "seller1", 5, 1, 1);
		Seller s2 = new Seller(server, "seller2", 5, 1, 1);
		Thread[] sellerThread = new Thread[2];
		sellerThread[0] = new Thread(s);
		sellerThread[1] = new Thread(s2);
		sellerThread[0].start();
		sellerThread[1].start();

		for (int i=0; i<2; ++i)
		{
			try
			{
				sellerThread[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		assertTrue("No items should be sold ", AuctionServer.getInstance().revenue()==0);

		assertTrue("Total items sold ", AuctionServer.getInstance().soldItemsCount() == 0);



	}
	

}
