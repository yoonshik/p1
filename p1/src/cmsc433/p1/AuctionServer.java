package cmsc433.p1;

/**
 *  @author YOUR NAME SHOULD GO HERE
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected. 
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	private int soldItemsCount = 0;
	private int revenue = 0;

	public int soldItemsCount()
	{
		return this.soldItemsCount;
	}

	public int revenue()
	{
		return this.revenue;
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.


	/* Statistic variables and server constants: End code you should likely leave alone. */



	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */

	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();


	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1; 

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 

	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();



	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	// private Object instanceLock = new Object(); 

	private Object lastListingIDLock = new Object();
	private Object revenueLock = new Object();
	private Object submitBidLock = new Object();


	/*
	 *  The code from this point forward can and should be changed to correctly and safely 
	 *  implement the methods as needed to create a working multi-threaded server for the 
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */


	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */
	public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{
		synchronized(submitBidLock) {
			synchronized(itemsUpForBidding) {
				//   Make sure there's room in the auction site.
				if (itemsUpForBidding.size() >= serverCapacity) {
					//					System.out.println("itemsUpForBidding.size() " + itemsUpForBidding.size() +  " > serverCapacity");
					return -1;
				}
			}

			synchronized(itemsPerSeller) {
				//   If the seller is a new one, add them to the list of sellers.
				if (!itemsPerSeller.containsKey(sellerName)) {
					itemsPerSeller.put(sellerName,  0);
				}

				//   If the seller has too many items up for bidding, don't let them add this one.			
				if (itemsPerSeller.get(sellerName) >= maxSellerItems) {
					//					System.out.println("itemsPerSeller.get(sellerName) > maxSellerItems");
					return -1;
				}

				//   Don't forget to increment the number of things the seller has currently listed.
				itemsPerSeller.put(sellerName, itemsPerSeller.get(sellerName)+1);
			}

			// CAUTION: not synchronizing newItem
			synchronized(lastListingIDLock) {
				Item newItem = new Item(sellerName, itemName, ++lastListingID, lowestBiddingPrice, biddingDurationMs);
				synchronized(itemsAndIDs) {
					itemsAndIDs.put(lastListingID, newItem);
				}

				synchronized(itemsUpForBidding) {
					itemsUpForBidding.add(newItem);
				}
			}
			System.out.println((lastListingID) + " submitted. Minimum " + lowestBiddingPrice);
			return lastListingID;
		}
	}



	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{	
		synchronized(submitBidLock) {
			// Some reminders:
			//    Don't forget that whatever you return is now outside of your control.
			ArrayList<Item> itemsCopy;
			synchronized(itemsUpForBidding) {
				itemsCopy = new ArrayList<Item>(itemsUpForBidding);
			}
			return itemsCopy;
		}
	}


	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	public boolean submitBid(String bidderName, int listingID, int biddingAmount)
	{

		synchronized(submitBidLock) {
			//   See if the item exists.
			Item listing;
			synchronized(itemsAndIDs) {
				if (!itemsAndIDs.containsKey(listingID)) {
					return false;
				}
				listing = itemsAndIDs.get(listingID);
			}

			//   See if it can be bid upon. TODO? or done?
			synchronized(listing) {
				if (biddingAmount < listing.lowestBiddingPrice()) {
					return false;
				}
			}

			//	See if the item is no longer for sale
			synchronized(itemsUpForBidding) {
				if (!itemsUpForBidding.contains(listing)) {
					return false;
				}
			}

			//   See if this bidder has too many items in their bidding list.
			synchronized(itemsPerBuyer) {
				//TODO: not sure if i should be checking for 0
				if (!itemsPerBuyer.containsKey(bidderName)) {
					itemsPerBuyer.put(bidderName, 0); 
				}

				if (itemsPerBuyer.get(bidderName) > maxBidCount) {
					return false;
				}
			}

			String currentBidder;
			//   Get current bidding info.
			synchronized(highestBidders) {
				if (!highestBidders.containsKey(listingID)) {
					currentBidder = null;
				} else {
					currentBidder = highestBidders.get(listingID);
					//   See if they already hold the highest bid.
					if (currentBidder.equals(bidderName) ) {
						return false;
					}
				}

			}

			boolean formerBidExists = true;
			synchronized(highestBids) {

				if (highestBids.containsKey(listingID)) {
					int currentBid = highestBids.get(listingID);

					//   See if the new bid isn't better than the existing/opening bid floor.
					if (biddingAmount <= currentBid) { 
						return false;
					}
					System.out.println(listingID + ": current " + currentBid + ", bid " + biddingAmount + " bid amount good");
				} else {
					formerBidExists = false;
				}
			}

			//   Decrement the former winning bidder's count
			if (formerBidExists) {
				synchronized(itemsPerBuyer) {
					itemsPerBuyer.put(currentBidder, itemsPerBuyer.get(currentBidder)-1);
				}
			}


			//   Put your bid in place
			synchronized(highestBidders){
				highestBidders.put(listingID, bidderName);
			}
			synchronized(highestBids){
				highestBids.put(listingID, biddingAmount);
			}
			//			System.out.println(listingID + ": minimum " + listing.lowestBiddingPrice() + ", bid " + biddingAmount + " bid success");

			return true;
		}

	}

	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 2 (open) if this <code>Item</code> is still up for auction<br>
	 * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public int checkBidStatus(String bidderName, int listingID)
	{
		synchronized(submitBidLock) {
			Item item;
			boolean biddingOpen;
			//   If the bidding is closed, clean up for that item.
			synchronized(itemsAndIDs) {
				item = itemsAndIDs.get(listingID);
				biddingOpen = item.biddingOpen();
			}



			String highestBidder = null;

			int value;
			boolean bidExists = true;
			//     Remove item from the list of things up for bidding.
			synchronized(highestBids) {
				if (highestBids.containsKey(listingID)) {
					value = highestBids.remove(listingID);
				} else {
					value = -1;
					bidExists = false;
				}
			}
			synchronized(highestBidders) {
				if (highestBidders.containsKey(listingID)) {
					highestBidder = highestBidders.remove(listingID);
				} else {
					bidExists = false;
				}
			}

			if (biddingOpen) {
				return 2; //open
			}
			if (!bidExists) {
				return 3; //failed
			}

			//     Decrease the count of items being bid on by the winning bidder if there was any...
			synchronized(itemsPerBuyer) {
				if (highestBidder != null) {
					itemsPerBuyer.put(highestBidder, itemsPerBuyer.get(highestBidder)-1);
				} else {
					System.out.println("ERROR: This should not be running");
					return 3;
				}
			}

			//     Update the number of open bids for this seller
			synchronized(itemsPerSeller) {
				itemsPerSeller.put(item.seller(), itemsPerSeller.get(item.seller())-1);
			}


			if (value != -1) {
				synchronized(revenueLock) {
					revenue += value;
				}
			}

			return 1;	
		}
	}

	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if no bid has been made,
	 * -1 if no <code>Item</code> exists
	 */
	public int itemPrice(int listingID)
	{
		synchronized(submitBidLock) {
			synchronized(highestBids) {
				if (!highestBids.containsKey(listingID)) {
					return -1;
				}
				return highestBids.get(listingID);
			}
		}
	}

	/**
	 * Check whether an <code>Item</code> has been bid upon yet
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public Boolean itemUnbid(int listingID)
	{
		synchronized(submitBidLock) {
			boolean inBid = true;

			Item current;
			synchronized(itemsAndIDs) {
				current = itemsAndIDs.get(listingID);
				if (current == null) {
					inBid = false;
				}
			}

			synchronized(itemsUpForBidding) {
				return inBid && itemsUpForBidding.contains(current);
			}
		}
	}


}
